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
import java.util.List;
import java.util.stream.Collectors;

public class GoogleSearchSteps {
    private WebDriver driver;
    private WebDriverWait wait;

    public GoogleSearchSteps() {
        this.driver = DriverManager.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @Given("I am on the Google homepage")
    public void i_am_on_the_google_homepage() {
        driver.get("https://www.google.com");
        // Handle cookie consent  if present (different locales show different buttons)
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            By[] consentSelectors = new By[] {
                    By.id("L2AGLb"),
                    By.id("introAgreeButton"),
                    By.cssSelector("button[aria-label='Accept all']"),
                    By.xpath("//button[.//div[text()='I agree']]"),
                    By.xpath("//button[normalize-space()='I agree']"),
                    By.xpath("//button[contains(., 'Accept all')]"),
                    By.xpath("//div[@role='none']//button[contains(., 'Accept')]")
            };
            for (By selector : consentSelectors) {
                try {
                    WebElement btn = shortWait.until(ExpectedConditions.elementToBeClickable(selector));
                    btn.click();
                    break;
                } catch (Exception ignore) {
                    // try next selector
                }
            }
        } catch (Exception ignoreOuter) {
            // consent not shown; proceed
        }
    }

    @When("I search for {string}")
    public void i_search_for(String searchTerm) {
        // Google may render the search box as input or textarea depending on UI
        By[] searchSelectors = new By[] {
                By.cssSelector("input[name='q']"),
                By.cssSelector("textarea[name='q']")
        };

        WebElement searchBox = null;
        for (By selector : searchSelectors) {
            try {
                searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(selector));
                wait.until(ExpectedConditions.elementToBeClickable(searchBox));
                break;
            } catch (Exception ignore) {
                // try next selector
            }
        }

        if (searchBox == null) {
            // As a fallback, try focusing the body and sending '/' which often focuses search
            throw new AssertionError("Could not find Google search box.");
        }

        try {
            // Ensure focus before typing
            searchBox.click();
            searchBox.clear();
            searchBox.sendKeys(searchTerm);
            searchBox.submit();
        } catch (Exception e) {
            // Retry once in case of overlays or stale elements
            WebElement retryBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    searchBox.getTagName().equalsIgnoreCase("textarea") ? By.cssSelector("textarea[name='q']") : By.cssSelector("input[name='q']")));
            retryBox.click();
            retryBox.clear();
            retryBox.sendKeys(searchTerm);
            retryBox.submit();
        }
    }

    @Then("I should see search results containing {string}")
    public void i_should_see_search_results_containing(String expectedText) {
        // Wait for the search results page to load completely
        WebDriverWait extendedWait = new WebDriverWait(driver, Duration.ofSeconds(20));
        try {
            extendedWait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector("#rso")),
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector("#search")),
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector("h3"))
            ));
        } catch (Exception ignore) {
            // proceed to attempt collecting results
        }

        // Try multiple selector strategies for Google results
        List<WebElement> results = driver.findElements(By.cssSelector("#rso h3, #search h3, div.g h3, a h3"));
        
        // Get all visible h3 elements with text
        List<String> resultTexts = results.stream()
                .filter(element -> {
                    try {
                        return element.isDisplayed() && !element.getText().trim().isEmpty();
                    } catch (Exception e) {
                        return false;
                    }
                })
                .map(WebElement::getText)
                .collect(Collectors.toList());
        
        // Check if any result contains the expected text
        boolean found = resultTexts.stream()
                .anyMatch(text -> text.toLowerCase().contains(expectedText.toLowerCase()));
        
        String displayResults = resultTexts.stream()
                .limit(5) // Only show first 5 for readability
                .collect(Collectors.joining(" | "));
        
        Assert.assertTrue("Search results should contain: " + expectedText + 
                ". Found " + resultTexts.size() + " results: " + displayResults, found);
    }

    @Then("the page title should contain {string}")
    public void the_page_title_should_contain(String expectedTitle) {
        wait.until(ExpectedConditions.titleContains(expectedTitle));
        String actualTitle = driver.getTitle();
        Assert.assertTrue("Page title should contain: " + expectedTitle, 
                actualTitle.toLowerCase().contains(expectedTitle.toLowerCase()));
    }
}
