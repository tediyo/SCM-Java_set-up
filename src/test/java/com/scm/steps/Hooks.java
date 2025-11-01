package com.scm.steps;

import com.scm.utils.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Hooks {
    private WebDriver driver;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Before
    public void setUp(Scenario scenario) {
        driver = DriverManager.getDriver();
        scenario.log("Scenario started at: " + LocalDateTime.now().format(FORMATTER));
        scenario.log("Browser: " + DriverManager.getBrowserName());
    }

    @After
    public void tearDown(Scenario scenario) {
        // Take screenshot on failure with better naming
        if (scenario.isFailed()) {
            try {
                final byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
                String screenshotName = scenario.getName()
                        .replaceAll("[^a-zA-Z0-9\\s]", "_")
                        .replaceAll("\\s+", "_") + "_" + timestamp;
                scenario.attach(screenshot, "image/png", screenshotName);
                scenario.log("Screenshot captured: " + screenshotName);
            } catch (Exception e) {
                scenario.log("Failed to capture screenshot: " + e.getMessage());
            }
        } else {
            // Optionally take screenshot for passed scenarios (uncomment if needed)
            // final byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            // scenario.attach(screenshot, "image/png", scenario.getName() + "_success");
        }
        
        scenario.log("Scenario completed at: " + LocalDateTime.now().format(FORMATTER));
        scenario.log("Status: " + (scenario.isFailed() ? "FAILED" : "PASSED"));
        
        DriverManager.quitDriver();
    }
}
