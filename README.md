# ecommerce-site

A small e-commerce system, split from a single Spring Boot application into four
services behind an API gateway, with a React frontend.

- **Audit of the original code**, measured rather than assumed: [`docs/AUDIT.md`](docs/AUDIT.md)
- **Why it is shaped this way**: [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)
- **What works, what was run, what is not done**: [`docs/STATUS.md`](docs/STATUS.md)

---

## Architecture

```
                         browser (React + Vite)
                                  │
                                  │  https, one base URL: VITE_API_BASE_URL
                                  ▼
                     ┌────────────────────────────┐
                     │        api-gateway         │   :8080
                     │  Spring Cloud Gateway      │
                     │                            │
                     │  • CORS, from one env var  │
                     │  • verifies the JWT        │
                     │  • sets X-User-Id / Roles  │
                     │    (and strips any the     │
                     │     client tried to send)  │
                     │  • stamps X-Correlation-Id │
                     │  • strips the /api prefix  │
                     └───┬──────────┬─────────┬───┘
            /api/products│          │         │/api/orders
          /api/categories│  /api/auth│        │
             /api/images │  /api/users        │
                         ▼          ▼         ▼
          ┌──────────────────┐ ┌──────────┐ ┌──────────────────┐
          │ catalog-service  │ │   auth   │ │  order-service   │
          │      :8081       │ │  :8082   │ │      :8083       │
          │                  │ │          │ │                  │
          │ products         │ │ users    │ │ orders           │
          │ categories       │ │ roles    │ │ order_items      │
          │ images           │ │ refresh  │ │ outbox_events    │
          │ stock (owner)    │ │  tokens  │ │ idempotency_keys │
          └────┬────────┬────┘ └────┬─────┘ └────┬────────┬────┘
               │        │           │            │        │
               ▼        ▼           ▼            ▼        ▼
        ┌──────────┐ ┌───────┐ ┌──────────┐ ┌──────────┐ │
        │ MongoDB  │ │ Redis │ │ Postgres │ │ Postgres │ │
        │ catalog  │ │ cache │ │   auth   │ │  orders  │ │
        │  :27017  │ │ :6379 │ │  :5433   │ │  :5434   │ │
        └──────────┘ └───────┘ └──────────┘ └──────────┘ │
                                                          │
          ▲                                               │
          │                                               ▼
          │            ┌─────────────────────┐   ┌──────────────────┐
          │            │       Kafka         │   │ outbox publisher │
          └────────────┤   KRaft, 1 node     │◄──┤  every 1s, from  │
   consumes            │   topic:            │   │  the outbox table│
   OrderPlaced,        │   order-placed      │   └──────────────────┘
   decrements stock    │       :9092         │
   once per event id   └─────────────────────┘

                     ┌─────────────────────┐
                     │     Prometheus      │  :9090
                     │  scrapes all four   │
                     └─────────────────────┘
```

Two paths matter:

**Placing an order (synchronous, then asynchronous).** order-service calls
catalog-service over HTTP for the real price and stock, because the basket comes
from the browser and its prices cannot be trusted. That call has a timeout, a
retry and a circuit breaker. The order, its lines, an `OrderPlaced` event and the
idempotency record then commit in **one transaction**. A scheduled publisher sends
the event to Kafka, and catalog-service decrements stock when it arrives.

**Nothing reads another service's database.** Ever.

---

## Running it locally

You need Docker and Docker Compose. Nothing else: the services build inside Docker.

```bash
cp .env.example .env
```

Fill in `.env`. Generate the secrets rather than inventing them:

```bash
printf 'JWT_SECRET=%s\n' "$(openssl rand -hex 32)" >> .env
```

Then bring everything up:

```bash
docker compose up -d --build
```

The first build takes a few minutes. When it settles, all ten containers report
healthy:

```bash
docker compose ps
```

Check it works end to end. This registers a user, logs in, places an order, waits
for stock to drop, and replays the request to prove no duplicate is created:

```bash
./scripts/smoke.sh --no-up
```

Tear it down, volumes and all:

```bash
docker compose down -v
```

### The frontend

```bash
cd frontend && npm ci
```

```bash
printf 'VITE_API_BASE_URL=http://localhost:8080/api\n' > frontend/.env.local
```

```bash
cd frontend && npm run dev
```

It talks to the gateway and nothing else. `frontend/.env.example` has the details.

---

## Services and ports

| Service | Port | Purpose | Store |
| --- | --- | --- | --- |
| api-gateway | 8080 | The only thing a browser talks to | none |
| catalog-service | 8081 | Products, categories, images, stock | MongoDB |
| auth-service | 8082 | Register, login, refresh, current user | PostgreSQL |
| order-service | 8083 | Orders, outbox, idempotency | PostgreSQL |
| catalog-mongo | 27017 | catalog-service only | |
| redis | 6379 | catalog-service cache only | |
| auth-postgres | 5433 | auth-service only | |
| order-postgres | 5434 | order-service only | |
| kafka | 29092 | `order-placed` topic (9092 inside the network) | |
| prometheus | 9090 | Scrapes all four services | |

Every service exposes the same endpoints:

