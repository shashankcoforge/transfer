Feature: I want to login Branch Bank Office main page

  Background: User is Logged In
    Given Dynamic User password is available

  @Shashank
  Scenario: As a user, I want to login and logout successfully from the application
    Given the application "SSD"
    When the user login into the application
    And the user click on Logout button
    Then the user should navigate to "login" page

  Scenario: As a user, I want to add xml feed into application
    Given the application "SSD"
    When the user login into the application
    And the user adds "XML Feed" file into Task Server
    And the user runs file mover job
    And the user runs feed loader job
