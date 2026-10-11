package com.agrimonitor.menu;

import com.agrimonitor.dao.FieldDAO;
import com.agrimonitor.dao.IrrigationDAO;
import com.agrimonitor.model.Irrigation;

import java.util.List;
import java.util.Scanner;

public class IrrigationMenu {

    private IrrigationDAO irrigationDAO;
    private FieldDAO fieldDAO;
    private Scanner scanner;

    public IrrigationMenu(Scanner scanner) {
        this.irrigationDAO = new IrrigationDAO();
        this.fieldDAO = new FieldDAO();
        this.scanner = scanner;
    }

    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println("\n----- Irrigation Management -----");
            System.out.println("1. Record Irrigation");
            System.out.println("2. View All Irrigation Records");
            System.out.println("3. View Irrigation by Field ID");
            System.out.println("4. Back to Main Menu");
            System.out.print("Enter your choice: ");

            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    addIrrigation();
                    break;
                case 2:
                    viewAllIrrigation();
                    break;
                case 3:
                    viewIrrigationByField();
                    break;
                case 4:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void addIrrigation() {
        System.out.print("Enter Field ID: ");
        String fieldId = scanner.nextLine();

        if (!fieldDAO.fieldExists(fieldId)) {
            System.out.println("Field ID does not exist.");
            return;
        }

        System.out.print("Enter Irrigation Date (YYYY-MM-DD): ");
        String date = scanner.nextLine();

        System.out.print("Enter Water Quantity (liters): ");
        double waterQuantity = Double.parseDouble(scanner.nextLine());

        Irrigation irrigation = new Irrigation(fieldId, date, waterQuantity);
        boolean success = irrigationDAO.addIrrigation(irrigation);

        if (success) {
            System.out.println("Irrigation record added successfully.");
        } else {
            System.out.println("Failed to add irrigation record.");
        }
    }

    private void viewAllIrrigation() {
        List<Irrigation> list = irrigationDAO.getAllIrrigations();
        if (list.isEmpty()) {
            System.out.println("No irrigation records found.");
        } else {
            System.out.println("----- All Irrigation Records -----");
            for (Irrigation irrigation : list) {
                System.out.println(irrigation);
            }
        }
    }

    private void viewIrrigationByField() {
        System.out.print("Enter Field ID: ");
        String fieldId = scanner.nextLine();

        List<Irrigation> list = irrigationDAO.getIrrigationByFieldId(fieldId);
        if (list.isEmpty()) {
            System.out.println("No irrigation records found for this field.");
        } else {
            System.out.println("----- Irrigation Records for Field " + fieldId + " -----");
            for (Irrigation irrigation : list) {
                System.out.println(irrigation);
            }
        }
    }
}