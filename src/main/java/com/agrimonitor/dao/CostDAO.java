package com.agrimonitor.dao;

import com.agrimonitor.db.MongoDBConnection;
import com.agrimonitor.model.Cost;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class CostDAO {

    private MongoCollection<Document> collection;

    public CostDAO() {
        collection = MongoDBConnection.getDatabase().getCollection("costs");
    }

    public boolean addCost(Cost cost) {
        try {
            Document doc = new Document("cropId", cost.getCropId())
                    .append("fieldId", cost.getFieldId())
                    .append("description", cost.getDescription())
                    .append("amount", cost.getAmount())
                    .append("date", cost.getDate());
            collection.insertOne(doc);
            return true;
        } catch (Exception e) {
            System.out.println("Error adding cost record: " + e.getMessage());
            return false;
        }
    }

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
                list.add(cost);
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    public double getTotalCostByCropId(String cropId) {
        double total = 0.0;
        MongoCursor<Document> cursor = collection.find(Filters.eq("cropId", cropId)).iterator();
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
}