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
        System.out.println("----- Field Report -----");
        List<Field> fields = fieldDAO.getAllFields();
        if (fields.isEmpty()) {
            System.out.println("No field records found.");
        } else {
            for (Field field : fields) {
                System.out.println(field);
            }
        }
        System.out.println("-------------------------");
    }

    public void generateCropReport() {
        System.out.println("----- Crop Report -----");
        List<Crop> crops = cropDAO.getAllCrops();
        if (crops.isEmpty()) {
            System.out.println("No crop records found.");
        } else {
            for (Crop crop : crops) {
                System.out.println(crop);
            }
        }
        System.out.println("------------------------");
    }

    public void generateIrrigationReport() {
        System.out.println("----- Irrigation Report -----");
        List<Irrigation> list = irrigationDAO.getAllIrrigations();
        if (list.isEmpty()) {
            System.out.println("No irrigation records found.");
        } else {
            for (Irrigation irrigation : list) {
                System.out.println(irrigation);
            }
        }
        System.out.println("------------------------------");
    }

    public void generatePesticideReport() {
        System.out.println("----- Pesticide Report -----");
        List<Pesticide> list = pesticideDAO.getAllPesticides();
        if (list.isEmpty()) {
            System.out.println("No pesticide records found.");
        } else {
            for (Pesticide pesticide : list) {
                System.out.println(pesticide);
            }
        }
        System.out.println("-----------------------------");
    }

    public void generateCostReport() {
        System.out.println("----- Cost Report -----");
        List<Cost> list = costDAO.getAllCosts();
        if (list.isEmpty()) {
            System.out.println("No cost records found.");
        } else {
            for (Cost cost : list) {
                System.out.println(cost);
            }
        }
        System.out.println("------------------------");
    }

    public void generateSalesReport() {
        System.out.println("----- Sales Report -----");
        List<Sales> list = salesDAO.getAllSales();
        if (list.isEmpty()) {
            System.out.println("No sales records found.");
        } else {
            for (Sales sale : list) {
                System.out.println(sale);
            }
        }
        System.out.println("-------------------------");
    }

    public void generateProfitLossReport() {
        profitLossService.displayProfitLossReport();
    }

    public void generateOverallFarmReport() {
        System.out.println("===== Overall Farm Report =====");
        generateFieldReport();
        generateCropReport();
        generateIrrigationReport();
        generatePesticideReport();
        generateCostReport();
        generateSalesReport();
        generateProfitLossReport();
        System.out.println("================================");
    }
}