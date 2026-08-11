Feature: Favorite Article
  As an authenticated user
  I want to favorite and unfavorite articles
  So that I can keep track of the articles I like

  Background:
    Given a registered user "johndoe" with email "john@example.com" and password "password123"
    And a registered user "janedoe" with email "jane@example.com" and password "password123"
    And an article titled "Spring Boot basics" exists authored by "johndoe"

  Scenario: Favorite an article
    Given I am logged in as "janedoe"
    When I favorite the article "Spring Boot basics"
    Then the response status should be 200
    And the article should be marked as favorited
    And the article favorites count should be 1

  Scenario: Unfavorite a previously favorited article
    Given I am logged in as "janedoe"
    And I have favorited the article "Spring Boot basics"
    When I unfavorite the article "Spring Boot basics"
    Then the response status should be 200
    And the article should not be marked as favorited
    And the article favorites count should be 0

  Scenario: Unfavoriting an article that was never favorited is a no-op
    Given I am logged in as "janedoe"
    When I unfavorite the article "Spring Boot basics"
    Then the response status should be 200
    And the article should not be marked as favorited
    And the article favorites count should be 0

  Scenario: Favoriting the same article twice keeps the count at one
    Given I am logged in as "janedoe"
    And I have favorited the article "Spring Boot basics"
    When I favorite the article "Spring Boot basics"
    Then the response status should be 200
    And the article favorites count should be 1

  Scenario: Favorites count aggregates across users
    Given I am logged in as "janedoe"
    And I have favorited the article "Spring Boot basics"
    And I am logged in as "johndoe"
    When I favorite the article "Spring Boot basics"
    Then the response status should be 200
    And the article favorites count should be 2

  Scenario: Favorited flag is per user
    Given I am logged in as "janedoe"
    And I have favorited the article "Spring Boot basics"
    And I am logged in as "johndoe"
    When I fetch the article "Spring Boot basics"
    Then the response status should be 200
    And the article should not be marked as favorited
    And the article favorites count should be 1

  Scenario: Favorites count is visible on the global feed
    Given I am logged in as "janedoe"
    And I have favorited the article "Spring Boot basics"
    And I am not logged in
    When I request the global feed
    Then the response status should be 200
    And the feed article "Spring Boot basics" should have a favorites count of 1

  Scenario: Filter the global feed by the user who favorited an article
    Given I am logged in as "janedoe"
    And I have favorited the article "Spring Boot basics"
    And I am not logged in
    When I request the global feed filtered by favorited by "janedoe"
    Then the response status should be 200
    And the feed should contain 1 articles
    And the feed should contain the article "Spring Boot basics"

  Scenario: Favoriting requires authentication
    Given I am not logged in
    When I favorite the article "Spring Boot basics"
    Then the response status should be 401

  Scenario: Favoriting an unknown article returns not found
    Given I am logged in as "janedoe"
    When I favorite the article with slug "does-not-exist"
    Then the response status should be 404
