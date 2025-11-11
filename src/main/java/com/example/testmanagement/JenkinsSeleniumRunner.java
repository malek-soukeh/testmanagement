package com.example.testmanagement;
import com.example.testmanagement.Entities.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.nio.file.*;
import java.util.List;
import java.util.Map;
public class JenkinsSeleniumRunner {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("No scenario JSON provided");
            System.exit(1);
        }

        String scenarioJson = args[0];
        ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> scenario = mapper.readValue(scenarioJson, Map.class);

        String title = (String) scenario.get("title");
        List<Map<String,Object>> steps = (List<Map<String,Object>>) scenario.get("steps");

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
        WebDriver driver = new ChromeDriver(options);

            Path reportFolder = Paths.get("target/selenium-runs/" + title.replaceAll("\\s+","_"));
            Files.createDirectories(reportFolder);

            boolean overallPassed = true;
        for (int i=0; i<steps.size(); i++) {
            Map<String,Object> step = steps.get(i);
            String actionType = ((String) step.get("actionType")).toLowerCase();
            String target = (String) step.get("actionTarget");
            String value = (String) step.get("actionValue");

                try {
                    // Execute action (simplifié)
                    switch (actionType.toLowerCase()) {
                        case "goto":
                            driver.get(target);
                            Thread.sleep(500);
                            break;
                        case "click":
                            driver.findElement(By.cssSelector(target)).click();
                            Thread.sleep(300);
                            break;
                            case "inputtext":
                            driver.findElement(By.cssSelector(target)).sendKeys(value);
                            break;
                        // ajouter les autres actions comme verifyText, screenshot
                    }
                    System.out.println("Step " + (i+1) + " passed");
                } catch (Exception e) {
                    overallPassed = false;
                    System.out.println("Step " + (i+1) + " failed: " + e.getMessage());
                }
            }

            driver.quit();
            System.out.println("Test " + (overallPassed ? "PASSED" : "FAILED"));
        }
    }


