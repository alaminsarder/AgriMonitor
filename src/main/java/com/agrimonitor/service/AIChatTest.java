package com.agrimonitor.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AIChatTest {
    public static void main(String[] args) throws Exception {
        AIChatService ai = new AIChatService();
        System.out.println("Provider: " + ai.getProvider() + " | Model: " + ai.getModel()
                + " | Configured: " + ai.isConfigured());
        if (!ai.isConfigured()) {
            System.out.println("Put AI_API_KEY=... in the .env file in the project root and run again.");
            return;
        }
        String system = "You are a friendly farming assistant. Reply in the same language the farmer uses.";
        List<ChatMessage> history = new ArrayList<>();
        Scanner sc = new Scanner(System.in, "UTF-8");
        System.out.println("Type your question (type 'exit' to quit).");
        while (true) {
            System.out.print("\nYou: ");
            if (!sc.hasNextLine()) break;
            String line = sc.nextLine().trim();
            if (line.equalsIgnoreCase("exit")) break;
            if (line.isEmpty()) continue;
            history.add(new ChatMessage(ChatMessage.USER, line));
            try {
                String reply = ai.chat(system, history);
                history.add(new ChatMessage(ChatMessage.ASSISTANT, reply));
                System.out.println("\nAI: " + reply);
            } catch (Exception e) {
                history.remove(history.size() - 1);
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}
