package com.agrimonitor.gui;

import com.agrimonitor.dao.CropDAO;
import com.agrimonitor.dao.SalesDAO;
import com.agrimonitor.model.Sales;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SalesPanel extends JPanel {

    private SalesDAO salesDAO;
    private CropDAO cropDAO;
    private JTextField cropIdField, cropNameField, quantitySoldField, priceField, dateField, buyerField, searchCropIdField;
    private JLabel totalLabel;
    private DefaultTableModel tableModel;
    private JTable table;

    public SalesPanel() {
        salesDAO = new SalesDAO();
        cropDAO = new CropDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Sales Information"));

        formPanel.add(new JLabel("Crop ID:"));
        cropIdField = new JTextField();
        formPanel.add(cropIdField);

        formPanel.add(new JLabel("Crop Name:"));
        cropNameField = new JTextField();
        formPanel.add(cropNameField);

        formPanel.add(new JLabel("Quantity Sold:"));
        quantitySoldField = new JTextField();
        formPanel.add(quantitySoldField);

        formPanel.add(new JLabel("Selling Price Per Unit:"));
        priceField = new JTextField();
        formPanel.add(priceField);

        formPanel.add(new JLabel("Selling Date (YYYY-MM-DD):"));
        dateField = new JTextField();
        formPanel.add(dateField);

        formPanel.add(new JLabel("Buyer Information:"));
        buyerField = new JTextField();
        formPanel.add(buyerField);

        formPanel.add(new JLabel("Search Total Sales by Crop ID:"));
        searchCropIdField = new JTextField();
        formPanel.add(searchCropIdField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add Sales Record");
        JButton refreshButton = new JButton("View All");
        JButton searchButton = new JButton("Get Total Sales");
        JButton clearButton = new JButton("Clear");

        buttonPanel.add(addButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(searchButton);
        buttonPanel.add(clearButton);

        formPanel.add(buttonPanel);

        totalLabel = new JLabel(" ");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(totalLabel);

        tableModel = new DefaultTableModel(new Object[]{"Crop ID", "Crop Name", "Qty Sold", "Price/Unit", "Total", "Date", "Buyer"}, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        add(formPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        addButton.addActionListener(e -> addSale());
        refreshButton.addActionListener(e -> loadAllSales());
        searchButton.addActionListener(e -> getTotalSales());
        clearButton.addActionListener(e -> clearForm());

        loadAllSales();
    }

    private void addSale() {
        try {
            String cropId = cropIdField.getText().trim();
            String cropName = cropNameField.getText().trim();
            double quantitySold = Double.parseDouble(quantitySoldField.getText().trim());
            double price = Double.parseDouble(priceField.getText().trim());
            String date = dateField.getText().trim();
            String buyer = buyerField.getText().trim();

            if (cropId.isEmpty() || cropName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Crop ID and Crop Name are required.");
                return;
            }

            if (!cropDAO.cropExists(cropId)) {
                JOptionPane.showMessageDialog(this, "Crop ID does not exist.");
                return;
            }

            Sales sale = new Sales(cropId, cropName, quantitySold, price, date, buyer);
            boolean success = salesDAO.addSale(sale);

            if (success) {
                JOptionPane.showMessageDialog(this, "Sales record added successfully. Total Sales: " + sale.getTotalSales() + " BDT");
                clearForm();
                loadAllSales();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add sales record.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for Quantity and Price.");
        }
    }

    private void loadAllSales() {
        tableModel.setRowCount(0);
        List<Sales> list = salesDAO.getAllSales();
        for (Sales sale : list) {
            tableModel.addRow(new Object[]{
                    sale.getCropId(), sale.getCropName(), sale.getQuantitySold(),
                    sale.getSellingPricePerUnit(), sale.getTotalSales(),
                    sale.getSellingDate(), sale.getBuyerInfo()
            });
        }
    }

    private void getTotalSales() {
        String cropId = searchCropIdField.getText().trim();
        if (cropId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Crop ID.");
            return;
        }
        double total = salesDAO.getTotalSalesByCropId(cropId);
        totalLabel.setText("Total Sales for Crop " + cropId + ": " + total + " BDT");
    }

    private void clearForm() {
        cropIdField.setText("");
        cropNameField.setText("");
        quantitySoldField.setText("");
        priceField.setText("");
        dateField.setText("");
        buyerField.setText("");
        searchCropIdField.setText("");
        totalLabel.setText(" ");
    }
}