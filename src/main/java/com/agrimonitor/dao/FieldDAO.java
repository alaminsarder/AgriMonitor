package com.agrimonitor.dao;

import com.agrimonitor.db.MongoDBConnection;
import com.agrimonitor.model.Field;
import com.agrimonitor.session.Session;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

public class FieldDAO {

    private MongoCollection<Document> collection;

    public FieldDAO() {
        collection = MongoDBConnection.getDatabase().getCollection("fields");
    }

    public boolean addField(Field field) {
        try {
            Document doc = new Document("fieldId", field.getFieldId())
                    .append("fieldName", field.getFieldName())
                    .append("location", field.getLocation())
                    .append("areaSize", field.getAreaSize())
                    .append("userId", Session.getUserId());
            collection.insertOne(doc);
            return true;
        } catch (Exception e) {
            System.out.println("Error adding field: " + e.getMessage());
            return false;
        }
    }

    public List<Field> getAllFields() {
        List<Field> fields = new ArrayList<>();
        Bson filter = Filters.eq("userId", Session.getUserId());
        MongoCursor<Document> cursor = collection.find(filter).iterator();
        try {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Field field = new Field(
                        doc.getString("fieldId"),
                        doc.getString("fieldName"),
                        doc.getString("location"),
                        doc.getDouble("areaSize")
                );
                field.setUserId(doc.getString("userId"));
                fields.add(field);
            }
        } finally {
            cursor.close();
        }
        return fields;
    }

    public Field getFieldById(String fieldId) {
        Bson filter = Filters.and(
                Filters.eq("fieldId", fieldId),
                Filters.eq("userId", Session.getUserId())
        );
        Document doc = collection.find(filter).first();
        if (doc != null) {
            Field field = new Field(
                    doc.getString("fieldId"),
                    doc.getString("fieldName"),
                    doc.getString("location"),
                    doc.getDouble("areaSize")
            );
            field.setUserId(doc.getString("userId"));
            return field;
        }
        return null;
    }

    public boolean updateField(String fieldId, String fieldName, String location, double areaSize) {
        try {
            Bson filter = Filters.and(
                    Filters.eq("fieldId", fieldId),
                    Filters.eq("userId", Session.getUserId())
            );
            collection.updateOne(
                    filter,
                    Updates.combine(
                            Updates.set("fieldName", fieldName),
                            Updates.set("location", location),
                            Updates.set("areaSize", areaSize)
                    )
            );
            return true;
        } catch (Exception e) {
            System.out.println("Error updating field: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteField(String fieldId) {
        Bson filter = Filters.and(
                Filters.eq("fieldId", fieldId),
                Filters.eq("userId", Session.getUserId())
        );
        DeleteResult result = collection.deleteOne(filter);
        return result.getDeletedCount() > 0;
    }

    public boolean fieldExists(String fieldId) {
        Bson filter = Filters.and(
                Filters.eq("fieldId", fieldId),
                Filters.eq("userId", Session.getUserId())
        );
        return collection.find(filter).first() != null;
    }
}