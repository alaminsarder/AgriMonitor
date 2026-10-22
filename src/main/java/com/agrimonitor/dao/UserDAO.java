package com.agrimonitor.dao;

import com.agrimonitor.db.MongoDBConnection;
import com.agrimonitor.model.User;
import com.agrimonitor.util.PasswordUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class UserDAO {

    private MongoCollection<Document> collection;

    public UserDAO() {
        MongoDatabase database = MongoDBConnection.getDatabase();
        collection = database.getCollection("users");
    }

    public boolean registerUser(String username, String password) {
        Document existing = collection.find(new Document("username", username)).first();
        if (existing != null) {
            return false;
        }
        Document doc = new Document("username", username)
                .append("password", PasswordUtil.hashPassword(password));
        collection.insertOne(doc);
        return true;
    }

    public User loginUser(String username, String password) {
        String hashed = PasswordUtil.hashPassword(password);
        Document doc = collection.find(new Document("username", username).append("password", hashed)).first();
        if (doc == null) {
            return null;
        }
        User user = new User();
        user.setId(doc.getObjectId("_id").toHexString());
        user.setUsername(doc.getString("username"));
        user.setPassword(doc.getString("password"));
        return user;
    }
}