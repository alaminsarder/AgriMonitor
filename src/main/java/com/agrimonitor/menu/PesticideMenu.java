package com.agrimonitor.menu;

import com.agrimonitor.dao.FieldDAO;
import com.agrimonitor.dao.PesticideDAO;
import com.agrimonitor.model.Pesticide;

import java.util.List;
import java.util.Scanner;

public class PesticideMenu {

    private PesticideDAO pesticideDAO;
    private FieldDAO fieldDAO;
    private Scanner scanner;

    public PesticideMenu(Scanner scanner) {
        this.pesticideDAO = new PesticideDAO();
        this.fieldDAO = new FieldDAO();
        this.scanner = scanner;
    }

    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println("\n----- Pesticide Management -----");
            System.out.println("1. Record Pesticide Usage");
            System.out.println("2. View All Pesticide Records");
            System.out.println("3. View Pesticide by Field ID");
            System.out.println("4. Back to Main Menu");
            System.out.print("Enter your choice: ");

            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    addPesticide();
                    break;
                case 2:
                    viewAllPesticides();
                    break;
                case 3:
                    viewPesticideByField();
                    break;
                case 4:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void addPesticide() {
        System.out.print("Enter Field ID: ");
        String fieldId = scanner.nextLine();

        if (!fieldDAO.fieldExists(fieldId)) {
            System.out.println("Field ID does not exist.");
            return;
        }

        System.out.print("Enter Pesticide Name: ");
        String pesticideName = scanner.nextLine();

        System.out.print("Enter Quantity: ");
        double quantity = Double.parseDouble(scanner.nextLine());

        System.out.print("Enter Application Date (YYYY-MM-DD): ");
        String date = scanner.nextLine();

        Pesticide pesticide = new Pesticide(fieldId, pesticideName, quantity, date);
        boolean success = pesticideDAO.addPesticide(pesticide);

        if (success) {
            System.out.println("Pesticide record added successfully.");
        } else {
            System.out.println("Failed to add pesticide record.");
        }
    }

    private void viewAllPesticides() {
        List<Pesticide> list = pesticideDAO.getAllPesticides();
        if (list.isEmpty()) {
            System.out.println("No pesticide records found.");
        } else {
            System.out.println("----- All Pesticide Records -----");
            for (Pesticide pesticide : list) {
                System.out.println(pesticide);
            }
        }
    }

    private void viewPesticideByField() {
        System.out.print("Enter Field ID: ");
        String fieldId = scanner.nextLine();

        List<Pesticide> list = pesticideDAO.getPesticideByFieldId(fieldId);
        if (list.isEmpty()) {
            System.out.println("No pesticide records found for this field.");
        } else {
            System.out.println("----- Pesticide Records for Field " + fieldId + " -----");
            for (Pesticide pesticide : list) {
                System.out.println(pesticide);
            }
        }
    }
}