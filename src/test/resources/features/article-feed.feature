@articles
Feature: Article Feed
  As a reader
  I want to browse articles
  So that I can find the ones I care about

  Background:
    Given the following users are registered:
      | username | email             | password    |
      | johndoe  | john@example.com  | password123 |
      | janedoe  | jane@example.com  | password123 |
      | bobsmith | bob@example.com   | password123 |
    And "johndoe" has published the following articles:
      | title             | description        | body                | tags               |
      | Spring Boot intro | Getting started    | Spring Boot is nice | java, spring-boot  |
      | REST APIs         | Designing APIs     | Resources and verbs | api-design, java   |
    And "janedoe" has published the following articles:
      | title         | description       | body               | tags          |
      | Microservices | Splitting things  | Small is beautiful | microservices |

  Scenario: Anonymous readers see every article in the global feed
    Given no one is logged in
    When I request the global feed
    Then the response status should be 200
    And the response should contain 3 articles
    And the response articles count should be 3
    And the response should contain the article "Spring Boot intro"
    And the response should contain the article "REST APIs"
    And the response should contain the article "Microservices"

  Scenario Outline: Filter the global feed by tag
    Given no one is logged in
    When I request the global feed with:
      | tag | <tag> |
    Then the response status should be 200
    And the response should contain <count> articles
    And the response articles count should be <count>
    And the response should contain the article "<article>"

    Examples:
      | tag           | count | article           |
      | spring-boot   | 1     | Spring Boot intro |
      | api-design    | 1     | REST APIs         |
      | microservices | 1     | Microservices     |
      | java          | 2     | REST APIs         |

  Scenario: An unknown tag matches nothing
    Given no one is logged in
    When I request the global feed with:
      | tag | kotlin |
    Then the response status should be 200
    And the response should contain 0 articles
    And the response articles count should be 0

  Scenario Outline: Filter the global feed by author
    Given no one is logged in
    When I request the global feed with:
      | author | <author> |
    Then the response status should be 200
    And the response should contain <count> articles
    And the response articles count should be <count>

    Examples:
      | author   | count |
      | johndoe  | 2     |
      | janedoe  | 1     |
      | bobsmith | 0     |

  Scenario: Filter the global feed by the user who favorited the articles
    Given "bobsmith" has favorited the article "Microservices"
    And no one is logged in
    When I request the global feed with:
      | favorited | bobsmith |
    Then the response status should be 200
    And the response should contain 1 article
    And the response should contain the article "Microservices"

  Scenario Outline: Page through the global feed
    Given no one is logged in
    When I request the global feed with:
      | limit  | <limit>  |
      | offset | <offset> |
    Then the response status should be 200
    And the response should contain <count> articles
    And the response articles count should be 3

    Examples: The total count always reports every matching article
      | limit | offset | count |
      | 2     | 0      | 2     |
      | 2     | 2      | 1     |
      | 1     | 0      | 1     |
      | 3     | 0      | 3     |
      | 20    | 0      | 3     |
      | 2     | 10     | 0     |

  Scenario: The user feed is empty until the reader follows someone
    Given "bobsmith" is logged in
    When I request my feed
    Then the response status should be 200
    And the response should contain 0 articles
    And the response articles count should be 0

  Scenario: The user feed only contains the articles of the followed authors
    Given "bobsmith" is logged in
    And "bobsmith" follows "johndoe"
    When I request my feed
    Then the response status should be 200
    And the response should contain 2 articles
    And the response articles count should be 2
    And the response should contain the article "Spring Boot intro"
    And the response should contain the article "REST APIs"
    And the response should not contain the article "Microservices"

  Scenario: The user feed grows when following another author
    Given "bobsmith" is logged in
    And "bobsmith" follows "johndoe"
    And "bobsmith" follows "janedoe"
    When I request my feed
    Then the response status should be 200
    And the response should contain 3 articles
    And the response articles count should be 3

  Scenario Outline: Page through the user feed
    Given "janedoe" has published the following articles:
      | title   | description      | body               |
      | Docker  | Containers 101   | Images and layers  |
      | Testing | Trust your suite | Red, green, refact |
    And "bobsmith" is logged in
    And "bobsmith" follows "janedoe"
    When I request my feed with:
      | limit  | <limit>  |
      | offset | <offset> |
    Then the response status should be 200
    And the response should contain <count> articles
    And the response articles count should be 3

    Examples:
      | limit | offset | count |
      | 2     | 0      | 2     |
      | 2     | 2      | 1     |
      | 20    | 0      | 3     |

  Scenario: The user feed requires an authenticated reader
    Given no one is logged in
    When I request my feed
    Then the response status should be 401
