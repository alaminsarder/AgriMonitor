package com.agrimonitor.menu;

import com.agrimonitor.dao.CostDAO;
import com.agrimonitor.dao.CropDAO;
import com.agrimonitor.dao.FieldDAO;
import com.agrimonitor.model.Cost;

import java.util.List;
import java.util.Scanner;

public class CostMenu {

    private CostDAO costDAO;
    private CropDAO cropDAO;
    private FieldDAO fieldDAO;
    private Scanner scanner;

    public CostMenu(Scanner scanner) {
        this.costDAO = new CostDAO();
        this.cropDAO = new CropDAO();
        this.fieldDAO = new FieldDAO();
        this.scanner = scanner;
    }

    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println("\n----- Cost Management -----");
            System.out.println("1. Add Cost Record");
            System.out.println("2. View All Cost Records");
            System.out.println("3. View Total Cost by Crop ID");
            System.out.println("4. Back to Main Menu");
            System.out.print("Enter your choice: ");

            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    addCost();
                    break;
                case 2:
                    viewAllCosts();
                    break;
                case 3:
                    viewTotalCostByCrop();
                    break;
                case 4:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void addCost() {
        System.out.print("Enter Crop ID: ");
        String cropId = scanner.nextLine();

        if (!cropDAO.cropExists(cropId)) {
            System.out.println("Crop ID does not exist.");
            return;
        }

        System.out.print("Enter Field ID: ");
        String fieldId = scanner.nextLine();

        if (!fieldDAO.fieldExists(fieldId)) {
            System.out.println("Field ID does not exist.");
            return;
        }

        System.out.print("Enter Cost Description (Seeds/Fertilizer/Labor/etc): ");
        String description = scanner.nextLine();

        System.out.print("Enter Amount (BDT): ");
        double amount = Double.parseDouble(scanner.nextLine());

        System.out.print("Enter Date (YYYY-MM-DD): ");
        String date = scanner.nextLine();

        Cost cost = new Cost(cropId, fieldId, description, amount, date);
        boolean success = costDAO.addCost(cost);

        if (success) {
            System.out.println("Cost record added successfully.");
        } else {
            System.out.println("Failed to add cost record.");
        }
    }

    private void viewAllCosts() {
        List<Cost> list = costDAO.getAllCosts();
        if (list.isEmpty()) {
            System.out.println("No cost records found.");
        } else {
            System.out.println("----- All Cost Records -----");
            for (Cost cost : list) {
                System.out.println(cost);
            }
        }
    }

    private void viewTotalCostByCrop() {
        System.out.print("Enter Crop ID: ");
        String cropId = scanner.nextLine();

        double total = costDAO.getTotalCostByCropId(cropId);
        System.out.println("Total Cost for Crop " + cropId + ": " + total + " BDT");
    }
}