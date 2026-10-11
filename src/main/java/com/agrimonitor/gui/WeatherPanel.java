package com.agrimonitor.gui;

import com.agrimonitor.service.WeatherService;

import javax.swing.*;
import java.awt.*;

public class WeatherPanel extends JPanel {

    private WeatherService weatherService;
    private JLabel resultLabel;

    public WeatherPanel() {
        weatherService = new WeatherService();
        setLayout(new GridBagLayout());

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(BorderFactory.createTitledBorder("Weather Simulation"));

        JButton simulateButton = new JButton("Simulate Weather");
        simulateButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        resultLabel = new JLabel("Weather: -");
        resultLabel.setFont(new Font("Arial", Font.BOLD, 24));
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        container.add(Box.createVerticalStrut(20));
        container.add(simulateButton);
        container.add(Box.createVerticalStrut(30));
        container.add(resultLabel);
        container.add(Box.createVerticalStrut(20));

        add(container);

        simulateButton.addActionListener(e -> {
            String weather = weatherService.simulateWeather();
            resultLabel.setText("Weather: " + weather);
        });
    }
}