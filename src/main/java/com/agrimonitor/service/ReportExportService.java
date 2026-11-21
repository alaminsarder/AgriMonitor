
package com.agrimonitor.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSimpleField;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReportExportService {

    private static final String APP_NAME = "AGRIMONITOR";
    private static final String SUBTITLE =
            "Smart Agricultural Monitoring System";

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm a");

    private static String getCurrentDate() {
        return LocalDateTime.now().format(DATE_FORMAT);
    }

    // ================= PDF EXPORT =================

    public static void exportPDF(
            File file,
            String reportTitle,
            String reportContent
    ) throws Exception {

        Document document = new Document(
                PageSize.A4, 45, 45, 55, 55
        );

        PdfWriter writer = PdfWriter.getInstance(
                document,
                new FileOutputStream(file)
        );

        writer.setPageEvent(new PDFPageFooter());

        document.open();

        // PDF Metadata
        document.addTitle(reportTitle);
        document.addAuthor(APP_NAME);
        document.addSubject("Agricultural Management Report");

        // Header
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);

        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(new Color(35, 91, 58));
        headerCell.setBorder(Rectangle.NO_BORDER);
        headerCell.setPadding(18);

        Font brandFont = new Font(
                Font.HELVETICA, 19, Font.BOLD, Color.WHITE
        );

        Font subtitleFont = new Font(
                Font.HELVETICA, 9, Font.NORMAL, Color.WHITE
        );

        Paragraph brand = new Paragraph(APP_NAME, brandFont);
        brand.setAlignment(Element.ALIGN_CENTER);

        Paragraph subtitle = new Paragraph(SUBTITLE, subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);

        headerCell.addElement(brand);
        headerCell.addElement(subtitle);

        header.addCell(headerCell);
        document.add(header);

        document.add(new Paragraph(" "));

        // Report Title
        Font titleFont = new Font(
                Font.HELVETICA, 17, Font.BOLD,
                new Color(35, 91, 58)
        );

        Paragraph title = new Paragraph(reportTitle, titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(8);

        document.add(title);

        // Generated Date
        Font dateFont = new Font(
                Font.HELVETICA, 9, Font.ITALIC,
                Color.GRAY
        );

        Paragraph date = new Paragraph(
                "Generated on: " + getCurrentDate(),
                dateFont
        );

        date.setAlignment(Element.ALIGN_CENTER);
        date.setSpacingAfter(22);

        document.add(date);

        // Divider
        PdfPTable divider = new PdfPTable(1);
        divider.setWidthPercentage(100);

        PdfPCell dividerCell = new PdfPCell();
        dividerCell.setFixedHeight(2);
        dividerCell.setBackgroundColor(
                new Color(35, 91, 58)
        );
        dividerCell.setBorder(Rectangle.NO_BORDER);

        divider.addCell(dividerCell);
        document.add(divider);

        document.add(new Paragraph(" "));

        // Report Content
        Font normalFont = new Font(
                Font.HELVETICA, 10, Font.NORMAL,
                new Color(45, 45, 45)
        );

        Font headingFont = new Font(
                Font.HELVETICA, 11, Font.BOLD,
                new Color(35, 91, 58)
        );

        String[] lines = reportContent.split("\\R");

        for (String line : lines) {

            String trimmed = line.trim();

            if (trimmed.isEmpty()) {
                document.add(new Paragraph(" "));
                continue;
            }

            Paragraph paragraph;

            if (trimmed.matches("^[A-Z][A-Z0-9 /&()_-]{2,}$")) {

                paragraph = new Paragraph(trimmed, headingFont);
                paragraph.setSpacingBefore(8);
                paragraph.setSpacingAfter(5);

            } else {

                paragraph = new Paragraph(trimmed, normalFont);
                paragraph.setLeading(15);
                paragraph.setSpacingAfter(3);
            }

            document.add(paragraph);
        }

        document.close();
    }

    // PDF Page Footer
    private static class PDFPageFooter
            extends PdfPageEventHelper {

        @Override
        public void onEndPage(PdfWriter writer, Document document) {

            Font footerFont = new Font(
                    Font.HELVETICA, 8, Font.NORMAL,
                    Color.GRAY
            );

            Phrase footer = new Phrase(
                    "AgriMonitor  |  Confidential  |  Page "
                            + writer.getPageNumber(),
                    footerFont
            );

            ColumnText.showTextAligned(
                    writer.getDirectContent(),
                    Element.ALIGN_CENTER,
                    footer,
                    (document.left() + document.right()) / 2,
                    document.bottom() - 25,
                    0
            );
        }
    }

    // ================= DOCX EXPORT =================

    public static void exportDOCX(
            File file,
            String reportTitle,
            String reportContent
    ) throws IOException {

        try (XWPFDocument document = new XWPFDocument()) {

            // Header
            XWPFHeader header = document.createHeader(
                    HeaderFooterType.DEFAULT
            );

            XWPFParagraph headerParagraph =
                    header.createParagraph();

            headerParagraph.setAlignment(
                    ParagraphAlignment.CENTER
            );

            XWPFRun headerRun = headerParagraph.createRun();

            headerRun.setText(APP_NAME);
            headerRun.setBold(true);
            headerRun.setFontSize(18);
            headerRun.setColor("235B3A");
            headerRun.setFontFamily("Arial");

            // Main Title
            XWPFParagraph titleParagraph =
                    document.createParagraph();

            titleParagraph.setAlignment(
                    ParagraphAlignment.CENTER
            );

            titleParagraph.setSpacingBefore(350);
            titleParagraph.setSpacingAfter(120);

            XWPFRun titleRun = titleParagraph.createRun();

            titleRun.setText(reportTitle);
            titleRun.setBold(true);
            titleRun.setFontSize(20);
            titleRun.setColor("235B3A");
            titleRun.setFontFamily("Arial");

            // Subtitle
            XWPFParagraph subtitleParagraph =
                    document.createParagraph();

            subtitleParagraph.setAlignment(
                    ParagraphAlignment.CENTER
            );

            XWPFRun subtitleRun =
                    subtitleParagraph.createRun();

            subtitleRun.setText(SUBTITLE);
            subtitleRun.setItalic(true);
            subtitleRun.setFontSize(10);
            subtitleRun.setColor("777777");

            // Generated Date
            XWPFParagraph dateParagraph =
                    document.createParagraph();

            dateParagraph.setAlignment(
                    ParagraphAlignment.CENTER
            );

            dateParagraph.setSpacingAfter(300);

            XWPFRun dateRun = dateParagraph.createRun();

            dateRun.setText(
                    "Generated on: " + getCurrentDate()
            );

            dateRun.setFontSize(9);
            dateRun.setColor("888888");

            // Content
            String[] lines = reportContent.split("\\R");

            for (String line : lines) {

                String trimmed = line.trim();

                XWPFParagraph paragraph =
                        document.createParagraph();

                paragraph.setSpacingAfter(100);

                if (trimmed.isEmpty()) {
                    continue;
                }

                XWPFRun run = paragraph.createRun();

                run.setFontFamily("Arial");

                if (trimmed.matches(
                        "^[A-Z][A-Z0-9 /&()_-]{2,}$"
                )) {

                    run.setText(trimmed);
                    run.setBold(true);
                    run.setFontSize(12);
                    run.setColor("235B3A");

                    paragraph.setSpacingBefore(120);

                } else {

                    run.setText(trimmed);
                    run.setFontSize(10);
                    run.setColor("333333");
                }
            }

            // Footer
            XWPFFooter footer = document.createFooter(
                    HeaderFooterType.DEFAULT
            );

            XWPFParagraph footerParagraph =
                    footer.createParagraph();

            footerParagraph.setAlignment(
                    ParagraphAlignment.CENTER
            );

            XWPFRun footerRun =
                    footerParagraph.createRun();

            footerRun.setText("AgriMonitor | Page ");
            footerRun.setFontSize(9);
            footerRun.setColor("888888");

            // Dynamic page number field
            CTSimpleField pageField =
                    footerParagraph.getCTP().addNewFldSimple();

            pageField.setInstr("PAGE");

            try (FileOutputStream output =
                         new FileOutputStream(file)) {

                document.write(output);
            }
        }
    }

    // ================= TXT EXPORT =================

    public static void exportTXT(
            File file,
            String reportTitle,
            String reportContent
    ) throws IOException {

        StringBuilder content = new StringBuilder();

        content.append("========================================\n");
        content.append("              AGRIMONITOR\n");
        content.append("     Smart Agricultural Monitoring\n");
        content.append("========================================\n\n");

        content.append(reportTitle).append("\n");
        content.append("Generated on: ")
                .append(getCurrentDate())
                .append("\n\n");

        content.append("----------------------------------------\n\n");

        content.append(reportContent);

        content.append("\n\n----------------------------------------\n");
        content.append("Generated by AgriMonitor System\n");

        Files.writeString(
                file.toPath(),
                content.toString(),
                StandardCharsets.UTF_8
        );
    }
}
