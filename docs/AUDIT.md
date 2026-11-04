# Audit of the existing system

Date: 2026-11-04. Branch: `microservices`, cut from `main` at `5f66c03`.

Everything in this file was checked by reading the code and by running it. Where a
claim comes from a command, the command and its result are shown.

---

## 1. Backend

### 1.1 Stack and versions

| Item | Value | Source |
| --- | --- | --- |
| Spring Boot | **3.2.0** | `backend/pom.xml` parent |
| Java release target | **17** | `<java.version>17</java.version>` |
| JDK available in this environment | 21.0.12.1 (no JDK 17 installed) | `java -version` |
| Maven | 3.9.11 | `mvn -version` |
| Group / artifact | `com.ecommerce` / `ecommerce-backend` 1.0.0 | `pom.xml` |
| Database | MongoDB only | `spring-boot-starter-data-mongodb` |
| Starters | web, data-mongodb, validation, actuator | `pom.xml` |
| Extras | Lombok 1.18.30, MapStruct 1.5.5.Final | `pom.xml` |

There is **no** security starter, **no** JWT library, **no** Redis client and **no**
Prometheus registry on the classpath, even though several YAML files configure
`spring.redis`, `spring.session.store-type: redis` and a `jwt.secret`. Those blocks
are inert: nothing reads them.

### 1.2 Source inventory

```
com.ecommerce
  SimpleEcommerceApplication      @SpringBootApplication + @RestController (serves /api/categories itself)
  config/
    DataInitializer               CommandLineRunner: WIPES and reseeds products on every boot
    ElectronicsProducts           hardcoded seed data (fallback)
    FashionProducts               hardcoded seed data (fallback)
    FashionProductsExtra          hardcoded seed data (fallback)
    HomeAndGardenProducts         hardcoded seed data (fallback)
    SportsProducts                hardcoded seed data (fallback)
    ProductData                   seed helper
    WebConfig                     CORS + static /products/** resource handler
  controller/
    ProductController             @RequestMapping("/products")
    CategoryController            @RequestMapping("/categories")  (hardcoded category list)
    ImageController               @RequestMapping("/images")
    HealthController              /, /health, /test, /debug
  model/Product                   @Document("products")
  repository/ProductRepository    MongoRepository
  service/ProductService          query dispatch
  validation/                     6 constraints + 6 validators (Email, Password, PhoneNumber,
                                  Price, File, ImageUrl) - all unused by any controller today
```

### 1.3 `Product` model

Fields: `id, name, brand, price, discountPrice, category, subcategory, images,
description, stock, rating, reviewCount, specs, features, slug, isNew, isFeatured,
isOnSale, color, size, material, createdAt, updatedAt`.

**`stock` already exists** (`private int stock`), so no new field is needed for the
catalog service, only the plumbing to decrement it.

The class carries `@Data @Builder @AllArgsConstructor` **and** a full set of
hand-written getters and setters, with the comment "Lombok not working properly".
That duplication is harmless but it is dead weight, and the hand-written
`isNew()`/`setNew()`/`setFeatured()`/`setOnSale()` pairs are why the JSON serialises
those three flags as `new`, `featured`, `onSale` rather than `isNew`, `isFeatured`,
`isOnSale`. Confirmed from a live response (see 1.6).

### 1.4 `ProductService`

A single `findProducts` method with an if-else cascade. Behaviour worth preserving
exactly, because it is quietly surprising:

- `search` wins over everything else, and searches **name only**.
- `category` + `subcategory` -> both.
- `category` alone -> category.
- `subcategory` alone -> subcategory.
- `minPrice` + `maxPrice` -> price range, but **only when no category, subcategory or
  search was given**. A price filter combined with a category is silently ignored.
- otherwise `findAll`.

`getAllCategories()` loads **every** product into memory to build a category ->
subcategory map. At 130 products that is fine; it does not scale.

### 1.5 `WebConfig` (the CORS logic to be moved to the gateway)

```java
@Value("${app.cors.allowed-origins:http://localhost:3000}") String[] allowedOrigins;
registry.addMapping("/**")
    .allowedOrigins(allowedOrigins)
    .allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
    .allowedHeaders("*")
    .allowCredentials(false)
    .maxAge(3600);
```

plus a static handler mapping `/products/**` to `file:${app.images.base-path}/`.

