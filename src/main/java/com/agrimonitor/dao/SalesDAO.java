package com.agrimonitor.dao;

import com.agrimonitor.db.MongoDBConnection;
import com.agrimonitor.model.Sales;
import com.agrimonitor.session.Session;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

public class SalesDAO {

    private MongoCollection<Document> collection;

    public SalesDAO() {
        collection = MongoDBConnection.getDatabase().getCollection("sales");
    }

    public boolean addSale(Sales sale) {
        try {
            Document doc = new Document("cropId", sale.getCropId())
                    .append("cropName", sale.getCropName())
                    .append("quantitySold", sale.getQuantitySold())
                    .append("sellingPricePerUnit", sale.getSellingPricePerUnit())
                    .append("totalSales", sale.getTotalSales())
                    .append("sellingDate", sale.getSellingDate())
                    .append("buyerInfo", sale.getBuyerInfo())
                    .append("userId", Session.getUserId());
            collection.insertOne(doc);
            return true;
        } catch (Exception e) {
            System.out.println("Error adding sales record: " + e.getMessage());
            return false;
        }
    }

    public List<Sales> getAllSales() {
        List<Sales> list = new ArrayList<>();
        Bson filter = Filters.eq("userId", Session.getUserId());
        MongoCursor<Document> cursor = collection.find(filter).iterator();
        try {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Sales sale = new Sales(
                        doc.getString("cropId"),
                        doc.getString("cropName"),
                        doc.getDouble("quantitySold"),
                        doc.getDouble("sellingPricePerUnit"),
                        doc.getString("sellingDate"),
                        doc.getString("buyerInfo")
                );
                sale.setUserId(doc.getString("userId"));
                list.add(sale);
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    public double getTotalSalesByCropId(String cropId) {
        double total = 0.0;
        Bson filter = Filters.and(
                Filters.eq("cropId", cropId),
                Filters.eq("userId", Session.getUserId())
        );
        MongoCursor<Document> cursor = collection.find(filter).iterator();
        try {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                total += doc.getDouble("totalSales");
            }
        } finally {
            cursor.close();
        }
        return total;
    }

    public double getTotalSalesAll() {
        double total = 0.0;
        Bson filter = Filters.eq("userId", Session.getUserId());
        MongoCursor<Document> cursor = collection.find(filter).iterator();
        try {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                total += doc.getDouble("totalSales");
            }
        } finally {
            cursor.close();
        }
        return total;
    }
}