package com.agrimonitor.model;

public class Cost {
    private String cropId;
    private String fieldId;
    private String description;
    private double amount;
    private String date;

    public Cost(String cropId, String fieldId, String description, double amount, String date) {
        this.cropId = cropId;
        this.fieldId = fieldId;
        this.description = description;
        this.amount = amount;
        this.date = date;
    }

    public String getCropId() { return cropId; }
    public String getFieldId() { return fieldId; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }

    @Override
    public String toString() {
        return "Crop ID: " + cropId + ", Field ID: " + fieldId +
               ", Desc: " + description + ", Amount: " + amount + " BDT, Date: " + date;
    }
}