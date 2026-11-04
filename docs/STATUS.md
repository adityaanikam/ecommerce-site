# Status

What was built, what was actually run, and what is not done. Anything marked "run"
below has a command and a result behind it, either in this file or in the commit
that introduced it.

Date: 2026-11-04. Branch: `microservices`.

---

## 1. What was built

| Thing | State | Verified how |
| --- | --- | --- |
| api-gateway (routing, CORS, JWT, correlation id) | Done | 29 tests; running stack; CI |
| catalog-service (products, categories, images, stock) | Done | 37 tests; running stack; CI |
| auth-service (register, login, refresh, me) | Done | 52 tests; running stack; CI |
| order-service (orders, outbox, idempotency) | Done | 32 tests; running stack; CI |
| MongoDB per catalog, PostgreSQL per auth and per order | Done | All four running under compose |
| Redis caching with eviction on write | Done | Key observed in Redis with a 600s TTL |
| Kafka in KRaft mode, single node | Done | Event observed on the topic |
| Transactional outbox plus scheduled publisher | Done | Row written with the order, published, marked |
| Idempotency-Key on POST /orders | Done | Retry returned the same order, one row |
| Resilience4j circuit breaker on the catalog call | Done | Observed CLOSED to OPEN with catalog stopped |
| Actuator health, liveness, readiness on all four | Done | All probes 200 |
| Micrometer Prometheus on all four | Done | 5 of 5 Prometheus targets up |
| prometheus.yml and alert.rules.yml | Done | Rules loaded; every expression returns series |
| Correlation id in every log line | Done | One id traced across three services and Kafka |
| Secrets out of the repo, .env plus .env.example | Done | CI greps for the old placeholders |
| scripts/smoke.sh | Done | 20 of 20 passed from a clean slate |
| CI workflow | Done, green | GitHub run 41810558773, all 10 jobs passed |
| Kubernetes manifests | Written, **not deployed** | Schema-validated only, see section 4 |
| Frontend pointed at the gateway | Done | Loaded in a real browser, 130 products rendered |
| README, ARCHITECTURE, AUDIT, STATUS | Done | This file |

---

## 2. Test results

All run on this machine with a real Docker daemon. Integration tests use
Testcontainers against real MongoDB, PostgreSQL and Kafka, not fakes.

| Service | Unit + slice | Integration | Total | Result |
| --- | --- | --- | --- | --- |
| api-gateway | 29 | 0 | 29 | pass |
| catalog-service | 34 | 3 | 37 | pass |
| auth-service | 41 | 11 | 52 | pass |
| order-service | 22 | 10 | 32 | pass |
| **Total** | **126** | **24** | **150** | **pass** |

Each was also built and tested from the **committed tree** rather than the working
directory, via `git archive HEAD` into a temporary folder, as required.

### The four hard cases, specifically

| Claim | Test | Level |
| --- | --- | --- |
| Same Idempotency-Key twice creates one order | `OrderOutboxIT.sameIdempotencyKeyCreatesOneOrder` | Real PostgreSQL |
| | `OrderServiceTest.sameKeyTwiceCreatesOneOrder` | Unit |
| Duplicate OrderPlaced decrements stock once | `OrderPlacedKafkaIT.duplicateEventDecrementsOnlyOnce` | Real Kafka + MongoDB |
| | `OrderPlacedConsumerTest.duplicateEventDecrementsOnce` | Unit |
| Circuit breaker opens when catalog is down | `CatalogClientCircuitBreakerTest.breakerOpensWhenCatalogIsDown` | Real socket, hanging server |
| | `...openBreakerShortCircuits` (no call made, under 250ms) | Real socket |
| Gateway rejects a missing or bad JWT | `GatewayRoutingTest` x6 (missing, garbage, expired, wrong key, non-Bearer, write on a read-only route) | Real HTTP |

All of the above were additionally confirmed against the running compose stack, not
only in tests.

### Docker availability

Docker **was** available and all integration tests ran. Two things had to be fixed
for that to be true, and both are worth knowing:

