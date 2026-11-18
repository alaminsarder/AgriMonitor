# 🌾 AgriMonitor — Smart Agriculture Management System

A desktop-based **farm management system** built with **Java Swing**, backed by **MongoDB Atlas**, with an integrated **AI farming assistant** powered by **Google Gemini**.

AgriMonitor helps farmers keep every part of their farm in one place — fields, crops, irrigation, pesticide usage, costs, sales, profit/loss, weather — and get **AI advice based on their own real farm data**.

---

## ✨ Features

| Module | Description |
|---|---|
| 🏠 Dashboard | Overview cards with live farm statistics |
| 🌱 Field Management | Add, edit, delete fields (name, location, area) |
| 🌿 Crop Management | Track crops per field, variety, planting date, status |
| 💧 Irrigation Management | Log and track irrigation events per field |
| 🧪 Pesticide Log | Record pesticide/fertilizer applications |
| 💰 Cost Management | Track all farming expenses by category |
| 🛒 Sales Management | Record harvest sales and revenue |
| 📈 Profit / Loss | Automatic profit-loss calculation from costs vs sales |
| 🌦️ Weather | Current weather information for farm planning |
| 🤖 AI Assistant | Gemini-powered chatbot that knows **your** fields, crops and irrigation history |
| 📄 Report Generation | Generate farm reports |
| 👤 Profile | Account details and session info |

### 🤖 AI Assistant highlights

- Answers using your **real farm data** (fields, crops, irrigation records) — no generic guesses
- Chat history is **saved per user** in MongoDB (`chat_messages` collection)
- Understands **Bangla and English**
- Automatically adapts to different Gemini models (handles `thinkingConfig` compatibility)

---

## 🛠️ Tech Stack

- **Language:** Java 17 (Swing GUI, custom-drawn vector icons — no image assets)
- **Database:** MongoDB Atlas (cloud)
- **AI:** Google Gemini API
- **JSON:** org.json
- **Architecture:** DAO pattern, service layer, session management

---

## 📁 Project Structure

```
AgriMonitor/
├── src/main/java/com/agrimonitor/
│   ├── Main.java                 # Application entry point
│   ├── dao/                      # Data Access Objects (MongoDB)
│   │   ├── FieldDAO.java
│   │   ├── CropDAO.java
│   │   ├── ChatHistoryDAO.java
│   │   └── ...
│   ├── db/                       # Database & environment
│   │   ├── MongoDBConnection.java
│   │   └── EnvLoader.java
│   ├── gui/                      # Swing UI
│   │   ├── MainFrame.java        # Sidebar, header, navigation
│   │   ├── LoginFrame.java
│   │   ├── HomePanel.java
│   │   ├── FieldPanel.java
│   │   ├── CropPanel.java
│   │   ├── IrrigationPanel.java
│   │   ├── PesticidePanel.java
│   │   ├── CostPanel.java
│   │   ├── SalesPanel.java
│   │   ├── ProfitLossPanel.java
│   │   ├── WeatherPanel.java
│   │   ├── ChatPanel.java        # AI Assistant UI
│   │   ├── ReportPanel.java
│   │   ├── ProfilePanel.java
│   │   ├── UITheme.java          # Colors, fonts, icons
│   │   └── UIKit.java            # Reusable UI components
│   ├── model/                    # Data models
│   ├── service/                  # Business logic
│   │   ├── AIChatService.java    # Gemini / OpenAI-compatible client
│   │   ├── FarmContextBuilder.java  # Builds farm context for the AI
│   │   ├── FarmAssistantPrompt.java
│   │   ├── ProfitLossService.java
│   │   └── EmailService.java
│   ├── session/                  # User session
│   └── util/                     # Helpers
├── libs/                         # External JARs (MongoDB driver, org.json, ...)
├── .env                          # Secrets (NOT committed to git)
├── .env.example                  # Template for .env
└── README.md
```

---

## ⚙️ Requirements

- **JDK 17 or newer** (JDK 8 will NOT work)
- Internet connection (MongoDB Atlas, Gemini API, weather)
- Windows / Linux / macOS

---

## 🚀 Setup

### 1. Clone the repository

```bash
git clone https://github.com/alaminsarder/AgriMonitor.git
cd AgriMonitor
```

### 2. Configure environment variables

Copy `.env.example` to `.env` and fill in your values:

```env
MONGODB_URI=mongodb+srv://<user>:<password>@<cluster>.mongodb.net/agrimonitor_db
AI_PROVIDER=gemini
AI_API_KEY=your-gemini-api-key
AI_MODEL=gemini-flash-latest
```

| Key | Description |
|---|---|
| `MONGODB_URI` | MongoDB Atlas connection string |
| `AI_PROVIDER` | `gemini` (default) or an OpenAI-compatible provider |
| `AI_API_KEY` | API key for the AI provider |
| `AI_MODEL` | Optional — defaults to `gemini-flash-latest` |
| `AI_BASE_URL` | Optional — only for OpenAI-compatible providers |


### 3. Compile and run

**Windows (PowerShell):**

```powershell
javac -cp "libs/*" -d out -encoding UTF-8 (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp "out;libs/*" com.agrimonitor.Main
```

**Linux / macOS:**

```bash
javac -cp "libs/*" -d out -encoding UTF-8 $(find src -name "*.java")
java -cp "out:libs/*" com.agrimonitor.Main
```

> Changing only `.env` does **not** require recompiling — just restart the app.

---

## 🗄️ Database

MongoDB Atlas database: **`agrimonitor_db`**

Main collections:

| Collection | Contents |
|---|---|
| `users` | Registered user accounts |
| `fields` | Farm fields (name, location, area) |
| `crops` | Crops linked to fields |
| `irrigations` | Irrigation event logs |
| `pesticides` | Pesticide application logs |
| `costs` | Expense records |
| `sales` | Sales records |
| `chat_messages` | AI chat history per user (`userId`, `role`, `text`, `createdAt`) |


## 👨‍💻 Author

**Alamin Sarder** — [github.com/alaminsarder](https://github.com/alaminsarder)

## 📄 License

This project is licensed under the MIT License.
