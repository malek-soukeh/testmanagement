package com.example.testmanagement.Services;

import com.example.testmanagement.DTOs.SeleniumStepSummary;
import com.example.testmanagement.DTOs.SeleniumSummaryDTO;
import com.example.testmanagement.Entities.TestResult;
import com.example.testmanagement.Repository.TestResultRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TestReportService {

    private final TestResultRepository testResultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public byte[] buildSeleniumPdf(Long testResultId) {
        TestResult result = testResultRepository.findById(testResultId)
                .orElseThrow(() -> new RuntimeException("TestResult not found"));
        SeleniumSummaryDTO summary = parseSummary(result.getExecutionReport(), result.getTestName());
        try {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

            document.add(new Paragraph("Automated Test Execution Report", titleFont));
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("Test Case: " + result.getTestName(), subtitleFont));
            Font statusFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, statusColor(result.getStatus()));
            document.add(new Paragraph("Status: " + result.getStatus(), statusFont));
            document.add(Chunk.NEWLINE);

            List<SeleniumStepSummary> steps = summary.getSteps();
            if (steps != null && !steps.isEmpty()) {
                for (SeleniumStepSummary step : steps) {
                    document.add(new Paragraph(String.format("Step %02d - %s",
                            step.getStepIndex(), step.getStepName()), subtitleFont));
                    Font resultFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11,
                            Boolean.TRUE.equals(step.getSuccess()) ? BaseColor.GREEN : BaseColor.RED);
                    document.add(new Paragraph(
                            "Result: " + (Boolean.TRUE.equals(step.getSuccess()) ? "PASSED" : "FAILED"), resultFont));
                    if (step.getExpectedResult() != null) {
                        document.add(new Paragraph("Expected: " + step.getExpectedResult(), normalFont));
                    }
                    if (step.getActualResult() != null) {
                        document.add(new Paragraph("Actual: " + step.getActualResult(), normalFont));
                    }
                    if (step.getScreenshotBase64() != null && !step.getScreenshotBase64().isBlank()) {
                        try {
                            byte[] imgBytes = Base64.getDecoder().decode(step.getScreenshotBase64());
                            Image img = Image.getInstance(imgBytes);
                            img.scaleToFit(450, 250);
                            img.setAlignment(Image.ALIGN_CENTER);
                            document.add(img);
                        } catch (Exception ignored) {
                        }
                    }
                    document.add(Chunk.NEWLINE);
                }
            } else {
                document.add(new Paragraph("No detailed steps available.", normalFont));
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    private SeleniumSummaryDTO parseSummary(String json, String fallbackTitle) {
        SeleniumSummaryDTO summary = new SeleniumSummaryDTO();
        summary.setTitle(fallbackTitle);
        try {
            if (json == null || json.isBlank()) {
                return summary;
            }
            JsonNode node = objectMapper.readTree(json);
            if (node.isArray() && node.size() > 0) {
                node = node.get(0);
            }
            summary = objectMapper.treeToValue(node, SeleniumSummaryDTO.class);
            if (summary.getTitle() == null) summary.setTitle(fallbackTitle);
        } catch (Exception ignored) {
            summary.setTitle(fallbackTitle);
        }
        return summary;
    }

    private BaseColor statusColor(TestResult.ResultStatus status) {
        if (status == null) return BaseColor.DARK_GRAY;
        return switch (status) {
            case PASSED -> BaseColor.GREEN;
            case FAILED -> BaseColor.RED;
            case RUNNING -> BaseColor.ORANGE;
            default -> BaseColor.DARK_GRAY;
        };
    }

}

