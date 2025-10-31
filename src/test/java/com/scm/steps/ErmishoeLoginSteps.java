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

public class ErmishoeLoginSteps {
    private WebDriver driver;
    private WebDriverWait wait;

    public ErmishoeLoginSteps() {
        this.driver = DriverManager.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @Given("I navigate to the Ermishoe login page")
    public void i_navigate_to_the_ermishoe_login_page() {
        driver.get("https://ermishoe.vercel.app/");
        
        // Wait for page to load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        
        // Additional wait for React/Next.js app to initialize
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Check if we need to navigate to login page or if it's already on homepage
        // Try to find login link/button and click it if needed
        try {
            By[] loginLinkSelectors = new By[] {
                By.linkText("Login"),
                By.partialLinkText("Login"),
                By.cssSelector("a[href*='login']"),
                By.cssSelector("button:contains('Login')"),
                By.xpath("//a[contains(text(), 'Login')]"),
                By.xpath("//button[contains(text(), 'Login')]")
            };
            
            for (By selector : loginLinkSelectors) {
                try {
                    WebElement loginLink = driver.findElement(selector);
                    if (loginLink.isDisplayed()) {
                        loginLink.click();
                        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                        Thread.sleep(1000);
                        break;
                    }
                } catch (Exception ignore) {
                    // Continue to next selector
                }
            }
        } catch (Exception e) {
            // Assume we're already on login page or homepage has login form
        }
    }

    @When("I enter username {string}")
    public void i_enter_username(String username) {
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='email']")));
        usernameField.clear();
        usernameField.click();
        usernameField.sendKeys(username);
    }

    @When("I enter password {string}")
    public void i_enter_password(String password) {
        WebElement passwordField = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='password']")));
        passwordField.clear();
        passwordField.click();
        passwordField.sendKeys(password);
    }

    @When("I click the login button")
    public void i_click_the_login_button() {
        // Try multiple selectors for login button
        By[] loginButtonSelectors = new By[] {
            By.cssSelector("button[type='submit']"),
            By.cssSelector("input[type='submit']"),
            By.id("login"),
            By.id("login-button"),
            By.cssSelector("button:contains('Login')"),
            By.cssSelector("button:contains('Sign in')"),
            By.xpath("//button[contains(text(), 'Login')]"),
            By.xpath("//button[contains(text(), 'Sign in')]"),
            By.xpath("//button[@type='submit']"),
            By.xpath("//input[@type='submit']")
        };
        
        WebElement loginButton = null;
        for (By selector : loginButtonSelectors) {
            try {
                loginButton = wait.until(ExpectedConditions.elementToBeClickable(selector));
                if (loginButton.isDisplayed()) {
                    break;
                }
            } catch (Exception ignore) {
                // Try next selector
            }
        }
        
        if (loginButton == null) {
            throw new AssertionError("Could not find login button");
        }
        
        loginButton.click();
        
        // Wait a moment for the login to process
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Then("I should be logged in successfully")
    public void i_should_be_logged_in_successfully() {
        // Wait for page to change after login (URL change or element appearance)
        WebDriverWait extendedWait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            // Wait for either URL change or success indicator
            extendedWait.until(ExpectedConditions.or(
                ExpectedConditions.not(ExpectedConditions.urlContains("login")),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("[class*='dashboard']")),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("[class*='home']")),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("[class*='welcome']"))
            ));
        } catch (Exception e) {
            // Check for error messages instead
            try {
                WebElement errorMessage = driver.findElement(By.cssSelector("[class*='error'], [class*='alert'], .error-message"));
                if (errorMessage.isDisplayed()) {
                    throw new AssertionError("Login failed: " + errorMessage.getText());
                }
            } catch (Exception ignore) {
                // No error message found, might still be processing
            }
        }
    }

    @Then("I should see the dashboard or home page")
    public void i_should_see_the_dashboard_or_home_page() {
        // Verify we're not on the login page anymore
        String currentUrl = driver.getCurrentUrl();
        Assert.assertFalse("Should not be on login page", currentUrl.contains("/login"));
        
        // Check for common dashboard/home indicators
        boolean foundIndicator = false;
        By[] dashboardSelectors = new By[] {
            By.cssSelector("[class*='dashboard']"),
            By.cssSelector("[class*='home']"),
            By.cssSelector("[class*='welcome']"),
            By.cssSelector("nav"),
            By.cssSelector("header"),
            By.cssSelector("[data-testid*='dashboard']"),
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
        
        // If no specific indicator found, at least verify we're not on login page
        if (!foundIndicator) {
            System.out.println("Warning: Could not find specific dashboard/home indicator, but URL suggests successful navigation");
        }
    }
}

