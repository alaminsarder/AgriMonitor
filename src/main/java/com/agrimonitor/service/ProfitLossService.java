package com.agrimonitor.service;

import com.agrimonitor.dao.CostDAO;
import com.agrimonitor.dao.SalesDAO;

public class ProfitLossService {

    private CostDAO costDAO;
    private SalesDAO salesDAO;

    public ProfitLossService() {
        costDAO = new CostDAO();
        salesDAO = new SalesDAO();
    }

    public double calculateTotalCost() {
        return costDAO.getTotalCostAll();
    }

    public double calculateTotalSales() {
        return salesDAO.getTotalSalesAll();
    }

    public double calculateProfitOrLoss() {
        double totalCost = calculateTotalCost();
        double totalSales = calculateTotalSales();
        return totalSales - totalCost;
    }

    public double calculateProfitOrLossByCrop(String cropId) {
        double totalCost = costDAO.getTotalCostByCropId(cropId);
        double totalSales = salesDAO.getTotalSalesByCropId(cropId);
        return totalSales - totalCost;
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