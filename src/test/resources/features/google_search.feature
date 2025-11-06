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
    When I search for "xyzabc123nonexisent"
    Then I should see search results
    And the page title should contain "xyzabc123nonexisent"

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

  @negative
  Scenario: Empty search query handling
    Given I am on the Google homepage
    When I search for ""
    Then I should remain on the Google homepage
    And the search box should be empty

  @edge-case
  Scenario: Search with numbers only
    Given I am on the Google homepage
    When I search for "12345"
    Then I should see search results
    And the page title should contain "12345"

  @regression
  Scenario: Search with search operators - exact phrase
    Given I am on the Google homepage
    When I search for "\"Selenium WebDriver\""
    Then I should see search results containing "Selenium"
    And the page title should contain "Selenium"

  @ui
  Scenario: Verify search suggestions appear while typing
    Given I am on the Google homepage
    When I type "selenium" in the search box
    Then I should see search suggestions dropdown

  @functional
  Scenario: Clear search box functionality
    Given I am on the Google homepage
    When I type "test query" in the search box
    And I clear the search box
    Then the search box should be empty

  @functional
  Scenario: Multiple consecutive searches
    Given I am on the Google homepage
    When I search for "Python"
    Then I should see search results containing "Python"
    When I search for "Java"
    Then I should see search results containing "Java"
    When I search for "JavaScript"
    Then I should see search results containing "JavaScript"

  @edge-case
  Scenario: Search with very short query
    Given I am on the Google homepage
    When I search for "AI"
    Then I should see search results containing "AI"
    And the page title should contain "AI"

  @regression
  Scenario: Search with mixed case query
    Given I am on the Google homepage
    When I search for "SeLeNiUm TeStInG"
    Then I should see search results containing "Selenium"
    And the page title should contain "Selenium"

  @edge-case
  Scenario: Search with URL-like query
    Given I am on the Google homepage
    When I search for "www.google.com"
    Then I should see search results
    And the page title should contain "google.com"

  @functional
  Scenario: Search result count verification
    Given I am on the Google homepage
    When I search for "automation testing"
    Then I should see at least 1 search result
    And I should see search results containing "automation"
 