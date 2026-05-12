Feature: Favorite Article
  As an authenticated user
  I want to favorite and unfavorite articles
  So that I can bookmark articles I enjoy

  Background:
    Given a registered user exists
    And the user is authenticated
    And an article exists in the system

  Scenario: Successfully favorite an article
    When the user favorites the article
    Then the response status should be 200
    And the article should be marked as favorited
    And the article favorites count should be at least 1

  Scenario: Successfully unfavorite an article
    Given the user has favorited the article
    When the user unfavorites the article
    Then the response status should be 200
    And the article should not be marked as favorited

  Scenario: Favorite count increments when a user favorites an article
    Given the initial favorites count is recorded
    When the user favorites the article
    Then the response status should be 200
    And the article favorites count should be greater than the initial count
