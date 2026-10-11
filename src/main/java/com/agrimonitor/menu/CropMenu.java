package com.agrimonitor.menu;

import com.agrimonitor.dao.CropDAO;
import com.agrimonitor.dao.FieldDAO;
import com.agrimonitor.model.Crop;

import java.util.List;
import java.util.Scanner;

public class CropMenu {

    private CropDAO cropDAO;
    private FieldDAO fieldDAO;
    private Scanner scanner;

    public CropMenu(Scanner scanner) {
        this.cropDAO = new CropDAO();
        this.fieldDAO = new FieldDAO();
        this.scanner = scanner;
    }

    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println("\n----- Crop Management -----");
            System.out.println("1. Add Crop");
            System.out.println("2. View All Crops");
            System.out.println("3. Update Crop");
            System.out.println("4. Back to Main Menu");
            System.out.print("Enter your choice: ");

            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    addCrop();
                    break;
                case 2:
                    viewCrops();
                    break;
                case 3:
                    updateCrop();
                    break;
                case 4:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void addCrop() {
        System.out.print("Enter Crop ID: ");
        String cropId = scanner.nextLine();

        if (cropDAO.cropExists(cropId)) {
            System.out.println("Crop ID already exists.");
            return;
        }

        System.out.print("Enter Crop Name: ");
        String cropName = scanner.nextLine();

        System.out.print("Enter Crop Type: ");
        String cropType = scanner.nextLine();

        System.out.print("Enter Growth Duration (days): ");
        int growthDuration = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter Field ID: ");
        String fieldId = scanner.nextLine();

        if (!fieldDAO.fieldExists(fieldId)) {
            System.out.println("Field ID does not exist. Please add field first.");
            return;
        }

        Crop crop = new Crop(cropId, cropName, cropType, growthDuration, fieldId);
        boolean success = cropDAO.addCrop(crop);

        if (success) {
            System.out.println("Crop added successfully.");
        } else {
            System.out.println("Failed to add crop.");
        }
    }

    private void viewCrops() {
        List<Crop> crops = cropDAO.getAllCrops();
        if (crops.isEmpty()) {
            System.out.println("No crops found.");
        } else {
            System.out.println("----- All Crops -----");
            for (Crop crop : crops) {
                System.out.println(crop);
            }
        }
    }

    private void updateCrop() {
        System.out.print("Enter Crop ID to update: ");
        String cropId = scanner.nextLine();

        if (!cropDAO.cropExists(cropId)) {
            System.out.println("Crop ID not found.");
            return;
        }

        System.out.print("Enter New Crop Name: ");
        String cropName = scanner.nextLine();

        System.out.print("Enter New Crop Type: ");
        String cropType = scanner.nextLine();

        System.out.print("Enter New Growth Duration (days): ");
        int growthDuration = Integer.parseInt(scanner.nextLine());

        boolean success = cropDAO.updateCrop(cropId, cropName, cropType, growthDuration);

        if (success) {
            System.out.println("Crop updated successfully.");
        } else {
            System.out.println("Failed to update crop.");
        }
    }
}