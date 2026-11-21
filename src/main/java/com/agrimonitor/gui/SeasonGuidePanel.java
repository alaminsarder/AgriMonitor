package com.agrimonitor.gui;

import com.agrimonitor.dao.SeasonCropDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SeasonGuidePanel extends JPanel {

    private static final String[] MONTHS = {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};
    private static final String[] SHORT = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    // name, season, sowFrom, sowTo, harvest, days
    private static final Object[][] CROPS = {
        {"Aus Rice", "Kharif-1", 3, 4, "Jul - Aug", 100},
        {"Aman Rice (seedbed)", "Kharif-2", 6, 7, "Nov - Dec", 135},
        {"Boro Rice (seedbed)", "Rabi", 11, 1, "Apr - Jun", 150},
        {"Wheat", "Rabi", 11, 12, "Mar", 105},
        {"Maize", "Rabi", 11, 12, "Mar - Apr", 140},
        {"Potato", "Rabi", 10, 11, "Jan - Mar", 90},
        {"Mustard", "Rabi", 10, 11, "Jan - Feb", 85},
        {"Lentil", "Rabi", 10, 11, "Feb - Mar", 110},
        {"Chickpea", "Rabi", 11, 12, "Mar - Apr", 110},
        {"Onion (transplant)", "Rabi", 11, 12, "Mar", 110},
        {"Jute", "Kharif-1", 3, 4, "Jul - Aug", 120},
        {"Mung Bean", "Kharif-1", 2, 4, "Apr - Jun", 65}
    };

    private final SeasonCropDAO dao = new SeasonCropDAO();
    private final List<Object[]> custom = new ArrayList<>();

    private final DefaultTableModel model = UIKit.model("Crop", "Season", "Sowing Months", "Harvest Months", "Duration (days)");
    private final JTable table = new JTable(model) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JComboBox<String> monthBox;
    private final JTextField sowField = UITheme.createTextField();
    private final JLabel resultLabel = new JLabel("Select a crop, enter sowing date (yyyy-MM-dd), then click Calculate.");

    public SeasonGuidePanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BACKGROUND);
        setBorder(new EmptyBorder(22, 26, 22, 26));

        String[] items = new String[13];
        items[0] = "All Months";
        System.arraycopy(MONTHS, 0, items, 1, 12);
        monthBox = UIKit.createComboBox(items);
        monthBox.setSelectedIndex(LocalDate.now().getMonthValue());
        monthBox.addActionListener(e -> reload());

        JLabel monthLabel = new JLabel("MONTH");
        monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        monthLabel.setForeground(UITheme.TEXT_LIGHT);

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filterRow.setOpaque(false);
        filterRow.add(monthLabel);
        filterRow.add(monthBox);

        JButton addBtn = UITheme.createStyledButton("Add Crop", UITheme.PRIMARY);
        addBtn.addActionListener(e -> addCrop());
        JButton delBtn = UITheme.createStyledButton("Delete Selected", UITheme.DANGER);
        delBtn.addActionListener(e -> deleteSelected());
        filterRow.add(addBtn);
        filterRow.add(delBtn);

        JPanel filterCard = UIKit.card(new BorderLayout());
        filterCard.add(filterRow, BorderLayout.CENTER);

        sowField.setText(LocalDate.now().toString());
        sowField.setPreferredSize(new Dimension(150, 40));
        JButton calcBtn = UITheme.createStyledButton("Calculate Harvest Date", UITheme.PRIMARY);
        calcBtn.addActionListener(e -> calculate());

        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        resultLabel.setForeground(UITheme.TEXT_DARK);

        JLabel sowLabel = new JLabel("SOWING DATE");
        sowLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        sowLabel.setForeground(UITheme.TEXT_LIGHT);

        JPanel calcRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        calcRow.setOpaque(false);
        calcRow.add(sowLabel);
        calcRow.add(sowField);
        calcRow.add(calcBtn);
        calcRow.add(resultLabel);

        JPanel calcCard = UIKit.card(new BorderLayout());
        calcCard.add(calcRow, BorderLayout.CENTER);

        JLabel note = new JLabel("Based on BARI / BRRI / BJRI research. Timing varies by region and variety. Confirm with your local Agriculture Office.");
        note.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        note.setForeground(UITheme.TEXT_LIGHT);

        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setOpaque(false);
        bottom.add(calcCard, BorderLayout.CENTER);
        bottom.add(note, BorderLayout.SOUTH);

        add(filterCard, BorderLayout.NORTH);
        add(UIKit.tableCard("Crops to Sow", table, "Search crop..."), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        loadCustom();
        reload();
    }

    private void loadCustom() {
        custom.clear();
        custom.addAll(dao.getAll());
    }

    private void addCrop() {
        JTextField nameF = new JTextField();
        JComboBox<String> seasonF = new JComboBox<>(new String[]{"Rabi", "Kharif-1", "Kharif-2"});
        JComboBox<String> fromF = new JComboBox<>(MONTHS);
        JComboBox<String> toF = new JComboBox<>(MONTHS);
        JTextField harvestF = new JTextField();
        JTextField daysF = new JTextField();

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Crop name"));
        form.add(nameF);
        form.add(new JLabel("Season"));
        form.add(seasonF);
        form.add(new JLabel("Sowing from"));
        form.add(fromF);
        form.add(new JLabel("Sowing to"));
        form.add(toF);
        form.add(new JLabel("Harvest months (e.g. Mar - Apr)"));
        form.add(harvestF);
        form.add(new JLabel("Days to harvest"));
        form.add(daysF);

        int r = JOptionPane.showConfirmDialog(this, form, "Add Crop", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return;

        String name = nameF.getText().trim();
        String harvest = harvestF.getText().trim();
        int days;
        try {
            days = Integer.parseInt(daysF.getText().trim());
        } catch (NumberFormatException ex) {
            UIKit.warn(this, "Days must be a number.");
            return;
        }
        if (name.isEmpty() || harvest.isEmpty() || days <= 0 || name.contains("|") || harvest.contains("|")) {
            UIKit.warn(this, "Fill all fields correctly (days must be above 0, do not use the | character).");
            return;
        }
        if (!dao.add(name, String.valueOf(seasonF.getSelectedItem()), fromF.getSelectedIndex() + 1,
                toF.getSelectedIndex() + 1, harvest, days)) {
            UIKit.warn(this, "Could not save to the database. Check your MongoDB connection.");
            return;
        }
        loadCustom();
        reload();
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIKit.warn(this, "Select a crop from the table first.");
            return;
        }
        String name = String.valueOf(table.getValueAt(row, 0));
        for (int i = 0; i < custom.size(); i++) {
            if (String.valueOf(custom.get(i)[0]).equals(name)) {
                int ok = JOptionPane.showConfirmDialog(this, "Delete \"" + name + "\"?", "Delete Crop",
                        JOptionPane.YES_NO_OPTION);
                if (ok == JOptionPane.YES_OPTION) {
                    if (!dao.delete(name)) {
                        UIKit.warn(this, "Could not delete from the database.");
                        return;
                    }
                    loadCustom();
                    reload();
                }
                return;
            }
        }
        UIKit.info(this, "Built-in crops cannot be deleted. Only crops you added can be deleted.");
    }

    private static boolean inRange(int m, int from, int to) {
        return from <= to ? (m >= from && m <= to) : (m >= from || m <= to);
    }

    private void reload() {
        int sel = monthBox.getSelectedIndex();
        model.setRowCount(0);
        List<Object[]> all = new ArrayList<>(Arrays.asList(CROPS));
        all.addAll(custom);
        for (Object[] c : all) {
            int from = (Integer) c[2];
            int to = (Integer) c[3];
            if (sel == 0 || inRange(sel, from, to)) {
                String sow = from == to ? SHORT[from - 1] : SHORT[from - 1] + " - " + SHORT[to - 1];
                model.addRow(new Object[]{c[0], c[1], sow, c[4], c[5]});
            }
        }
    }

    private void calculate() {
        int row = table.getSelectedRow();
        if (row < 0) {
            resultLabel.setText("Select a crop from the table first.");
            return;
        }
        try {
            LocalDate sow = LocalDate.parse(sowField.getText().trim());
            int days = Integer.parseInt(String.valueOf(table.getValueAt(row, 4)));
            String name = String.valueOf(table.getValueAt(row, 0));
            LocalDate harvest = sow.plusDays(days);
            resultLabel.setText(name + ": harvest about " + harvest.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                    + " (" + days + " days)");
        } catch (DateTimeParseException ex) {
            resultLabel.setText("Date format must be yyyy-MM-dd (e.g. 2026-12-01).");
        }
    }
}