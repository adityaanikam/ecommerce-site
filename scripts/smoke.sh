#!/usr/bin/env bash
#
# End-to-end smoke test for the microservices stack.
#
# Brings everything up with docker compose, then walks the path a real customer
# takes and checks the parts that are easy to get wrong:
#
#   1. every service reports ready
#   2. register a new user
#   3. log in and get a token
#   4. list products through the gateway
#   5. check the gateway rejects a request with no token
#   6. place an order with an Idempotency-Key
#   7. check stock went down, which only happens if the OrderPlaced event made it
#      through the outbox, Kafka and the consumer
#   8. replay the same Idempotency-Key and check no second order was created and
#      stock did not move again
#
# Usage:
#   scripts/smoke.sh              bring the stack up, test, leave it running
#   scripts/smoke.sh --down       tear the stack down afterwards
#   scripts/smoke.sh --no-up      test a stack that is already running
#
# Exits non-zero on the first failure.

set -Eeuo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

GATEWAY="${GATEWAY_URL:-http://localhost:8080}"
API="$GATEWAY/api"
CATALOG_DIRECT="${CATALOG_URL:-http://localhost:8081}"

BRING_UP=1
TEAR_DOWN=0
for arg in "$@"; do
  case "$arg" in
    --down)   TEAR_DOWN=1 ;;
    --no-up)  BRING_UP=0 ;;
    -h|--help) sed -n '2,30p' "${BASH_SOURCE[0]}"; exit 0 ;;
    *) echo "unknown option: $arg" >&2; exit 2 ;;
  esac
done

PASS=0
FAIL=0

green() { printf '\033[0;32m%s\033[0m\n' "$1"; }
red()   { printf '\033[0;31m%s\033[0m\n' "$1"; }
step()  { printf '\n\033[1m== %s\033[0m\n' "$1"; }

ok()   { green "  PASS  $1"; PASS=$((PASS + 1)); }
bad()  { red   "  FAIL  $1"; FAIL=$((FAIL + 1)); }

# Compare two values and report.
expect_eq() {
  local label="$1" actual="$2" expected="$3"
  if [ "$actual" = "$expected" ]; then
    ok "$label (got $actual)"
  else
    bad "$label: expected $expected, got $actual"
  fi
}

cleanup() {
  local status=$?
  if [ "$TEAR_DOWN" = "1" ]; then
    step "Tearing the stack down"
    docker compose down -v >/dev/null 2>&1 || true
  fi
  exit $status
}
trap cleanup EXIT

# ---------------------------------------------------------------- preflight
if [ ! -f .env ]; then
  red "No .env file. Copy .env.example to .env and fill in the values:"
  echo "    cp .env.example .env"
  exit 1
fi

for tool in docker curl python3; do
  command -v "$tool" >/dev/null 2>&1 || { red "$tool is required but not installed"; exit 1; }
done

# jq is not assumed to be installed, so JSON is read with python3, which already
# has to be present. The path is passed as an argument rather than interpolated
# into the program text, so quoting cannot break it.
json() {
  python3 -c '
import json, sys
try:
    value = json.load(sys.stdin)
except Exception:
    sys.exit(0)
for key in sys.argv[1].split("."):
    if value is None:
        break
    if isinstance(value, list):
        try:
            value = value[int(key)]
        except (ValueError, IndexError):
            value = None
    elif isinstance(value, dict):
        value = value.get(key)
    else:
        value = None
if value is not None:
    print(value)
' "$1"
}

# ------------------------------------------------------------------- bring up
if [ "$BRING_UP" = "1" ]; then
  step "Bringing the stack up"
  docker compose up -d --build
fi

step "Waiting for every service to report ready"
wait_ready() {
  local name="$1" url="$2" tries=60
  while [ "$tries" -gt 0 ]; do
    if curl -fsS --max-time 3 "$url" >/dev/null 2>&1; then
      ok "$name is ready"
      return 0
    fi
    tries=$((tries - 1))
    sleep 2
  done
  bad "$name never became ready at $url"
  return 1
}

wait_ready "api-gateway"     "$GATEWAY/actuator/health/readiness"
wait_ready "catalog-service" "$CATALOG_DIRECT/actuator/health/readiness"

# ---------------------------------------------------------------- 1. register
step "1. Register a new user"
EMAIL="smoke-$(date +%s)-$RANDOM@example.com"
PASSWORD='Str0ng!Pass'

REGISTER_BODY=$(printf '{"firstName":"Smoke","lastName":"Test","email":"%s","password":"%s","phoneNumber":"+447700900123"}' "$EMAIL" "$PASSWORD")
REGISTER_HTTP=$(curl -s -o /tmp/smoke-register.json -w '%{http_code}' \
  -X POST "$API/auth/register" -H 'Content-Type: application/json' -d "$REGISTER_BODY")
expect_eq "register returns 201" "$REGISTER_HTTP" "201"

REGISTERED_EMAIL=$(json data.user.email < /tmp/smoke-register.json)
expect_eq "registered email matches" "$REGISTERED_EMAIL" "$(echo "$EMAIL" | tr '[:upper:]' '[:lower:]')"

# -------------------------------------------------------------------- 2. login
step "2. Log in"
LOGIN_BODY=$(printf '{"email":"%s","password":"%s"}' "$EMAIL" "$PASSWORD")
LOGIN_HTTP=$(curl -s -o /tmp/smoke-login.json -w '%{http_code}' \
  -X POST "$API/auth/login" -H 'Content-Type: application/json' -d "$LOGIN_BODY")
expect_eq "login returns 200" "$LOGIN_HTTP" "200"

TOKEN=$(json data.accessToken < /tmp/smoke-login.json)
if [ -n "$TOKEN" ]; then ok "got an access token"; else bad "no access token in the login response"; fi

