
package com.agrimonitor.gui;

import com.agrimonitor.service.ReportService;
import com.agrimonitor.service.ReportExportService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

public class ReportPanel extends JPanel {

    private static final String PLACEHOLDER =
            "Select a report above to generate it here.";

    private final ReportService reports = new ReportService();

    private final JTextArea area =
            new JTextArea(PLACEHOLDER);

    private final JLabel status =
            new JLabel("No report generated yet");

    private String currentReportTitle = "";
    private String currentReportText = "";

    public ReportPanel() {

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BACKGROUND);
        setBorder(new EmptyBorder(22, 26, 22, 26));

        // ================= REPORT BUTTONS =================

        JPanel grid = new JPanel(
                new GridLayout(2, 4, 12, 12)
        );

        grid.setOpaque(false);

        addReport(
                grid, "Field Report",
                UITheme.PRIMARY,
                reports::getFieldReportText
        );

        addReport(
                grid, "Crop Report",
                UITheme.SUCCESS,
                reports::getCropReportText
        );

        addReport(
                grid, "Irrigation Report",
                UITheme.INFO,
                reports::getIrrigationReportText
        );

        addReport(
                grid, "Pesticide Report",
                UITheme.DANGER,
                reports::getPesticideReportText
        );

        addReport(
                grid, "Cost Report",
                UITheme.PRIMARY,
                reports::getCostReportText
        );

        addReport(
                grid, "Sales Report",
                UITheme.SUCCESS,
                reports::getSalesReportText
        );

        addReport(
                grid, "Profit / Loss Report",
                UITheme.INFO,
                reports::getProfitLossReportText
        );

        addReport(
                grid, "Overall Farm Report",
                UITheme.PRIMARY_DARK,
                reports::getOverallFarmReportText
        );

        // ================= REPORT OUTPUT =================

        area.setEditable(false);

        area.setFont(
                new Font("Consolas", Font.PLAIN, 13)
        );

        area.setForeground(UITheme.TEXT_DARK);

        area.setBackground(
                new Color(252, 250, 245)
        );

        area.setMargin(
                new Insets(12, 14, 12, 14)
        );

        area.setLineWrap(false);

        // Status
        status.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        status.setForeground(UITheme.TEXT_LIGHT);

        // ================= ACTION BUTTONS =================

        JButton pdfBtn = UITheme.createStyledButton(
                "Export PDF", UITheme.DANGER
        );

        JButton docxBtn = UITheme.createStyledButton(
                "Export DOCX", UITheme.INFO
        );

        JButton txtBtn = UITheme.createStyledButton(
                "Export TXT", UITheme.SUCCESS
        );

        JButton copyBtn = UITheme.createStyledButton(
                "Copy", UITheme.TEXT_LIGHT
        );

        JButton clearBtn = UITheme.createStyledButton(
                "Clear", UITheme.TEXT_LIGHT
        );

        // PDF Export
        pdfBtn.addActionListener(e ->
                exportReport("pdf")
        );

        // DOCX Export
        docxBtn.addActionListener(e ->
                exportReport("docx")
        );

        // TXT Export
        txtBtn.addActionListener(e ->
                exportReport("txt")
        );

        // Copy Report
        copyBtn.addActionListener(e -> {

            if (currentReportText.isBlank()) {
                UIKit.warn(this, "Nothing to copy yet.");
                return;
            }

            Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .setContents(
                            new StringSelection(area.getText()),
                            null
                    );

            status.setText("Copied to clipboard");
        });

        // Clear Report
        clearBtn.addActionListener(e -> {

            area.setText(PLACEHOLDER);

            currentReportTitle = "";
            currentReportText = "";

            status.setText("No report generated yet");
        });

        // ================= HEADER ACTIONS =================

        JPanel right = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0)
        );

        right.setOpaque(false);

        right.add(status);
        right.add(pdfBtn);
        right.add(docxBtn);
        right.add(txtBtn);
        right.add(copyBtn);
        right.add(clearBtn);

        JPanel head = new JPanel(
                new BorderLayout()
        );

        head.setOpaque(false);

        head.add(
                UIKit.heading("Report Output"),
                BorderLayout.WEST
        );

        head.add(right, BorderLayout.EAST);

        // Output Card
        JPanel out = UIKit.card(
                new BorderLayout(0, 14)
        );

        out.add(head, BorderLayout.NORTH);

        out.add(
                UIKit.styledScroll(area),
                BorderLayout.CENTER
        );

        // Main Layout
        add(
                UIKit.formCard(
                        "Generate Reports",
                        grid,
                        null
                ),
                BorderLayout.NORTH
        );

        add(out, BorderLayout.CENTER);
    }

    // ================= GENERATE REPORT =================

    private void addReport(
            JPanel grid,
            String label,
            Color color,
            Supplier<String> source
    ) {

        JButton button = UITheme.createStyledButton(
                label, color
        );

        button.addActionListener(e -> {

            try {

                String result = source.get();

                if (result == null || result.isBlank()) {
                    UIKit.warn(
                            this,
                            "No data available for this report."
                    );
                    return;
                }

                currentReportTitle = label;
                currentReportText = result;

                area.setText(result);
                area.setCaretPosition(0);

                status.setText(
                        label + " - generated "
                        + LocalTime.now().format(
                                DateTimeFormatter.ofPattern(
                                        "HH:mm:ss"
                                )
                        )
                );

            } catch (Exception ex) {

                UIKit.warn(
                        this,
                        "Could not generate report: "
                        + ex.getMessage()
                );
            }
        });

        grid.add(button);
    }

    // ================= EXPORT REPORT =================

    private void exportReport(String format) {

        if (currentReportText.isBlank()) {

            UIKit.warn(
                    this,
                    "Please generate a report before exporting."
            );

            return;
        }

        String extension = format.toLowerCase();

        String fileName = currentReportTitle
                .replaceAll("[^a-zA-Z0-9_-]", "_")
                + "_"
                + LocalTime.now().format(
                        DateTimeFormatter.ofPattern("HHmmss")
                );

        JFileChooser chooser = new JFileChooser();

        chooser.setDialogTitle(
                "Export " + currentReportTitle
        );

        chooser.setSelectedFile(
                new File(fileName + "." + extension)
        );

        chooser.setFileFilter(
                new FileNameExtensionFilter(
                        format.toUpperCase() + " Files (*."
                                + extension + ")",
                        extension
                )
        );

        int result = chooser.showSaveDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

        // Add extension automatically
        if (!file.getName().toLowerCase()
                .endsWith("." + extension)) {

            file = new File(
                    file.getPath() + "." + extension
            );
        }

        // Confirm overwrite
        if (file.exists()) {

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "This file already exists.\n"
                            + "Do you want to replace it?",
                    "Confirm Overwrite",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        // Export
        try {

            switch (extension) {

                case "pdf":

                    ReportExportService.exportPDF(
                            file,
                            currentReportTitle,
                            currentReportText
                    );

                    break;

                case "docx":

                    ReportExportService.exportDOCX(
                            file,
                            currentReportTitle,
                            currentReportText
                    );

                    break;

                case "txt":

                    ReportExportService.exportTXT(
                            file,
                            currentReportTitle,
                            currentReportText
                    );

                    break;

                default:

                    throw new IllegalArgumentException(
                            "Unsupported export format."
                    );
            }

            status.setText(
                    "Exported: " + file.getName()
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Report exported successfully!\n\n"
                            + file.getAbsolutePath(),
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception ex) {

            ex.printStackTrace();

            UIKit.warn(
                    this,
                    "Export failed: " + ex.getMessage()
            );
        }
    }
}

