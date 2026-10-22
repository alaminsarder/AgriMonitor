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
        setLayout(new BorderLayout(0, 15));
        setBackground(UITheme.BACKGROUND);

        mainCard = new JPanel(new BorderLayout(20, 10));
        mainCard.setBackground(UITheme.PRIMARY_DARK);
        mainCard.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        iconLabel = new JLabel("\u2600", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setPreferredSize(new Dimension(150, 150));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        conditionLabel = new JLabel("No data yet");
        conditionLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        conditionLabel.setForeground(Color.WHITE);

        temperatureLabel = new JLabel("Temperature: -- °C");
        temperatureLabel.setFont(UITheme.FONT_BODY);
        temperatureLabel.setForeground(Color.WHITE);

        humidityLabel = new JLabel("Humidity: -- %");
        humidityLabel.setFont(UITheme.FONT_BODY);
        humidityLabel.setForeground(Color.WHITE);

        windLabel = new JLabel("Wind Speed: -- km/h");
        windLabel.setFont(UITheme.FONT_BODY);
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

        JPanel controlCard = UITheme.createCardPanel();
        controlCard.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        JButton simulateButton = UITheme.createStyledButton("Simulate Weather", UITheme.PRIMARY);
        controlCard.add(simulateButton);

        JLabel cityLabel = UITheme.createFormLabel("City:");
        controlCard.add(Box.createRigidArea(new Dimension(15, 0)));
        controlCard.add(cityLabel);

        cityField = UITheme.createTextField();
        cityField.setPreferredSize(new Dimension(150, 35));
        cityField.setText("Dhaka");
        controlCard.add(cityField);

        JButton realWeatherButton = UITheme.createStyledButton("Get Real Weather", UITheme.INFO);
        controlCard.add(realWeatherButton);

        JPanel recommendationPanel = UITheme.createCardPanel();
        recommendationPanel.setLayout(new BorderLayout());
        recommendationPanel.add(UITheme.createCardTitle("Farming Recommendation"), BorderLayout.NORTH);

        recommendationArea = new JTextArea("Choose a weather source to get started.");
        recommendationArea.setEditable(false);
        recommendationArea.setLineWrap(true);
        recommendationArea.setWrapStyleWord(true);
        recommendationArea.setFont(UITheme.FONT_BODY);
        recommendationArea.setRows(3);
        recommendationPanel.add(recommendationArea, BorderLayout.CENTER);

        JPanel historyCard = UITheme.createCardPanel();
        historyCard.setLayout(new BorderLayout());
        historyCard.add(UITheme.createCardTitle("Weather History"), BorderLayout.NORTH);

        historyModel = new DefaultTableModel(new Object[]{"Time", "Source", "Condition", "Temperature", "Humidity", "Wind Speed"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        historyTable = new JTable(historyModel);
        UITheme.styleTable(historyTable);

        JScrollPane historyScroll = new JScrollPane(historyTable);
        historyScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        historyCard.add(historyScroll, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setBackground(UITheme.BACKGROUND);
        centerPanel.add(recommendationPanel, BorderLayout.NORTH);
        centerPanel.add(historyCard, BorderLayout.CENTER);

        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.Y_AXIS));
        northContainer.setBackground(UITheme.BACKGROUND);
        northContainer.add(mainCard);
        northContainer.add(Box.createRigidArea(new Dimension(0, 15)));
        northContainer.add(controlCard);

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