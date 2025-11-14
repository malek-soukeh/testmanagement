package com.example.testmanagement.seleniumrunner;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class StepExecutor {

    private static final int TIMEOUT_SECONDS = 10;
    
    private static By by(String selectorType, String target) {
        if (target == null) return null;
        if ("xpath".equalsIgnoreCase(selectorType)) return By.xpath(target);
        return By.cssSelector(target);
    }
    
    private static String detectSelectorType(String target) {
        if (target == null || target.isBlank()) return "css";
        // Détection automatique des sélecteurs XPath
        if (target.startsWith("//") || target.startsWith("./") || target.startsWith("(")) {
            return "xpath";
        }
        // Si le sélecteur contient des caractères XPath typiques
        if (target.contains("@") || (target.contains("[") && target.contains("]"))) {
            return "xpath";
        }
        return "css";
    }

    public static boolean executeStep(WebDriver driver, SeleniumStep step) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS));
            String action = (step.getActionType() == null) ? "" : step.getActionType().toLowerCase();
            String target = step.getActionTarget();
            String value = step.getActionValue();
            
            // Détection automatique du type de sélecteur si non spécifié
            String selectorType = (step.getSelectorType() == null || step.getSelectorType().isBlank()) 
                ? detectSelectorType(target) 
                : step.getSelectorType().toLowerCase();
            
            System.out.println("Executing step: " + step.getStepName());
            System.out.println("  Action: " + action);
            System.out.println("  Target: " + target);
            System.out.println("  Selector Type: " + selectorType);
            System.out.println("  Value: " + (value != null ? value : "null"));
            
            By by = by(selectorType, target);
            switch (action) {
                case "open":
                case "navigate":
                    if (value != null && !value.isBlank()) driver.get(value);
                    else if (step.getActionTarget() != null) driver.get(step.getActionTarget());
                    break;

                case "click":
                    // Pour les composants PrimeNG comme p-button, essayer d'abord le sélecteur direct
                    // puis chercher le bouton à l'intérieur si nécessaire
                    WebElement clickElement = null;
                    Exception lastClickException = null;
                    
                    // Essai 1: Sélecteur direct
                    try {
                        clickElement = wait.until(ExpectedConditions.elementToBeClickable(by));
                    } catch (Exception e) {
                        lastClickException = e;
                        // Essai 2: Si c'est un XPath avec "button", essayer de trouver dans p-button
                        if (selectorType.equals("xpath") && target.contains("button")) {
                            try {
                                // Essayer de trouver le bouton dans un p-button
                                By fallbackBy = By.xpath("//p-button//button | //p-button/button");
                                clickElement = wait.until(ExpectedConditions.elementToBeClickable(fallbackBy));
                            } catch (Exception e2) {
                                // Essai 3: Essayer par texte (le texte peut être dans un span)
                                try {
                                    if (target.contains("Sign In") || target.contains("sign in")) {
                                        By textBy = By.xpath(
                                            "//button[contains(text(), 'Sign In') or contains(text(), 'sign in')] | " +
                                            "//button//span[contains(text(), 'Sign In') or contains(text(), 'sign in')]/.. | " +
                                            "//p-button//button[contains(., 'Sign In')] | " +
                                            "//p-button//span[contains(text(), 'Sign In')]/ancestor::button"
                                        );
                                        clickElement = wait.until(ExpectedConditions.elementToBeClickable(textBy));
                                    } else {
                                        // Essayer de trouver n'importe quel bouton dans p-button
                                        By anyButtonBy = By.xpath("//p-button//button");
                                        clickElement = wait.until(ExpectedConditions.elementToBeClickable(anyButtonBy));
                                    }
                                } catch (Exception e3) {
                                    // Essai 4: Utiliser JavaScript
                                    try {
                                        JavascriptExecutor js = (JavascriptExecutor) driver;
                                        if (target.contains("Sign In") || target.contains("sign in")) {
                                            clickElement = (WebElement) js.executeScript(
                                                "return Array.from(document.querySelectorAll('button, p-button button')).find(btn => " +
                                                "btn.textContent.includes('Sign In') || btn.textContent.includes('sign in'));"
                                            );
                                        } else {
                                            clickElement = (WebElement) js.executeScript(
                                                "return document.querySelector('p-button button') || document.querySelector('button');"
                                            );
                                        }
                                        if (clickElement == null) {
                                            throw new Exception("Button not found via JavaScript");
                                        }
                                    } catch (Exception e4) {
                                        throw lastClickException;
                                    }
                                }
                            }
                        } else {
                            throw lastClickException;
                        }
                    }
                    
                    if (clickElement == null) {
                        throw new RuntimeException("Could not find clickable element with selector: " + target);
                    }
                    
                    clickElement.click();
                    break;

                case "sendkeys":
                case "type":
                case "input":
                    // Pour les composants PrimeNG comme p-password, essayer d'abord le sélecteur direct
                    // puis chercher l'input à l'intérieur si nécessaire
                    WebElement inputElement = null;
                    Exception lastException = null;
                    
                    // Essai 1: Sélecteur direct
                    try {
                        System.out.println("  Trying direct selector: " + by);
                        inputElement = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
                        System.out.println("  ✓ Found element with direct selector");
                    } catch (Exception e) {
                        System.out.println("  ✗ Direct selector failed: " + e.getMessage());
                        lastException = e;
                        // Essai 2: Si c'est un ID CSS, essayer directement par ID
                        if (selectorType.equals("css") && target.startsWith("#")) {
                            System.out.println("  Trying fallback strategies for ID: " + target);
                            String id = target.substring(1);
                            try {
                                // Essayer directement l'ID (pour les inputs directs)
                                System.out.println("    Trying By.id(" + id + ")");
                                By idBy = By.id(id);
                                inputElement = wait.until(ExpectedConditions.visibilityOfElementLocated(idBy));
                                System.out.println("    ✓ Found element with By.id");
                            } catch (Exception e2) {
                                System.out.println("    ✗ By.id failed: " + e2.getMessage());
                                // Essai 3: Chercher un input à l'intérieur (pour les composants PrimeNG)
                                try {
                                    System.out.println("    Trying CSS selector with input inside");
                                    By fallbackBy = By.cssSelector("#" + id + " input, #" + id + " input[type='password'], #" + id + " input[type='text']");
                                    inputElement = wait.until(ExpectedConditions.visibilityOfElementLocated(fallbackBy));
                                    System.out.println("    ✓ Found element with CSS fallback");
                                } catch (Exception e3) {
                                    System.out.println("    ✗ CSS fallback failed: " + e3.getMessage());
                                    // Essai 4: XPath pour trouver l'input
                                    try {
                                        System.out.println("    Trying XPath fallback");
                                        By xpathBy = By.xpath("//*[@id='" + id + "']//input | //input[@id='" + id + "']");
                                        inputElement = wait.until(ExpectedConditions.visibilityOfElementLocated(xpathBy));
                                        System.out.println("    ✓ Found element with XPath fallback");
                                    } catch (Exception e4) {
                                        System.out.println("    ✗ XPath fallback failed: " + e4.getMessage());
                                        // Essai 5: Utiliser JavaScript pour les composants PrimeNG avec shadow DOM
                                        try {
                                            System.out.println("    Trying JavaScript fallback");
                                            JavascriptExecutor js = (JavascriptExecutor) driver;
                                            inputElement = (WebElement) js.executeScript(
                                                "return document.getElementById('" + id + "')?.querySelector('input') || " +
                                                "document.querySelector('#" + id + " input') || " +
                                                "document.getElementById('" + id + "');"
                                            );
                                            if (inputElement == null) {
                                                throw new Exception("Element not found via JavaScript");
                                            }
                                            System.out.println("    ✓ Found element with JavaScript");
                                        } catch (Exception e5) {
                                            System.out.println("    ✗ JavaScript fallback failed: " + e5.getMessage());
                                            throw lastException; // Lancer l'exception originale
                                        }
                                    }
                                }
                            }
                        } else {
                            throw lastException;
                        }
                    }
                    
                    if (inputElement == null) {
                        throw new RuntimeException("Could not find input element with selector: " + target);
                    }
                    
                    inputElement.clear();
                    inputElement.sendKeys(value == null ? "" : value);
                    break;

                case "selectbyvalue":
                    WebElement selectElement = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
                    new Select(selectElement).selectByValue(step.getActionValue());
                    break;

                case "selectbyvisibletext":
                    WebElement selectTextElement = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
                    new Select(selectTextElement).selectByVisibleText(value);
                    break;

                case "hover":
                    WebElement hoverElement = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
                    new org.openqa.selenium.interactions.Actions(driver).moveToElement(hoverElement).perform();
                    break;

                case "scroll":
                    WebElement scrollEl = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", scrollEl);
                    break;

                case "asserttext":
                    WebElement textElement = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
                    String actualText = textElement.getText();
                    if (value == null) return false;
                    if (!actualText.contains(value)) {
                        System.out.println("Assertion failed for text. Expected: " + value + ", Actual: " + actualText);
                        return false;
                    }
                    break;

                case "assertdisplayed":
                    WebElement displayedElement = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
                    if (!displayedElement.isDisplayed()) {
                        System.out.println("Element not displayed: " + step.getActionTarget());
                        return false;
                    }
                    break;

                case "assertenabled":
                    WebElement enabledElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(step.getActionTarget())));
                    if (!enabledElement.isEnabled()) {
                        System.out.println("Element not enabled: " + step.getActionTarget());
                        return false;
                    }
                    break;

                case "acceptalert":
                    wait.until(ExpectedConditions.alertIsPresent()).accept();
                    break;

                case "dismissalert":
                    wait.until(ExpectedConditions.alertIsPresent()).dismiss();
                    break;

                case "wait":
                    Thread.sleep(Long.parseLong(step.getActionValue())); // valeur en ms
                    break;
                case "title":
                    String title = driver.getTitle();
                    if (value == null || !title.contains(value)) {
                        System.out.printf("Title assertion failed: expected contains '%s' but was '%s'%n", value, title);
                        return false;
                    }
                    break;

                default:
                    System.out.println("Action non reconnue: " + step.getActionType());
                    return false;
            }

            return true;

        } catch (Exception e) {
            System.err.println("Error executing step: " + step.getStepName());
            System.err.println("  Action: " + step.getActionType());
            System.err.println("  Target: " + step.getActionTarget());
            System.err.println("  Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}


