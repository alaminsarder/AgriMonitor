package com.agrimonitor.service;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class WeatherService {

    private String[] weatherConditions = {"Sunny", "Cloudy", "Rainy", "Stormy"};
    private Random random = new Random();

    private static final String API_KEY = "764b8ca9b084145040e857d9206d2c49";
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric";

    public String simulateWeather() {
        int index = random.nextInt(weatherConditions.length);
        return weatherConditions[index];
    }

    public int simulateTemperature(String weather) {
        switch (weather) {
            case "Sunny": return 28 + random.nextInt(8);
            case "Cloudy": return 24 + random.nextInt(6);
            case "Rainy": return 20 + random.nextInt(5);
            case "Stormy": return 18 + random.nextInt(5);
            default: return 25;
        }
    }

    public int simulateHumidity(String weather) {
        switch (weather) {
            case "Sunny": return 30 + random.nextInt(20);
            case "Cloudy": return 50 + random.nextInt(20);
            case "Rainy": return 75 + random.nextInt(20);
            case "Stormy": return 80 + random.nextInt(20);
            default: return 50;
        }
    }

    public int simulateWindSpeed(String weather) {
        switch (weather) {
            case "Sunny": return 5 + random.nextInt(10);
            case "Cloudy": return 10 + random.nextInt(10);
            case "Rainy": return 15 + random.nextInt(15);
            case "Stormy": return 35 + random.nextInt(25);
            default: return 10;
        }
    }

    public String getFarmingRecommendation(String weather) {
        switch (weather) {
            case "Sunny": return "Good day for irrigation and field work. Monitor soil moisture levels regularly.";
            case "Cloudy": return "Favorable conditions for planting and transplanting activities.";
            case "Rainy": return "Avoid pesticide application. Ensure proper field drainage to prevent waterlogging.";
            case "Stormy": return "Postpone all field activities. Secure equipment and protect young crops.";
            case "Clear": return "Excellent conditions for irrigation and field work.";
            case "Clouds": return "Favorable conditions for planting and transplanting activities.";
            case "Rain": return "Avoid pesticide application. Ensure proper field drainage.";
            case "Thunderstorm": return "Postpone all field activities immediately. Secure equipment.";
            case "Snow": return "Protect crops from frost damage.";
            case "Mist": case "Fog": case "Haze": return "Low visibility, delay spraying activities.";
            default: return "Monitor conditions and plan activities accordingly.";
        }
    }

    public String getWeatherIcon(String weather) {
        switch (weather) {
            case "Sunny": case "Clear": return "☀";
            case "Cloudy": case "Clouds": return "☁";
            case "Rainy": case "Rain": case "Drizzle": return "🌧";
            case "Stormy": case "Thunderstorm": return "⛈";
            case "Snow": return "❄";
            case "Mist": case "Fog": case "Haze": return "🌫";
            default: return "?";
        }
    }

    public void displayWeather() {
        String weather = simulateWeather();
        System.out.println("Simulated Weather Condition: " + weather);
    }

    public JSONObject fetchRealWeather(String city) throws Exception {
        String urlString = String.format(API_URL, city.replace(" ", "+"), API_KEY);
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        int responseCode = connection.getResponseCode();

        if (responseCode != 200) {
            throw new Exception("City not found or API error. Response Code: " + responseCode);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        return new JSONObject(response.toString());
    }
}