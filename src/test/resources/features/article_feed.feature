Feature: Article Feed
  As a reader
  I want to browse articles globally, by the authors I follow, and by tag
  So that I can find content I care about

  Background:
    Given a registered user "johndoe" with email "john@example.com" and password "password123"
    And a registered user "janedoe" with email "jane@example.com" and password "password123"
    And a registered user "bobsmith" with email "bob@example.com" and password "password123"
    And the following articles exist:
      | author   | title                | description   | body        | tags               |
      | johndoe  | Spring Boot basics   | john desc 1   | john body 1 | java,spring-boot   |
      | johndoe  | REST API design      | john desc 2   | john body 2 | java,api-design    |
      | janedoe  | Microservices intro  | jane desc 1   | jane body 1 | microservices      |
      | janedoe  | Docker for devs      | jane desc 2   | jane body 2 | docker,tutorial    |
      | bobsmith | Testing with Cucumber| bob desc 1    | bob body 1  | testing,tutorial   |

  Scenario: Global feed returns every article to an anonymous reader
    Given I am not logged in
    When I request the global feed
    Then the response status should be 200
    And the feed should contain 5 articles
    And the articles count should be 5
    And the feed should contain the article "Testing with Cucumber"

  Scenario: Global feed can be filtered by author
    Given I am not logged in
    When I request the global feed filtered by author "janedoe"
    Then the response status should be 200
    And the feed should contain 2 articles
    And every article in the feed should be authored by "janedoe"

  Scenario: User feed only contains articles from followed authors
    Given I am logged in as "bobsmith"
    And I follow "janedoe"
    When I request my user feed
    Then the response status should be 200
    And the feed should contain 2 articles
    And every article in the feed should be authored by "janedoe"

  Scenario: User feed is empty when following nobody
    Given I am logged in as "bobsmith"
    When I request my user feed
    Then the response status should be 200
    And the feed should contain 0 articles

  Scenario: User feed requires authentication
    Given I am not logged in
    When I request my user feed
    Then the response status should be 401

  Scenario: Filter the global feed by tag
    Given I am not logged in
    When I request the global feed filtered by tag "java"
    Then the response status should be 200
    And the feed should contain 2 articles
    And every article in the feed should be tagged "java"
    And the feed should contain the article "Spring Boot basics"
    And the feed should contain the article "REST API design"

  Scenario: Filter the global feed by a tag shared across authors
    Given I am not logged in
    When I request the global feed filtered by tag "tutorial"
    Then the response status should be 200
    And the feed should contain 2 articles
    And every article in the feed should be tagged "tutorial"

  Scenario: Filtering by an unknown tag returns no articles
    Given I am not logged in
    When I request the global feed filtered by tag "nonexistent-tag"
    Then the response status should be 200
    And the feed should contain 0 articles
    And the articles count should be 0

  Scenario: Paginate the global feed
    Given I am not logged in
    When I request the global feed with limit 2 and offset 0
    Then the response status should be 200
    And the feed should contain 2 articles
    And the articles count should be 5

  Scenario: Second page of the global feed
    Given I am not logged in
    When I request the global feed with limit 2 and offset 2
    Then the response status should be 200
    And the feed should contain 2 articles
    And the articles count should be 5

  Scenario: Offset beyond the last article returns an empty page
    Given I am not logged in
    When I request the global feed with limit 2 and offset 10
    Then the response status should be 200
    And the feed should contain 0 articles
    And the articles count should be 5

  Scenario: Pagination pages do not overlap
    Given I am not logged in
    When I request the global feed with limit 2 and offset 0
    And I remember the returned article titles as "page one"
    And I request the global feed with limit 2 and offset 2
    And I remember the returned article titles as "page two"
    Then "page one" and "page two" should not share any article

  Scenario: Paginate the user feed
    Given a registered user "carol" with email "carol@example.com" and password "password123"
    And the following articles exist:
      | author | title            | description  | body        | tags |
      | carol  | Carol article 1  | carol desc 1 | carol body1 |      |
      | carol  | Carol article 2  | carol desc 2 | carol body2 |      |
      | carol  | Carol article 3  | carol desc 3 | carol body3 |      |
    And I am logged in as "bobsmith"
    And I follow "carol"
    When I request my user feed with limit 2 and offset 0
    Then the response status should be 200
    And the feed should contain 2 articles
    And the articles count should be 3

  Scenario: Second page of the user feed
    Given a registered user "carol" with email "carol@example.com" and password "password123"
    And the following articles exist:
      | author | title            | description  | body        | tags |
      | carol  | Carol article 1  | carol desc 1 | carol body1 |      |
      | carol  | Carol article 2  | carol desc 2 | carol body2 |      |
      | carol  | Carol article 3  | carol desc 3 | carol body3 |      |
    And I am logged in as "bobsmith"
    And I follow "carol"
    When I request my user feed with limit 2 and offset 2
    Then the response status should be 200
    And the feed should contain 1 articles
    And the articles count should be 3
