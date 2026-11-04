# AgriMonitor – Smart Agricultural Monitoring System

AgriMonitor is a Java desktop application with a MongoDB Atlas cloud backend. It helps farmers and farm supervisors manage field, crop, irrigation, pesticide, cost, sales and profit information through a custom-built Java Swing interface.

The system provides secure user authentication, agricultural record management, automatic profit/loss calculation, live weather information and exportable reports.

## Features

- User registration and login with hashed passwords
- Private, isolated data for every user
- **Home dashboard** with a live summary of sales, cost, profit margin and top crops, plus quick access to every module
- **Field Management** – add, update, delete and search fields, with a land-tile overview
- **Crop Management** – crop records linked to fields, with a growth timeline
- **Irrigation Management** – water usage records with a usage chart
- **Pesticide Log** – pesticide usage tracking per field
- **Cost Management** – categorised expenses with per-crop totals
- **Sales Management** – sales records with buyer details and per-crop totals
- **Profit / Loss** – automatic calculation with a donut-chart breakdown and crop-wise results
- **Weather** – real-time data from OpenWeatherMap with farming recommendations and a search history
- **Report Generation** – eight report types, which can be viewed, copied and exported to PDF, DOCX and TXT
- **Profile** – account details for the signed-in user
- Cloud storage with MongoDB Atlas
- Custom animated dashboard cards, drawn with pure Java2D (no external UI library)

## Video

https://youtu.be/H7u584VSkHY

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Core Java |
| GUI | Java Swing, AWT, Java2D |
| Database | MongoDB Atlas (cloud) |
| DB connectivity | MongoDB Java Driver 4.11.1 |
| Weather API | OpenWeatherMap |
| Report export | OpenPDF (PDF), Apache POI (DOCX) |
| Packaging | Java Compiler and jpackage |

## How to Run

### Option 1: Download the Installer (Recommended)

1. Open the [AgriMonitor Releases](https://github.com/alaminsarder/AgriMonitor/releases) page.
2. Download the latest `AgriMonitor-x.x.x.exe` installer.
3. Run the installer and follow the setup instructions.
4. Launch AgriMonitor from the Desktop or Start Menu shortcut.
5. Register a new account and start managing your agricultural records.

**Note:** If no installer is available, check the Releases page for the latest published version or use Option 2.

### Option 2: Build and Run from Source

#### Prerequisites

- JDK 8 or higher (JDK 11 or newer is recommended)
- Git
- A MongoDB database (MongoDB Atlas or a local MongoDB)
- An OpenWeatherMap API key (only needed for the weather feature)
- The required libraries in the `libs/` folder (see below)

#### Step 1: Clone the Repository

```bash
git clone https://github.com/alaminsarder/AgriMonitor.git
cd AgriMonitor
```

#### Step 2: Add the Required Libraries

The `libs/` folder must contain these jars:

| Purpose | Jars |
|---|---|
| MongoDB | `bson-4.11.1`, `mongodb-driver-core-4.11.1`, `mongodb-driver-sync-4.11.1` |
| JSON | `json-20231013` |
| PDF export | `openpdf-1.3.30` |
| DOCX export | `poi-5.2.5`, `poi-ooxml-5.2.5`, `poi-ooxml-lite-5.2.5`, `xmlbeans-5.2.0` |
| POI dependencies | `commons-collections4-4.4`, `commons-compress-1.25.0`, `commons-io-2.15.0`, `commons-math3-3.6.1`, `curvesapi-1.08`, `log4j-api-2.21.1`, `SparseBitSet-1.3` |

All of them can be downloaded from Maven Central: https://repo1.maven.org/maven2/

#### Step 3: Configure the Environment

Create a file named `.env` in the project root (next to `src` and `libs`):

```env
MONGO_URI=your_mongodb_connection_string
WEATHER_API_KEY=your_openweathermap_api_key
```

- For MongoDB Atlas, add your IP address under **Network Access**.
- `.env` is listed in `.gitignore`. Never commit it.

#### Step 4: Compile

**Windows PowerShell** (run from the project root):

```powershell
Remove-Item -Recurse -Force out -ErrorAction SilentlyContinue
New-Item -ItemType Directory out | Out-Null
$files = Get-ChildItem -Recurse src\main\java -Filter *.java | ForEach-Object { $_.FullName }
javac -cp "libs/*" -d out -encoding UTF-8 $files
```

**Windows Command Prompt:**

```bat
dir /s /b src\main\java\*.java > sources.txt
javac -cp "libs/*" -d out -encoding UTF-8 @sources.txt
```

**Linux / macOS:**

```bash
find src/main/java -name "*.java" > sources.txt
javac -cp "libs/*" -d out -encoding UTF-8 @sources.txt
```

> PowerShell does not expand `*.java` wildcards for `javac`, so use the PowerShell block above instead of the one-line wildcard command.

#### Step 5: Run

**Windows:**

```bash
java -cp "out;libs/*" com.agrimonitor.Main
```

**Linux / macOS:**

```bash
java -cp "out:libs/*" com.agrimonitor.Main
```

Always run from the project root, so that `.env` and `libs/` are found.

## Project Structure

```text
AgriMonitor
├── libs/                         Required jar libraries
├── .env                          Local secrets (not committed)
└── src/main/java/com/agrimonitor
    ├── Main.java                 Application entry point
    ├── gui/                      Swing panels, frames and UI helpers
    ├── dao/                      MongoDB data access classes
    ├── db/                       Database connection
    ├── model/                    Data models
    ├── service/                  Business logic, reports and export
    ├── session/                  Logged-in user session
    ├── menu/                     Navigation items
    └── util/                     Utilities (password hashing)
```

## Usage Guide

1. Register an account and log in.
2. Add your **Fields** first. Crops, irrigation, pesticide and cost records refer to a Field ID.
3. Add **Crops** linked to those fields. Cost and sales records refer to a Crop ID.
4. Record **Irrigation**, **Pesticide**, **Cost** and **Sales** entries as work happens.
5. Open **Profit / Loss** to see totals, margin and the crop-wise result.
6. Open **Report Generation** to view a report and export it as PDF, DOCX or TXT.
7. Use the **Home** page to see the whole farm at a glance and jump to any module.

## Security Notes

- Passwords are stored as hashes, never as plain text.
- Every record belongs to the user who created it, and users cannot see each other's data.
- Keep database credentials and API keys private. Do not commit passwords, connection strings or API keys.
- If a credential is ever exposed, rotate it immediately in MongoDB Atlas or OpenWeatherMap.

## Troubleshooting

| Problem | Fix |
|---|---|
| `javac` says no source files or invalid flag in PowerShell | Use the PowerShell compile block in Step 4 |
| `package ... does not exist` for PDF or POI classes | A jar is missing in `libs/`, so check the table in Step 2 |
| `illegal character: '\ufeff'` | A file was saved with a BOM. Re-save it as UTF-8 without BOM |
| Cannot connect to the database | Check `.env` and the Atlas IP allow-list |
| Weather does not load | Check `WEATHER_API_KEY` and your internet connection |
| `SLF4J: Failed to load class` | A harmless warning that can be ignored |

## Project Team

| Name | Roll | Student ID |
|---|---:|---|
| Md. Samiul Mia | 01 | 0322410105101001 |
| Md Hasan Ruhanee Ruhan | 10 | 0322410105101010 |
| Md AL Amin Sarder | 11 | 0322410105101011 |

## License

This project is developed for academic purposes.
