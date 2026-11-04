# Architecture

Why the system is shaped the way it is. Written so every decision here can be
defended, including the ones that are trade-offs rather than wins.

---

## 1. The starting point

One Spring Boot application over one MongoDB, serving products, categories and
images. `docs/AUDIT.md` has the measured detail. The parts that shaped this design:

- it served every route at the root, not under `/api`, because
  `application.properties` quietly beat `application.yml`;
- `stock` existed on the product model but nothing ever decremented it;
- the seeder deleted the whole collection on every boot;
- CORS was declared in five places, four of them hardcoded;
- there was no authentication of any kind, and the frontend had a complete auth
  UI calling endpoints that did not exist.

---

## 2. Service boundaries

Four services. The boundary used is **who owns which data and who is allowed to
change it**, not "what nouns exist".

| Service | Owns | Store |
| --- | --- | --- |
| api-gateway | nothing | none |
| catalog-service | products, categories, stock | MongoDB |
| auth-service | users, credentials, refresh tokens | PostgreSQL |
| order-service | orders, order lines, the outbox | PostgreSQL |

### Why these four and not others

**catalog and orders are split because their write patterns have nothing in
common.** The catalog is read constantly and written rarely, so it wants caching
and read replicas. Orders are written once and read occasionally, and every write
must be transactional and durable. Those two want opposite things from a database,
which is also why one is on MongoDB and the other on PostgreSQL.

**auth is split because its blast radius is different.** It holds password hashes.
Keeping it separate means the service that handles HTML-rendered product data and
the service that holds credentials do not share a process, a dependency tree or a
database connection. It is also the only service with a reason to need stricter
access control around its database.

**The gateway is separate because the alternative is duplicating it.** CORS, JWT
verification and correlation ids have to happen for every request. Doing them in
each service means three copies that drift, which is exactly what the monolith's
five CORS declarations already demonstrated.

### Why not more services

There is no separate inventory-service, payment-service or notification-service.
Stock lives in catalog because stock is an attribute of a product and splitting
them would mean a distributed transaction for every product write, for no benefit
at this size. Payment is not implemented at all rather than stubbed. Splitting
further would add network hops and failure modes to buy nothing.

### Why no shared library

Deliberate, and the one rule applied most strictly. order-service and
catalog-service each define their own copy of the `OrderPlaced` shape, with only
the fields they use and `@JsonIgnoreProperties(ignoreUnknown = true)`.

A shared `common-domain` jar is the single most common way a microservices
codebase turns back into a distributed monolith: every service's release becomes
coupled to every other's, and a field added for one consumer forces all of them to
rebuild. The cost of avoiding it is a few duplicated records, maybe forty lines
across the repo. That is cheap. The `CorrelationIdFilter` is duplicated three times
for the same reason.

The one thing that genuinely is shared is the JWT secret, and that is config, not
code.

---

## 3. Synchronous or asynchronous, and why

Exactly one synchronous service-to-service call exists, and exactly one
asynchronous flow.

### Synchronous: order-service asks catalog-service for prices and stock

```
POST /api/orders ──► order-service ──HTTP──► catalog-service /internal/stock/lookup
```

**Why synchronous.** The order cannot be written without knowing what to charge.
The basket arrives from `localStorage`, where the user can edit the prices, so the
client's numbers cannot be trusted. There is no coherent version of "accept the
order now and find out the price later", so this call blocks and the order fails if
it cannot be made.

**What guards it**, each solving a different failure:

- **Timeouts** (1s connect, 2s read). The JDK's default is no timeout at all, which
  is how one slow dependency becomes every request thread blocked for ever.
- **One retry**, for a single dropped connection. Only one: more retries make a
  struggling dependency worse.
- **A circuit breaker.** Once the catalog is properly down, every order request
  would otherwise sit on the 2 second timeout and the thread pool would fill. Open,
  the breaker fails them instantly. Verified: with catalog stopped, the breaker
  moved from CLOSED to OPEN after four failures, and subsequent calls returned in
  under 250ms without touching the network.

The result is a **503 with `Retry-After`**, not a 500. The request was fine; a
dependency was not, and retrying shortly is the correct advice.

**The call is made outside the transaction.** Holding a database transaction open
across a network call to another service means a pooled connection is held for the
duration of that call. Under load, that is how the pool runs dry.

### Asynchronous: stock decrement

```
order-service ──outbox table──► publisher ──Kafka──► catalog-service ──► stock -= qty
```

**Why asynchronous.** The order has already been accepted and paid for. If the
stock decrement were synchronous and failed, there would be two bad options:
refuse an order the customer has already been charged for, or leave the order
written and the stock wrong with no record that it needs fixing. An event decouples
them: the order is durable the moment it commits, and the decrement happens
reliably but not necessarily immediately.

**Why catalog owns the decrement rather than order-service writing to it.**
Database per service. Nothing outside catalog-service may write to products.

---

## 4. Database per service

Three separate databases on three separate instances: MongoDB for catalog,
PostgreSQL for auth, a different PostgreSQL for orders. Not three schemas on one
server.

