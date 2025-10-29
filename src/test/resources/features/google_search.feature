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
