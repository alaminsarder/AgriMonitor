package com.agrimonitor.dao;

import com.agrimonitor.db.MongoDBConnection;
import com.agrimonitor.model.Sales;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.bson.Document;

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
                    .append("buyerInfo", sale.getBuyerInfo());
            collection.insertOne(doc);
            return true;
        } catch (Exception e) {
            System.out.println("Error adding sales record: " + e.getMessage());
            return false;
        }
    }

    public List<Sales> getAllSales() {
        List<Sales> list = new ArrayList<>();
        MongoCursor<Document> cursor = collection.find().iterator();
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
                list.add(sale);
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    public double getTotalSalesByCropId(String cropId) {
        double total = 0.0;
        MongoCursor<Document> cursor = collection.find(Filters.eq("cropId", cropId)).iterator();
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
        MongoCursor<Document> cursor = collection.find().iterator();
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