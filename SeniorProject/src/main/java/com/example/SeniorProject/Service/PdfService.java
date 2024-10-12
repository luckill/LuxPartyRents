package com.example.SeniorProject.Service;

import com.example.SeniorProject.Model.Customer;
import com.example.SeniorProject.Model.Order;
import com.example.SeniorProject.Model.OrderProduct;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@Service
public class PdfService {

    public ByteArrayResource generateInvoicePDF(Map<String, Object> model) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Set font and leading
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.setLeading(18f);

            // Company Name and Invoice Title
            contentStream.beginText();
            contentStream.newLineAtOffset(220, 750); // Centered
            contentStream.showText("Potapov Weddings"); // Company Name
            contentStream.endText();

            // Draw a line below the company name
            contentStream.setLineWidth(0.5f);
            contentStream.moveTo(50, 740);
            contentStream.lineTo(550, 740);
            contentStream.stroke();

            // Invoice Information (top right)
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
            contentStream.newLineAtOffset(400, 720);
            contentStream.showText("INVOICE");
            contentStream.newLine();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.showText("Invoice #: " + ((Order) model.get("order")).getId());
            contentStream.newLine();
            contentStream.showText("Date: " + ((Order) model.get("order")).getCreationDate().toString());
            contentStream.endText();

            // Bill To Section
            Customer customer = (Customer) model.get("customer");
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(50, 670);
            contentStream.showText("BILL TO");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(50, 650);
            contentStream.showText(customer.getFirstName() + " " + customer.getLastName());
            contentStream.newLine();
            contentStream.showText("Phone: " + customer.getPhone());
            contentStream.newLine();
            contentStream.showText("Email: " + customer.getEmail());
            contentStream.endText();

            // Adding more space after email (by adjusting yPosition)
            float yPosition = 600; // Adjusted to give more space

            // Add more spacing before the table header to avoid overlap
            contentStream.setLineWidth(1f);
            contentStream.moveTo(50, yPosition);
            contentStream.lineTo(550, yPosition);
            contentStream.stroke();

            // Table Header - Description, Quantity, Price
            yPosition -= 10; // Give space above the table
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("DESCRIPTION");
            contentStream.newLineAtOffset(350, 0);
            contentStream.showText("QUANTITY");
            contentStream.newLineAtOffset(100, 0);
            contentStream.showText("PRICE");
            contentStream.endText();

            // Draw a line below the header
            yPosition -= 20;
            contentStream.moveTo(50, yPosition);
            contentStream.lineTo(550, yPosition);
            contentStream.stroke();

            double subtotal = 0;
            yPosition -= 20; // Start below the header

            // Table Rows - Product List
            for (OrderProduct orderProduct : ((Order) model.get("order")).getOrderProducts()) {
                String productName = orderProduct.getProduct().getName();
                int quantity = orderProduct.getQuantity();
                double productPrice = orderProduct.getProduct().getPrice() * orderProduct.getQuantity();

                // Description (left column)
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, yPosition);
                contentStream.showText(productName);
                contentStream.endText();

                // Quantity (middle column)
                contentStream.beginText();
                contentStream.newLineAtOffset(400, yPosition);
                contentStream.showText(String.valueOf(quantity));
                contentStream.endText();

                // Price (right column)
                contentStream.beginText();
                contentStream.newLineAtOffset(500, yPosition);
                contentStream.showText(String.format("$%.2f", productPrice));
                contentStream.endText();

                subtotal += productPrice;
                yPosition -= 20; // Move to the next row

                // Draw a line after each product row
                contentStream.moveTo(50, yPosition + 10);
                contentStream.lineTo(550, yPosition + 10);
                contentStream.stroke();
            }

            // Subtotal, Tax, and Total - at the bottom
            double tax = subtotal * 0.07; // Assuming 7% tax
            double total = subtotal + tax;

            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition - 20);
            contentStream.showText("Subtotal");
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(500, yPosition - 20);
            contentStream.showText(String.format("$%.2f", subtotal));
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition - 40);
            contentStream.showText("Tax (7%)");
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(500, yPosition - 40);
            contentStream.showText(String.format("$%.2f", tax));
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(50, yPosition - 60);
            contentStream.showText("TOTAL");
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(500, yPosition - 60);
            contentStream.showText(String.format("$%.2f", total));
            contentStream.endText();

            // Thank You Note
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(50, yPosition - 100);
            contentStream.showText("Thank you for your business with us!");
            contentStream.endText();

            // Close the content stream
            contentStream.close();

            // Write the PDF content to a byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);

            return new ByteArrayResource(outputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }
}
