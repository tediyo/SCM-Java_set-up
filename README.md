# Selenium Cucumber Java Test Automation Framework

This project is a test automation framework using Selenium WebDriver, Cucumber (BDD), Java, and Gherkin syntax.

## Prerequisites

- Java JDK 11 or higher
- Maven 3.6+ 
- IDE (IntelliJ IDEA, Eclipse, or VS Code recommended)
- Chrome, Firefox, or Edge browser installed

## Project Structure

```
SCM/
├── pom.xml                              # Maven configuration file
├── README.md                            # Project documentation
└── src/
    └── test/
        ├── java/
        │   └── com/
        │       └── scm/
        │           ├── runners/
        │           │   └── RunCucumberTest.java    # Test runner class
        │           ├── steps/
        │           │   ├── Hooks.java              # Before/After hooks
        │           │   ├── GoogleSearchSteps.java  # Step definitions for Google search
        │           │   └── ErmishoeLoginSteps.java # Step definitions for Ermishoe login
        │           └── utils/
        │               └── DriverManager.java      # WebDriver management utility
        └── resources/
            └── features/
                ├── google_search.feature           # Google search feature file
                └── ermishoe_login.feature          # Ermishoe login feature file
```

## Setup Instructions

### 1. Clone or download the project

### 2. Install dependencies

Maven will automatically download all dependencies when you build the project.

```bash
mvn clean install
```

### 3. Verify setup

Run the tests to verify everything is set up correctly:

```bash
mvn test
```

Or run tests with a specific tag:

```bash
mvn test -Dcucumber.filter.tags="@smoke"
```

## Running Tests

### Run all tests

```bash
mvn test
```

### Run tests with specific tags

```bash
# Run only smoke tests
mvn test -Dcucumber.filter.tags="@smoke"

# Run only regression tests
mvn test -Dcucumber.filter.tags="@regression"

# Run both smoke and regression
mvn test -Dcucumber.filter.tags="@smoke or @regression"
```

### Run tests with specific browser

```bash
# Run with Chrome (default)
mvn test -Dbrowser=chrome

# Run with Firefox
mvn test -Dbrowser=firefox

# Run with Edge
mvn test -Dbrowser=edge
```

### Run a specific feature file

```bash
mvn test -Dcucumber.features=src/test/resources/features/google_search.feature
```

## Writing Features

Feature files use Gherkin syntax and are located in `src/test/resources/features/`.

Example:

```gherkin
Feature: Google Search
  As a user
  I want to search on Google
  So that I can find information on the internet

  @smoke
  Scenario: Search for a term on Google
    Given I am on the Google homepage
    When I search for "Selenium WebDriver"
    Then I should see search results containing "Selenium"
```

## Writing Step Definitions

Step definitions are Java classes in `src/test/java/com/scm/steps/` that implement the Gherkin steps.

Example:

```java
@Given("I am on the Google homepage")
public void i_am_on_the_google_homepage() {
    driver.get("https://www.google.com");
}
```

## Test Reports

After running tests, reports are generated in:
- HTML Report: `target/cucumber-reports/cucumber.html`
- JSON Report: `target/cucumber-reports/cucumber.json`
- JUnit XML: `target/cucumber-reports/cucumber.xml`

To view the HTML report, open `target/cucumber-reports/cucumber.html` in your browser.

## Key Components

### DriverManager

Manages WebDriver instances and supports multiple browsers (Chrome, Firefox, Edge). Automatically handles driver setup using WebDriverManager.

### Hooks

Contains `@Before` and `@After` hooks that:
- Initialize the WebDriver before each scenario
- Take screenshots on test failure
- Clean up the WebDriver after each scenario

### Test Runner

`RunCucumberTest.java` is the JUnit test runner that executes Cucumber scenarios.

## Configuration

### Browser Selection

Set the browser using system property:
```bash
-Dbrowser=chrome
```

Supported browsers: `chrome`, `firefox`, `edge`

### Headless Mode

To run tests in headless mode, uncomment the headless options in `DriverManager.java`:

```java
chromeOptions.addArguments("--headless");
```

## Dependencies

- **Selenium WebDriver 4.15.0** - Browser automation
- **Cucumber 7.14.0** - BDD testing framework
- **JUnit 5.10.0** - Test execution framework
- **WebDriverManager 5.6.2** - Automatic driver management

## Troubleshooting

### Browser driver issues

WebDriverManager should automatically download the correct driver. If you encounter issues:
1. Make sure you have internet connectivity
2. Check that your browser is up to date
3. Manually specify driver path if needed

### Tests failing

1. Check that the target website is accessible
2. Verify that selectors match the current page structure
3. Check screenshots in the report for visual debugging

## Next Steps

1. Add more feature files for your application
2. Create page object model classes for better organization
3. Integrate with CI/CD pipelines
4. Add data-driven testing with Cucumber data tables
5. Configure parallel test execution

## Resources

- [Cucumber Documentation](https://cucumber.io/docs/cucumber/)
- [Selenium Documentation](https://www.selenium.dev/documentation/)
- [Gherkin Syntax](https://cucumber.io/docs/gherkin/)
