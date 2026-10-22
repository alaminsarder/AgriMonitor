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
        setLayout(new BorderLayout(0, 15));
        setBackground(UITheme.BACKGROUND);

        add(buildFormCard(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);

        loadAllSales();
    }

    private JPanel buildFormCard() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout());

        card.add(UITheme.createCardTitle("Sales Information"), BorderLayout.NORTH);

        JPanel fieldsGrid = new JPanel(new GridLayout(2, 4, 15, 12));
        fieldsGrid.setBackground(UITheme.CARD_BG);

        cropIdField = UITheme.createTextField();
        cropNameField = UITheme.createTextField();
        quantitySoldField = UITheme.createTextField();
        priceField = UITheme.createTextField();
        dateField = UITheme.createTextField();
        buyerField = UITheme.createTextField();
        searchCropIdField = UITheme.createTextField();

        fieldsGrid.add(UITheme.labeledField("Crop ID", cropIdField));
        fieldsGrid.add(UITheme.labeledField("Crop Name", cropNameField));
        fieldsGrid.add(UITheme.labeledField("Quantity Sold", quantitySoldField));
        fieldsGrid.add(UITheme.labeledField("Selling Price Per Unit", priceField));
        fieldsGrid.add(UITheme.labeledField("Selling Date (YYYY-MM-DD)", dateField));
        fieldsGrid.add(UITheme.labeledField("Buyer Information", buyerField));
        fieldsGrid.add(UITheme.labeledField("Search Total Sales by Crop ID", searchCropIdField));

        card.add(fieldsGrid, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(UITheme.CARD_BG);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        buttonPanel.setBackground(UITheme.CARD_BG);

        JButton addButton = UITheme.createStyledButton("Add Sales Record", UITheme.PRIMARY);
        JButton refreshButton = UITheme.createStyledButton("View All", UITheme.NEUTRAL);
        JButton searchButton = UITheme.createStyledButton("Get Total Sales", UITheme.INFO);
        JButton clearButton = UITheme.createStyledButton("Clear", UITheme.TEXT_LIGHT);

        buttonPanel.add(addButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(searchButton);
        buttonPanel.add(clearButton);

        totalLabel = new JLabel(" ");
        totalLabel.setFont(UITheme.FONT_SUBHEADER);
        totalLabel.setForeground(UITheme.SUCCESS);
        totalLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        bottomPanel.add(buttonPanel);
        bottomPanel.add(totalLabel);

        card.add(bottomPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addSale());
        refreshButton.addActionListener(e -> loadAllSales());
        searchButton.addActionListener(e -> getTotalSales());
        clearButton.addActionListener(e -> clearForm());

        return card;
    }

    private JPanel buildTableCard() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout());

        card.add(UITheme.createCardTitle("All Sales Records"), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"Crop ID", "Crop Name", "Qty Sold", "Price/Unit", "Total", "Date", "Buyer"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
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