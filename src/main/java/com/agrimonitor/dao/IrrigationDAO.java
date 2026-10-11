package com.agrimonitor.dao;

import com.agrimonitor.db.MongoDBConnection;
import com.agrimonitor.model.Irrigation;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.bson.Document;

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
                    .append("waterQuantity", irrigation.getWaterQuantity());
            collection.insertOne(doc);
            return true;
        } catch (Exception e) {
            System.out.println("Error adding irrigation record: " + e.getMessage());
            return false;
        }
    }

    public List<Irrigation> getAllIrrigations() {
        List<Irrigation> list = new ArrayList<>();
        MongoCursor<Document> cursor = collection.find().iterator();
        try {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Irrigation irrigation = new Irrigation(
                        doc.getString("fieldId"),
                        doc.getString("irrigationDate"),
                        doc.getDouble("waterQuantity")
                );
                list.add(irrigation);
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    public List<Irrigation> getIrrigationByFieldId(String fieldId) {
        List<Irrigation> list = new ArrayList<>();
        MongoCursor<Document> cursor = collection.find(Filters.eq("fieldId", fieldId)).iterator();
        try {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Irrigation irrigation = new Irrigation(
                        doc.getString("fieldId"),
                        doc.getString("irrigationDate"),
                        doc.getDouble("waterQuantity")
                );
                list.add(irrigation);
            }
        } finally {
            cursor.close();
        }
        return list;
    }
}