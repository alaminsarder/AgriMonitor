package com.agrimonitor.model;

public class Crop {
    private String cropId;
    private String cropName;
    private String cropType;
    private int growthDuration;
    private String fieldId;

    public Crop(String cropId, String cropName, String cropType, int growthDuration, String fieldId) {
        this.cropId = cropId;
        this.cropName = cropName;
        this.cropType = cropType;
        this.growthDuration = growthDuration;
        this.fieldId = fieldId;
    }

    public String getCropId() { return cropId; }
    public void setCropId(String cropId) { this.cropId = cropId; }

    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }

    public String getCropType() { return cropType; }
    public void setCropType(String cropType) { this.cropType = cropType; }

    public int getGrowthDuration() { return growthDuration; }
    public void setGrowthDuration(int growthDuration) { this.growthDuration = growthDuration; }

    public String getFieldId() { return fieldId; }
    public void setFieldId(String fieldId) { this.fieldId = fieldId; }

    @Override
    public String toString() {
        return "Crop ID: " + cropId + ", Name: " + cropName + ", Type: " + cropType +
               ", Duration: " + growthDuration + " days, Field ID: " + fieldId;
    }
}