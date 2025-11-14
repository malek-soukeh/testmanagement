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
                    WebElement clickElement;
                    try {
                        clickElement = wait.until(ExpectedConditions.elementToBeClickable(by));
                    } catch (Exception e) {
                        // Si l'élément n'est pas trouvé, essayer de trouver un bouton à l'intérieur
                        // (pour les composants PrimeNG comme p-button)
                        if (selectorType.equals("xpath") && target.contains("button")) {
                            // Essayer de trouver le bouton dans un p-button
                            By fallbackBy = By.xpath("//p-button//button | //p-button/button");
                            try {
                                clickElement = wait.until(ExpectedConditions.elementToBeClickable(fallbackBy));
                            } catch (Exception e2) {
                                // Essayer par texte
                                if (target.contains("Sign In") || target.contains("sign in")) {
                                    By textBy = By.xpath("//button[contains(text(), 'Sign In') or contains(text(), 'sign in')]");
                                    clickElement = wait.until(ExpectedConditions.elementToBeClickable(textBy));
                                } else {
                                    throw e;
                                }
                            }
                        } else {
                            throw e;
                        }
                    }
                    clickElement.click();
                    break;

                case "sendkeys":
                case "type":
                case "input":
                    // Pour les composants PrimeNG comme p-password, essayer d'abord le sélecteur direct
                    // puis chercher l'input à l'intérieur si nécessaire
                    WebElement inputElement;
                    try {
                        inputElement = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
                    } catch (Exception e) {
                        // Si l'élément n'est pas trouvé, essayer de trouver un input à l'intérieur
                        // (pour les composants PrimeNG comme p-password)
                        if (selectorType.equals("css") && target.startsWith("#")) {
                            String id = target.substring(1);
                            By fallbackBy = By.cssSelector("#" + id + " input, #" + id + " input[type='password'], #" + id + " input[type='text']");
                            try {
                                inputElement = wait.until(ExpectedConditions.visibilityOfElementLocated(fallbackBy));
                            } catch (Exception e2) {
                                // Essayer avec XPath
                                By xpathBy = By.xpath("//*[@id='" + id + "']//input");
                                inputElement = wait.until(ExpectedConditions.visibilityOfElementLocated(xpathBy));
                            }
                        } else {
                            throw e;
                        }
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
