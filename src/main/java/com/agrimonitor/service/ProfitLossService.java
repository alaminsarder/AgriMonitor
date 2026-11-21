package com.agrimonitor.service;

import com.agrimonitor.dao.CostDAO;
import com.agrimonitor.dao.CropDAO;
import com.agrimonitor.dao.SalesDAO;
import com.agrimonitor.model.Crop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProfitLossService {

    private CostDAO costDAO;
    private SalesDAO salesDAO;
    private CropDAO cropDAO;

    public ProfitLossService() {
        costDAO = new CostDAO();
        salesDAO = new SalesDAO();
        cropDAO = new CropDAO();
    }

    public double calculateTotalCost() {
        return costDAO.getTotalCostAll();
    }

    public double calculateTotalSales() {
        return salesDAO.getTotalSalesAll();
    }

    public double calculateProfitOrLoss() {
        return calculateTotalSales() - calculateTotalCost();
    }

    public double calculateProfitOrLossByCrop(String cropId) {
        double totalCost = costDAO.getTotalCostByCropId(cropId);
        double totalSales = salesDAO.getTotalSalesByCropId(cropId);
        return totalSales - totalCost;
    }

    public double calculateProfitMarginPercent() {
        double totalSales = calculateTotalSales();
        if (totalSales == 0) return 0;
        return (calculateProfitOrLoss() / totalSales) * 100;
    }

    public Map<String, Double> getCostBreakdown() {
        return costDAO.getCostBreakdownByCategory();
    }

    public List<Object[]> getCropWiseProfitLoss() {
        List<Object[]> result = new ArrayList<>();
        List<Crop> crops = cropDAO.getAllCrops();
        for (Crop crop : crops) {
            double cost = costDAO.getTotalCostByCropId(crop.getCropId());
            double sales = salesDAO.getTotalSalesByCropId(crop.getCropId());
            double profit = sales - cost;
            result.add(new Object[]{crop.getCropId(), crop.getCropName(), cost, sales, profit});
        }
        return result;
    }

    public void displayProfitLossReport() {
        double totalCost = calculateTotalCost();
        double totalSales = calculateTotalSales();
        double result = totalSales - totalCost;

        System.out.println("----- Profit / Loss Report -----");
        System.out.println("Total Cost: " + totalCost + " BDT");
        System.out.println("Total Sales: " + totalSales + " BDT");

        if (result >= 0) {
            System.out.println("Profit: " + result + " BDT");
        } else {
            System.out.println("Loss: " + Math.abs(result) + " BDT");
        }
        System.out.println("---------------------------------");
    }
}