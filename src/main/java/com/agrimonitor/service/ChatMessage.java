package com.agrimonitor.service;

import java.util.Objects;

public final class ChatMessage {

    public static final String USER = "user";
    public static final String ASSISTANT = "assistant";

    private final String role;
    private final String text;

    public ChatMessage(String role, String text) {
        String normalizedRole = Objects.requireNonNull(role, "Role must not be null").trim().toLowerCase();
        if (!USER.equals(normalizedRole) && !ASSISTANT.equals(normalizedRole)) {
            throw new IllegalArgumentException("Invalid role: " + role + ". Expected 'user' or 'assistant'.");
        }
        this.role = normalizedRole;
        this.text = Objects.requireNonNull(text, "Text must not be null");
    }

    public static ChatMessage user(String text) {
        return new ChatMessage(USER, text);
    }

    public static ChatMessage assistant(String text) {
        return new ChatMessage(ASSISTANT, text);
    }

    public String getRole() {
        return role;
    }

    public String getText() {
        return text;
    }

    public boolean isUser() {
        return USER.equals(role);
    }

    public boolean isAssistant() {
        return ASSISTANT.equals(role);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatMessage that)) return false;
        return role.equals(that.role) && text.equals(that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role, text);
    }

    @Override
    public String toString() {
        return "ChatMessage{role='" + role + "', text='" + text + "'}";
    }
}