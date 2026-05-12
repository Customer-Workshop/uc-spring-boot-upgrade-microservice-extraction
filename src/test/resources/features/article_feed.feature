Feature: Article Feed
  As a user
  I want to browse and filter articles
  So that I can discover content that interests me

  Scenario: Get the global article feed without authentication
    Given articles exist in the system
    When a user requests the global article feed
    Then the response status should be 200
    And the response should contain a list of articles
    And the response should contain an articles count

  Scenario: Get the user feed for an authenticated user who follows others
    Given a registered user exists
    And the user is authenticated
    And the user follows another author who has articles
    When the user requests their personal feed
    Then the response status should be 200
    And the response should contain articles from followed authors

  Scenario: Get the user feed returns empty when not following anyone
    Given a fresh user exists with no follows
    And the fresh user is authenticated
    When the fresh user requests their personal feed
    Then the response status should be 200
    And the feed should contain 0 articles

  Scenario: Filter articles by tag
    Given articles exist in the system
    When a user requests articles filtered by tag "java"
    Then the response status should be 200
    And all returned articles should have tag "java"

  Scenario: Filter articles by tag returns empty for non-existent tag
    Given articles exist in the system
    When a user requests articles filtered by tag "nonexistenttag12345"
    Then the response status should be 200
    And the feed should contain 0 articles

  Scenario: Paginate global article feed with limit
    Given articles exist in the system
    When a user requests articles with limit 2 and offset 0
    Then the response status should be 200
    And the response should contain at most 2 articles

  Scenario: Paginate global article feed with offset
    Given articles exist in the system
    When a user requests articles with limit 2 and offset 0
    Then the response status should be 200
    When a user requests articles with limit 2 and offset 2
    Then the response status should be 200
    And the two pages should have different articles
