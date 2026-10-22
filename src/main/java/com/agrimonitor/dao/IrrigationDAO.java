package com.agrimonitor.dao;

import com.agrimonitor.db.MongoDBConnection;
import com.agrimonitor.model.Irrigation;
import com.agrimonitor.session.Session;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

public class IrrigationDAO {

    private MongoCollection<Document> collection;

    public IrrigationDAO() {
        collection = MongoDBConnection.getDatabase().getCollection("irrigation");
    }

    public boolean addIrrigation(Irrigation irrigation) {
        try {
            Document doc = new Document("fieldId", irrigation.getFieldId())
                    .append("irrigationDate", irrigation.getIrrigationDate())
                    .append("waterQuantity", irrigation.getWaterQuantity())
                    .append("userId", Session.getUserId());
            collection.insertOne(doc);
            return true;
        } catch (Exception e) {
            System.out.println("Error adding irrigation record: " + e.getMessage());
            return false;
        }
    }

    public List<Irrigation> getAllIrrigations() {
        List<Irrigation> list = new ArrayList<>();
        Bson filter = Filters.eq("userId", Session.getUserId());
        MongoCursor<Document> cursor = collection.find(filter).iterator();
        try {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Irrigation irrigation = new Irrigation(
                        doc.getString("fieldId"),
                        doc.getString("irrigationDate"),
                        doc.getDouble("waterQuantity")
                );
                irrigation.setUserId(doc.getString("userId"));
                list.add(irrigation);
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    public List<Irrigation> getIrrigationByFieldId(String fieldId) {
        List<Irrigation> list = new ArrayList<>();
        Bson filter = Filters.and(
                Filters.eq("fieldId", fieldId),
                Filters.eq("userId", Session.getUserId())
        );
        MongoCursor<Document> cursor = collection.find(filter).iterator();
        try {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Irrigation irrigation = new Irrigation(
                        doc.getString("fieldId"),
                        doc.getString("irrigationDate"),
                        doc.getDouble("waterQuantity")
                );
                irrigation.setUserId(doc.getString("userId"));
                list.add(irrigation);
            }
        } finally {
            cursor.close();
        }
        return list;
    }
}