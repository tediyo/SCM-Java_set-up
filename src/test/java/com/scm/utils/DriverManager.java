package com.scm.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

public class DriverManager {
    private static WebDriver driver;
    private static String browserName = System.getProperty("browser", "chrome").toLowerCase();

    public static WebDriver getDriver() {
        if (driver == null) {
            driver = createDriver();
        }
        return driver;
    }

    private static WebDriver createDriver() {
        WebDriver webDriver;

        switch (browserName) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                // Uncomment the line below to run in headless mode
                // chromeOptions.addArguments("--headless");
                webDriver = new ChromeDriver(chromeOptions);
                break;
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                // Uncomment the line below to run in headless mode
                // firefoxOptions.addArguments("--headless");
                webDriver = new FirefoxDriver(firefoxOptions);
                break;
            case "edge":
                WebDriverManager.edgedriver().setup();
                EdgeOptions edgeOptions = new EdgeOptions();
                // Uncomment the line below to run in headless mode
                // edgeOptions.addArguments("--headless");
                webDriver = new EdgeDriver(edgeOptions);
                break;
            default:
                throw new IllegalArgumentException("Browser not supported: " + browserName);
        }

        webDriver.manage().window().maximize();
        webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        webDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));

        return webDriver;
    }

    public static void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    public static void closeDriver() {
        if (driver != null) {
            driver.close();
        }
    }
}