package com.agrimonitor.dao;

import com.agrimonitor.db.MongoDBConnection;
import com.agrimonitor.model.Crop;
import com.agrimonitor.session.Session;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

public class CropDAO {

    private MongoCollection<Document> collection;

    public CropDAO() {
        collection = MongoDBConnection.getDatabase().getCollection("crops");
    }

    public boolean addCrop(Crop crop) {
        try {
            Document doc = new Document("cropId", crop.getCropId())
                    .append("cropName", crop.getCropName())
                    .append("cropType", crop.getCropType())
                    .append("growthDuration", crop.getGrowthDuration())
                    .append("fieldId", crop.getFieldId())
                    .append("userId", Session.getUserId());
            collection.insertOne(doc);
            return true;
        } catch (Exception e) {
            System.out.println("Error adding crop: " + e.getMessage());
            return false;
        }
    }

    public List<Crop> getAllCrops() {
        List<Crop> crops = new ArrayList<>();
        Bson filter = Filters.eq("userId", Session.getUserId());
        MongoCursor<Document> cursor = collection.find(filter).iterator();
        try {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Crop crop = new Crop(
                        doc.getString("cropId"),
                        doc.getString("cropName"),
                        doc.getString("cropType"),
                        doc.getInteger("growthDuration"),
                        doc.getString("fieldId")
                );
                crop.setUserId(doc.getString("userId"));
                crops.add(crop);
            }
        } finally {
            cursor.close();
        }
        return crops;
    }

    public Crop getCropById(String cropId) {
        Bson filter = Filters.and(
                Filters.eq("cropId", cropId),
                Filters.eq("userId", Session.getUserId())
        );
        Document doc = collection.find(filter).first();
        if (doc != null) {
            Crop crop = new Crop(
                    doc.getString("cropId"),
                    doc.getString("cropName"),
                    doc.getString("cropType"),
                    doc.getInteger("growthDuration"),
                    doc.getString("fieldId")
            );
            crop.setUserId(doc.getString("userId"));
            return crop;
        }
        return null;
    }

    public boolean updateCrop(String cropId, String cropName, String cropType, int growthDuration) {
        try {
            Bson filter = Filters.and(
                    Filters.eq("cropId", cropId),
                    Filters.eq("userId", Session.getUserId())
            );
            collection.updateOne(
                    filter,
                    Updates.combine(
                            Updates.set("cropName", cropName),
                            Updates.set("cropType", cropType),
                            Updates.set("growthDuration", growthDuration)
                    )
            );
            return true;
        } catch (Exception e) {
            System.out.println("Error updating crop: " + e.getMessage());
            return false;
        }
    }

    public boolean cropExists(String cropId) {
        Bson filter = Filters.and(
                Filters.eq("cropId", cropId),
                Filters.eq("userId", Session.getUserId())
        );
        return collection.find(filter).first() != null;
    }
}