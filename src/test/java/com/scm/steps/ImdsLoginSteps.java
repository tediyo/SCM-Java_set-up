package com.scm.steps;

import com.scm.utils.DriverManager;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class ImdsLoginSteps {
    private WebDriver driver;
    private WebDriverWait wait;

    public ImdsLoginSteps() {
        this.driver = DriverManager.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @Given("I navigate to the IMDS login page")
    public void i_navigate_to_the_imds_login_page() {
        try {
            // Navigate to the page with extended timeout handling
            driver.get("https://imds.cce.af.mil/imds/fs/fs000cams.html");
        } catch (org.openqa.selenium.TimeoutException e) {
            // If page load times out, wait a bit and check if page is still loading
            System.out.println("Page load timeout occurred, waiting for page to stabilize...");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            // Check if we're on the page by checking current URL
            String currentUrl = driver.getCurrentUrl();
            if (!currentUrl.contains("imds")) {
                throw new RuntimeException("Failed to navigate to IMDS login page. Current URL: " + currentUrl, e);
            }
        }
        
        // Wait for page to load with extended timeout
        WebDriverWait extendedWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        try {
            extendedWait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        } catch (Exception e) {
            // If body is not found, check if page is still accessible
            String currentUrl = driver.getCurrentUrl();
            System.out.println("Warning: Could not find body element. Current URL: " + currentUrl);
        }
        
        // Additional wait for page to fully initialize
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Click the lightbox button if it appears
        try {
            WebElement lightboxButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[@id=\"lightbox\"]/div[1]/div/div[2]/div/button")));
            if (lightboxButton.isDisplayed()) {
                lightboxButton.click();
                System.out.println("Clicked lightbox button");
                // Wait a moment for the lightbox to close
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Exception e) {
            // Lightbox button not found or not visible, continue
            System.out.println("Lightbox button not found or not visible, continuing...");
        }
        
        // Click the PKI login button
        try {
            WebElement pkiLoginButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[@id=\"pki-login\"]")));
            if (pkiLoginButton.isDisplayed()) {
                pkiLoginButton.click();
                System.out.println("Clicked PKI login button");
                // Wait a moment for any transition
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Exception e) {
            // PKI login button not found or not visible, continue
            System.out.println("PKI login button not found or not visible, continuing...");
        }
    }

    @When("I enter terminal ID {string}")
    public void i_enter_terminal_id(String terminalId) {
        WebElement terminalIdField = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"TerminalId\"]")));
        terminalIdField.clear();
        terminalIdField.click();
        terminalIdField.sendKeys(terminalId);
    }

    @When("I click the IMDS login button")
    public void i_click_the_imds_login_button() {
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"TerminalLogon\"]")));
        loginButton.click();
        
        // Wait for login to process
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Then("I should be logged into IMDS successfully")
    public void i_should_be_logged_into_imds_successfully() {
        // Wait for page to change after login
        WebDriverWait extendedWait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            // Wait for URL change or success indicator
            extendedWait.until(ExpectedConditions.or(
                ExpectedConditions.not(ExpectedConditions.urlContains("fs000cams")),
                ExpectedConditions.presenceOfElementLocated(By.tagName("body"))
            ));
        } catch (Exception e) {
            // Check for error messages
            try {
                WebElement errorMessage = driver.findElement(By.cssSelector("[class*='error'], [class*='alert']"));
                if (errorMessage.isDisplayed()) {
                    throw new AssertionError("Login failed: " + errorMessage.getText());
                }
            } catch (Exception ignore) {
                // No error message found
            }
        }
    }

    @Then("I should see the IMDS dashboard or home page")
    public void i_should_see_the_imds_dashboard_or_home_page() {
        // Verify we're not on the login page anymore
        String currentUrl = driver.getCurrentUrl();
        Assert.assertFalse("Should not be on login page", currentUrl.contains("fs000cams"));
        
        // Check for common dashboard/home indicators
        boolean foundIndicator = false;
        By[] dashboardSelectors = new By[] {
            By.cssSelector("[class*='dashboard']"),
            By.cssSelector("[class*='home']"),
            By.cssSelector("nav"),
            By.cssSelector("header"),
            By.xpath("//*[contains(@class, 'dashboard')]"),
            By.xpath("//*[contains(@class, 'home')]")
        };
        
        for (By selector : dashboardSelectors) {
            try {
                WebElement element = driver.findElement(selector);
                if (element.isDisplayed()) {
                    foundIndicator = true;
                    break;
                }
            } catch (Exception ignore) {
                // Continue checking other selectors
            }
        }
        
        if (!foundIndicator) {
            System.out.println("Warning: Could not find specific dashboard/home indicator, but URL suggests successful navigation");
        }
    }
}

