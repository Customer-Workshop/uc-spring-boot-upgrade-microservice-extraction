#!/usr/bin/env bash
# Exercises the comments and favorites REST contracts against a running monolith and
# prints a normalised transcript (volatile ids/timestamps removed) so the same script
# can be run with the strangler flags off and on and the two transcripts diffed.
#
# Usage: BASE=http://localhost:8080 scripts/strangler-parity.sh
set -euo pipefail

BASE="${BASE:-http://localhost:8080}"
SLUG_OWN="${SLUG_OWN:-getting-started-with-spring-boot}"   # authored by user-1
SLUG_OTHER="${SLUG_OTHER:-rest-api-best-practices}"        # authored by user-2

# drops volatile keys and rewrites uuids that appear inside string values (e.g. error paths)
normalize() {
  jq -S 'walk(if type == "object"
              then with_entries(select(.key | test("^(id|createdAt|updatedAt|cursor|timestamp)$") | not))
              else . end)' 2>/dev/null \
    | sed -E 's/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/<uuid>/g'
}

login() {
  curl -s -X POST "$BASE/users/login" -H 'Content-Type: application/json' \
    -d "{\"user\":{\"email\":\"$1\",\"password\":\"password123\"}}" | jq -r '.user.token'
}

# status + normalised body for one request
call() {
  local label="$1" method="$2" path="$3" token="${4:-}" body="${5:-}"
  local args=(-s -o /tmp/parity.body -w '%{http_code}' -X "$method" "$BASE$path" -H 'Accept: application/json')
  [ -n "$token" ] && args+=(-H "Authorization: Token $token")
  [ -n "$body" ] && args+=(-H 'Content-Type: application/json' -d "$body")
  local code
  code=$(curl "${args[@]}")
  echo "### $label -> HTTP $code"
  if [ -s /tmp/parity.body ]; then
    normalize < /tmp/parity.body || cat /tmp/parity.body
    echo
  else
    echo '(empty body)'
  fi
}

TOKEN1=$(login john@example.com)
TOKEN3=$(login bob@example.com)

echo "== comments =="
call "GET comments (anonymous)" GET "/articles/$SLUG_OWN/comments"
call "GET comments (authenticated)" GET "/articles/$SLUG_OWN/comments" "$TOKEN1"
call "POST comment (empty body -> 422)" POST "/articles/$SLUG_OWN/comments" "$TOKEN1" '{"comment":{"body":""}}'
call "POST comment (unauthenticated -> 401)" POST "/articles/$SLUG_OWN/comments" "" '{"comment":{"body":"nope"}}'
call "GET comments (unknown slug -> 404)" GET "/articles/no-such-article/comments" "$TOKEN1"

# create + read back + delete, so the run leaves no state behind
CREATED=$(curl -s -X POST "$BASE/articles/$SLUG_OWN/comments" \
  -H "Authorization: Token $TOKEN1" -H 'Content-Type: application/json' \
  -d '{"comment":{"body":"parity probe"}}')
echo "### POST comment -> body"
echo "$CREATED" | normalize
CID=$(echo "$CREATED" | jq -r '.comment.id')
call "GET comments after create" GET "/articles/$SLUG_OWN/comments" "$TOKEN1"
call "DELETE own comment -> 204" DELETE "/articles/$SLUG_OWN/comments/$CID" "$TOKEN1"

# 403: bob comments on user-2's article, john (not the author of either) tries to delete
OTHERS=$(curl -s -X POST "$BASE/articles/$SLUG_OTHER/comments" \
  -H "Authorization: Token $TOKEN3" -H 'Content-Type: application/json' \
  -d '{"comment":{"body":"parity probe by bob"}}')
OID=$(echo "$OTHERS" | jq -r '.comment.id')
call "DELETE someone else's comment -> 403" DELETE "/articles/$SLUG_OTHER/comments/$OID" "$TOKEN1"
call "DELETE cleanup by author -> 204" DELETE "/articles/$SLUG_OTHER/comments/$OID" "$TOKEN3"

echo "== favorites =="
call "POST favorite -> 200" POST "/articles/$SLUG_OTHER/favorite" "$TOKEN1"
call "POST favorite again (idempotent) -> 200" POST "/articles/$SLUG_OTHER/favorite" "$TOKEN1"
call "DELETE favorite -> 200" DELETE "/articles/$SLUG_OTHER/favorite" "$TOKEN1"
call "DELETE favorite again (no row) -> 200" DELETE "/articles/$SLUG_OTHER/favorite" "$TOKEN1"
call "POST favorite (unauthenticated -> 401)" POST "/articles/$SLUG_OTHER/favorite" ""
call "POST favorite (unknown slug -> 404)" POST "/articles/no-such-article/favorite" "$TOKEN1"