| Path | What |
| --- | --- |
| `/actuator/health` | Overall health |
| `/actuator/health/liveness` | Is it wedged? Excludes dependencies |
| `/actuator/health/readiness` | Should it get traffic? Includes dependencies |
| `/actuator/prometheus` | Metrics |

order-service also has `/actuator/circuitbreakers`.

### The API, through the gateway

| Method | Path | Auth |
| --- | --- | --- |
| POST | `/api/auth/register` | public |
| POST | `/api/auth/login` | public |
| POST | `/api/auth/refresh` | public |
| GET | `/api/auth/me` | required |
| POST | `/api/auth/logout` | required |
| GET | `/api/users/me` | required |
| GET | `/api/products` | public |
| GET | `/api/products/{id}` | public |
| GET | `/api/products/featured`, `/new`, `/on-sale`, `/categories` | public |
| GET | `/api/categories`, `/api/categories/{slug}`, `/api/categories/stats` | public |
| GET | `/api/images/product/{category}/{name}/{file}` | public |
| POST | `/api/orders` | required, accepts `Idempotency-Key` |
| GET | `/api/orders`, `/api/orders/user`, `/api/orders/{id}` | required |

Reads of the catalog are public; writes to anything are not.

---

## Environment variables

Everything secret comes from `.env`, which is git-ignored. `.env.example` is the
committed template. No service has a hardcoded credential, and the gateway and
auth-service **refuse to start** without `JWT_SECRET` rather than falling back to
a default.

| Variable | Used by | Notes |
| --- | --- | --- |
| `JWT_SECRET` | api-gateway, auth-service | At least 32 bytes. auth signs, the gateway verifies, so they must match |
| `GATEWAY_ALLOWED_ORIGINS` | api-gateway | Comma separated browser origins. No wildcard default |
| `CATALOG_MONGO_USERNAME` / `_PASSWORD` | catalog-mongo, catalog-service | |
| `REDIS_PASSWORD` | redis, catalog-service | |
| `AUTH_POSTGRES_DB` / `_USER` / `_PASSWORD` | auth-postgres, auth-service | |
| `ORDER_POSTGRES_DB` / `_USER` / `_PASSWORD` | order-postgres, order-service | |

Frontend (`frontend/.env.local`):

| Variable | Notes |
| --- | --- |
| `VITE_API_BASE_URL` | The gateway, including `/api`. Defaults to `http://localhost:8080/api` |
| `VITE_IMAGE_BASE_URL` | Leave empty: product images are served from the app's own `public/` |

Other variables with working defaults, so they only need setting to override:
`SERVER_PORT`, `CATALOG_SERVICE_URL`, `AUTH_SERVICE_URL`, `ORDER_SERVICE_URL`,
`KAFKA_BOOTSTRAP_SERVERS`, `CATALOG_CONNECT_TIMEOUT_MS`, `CATALOG_READ_TIMEOUT_MS`,
`OUTBOX_POLL_INTERVAL_MS`, `JWT_ACCESS_TOKEN_TTL`, `JWT_REFRESH_TOKEN_TTL`,
`CATALOG_SEED_ENABLED`, `CATALOG_IMAGES_BASE_PATH`.

---

## Building and testing

Each service is a standalone Maven project with no parent aggregator, so it builds
on its own:

```bash
cd services/catalog-service && mvn verify
```

`verify` rather than `test`, because the integration tests are bound to failsafe
and they are the ones that prove the interesting parts. They use Testcontainers,
so a Docker daemon is needed; if there is none, they skip with a clear message
rather than failing.

> On a Docker daemon older than 25, pass `-Ddocker.api.version=1.41`. The version
> is pinned in the poms because docker-java otherwise asks for API 1.32, which
> Docker 25+ rejects, and the integration tests then **skip silently** while the
> build stays green.

Everything at once:

```bash
for s in api-gateway catalog-service auth-service order-service; do (cd "services/$s" && mvn -q verify) || break; done
```

Frontend:

```bash
cd frontend && npm ci && npm run build
```

The existing `vitest` suite fails for reasons that predate this work and is not a
CI gate. See `docs/STATUS.md` section 5.

---

## Kubernetes

Manifests are in [`k8s/`](k8s/). They have **not been deployed**: no cluster was
available. Read [`k8s/README.md`](k8s/README.md) before trusting them.

---

## Repository layout

```
services/
  api-gateway/        Spring Cloud Gateway
  catalog-service/    products, categories, images, stock     (MongoDB, Redis, Kafka consumer)
  auth-service/       users and tokens                        (PostgreSQL, Flyway)
  order-service/      orders and the outbox                   (PostgreSQL, Flyway, Kafka producer)
backend/              the original monolith, untouched, still what Render deploys from main
frontend/             React + TypeScript + Vite
k8s/                  Kubernetes manifests (untested)
prometheus/           scrape config and alert rules
scripts/smoke.sh      end-to-end check against a running stack
docs/                 AUDIT, ARCHITECTURE, STATUS
docker-compose.yml    the whole stack
```

`backend/` is deliberately left alone. Render deploys it from `main`, and this
branch does not touch `main`.
