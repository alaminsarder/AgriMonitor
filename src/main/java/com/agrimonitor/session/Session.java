package com.agrimonitor.session;

public class Session {

    private static String currentUserId;
    private static String currentUsername;

    public static void login(String userId, String username) {
        currentUserId = userId;
        currentUsername = username;
    }

    public static void logout() {
        currentUserId = null;
        currentUsername = null;
    }

    public static String getUserId() {
        return currentUserId;
    }

    public static String getUsername() {
        return currentUsername;
    }

    public static boolean isLoggedIn() {
        return currentUserId != null;
    }
}