**Why it matters more than it looks.** A shared database is a shared, implicit,
unversioned API. Any service can read any table, so any schema change can break a
service nobody thought about, and nothing in the code says so. Separate instances
mean a boundary that cannot be crossed by accident.

**What it costs**, honestly:

- **No cross-service joins.** "Show me this order with its product details" is two
  calls or two round trips, not a join. This is why `order_items` copies
  `product_name` and `unit_price` at the time of purchase.
- **No cross-service transactions.** Hence the outbox.
- **More to operate.** Three databases to back up, monitor and upgrade.

The copying is a feature here, not a workaround. An order is a record of what was
bought at the time. Renaming a product in the catalog must not rewrite the history
of an order already placed, and a price change must not retroactively change what
somebody was charged. A join would do exactly that.

---

## 5. The transactional outbox

The problem: when an order is placed, two things must happen together, and they
live in two systems that cannot commit together.

```java
orderRepository.save(order);
kafkaTemplate.send("order-placed", event);   // wrong
```

Crash between those lines and the order exists but stock is never decremented. Swap
them and a rolled-back transaction leaves an event claiming an order that does not
exist. There is no ordering of those two lines that is correct.

**The fix: make it one write.**

```java
@Transactional
public OrderView createOrder(...) {
    orders.save(order);
    outbox.save(orderPlacedEvent);      // same transaction, same database
    idempotencyKeys.save(record);
}
```

A separate scheduled publisher polls `outbox_events`, sends to Kafka, and marks the
row published. The order and the intent to publish are now atomic, because they are
one commit to one database.

### The details that make it work

**The event id is the outbox row's primary key**, generated inside the transaction.
A row republished after a crash therefore carries the *same* event id, which is what
lets the consumer recognise it as a duplicate. Generating the id at publish time
would make every retry look like a brand new event and defeat deduplication.

**Claiming uses `FOR UPDATE SKIP LOCKED`**, so two instances of order-service take
different rows instead of both publishing the same one. Without it, scaling out
would turn at-least-once into reliably-twice.

**Publishing is scheduled, not triggered by the write.** A broker that was down when
the order was placed costs a delay, nothing more.

**It is at-least-once, on purpose.** A crash between Kafka acknowledging and the
transaction committing leaves the row unpublished, and it is sent again. Making it
exactly-once would need a distributed transaction across PostgreSQL and Kafka.
At-least-once plus a deduplicating consumer is the standard trade and far easier to
reason about.

**The backlog is the thing to monitor.** This is the failure mode the pattern can
hide: orders keep succeeding while events quietly stop publishing. Nothing errors,
no customer complains, and stock silently stops moving until somebody counts the
warehouse. Hence the `order_outbox_pending_events` gauge and an alert on it.

---

## 6. Idempotency

Two different problems, solved in two different places.

### The client retries POST /orders

A timeout does not tell the client whether the order was created. Retrying could
create a second one.

The client sends an `Idempotency-Key`. The table has a **primary key on
`(user_id, idempotency_key)`**.

```
1. Already used this key? Return that order, do nothing else.
2. Otherwise create the order, and store the key in the same transaction.
3. If the insert collides, two copies raced: re-read and return the first one's order.
```

Step 3 is the one that actually matters. A lookup-then-insert is a race: two
requests arriving together both see "not used" and both proceed. The **constraint**
is what guarantees one order; the lookup is just a fast path that avoids the
exception in the common case.

The key is **scoped to the user**, so two clients picking the same key cannot
collide, and nobody can read a stranger's order by guessing one.

A key reused with a *different* basket is rejected with 422 rather than silently
answered with the first order. That is a client bug, and hiding it would make it
permanent. This is why the record stores a fingerprint of the request.

### The same event is delivered twice

Kafka is at-least-once, and the outbox is too, so the consumer must assume
duplicates. catalog-service keeps an **inbox**: a `processed_events` collection
keyed on the event id.

```
1. Insert the event id. Duplicate key means already handled: return.
2. Otherwise decrement.
```

**Claim first, then act, and that ordering is a deliberate trade.** A crash between
the two loses that decrement rather than applying it twice. For stock,
double-decrementing is the worse failure: it sells inventory that does not exist and
the customer finds out after paying. A missed decrement shows up as a stock count
that is too high and is caught by reconciliation. Truly atomic would need the
decrement and the inbox write in one transaction, which MongoDB supports only on a
replica set, not on the single node this runs on.

---

## 7. Authentication

auth-service signs, the gateway verifies, and no other service does either.

**Verification at the gateway, not per service.** The gateway checks the signature
and expiry, then sets `X-User-Id`, `X-User-Email` and `X-User-Roles` from the
verified claims. Downstream services simply trust those headers.

**Which only works because the gateway strips them first.** Any copy the client
sent is removed before the gateway sets its own, on public routes as well as
authenticated ones. Without that, anyone could send `X-User-Id: <somebody else>`
and be them. This is tested explicitly.

