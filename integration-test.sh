#!/bin/bash
# Integration test script that verifies monolith and comments microservice communication.
# Requires both services to be running (e.g., via docker-compose up).

set -e

MONOLITH_URL="${MONOLITH_URL:-http://localhost:8080}"
COMMENTS_URL="${COMMENTS_URL:-http://localhost:8081}"

echo "=== Integration Test: Monolith <-> Comments Microservice ==="

# Test 1: Comments microservice health
echo ""
echo "--- Test 1: Comments microservice is reachable ---"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$COMMENTS_URL/api/comments?articleId=article-1")
if [ "$HTTP_CODE" = "200" ]; then
  echo "PASS: Comments microservice returned HTTP $HTTP_CODE"
else
  echo "FAIL: Comments microservice returned HTTP $HTTP_CODE (expected 200)"
  exit 1
fi

# Test 2: Create a comment via the microservice directly
echo ""
echo "--- Test 2: Create comment via comments microservice ---"
COMMENT_RESPONSE=$(curl -s -X POST "$COMMENTS_URL/api/comments" \
  -H "Content-Type: application/json" \
  -d '{"body":"Integration test comment","userId":"user-1","articleId":"article-1"}')
COMMENT_ID=$(echo "$COMMENT_RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin)['id'])" 2>/dev/null || echo "")
if [ -n "$COMMENT_ID" ]; then
  echo "PASS: Comment created with ID: $COMMENT_ID"
else
  echo "FAIL: Could not create comment. Response: $COMMENT_RESPONSE"
  exit 1
fi

# Test 3: Retrieve comments from the microservice
echo ""
echo "--- Test 3: Retrieve comments from comments microservice ---"
COMMENTS=$(curl -s "$COMMENTS_URL/api/comments?articleId=article-1")
COMMENT_COUNT=$(echo "$COMMENTS" | python3 -c "import sys, json; print(len(json.load(sys.stdin)))" 2>/dev/null || echo "0")
if [ "$COMMENT_COUNT" -gt 0 ]; then
  echo "PASS: Found $COMMENT_COUNT comment(s) for article-1"
else
  echo "FAIL: No comments found for article-1"
  exit 1
fi

# Test 4: Delete the comment via the microservice
echo ""
echo "--- Test 4: Delete comment via comments microservice ---"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$COMMENTS_URL/api/comments/$COMMENT_ID")
if [ "$HTTP_CODE" = "204" ]; then
  echo "PASS: Comment deleted successfully"
else
  echo "FAIL: Delete returned HTTP $HTTP_CODE (expected 204)"
  exit 1
fi

# Test 5: Verify the monolith is reachable
echo ""
echo "--- Test 5: Monolith is reachable ---"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$MONOLITH_URL/tags")
if [ "$HTTP_CODE" = "200" ]; then
  echo "PASS: Monolith returned HTTP $HTTP_CODE"
else
  echo "FAIL: Monolith returned HTTP $HTTP_CODE (expected 200)"
  exit 1
fi

echo ""
echo "=== All integration tests passed ==="
