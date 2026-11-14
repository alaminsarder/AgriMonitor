# AgriMonitor – Smart Agricultural Monitoring System

AgriMonitor is a Java-based desktop application with a MongoDB Atlas cloud backend that helps farmers and farm supervisors manage field, crop, irrigation, pesticide, cost, sales, and profit-related information through an easy-to-use graphical user interface (GUI) built with Java Swing.

The system provides user authentication, agricultural record management, automatic profit/loss calculation, weather information, and report generation to support efficient farm management.

## Features

- User Login and Registration
- Private and Isolated User Data
- Field and Crop Management
- Irrigation Record Tracking
- Pesticide Usage Log
- Cost and Sales Management
- Automatic Profit/Loss Calculation
- Weather Simulation with Real-Time OpenWeatherMap Data
- Report Generation
- Persistent Data Storage with MongoDB Atlas (Cloud Database)
- Easy-to-Use Java Swing GUI

## Video
https://youtu.be/H7u584VSkHY

## Tech Stack

- **Programming Language:** Core Java
- **GUI Framework:** Java Swing
- **Database:** MongoDB Atlas (Cloud)
- **Database Connectivity:** MongoDB Java Driver
- **Weather API:** OpenWeatherMap
- **Build and Packaging:** Java Compiler and jpackage

## How to Run

### Option 1: Download the Installer (Recommended)

1. Visit the [AgriMonitor Releases](https://github.com/alaminsarder/AgriMonitor/releases) page.
2. Download the latest `AgriMonitor-x.x.x.exe` installer.
3. Run the installer and follow the setup instructions.
4. Launch AgriMonitor from the Desktop or Start Menu shortcut.
5. Register a new account and start managing your agricultural records.

**Note:** If no installer is available, check the Releases page for the latest published version or use Option 2.

### Option 2: Build and Run from Source

#### Prerequisites

- JDK 8 or higher
- Git
- Required MongoDB Java Driver libraries in the `libs/` directory
- A configured MongoDB database (local MongoDB or MongoDB Atlas)
- An OpenWeatherMap API key for weather functionality

#### Step 1: Clone the Repository

```bash
git clone https://github.com/alaminsarder/AgriMonitor.git
cd AgriMonitor
```

#### Step 2: Configure the Database

Configure the MongoDB connection string and required environment variables according to the project's configuration. For MongoDB Atlas, ensure that your IP address is allowed in the Atlas network access settings and that the database credentials are configured securely.

#### Step 3: Compile the Project

Run the following command in Windows PowerShell or Command Prompt from the project root directory:

```bash
javac -cp "libs/*" -d out -encoding UTF-8 src/main/java/com/agrimonitor/*.java src/main/java/com/agrimonitor/*/*.java
```

#### Step 4: Run the Application

**Required run command for Windows:**

```bash
java -cp "out;libs/*" com.agrimonitor.Main
```

Run the command from the project root directory after compilation. Make sure the `libs/` directory contains the required dependencies and that the database configuration is correct.

**For Linux/macOS**, use a colon (`:`) instead of a semicolon (`;`) in the classpath:

```bash
java -cp "out:libs/*" com.agrimonitor.Main
```

## Database and Configuration

AgriMonitor uses MongoDB for persistent data storage. The application can connect to MongoDB Atlas for cloud-based storage.

Keep database credentials and API keys private. Do not commit passwords, connection strings containing credentials, or API keys to the public repository.

## Forgot Password (email code)

Users enter an email when they register. On the login screen, **Forgot password?** emails a 6-digit code to that address (valid 10 minutes, max 5 wrong tries, 60 s between resends). After entering the code the user sets a new password. Accounts created before this feature are asked for their email the next time they sign in.

The app sends the email through SMTP (Gmail by default) using a sender account. Add these to the `.env` file in the project root:

```
MONGODB_URI=your-mongodb-connection-string
SMTP_EMAIL=sender-account@gmail.com
SMTP_PASSWORD=your-16-character-gmail-app-password
```

For Gmail, turn on 2-Step Verification and create an **App Password** (Google Account > Security > App passwords). Do not use the normal Gmail password. Optional: `SMTP_HOST` (default `smtp.gmail.com`) and `SMTP_PORT` (default `465`; any other port uses STARTTLS, e.g. `587`). See `.env.example`.

## Project Team

| Name | Roll | Student ID |
|---|---:|---|
| Md. Samiul Mia | 01 | 0322410105101001 |
| Md Hasan Ruhanee Ruhan | 10 | 0322410105101010 |
| Md AL Amin Sarder | 11 | 0322410105101011 |

## License

This project is developed for academic purposes.
