package com.agrimonitor.dao;

import com.agrimonitor.db.MongoDBConnection;
import com.agrimonitor.service.ChatMessage;
import com.agrimonitor.session.Session;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/** Stores each farmer's chatbot conversation (collection "chat_messages"), private per user. */
public class ChatHistoryDAO {

    private final MongoCollection<Document> collection;

    public ChatHistoryDAO() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        collection = db == null ? null : db.getCollection("chat_messages");
    }

    /** Saves one question + its answer (timestamps 1 ms apart so the order is always right). */
    public boolean saveExchange(String userText, String assistantText) {
        if (collection == null || Session.getUserId() == null) return false;
        try {
            long t = System.currentTimeMillis();
            List<Document> docs = new ArrayList<>();
            docs.add(doc(ChatMessage.USER, userText, t));
            docs.add(doc(ChatMessage.ASSISTANT, assistantText, t + 1));
            collection.insertMany(docs);
            return true;
        } catch (Exception e) {
            System.out.println("Error saving chat: " + e.getMessage());
            return false;
        }
    }

    /** Last {@code limit} messages of the current user, oldest first. */
    public List<ChatMessage> loadRecent(int limit) {
        List<ChatMessage> list = new ArrayList<>();
        if (collection == null || Session.getUserId() == null) return list;
        try (MongoCursor<Document> cursor = collection
                .find(Filters.eq("userId", Session.getUserId()))
                .sort(Sorts.descending("createdAt"))
                .limit(limit)
                .iterator()) {
            while (cursor.hasNext()) {
                Document d = cursor.next();
                list.add(new ChatMessage(d.getString("role"), d.getString("text")));
            }
        } catch (Exception e) {
            System.out.println("Error loading chat: " + e.getMessage());
        }
        Collections.reverse(list);
        return list;
    }

    /** Deletes the current user's whole chat history. */
    public boolean clear() {
        if (collection == null || Session.getUserId() == null) return false;
        try {
            collection.deleteMany(Filters.eq("userId", Session.getUserId()));
            return true;
        } catch (Exception e) {
            System.out.println("Error clearing chat: " + e.getMessage());
            return false;
        }
    }

    private Document doc(String role, String text, long millis) {
        return new Document("userId", Session.getUserId())
                .append("role", role)
                .append("text", text)
                .append("createdAt", new Date(millis));
    }
}
