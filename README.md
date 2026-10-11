# AgriMonitor – Smart Agricultural Monitoring System

AgriMonitor is a Java-based desktop application with a MongoDB backend that helps farmers and farm supervisors manage field, crop, irrigation, pesticide, cost, sales, and profit-related information through an easy-to-use GUI built with Java Swing.

## Features

- Field and Crop Management
- Irrigation Record Tracking
- Pesticide Usage Log
- Cost and Sales Management
- Automatic Profit/Loss Calculation
- Weather Simulation
- Report Generation
- Persistent Data Storage with MongoDB

## Screenshots

### Main Dashboard
Main Dashboard

<img width="1088" height="640" alt="Screenshot_1" src="https://github.com/user-attachments/assets/b7ea6f85-478e-4c8d-8779-76c0ce9fe388" />



### Sales Management
Sales Management

<img width="1088" height="640" alt="Screenshot_2" src="https://github.com/user-attachments/assets/df4fc771-a871-4ac0-8fc0-a76488dca580" />


### Profit Loss Calculation
Pending..

### Report Generation
Pending..

## Tech Stack

- Language: Core Java
- GUI: Java Swing
- Database: MongoDB
- Database Connectivity: MongoDB Java Driver

## Project Structure

AgriMonitor/
- model: Data entities
- dao: MongoDB CRUD operations
- service: Business logic for Profit/Loss, Weather, and Reports
- gui: Swing based user interface
- Main.java: Application entry point

## How to Run

1. Install JDK 8 or higher and MongoDB
2. Clone the repository
   git clone https://github.com/alaminsarder/AgriMonitor.git
3. Make sure MongoDB service is running
4. Compile the project
   javac -cp "libs/*" -d out -encoding UTF-8 src/main/java/com/agrimonitor/*.java src/main/java/com/agrimonitor/*/*.java
5. Run the application
   java -cp "out;libs/*" com.agrimonitor.Main

## Project Team

Md. Samiul Mia - Roll 01 - ID 0322410105101001

Md Hasan Ruhanee Ruhan - Roll 10 - ID 0322410105101010

Md AL Amin Sarder - Roll 11 - ID 0322410105101011

## License

This project is developed for academic purposes.
