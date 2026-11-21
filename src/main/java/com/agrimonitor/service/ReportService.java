package com.agrimonitor.service;

import com.agrimonitor.dao.*;
import com.agrimonitor.model.*;

import java.util.List;

public class ReportService {

    private FieldDAO fieldDAO;
    private CropDAO cropDAO;
    private IrrigationDAO irrigationDAO;
    private PesticideDAO pesticideDAO;
    private CostDAO costDAO;
    private SalesDAO salesDAO;
    private ProfitLossService profitLossService;

    public ReportService() {
        fieldDAO = new FieldDAO();
        cropDAO = new CropDAO();
        irrigationDAO = new IrrigationDAO();
        pesticideDAO = new PesticideDAO();
        costDAO = new CostDAO();
        salesDAO = new SalesDAO();
        profitLossService = new ProfitLossService();
    }

    public void generateFieldReport() {
        System.out.println(getFieldReportText());
    }

    public void generateCropReport() {
        System.out.println(getCropReportText());
    }

    public void generateIrrigationReport() {
        System.out.println(getIrrigationReportText());
    }

    public void generatePesticideReport() {
        System.out.println(getPesticideReportText());
    }

    public void generateCostReport() {
        System.out.println(getCostReportText());
    }

    public void generateSalesReport() {
        System.out.println(getSalesReportText());
    }

    public void generateProfitLossReport() {
        System.out.println(getProfitLossReportText());
    }

    public void generateOverallFarmReport() {
        System.out.println(getOverallFarmReportText());
    }

    public String getFieldReportText() {
        StringBuilder sb = new StringBuilder();
        sb.append("----- Field Report -----\n");
        List<Field> fields = fieldDAO.getAllFields();
        if (fields.isEmpty()) {
            sb.append("No field records found.\n");
        } else {
            for (Field field : fields) {
                sb.append(field.toString()).append("\n");
            }
        }
        return sb.toString();
    }

    public String getCropReportText() {
        StringBuilder sb = new StringBuilder();
        sb.append("----- Crop Report -----\n");
        List<Crop> crops = cropDAO.getAllCrops();
        if (crops.isEmpty()) {
            sb.append("No crop records found.\n");
        } else {
            for (Crop crop : crops) {
                sb.append(crop.toString()).append("\n");
            }
        }
        return sb.toString();
    }

    public String getIrrigationReportText() {
        StringBuilder sb = new StringBuilder();
        sb.append("----- Irrigation Report -----\n");
        List<Irrigation> list = irrigationDAO.getAllIrrigations();
        if (list.isEmpty()) {
            sb.append("No irrigation records found.\n");
        } else {
            for (Irrigation irrigation : list) {
                sb.append(irrigation.toString()).append("\n");
            }
        }
        return sb.toString();
    }

    public String getPesticideReportText() {
        StringBuilder sb = new StringBuilder();
        sb.append("----- Pesticide Report -----\n");
        List<Pesticide> list = pesticideDAO.getAllPesticides();
        if (list.isEmpty()) {
            sb.append("No pesticide records found.\n");
        } else {
            for (Pesticide pesticide : list) {
                sb.append(pesticide.toString()).append("\n");
            }
        }
        return sb.toString();
    }

    public String getCostReportText() {
        StringBuilder sb = new StringBuilder();
        sb.append("----- Cost Report -----\n");
        List<Cost> list = costDAO.getAllCosts();
        if (list.isEmpty()) {
            sb.append("No cost records found.\n");
        } else {
            for (Cost cost : list) {
                sb.append(cost.toString()).append("\n");
            }
        }
        return sb.toString();
    }

    public String getSalesReportText() {
        StringBuilder sb = new StringBuilder();
        sb.append("----- Sales Report -----\n");
        List<Sales> list = salesDAO.getAllSales();
        if (list.isEmpty()) {
            sb.append("No sales records found.\n");
        } else {
            for (Sales sale : list) {
                sb.append(sale.toString()).append("\n");
            }
        }
        return sb.toString();
    }

    public String getProfitLossReportText() {
        StringBuilder sb = new StringBuilder();
        double totalCost = profitLossService.calculateTotalCost();
        double totalSales = profitLossService.calculateTotalSales();
        double result = totalSales - totalCost;

        sb.append("----- Profit / Loss Report -----\n");
        sb.append("Total Cost: ").append(totalCost).append(" BDT\n");
        sb.append("Total Sales: ").append(totalSales).append(" BDT\n");

        if (result >= 0) {
            sb.append("Profit: ").append(result).append(" BDT\n");
        } else {
            sb.append("Loss: ").append(Math.abs(result)).append(" BDT\n");
        }
        return sb.toString();
    }

    public String getOverallFarmReportText() {
        StringBuilder sb = new StringBuilder();
        sb.append("===== Overall Farm Report =====\n\n");
        sb.append(getFieldReportText()).append("\n");
        sb.append(getCropReportText()).append("\n");
        sb.append(getIrrigationReportText()).append("\n");
        sb.append(getPesticideReportText()).append("\n");
        sb.append(getCostReportText()).append("\n");
        sb.append(getSalesReportText()).append("\n");
        sb.append(getProfitLossReportText()).append("\n");
        return sb.toString();
    }
}