**Access tokens are not checked against the database.** That is what makes the
gateway fast and stateless. The cost is that a revoked user keeps working until
their access token expires, which is why they live 15 minutes. The refresh token
*is* checked against the database every time, so revocation lands there.

**Refresh tokens rotate.** Using one revokes it and issues a new one, so a stolen
token is good at most once. If an already-consumed token is presented, that is
evidence of theft, and the user's whole token family is revoked. That revocation
runs in its own transaction, because the exception thrown immediately afterwards
rolls the enclosing one back and would otherwise undo it.

**Only hashes are stored.** Passwords with BCrypt, which is deliberately slow to
make an offline attack on a leaked table expensive. Refresh tokens with plain
SHA-256, which is correct *because* the token is already 256 bits of uniform
randomness: there is nothing to brute force, and BCrypt's slowness on a path that
runs at every refresh would be a cost with no benefit.

**Login does not reveal which emails are registered.** An unknown email and a wrong
password return the same message, and an unknown email still runs a BCrypt hash so
the two take about the same time. The shared message is pointless without the
timing fix.

---

## 8. Caching

Redis, in catalog-service only, with one invalidation rule:

> A product is cached under its own id and evicted the moment that product is
> written. The category tree is cached under one key.

**Only single-product reads are cached**, not list queries. A `Page` is awkward to
serialise, and every list carries a different filter and sort, so the hit rate would
be poor and the invalidation rule would stop being obvious. A cache whose
invalidation rule cannot be stated in one sentence is a bug waiting to happen.

**Eviction sits next to the write.** `StockService` is the only thing that mutates a
product, and it evicts in the same method. TTLs (10 and 30 minutes) are a backstop,
not the mechanism.

**An unreachable Redis degrades, it does not fail.** A `CacheErrorHandler` logs and
falls through to MongoDB. A cache is an optimisation; treating it as a dependency
means a cache outage becomes a site outage.

**A note on how easy this is to get wrong:** the first version of this had
`@Cacheable` on a method called from inside the same bean. Spring applies it through
a proxy, so a self-invocation skips it entirely. Every behavioural test passed and
Redis stayed empty. Only looking in Redis found it, which is why there is now a test
that asserts the key exists.

---

## 9. Failure handling

| What fails | What happens | Why |
| --- | --- | --- |
| Redis down | catalog serves from MongoDB, slower | A cache is not a dependency |
| catalog-service down | orders rejected 503 + Retry-After; breaker opens | Cannot price an order; fail fast rather than queue |
| Kafka down | orders still succeed; events queue in the outbox | The order is durable; publishing catches up |
| order-service dies mid-publish | event republished with the same id, consumer ignores it | At-least-once plus an inbox |
| Two order-service instances | each claims different rows (`SKIP LOCKED`) | Scaling out must not double-publish |
| Two catalog-service instances | partitions shared; inbox guards a rebalance replay | Consumer group plus deduplication |
| auth-service down | existing tokens keep working until expiry; no new logins | Verification is stateless, by design |
| MongoDB down | catalog readiness fails, removed from endpoints | Readiness includes dependencies, liveness does not |
| Client retries an order | the same order comes back | Idempotency key plus a primary key |
| Stock insufficient at decrement time | logged as an oversell for reconciliation | The order was already accepted; cannot be undone here |

That last row is a genuine gap. Stock is checked before the order is written, but
the decrement happens later and asynchronously, so two orders placed within the
same second can both pass the check and oversell. Closing it properly means
reserving stock synchronously at order time, which is a larger change. It is
flagged rather than hidden.

---

## 10. Trade-offs taken knowingly

**Microservices are the wrong default for this application.** 130 products and no
traffic do not need four services, three databases and a broker. A modular monolith
would be cheaper to run and easier to change. The split is justified here as an
exercise in the patterns, and because the original code had already grown the
problems the split addresses, but nobody should infer this is the right shape for
every shop.

**Stock is eventually consistent.** A product page can show stock that is a second
or two stale. For a shop, that is nearly always fine, and the alternative is a
synchronous decrement that couples order placement to catalog availability.

**One Kafka broker and single-replica databases.** No fault tolerance anywhere in
the data layer. Fine for a demo, and called out in `k8s/README.md`.

**Prices use `BigDecimal` and `NUMERIC(12,2)`; the catalog still uses `double`.**
The monolith stored prices as a Java `double`, which cannot represent 0.1 exactly.
order-service converts to `BigDecimal` at its boundary and all order money is exact.
Fixing the catalog's type would change its JSON output and break the live frontend,
so it is left as it is and the conversion happens where the money matters.

**`/internal/stock/lookup` is protected only by not being routed.** The gateway
does not route `/internal/**`, so it is unreachable from outside. Inside the
cluster, anything can call it. Proper service-to-service authentication, mTLS or a
mesh, is the real answer and is not implemented.

**The frontend is largely unchanged.** It has two parallel API layers, a rich one
that no page imports and a simpler one that the pages use, plus three hooks calling
a hardcoded `localhost:8080`. Only what was needed to work against the gateway was
changed. Unifying them is a frontend refactor, not part of this.
