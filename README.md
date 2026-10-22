# AgriMonitor – Smart Agricultural Monitoring System

AgriMonitor is a Java-based desktop application with a MongoDB Atlas (cloud) backend that helps farmers and farm supervisors manage field, crop, irrigation, pesticide, cost, sales, and profit-related information through an easy-to-use GUI built with Java Swing.

## Features

- User Login & Registration (each user's data is kept private and isolated)
- Field and Crop Management
- Irrigation Record Tracking
- Pesticide Usage Log
- Cost and Sales Management
- Automatic Profit/Loss Calculation
- Weather Simulation with real-time OpenWeatherMap data
- Report Generation
- Persistent Data Storage with MongoDB Atlas (cloud database)

## Screenshots

### Login / Register

### Main Dashboard

### Sales Management

### Profit Loss Calculation

### Report Generation

## Tech Stack

- Language: Core Java
- GUI: Java Swing
- Database: MongoDB Atlas (Cloud)
- Database Connectivity: MongoDB Java Driver
- Weather API: OpenWeatherMap

## Project Structure

AgriMonitor/
- model: Data entities
- dao: MongoDB CRUD operations
- service: Business logic for Profit/Loss, Weather, and Reports
- gui: Swing based user interface
- session: Logged-in user session management
- util: Password hashing and environment config loading
- Main.java: Application entry point

## How to Run

### Option 1: Download the Installer (Recommended)

1. Go to the [Releases](https://github.com/alaminsarder/AgriMonitor/releases) page
2. Download the latest `AgriMonitor-x.x.x.exe`
3. Run the installer and follow the setup steps
4. Launch AgriMonitor from the Desktop/Start Menu shortcut
5. Register a new account and start using the app

### Option 2: Build from Source

1. Install JDK 8 or higher
2. Clone the repository
