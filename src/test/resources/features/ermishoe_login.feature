Feature: Ermishoe Login
  As a user
  I want to login to the Ermishoe application
  So that I can access my account

  @smoke
  Scenario: Successful login with valid credentials
    Given I navigate to the Ermishoe login page
    When I enter username "test@example.com"
    And I enter password "password123"
    And I click the login button
    Then I should be logged in successfully
    And I should see the dashboard or home page

