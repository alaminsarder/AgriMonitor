package com.agrimonitor.dao;

import com.agrimonitor.db.MongoDBConnection;
import com.agrimonitor.model.Cost;
import com.agrimonitor.session.Session;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CostDAO {

    private MongoCollection<Document> collection;

    public CostDAO() {
        collection = MongoDBConnection.getDatabase().getCollection("costs");
        // Automatically demo data check korbe ebong na thakle add kore nibe
        addDemoData();
    }

    public boolean addCost(Cost cost) {
        try {
            Document doc = new Document("cropId", cost.getCropId())
                    .append("fieldId", cost.getFieldId())
                    .append("description", cost.getDescription())
                    .append("amount", cost.getAmount())
                    .append("date", cost.getDate())
                    .append("userId", "demo_user");
            collection.insertOne(doc);
            return true;
        } catch (Exception e) {
            System.out.println("Error adding cost record: " + e.getMessage());
            return false;
        }
    }

    // Shobai shudhu demo data-i dekhbe
    public List<Cost> getAllCosts() {
        List<Cost> list = new ArrayList<>();
        MongoCursor<Document> cursor = collection.find().iterator();
        try {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Cost cost = new Cost(
                        doc.getString("cropId"),
                        doc.getString("fieldId"),
                        doc.getString("description"),
                        doc.getDouble("amount"),
                        doc.getString("date")
                );
                cost.setUserId(doc.getString("userId"));
                list.add(cost);
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    public double getTotalCostByCropId(String cropId) {
        double total = 0.0;
        Bson filter = Filters.eq("cropId", cropId);
        MongoCursor<Document> cursor = collection.find(filter).iterator();
        try {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                total += doc.getDouble("amount");
            }
        } finally {
            cursor.close();
        }
        return total;
    }

    public double getTotalCostAll() {
        double total = 0.0;
        MongoCursor<Document> cursor = collection.find().iterator();
        try {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                total += doc.getDouble("amount");
            }
        } finally {
            cursor.close();
        }
        return total;
    }

    public Map<String, Double> getCostBreakdownByCategory() {
        Map<String, Double> breakdown = new LinkedHashMap<>();
        MongoCursor<Document> cursor = collection.find().iterator();
        try {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                String description = doc.getString("description");
                double amount = doc.getDouble("amount");
                breakdown.merge(description, amount, Double::sum);
            }
        } finally {
            cursor.close();
        }
        return breakdown;
    }

    // Auto Demo Data Add korar method
    public void addDemoData() {
        if (collection.countDocuments() == 0) {
            try {
                List<Document> demoDocs = new ArrayList<>();

                demoDocs.add(new Document("cropId", "CROP001")
                        .append("fieldId", "FIELD01")
                        .append("description", "Seeds")
                        .append("amount", 2500.0)
                        .append("date", "2026-04-10")
                        .append("userId", "demo_user"));

                demoDocs.add(new Document("cropId", "CROP001")
                        .append("fieldId", "FIELD01")
                        .append("description", "Fertilizer")
                        .append("amount", 1800.0)
                        .append("date", "2026-04-18")
                        .append("userId", "demo_user"));

                demoDocs.add(new Document("cropId", "CROP002")
                        .append("fieldId", "FIELD02")
                        .append("description", "Labor")
                        .append("amount", 3500.0)
                        .append("date", "2026-04-22")
                        .append("userId", "demo_user"));

                demoDocs.add(new Document("cropId", "CROP002")
                        .append("fieldId", "FIELD02")
                        .append("description", "Pesticide")
                        .append("amount", 1200.0)
                        .append("date", "2026-05-02")
                        .append("userId", "demo_user"));

                collection.insertMany(demoDocs);
                System.out.println("Demo data initialized successfully!");
            } catch (Exception e) {
                System.out.println("Error adding demo data: " + e.getMessage());
            }
        }
    }
}