package com.agrimonitor.dao;

import com.agrimonitor.db.MongoDBConnection;
import com.agrimonitor.model.User;
import com.agrimonitor.util.PasswordUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Locale;

public class UserDAO {

    public enum RegisterResult { OK, USERNAME_TAKEN, EMAIL_TAKEN }

    public enum ResetResult { OK, INVALID_CODE, EXPIRED, TOO_MANY_ATTEMPTS, NO_REQUEST }

    public static final long CODE_VALID_MS = 10 * 60 * 1000L;
    public static final long RESEND_COOLDOWN_MS = 60 * 1000L;
    private static final int MAX_ATTEMPTS = 5;

    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$";

    private final SecureRandom random = new SecureRandom();
    private MongoCollection<Document> collection;

    public UserDAO() {
        MongoDatabase database = MongoDBConnection.getDatabase();
        collection = database.getCollection("users");
        try {
            // one account per email; sparse so old accounts without an email are allowed
            collection.createIndex(Indexes.ascending("email"),
                    new IndexOptions().unique(true).sparse(true));
        } catch (Exception e) {
            System.out.println("Could not create email index: " + e.getMessage());
        }
    }

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches(EMAIL_REGEX);
    }

    public RegisterResult registerUser(String username, String email, String password) {
        email = normalizeEmail(email);
        if (collection.find(new Document("username", username)).first() != null) {
            return RegisterResult.USERNAME_TAKEN;
        }
        if (collection.find(Filters.eq("email", email)).first() != null) {
            return RegisterResult.EMAIL_TAKEN;
        }
        Document doc = new Document("username", username)
                .append("email", email)
                .append("password", PasswordUtil.hashPassword(password));
        try {
            collection.insertOne(doc);
        } catch (com.mongodb.MongoWriteException e) {
            if (e.getError().getCode() == 11000) {
                return RegisterResult.EMAIL_TAKEN;
            }
            throw e;
        }
        return RegisterResult.OK;
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
        user.setEmail(doc.getString("email"));
        user.setPassword(doc.getString("password"));
        return user;
    }

    // ---------- email on the account ----------

    public boolean emailExists(String email) {
        return collection.find(Filters.eq("email", normalizeEmail(email))).first() != null;
    }

    public String getUsernameByEmail(String email) {
        Document doc = collection.find(Filters.eq("email", normalizeEmail(email))).first();
        return doc == null ? null : doc.getString("username");
    }

    /** For accounts created before e-mail existed. Returns false if the email is already used. */
    public boolean setEmail(String userId, String email) {
        email = normalizeEmail(email);
        if (collection.find(Filters.eq("email", email)).first() != null) {
            return false;
        }
        try {
            collection.updateOne(Filters.eq("_id", new org.bson.types.ObjectId(userId)),
                    Updates.set("email", email));
        } catch (com.mongodb.MongoWriteException e) {
            if (e.getError().getCode() == 11000) {
                return false;
            }
            throw e;
        }
        return true;
    }

    // ---------- forgot password ----------

    private static String codeHash(String email, String code) {
        return PasswordUtil.hashPassword(email + ":" + code);
    }

    private static long number(Document doc, String key) {
        Object v = doc.get(key);
        return v instanceof Number ? ((Number) v).longValue() : 0L;
    }

    /** Seconds the user must still wait before another code can be sent (0 = can send now). */
    public long resendWaitSeconds(String email) {
        Document doc = collection.find(Filters.eq("email", normalizeEmail(email))).first();
        if (doc == null) {
            return 0;
        }
        long left = number(doc, "resetSentAt") + RESEND_COOLDOWN_MS - System.currentTimeMillis();
        return left <= 0 ? 0 : (left + 999) / 1000;
    }

    /** Creates and stores a new 6-digit code (hashed). Returns the plain code, or null if no such account. */
    public String createResetCode(String email) {
        email = normalizeEmail(email);
        String code = String.format("%06d", random.nextInt(1_000_000));
        long now = System.currentTimeMillis();
        UpdateResult r = collection.updateOne(Filters.eq("email", email), Updates.combine(
                Updates.set("resetCodeHash", codeHash(email, code)),
                Updates.set("resetExpiresAt", now + CODE_VALID_MS),
                Updates.set("resetSentAt", now),
                Updates.set("resetAttempts", 0)));
        return r.getMatchedCount() == 0 ? null : code;
    }

    /** Removes any pending code (used when the email could not be sent). */
    public void clearResetCode(String email) {
        collection.updateOne(Filters.eq("email", normalizeEmail(email)), clearFields());
    }

    private static Bson clearFields() {
        return Updates.combine(
                Updates.unset("resetCodeHash"),
                Updates.unset("resetExpiresAt"),
                Updates.unset("resetSentAt"),
                Updates.unset("resetAttempts"));
    }

    public ResetResult resetPassword(String email, String code, String newPassword) {
        email = normalizeEmail(email);
        Bson byEmail = Filters.eq("email", email);
        Document doc = collection.find(byEmail).first();
        if (doc == null || doc.getString("resetCodeHash") == null) {
            return ResetResult.NO_REQUEST;
        }
        if (System.currentTimeMillis() > number(doc, "resetExpiresAt")) {
            collection.updateOne(byEmail, clearFields());
            return ResetResult.EXPIRED;
        }
        long attempts = number(doc, "resetAttempts");
        if (attempts >= MAX_ATTEMPTS) {
            return ResetResult.TOO_MANY_ATTEMPTS;
        }

        byte[] expected = doc.getString("resetCodeHash").getBytes();
        byte[] given = codeHash(email, code).getBytes();
        if (!MessageDigest.isEqual(expected, given)) {
            collection.updateOne(byEmail, Updates.inc("resetAttempts", 1));
            return attempts + 1 >= MAX_ATTEMPTS ? ResetResult.TOO_MANY_ATTEMPTS : ResetResult.INVALID_CODE;
        }

        collection.updateOne(byEmail, Updates.combine(
                Updates.set("password", PasswordUtil.hashPassword(newPassword)),
                Updates.unset("resetCodeHash"),
                Updates.unset("resetExpiresAt"),
                Updates.unset("resetSentAt"),
                Updates.unset("resetAttempts")));
        return ResetResult.OK;
    }
}
