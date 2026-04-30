#!/bin/bash
# Integration test script that verifies monolith and comments microservice communicate correctly.
# Usage: ./integration-test.sh
# Prerequisites: docker-compose must be available

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "=== Building and starting services ==="
docker-compose up --build -d

echo "=== Waiting for services to be healthy ==="
RETRIES=30
until docker-compose exec -T comments-service sh -c 'curl -sf http://localhost:8081/api/comments?articleId=test > /dev/null' 2>/dev/null || [ $RETRIES -eq 0 ]; do
  echo "Waiting for comments-service... ($RETRIES retries left)"
  RETRIES=$((RETRIES - 1))
  sleep 5
done

if [ $RETRIES -eq 0 ]; then
  echo "FAIL: comments-service did not become healthy"
  docker-compose logs
  docker-compose down
  exit 1
fi

RETRIES=30
until curl -sf http://localhost:8080/tags > /dev/null 2>&1 || [ $RETRIES -eq 0 ]; do
  echo "Waiting for monolith... ($RETRIES retries left)"
  RETRIES=$((RETRIES - 1))
  sleep 5
done

if [ $RETRIES -eq 0 ]; then
  echo "FAIL: monolith did not become healthy"
  docker-compose logs
  docker-compose down
  exit 1
fi

echo "=== Services are up. Running integration tests ==="

PASS=0
FAIL=0

# Test 1: Register a user
echo "--- Test 1: Register user ---"
REGISTER_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"user":{"email":"integration@test.com","username":"integrationuser","password":"password123"}}')
HTTP_CODE=$(echo "$REGISTER_RESPONSE" | tail -n1)
BODY=$(echo "$REGISTER_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "201" ]; then
  TOKEN=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin)['user']['token'])" 2>/dev/null || echo "")
  if [ -n "$TOKEN" ]; then
    echo "PASS: User registered, got token"
    PASS=$((PASS + 1))
  else
    echo "FAIL: User registered but no token"
    FAIL=$((FAIL + 1))
  fi
else
  echo "FAIL: User registration failed with HTTP $HTTP_CODE"
  echo "$BODY"
  FAIL=$((FAIL + 1))
fi

# Test 2: Create an article
echo "--- Test 2: Create article ---"
ARTICLE_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST http://localhost:8080/articles \
  -H "Content-Type: application/json" \
  -H "Authorization: Token $TOKEN" \
  -d '{"article":{"title":"Integration Test Article","description":"Testing microservice communication","body":"This article tests that comments are stored in the microservice","tagList":["test"]}}')
HTTP_CODE=$(echo "$ARTICLE_RESPONSE" | tail -n1)
BODY=$(echo "$ARTICLE_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "201" ]; then
  SLUG=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin)['article']['slug'])" 2>/dev/null || echo "")
  echo "PASS: Article created with slug: $SLUG"
  PASS=$((PASS + 1))
else
  echo "FAIL: Article creation failed with HTTP $HTTP_CODE"
  echo "$BODY"
  FAIL=$((FAIL + 1))
fi

# Test 3: Create a comment via monolith (should be stored in comments-service)
echo "--- Test 3: Create comment via monolith ---"
COMMENT_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "http://localhost:8080/articles/$SLUG/comments" \
  -H "Content-Type: application/json" \
  -H "Authorization: Token $TOKEN" \
  -d '{"comment":{"body":"This comment is stored in the microservice!"}}')
HTTP_CODE=$(echo "$COMMENT_RESPONSE" | tail -n1)
BODY=$(echo "$COMMENT_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "201" ]; then
  COMMENT_ID=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin)['comment']['id'])" 2>/dev/null || echo "")
  echo "PASS: Comment created via monolith with id: $COMMENT_ID"
  PASS=$((PASS + 1))
else
  echo "FAIL: Comment creation failed with HTTP $HTTP_CODE"
  echo "$BODY"
  FAIL=$((FAIL + 1))
fi

# Test 4: Verify comment exists in comments-service directly
echo "--- Test 4: Verify comment exists in comments-service ---"
DIRECT_RESPONSE=$(curl -s -w "\n%{http_code}" "http://localhost:8081/api/comments/$COMMENT_ID")
HTTP_CODE=$(echo "$DIRECT_RESPONSE" | tail -n1)
BODY=$(echo "$DIRECT_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "200" ]; then
  DIRECT_BODY=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin)['body'])" 2>/dev/null || echo "")
  if [ "$DIRECT_BODY" = "This comment is stored in the microservice!" ]; then
    echo "PASS: Comment verified in comments-service"
    PASS=$((PASS + 1))
  else
    echo "FAIL: Comment body mismatch in comments-service"
    FAIL=$((FAIL + 1))
  fi
else
  echo "FAIL: Comment not found in comments-service (HTTP $HTTP_CODE)"
  FAIL=$((FAIL + 1))
fi

# Test 5: Get comments via monolith
echo "--- Test 5: Get comments via monolith ---"
GET_RESPONSE=$(curl -s -w "\n%{http_code}" "http://localhost:8080/articles/$SLUG/comments")
HTTP_CODE=$(echo "$GET_RESPONSE" | tail -n1)
BODY=$(echo "$GET_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "200" ]; then
  COMMENT_COUNT=$(echo "$BODY" | python3 -c "import sys,json; print(len(json.load(sys.stdin)['comments']))" 2>/dev/null || echo "0")
  if [ "$COMMENT_COUNT" -ge "1" ]; then
    echo "PASS: Got $COMMENT_COUNT comment(s) via monolith"
    PASS=$((PASS + 1))
  else
    echo "FAIL: No comments returned via monolith"
    FAIL=$((FAIL + 1))
  fi
else
  echo "FAIL: Getting comments failed with HTTP $HTTP_CODE"
  FAIL=$((FAIL + 1))
fi

# Test 6: Delete comment via monolith
echo "--- Test 6: Delete comment via monolith ---"
DELETE_RESPONSE=$(curl -s -w "\n%{http_code}" -X DELETE "http://localhost:8080/articles/$SLUG/comments/$COMMENT_ID" \
  -H "Authorization: Token $TOKEN")
HTTP_CODE=$(echo "$DELETE_RESPONSE" | tail -n1)

if [ "$HTTP_CODE" = "204" ]; then
  echo "PASS: Comment deleted via monolith"
  PASS=$((PASS + 1))
else
  echo "FAIL: Comment deletion failed with HTTP $HTTP_CODE"
  FAIL=$((FAIL + 1))
fi

# Test 7: Verify comment is gone from comments-service
echo "--- Test 7: Verify comment is deleted from comments-service ---"
VERIFY_RESPONSE=$(curl -s -w "\n%{http_code}" "http://localhost:8081/api/comments/$COMMENT_ID")
HTTP_CODE=$(echo "$VERIFY_RESPONSE" | tail -n1)

if [ "$HTTP_CODE" = "404" ]; then
  echo "PASS: Comment verified deleted from comments-service"
  PASS=$((PASS + 1))
else
  echo "FAIL: Comment still exists in comments-service (HTTP $HTTP_CODE)"
  FAIL=$((FAIL + 1))
fi

echo ""
echo "=== Integration Test Results ==="
echo "PASSED: $PASS"
echo "FAILED: $FAIL"
echo "TOTAL:  $((PASS + FAIL))"

echo ""
echo "=== Cleaning up ==="
docker-compose down -v

if [ $FAIL -gt 0 ]; then
  exit 1
else
  echo "All integration tests passed!"
  exit 0
fi
