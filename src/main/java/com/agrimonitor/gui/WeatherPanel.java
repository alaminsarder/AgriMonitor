package com.agrimonitor.gui;

import com.agrimonitor.service.WeatherService;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WeatherPanel extends JPanel {

    private WeatherService weatherService;

    private JLabel iconLabel;
    private JLabel conditionLabel;
    private JLabel temperatureLabel;
    private JLabel humidityLabel;
    private JLabel windLabel;
    private JTextArea recommendationArea;
    private JTextField cityField;

    private DefaultTableModel historyModel;
    private JTable historyTable;

    private JPanel mainCard;

    public WeatherPanel() {
        weatherService = new WeatherService();
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel header = new JLabel("Weather Dashboard");
        header.setFont(new Font("Arial", Font.BOLD, 20));
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        mainCard = new JPanel(new BorderLayout(20, 10));
        mainCard.setBackground(new Color(34, 87, 59));
        mainCard.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        iconLabel = new JLabel("?", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setPreferredSize(new Dimension(150, 150));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        conditionLabel = new JLabel("No data yet");
        conditionLabel.setFont(new Font("Arial", Font.BOLD, 26));
        conditionLabel.setForeground(Color.WHITE);

        temperatureLabel = new JLabel("Temperature: -- °C");
        temperatureLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        temperatureLabel.setForeground(Color.WHITE);

        humidityLabel = new JLabel("Humidity: -- %");
        humidityLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        humidityLabel.setForeground(Color.WHITE);

        windLabel = new JLabel("Wind Speed: -- km/h");
        windLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        windLabel.setForeground(Color.WHITE);

        infoPanel.add(conditionLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(temperatureLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(humidityLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(windLabel);

        mainCard.add(iconLabel, BorderLayout.WEST);
        mainCard.add(infoPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Weather Source"));

        JButton simulateButton = new JButton("Simulate Weather");
        controlPanel.add(simulateButton);

        controlPanel.add(new JLabel("     City:"));
        cityField = new JTextField(15);
        cityField.setText("Dhaka");
        controlPanel.add(cityField);

        JButton realWeatherButton = new JButton("Get Real Weather");
        controlPanel.add(realWeatherButton);

        JPanel recommendationPanel = new JPanel(new BorderLayout());
        recommendationPanel.setBorder(BorderFactory.createTitledBorder("Farming Recommendation"));
        recommendationArea = new JTextArea("Choose a weather source to get started.");
        recommendationArea.setEditable(false);
        recommendationArea.setLineWrap(true);
        recommendationArea.setWrapStyleWord(true);
        recommendationArea.setFont(new Font("Arial", Font.PLAIN, 14));
        recommendationArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        recommendationArea.setRows(3);
        recommendationPanel.add(recommendationArea, BorderLayout.CENTER);

        historyModel = new DefaultTableModel(new Object[]{"Time", "Source", "Condition", "Temperature", "Humidity", "Wind Speed"}, 0);
        historyTable = new JTable(historyModel);
        historyTable.setRowHeight(24);
        JScrollPane historyScroll = new JScrollPane(historyTable);
        historyScroll.setBorder(BorderFactory.createTitledBorder("Weather History"));

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(recommendationPanel, BorderLayout.NORTH);
        centerPanel.add(historyScroll, BorderLayout.CENTER);

        JPanel northContainer = new JPanel(new BorderLayout());
        JPanel topWrap = new JPanel();
        topWrap.setLayout(new BoxLayout(topWrap, BoxLayout.Y_AXIS));
        topWrap.add(header);
        topWrap.add(mainCard);
        topWrap.add(controlPanel);

        northContainer.add(topWrap, BorderLayout.CENTER);

        add(northContainer, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        simulateButton.addActionListener(e -> simulateWeather());
        realWeatherButton.addActionListener(e -> fetchRealWeather());
    }

    private void simulateWeather() {
        String weather = weatherService.simulateWeather();
        int temperature = weatherService.simulateTemperature(weather);
        int humidity = weatherService.simulateHumidity(weather);
        int wind = weatherService.simulateWindSpeed(weather);
        String icon = weatherService.getWeatherIcon(weather);
        String recommendation = weatherService.getFarmingRecommendation(weather);

        updateDisplay(icon, weather, temperature, humidity, wind, recommendation, "Simulated");
    }

    private void fetchRealWeather() {
        String city = cityField.getText().trim();
        if (city.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a city name.");
            return;
        }

        try {
            JSONObject data = weatherService.fetchRealWeather(city);

            String condition = data.getJSONArray("weather").getJSONObject(0).getString("main");
            double temp = data.getJSONObject("main").getDouble("temp");
            int humidity = data.getJSONObject("main").getInt("humidity");
            double windSpeed = data.getJSONObject("wind").getDouble("speed") * 3.6;

            String icon = weatherService.getWeatherIcon(condition);
            String recommendation = weatherService.getFarmingRecommendation(condition);

            updateDisplay(icon, condition + " (" + city + ")", (int) temp, humidity, (int) windSpeed, recommendation, "Real-Time");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not fetch weather data.\nCheck city name, internet connection, or API key.\n\nError: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateDisplay(String icon, String condition, int temperature, int humidity, int wind, String recommendation, String source) {
        iconLabel.setText(icon);
        conditionLabel.setText(condition);
        temperatureLabel.setText("Temperature: " + temperature + " °C");
        humidityLabel.setText("Humidity: " + humidity + " %");
        windLabel.setText("Wind Speed: " + wind + " km/h");
        recommendationArea.setText(recommendation);

        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        historyModel.insertRow(0, new Object[]{time, source, condition, temperature + " °C", humidity + " %", wind + " km/h"});
    }
}