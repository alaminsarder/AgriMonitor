package com.agrimonitor.service;

import com.agrimonitor.db.EnvLoader;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AIChatService {

    private static final int CONNECT_TIMEOUT_MS = 15000;
    private static final int READ_TIMEOUT_MS = 60000;

    private final String provider;
    private final String apiKey;
    private final String model;
    private final String baseUrl;

    public AIChatService() {
        String p = EnvLoader.get("AI_PROVIDER");
        this.provider = (p == null || p.trim().isEmpty()) ? "gemini" : p.trim().toLowerCase();
        String k = EnvLoader.get("AI_API_KEY");
        this.apiKey = k == null ? "" : k.trim();

        String m = EnvLoader.get("AI_MODEL");
        if (m == null || m.trim().isEmpty()) {
            m = "gemini".equals(provider) ? "gemini-flash-latest" : "gpt-4o-mini";
        }
        this.model = m.trim();

        String b = EnvLoader.get("AI_BASE_URL");
        if (b == null || b.trim().isEmpty()) b = "https://api.openai.com/v1";
        b = b.trim();
        this.baseUrl = b.endsWith("/") ? b.substring(0, b.length() - 1) : b;
    }

    public boolean isConfigured() {
        return !apiKey.isEmpty() && !apiKey.startsWith("your-");
    }

    public String getProvider() { return provider; }
    public String getModel() { return model; }

    public String chat(String systemPrompt, List<ChatMessage> history) throws Exception {
        if (!isConfigured()) {
            throw new IllegalStateException(
                    "AI is not configured. Add AI_API_KEY to your .env file (see .env.example).");
        }
        if (history == null || history.isEmpty()) {
            throw new IllegalArgumentException("Nothing to send.");
        }
        List<ChatMessage> clean = sanitize(history);
        if (clean.isEmpty()) {
            throw new IllegalArgumentException("Nothing to send.");
        }
        if ("gemini".equals(provider)) return callGemini(systemPrompt, clean);
        return callOpenAiStyle(systemPrompt, clean);
    }

    private static List<ChatMessage> sanitize(List<ChatMessage> history) {
        List<ChatMessage> out = new ArrayList<>();
        for (ChatMessage m : history) {
            if (m == null || m.getText() == null || m.getText().trim().isEmpty()) continue;
            String t = m.getText().trim();
            if (t.startsWith("AI request failed") || t.startsWith("AI model not found")
                    || t.startsWith("AI limit reached") || t.startsWith("AI is not configured")
                    || t.startsWith("The AI ") || t.startsWith("Nothing to send")) {
                continue;
            }
            out.add(m);
        }
        while (!out.isEmpty() && !out.get(0).isUser()) {
            out.remove(0);
        }
        while (!out.isEmpty() && !out.get(out.size() - 1).isUser()) {
            out.remove(out.size() - 1);
        }
        return out;
    }

    private String callGemini(String systemPrompt, List<ChatMessage> history) throws Exception {
        try {
            return callGemini(systemPrompt, history, true);
        } catch (Exception e) {
            String m = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            if (m.contains("thinking") || m.contains("invalid argument") || m.contains("http 400")) {
                return callGemini(systemPrompt, history, false);
            }
            throw e;
        }
    }

    private String callGemini(String systemPrompt, List<ChatMessage> history, boolean noThinking) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent";

        JSONObject body = new JSONObject();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            body.put("system_instruction", new JSONObject()
                    .put("parts", new JSONArray().put(new JSONObject().put("text", systemPrompt))));
        }
        JSONArray contents = new JSONArray();
        for (ChatMessage m : history) {
            String role = m.isUser() ? "user" : "model";
            int n = contents.length();
            if (n > 0 && contents.getJSONObject(n - 1).optString("role").equals(role)) {
                JSONObject prev = contents.getJSONObject(n - 1);
                JSONObject part = prev.getJSONArray("parts").getJSONObject(0);
                part.put("text", part.optString("text") + "\n" + m.getText().trim());
            } else {
                contents.put(new JSONObject()
                        .put("role", role)
                        .put("parts", new JSONArray().put(new JSONObject().put("text", m.getText().trim()))));
            }
        }
        body.put("contents", contents);

        JSONObject gen = new JSONObject().put("temperature", 0.6).put("maxOutputTokens", 1500);
        if (noThinking) {
            gen.put("thinkingConfig", new JSONObject().put("thinkingBudget", 0));
        }
        body.put("generationConfig", gen);

        String json = post(url, body.toString(), "x-goog-api-key", apiKey);
        JSONObject res = new JSONObject(json);

        JSONArray cands = res.optJSONArray("candidates");
        if (cands == null || cands.length() == 0) {
            JSONObject fb = res.optJSONObject("promptFeedback");
            if (fb != null && fb.has("blockReason")) {
                throw new Exception("The AI could not answer this message (" + fb.optString("blockReason") + ").");
            }
            throw new Exception("The AI returned an empty answer. Please try again.");
        }
        JSONObject content = cands.getJSONObject(0).optJSONObject("content");
        JSONArray parts = content == null ? null : content.optJSONArray("parts");
        StringBuilder sb = new StringBuilder();
        if (parts != null) {
            for (int i = 0; i < parts.length(); i++) sb.append(parts.getJSONObject(i).optString("text", ""));
        }
        String out = sb.toString().trim();
        if (out.isEmpty()) throw new Exception("The AI returned an empty answer. Please try again.");
        return out;
    }

    private String callOpenAiStyle(String systemPrompt, List<ChatMessage> history) throws Exception {
        JSONArray messages = new JSONArray();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            messages.put(new JSONObject().put("role", "system").put("content", systemPrompt));
        }
        for (ChatMessage m : history) {
            messages.put(new JSONObject().put("role", m.isUser() ? "user" : "assistant").put("content", m.getText()));
        }
        JSONObject body = new JSONObject()
                .put("model", model)
                .put("messages", messages)
                .put("temperature", 0.6)
                .put("max_tokens", 1500);

        String json = post(baseUrl + "/chat/completions", body.toString(), "Authorization", "Bearer " + apiKey);
        JSONObject res = new JSONObject(json);
        JSONArray choices = res.optJSONArray("choices");
        if (choices == null || choices.length() == 0) throw new Exception("The AI returned an empty answer. Please try again.");
        String out = choices.getJSONObject(0).getJSONObject("message").optString("content", "").trim();
        if (out.isEmpty()) throw new Exception("The AI returned an empty answer. Please try again.");
        return out;
    }

    private String post(String urlString, String body, String authHeader, String authValue) throws Exception {
        int attempts = 0;
        while (true) {
            attempts++;
            HttpURLConnection c = (HttpURLConnection) new URL(urlString).openConnection();
            try {
                c.setRequestMethod("POST");
                c.setConnectTimeout(CONNECT_TIMEOUT_MS);
                c.setReadTimeout(READ_TIMEOUT_MS);
                c.setDoOutput(true);
                c.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                c.setRequestProperty(authHeader, authValue);
                try (OutputStream os = c.getOutputStream()) {
                    os.write(body.getBytes(StandardCharsets.UTF_8));
                }
                int code = c.getResponseCode();
                String text = read(code >= 400 ? c.getErrorStream() : c.getInputStream());
                if (code >= 200 && code < 300) return text;

                if ((code == 429 || code == 503) && attempts < 2) {
                    Thread.sleep(2500);
                    continue;
                }
                throw new Exception(friendlyError(code, text));
            } finally {
                c.disconnect();
            }
        }
    }

    private static String read(InputStream in) throws Exception {
        if (in == null) return "";
        try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) sb.append(line).append('\n');
            return sb.toString();
        }
    }

    private static String friendlyError(int code, String body) {
        String detail = "";
        try {
            JSONObject o = new JSONObject(body);
            JSONObject err = o.optJSONObject("error");
            if (err != null) detail = err.optString("message", "");
        } catch (Exception ignored) { }

        String msg;
        if (code == 400 && detail.toLowerCase().contains("api key")) msg = "The AI API key is invalid.";
        else if (code == 401 || code == 403) msg = "The AI API key was rejected. Check AI_API_KEY in .env.";
        else if (code == 404) msg = "AI model not found. Check AI_MODEL in .env.";
        else if (code == 429) msg = "AI limit reached (too many requests). Please wait a minute and try again.";
        else if (code >= 500) msg = "The AI server is busy right now. Please try again shortly.";
        else msg = "AI request failed (HTTP " + code + ").";
        return detail.isEmpty() ? msg : msg + "\n(" + detail + ")";
    }
}