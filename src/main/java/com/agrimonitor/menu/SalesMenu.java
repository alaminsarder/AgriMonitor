package com.agrimonitor.menu;

import com.agrimonitor.dao.CropDAO;
import com.agrimonitor.dao.SalesDAO;
import com.agrimonitor.model.Sales;

import java.util.List;
import java.util.Scanner;

public class SalesMenu {

    private SalesDAO salesDAO;
    private CropDAO cropDAO;
    private Scanner scanner;

    public SalesMenu(Scanner scanner) {
        this.salesDAO = new SalesDAO();
        this.cropDAO = new CropDAO();
        this.scanner = scanner;
    }

    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println("\n----- Sales Management -----");
            System.out.println("1. Add Sales Record");
            System.out.println("2. View All Sales Records");
            System.out.println("3. View Total Sales by Crop ID");
            System.out.println("4. Back to Main Menu");
            System.out.print("Enter your choice: ");

            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    addSale();
                    break;
                case 2:
                    viewAllSales();
                    break;
                case 3:
                    viewTotalSalesByCrop();
                    break;
                case 4:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void addSale() {
        System.out.print("Enter Crop ID: ");
        String cropId = scanner.nextLine();

        if (!cropDAO.cropExists(cropId)) {
            System.out.println("Crop ID does not exist.");
            return;
        }

        System.out.print("Enter Crop Name: ");
        String cropName = scanner.nextLine();

        System.out.print("Enter Quantity Sold: ");
        double quantitySold = Double.parseDouble(scanner.nextLine());

        System.out.print("Enter Selling Price Per Unit: ");
        double price = Double.parseDouble(scanner.nextLine());

        System.out.print("Enter Selling Date (YYYY-MM-DD): ");
        String date = scanner.nextLine();

        System.out.print("Enter Buyer Information: ");
        String buyerInfo = scanner.nextLine();

        Sales sale = new Sales(cropId, cropName, quantitySold, price, date, buyerInfo);
        boolean success = salesDAO.addSale(sale);

        if (success) {
            System.out.println("Sales record added successfully.");
            System.out.println("Total Sales Amount: " + sale.getTotalSales() + " BDT");
        } else {
            System.out.println("Failed to add sales record.");
        }
    }

    private void viewAllSales() {
        List<Sales> list = salesDAO.getAllSales();
        if (list.isEmpty()) {
            System.out.println("No sales records found.");
        } else {
            System.out.println("----- All Sales Records -----");
            for (Sales sale : list) {
                System.out.println(sale);
            }
        }
    }

    private void viewTotalSalesByCrop() {
        System.out.print("Enter Crop ID: ");
        String cropId = scanner.nextLine();

        double total = salesDAO.getTotalSalesByCropId(cropId);
        System.out.println("Total Sales for Crop " + cropId + ": " + total + " BDT");
    }
}