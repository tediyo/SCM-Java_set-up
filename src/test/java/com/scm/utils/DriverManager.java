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
                try {
                    // Try to setup Edge driver
                    WebDriverManager.edgedriver().setup();
                } catch (Exception e) {
                    System.err.println("ERROR: Could not download Edge driver from the internet.");
                    System.err.println("Possible causes:");
                    System.err.println("  1. No internet connection");
                    System.err.println("  2. Firewall/proxy blocking access to msedgedriver.azureedge.net");
                    System.err.println("  3. DNS resolution issues");
                    System.err.println("\nSolutions:");
                    System.err.println("  1. Check your internet connection");
                    System.err.println("  2. Download EdgeDriver manually and add it to PATH:");
                    System.err.println("     https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/");
                    System.err.println("  3. If using a proxy, configure it in your network settings");
                    throw new RuntimeException("Edge driver setup failed. See error messages above.", e);
                }
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

    public static String getBrowserName() {
        return browserName;
    }
}