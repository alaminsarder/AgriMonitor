package com.agrimonitor.model;

public class Field {
    private String fieldId;
    private String fieldName;
    private String location;
    private double areaSize;

    public Field(String fieldId, String fieldName, String location, double areaSize) {
        this.fieldId = fieldId;
        this.fieldName = fieldName;
        this.location = location;
        this.areaSize = areaSize;
    }

    public String getFieldId() { return fieldId; }
    public void setFieldId(String fieldId) { this.fieldId = fieldId; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public double getAreaSize() { return areaSize; }
    public void setAreaSize(double areaSize) { this.areaSize = areaSize; }

    @Override
    public String toString() {
        return "Field ID: " + fieldId + ", Name: " + fieldName +
               ", Location: " + location + ", Area: " + areaSize + " acres";
    }
}