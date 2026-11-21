package com.agrimonitor.menu;

import com.agrimonitor.service.ReportService;

import java.util.Scanner;

public class ReportMenu {

    private ReportService reportService;
    private Scanner scanner;

    public ReportMenu(Scanner scanner) {
        this.reportService = new ReportService();
        this.scanner = scanner;
    }

    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println("\n----- Report Generation -----");
            System.out.println("1. Field Report");
            System.out.println("2. Crop Report");
            System.out.println("3. Irrigation Report");
            System.out.println("4. Pesticide Report");
            System.out.println("5. Cost Report");
            System.out.println("6. Sales Report");
            System.out.println("7. Profit/Loss Report");
            System.out.println("8. Overall Farm Report");
            System.out.println("9. Back to Main Menu");
            System.out.print("Enter your choice: ");

            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    reportService.generateFieldReport();
                    break;
                case 2:
                    reportService.generateCropReport();
                    break;
                case 3:
                    reportService.generateIrrigationReport();
                    break;
                case 4:
                    reportService.generatePesticideReport();
                    break;
                case 5:
                    reportService.generateCostReport();
                    break;
                case 6:
                    reportService.generateSalesReport();
                    break;
                case 7:
                    reportService.generateProfitLossReport();
                    break;
                case 8:
                    reportService.generateOverallFarmReport();
                    break;
                case 9:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
}