package com.agrimonitor.dao;

import com.agrimonitor.db.MongoDBConnection;
import com.agrimonitor.model.Field;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;

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
                    .append("areaSize", field.getAreaSize());
            collection.insertOne(doc);
            return true;
        } catch (Exception e) {
            System.out.println("Error adding field: " + e.getMessage());
            return false;
        }
    }

    public List<Field> getAllFields() {
        List<Field> fields = new ArrayList<>();
        MongoCursor<Document> cursor = collection.find().iterator();
        try {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Field field = new Field(
                        doc.getString("fieldId"),
                        doc.getString("fieldName"),
                        doc.getString("location"),
                        doc.getDouble("areaSize")
                );
                fields.add(field);
            }
        } finally {
            cursor.close();
        }
        return fields;
    }

    public Field getFieldById(String fieldId) {
        Document doc = collection.find(Filters.eq("fieldId", fieldId)).first();
        if (doc != null) {
            return new Field(
                    doc.getString("fieldId"),
                    doc.getString("fieldName"),
                    doc.getString("location"),
                    doc.getDouble("areaSize")
            );
        }
        return null;
    }

    public boolean updateField(String fieldId, String fieldName, String location, double areaSize) {
        try {
            collection.updateOne(
                    Filters.eq("fieldId", fieldId),
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
        DeleteResult result = collection.deleteOne(Filters.eq("fieldId", fieldId));
        return result.getDeletedCount() > 0;
    }

    public boolean fieldExists(String fieldId) {
        return collection.find(Filters.eq("fieldId", fieldId)).first() != null;
    }
}