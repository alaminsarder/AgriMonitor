package com.agrimonitor.menu;

import com.agrimonitor.dao.FieldDAO;
import com.agrimonitor.model.Field;

import java.util.List;
import java.util.Scanner;

public class FieldMenu {

    private FieldDAO fieldDAO;
    private Scanner scanner;

    public FieldMenu(Scanner scanner) {
        this.fieldDAO = new FieldDAO();
        this.scanner = scanner;
    }

    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println("\n----- Field Management -----");
            System.out.println("1. Add Field");
            System.out.println("2. View All Fields");
            System.out.println("3. Update Field");
            System.out.println("4. Delete Field");
            System.out.println("5. Back to Main Menu");
            System.out.print("Enter your choice: ");

            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    addField();
                    break;
                case 2:
                    viewFields();
                    break;
                case 3:
                    updateField();
                    break;
                case 4:
                    deleteField();
                    break;
                case 5:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void addField() {
        System.out.print("Enter Field ID: ");
        String fieldId = scanner.nextLine();

        if (fieldDAO.fieldExists(fieldId)) {
            System.out.println("Field ID already exists.");
            return;
        }

        System.out.print("Enter Field Name: ");
        String fieldName = scanner.nextLine();

        System.out.print("Enter Location: ");
        String location = scanner.nextLine();

        System.out.print("Enter Area Size (in acres): ");
        double areaSize = Double.parseDouble(scanner.nextLine());

        Field field = new Field(fieldId, fieldName, location, areaSize);
        boolean success = fieldDAO.addField(field);

        if (success) {
            System.out.println("Field added successfully.");
        } else {
            System.out.println("Failed to add field.");
        }
    }

    private void viewFields() {
        List<Field> fields = fieldDAO.getAllFields();
        if (fields.isEmpty()) {
            System.out.println("No fields found.");
        } else {
            System.out.println("----- All Fields -----");
            for (Field field : fields) {
                System.out.println(field);
            }
        }
    }

    private void updateField() {
        System.out.print("Enter Field ID to update: ");
        String fieldId = scanner.nextLine();

        if (!fieldDAO.fieldExists(fieldId)) {
            System.out.println("Field ID not found.");
            return;
        }

        System.out.print("Enter New Field Name: ");
        String fieldName = scanner.nextLine();

        System.out.print("Enter New Location: ");
        String location = scanner.nextLine();

        System.out.print("Enter New Area Size: ");
        double areaSize = Double.parseDouble(scanner.nextLine());

        boolean success = fieldDAO.updateField(fieldId, fieldName, location, areaSize);

        if (success) {
            System.out.println("Field updated successfully.");
        } else {
            System.out.println("Failed to update field.");
        }
    }

    private void deleteField() {
        System.out.print("Enter Field ID to delete: ");
        String fieldId = scanner.nextLine();

        boolean success = fieldDAO.deleteField(fieldId);

        if (success) {
            System.out.println("Field deleted successfully.");
        } else {
            System.out.println("Field ID not found.");
        }
    }
}