Feature: Create Article
  As an authenticated user
  I want to publish articles
  So that other users can read them

  Background:
    Given a registered user "johndoe" with email "john@example.com" and password "password123"
    And I am logged in as "johndoe"

  Scenario: Successfully create an article with all fields
    When I create an article with title "How to train your dragon", description "Ever wonder how?", body "You have to believe" and tags "dragons,training"
    Then the response status should be 200
    And the article title should be "How to train your dragon"
    And the article slug should be "how-to-train-your-dragon"
    And the article description should be "Ever wonder how?"
    And the article body should be "You have to believe"
    And the article author should be "johndoe"
    And the article tag list should contain "dragons" and "training"

  Scenario: Create an article without tags
    When I create an article with title "Untagged article", description "No tags here", body "Body text" and tags ""
    Then the response status should be 200
    And the article tag list should be empty

  Scenario Outline: Reject an article with a missing required field
    When I create an article with title "<title>", description "<description>" and body "<body>"
    Then the response status should be 422
    And the response should report the error "<field>" as "can't be empty"

    Examples:
      | title       | description | body      | field       |
      |             | A desc      | A body    | title       |
      | A title     |             | A body    | description |
      | A title     | A desc      |           | body        |

  Scenario: Reject an article with all required fields missing
    When I create an article with title "", description "" and body ""
    Then the response status should be 422
    And the response should report the error "title" as "can't be empty"
    And the response should report the error "description" as "can't be empty"
    And the response should report the error "body" as "can't be empty"

  Scenario: Reject an article whose title produces a duplicate slug
    Given an article titled "Duplicate slug article" exists authored by "johndoe"
    When I create an article with title "Duplicate slug article", description "Another desc" and body "Another body"
    Then the response status should be 422
    And the response should report the error "title" as "article name exists"

  Scenario: Another user cannot reuse an existing slug either
    Given an article titled "Shared title" exists authored by "johndoe"
    And a registered user "janedoe" with email "jane@example.com" and password "password123"
    And I am logged in as "janedoe"
    When I create an article with title "Shared title", description "Jane's desc" and body "Jane's body"
    Then the response status should be 422
    And the response should report the error "title" as "article name exists"

  Scenario: Anonymous users cannot create articles
    Given I am not logged in
    When I create an article with title "Anonymous article", description "Desc" and body "Body"
    Then the response status should be 401
