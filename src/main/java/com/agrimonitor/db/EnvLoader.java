package com.agrimonitor.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class EnvLoader {

    private static final Map<String, String> VALUES = new HashMap<>();

    static {
        load();
    }

    private static void load() {
        for (String p : new String[]{".env", "../.env"}) {
            Path path = Paths.get(p).toAbsolutePath().normalize();
            if (Files.isRegularFile(path)) {
                try (BufferedReader r = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                    parse(r);
                    System.out.println("EnvLoader: loaded keys " + VALUES.keySet() + " from " + path);
                    return;
                } catch (IOException e) {
                    System.out.println("Could not read " + path + ": " + e.getMessage());
                }
            }
        }

        try (InputStream in = EnvLoader.class.getClassLoader().getResourceAsStream(".env")) {
            if (in != null) {
                parse(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
                System.out.println("EnvLoader: loaded keys " + VALUES.keySet() + " from classpath");
                return;
            }
        } catch (IOException e) {
            System.out.println("Could not load .env from classpath: " + e.getMessage());
        }

        System.out.println("EnvLoader: .env file not found. Working dir = "
                + Paths.get("").toAbsolutePath());
    }

    private static String unquote(String s) {
        s = s.trim();
        if (s.length() >= 2
                && ((s.startsWith("\"") && s.endsWith("\""))
                || (s.startsWith("'") && s.endsWith("'")))) {
            return s.substring(1, s.length() - 1).trim();
        }
        return s;
    }

    private static void parse(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.replace("\uFEFF", "").trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            line = unquote(line);

            if (line.startsWith("export ")) {
                line = line.substring(7).trim();
            }

            int idx = line.indexOf('=');
            if (idx <= 0) continue;

            String key = unquote(line.substring(0, idx));
            String value = unquote(line.substring(idx + 1));

            VALUES.put(key, value);
        }
    }

    public static String get(String key) {
        String v = VALUES.get(key);
        if (v == null || v.isEmpty()) {
            v = System.getenv(key);
        }
        return v;
    }
}