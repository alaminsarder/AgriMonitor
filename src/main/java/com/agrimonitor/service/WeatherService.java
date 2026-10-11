package com.agrimonitor.service;

import java.util.Random;

public class WeatherService {

    private String[] weatherConditions = {"Sunny", "Cloudy", "Rainy", "Stormy"};

    public String simulateWeather() {
        Random random = new Random();
        int index = random.nextInt(weatherConditions.length);
        return weatherConditions[index];
    }

    public void displayWeather() {
        String weather = simulateWeather();
        System.out.println("Simulated Weather Condition: " + weather);
    }
}