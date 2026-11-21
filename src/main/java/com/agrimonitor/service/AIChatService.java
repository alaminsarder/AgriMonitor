package com.agrimonitor.service;

import com.agrimonitor.db.EnvLoader;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AIChatService {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(60);
    private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final String DEFAULT_OPENAI_URL = "https://api.openai.com/v1";

    private static final Set<String> IGNORED_PREFIXES = Set.of(
            "AI request failed", "AI model not found", "AI limit reached",
            "AI is not configured", "The AI ", "Nothing to send"
    );

    private final String provider;
    private final String apiKey;
    private final String model;
    private final String baseUrl;
    private final HttpClient httpClient;

    public AIChatService() {
        this.provider = getEnvOrDefault("AI_PROVIDER", "gemini").toLowerCase();
        this.apiKey = getEnvOrDefault("AI_API_KEY", "");

        String defaultModel = "gemini".equals(provider) ? "gemini-flash-latest" : "gpt-4o-mini";
        this.model = getEnvOrDefault("AI_MODEL", defaultModel);

        String rawBaseUrl = getEnvOrDefault("AI_BASE_URL", DEFAULT_OPENAI_URL);
        this.baseUrl = rawBaseUrl.endsWith("/") ? rawBaseUrl.substring(0, rawBaseUrl.length() - 1) : rawBaseUrl;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
    }

    public boolean isConfigured() {
        return !apiKey.isEmpty() && !apiKey.startsWith("your-");
    }

    public String getProvider() { return provider; }
    public String getModel() { return model; }

    public String chat(String systemPrompt, List<ChatMessage> history) throws Exception {
        if (!isConfigured()) {
            throw new IllegalStateException("AI is not configured. Add AI_API_KEY to your .env file (see .env.example).");
        }

        List<ChatMessage> cleanHistory = sanitize(history);
        if (cleanHistory.isEmpty()) {
            throw new IllegalArgumentException("Nothing to send.");
        }

        return "gemini".equals(provider)
                ? callGeminiWithFallback(systemPrompt, cleanHistory)
                : callOpenAiStyle(systemPrompt, cleanHistory);
    }

    private List<ChatMessage> sanitize(List<ChatMessage> history) {
        if (history == null || history.isEmpty()) return List.of();

        List<ChatMessage> cleaned = new ArrayList<>();
        for (ChatMessage msg : history) {
            if (msg == null || msg.getText() == null) continue;
            String text = msg.getText().trim();
            if (text.isEmpty() || isSystemErrorMessage(text)) continue;
            cleaned.add(msg);
        }

        // Ensure conversation starts and ends with a user message
        while (!cleaned.isEmpty() && !cleaned.get(0).isUser()) {
            cleaned.remove(0);
        }
        while (!cleaned.isEmpty() && !cleaned.get(cleaned.size() - 1).isUser()) {
            cleaned.remove(cleaned.size() - 1);
        }
        return cleaned;
    }

    private boolean isSystemErrorMessage(String text) {
        return IGNORED_PREFIXES.stream().anyMatch(text::startsWith);
    }

    private String callGeminiWithFallback(String systemPrompt, List<ChatMessage> history) throws Exception {
        try {
            return callGemini(systemPrompt, history, true);
        } catch (ApiException e) {
            String msg = e.getMessage().toLowerCase();
            if (e.statusCode == 400 && (msg.contains("thinking") || msg.contains("invalid argument"))) {
                return callGemini(systemPrompt, history, false);
            }
            throw e;
        }
    }

    private String callGemini(String systemPrompt, List<ChatMessage> history, boolean disableThinking) throws Exception {
        String encodedModel = URLEncoder.encode(model, StandardCharsets.UTF_8);
        String url = GEMINI_BASE_URL + encodedModel + ":generateContent";

        JSONObject body = new JSONObject();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            body.put("system_instruction", new JSONObject()
                    .put("parts", new JSONArray().put(new JSONObject().put("text", systemPrompt))));
        }

        body.put("contents", buildGeminiContents(history));

        JSONObject genConfig = new JSONObject()
                .put("temperature", 0.6)
                .put("maxOutputTokens", 1500);

        if (disableThinking) {
            genConfig.put("thinkingConfig", new JSONObject().put("thinkingBudget", 0));
        }
        body.put("generationConfig", genConfig);

        String responseJson = sendPostRequest(url, body.toString(), "x-goog-api-key", apiKey);
        return parseGeminiResponse(responseJson);
    }

    private JSONArray buildGeminiContents(List<ChatMessage> history) {
        JSONArray contents = new JSONArray();
        for (ChatMessage msg : history) {
            String role = msg.isUser() ? "user" : "model";
            String text = msg.getText().trim();
            int lastIdx = contents.length() - 1;

            // Merge consecutive messages from the same role
            if (lastIdx >= 0 && contents.getJSONObject(lastIdx).optString("role").equals(role)) {
                JSONObject lastPart = contents.getJSONObject(lastIdx).getJSONArray("parts").getJSONObject(0);
                lastPart.put("text", lastPart.optString("text") + "\n" + text);
            } else {
                contents.put(new JSONObject()
                        .put("role", role)
                        .put("parts", new JSONArray().put(new JSONObject().put("text", text))));
            }
        }
        return contents;
    }

    private String parseGeminiResponse(String json) throws Exception {
        JSONObject res = new JSONObject(json);
        JSONArray candidates = res.optJSONArray("candidates");

        if (candidates == null || candidates.isEmpty()) {
            JSONObject feedback = res.optJSONObject("promptFeedback");
            if (feedback != null && feedback.has("blockReason")) {
                throw new Exception("The AI could not answer this message (" + feedback.optString("blockReason") + ").");
            }
            throw new Exception("The AI returned an empty answer. Please try again.");
        }

        JSONObject content = candidates.getJSONObject(0).optJSONObject("content");
        JSONArray parts = (content != null) ? content.optJSONArray("parts") : null;

        StringBuilder reply = new StringBuilder();
        if (parts != null) {
            for (int i = 0; i < parts.length(); i++) {
                reply.append(parts.getJSONObject(i).optString("text", ""));
            }
        }

        String result = reply.toString().trim();
        if (result.isEmpty()) {
            throw new Exception("The AI returned an empty answer. Please try again.");
        }
        return result;
    }

    private String callOpenAiStyle(String systemPrompt, List<ChatMessage> history) throws Exception {
        JSONArray messages = new JSONArray();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.put(new JSONObject().put("role", "system").put("content", systemPrompt));
        }
        for (ChatMessage msg : history) {
            messages.put(new JSONObject()
                    .put("role", msg.isUser() ? "user" : "assistant")
                    .put("content", msg.getText().trim()));
        }

        JSONObject body = new JSONObject()
                .put("model", model)
                .put("messages", messages)
                .put("temperature", 0.6)
                .put("max_tokens", 1500);

        String responseJson = sendPostRequest(baseUrl + "/chat/completions", body.toString(), "Authorization", "Bearer " + apiKey);
        JSONObject res = new JSONObject(responseJson);
        JSONArray choices = res.optJSONArray("choices");

        if (choices == null || choices.isEmpty()) {
            throw new Exception("The AI returned an empty answer. Please try again.");
        }

        String result = choices.getJSONObject(0).getJSONObject("message").optString("content", "").trim();
        if (result.isEmpty()) {
            throw new Exception("The AI returned an empty answer. Please try again.");
        }
        return result;
    }

    private String sendPostRequest(String url, String payload, String authHeader, String authValue) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(READ_TIMEOUT)
                .header("Content-Type", "application/json; charset=UTF-8")
                .header(authHeader, authValue)
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();

        int maxAttempts = 2;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int statusCode = response.statusCode();

            if (statusCode >= 200 && statusCode < 300) {
                return response.body();
            }

            if ((statusCode == 429 || statusCode == 503) && attempt < maxAttempts) {
                Thread.sleep(2500);
                continue;
            }

            throw new ApiException(statusCode, formatFriendlyError(statusCode, response.body()));
        }
        throw new Exception("Unexpected error during API request.");
    }

    private static String formatFriendlyError(int statusCode, String responseBody) {
        String detail = "";
        try {
            JSONObject err = new JSONObject(responseBody).optJSONObject("error");
            if (err != null) detail = err.optString("message", "");
        } catch (Exception ignored) { }

        String userMessage = switch (statusCode) {
            case 400 -> detail.toLowerCase().contains("api key")
                    ? "The AI API key is invalid."
                    : "AI request failed (HTTP 400).";
            case 401, 403 -> "The AI API key was rejected. Check AI_API_KEY in .env.";
            case 404 -> "AI model not found. Check AI_MODEL in .env.";
            case 429 -> "AI limit reached (too many requests). Please wait a minute and try again.";
            default -> (statusCode >= 500)
                    ? "The AI server is busy right now. Please try again shortly."
                    : "AI request failed (HTTP " + statusCode + ").";
        };

        return detail.isEmpty() ? userMessage : userMessage + "\n(" + detail + ")";
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String val = EnvLoader.get(key);
        return (val == null || val.isBlank()) ? defaultValue : val.trim();
    }

    private static class ApiException extends Exception {
        final int statusCode;

        ApiException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }
    }
}