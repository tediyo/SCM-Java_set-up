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
    private long pageLoadStartTime;
    private long pageLoadEndTime;
    private long searchStartTime = 0;
    private long searchEndTime = 0;

    public GoogleSearchSteps() {
        this.driver = DriverManager.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @Given("I am on the Google homepage")
    public void i_am_on_the_google_homepage() {
        pageLoadStartTime = System.currentTimeMillis();
        driver.get("https://www.google.com");
        pageLoadEndTime = System.currentTimeMillis();
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

        // Start timing the search operation
        searchStartTime = System.currentTimeMillis();
        
        try {
            // Ensure focus before typing
            searchBox.click();
            searchBox.clear();
            
            // Handle empty search - just clear and don't submit
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                searchEndTime = System.currentTimeMillis();
                return; // Don't submit empty search
            }
            
            searchBox.sendKeys(searchTerm);
            searchBox.submit();
        } catch (Exception e) {
            // Retry once in case of overlays or stale elements
            WebElement retryBox = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    searchBox.getTagName().equalsIgnoreCase("textarea") ? By.cssSelector("textarea[name='q']") : By.cssSelector("input[name='q']")));
            retryBox.click();
            retryBox.clear();
            
            // Handle empty search - just clear and don't submit
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                searchEndTime = System.currentTimeMillis();
                return; // Don't submit empty search
            }
            
            retryBox.sendKeys(searchTerm);
            retryBox.submit();
        }
        
        // Wait for results page to load and mark end time
        WebDriverWait extendedWait = new WebDriverWait(driver, Duration.ofSeconds(20));
        try {
            extendedWait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector("#rso")),
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector("#search")),
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector("h3"))
            ));
        } catch (Exception ignore) {
            // proceed even if timeout
        }
        
        searchEndTime = System.currentTimeMillis();
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

    // Performance testing steps
    
    @Then("the page load time should be less than {int} seconds")
    public void the_page_load_time_should_be_less_than_seconds(int maxSeconds) {
        long pageLoadTime = pageLoadEndTime - pageLoadStartTime;
        long maxTimeMillis = maxSeconds * 1000L;
        
        System.out.println("Page load time: " + (pageLoadTime / 1000.0) + " seconds");
        
        Assert.assertTrue("Page load time (" + (pageLoadTime / 1000.0) + "s) should be less than " + maxSeconds + " seconds", 
                pageLoadTime < maxTimeMillis);
    }

    @Then("the search response time should be less than {int} seconds")
    public void the_search_response_time_should_be_less_than_seconds(int maxSeconds) {
        if (searchStartTime == 0 || searchEndTime == 0) {
            throw new AssertionError("Search was not performed. Cannot measure search response time.");
        }
        
        long searchResponseTime = searchEndTime - searchStartTime;
        long maxTimeMillis = maxSeconds * 1000L;
        
        System.out.println("Search response time: " + (searchResponseTime / 1000.0) + " seconds");
        
        Assert.assertTrue("Search response time (" + (searchResponseTime / 1000.0) + "s) should be less than " + maxSeconds + " seconds", 
                searchResponseTime < maxTimeMillis);
    }

    @Then("the total time should be less than {int} seconds")
    public void the_total_time_should_be_less_than_seconds(int maxSeconds) {
        if (searchStartTime == 0 || searchEndTime == 0) {
            throw new AssertionError("Search was not performed. Cannot measure total execution time.");
        }
        
        long totalTime = searchEndTime - pageLoadStartTime;
        long maxTimeMillis = maxSeconds * 1000L;
        
        System.out.println("Total execution time: " + (totalTime / 1000.0) + " seconds");
        System.out.println("  - Page load: " + ((pageLoadEndTime - pageLoadStartTime) / 1000.0) + " seconds");
        System.out.println("  - Search operation: " + ((searchEndTime - searchStartTime) / 1000.0) + " seconds");
        
        Assert.assertTrue("Total time (" + (totalTime / 1000.0) + "s) should be less than " + maxSeconds + " seconds", 
                totalTime < maxTimeMillis);
    }

    @Then("I should see performance metrics")
    public void i_should_see_performance_metrics() {
        long pageLoadTime = pageLoadEndTime - pageLoadStartTime;
        
        System.out.println("\n=== Performance Metrics ===");
        System.out.println("Page Load Time: " + (pageLoadTime / 1000.0) + " seconds");
        
        // Only calculate search metrics if search was performed
        if (searchStartTime > 0 && searchEndTime > 0) {
            long searchResponseTime = searchEndTime - searchStartTime;
            long totalTime = searchEndTime - pageLoadStartTime;
            System.out.println("Search Response Time: " + (searchResponseTime / 1000.0) + " seconds");
            System.out.println("Total Execution Time: " + (totalTime / 1000.0) + " seconds");
        } else {
            System.out.println("Search Response Time: N/A (no search performed)");
            System.out.println("Total Execution Time: N/A (no search performed)");
        }
        
        System.out.println("==========================\n");
        
        // Just log, don't fail - useful for monitoring
    }

    // Helper method to get search box element
    private WebElement getSearchBox() {
        By[] searchSelectors = new By[] {
                By.cssSelector("input[name='q']"),
                By.cssSelector("textarea[name='q']")
        };

        for (By selector : searchSelectors) {
            try {
                WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(selector));
                wait.until(ExpectedConditions.elementToBeClickable(searchBox));
                return searchBox;
            } catch (Exception ignore) {
                // try next selector
            }
        }
        throw new AssertionError("Could not find Google search box.");
    }

    @When("I type {string} in the search box")
    public void i_type_in_the_search_box(String text) {
        WebElement searchBox = getSearchBox();
        try {
            searchBox.click();
            searchBox.clear();
            searchBox.sendKeys(text);
            // Wait a bit for suggestions to appear
            Thread.sleep(500);
        } catch (Exception e) {
            // Retry once in case of overlays or stale elements
            WebElement retryBox = getSearchBox();
            retryBox.click();
            retryBox.clear();
            retryBox.sendKeys(text);
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Then("I should see search suggestions dropdown")
    public void i_should_see_search_suggestions_dropdown() {
        // Google search suggestions appear in various selectors
        By[] suggestionSelectors = new By[] {
                By.cssSelector("ul[role='listbox']"),
                By.cssSelector("div[role='listbox']"),
                By.cssSelector("ul.erkvQe"),
                By.cssSelector("div.sbct"),
                By.xpath("//ul[@role='listbox']//li"),
                By.xpath("//div[@role='listbox']//div[@role='option']")
        };

        boolean suggestionsFound = false;
        for (By selector : suggestionSelectors) {
            try {
                WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
                List<WebElement> suggestions = shortWait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(selector));
                if (!suggestions.isEmpty() && suggestions.get(0).isDisplayed()) {
                    suggestionsFound = true;
                    break;
                }
            } catch (Exception ignore) {
                // try next selector
            }
        }

        Assert.assertTrue("Search suggestions dropdown should be visible", suggestionsFound);
    }

    @Then("I should remain on the Google homepage")
    public void i_should_remain_on_the_google_homepage() {
        String currentUrl = driver.getCurrentUrl();
        // Google homepage URLs can be various formats
        boolean isHomepage = currentUrl.equals("https://www.google.com/") ||
                            currentUrl.equals("https://www.google.com") ||
                            currentUrl.startsWith("https://www.google.com/?") ||
                            currentUrl.startsWith("https://www.google.com/webhp");
        
        Assert.assertTrue("Should remain on Google homepage, but current URL is: " + currentUrl, isHomepage);
    }

    @Then("the search box should be empty")
    public void the_search_box_should_be_empty() {
        WebElement searchBox = getSearchBox();
        String value = searchBox.getAttribute("value");
        if (value == null) {
            value = searchBox.getText();
        }
        Assert.assertTrue("Search box should be empty, but contains: " + value, 
                value == null || value.trim().isEmpty());
    }

    @When("I clear the search box")
    public void i_clear_the_search_box() {
        WebElement searchBox = getSearchBox();
        try {
            searchBox.click();
            searchBox.clear();
            // Also try using keyboard shortcuts or clear button if available
            By[] clearButtonSelectors = new By[] {
                    By.cssSelector("button[aria-label='Clear']"),
                    By.cssSelector("span[aria-label='Clear']"),
                    By.xpath("//button[contains(@aria-label, 'Clear')]")
            };
            for (By selector : clearButtonSelectors) {
                try {
                    WebElement clearBtn = driver.findElement(selector);
                    if (clearBtn.isDisplayed()) {
                        clearBtn.click();
                        break;
                    }
                } catch (Exception ignore) {
                    // try next selector
                }
            }
        } catch (Exception e) {
            // Retry once
            WebElement retryBox = getSearchBox();
            retryBox.click();
            retryBox.clear();
        }
    }

    @Then("I should see at least {int} search result")
    public void i_should_see_at_least_search_result(int minCount) {
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
        
        Assert.assertTrue("Should see at least " + minCount + " search result(s), but found " + resultTexts.size(),
                resultTexts.size() >= minCount);
    }

    @Then("I should see search results")
    public void i_should_see_search_results() {
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
        
        Assert.assertTrue("Should see search results, but found " + resultTexts.size() + " results",
                !resultTexts.isEmpty());
    }
}
