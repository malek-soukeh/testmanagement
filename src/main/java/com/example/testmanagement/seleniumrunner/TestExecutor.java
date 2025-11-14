package com.example.testmanagement.seleniumrunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;

public class TestExecutor {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("No scenario JSON provided");
            System.exit(1);
        }
        String input = args[0];
        String json;
        Path p = Paths.get(input);
        if (Files.exists(p)) json = Files.readString(p);
        else json = input;

        ObjectMapper mapper = new ObjectMapper();
        
        // Gérer à la fois un tableau de scénarios et un scénario unique
        Object parsed = mapper.readValue(json, Object.class);
        Map<String, Object> scenario;
        
        if (parsed instanceof List) {
            // Si c'est un tableau, prendre le premier scénario
            List<?> scenarios = (List<?>) parsed;
            if (scenarios.isEmpty()) {
                System.out.println("Empty scenario array provided");
                System.exit(1);
            }
            scenario = (Map<String, Object>) scenarios.get(0);
        } else if (parsed instanceof Map) {
            // Si c'est un objet unique
            scenario = (Map<String, Object>) parsed;
        } else {
            System.out.println("Invalid JSON format: expected array or object");
            System.exit(1);
            return;
        }

        String title = (String) scenario.get("title");
        String url = (String) scenario.get("url");
        List<Map<String, Object>> steps = (List<Map<String, Object>>) scenario.getOrDefault("steps", List.of());
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu");
        WebDriver driver = new ChromeDriver(options);
        Path reportFolder = Paths.get("target/selenium-runs", title.replaceAll("\\s+","_") + "_" + System.currentTimeMillis());
        Files.createDirectories(reportFolder);
        boolean overallPassed = true;
        List<Map<String,Object>> stepSummaries = new ArrayList<>();
        try {
            if (url != null && !url.isBlank()) {
                System.out.println("Loading URL: " + url);
                driver.get(url);
                // Attendre que la page soit complètement chargée
                Thread.sleep(2000);
                // Attendre que le document soit prêt
                WebDriverWait pageWait = new WebDriverWait(driver, Duration.ofSeconds(10));
                pageWait.until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
                System.out.println("Page loaded: " + driver.getTitle());
            } else {
                System.out.println("WARNING: No URL provided, skipping page load");
            }

            int i=0;
            for (Map<String,Object> s : steps) {
                i++;
                SeleniumStep step = mapper.convertValue(s, SeleniumStep.class);
                boolean ok = StepExecutor.executeStep(driver, step);
                // screenshot for each step
                File ss = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                Path target = reportFolder.resolve(String.format("step-%02d-%s-%s.png", i, sanitize(step.getStepName()), ok ? "passed":"failed"));
                Files.copy(ss.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
                byte[] bytes = Files.readAllBytes(target);
                String base64 = Base64.getEncoder().encodeToString(bytes);
                Map<String,Object> sum = new HashMap<>();
                sum.put("stepIndex", i);
                sum.put("stepName", step.getStepName());
                sum.put("success", ok);
                sum.put("screenshot", target.toString());
                sum.put("screenshotBase64", base64);
                stepSummaries.add(sum);

                if (!ok) overallPassed = false;
            }
            Map<String,Object> summary = new HashMap<>();
            summary.put("title", title);
            summary.put("overallPassed", overallPassed);
            summary.put("steps", stepSummaries);
            Files.writeString(reportFolder.resolve("summary.json"), mapper.writerWithDefaultPrettyPrinter().writeValueAsString(summary));
            writeJUnitXml(reportFolder, title, stepSummaries, overallPassed);
            System.out.println("Test " + (overallPassed ? "PASSED" : "FAILED"));
            System.exit(overallPassed ? 0 : 1);
        } finally {
            driver.quit();
        }
    }

    private static String sanitize(String s) {
        if (s == null) return "step";
        return s.replaceAll("[^a-zA-Z0-9\\-_\\.]", "_");
    }

    private static void writeJUnitXml(Path reportFolder, String title, List<Map<String,Object>> steps, boolean overallPassed) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.newDocument();

        Element testsuite = doc.createElement("testsuite");
        testsuite.setAttribute("name", title);
        testsuite.setAttribute("tests", String.valueOf(steps.size()));
        testsuite.setAttribute("failures", String.valueOf(steps.stream().filter(s -> !(Boolean)s.get("success")).count()));
        doc.appendChild(testsuite);

        for (Map<String,Object> s : steps) {
            Element tc = doc.createElement("testcase");
            tc.setAttribute("name", String.format("Step %s - %s", s.get("stepIndex"), s.get("stepName")));
            testsuite.appendChild(tc);

            boolean success = (Boolean) s.get("success");
            if (!success) {
                Element failure = doc.createElement("failure");
                failure.setAttribute("message", "Step failed");
                failure.appendChild(doc.createTextNode("See screenshot: " + s.get("screenshot")));
                tc.appendChild(failure);
            }
        }
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource domSource = new DOMSource(doc);
        StreamResult sr = new StreamResult(reportFolder.resolve("junit-report.xml").toFile());
        transformer.transform(domSource, sr);
    }
}


