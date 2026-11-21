package com.agrimonitor.model;

public class Pesticide {
    private String fieldId;
    private String pesticideName;
    private double quantity;
    private String applicationDate;
    private String userId;

    public Pesticide(String fieldId, String pesticideName, double quantity, String applicationDate) {
        this.fieldId = fieldId;
        this.pesticideName = pesticideName;
        this.quantity = quantity;
        this.applicationDate = applicationDate;
    }

    public String getFieldId() { return fieldId; }
    public String getPesticideName() { return pesticideName; }
    public double getQuantity() { return quantity; }
    public String getApplicationDate() { return applicationDate; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    @Override
    public String toString() {
        return "Field ID: " + fieldId + ", Pesticide: " + pesticideName +
               ", Qty: " + quantity + ", Date: " + applicationDate;
    }
}