ME_HTTP=$(curl -s -o /dev/null -w '%{http_code}' "$API/auth/me" -H "Authorization: Bearer $TOKEN")
expect_eq "/auth/me with the token returns 200" "$ME_HTTP" "200"

# ----------------------------------------------------------------- 3. products
step "3. List products through the gateway"
PRODUCTS_HTTP=$(curl -s -o /tmp/smoke-products.json -w '%{http_code}' "$API/products?size=5")
expect_eq "product list returns 200" "$PRODUCTS_HTTP" "200"

PRODUCT_COUNT=$(json totalElements < /tmp/smoke-products.json)
if [ "${PRODUCT_COUNT:-0}" -gt 0 ] 2>/dev/null; then
  ok "catalog has $PRODUCT_COUNT products"
else
  bad "catalog reported no products"
fi

PRODUCT_ID="ELEC001"
STOCK_BEFORE=$(curl -s "$API/products/$PRODUCT_ID" | json stock)
if [ -n "$STOCK_BEFORE" ]; then
  ok "$PRODUCT_ID stock before the order: $STOCK_BEFORE"
else
  bad "could not read the stock for $PRODUCT_ID"
  exit 1
fi

# ------------------------------------------------------------- 4. auth is real
step "4. The gateway rejects an unauthenticated order"
NOAUTH_HTTP=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$API/orders" \
  -H 'Content-Type: application/json' -d '{}')
expect_eq "POST /api/orders with no token returns 401" "$NOAUTH_HTTP" "401"

BADTOKEN_HTTP=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$API/orders" \
  -H 'Authorization: Bearer not-a-real-token' -H 'Content-Type: application/json' -d '{}')
expect_eq "POST /api/orders with a bad token returns 401" "$BADTOKEN_HTTP" "401"

# -------------------------------------------------------------- 5. place order
step "5. Place an order"
QUANTITY=2
IDEMPOTENCY_KEY=$(python3 -c 'import uuid; print(uuid.uuid4())')
ORDER_BODY=$(printf '{"items":[{"productId":"%s","quantity":%d}],
  "shippingAddress":{"firstName":"Smoke","lastName":"Test","address":"1 Main St",
  "city":"London","zipCode":"SW1A 1AA","country":"GB","phoneNumber":"+447700900123"},
  "paymentMethod":{"type":"CARD"},"shippingMethod":"STANDARD"}' "$PRODUCT_ID" "$QUANTITY")

ORDER_HTTP=$(curl -s -o /tmp/smoke-order.json -w '%{http_code}' -X POST "$API/orders" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Idempotency-Key: $IDEMPOTENCY_KEY" \
  -H 'Content-Type: application/json' -d "$ORDER_BODY")
expect_eq "order returns 201" "$ORDER_HTTP" "201"

ORDER_ID=$(json data.id < /tmp/smoke-order.json)
ORDER_TOTAL=$(json data.total < /tmp/smoke-order.json)
if [ -n "$ORDER_ID" ]; then
  ok "order $ORDER_ID placed, total $ORDER_TOTAL"
else
  bad "no order id in the response"
  exit 1
fi

GET_ORDER_HTTP=$(curl -s -o /dev/null -w '%{http_code}' "$API/orders/$ORDER_ID" \
  -H "Authorization: Bearer $TOKEN")
expect_eq "the order can be read back" "$GET_ORDER_HTTP" "200"

# ------------------------------------------------- 6. stock went down via Kafka
step "6. Stock decremented asynchronously"
# This only passes if the outbox row was written with the order, the publisher sent
# it to Kafka, and catalog-service consumed it. It is the whole async path in one
# assertion, so it is given time rather than checked immediately.
EXPECTED_STOCK=$((STOCK_BEFORE - QUANTITY))
STOCK_AFTER="$STOCK_BEFORE"
for _ in $(seq 1 30); do
  STOCK_AFTER=$(curl -s "$API/products/$PRODUCT_ID" | json stock)
  [ "${STOCK_AFTER:-$STOCK_BEFORE}" = "$EXPECTED_STOCK" ] && break
  sleep 1
done
expect_eq "stock went from $STOCK_BEFORE to $EXPECTED_STOCK" "$STOCK_AFTER" "$EXPECTED_STOCK"

# --------------------------------------------------------- 7. duplicate ignored
step "7. Replaying the same Idempotency-Key is ignored"
REPLAY_HTTP=$(curl -s -o /tmp/smoke-replay.json -w '%{http_code}' -X POST "$API/orders" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Idempotency-Key: $IDEMPOTENCY_KEY" \
  -H 'Content-Type: application/json' -d "$ORDER_BODY")
expect_eq "the replay is accepted rather than erroring" "$REPLAY_HTTP" "201"

REPLAY_ORDER_ID=$(json data.id < /tmp/smoke-replay.json)
expect_eq "the replay returns the SAME order, so no duplicate was created" \
  "$REPLAY_ORDER_ID" "$ORDER_ID"

# The decisive check: the retry must not have taken more stock.
sleep 6
STOCK_AFTER_REPLAY=$(curl -s "$API/products/$PRODUCT_ID" | json stock)
expect_eq "stock did NOT move again after the replay" "$STOCK_AFTER_REPLAY" "$EXPECTED_STOCK"

ORDER_COUNT=$(curl -s "$API/orders?page=0&size=50" -H "Authorization: Bearer $TOKEN" | json data.totalCount)
expect_eq "this user has exactly one order" "$ORDER_COUNT" "1"

# ------------------------------------------------------------------- summary
step "Summary"
echo "  passed: $PASS"
echo "  failed: $FAIL"

if [ "$FAIL" -gt 0 ]; then
  red "SMOKE TEST FAILED"
  exit 1
fi
green "SMOKE TEST PASSED"