1. The Docker daemon was not running at session start. Started with `dockerd`.
2. Testcontainers reported "Could not find a valid Docker environment" because
   docker-java requests API version 1.32 and Docker 25+ refuses anything below
   1.40. **The build was green while every integration test silently skipped.**
   Fixed by pinning `docker.api.version` in each pom, so `mvn verify` runs them
   with no extra flags. Override with `-Ddocker.api.version=` on an older daemon.

Point 2 also affected GitHub Actions: the first green CI run had skipped 10 of 10
integration tests in order-service. The fix was pushed and the next run executed
them for real (31.25s against real PostgreSQL and Kafka).

---

## 3. What was run, with results

```
cd backend && mvn clean test                     FAIL (baseline, pre-existing)
  3 errors: SPRING_DATA_MONGODB_URI unresolved, see docs/AUDIT.md section 3.1

services/*/mvn verify                            PASS, 150 tests, 0 skipped
git archive HEAD | build each service            PASS
frontend: npm ci && npm run build                PASS
frontend: npx vitest run                         FAIL (pre-existing, see below)
docker compose up -d                             PASS, 10 containers healthy
scripts/smoke.sh                                 PASS, 20 of 20
gh run view 41810558773                          PASS, 10 of 10 jobs
```

Observed on the running stack:

```
stock before order      50
place order, qty 3      201
stock after             47     (outbox -> Kafka -> consumer)
retry same key          201, same order id
orders in postgres       1
outbox rows              1
stock after retry       47     (unchanged)

catalog stopped:  CLOSED CLOSED CLOSED OPEN OPEN OPEN, all 503, all under 100ms
correlation id:   one id in gateway, order-service and catalog-service logs
Prometheus:       5 of 5 targets up, 10 rules loaded, 0 alerts firing
Redis:            products::ELEC001 present, TTL 600s
PostgreSQL:       password_hash starts $2a$10$, 60 chars
Flyway:           auth 2 migrations applied, order 3 applied
```

---

## 4. Not done, or not verified

### Not verified

| Item | Why |
| --- | --- |
| **Kubernetes manifests were never applied** | No cluster available. `kind` could not be downloaded (egress proxy returned 403) and nothing else was reachable. The manifests validate against the Kubernetes API schemas (21 of 21 documents, nothing the API would silently drop) and the same images and configuration run green under compose, but that is not the same as a successful rollout. Treat them as untested. |
| **The live Render and Vercel deployments** | Both hosts are blocked by this environment's egress proxy (`CONNECT tunnel failed, response 403`). Nothing was changed on `main` and neither platform deploys from this branch, so they should be unaffected, but that is reasoning, not a check. |
| **The multi-stage Dockerfiles, locally** | A `docker build` here cannot reach Maven Central because the sandbox's TLS-intercepting proxy's CA is not in the container trust store. They **are** verified in CI, where all four images build. Locally the stack was run from jars built on the host. |

### Not built

| Item | Note |
| --- | --- |
| Payment processing | `paymentMethod.type` is recorded and nothing is charged. Card details are deliberately not accepted, to stay out of PCI scope. |
| Order status transitions | Orders are created `PENDING` and never move. No cancel, refund, tracking or returns. |
| Most of the frontend's user API | Addresses, wishlists, notifications, preferences, activity log, reviews, questions, coupons, admin inventory. All listed in `docs/AUDIT.md` section 2.2. They have no backend and are not stubbed with fake data. |
| Product writes | The catalog is read-only over HTTP; stock changes only via `OrderPlaced`. No admin create, update or delete. |
| Email | No verification, no password reset delivery. `/auth/forgot-password` and `/auth/reset-password` are not implemented. |
| Google OAuth | `/auth/google` is not implemented. |
| Cart service | The cart stays in the browser's `localStorage`, as before. No backend was needed. |
| Alertmanager | `prometheus.yml` points at one, none runs. Rules evaluate; nothing is delivered. |
| Grafana | Not included. |
| Ingress, HPA, PodDisruptionBudget, NetworkPolicy | Left out of `k8s/` rather than guessed at. |
| Service-to-service auth | `/internal/stock/lookup` is protected only by the gateway not routing `/internal/**`. Anything inside the cluster can call it. mTLS or a mesh is the real answer. |
| Distributed tracing | Correlation ids are propagated and logged, but there is no OpenTelemetry, Zipkin or Jaeger. |

