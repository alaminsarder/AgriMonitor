package com.agrimonitor.dao;

import com.agrimonitor.db.MongoDBConnection;
import com.agrimonitor.session.Session;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class DemoDataService {

    private static final String[][] DEMO_FIELDS = {
            {"FIELD01", "North Paddy Field"},
            {"FIELD02", "River Side Plot"},
            {"FIELD03", "East Wheat Field"},
            {"FIELD04", "South Potato Plot"},
            {"FIELD05", "Vegetable Garden"}
    };

    private static final String[][] DEMO_CROPS = {
            {"CROP001", "Boro Rice"},
            {"CROP002", "Jute"},
            {"CROP003", "Wheat"},
            {"CROP004", "Potato"},
            {"CROP005", "Tomato"}
    };

    private DemoDataService() {
    }

    public static void seedIfNeeded() {
        try {
            String userId = Session.getUserId();
            if (userId == null) {
                return;
            }
            MongoDatabase db = MongoDBConnection.getDatabase();
            if (db == null) {
                return;
            }

            MongoCollection<Document> users = db.getCollection("users");
            Bson byId = Filters.eq("_id", new ObjectId(userId));
            Document user = users.find(byId).first();
            if (user == null || Boolean.TRUE.equals(user.getBoolean("demoCleaned"))) {
                return;
            }

            removeDemoData(db, userId);
            users.updateOne(byId, Updates.set("demoCleaned", true));
        } catch (Exception e) {
            System.out.println("Demo cleanup skipped: " + e.getMessage());
        }
    }

    private static void removeDemoData(MongoDatabase db, String userId) {
        List<String> fieldIds = new ArrayList<>();
        for (String[] f : DEMO_FIELDS) {
            Document hit = db.getCollection("fields").find(Filters.and(
                    Filters.eq("userId", userId),
                    Filters.eq("fieldId", f[0]),
                    Filters.eq("fieldName", f[1]))).first();
            if (hit != null) {
                fieldIds.add(f[0]);
            }
        }

        List<String> cropIds = new ArrayList<>();
        for (String[] c : DEMO_CROPS) {
            Document hit = db.getCollection("crops").find(Filters.and(
                    Filters.eq("userId", userId),
                    Filters.eq("cropId", c[0]),
                    Filters.eq("cropName", c[1]))).first();
            if (hit != null) {
                cropIds.add(c[0]);
            }
        }

        if (!fieldIds.isEmpty()) {
            db.getCollection("irrigation").deleteMany(Filters.and(
                    Filters.eq("userId", userId), Filters.in("fieldId", fieldIds)));
            db.getCollection("pesticides").deleteMany(Filters.and(
                    Filters.eq("userId", userId), Filters.in("fieldId", fieldIds)));
            db.getCollection("costs").deleteMany(Filters.and(
                    Filters.eq("userId", userId), Filters.in("fieldId", fieldIds)));
            for (String[] f : DEMO_FIELDS) {
                if (fieldIds.contains(f[0])) {
                    db.getCollection("fields").deleteMany(Filters.and(
                            Filters.eq("userId", userId),
                            Filters.eq("fieldId", f[0]),
                            Filters.eq("fieldName", f[1])));
                }
            }
        }

        if (!cropIds.isEmpty()) {
            db.getCollection("costs").deleteMany(Filters.and(
                    Filters.eq("userId", userId), Filters.in("cropId", cropIds)));
            db.getCollection("sales").deleteMany(Filters.and(
                    Filters.eq("userId", userId), Filters.in("cropId", cropIds)));
            for (String[] c : DEMO_CROPS) {
                if (cropIds.contains(c[0])) {
                    db.getCollection("crops").deleteMany(Filters.and(
                            Filters.eq("userId", userId),
                            Filters.eq("cropId", c[0]),
                            Filters.eq("cropName", c[1])));
                }
            }
        }
    }
}