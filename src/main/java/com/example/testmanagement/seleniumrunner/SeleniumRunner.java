package com.example.testmanagement.seleniumrunner;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SeleniumRunner {
    public static class StepResult {
        private String stepName;
        private boolean success;
        private String screenshotBase64;
        private String message;


        public StepResult(String stepName, boolean success, String screenshotBase64, String message) {
            this.stepName = stepName;
            this.success = success;
            this.screenshotBase64 = screenshotBase64;
            this.message = message;

        }

        public String getStepName() { return stepName; }
        public boolean isSuccess() { return success; }
        public String getScreenshotBase64() { return screenshotBase64; }
        public String getMessage() { return message; }
    }

        public static List<StepResult> runScenario(SeleniumScenario scenario) throws IOException {
            List<StepResult> stepResults = new ArrayList<>();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path screenshotDir = Paths.get("screenshots/" + scenario.getTestCaseId() + "_" + timestamp);
            Files.createDirectories(screenshotDir);

                // Initialisation WebDriver
                WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");          // Important pour Jenkins
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--window-size=1920,1080");
            WebDriver driver = new ChromeDriver(options);

                try {
                    // Naviguer vers l'URL du test
                    driver.get(scenario.getUrl());

                    // Exécuter chaque étape
                    for (SeleniumStep step : scenario.getSteps()) {
                        boolean success ;
                        String message ;
                        try {
                            success = StepExecutor.executeStep(driver, step);
                            message = success ? "Step executed successfully" : "Step execution failed";
                        } catch (Exception e) {
                            success = false;
                            message = "Exception: " + e.getMessage();
                        }

                        // Capture d'écran
                        String screenshotFileName = step.getStepName()
                                .replaceAll("[^a-zA-Z0-9_-]", "_")
                                + (success ? "_success" : "_failed") + ".png";
                        File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                        Path finalScreenshotPath = screenshotDir.resolve(screenshotFileName);
                        Files.copy(screenshotFile.toPath(), finalScreenshotPath);
                        String base64Image = Base64.getEncoder().encodeToString(Files.readAllBytes(finalScreenshotPath));

                        // Ajouter le résultat avec image Base64
                        stepResults.add(new StepResult(step.getStepName(), success, base64Image, message));

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

