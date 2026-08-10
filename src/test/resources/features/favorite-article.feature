@articles
Feature: Favorite Article
  As a reader
  I want to favorite the articles I like
  So that I can find them back and show my appreciation

  Background:
    Given the following users are registered:
      | username | email             | password    |
      | johndoe  | john@example.com  | password123 |
      | janedoe  | jane@example.com  | password123 |
      | bobsmith | bob@example.com   | password123 |
    And "johndoe" has published the following articles:
      | title             | description     | body                | tags              |
      | Spring Boot intro | Getting started | Spring Boot is nice | java, spring-boot |
    And "janedoe" is logged in

  Scenario: Favorite an article
    When I favorite the article "Spring Boot intro"
    Then the response status should be 200
    And the response article should be favorited
    And the response article favorites count should be 1

  Scenario: Favoriting twice keeps a single favorite
    When I favorite the article "Spring Boot intro"
    Then the response article favorites count should be 1
    When I favorite the article "Spring Boot intro"
    Then the response status should be 200
    And the response article should be favorited
    And the response article favorites count should be 1

  Scenario: An author may favorite their own article
    Given "johndoe" is logged in
    When I favorite the article "Spring Boot intro"
    Then the response status should be 200
    And the response article should be favorited
    And the response article favorites count should be 1

  Scenario Outline: The favorites count adds up across readers
    Given "<reader>" has favorited the article "Spring Boot intro"
    When I request the article "Spring Boot intro"
    Then the response status should be 200
    And the response article favorites count should be <count>

    Examples: janedoe is the authenticated reader of the background
      | reader   | count |
      | janedoe  | 1     |
      | bobsmith | 1     |

  Scenario: Every favorite of an article is counted
    Given "johndoe" has favorited the article "Spring Boot intro"
    And "bobsmith" has favorited the article "Spring Boot intro"
    When I favorite the article "Spring Boot intro"
    Then the response article favorites count should be 3

  Scenario: Unfavorite an article
    Given "janedoe" has favorited the article "Spring Boot intro"
    When I unfavorite the article "Spring Boot intro"
    Then the response status should be 200
    And the response article should not be favorited
    And the response article favorites count should be 0

  Scenario: Unfavoriting only drops the favorite of the current reader
    Given "janedoe" has favorited the article "Spring Boot intro"
    And "bobsmith" has favorited the article "Spring Boot intro"
    When I unfavorite the article "Spring Boot intro"
    Then the response status should be 200
    And the response article should not be favorited
    And the response article favorites count should be 1

  Scenario: Unfavoriting an article that was never favorited is a no-op
    When I unfavorite the article "Spring Boot intro"
    Then the response status should be 200
    And the response article should not be favorited
    And the response article favorites count should be 0

  Scenario: A favorite is only visible to the reader who made it
    Given "bobsmith" has favorited the article "Spring Boot intro"
    When I request the article "Spring Boot intro"
    Then the response status should be 200
    And the response article should not be favorited
    And the response article favorites count should be 1

  Scenario: The favorites count shows up in the global feed
    Given "janedoe" has favorited the article "Spring Boot intro"
    And "bobsmith" has favorited the article "Spring Boot intro"
    When I request the global feed
    Then the response status should be 200
    And the article "Spring Boot intro" in the response should have 2 favorites

  Scenario Outline: Favoriting requires an existing article
    Given no one is logged in
    When I favorite the article "<title>"
    Then the response status should be 401

    Examples:
      | title             |
      | Spring Boot intro |
      | Unknown article   |

  Scenario: Favoriting an unknown article is rejected
    When I favorite the article "Unknown article"
    Then the response status should be 404