CORS is also declared a **second** time, inconsistently, as `@CrossOrigin` on three
controllers:

| Class | `@CrossOrigin` |
| --- | --- |
| `ProductController` | explicit list: localhost:3000 + the Vercel host, `allowCredentials="false"` |
| `CategoryController` | `origins = "*"` |
| `ImageController` | explicit list: localhost:3000 + the Vercel host |
| `HealthController` | explicit list: localhost:3000 + the Vercel host |

So the allowed origins are hardcoded in four places and configurable in one. This is
exactly the duplication the gateway is meant to collapse.

### 1.6 What the backend actually serves (measured, not inferred)

The config files disagree about the servlet context path: `application.yml`,
`application-prod.yml` and `application-docker.yml` all say `/api`, while
`application.properties` says `/`. Both sit in the same location, so I ran it to find
out which wins.

```
docker run -d --name audit-mongo -p 27017:27017 mongo:7.0
SPRING_DATA_MONGODB_URI=mongodb://localhost:27017/ecommerce \
SPRING_DATA_MONGODB_DATABASE=ecommerce \
java -jar backend/target/ecommerce-backend-1.0.0.jar
```

Boot succeeded and the seeder reported `Electronics: 50, Fashion: 30, Home & Garden:
25, Sports: 25` (130 products). Probing every candidate route:

| Status | Path |
| --- | --- |
| 200 | `/` |
| 200 | `/health` |
| 200 | `/test` |
| 200 | `/debug` |
| 200 | `/products` |
| 200 | `/products/featured` |
| 200 | `/products/new` |
| 200 | `/products/on-sale` |
| 200 | `/products/categories` |
| 200 | `/products/verify` |
| 200 | `/categories` |
| 200 | `/categories/stats` |
| 200 | `/categories/electronics` |
| 200 | `/images/test` |
| 200 | `/api/categories` |
| 200 | `/actuator/health` |
| 404 | `/api/products`, `/api/health`, `/api/`, `/api/images/test`, `/api/actuator/health` |
| 404 | `/actuator/prometheus`, `/actuator/info` |
| 404 | `/products/Electronics/iPhone%2013/1.webp` |

**`application.properties` wins: the real context path is `/`, not `/api`.** Routes
live at the root. The single exception is `/api/categories`, which answers only
because `SimpleEcommerceApplication` hardcodes `@GetMapping("/api/categories")` on
the application class itself. That explains the commit
`22e8605 Fix double /api prefix causing 404s on products and images endpoints`.

Two further facts from the same run:

- `/actuator/prometheus` is **404**. `micrometer-registry-prometheus` is not a
  dependency, so `prometheus/prometheus.yml` scrapes a path that does not exist and
  has never collected a single metric from this application.
- A sample product came back as
  `{"id":"SPORT005","stock":60,"images":["https://placehold.co/800x800/6366f1/ffffff"], ...}`
  with keys `featured`, `new`, `onSale` (not `isFeatured`/`isNew`/`isOnSale`), and the
  seeded image URLs in the committed `ecommerce.products.json` are **placeholder**
  URLs, not the GitHub raw URLs the `DataInitializer` log messages claim. The
  frontend compensates by regenerating image paths locally
  (`frontend/src/config/index.ts: getProductImageUrl`).

The static `/products/**` image handler returns 404 because `app.images.base-path`
defaults to `./Products` relative to the process working directory, and
`ImageController` hardcodes `/app/Products/` (a container path) regardless of profile.

### 1.6a The committed seed JSON has never once loaded

A correction to anything that assumes `ecommerce.products.json` is the data source.
It is not, and never has been. From the boot log of the run above:

```
Loading products from JSON file with GitHub image URLs...
Loading products from: /home/user/ecommerce-site/ecommerce.products.json
Failed to load products from JSON, falling back to hardcoded classes:
  Unrecognized field "_id" (class com.ecommerce.model.Product), not marked as ignorable
Added 50 electronics products
Added 20 fashion products
Added 10 extra fashion products
Added 25 home & garden products
Added 25 sports products
Successfully seeded 130 total products!
```

