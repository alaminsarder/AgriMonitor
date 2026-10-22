package com.agrimonitor.db;

import java.io.InputStream;
import java.util.Properties;

public class EnvLoader {

    public static String get(String key) {
        Properties props = new Properties();
        try (InputStream input = EnvLoader.class.getClassLoader().getResourceAsStream(".env")) {
            if (input != null) {
                props.load(input);
                return props.getProperty(key);
            }
        } catch (Exception e) {
            System.out.println("Could not load .env: " + e.getMessage());
        }
        return null;
    }
}