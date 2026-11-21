package com.agrimonitor.service;

public class ChatMessage {

    public static final String USER = "user";
    public static final String ASSISTANT = "assistant";

    private final String role;
    private final String text;

    public ChatMessage(String role, String text) {
        this.role = role;
        this.text = text;
    }

    public String getRole() { return role; }
    public String getText() { return text; }
    public boolean isUser() { return USER.equals(role); }
}
