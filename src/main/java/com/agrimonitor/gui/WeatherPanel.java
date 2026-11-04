package com.agrimonitor.gui;

import com.agrimonitor.service.WeatherService;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WeatherPanel extends JPanel {

    private final WeatherService weatherService = new WeatherService();

    private final JLabel iconLabel = new JLabel("\u2600", SwingConstants.CENTER);
    private final JLabel conditionLabel = new JLabel("No data yet");
    private final JLabel sourceLabel = new JLabel("Enter a city and press Get Real Weather");
    private final JLabel temperatureLabel = new JLabel("-- \u00B0C");
    private final JLabel humidityLabel = new JLabel("-- %");
    private final JLabel windLabel = new JLabel("-- km/h");
    private final JTextArea recommendationArea = new JTextArea("Get the real weather of your city to see a farming recommendation.");
    private final JTextField cityField = UITheme.createTextField();

    private final DefaultTableModel historyModel = UIKit.model("Time", "Source", "Condition", "Temperature", "Humidity", "Wind Speed");
    private final JTable historyTable = new JTable(historyModel);

    static class HeroPanel extends JPanel {
        HeroPanel(LayoutManager layout) {
            super(layout);
            setOpaque(false);
            setBorder(new EmptyBorder(24, 32, 24, 32));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setPaint(new GradientPaint(0, 0, new Color(22, 46, 38), w, h, UITheme.PRIMARY));
            g2.fillRoundRect(0, 0, w, h, 20, 20);
            g2.setColor(new Color(255, 255, 255, 14));
            g2.fillOval(w - 230, -80, 320, 320);
            g2.setColor(new Color(230, 200, 116, 28));
            g2.fillOval(w - 120, h - 90, 200, 200);
            g2.setColor(UITheme.ACCENT);
            g2.fillRoundRect(0, h - 4, w, 4, 4, 4);
            g2.dispose();
        }
    }

    public WeatherPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BACKGROUND);
        setBorder(new EmptyBorder(22, 26, 22, 26));

        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 72));
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setPreferredSize(new Dimension(130, 110));

        JLabel caption = new JLabel("CURRENT CONDITION");
        caption.setFont(new Font("Segoe UI", Font.BOLD, 11));
        caption.setForeground(UITheme.PRIMARY_LIGHT);

        conditionLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        conditionLabel.setForeground(Color.WHITE);

        sourceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sourceLabel.setForeground(new Color(214, 226, 218));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        info.add(Box.createVerticalGlue());
        info.add(caption);
        info.add(Box.createVerticalStrut(4));
        info.add(conditionLabel);
        info.add(Box.createVerticalStrut(6));
        info.add(sourceLabel);
        info.add(Box.createVerticalGlue());

        HeroPanel hero = new HeroPanel(new BorderLayout(24, 0));
        hero.add(iconLabel, BorderLayout.WEST);
        hero.add(info, BorderLayout.CENTER);

        JPanel stats = UIKit.statRow(
                UIKit.statCard("Temperature", temperatureLabel, UITheme.DANGER),
                UIKit.statCard("Humidity", humidityLabel, UITheme.INFO),
                UIKit.statCard("Wind Speed", windLabel, UITheme.ACCENT));

        JButton realWeatherButton = UITheme.createStyledButton("Get Real Weather", UITheme.INFO);

        JLabel cityLabel = new JLabel("CITY");
        cityLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        cityLabel.setForeground(UITheme.TEXT_LIGHT);

        cityField.setText("Dhaka");
        cityField.setPreferredSize(new Dimension(190, 40));

        JPanel controlRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        controlRow.setOpaque(false);
        controlRow.add(cityLabel);
        controlRow.add(cityField);
        controlRow.add(realWeatherButton);

        JPanel controlCard = UIKit.card(new BorderLayout());
        controlCard.add(controlRow, BorderLayout.CENTER);

        recommendationArea.setEditable(false);
        recommendationArea.setLineWrap(true);
        recommendationArea.setWrapStyleWord(true);
        recommendationArea.setOpaque(false);
        recommendationArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        recommendationArea.setForeground(UITheme.TEXT_DARK);
        recommendationArea.setRows(2);
        recommendationArea.setBorder(new EmptyBorder(2, 2, 2, 2));

        JPanel recommendationCard = UIKit.formCard("Farming Recommendation", recommendationArea, null);

        JButton clearHistory = UITheme.createStyledButton("Clear History", UITheme.TEXT_LIGHT);
        clearHistory.addActionListener(e -> historyModel.setRowCount(0));

        JPanel historyCard = UIKit.tableCard("Weather History", historyTable, "Search history...", clearHistory);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 16));
        centerPanel.setOpaque(false);
        centerPanel.add(recommendationCard, BorderLayout.NORTH);
        centerPanel.add(historyCard, BorderLayout.CENTER);

        add(UIKit.stack(hero, stats, controlCard), BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        realWeatherButton.addActionListener(e -> fetchRealWeather());
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
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());

        iconLabel.setText(icon);
        conditionLabel.setText(condition);
        sourceLabel.setText(source + " data  \u2022  updated " + time);
        temperatureLabel.setText(temperature + " \u00B0C");
        humidityLabel.setText(humidity + " %");
        windLabel.setText(wind + " km/h");
        recommendationArea.setText(recommendation);

        historyModel.insertRow(0, new Object[]{time, source, condition, temperature + " \u00B0C", humidity + " %", wind + " km/h"});
    }
}
