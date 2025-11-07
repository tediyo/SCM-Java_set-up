package com.scm.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                System.out.println("[DriverManager] Setting up ChromeDriver...");
                
                // Detect Chrome browser version
                String chromeVersion = detectChromeVersion();
                if (chromeVersion != null) {
                    System.out.println("[DriverManager] Detected Chrome browser version: " + chromeVersion);
                    System.out.println("[DriverManager] → You need ChromeDriver version: " + chromeVersion.split("\\.")[0]);
                    System.out.println("[DriverManager] → Download from: https://googlechromelabs.github.io/chrome-for-testing/");
                } else {
                    System.out.println("[DriverManager] ⚠ Could not detect Chrome version automatically");
                    System.out.println("[DriverManager] → Check manually: Chrome → Settings → About Chrome");
                }
                
                // Priority 1: Check for local driver in project's drivers folder
                String projectDir = System.getProperty("user.dir");
                String localDriverPath = projectDir + File.separator + "drivers" + File.separator + "chromedriver.exe";
                File localDriver = new File(localDriverPath);
                
                if (localDriver.exists()) {
                    System.out.println("[DriverManager] ✓ Found local ChromeDriver at: " + localDriverPath);
                    
                    // Verify ChromeDriver version if possible
                    try {
                        ProcessBuilder pb = new ProcessBuilder(localDriverPath, "--version");
                        Process process = pb.start();
                        java.io.BufferedReader reader = new java.io.BufferedReader(
                            new java.io.InputStreamReader(process.getInputStream()));
                        String versionLine = reader.readLine();
                        if (versionLine != null) {
                            System.out.println("[DriverManager] ChromeDriver version: " + versionLine);
                            // Extract major version number
                            Pattern versionPattern = Pattern.compile("(\\d+)\\.\\d+\\.\\d+");
                            Matcher versionMatcher = versionPattern.matcher(versionLine);
                            if (versionMatcher.find()) {
                                String driverMajorVersion = versionMatcher.group(1);
                                String chromeMajorVersion = chromeVersion != null ? chromeVersion.split("\\.")[0] : "?";
                                if (!driverMajorVersion.equals(chromeMajorVersion)) {
                                    System.err.println("[DriverManager] ⚠ WARNING: Version mismatch!");
                                    System.err.println("[DriverManager]    Chrome browser: " + chromeMajorVersion);
                                    System.err.println("[DriverManager]    ChromeDriver: " + driverMajorVersion);
                                    System.err.println("[DriverManager]    They must match! Download ChromeDriver version " + chromeMajorVersion);
                                } else {
                                    System.out.println("[DriverManager] ✓ Version match confirmed!");
                                }
                            }
                        }
                        process.waitFor();
                    } catch (Exception e) {
                        System.out.println("[DriverManager] Could not verify ChromeDriver version: " + e.getMessage());
                    }
                    
                    System.setProperty("webdriver.chrome.driver", localDriverPath);
                    System.out.println("[DriverManager] Using local ChromeDriver from project directory");
                } else {
                    // Priority 2: Try WebDriverManager (if not skipped)
                    String skipWdm = System.getProperty("skip.webdrivermanager", "false");
                    boolean useWdm = !"true".equalsIgnoreCase(skipWdm);
                    
                    if (useWdm) {
                        try {
                            System.out.println("[DriverManager] Local driver not found. Attempting WebDriverManager...");
                            WebDriverManager wdm = WebDriverManager.chromedriver();
                            wdm.setup();
                            System.out.println("[DriverManager] ✓ ChromeDriver setup via WebDriverManager successful!");
                        } catch (Exception e) {
                            System.err.println("[DriverManager] ⚠ WebDriverManager failed: " + e.getClass().getSimpleName());
                            System.err.println("[DriverManager] → Falling back to system PATH ChromeDriver...");
                            // Continue to use system PATH driver
                        }
                    } else {
                        System.out.println("[DriverManager] Local driver not found. Skipping WebDriverManager (using system PATH)...");
                    }
                }
                
                // Verify Chrome browser is accessible
                System.out.println("[DriverManager] Verifying Chrome browser accessibility...");
                String[] chromePaths = {
                        "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
                        "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe",
                        System.getenv("LOCALAPPDATA") + "\\Google\\Chrome\\Application\\chrome.exe"
                };
                
                boolean chromeFound = false;
                String chromePath = null;
                for (String path : chromePaths) {
                    File chromeExe = new File(path);
                    if (chromeExe.exists()) {
                        chromeFound = true;
                        chromePath = path;
                        System.out.println("[DriverManager] ✓ Found Chrome at: " + path);
                        break;
                    }
                }
                
                if (!chromeFound) {
                    System.err.println("[DriverManager] ⚠ WARNING: Chrome browser not found in standard locations!");
                    System.err.println("[DriverManager] ChromeDriver will try to find Chrome automatically...");
                }
                
                ChromeOptions chromeOptions = new ChromeOptions();
                // Uncomment the line below to run in headless mode
                // chromeOptions.addArguments("--headless");
                
                // Add some helpful Chrome options for better compatibility
                chromeOptions.addArguments("--no-sandbox");
                chromeOptions.addArguments("--disable-dev-shm-usage");
                chromeOptions.addArguments("--disable-gpu");
                chromeOptions.addArguments("--disable-blink-features=AutomationControlled");
                // Additional options for better stability with slow-loading pages
                chromeOptions.addArguments("--disable-extensions");
                chromeOptions.addArguments("--disable-web-security");
                chromeOptions.addArguments("--allow-running-insecure-content");
                chromeOptions.addArguments("--ignore-certificate-errors");
                chromeOptions.addArguments("--ignore-ssl-errors");
                chromeOptions.addArguments("--ignore-certificate-errors-spki-list");
                // Set page load strategy to 'normal' for better compatibility
                chromeOptions.setPageLoadStrategy(PageLoadStrategy.NORMAL);
                
                // If Chrome path was found, explicitly set it
                if (chromePath != null) {
                    chromeOptions.setBinary(chromePath);
                    System.out.println("[DriverManager] Using Chrome binary: " + chromePath);
                }
                
                System.out.println("[DriverManager] Creating ChromeDriver instance...");
                try {
                    webDriver = new ChromeDriver(chromeOptions);
                    System.out.println("[DriverManager] ✓ ChromeDriver ready!");
                } catch (Exception e) {
                    System.err.println("\n[DriverManager] ❌ ERROR: Failed to create ChromeDriver session!");
                    System.err.println("[DriverManager] Error: " + e.getMessage());
                    System.err.println("\n[DriverManager] Troubleshooting steps:");
                    System.err.println("  1. Verify Chrome browser is installed and accessible");
                    System.err.println("     - Try opening Chrome manually: " + (chromePath != null ? chromePath : "Check standard locations"));
                    System.err.println("  2. Check if Chrome is blocked by antivirus/firewall");
                    System.err.println("  3. Try running as Administrator");
                    System.err.println("  4. Check Windows Event Viewer for Chrome errors");
                    System.err.println("  5. Verify ChromeDriver is unblocked:");
                    System.err.println("     - Right-click chromedriver.exe → Properties → Unblock");
                    if (chromePath != null) {
                        System.err.println("  6. Chrome found at: " + chromePath);
                        System.err.println("     - Verify this file exists and is executable");
                    } else {
                        System.err.println("  6. Chrome not found in standard locations");
                        System.err.println("     - Reinstall Chrome or specify path manually");
                    }
                    throw new RuntimeException("ChromeDriver initialization failed. See troubleshooting steps above.", e);
                }
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
        // Increased page load timeout for slow-loading pages (e.g., IMDS)
        webDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(120));

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

    /**
     * Detects Chrome browser version by reading the version file
     * @return Chrome version string (e.g., "120.0.6099.109") or null if not found
     */
    private static String detectChromeVersion() {
        // Common Chrome installation paths on Windows
        String[] chromePaths = {
                "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
                "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe",
                System.getenv("LOCALAPPDATA") + "\\Google\\Chrome\\Application\\chrome.exe",
                System.getenv("PROGRAMFILES") + "\\Google\\Chrome\\Application\\chrome.exe",
                System.getenv("PROGRAMFILES(X86)") + "\\Google\\Chrome\\Application\\chrome.exe"
        };

        for (String chromePath : chromePaths) {
            File chromeExe = new File(chromePath);
            if (chromeExe.exists()) {
                // Chrome version is in a file in the same directory
                Path chromeDir = chromeExe.toPath().getParent();
                Path versionFile = chromeDir.resolve("version");
                
                if (Files.exists(versionFile)) {
                    try {
                        String content = Files.readString(versionFile);
                        // Extract version number (format: 120.0.6099.109)
                        Pattern pattern = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)");
                        Matcher matcher = pattern.matcher(content);
                        if (matcher.find()) {
                            return matcher.group(1);
                        }
                    } catch (IOException e) {
                        // Try alternative method
                    }
                }
                
                // Alternative: Try to get version from chrome.exe properties
                // For now, we'll use a simpler approach - check the directory name
                // Chrome stores version in folder name like "120.0.6099.109"
                try {
                    String[] files = chromeDir.toFile().list();
                    if (files != null) {
                        for (String file : files) {
                            Pattern pattern = Pattern.compile("^(\\d+\\.\\d+\\.\\d+\\.\\d+)$");
                            Matcher matcher = pattern.matcher(file);
                            if (matcher.find()) {
                                return matcher.group(1);
                            }
                        }
                    }
                } catch (Exception e) {
                    // Continue to next method
                }
            }
        }
        
        return null;
    }
}