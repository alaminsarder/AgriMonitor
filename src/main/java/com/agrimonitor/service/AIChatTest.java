package com.agrimonitor.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AIChatTest {

    private static final String SYSTEM_PROMPT =
            "You are a friendly farming assistant. Reply in the same language the farmer uses.";

    public static void main(String[] args) {
        AIChatService aiService = new AIChatService();

        System.out.printf("Provider: %s | Model: %s | Configured: %s%n",
                aiService.getProvider(),
                aiService.getModel(),
                aiService.isConfigured());

        if (!aiService.isConfigured()) {
            System.out.println("Put AI_API_KEY=... in the .env file in the project root and run again.");
            return;
        }

        runChatLoop(aiService);
    }

    private static void runChatLoop(AIChatService aiService) {
        List<ChatMessage> history = new ArrayList<>();

        System.out.println("Type your question (type 'exit' to quit).");

        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            while (true) {
                System.out.print("\nYou: ");
                if (!scanner.hasNextLine()) {
                    break;
                }

                String input = scanner.nextLine().trim();
                if ("exit".equalsIgnoreCase(input)) {
                    break;
                }
                if (input.isEmpty()) {
                    continue;
                }

                ChatMessage userMessage = new ChatMessage(ChatMessage.USER, input);
                history.add(userMessage);

                try {
                    String reply = aiService.chat(SYSTEM_PROMPT, history);
                    history.add(new ChatMessage(ChatMessage.ASSISTANT, reply));
                    System.out.println("\nAI: " + reply);
                } catch (Exception e) {
                    // Remove the failed user message so history stays consistent
                    history.remove(history.size() - 1);
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }
    }
}