`DataInitializer` catches the failure and silently falls back to the hardcoded
`ElectronicsProducts`, `FashionProducts`, `FashionProductsExtra`,
`HomeAndGardenProducts` and `SportsProducts` classes. Those are what production has
always actually served, which is why the seeded images are `placehold.co`
placeholders rather than the GitHub raw URLs the JSON contains and the log claims.

The JSON fails because it is a mongoexport dump: its keys are `_id`, `isFeatured`,
`isNew`, `isOnSale` and `_class`, none of which match the model's properties, and
the `ObjectMapper` is left at its default of rejecting unknown fields. So both
`ecommerce.products.json` (150 KB) and `ecommerce.products.updated.json` (116 KB)
are dead weight at the repository root, and `ProductData.java` (241 lines) is dead
code that nothing calls at all.

**Consequence for the migration:** catalog-service carries over the five hardcoded
seed classes, which is what preserves current behaviour, and drops the JSON path
entirely rather than carrying over a code path that has only ever thrown.

### 1.7 `DataInitializer` is destructive

```java
if (productRepository.count() == 0) { seedProducts(); }
else { productRepository.deleteAll(); seedProducts(); }   // <-- every boot
```

Every start deletes the whole `products` collection and reseeds it, then randomly
re-rolls `featured` (20%), `isNew`, and a 30-70% discount on a random 30% of rows.
So prices, badges and any stock change do not survive a restart. This is a real
problem for a catalog that is meant to own stock: an `OrderPlaced` event that
decrements stock would be undone by the next deploy. The catalog service must seed
**only when empty**.

### 1.8 Live deployment

Not verifiable from this environment: the agent proxy refuses both hosts.

```
curl https://ecommerce-backend-51pg.onrender.com/   -> curl: (56) CONNECT tunnel failed, response 403
curl https://ecommerce-site-five-phi.vercel.app/    -> curl: (56) CONNECT tunnel failed, response 403
```

Render and Vercel both build from `main`, which this branch does not touch, so the
live deployment is unaffected by this work.

---

## 2. Frontend API inventory

The frontend has **two parallel API layers**, which matters a lot for scoping.

- `src/services/*.ts` (`productService`, `categoryService`) - **actually wired into
  pages**, via `src/lib/axios.ts`.
- `src/services/api/*.ts` (`ProductService`, `OrderService`, `AuthService`,
  `UserService`, `CartService`) - a much richer layer that **no page imports**. Only
  `AuthService` is reachable from the UI, through `hooks/useAuth` ->
  `components/auth/*`.
- Three hooks (`useWishlist`, `useReviews`, `useInventory`) bypass both layers and
  call raw `fetch` against a **hardcoded** `http://localhost:8080/api`.

Base URL plumbing: `VITE_API_BASE_URL` (default `http://localhost:8080/api`) in both
`src/config/api.ts` and `src/config/index.ts`; `VITE_IMAGE_BASE_URL` for images.
Note the default already carries `/api`, which the backend does not serve (see 1.6),
so local development against the default is broken today.

### 2.1 Reachable from the UI

| Call | Endpoint used | Served today? |
| --- | --- | --- |
| `productService.getProducts` | `GET {base}/products?...` | **Yes** (at `/products`, not `/api/products`) |
| `productService.getProductById` | `GET {base}/products/{id}` | **Yes** |
| `productService.getCategories` | `GET {base}/categories` | **Yes** (hardcoded list, empty subcategories) |
| `categoryService.getCategoryBySlug` | `GET {base}/categories/{slug}` | **Yes** (echoes the slug back, fabricated) |
| `categoryService.getCategoryStats` | `GET {base}/categories/stats` | **Yes** (hardcoded `4` / `130`) |
| `AuthService.login` | `POST {base}/auth/login` | **No backend** |
| `AuthService.register` | `POST {base}/auth/register` | **No backend** |
| `AuthService.refreshToken` | `POST {base}/auth/refresh` | **No backend** |
| `AuthService.getCurrentUser` | `GET {base}/auth/me` | **No backend** |
| `AuthService.logout` | `POST {base}/auth/logout` | **No backend** |
| `AuthService.changePassword` | `POST {base}/auth/change-password` | **No backend** |
| `AuthService.forgotPassword` | `POST {base}/auth/forgot-password` | **No backend** |
| `AuthService.resetPassword` | `POST {base}/auth/reset-password` | **No backend** |
| `AuthService.verifyEmail` | `POST {base}/auth/verify-email` | **No backend** |
| `AuthService.resendVerification` | `POST {base}/auth/resend-verification` | **No backend** |
| `AuthService.googleAuth` | `POST {base}/auth/google` | **No backend** |
| `useWishlist` | `fetch http://localhost:8080/api/users/wishlists...` | **No backend** |
| `useReviews` | `fetch .../api/products/{id}/reviews`, `/questions` | **No backend** |
| `useInventory` | `fetch .../api/inventory`, `/api/admin/inventory/*` | **No backend** |

