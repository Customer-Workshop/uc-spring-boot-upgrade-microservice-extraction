Feature: Create Article
  As an authenticated user
  I want to create articles
  So that I can share content with the community

  Background:
    Given a registered user exists
    And the user is authenticated

  Scenario: Successfully create an article with valid fields
    When the user creates an article with title "Test Article" description "A test description" and body "Article body content"
    Then the response status should be 200
    And the response should contain article with title "Test Article"
    And the response should contain article with slug "test-article"
    And the response should contain article with description "A test description"
    And the response should contain article with body "Article body content"

  Scenario: Successfully create an article with tags
    When the user creates an article with title "Tagged Article" description "Tagged desc" body "Tagged body" and tags "java,spring"
    Then the response status should be 200
    And the response should contain article with title "Tagged Article"
    And the article should have tag "java"
    And the article should have tag "spring"

  Scenario: Fail to create an article with missing title
    When the user creates an article with title "" description "Some description" and body "Some body"
    Then the response status should be 422

  Scenario: Fail to create an article with missing description
    When the user creates an article with title "No Desc Article" description "" and body "Some body"
    Then the response status should be 422

  Scenario: Fail to create an article with missing body
    When the user creates an article with title "No Body Article" description "Has desc" and body ""
    Then the response status should be 422

  Scenario: Fail to create an article with a duplicate slug
    When the user creates an article with title "Unique Title" description "First article" and body "First body"
    Then the response status should be 200
    When the user creates an article with title "Unique Title" description "Second article" and body "Second body"
    Then the response status should be 422
