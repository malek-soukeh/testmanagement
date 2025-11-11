package com.example.testmanagement.seleniumrunner;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class StepExecutor {

    private static final int TIMEOUT_SECONDS = 10;

    public static boolean executeStep(WebDriver driver, SeleniumStep step) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS));

            switch (step.getActionType().toLowerCase()) {

                case "navigate":
                    driver.get(step.getActionValue());
                    break;

                case "click":
                    WebElement clickElement = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(step.getActionTarget())));
                    clickElement.click();
                    break;

                case "sendkeys":
                    WebElement inputElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(step.getActionTarget())));
                    inputElement.clear();
                    inputElement.sendKeys(step.getActionValue());
                    break;

                case "selectbyvalue":
                    WebElement selectElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(step.getActionTarget())));
                    new Select(selectElement).selectByValue(step.getActionValue());
                    break;

                case "selectbyvisibletext":
                    WebElement selectTextElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(step.getActionTarget())));
                    new Select(selectTextElement).selectByVisibleText(step.getActionValue());
                    break;

                case "hover":
                    WebElement hoverElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(step.getActionTarget())));
                    new org.openqa.selenium.interactions.Actions(driver).moveToElement(hoverElement).perform();
                    break;

                case "scroll":
                    WebElement scrollElement = driver.findElement(By.xpath(step.getActionTarget()));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", scrollElement);
                    break;

                case "asserttext":
                    WebElement textElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(step.getActionTarget())));
                    String actualText = textElement.getText();
                    if (!actualText.equals(step.getExpectedResult())) {
                        System.out.println("Assertion failed for text. Expected: " + step.getExpectedResult() + ", Actual: " + actualText);
                        return false;
                    }
                    break;

                case "assertdisplayed":
                    WebElement displayedElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(step.getActionTarget())));
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

                default:
                    System.out.println("Action non reconnue: " + step.getActionType());
                    return false;
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