### 2.2 Defined but not reachable from any page (`src/services/api/`)

All of these have **no backend** today. Listing them so the scope decision is explicit.

- `/products`: `POST`, `PUT /{id}`, `DELETE /{id}`, `PUT /bulk`, `/search`, `/featured`,
  `/{id}/related`, `/{id}/reviews` (GET+POST), `/reviews/{id}` (PUT+DELETE), `/brands`,
  `/tags`, `/upload-image`, `/images`, `/{id}/analytics`
- `/categories/{id}/products`
- `/orders`: `POST`, `GET`, `/user`, `/{id}` (GET+PUT), `/{id}/cancel`, `/{id}/return`,
  `/{id}/tracking`, `/{id}/invoice`, `/{id}/payment`, `/{id}/refund`,
  `/{id}/status-history`, `/{id}/notes` (GET+POST), `/analytics`, `/statistics`,
  `/bulk-update`, `/export`
- `/users`: `/me`, `/profile`, `/avatar`, `/addresses` (+`/{id}`, `/{id}/default`),
  `/wishlist` (+`/{productId}`), `/notification-settings`, `/preferences`,
  `/activity-log`, `/statistics`, `/account`, `/export-data`, `/notifications`
  (+`/{id}/read`, `/read-all`, `/{id}`), `/reviews`
- `/cart`: `GET`, `/items` (POST), `/items/{id}` (PUT+DELETE), `DELETE /cart`,
  `/items/{id}/move-to-wishlist`, `/summary`, `/validate`, `/coupon`

### 2.3 The cart and order payload

The cart is **client side only**, in `localStorage` under `ecommerce-cart`
(`contexts/CartContext.tsx`). There is no `/cart` backend and none is needed.

```ts
interface CartItem { productId, quantity, name, price, image, stock }
```

`CheckoutPage.tsx` has a `handleSubmit` that does **not** call any API; no page calls
`OrderService.createOrder`. `CreateOrderRequest` (in `services/api/orderService.ts`)
carries `shippingAddress`, optional `billingAddress`, `paymentMethod`,
`shippingMethod`, optional `couponCode`, `notes` - and **no items**, because that
layer assumed a server-side cart.

**Consequence for order-service:** the payload it must accept is the client cart
items (`productId` + `quantity`) plus the `CreateOrderRequest` address/payment
fields. Prices in the cart are client-supplied and must not be trusted, which is
exactly why order-service re-reads price and stock from catalog-service before saving.

### 2.4 Auth token storage is inconsistent

`contexts/AuthContext.tsx` stores the access token under `localStorage["token"]`;
`hooks/useAuth.ts` stores it under `localStorage["accessToken"]`. Whichever ran last
wins. `src/lib/tokenManager.ts` exists as a third mechanism. The gateway work needs
one consistent key.

---

## 3. Build and test results

### 3.1 Backend tests: FAIL

```
cd backend && mvn -B clean test
...
[ERROR] Tests run: 3, Failures: 0, Errors: 3, Skipped: 0
[ERROR] Failed to execute goal ...maven-surefire-plugin:3.1.2:test
=== EXIT: 1 ===
```

The only test class is `ImageServingTest`, a full `@SpringBootTest`. All three of its
methods error with `Failed to load ApplicationContext`. Root cause, from
`target/surefire-reports/com.ecommerce.ImageServingTest.txt`:

```
Failed to instantiate [com.mongodb.client.MongoClient]: Factory method 'mongo' threw
exception with message: ... The connection string is invalid. Connection strings must
start with either 'mongodb://' or 'mongodb+srv://
```

`application.yml` declares `spring.data.mongodb.uri: ${SPRING_DATA_MONGODB_URI}` with
**no default**, so with the variable unset the placeholder resolves to an empty
string and the Mongo driver rejects it. The test therefore needs a live MongoDB and
an environment variable that nothing in the repo or CI provides.

