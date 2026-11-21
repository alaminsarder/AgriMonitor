package com.agrimonitor.dao;

import com.agrimonitor.db.MongoDBConnection;
import com.agrimonitor.session.Session;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/** Crops a farmer added to the Season Guide (collection "season_crops"), private per user. */
public class SeasonCropDAO {

    private final MongoCollection<Document> collection;

    public SeasonCropDAO() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        collection = db == null ? null : db.getCollection("season_crops");
    }

    public boolean add(String name, String season, int sowFrom, int sowTo, String harvest, int days) {
        if (collection == null || Session.getUserId() == null) return false;
        try {
            collection.insertOne(new Document("name", name)
                    .append("season", season)
                    .append("sowFrom", sowFrom)
                    .append("sowTo", sowTo)
                    .append("harvest", harvest)
                    .append("days", days)
                    .append("userId", Session.getUserId()));
            return true;
        } catch (Exception e) {
            System.out.println("Error adding season crop: " + e.getMessage());
            return false;
        }
    }

    /** Each row: name, season, sowFrom, sowTo, harvest, days. */
    public List<Object[]> getAll() {
        List<Object[]> list = new ArrayList<>();
        if (collection == null || Session.getUserId() == null) return list;
        try (MongoCursor<Document> cursor = collection
                .find(Filters.eq("userId", Session.getUserId()))
                .iterator()) {
            while (cursor.hasNext()) {
                Document d = cursor.next();
                list.add(new Object[]{
                        d.getString("name"),
                        d.getString("season"),
                        ((Number) d.get("sowFrom")).intValue(),
                        ((Number) d.get("sowTo")).intValue(),
                        d.getString("harvest"),
                        ((Number) d.get("days")).intValue()});
            }
        } catch (Exception e) {
            System.out.println("Error loading season crops: " + e.getMessage());
        }
        return list;
    }

    public boolean delete(String name) {
        if (collection == null || Session.getUserId() == null) return false;
        try {
            return collection.deleteOne(Filters.and(
                    Filters.eq("userId", Session.getUserId()),
                    Filters.eq("name", name))).getDeletedCount() > 0;
        } catch (Exception e) {
            System.out.println("Error deleting season crop: " + e.getMessage());
            return false;
        }
    }
}