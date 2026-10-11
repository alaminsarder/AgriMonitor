package com.agrimonitor;

import com.agrimonitor.db.MongoDBConnection;
import com.agrimonitor.menu.*;
import com.agrimonitor.service.WeatherService;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        MongoDBConnection.getDatabase();

        Scanner scanner = new Scanner(System.in);
        WeatherService weatherService = new WeatherService();

        FieldMenu fieldMenu = new FieldMenu(scanner);
        CropMenu cropMenu = new CropMenu(scanner);
        IrrigationMenu irrigationMenu = new IrrigationMenu(scanner);
        PesticideMenu pesticideMenu = new PesticideMenu(scanner);
        CostMenu costMenu = new CostMenu(scanner);
        SalesMenu salesMenu = new SalesMenu(scanner);
        ReportMenu reportMenu = new ReportMenu(scanner);

        boolean exit = false;

        System.out.println("=====================================");
        System.out.println("   Welcome to AgriMonitor System");
        System.out.println("=====================================");

        while (!exit) {
            System.out.println("\n----- Main Menu -----");
            System.out.println("1. Field Management");
            System.out.println("2. Crop Management");
            System.out.println("3. Irrigation Management");
            System.out.println("4. Pesticide Management");
            System.out.println("5. Cost Management");
            System.out.println("6. Sales Management");
            System.out.println("7. Profit/Loss");
            System.out.println("8. Weather Simulation");
            System.out.println("9. Report Generation");
            System.out.println("10. Exit");
            System.out.print("Enter your choice: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1:
                    fieldMenu.show();
                    break;
                case 2:
                    cropMenu.show();
                    break;
                case 3:
                    irrigationMenu.show();
                    break;
                case 4:
                    pesticideMenu.show();
                    break;
                case 5:
                    costMenu.show();
                    break;
                case 6:
                    salesMenu.show();
                    break;
                case 7:
    com.agrimonitor.service.ProfitLossService profitLossService = new com.agrimonitor.service.ProfitLossService();
    profitLossService.displayProfitLossReport();
    break;
                case 8:
                    weatherService.displayWeather();
                    break;
                case 9:
                    reportMenu.show();
                    break;
                case 10:
                    exit = true;
                    System.out.println("Thank you for using AgriMonitor. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }

        MongoDBConnection.closeConnection();
        scanner.close();
    }
}