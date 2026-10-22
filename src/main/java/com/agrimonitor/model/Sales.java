package com.agrimonitor.model;

public class Sales {
    private String cropId;
    private String cropName;
    private double quantitySold;
    private double sellingPricePerUnit;
    private String sellingDate;
    private String buyerInfo;
    private String userId;

    public Sales(String cropId, String cropName, double quantitySold,
                 double sellingPricePerUnit, String sellingDate, String buyerInfo) {
        this.cropId = cropId;
        this.cropName = cropName;
        this.quantitySold = quantitySold;
        this.sellingPricePerUnit = sellingPricePerUnit;
        this.sellingDate = sellingDate;
        this.buyerInfo = buyerInfo;
    }

    public String getCropId() { return cropId; }
    public String getCropName() { return cropName; }
    public double getQuantitySold() { return quantitySold; }
    public double getSellingPricePerUnit() { return sellingPricePerUnit; }
    public String getSellingDate() { return sellingDate; }
    public String getBuyerInfo() { return buyerInfo; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public double getTotalSales() {
        return quantitySold * sellingPricePerUnit;
    }

    @Override
    public String toString() {
        return "Crop: " + cropName + ", Qty: " + quantitySold +
               ", Price/Unit: " + sellingPricePerUnit +
               ", Total: " + getTotalSales() + " BDT, Buyer: " + buyerInfo;
    }
}