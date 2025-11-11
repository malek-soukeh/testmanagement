package com.example.testmanagement.seleniumrunner;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class SeleniumRunner {
    public static class StepResult {
        private String stepName;
        private boolean success;
        private String screenshotBase64;

        public StepResult(String stepName, boolean success, String screenshotBase64) {
            this.stepName = stepName;
            this.success = success;
            this.screenshotBase64 = screenshotBase64;
        }

        public String getStepName() { return stepName; }
        public boolean isSuccess() { return success; }
        public String getScreenshotBase64() { return screenshotBase64; }
    }

        public static List<StepResult> runScenario(SeleniumScenario scenario) throws IOException {
            List<StepResult> stepResults = new ArrayList<>();

                // Initialisation WebDriver
                WebDriverManager.chromedriver().setup();
                WebDriver driver = new ChromeDriver();

                // Créer dossier pour les screenshots si nécessaire
                Files.createDirectories(Paths.get("screenshots"));

                try {
                    // Naviguer vers l'URL du test
                    driver.get(scenario.getUrl());

                    // Exécuter chaque étape
                    for (SeleniumStep step : scenario.getSteps()) {
                        boolean success = StepExecutor.executeStep(driver, step);

                        // Capture d'écran
                        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                        byte[] imageBytes = Files.readAllBytes(screenshot.toPath());
                        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

                        // Sauvegarder sur disque (optionnel)
                        String fileName = "screenshots/" + step.getStepName() + (success ? "_passed" : "_failed") + ".png";
                        Files.copy(screenshot.toPath(), Paths.get(fileName));

                        // Ajouter le résultat avec image Base64
                        stepResults.add(new StepResult(step.getStepName(), success, base64Image));

                        if (!success) {
                            System.out.println("Step failed: " + step.getStepName());
                        }
                    }

                } finally {
                    driver.quit();
                }

                return stepResults;
            }

    }

