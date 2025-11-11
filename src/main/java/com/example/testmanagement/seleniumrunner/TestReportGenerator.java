package com.example.testmanagement.seleniumrunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.nio.file.Files;

public class TestReportGenerator {

    public static void generateHtmlReport(String scenarioName, List<SeleniumRunner.StepResult> stepResults, String reportPath) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'><title>").append(scenarioName).append("</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; }");
        html.append(".step { margin-bottom: 20px; }");
        html.append(".passed { color: green; }");
        html.append(".failed { color: red; }");
        html.append("</style>");
        html.append("</head><body>");
        html.append("<h1>Rapport de test : ").append(scenarioName).append("</h1>");

        for (SeleniumRunner.StepResult step : stepResults) {
            html.append("<div class='step'>");
            html.append("<h2>").append(step.getStepName()).append("</h2>");
            html.append("<p>Status : <span class='").append(step.isSuccess() ? "passed" : "failed").append("'>")
                    .append(step.isSuccess() ? "PASSED" : "FAILED").append("</span></p>");

            try {
                // Lire le fichier screenshot et encoder en Base64
                String base64 = Base64.getEncoder().encodeToString(
                        Files.readAllBytes(new File("screenshots/" + step.getStepName() + (step.isSuccess() ? "_passed" : "_failed") + ".png").toPath())
                );
                html.append("<img src='data:image/png;base64,").append(base64).append("' width='600'/>");
            } catch (IOException e) {
                html.append("<p>Erreur lors de la lecture de la capture d'écran</p>");
            }

            html.append("</div>");
        }

        html.append("</body></html>");

        try (FileWriter writer = new FileWriter(reportPath)) {
            writer.write(html.toString());
            System.out.println("Rapport généré : " + reportPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}