### Known gaps in what *is* built

| Gap | Detail |
| --- | --- |
| **Stock can oversell under concurrency** | Stock is checked before the order is written, but decremented later and asynchronously. Two orders placed in the same second can both pass the check. The decrement itself is atomic and cannot go below zero, so the loser is logged as an oversell for reconciliation rather than silently applied. Closing this properly needs a synchronous reservation at order time. |
| **A crash between claiming an event and applying it loses that decrement** | A deliberate trade: for stock, double-decrementing is worse than missing one. Explained in `docs/ARCHITECTURE.md` section 6. |
| **Access token revocation lags by up to 15 minutes** | The gateway verifies by signature and does not call auth-service per request. Refresh tokens are checked against the database, so revocation lands there immediately. |
| **The catalog still stores prices as `double`** | Changing it would change the JSON the live frontend parses. order-service converts to `BigDecimal` at its boundary, so all order money is exact. |
| **Product images have no volume in Kubernetes** | `/api/images/**` will 404 there. The frontend serves product images from its own `public/` directory, so nothing user-visible depends on it. |
| **Single Kafka broker, single-replica databases** | No fault tolerance in the data layer. Fine for local, not for production. |

---

## 5. Pre-existing failures left alone

### Frontend test suite: still failing

```
cd frontend && npx vitest run
Test Files  6 failed (6)
     Tests  25 failed | 5 passed (30)
```

Two causes, both in the test code and both predating this work:

- `src/test/integration/UserFlow.test.tsx`: `You cannot render a <Router> inside
  another <Router>`. The test wraps a component that already contains a router.
- `src/test/components/ProductCard.test.tsx`: asserts the add-to-cart button is
  disabled when it is not.

The brief said to leave the existing debug and test components alone, so they are
untouched. **CI therefore gates on the frontend build, not on this suite**, and
says so in a comment in the workflow rather than hiding it behind a disabled step.
`npm run build` passes.

### Monolith test: still failing

`backend/src/test/java/com/ecommerce/ImageServingTest.java` still fails for the
reasons in `docs/AUDIT.md` section 3.1. The `backend/` module is left exactly as it
was so the live Render deployment is not disturbed; it is not part of the new CI
matrix, and the services that replace it have their own tests.

### Frontend type errors: 46, pre-existing

`npx tsc --noEmit` reports 46 errors, none of them introduced here. Confirmed by
stashing the changes and re-running: the one error in a file this work touched
(`services/api.ts`) exists identically before the change, shifted only by added
comment lines. Vite does not type-check, so the build passes regardless.

---

## 6. Four bugs found by running things rather than by testing them

Worth recording, because each one passed its tests while being wrong.

1. **Redis cached nothing.** `@Cacheable` sat on a method called from inside the
   same bean, so the Spring proxy was bypassed. Every behavioural test passed.
   Found by looking in Redis and seeing it empty. There is now a test that asserts
   the key exists.

2. **The outbox publisher threw on every tick.** Same self-invocation problem with
   `@Transactional`. The scheduler caught and logged it, so the service looked
   healthy while orders piled up unpublished.

3. **Breach revocation was rolled back.** Reuse of a consumed refresh token revoked
   the user's token family, then threw, and the throw rolled back the transaction
   that had just revoked them. Caught by an integration test that checked the
   rotated token afterwards.

4. **CI was green with every integration test skipped.** Covered in section 2.

The pattern in the first three is the same: Spring applies `@Transactional` and
`@Cacheable` through a proxy, and a call from inside the same bean skips it
silently. Each is now in a separate bean, with a comment saying why.

---

## 7. If this were going further

In the order worth doing:

1. Synchronous stock reservation at order time, to close the oversell gap.
2. Service-to-service authentication, so `/internal/**` is not protected by routing
   alone.
3. A dead letter topic and a replay tool for events that exhaust their retries.
4. OpenTelemetry, so the correlation ids become real traces.
5. Deploy the Kubernetes manifests and find out what is wrong with them.
6. Unify the frontend's two API layers.
