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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Puts sample (demo) records into every module for the logged-in user:
 * Fields, Crops, Irrigation, Pesticides, Costs and Sales.
 *
 * - Runs automatically right after login / register (see LoginFrame).
 * - Runs only ONCE per account (flag "demoSeeded" on the user document),
 *   so records the user deletes later do not come back.
 * - Skipped if the user already has their own fields or crops.
 * - Every record is stored with the user's own userId, so each user only
 *   sees his/her own data. The user can add, edit, or delete records normally.
 */
public class DemoDataService {

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
            if (user == null || Boolean.TRUE.equals(user.getBoolean("demoSeeded"))) {
                return;
            }

            boolean hasOwnData = hasData(db, "fields", userId) || hasData(db, "crops", userId);
            if (!hasOwnData) {
                insertDemoData(db, userId);
                System.out.println("Demo data added for user: " + Session.getUsername());
            }
            users.updateOne(byId, Updates.set("demoSeeded", true));
        } catch (Exception e) {
            // Never block the login because of demo data.
            System.out.println("Demo data skipped: " + e.getMessage());
        }
    }

    private static boolean hasData(MongoDatabase db, String collection, String userId) {
        return db.getCollection(collection).find(Filters.eq("userId", userId)).first() != null;
    }

    private static String day(int daysAgo) {
        return LocalDate.now().minusDays(daysAgo).toString();   // yyyy-MM-dd
    }

    private static void insertDemoData(MongoDatabase db, String u) {
        // ---------- Fields (area in acres) ----------
        List<Document> fields = new ArrayList<>();
        fields.add(field(u, "FIELD01", "North Paddy Field", "Paba, Rajshahi", 5.0));
        fields.add(field(u, "FIELD02", "River Side Plot", "Godagari, Rajshahi", 3.5));
        fields.add(field(u, "FIELD03", "East Wheat Field", "Bagmara, Rajshahi", 2.5));
        fields.add(field(u, "FIELD04", "South Potato Plot", "Puthia, Rajshahi", 3.0));
        fields.add(field(u, "FIELD05", "Vegetable Garden", "Durgapur, Rajshahi", 1.5));
        db.getCollection("fields").insertMany(fields);

        // ---------- Crops (growth duration in days) ----------
        List<Document> crops = new ArrayList<>();
        crops.add(crop(u, "CROP001", "Boro Rice", "Cereal", 150, "FIELD01"));
        crops.add(crop(u, "CROP002", "Jute", "Fiber", 120, "FIELD02"));
        crops.add(crop(u, "CROP003", "Wheat", "Cereal", 115, "FIELD03"));
        crops.add(crop(u, "CROP004", "Potato", "Vegetable", 90, "FIELD04"));
        crops.add(crop(u, "CROP005", "Tomato", "Vegetable", 100, "FIELD05"));
        db.getCollection("crops").insertMany(crops);

        // ---------- Irrigation (water in liters) ----------
        List<Document> irrigation = new ArrayList<>();
        irrigation.add(irrigation(u, "FIELD01", day(40), 25000));
        irrigation.add(irrigation(u, "FIELD01", day(25), 22000));
        irrigation.add(irrigation(u, "FIELD01", day(10), 24000));
        irrigation.add(irrigation(u, "FIELD02", day(30), 18000));
        irrigation.add(irrigation(u, "FIELD02", day(12), 16500));
        irrigation.add(irrigation(u, "FIELD03", day(20), 12000));
        irrigation.add(irrigation(u, "FIELD04", day(15), 14000));
        irrigation.add(irrigation(u, "FIELD04", day(5), 13500));
        irrigation.add(irrigation(u, "FIELD05", day(7), 6000));
        irrigation.add(irrigation(u, "FIELD05", day(2), 5500));
        db.getCollection("irrigation").insertMany(irrigation);

        // ---------- Pesticides ----------
        List<Document> pesticides = new ArrayList<>();
        pesticides.add(pesticide(u, "FIELD01", "Neem Oil", 3.0, day(35)));
        pesticides.add(pesticide(u, "FIELD01", "Imidacloprid", 1.2, day(18)));
        pesticides.add(pesticide(u, "FIELD02", "Malathion", 1.5, day(28)));
        pesticides.add(pesticide(u, "FIELD03", "Mancozeb", 2.0, day(22)));
        pesticides.add(pesticide(u, "FIELD04", "Copper Oxychloride", 2.2, day(14)));
        pesticides.add(pesticide(u, "FIELD05", "Cypermethrin", 1.0, day(9)));
        db.getCollection("pesticides").insertMany(pesticides);

        // ---------- Costs (BDT) ----------
        List<Document> costs = new ArrayList<>();
        costs.add(cost(u, "CROP001", "FIELD01", "Seeds - Boro rice seed", 4500, day(150)));
        costs.add(cost(u, "CROP001", "FIELD01", "Fertilizer - Urea and TSP", 6800, day(120)));
        costs.add(cost(u, "CROP001", "FIELD01", "Labor - Transplanting", 9000, day(140)));
        costs.add(cost(u, "CROP001", "FIELD01", "Irrigation - Diesel for pump", 3200, day(60)));
        costs.add(cost(u, "CROP001", "FIELD01", "Pesticide", 1800, day(35)));
        costs.add(cost(u, "CROP002", "FIELD02", "Seeds - Jute seed", 1500, day(130)));
        costs.add(cost(u, "CROP002", "FIELD02", "Labor - Weeding", 4200, day(90)));
        costs.add(cost(u, "CROP002", "FIELD02", "Fertilizer", 2600, day(100)));
        costs.add(cost(u, "CROP003", "FIELD03", "Seeds", 3000, day(110)));
        costs.add(cost(u, "CROP003", "FIELD03", "Fertilizer", 3500, day(95)));
        costs.add(cost(u, "CROP003", "FIELD03", "Machinery - Power tiller rent", 2500, day(112)));
        costs.add(cost(u, "CROP004", "FIELD04", "Seeds - Potato tuber", 12000, day(85)));
        costs.add(cost(u, "CROP004", "FIELD04", "Fertilizer", 5200, day(70)));
        costs.add(cost(u, "CROP004", "FIELD04", "Labor - Planting", 6000, day(84)));
        costs.add(cost(u, "CROP004", "FIELD04", "Transport", 1800, day(20)));
        costs.add(cost(u, "CROP005", "FIELD05", "Seeds", 1200, day(95)));
        costs.add(cost(u, "CROP005", "FIELD05", "Pesticide", 1500, day(9)));
        costs.add(cost(u, "CROP005", "FIELD05", "Labor", 2800, day(50)));
        db.getCollection("costs").insertMany(costs);

        // ---------- Sales (quantity in kg, price per kg in BDT) ----------
        List<Document> sales = new ArrayList<>();
        sales.add(sale(u, "CROP001", "Boro Rice", 1200, 38, day(25), "Rahim Traders, Rajshahi"));
        sales.add(sale(u, "CROP001", "Boro Rice", 800, 40, day(18), "Hasan Rice Mill"));
        sales.add(sale(u, "CROP002", "Jute", 500, 55, day(22), "Jute Mill Agent, Bogura"));
        sales.add(sale(u, "CROP003", "Wheat", 600, 42, day(15), "Karim Flour Mill"));
        sales.add(sale(u, "CROP004", "Potato", 2000, 25, day(8), "Alam Cold Storage"));
        sales.add(sale(u, "CROP005", "Tomato", 100, 40, day(3), "Sabuj Bazar Wholesale")); // small loss on purpose
        db.getCollection("sales").insertMany(sales);
    }

    // Field names below are exactly the ones used by the DAO classes.

    private static Document field(String u, String id, String name, String location, double area) {
        return new Document("fieldId", id)
                .append("fieldName", name)
                .append("location", location)
                .append("areaSize", area)
                .append("userId", u);
    }

    private static Document crop(String u, String id, String name, String type, int days, String fieldId) {
        return new Document("cropId", id)
                .append("cropName", name)
                .append("cropType", type)
                .append("growthDuration", days)
                .append("fieldId", fieldId)
                .append("userId", u);
    }

    private static Document irrigation(String u, String fieldId, String date, double liters) {
        return new Document("fieldId", fieldId)
                .append("irrigationDate", date)
                .append("waterQuantity", liters)
                .append("userId", u);
    }

    private static Document pesticide(String u, String fieldId, String name, double qty, String date) {
        return new Document("fieldId", fieldId)
                .append("pesticideName", name)
                .append("quantity", qty)
                .append("applicationDate", date)
                .append("userId", u);
    }

    private static Document cost(String u, String cropId, String fieldId, String desc, double amount, String date) {
        return new Document("cropId", cropId)
                .append("fieldId", fieldId)
                .append("description", desc)
                .append("amount", amount)
                .append("date", date)
                .append("userId", u);
    }

    private static Document sale(String u, String cropId, String cropName, double qty,
                                 double price, String date, String buyer) {
        return new Document("cropId", cropId)
                .append("cropName", cropName)
                .append("quantitySold", qty)
                .append("sellingPricePerUnit", price)
                .append("totalSales", qty * price)
                .append("sellingDate", date)
                .append("buyerInfo", buyer)
                .append("userId", u);
    }
}