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





### Sales Management




### Profit Loss Calculation



### Report Generation

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
5. ### 5. Run the Application

Copy and run the following command:

java -cp "out;libs/*" com.agrimonitor.Main



## Project Team

Md. Samiul Mia - Roll 01 - ID 0322410105101001

Md Hasan Ruhanee Ruhan - Roll 10 - ID 0322410105101010

Md AL Amin Sarder - Roll 11 - ID 0322410105101011

## License

This project is developed for academic purposes.
