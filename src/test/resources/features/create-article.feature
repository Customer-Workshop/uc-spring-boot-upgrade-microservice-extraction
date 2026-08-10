@articles
Feature: Create Article
  As an authenticated author
  I want to publish articles
  So that other users can read them

  Background:
    Given the following users are registered:
      | username | email             | password    |
      | johndoe  | john@example.com  | password123 |
      | janedoe  | jane@example.com  | password123 |
    And "johndoe" is logged in

  Scenario: Publish a valid article
    When I create an article with:
      | title       | How to train your dragon |
      | description | Ever wonder how?         |
      | body        | You have to believe      |
      | tags        | dragons, training        |
    Then the response status should be 200
    And the response article title should be "How to train your dragon"
    And the response article slug should be derived from "How to train your dragon"
    And the response article description should be "Ever wonder how?"
    And the response article body should be "You have to believe"
    And the response article tags should be "dragons, training"
    And the response article author should be "johndoe"
    And the response article favorites count should be 0
    And the response article should not be favorited

  Scenario: A published article shows up in the global feed
    When I create an article with:
      | title       | How to train your dragon |
      | description | Ever wonder how?         |
      | body        | You have to believe      |
    Then the response status should be 200
    When I request the global feed
    Then the response status should be 200
    And the response should contain 1 article
    And the response should contain the article "How to train your dragon"

  Scenario Outline: Reject an article with a missing required field
    When I create an article with:
      | title       | <title>       |
      | description | <description> |
      | body        | <body>        |
    Then the response status should be 422
    And the response should report "can't be empty" for the field "<field>"

    Examples: [empty] is sent as an empty string
      | field       | title      | description | body                |
      | title       | [empty]    | Ever wonder | You have to believe |
      | description | My article | [empty]     | You have to believe |
      | body        | My article | Ever wonder | [empty]             |

    Examples: A blank cell is sent as a null value
      | field       | title      | description | body                |
      | description | My article |             | You have to believe |
      | body        | My article | Ever wonder |                     |

  Scenario: Reject an article with every field left empty
    When I create an article with:
      | title       | [empty] |
      | description | [empty] |
      | body        | [empty] |
      | tags        | dragons |
    Then the response status should be 422
    And the response should report "can't be empty" for the field "title"
    And the response should report "can't be empty" for the field "description"
    And the response should report "can't be empty" for the field "body"

  Scenario: Reject an article whose title yields a duplicated slug
    Given "johndoe" has published the following articles:
      | title                    | description      | body                | tags    |
      | How to train your dragon | Ever wonder how? | You have to believe | dragons |
    When I create an article with:
      | title       | How to train your dragon |
      | description | A different description  |
      | body        | A different body         |
    Then the response status should be 422
    And the response should report "article name exists" for the field "title"

  Scenario Outline: Reject a duplicated slug whatever the casing or punctuation of the title
    Given "johndoe" has published the following articles:
      | title                    | description      | body                |
      | How to train your dragon | Ever wonder how? | You have to believe |
    When I create an article with:
      | title       | <title>                 |
      | description | A different description |
      | body        | A different body        |
    Then the response status should be 422
    And the response should report "article name exists" for the field "title"

    Examples: Titles sharing the slug how-to-train-your-dragon
      | title                    |
      | How to train your dragon |
      | HOW TO TRAIN YOUR DRAGON |
      | how to train your dragon |

  Scenario: Another author may not reuse an existing slug either
    Given "johndoe" has published the following articles:
      | title                    | description      | body                |
      | How to train your dragon | Ever wonder how? | You have to believe |
    And "janedoe" is logged in
    When I create an article with:
      | title       | How to train your dragon |
      | description | My own take              |
      | body        | My own body              |
    Then the response status should be 422
    And the response should report "article name exists" for the field "title"

  Scenario: Reject an anonymous article creation
    Given no one is logged in
    When I create an article with:
      | title       | How to train your dragon |
      | description | Ever wonder how?         |
      | body        | You have to believe      |
    Then the response status should be 401
