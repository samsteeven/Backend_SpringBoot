package com.app.easypharma_backend.infrastructure.pdf;

import com.app.easypharma_backend.domain.order.entity.Order;
import com.app.easypharma_backend.domain.order.entity.OrderItem;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    public byte[] generateInvoice(Order order) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Header
            document.add(new Paragraph("EASYPHARMA")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.BLUE));
            
            document.add(new Paragraph("Facture / Reçu")
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("\n"));

            // Info Commande
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
            infoTable.setWidth(UnitValue.createPercentValue(100));

            infoTable.addCell(new Cell().add(new Paragraph("Pharmacie: " + order.getPharmacy().getName())).setBorder(Border.NO_BORDER));
            infoTable.addCell(new Cell().add(new Paragraph("Client: " + order.getPatient().getFirstName() + " " + order.getPatient().getLastName())).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));
            
            infoTable.addCell(new Cell().add(new Paragraph("N° Commande: " + order.getOrderNumber())).setBorder(Border.NO_BORDER));
            infoTable.addCell(new Cell().add(new Paragraph("Date: " + order.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE))).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));

            document.add(infoTable);
            document.add(new Paragraph("\n"));

            // Details Articles
            Table table = new Table(UnitValue.createPercentArray(new float[]{4, 1, 2, 2}));
            table.setWidth(UnitValue.createPercentValue(100));

            table.addHeaderCell(new Cell().add(new Paragraph("Médicament").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Qté").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Prix Unit.").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Total").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));

            for (OrderItem item : order.getItems()) {
                table.addCell(new Paragraph(item.getMedication().getName()));
                table.addCell(new Paragraph(String.valueOf(item.getQuantity())));
                table.addCell(new Paragraph(item.getUnitPrice().toString() + " €"));
                table.addCell(new Paragraph(item.getUnitPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())).toString() + " €"));
            }

            document.add(table);

            // Total
            document.add(new Paragraph("\n"));
            Paragraph total = new Paragraph("Total: " + order.getTotalAmount() + " €")
                    .setBold()
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.RIGHT);
            document.add(total);

            // Footer
            document.add(new Paragraph("\n\nMerci de votre confiance.")
                    .setItalic()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY));

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }

        return baos.toByteArray();
    }
}
