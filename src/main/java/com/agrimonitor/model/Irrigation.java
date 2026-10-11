package com.agrimonitor.model;

public class Irrigation {
    private String fieldId;
    private String irrigationDate;
    private double waterQuantity;

    public Irrigation(String fieldId, String irrigationDate, double waterQuantity) {
        this.fieldId = fieldId;
        this.irrigationDate = irrigationDate;
        this.waterQuantity = waterQuantity;
    }

    public String getFieldId() { return fieldId; }
    public String getIrrigationDate() { return irrigationDate; }
    public double getWaterQuantity() { return waterQuantity; }

    @Override
    public String toString() {
        return "Field ID: " + fieldId + ", Date: " + irrigationDate +
               ", Water: " + waterQuantity + " liters";
    }
}