Even with a database, the assertions could not pass: they request
`/products/Electronics/Airpods+Pro+2/1.webp` (a literal `+`, and `Airpods` with a
lowercase `p`), while the asset directory is `backend/Products/Electronics/AirPods
Pro 2/`. And `/products/**` returned 404 in the live run above.

### 3.2 Backend package: PASS

```
cd backend && mvn -B clean package -DskipTests
=== PKG EXIT: 0 ===
-rw-r--r-- 1 root root 30208506 backend/target/ecommerce-backend-1.0.0.jar
```

Compiles cleanly on JDK 21 with release 17.

### 3.3 Frontend install and build: PASS

```
cd frontend && npm ci      -> exit 0
cd frontend && npm run build
  1914 modules transformed, built in 6.94s
  dist/assets/index-a298a958.js  494.86 kB
=== BUILD EXIT: 0 ===
```

Two pre-existing warnings: `/404-pattern.svg` is referenced from
`src/index.css` and `src/styles/header.css` but is not in `public/`.

### 3.4 Frontend tests: FAIL (pre-existing)

```
cd frontend && npx vitest run
 Test Files  6 failed (6)
      Tests  25 failed | 5 passed (30)
=== VITEST EXIT: 1 ===
```

Two distinct pre-existing causes, both in the test code rather than in app code:

- `src/test/integration/UserFlow.test.tsx` - `You cannot render a <Router> inside
  another <Router>`: the test wraps a component that already contains a router.
- `src/test/components/ProductCard.test.tsx` - asserts the add-to-cart button is
  disabled when it is not.

These are the existing test components the brief says to leave alone, so they are
left untouched and CI gates on the frontend **build**, not on this suite.

### 3.5 Tooling available here

| Tool | Available | Note |
| --- | --- | --- |
| Docker daemon | **Yes** | not running at session start; started with `dockerd`, `docker run --rm hello-world` succeeded, so image pulls work and **Testcontainers is viable** |
| Docker Compose | Yes | v5.6.1 |
| JDK | 21 only | no JDK 17 installed; builds target release 17 |
| Maven | 3.9.11 | |
| Node / npm | 22.23.4 / 10.9.9 | |
| kind / minikube / kubectl | **No** | none on `PATH` |

---

## 4. Why `.github/workflows/ci-cd.yml` fails

**The workflow file is invalid, so not one step has ever run.**

```
gh run list --limit 8
completed  failure  ... ci-cd.yml  main  push  29702066610  0s  2026-07-19T20:12:15Z
completed  failure  ... ci-cd.yml  main  push  29701903249  0s  2026-07-19T20:06:58Z
...  (every run on main: failure, duration 0s)

gh run view 29702066610
X This run likely failed because of a workflow file issue.
```

Duration `0s` and that message mean GitHub rejected the file at parse time. The
reason is visible in the file itself: **two workflows were concatenated into one
file**, so `on:` and `jobs:` each appear twice at the top level.

```
$ grep -nE "^(on|jobs|name):" .github/workflows/ci-cd.yml
1:name: CI/CD Pipeline
3:on:
9:jobs:
143:on:        <-- duplicate top-level key
149:jobs:      <-- duplicate top-level key
```

GitHub Actions rejects duplicate mapping keys. (PyYAML's `safe_load` accepts the file
by silently keeping the last of each duplicate, which is why a naive YAML check says
it is fine.)

Behind that fatal error sit five more failures that would surface the moment the file
parsed, so a fix has to address all of them:

1. **`mvn clean test` cannot pass.** As in 3.1, the tests need
   `SPRING_DATA_MONGODB_URI`, which the workflow never sets. It starts a `mongo:6`
   service container but passes no connection string to Maven.
2. **`npm test` hangs forever.** `package.json` maps `test` to bare `vitest`, which is
   watch mode. CI needs `vitest run`. (And the suite fails anyway, see 3.4.)
3. **`actions/upload-artifact@v3` and `actions/download-artifact@v3` are retired** and
   now fail hard rather than warn. Used in five places.
