Feature: Google Search
  As a user
  I want to search on Google
  So that I can find information on the internet

  @smoke
  Scenario: Search for "selenium test" on Google
    Given I am on the Google homepage
    When I search for "selenium test"
    Then I should see search results containing "selenium"
    And the page title should contain "selenium test"

  @smoke
  Scenario: Search for different term - "Java programming"
    Given I am on the Google homepage
    When I search for "Java programming"
    Then I should see search results containing "Java"
    And the page title should contain "Java"

  @regression
  Scenario: Search with special characters
    Given I am on the Google homepage
    When I search for "C# programming"
    Then I should see search results containing "C#"
    And the page title should contain "C#"


  @critical
  Scenario: Search for popular term - "Python tutorial"
    Given I am on the Google homepage
    When I search for "Python tutorial"
    Then I should see search results containing "Python"
    And the page title should contain "Python"


  @edge-case
  Scenario: Search for long phrase
    Given I am on the Google homepage
    When I search for "how to learn web automation testing with Selenium"
    Then I should see search results containing "Selenium"
    And the page title should contain "Selenium"


  @ui
  Scenario: Verify search results page displays correctly
    Given I am on the Google homepage
    When I search for "JavaScript"
    Then I should see search results containing "JavaScript"
    And the page title should contain "JavaScript"

  @negative
  Scenario: Search for non-existent term still shows results
    Given I am on the Google homepage
    When I search for "xyzabc123nonexistent"
    Then I should see search results
    And the page title should contain "xyzabc123nonexistent"

  @performance
  Scenario: Search response time is acceptable
    Given I am on the Google homepage
    When I search for "React framework"
    Then I should see search results containing "React"
    And the page title should contain "React"
    And the page load time should be less than 5 seconds
    And the search response time should be less than 25 seconds
    And the total time should be less than 30 seconds
    And I should see performance metrics

  @performance
  Scenario: Verify homepage loads quickly
    Given I am on the Google homepage
    Then the page load time should be less than 3 seconds
    And I should see performance metrics