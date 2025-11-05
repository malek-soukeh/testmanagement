package com.example.testmanagement.Services;

import org.springframework.stereotype.Service;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
@Service
public class GenerationPdf {
    public byte[] generatePdfFromXml(String xmlContent) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 54, 36); // marges
        PdfWriter.getInstance(document, out);
        document.open();

        // 🔹 Titre du rapport
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        document.add(new Paragraph("📊 Performance Test Report", titleFont));
        document.add(new Paragraph("Generated from Jenkins - standardResults.xml\n\n"));

        // 🔹 Parser le XML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        org.w3c.dom.Document xmlDoc =
                builder.parse(new ByteArrayInputStream(xmlContent.getBytes()));

        NodeList apis = xmlDoc.getElementsByTagName("api");

        // 🔹 Tableau PDF
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.addCell("API");
        table.addCell("Samples");
        table.addCell("Avg (ms)");
        table.addCell("Min");
        table.addCell("Max");
        table.addCell("90th%");
        table.addCell("95th%");
        table.addCell("Errors (%)");

        for (int i = 0; i < apis.getLength(); i++) {
            Element api = (Element) apis.item(i);
            table.addCell(api.getElementsByTagName("uri").item(0).getTextContent());
            table.addCell(api.getElementsByTagName("samples").item(0).getTextContent());
            table.addCell(api.getElementsByTagName("average").item(0).getTextContent());
            table.addCell(api.getElementsByTagName("min").item(0).getTextContent());
            table.addCell(api.getElementsByTagName("max").item(0).getTextContent());
            table.addCell(api.getElementsByTagName("ninetieth").item(0).getTextContent());
            table.addCell(api.getElementsByTagName("ninetyFifth").item(0).getTextContent());
            table.addCell(api.getElementsByTagName("errors").item(0).getTextContent());
        }

        document.add(table);
        document.close();

        return out.toByteArray();
    }
}
