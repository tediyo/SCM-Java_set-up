# How to Run Tests

## Quick Start

Since we're using Maven Wrapper, use `.\mvnw.cmd` instead of `mvn`.

## Running All Tests

```bash
.\mvnw.cmd test
```

This will run all Cucumber scenarios in all feature files.

## Running Tests with Specific Tags

### Run only smoke tests:
```bash
.\mvnw.cmd test -Dcucumber.filter.tags="@smoke"
```

### Run only regression tests:
```bash
.\mvnw.cmd test -Dcucumber.filter.tags="@regression"
```

### Run both smoke and regression:
```bash
.\mvnw.cmd test -Dcucumber.filter.tags="@smoke or @regression"
```

## Running a Specific Feature File

```bash
.\mvnw.cmd test -Dcucumber.features=src/test/resources/features/google_search.feature
```

## Running Tests with Different Browsers

### Chrome (default):
```bash
.\mvnw.cmd test -Dbrowser=chrome
```

### Firefox:
```bash
.\mvnw.cmd test -Dbrowser=firefox
```

### Edge:
```bash
.\mvnw.cmd test -Dbrowser=edge
```

## Combine Options

You can combine browser and tags:
```bash
.\mvnw.cmd test -Dbrowser=firefox -Dcucumber.filter.tags="@smoke"
```

## Viewing Test Reports

After tests complete, you have multiple ways to view reports:

### Quick Access - Report Index
Open the report index page for easy navigation to all reports:
```
target/cucumber-reports/index.html
```

### Individual Reports
- **HTML Report**: `target/cucumber-reports/cucumber-html-report.html` - Detailed interactive report
- **Timeline Report**: `target/cucumber-reports/timeline/timeline.html` - Visual execution timeline
- **JSON Report**: `target/cucumber-reports/cucumber.json` - For CI/CD integration
- **Usage Report**: `target/cucumber-reports/cucumber-usage.json` - Performance metrics

### Report Features
- ✅ Automatic screenshots on failures
- ✅ Scenario logs with timestamps
- ✅ Browser information
- ✅ Performance metrics
- ✅ Failed test rerun file

📖 See [REPORTS.md](REPORTS.md) for detailed information about all available reports.

## Common Commands Summary

| Command | Description |
|---------|-------------|
| `.\mvnw.cmd test` | Run all tests |
| `.\mvnw.cmd clean test` | Clean and run all tests |
| `.\mvnw.cmd clean install` | Clean, compile, and install (downloads dependencies) |
| `.\mvnw.cmd test -Dcucumber.filter.tags="@smoke"` | Run smoke tests only |

## Note

If you have Maven installed system-wide, you can use `mvn` instead of `.\mvnw.cmd`:
```bash
mvn test
```