4. **`aws-actions/configure-aws-credentials@v1`** is a Node 16 action, also retired.
5. **The `deploy` and `build-and-push` jobs depend on secrets that do not exist** in
   this repository (`AWS_ACCESS_KEY_ID`, `AWS_REGION`, `DOCKERHUB_USERNAME`,
   `EC2_SSH_PRIVATE_KEY`, ...) and deploy to ECS/EC2 infrastructure that does not
   exist either. The real deployment targets are Render and Vercel, which both
   deploy from their own Git integrations and need nothing from this workflow.

Also worth noting: `actions/checkout@v3` and `actions/setup-java@v3` / `setup-node@v3`
are old but not yet failing.

---

## 5. Secrets committed to the repository

These are live-looking values sitting in tracked files and must move to `.env`:

| Value | File |
| --- | --- |
| `MONGO_INITDB_ROOT_PASSWORD: password123` | `docker-compose.yml`, `backend/docker-compose.yml` |
| `--requirepass redis123` | `docker-compose.yml`, `backend/docker-compose.yml` |
| `SPRING_DATA_REDIS_PASSWORD: redis123` | `docker-compose.yml`, `backend/docker-compose.yml` |
| `JWT_SECRET: mySecretKey123456789012345678901234567890` | `docker-compose.yml`, `backend/docker-compose.yml` |
| `jwt.secret` default `mySecretKey123456789012345678901234567890` | `application-docker.yml` |
| `jwt.secret: your_dev_jwt_secret_key` | `application-dev.yml` |
| `admin` Mongo root user | `docker-compose.yml`, `backend/mongo-init.js` |

`.gitignore` does already ignore `.env`, and no `.env` file is tracked.

`prometheus/prometheus.yml` references `${PROMETHEUS_USER}` / `${PROMETHEUS_PASSWORD}`,
which Prometheus does **not** expand in a config file (it has no env substitution for
`basic_auth` in a static config), so that basic auth block cannot work as written.

---

## 6. Git hygiene problems

`backend/target` is **tracked**, despite `.gitignore` containing `target/`:

```
$ git ls-files | grep -c backend/target
22
```

`.gitignore` was added after those files were committed, so the ignore rule never
applied to them. They are also stale: the tree still contains
`backend/target/classes/com/ecommerce/config/SecurityConfig.class` for a
`SecurityConfig` source file that no longer exists. Any build dirties the working
tree, which pollutes every diff. The `target/` pattern is also unanchored, so it
matches a `target/` directory at any depth, including source directories that happen
to be named that.

Other stray files at the root: `DIRECT_CORB_FIX.md`, `ecommerce.products.json` (150
KB, read at runtime by `DataInitializer`), `ecommerce.products.updated.json` (116 KB,
referenced by nothing), `.idea/`, and `backend/docker-compose.yml`, which is a stale
near-duplicate of the root compose file with paths (`./backend/mongo-init.js`,
`build: ./backend`) that are wrong relative to its own location and so cannot work.

---

## 7. What this means for the migration

| Finding | Consequence |
| --- | --- |
| Real context path is `/`, routes at the root | The gateway must route `/api/**` and strip the prefix, which also finally gives the frontend one consistent base URL |
| `stock` already on `Product` | No model change needed in catalog, only decrement plumbing |
| `DataInitializer` wipes on every boot | Catalog must seed only when empty, or stock decrements are lost on restart |
| CORS declared in 5 places, 4 of them hardcoded | Remove all `@CrossOrigin`, delete `WebConfig`'s CORS, one env-driven config at the gateway |
| No security/JWT/Redis/Prometheus on the classpath | auth-service, caching and metrics are all new code, not a port |
| `/actuator/prometheus` 404 | Prometheus has never scraped this app; `prometheus.yml` needs real targets |
| Six validators exist and are unused | Email, Password, PhoneNumber get reused by auth-service as instructed |
| The seed JSON has never loaded | Carry over the five hardcoded seed classes, drop the dead JSON path |
| Cart is client side, no items in `CreateOrderRequest` | order-service accepts items + addresses, and must re-price from catalog |
| Two token keys in localStorage | Settle on one before wiring auth |
| Frontend tests fail for pre-existing reasons | CI gates the frontend build, not that suite |
| No kind/minikube/kubectl | k8s manifests will be written but cannot be deployed here |
| Docker works | Testcontainers integration tests are viable |
