package com.agrimonitor.session;

public class Session {

    private static String currentUserId;
    private static String currentUsername;
    private static String currentEmail;

    public static void login(String userId, String username) {
        login(userId, username, null);
    }

    public static void login(String userId, String username, String email) {
        currentUserId = userId;
        currentUsername = username;
        currentEmail = email;
    }

    public static void logout() {
        currentUserId = null;
        currentUsername = null;
        currentEmail = null;
    }

    public static String getUserId() {
        return currentUserId;
    }

    public static String getUsername() {
        return currentUsername;
    }

    public static String getEmail() {
        return currentEmail;
    }

    public static boolean isLoggedIn() {
        return currentUserId != null;
    }
}