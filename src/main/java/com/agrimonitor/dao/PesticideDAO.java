package com.agrimonitor.dao;

import com.agrimonitor.db.MongoDBConnection;
import com.agrimonitor.model.Pesticide;
import com.agrimonitor.session.Session;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

public class PesticideDAO {

    private MongoCollection<Document> collection;

    public PesticideDAO() {
        collection = MongoDBConnection.getDatabase().getCollection("pesticides");
    }

    public boolean addPesticide(Pesticide pesticide) {
        try {
            Document doc = new Document("fieldId", pesticide.getFieldId())
                    .append("pesticideName", pesticide.getPesticideName())
                    .append("quantity", pesticide.getQuantity())
                    .append("applicationDate", pesticide.getApplicationDate())
                    .append("userId", Session.getUserId());
            collection.insertOne(doc);
            return true;
        } catch (Exception e) {
            System.out.println("Error adding pesticide record: " + e.getMessage());
            return false;
        }
    }

    public List<Pesticide> getAllPesticides() {
        List<Pesticide> list = new ArrayList<>();
        Bson filter = Filters.eq("userId", Session.getUserId());
        MongoCursor<Document> cursor = collection.find(filter).iterator();
        try {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Pesticide pesticide = new Pesticide(
                        doc.getString("fieldId"),
                        doc.getString("pesticideName"),
                        doc.getDouble("quantity"),
                        doc.getString("applicationDate")
                );
                pesticide.setUserId(doc.getString("userId"));
                list.add(pesticide);
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    public List<Pesticide> getPesticideByFieldId(String fieldId) {
        List<Pesticide> list = new ArrayList<>();
        Bson filter = Filters.and(
                Filters.eq("fieldId", fieldId),
                Filters.eq("userId", Session.getUserId())
        );
        MongoCursor<Document> cursor = collection.find(filter).iterator();
        try {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Pesticide pesticide = new Pesticide(
                        doc.getString("fieldId"),
                        doc.getString("pesticideName"),
                        doc.getDouble("quantity"),
                        doc.getString("applicationDate")
                );
                pesticide.setUserId(doc.getString("userId"));
                list.add(pesticide);
            }
        } finally {
            cursor.close();
        }
        return list;
    }
}