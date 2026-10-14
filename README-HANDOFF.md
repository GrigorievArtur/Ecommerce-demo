# LegoStore — E-commerce Platform (Hand-off)

A server-rendered storefront, customer accounts, checkout with a pluggable payment
gateway, and an admin panel — built on the existing Spring Boot + FreeMarker + MySQL
codebase in this repository.

> **Status:** working vertical slice. Catalogue, cart, accounts, checkout, admin CRUD,
> mock payments, SEO endpoints and sales analytics are implemented and covered by tests.
> See [Roadmap / not yet production-ready](#roadmap--not-yet-production-ready) for the
> deliberately deferred items.

---

## 1. Stack and why

| Concern | Choice | Why |
| --- | --- | --- |
| Language / framework | **Java 17 + Spring Boot 4** | Already the basis of this codebase; strong typing, mature security, huge hiring pool. |
| Templating | **FreeMarker** | Requested; first-class Spring integration; server-rendered HTML is best for **SEO** and works without JS. |
| Data | **MySQL 8 + Spring Data JPA / Hibernate** | Relational integrity for orders, inventory and users; already configured. |
| Security | **Spring Security** — stateless JWT for `/api/**`, session form-login + CSRF for the site | One codebase serves both an HTML site and a token API. |
| Payments | **Provider-agnostic `PaymentGateway`** (mock by default) | Keep card data out of our systems; drop in Stripe/Adyen without touching checkout logic. |
| Hosting | **Single fat JAR** (or Docker Compose) behind Nginx | Simple, cheap, portable across hosts. |

**Why not WordPress/WooCommerce or Shopify?** They are excellent for launching fast on a
standard catalogue. This project needs custom pricing snapshots, a JSON order snapshot
model and an existing Java domain — building on Spring keeps that logic in one place and
avoids plugin sprawl, per-order fees and vendor lock-in. If time-to-market trumped
customisation, Shopify would be the pragmatic recommendation.

---

## 2. Run it locally

### Prerequisites
- JDK 17+ (the build uses the bundled Maven wrapper `./mvnw`)
- MySQL 8 (or Docker)

### Option A — Docker Compose (recommended)
```bash
docker compose up --build
# storefront: http://localhost:8080
# admin:      http://localhost:8080/admin  (admin@legostore.test / admin12345)
```

### Option B — local MySQL
Create the schema and a user matching `src/main/resources/application.properties`, then:
```bash
./mvnw spring-boot:run
```

### Configuration (override via environment variables)
| Property | Env var | Default |
| --- | --- | --- |
| `spring.datasource.url` | `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/LegoStore…` |
| `app.base-url` | `APP_BASE_URL` | `http://localhost:8080` |
| `app.seed.enabled` | `APP_SEED_ENABLED` | `true` |
| `app.admin.email` | `APP_ADMIN_EMAIL` | `admin@legostore.test` |
| `app.admin.password` | `APP_ADMIN_PASSWORD` | `admin12345` |
| `app.payment.provider` | `APP_PAYMENT_PROVIDER` | `mock` |

> The first start seeds a demo catalogue and the admin account **only if the product
> table is empty**. Set `APP_SEED_ENABLED=false` in production.

### Tests
```bash
./mvnw test          # 81 tests: API, services, rendering, account/address book and the full web checkout flow
```

---

## 3. What was built

### Storefront (`com.example.ecommercedemo.web`, `templates/`)
- `GET /` home with featured products
- `GET /products` catalogue — **search**, **category filter**, sorting, pagination
- `GET /products/{id}` product detail with stock status, sale pricing and **JSON-LD**
- `GET/POST /cart…` guest + signed-in cart, cookie-backed (`suid`)
- `GET/POST /checkout` delivery + payment, order confirmation
- `GET/POST /register`, `GET /account` (order history + **address book**: add, edit, delete, set default)
- `GET /returns` returns policy
- Responsive CSS with **automatic + manual dark mode** (`static/css/app.css`), progressive-enhancement JS (`static/js/app.js`)
- QOL details: dismissible flash messages, back-to-top, quantity steppers, product image gallery,
  related products, free-delivery progress bar, windowed pagination, print-friendly receipt,
  skip-link and visible focus states

### Admin panel (`/admin`, role `ADMIN`)
- Dashboard with sales analytics and low-stock alerts
- Product list, create, edit and delete

### Platform
- **Cart ownership**: guest and account carts are separate first-class types (`CartOwner`), so
  the service never sniffs the security context. A guest cart is keyed by the `suid` cookie and
  is merged into the account cart on login. The REST contract keeps reads and creation apart:
  `GET /api/cart` is read-only (404 when the caller has none) and `POST /api/cart` is the only
  way to create one (409 when it already exists). Mutation endpoints (`/api/cart/items`) only
  ever touch an existing cart; adding the first item lazily starts a guest cart.
- **Inventory**: nullable `stock` on `Product` (`null` = untracked/unlimited); enforced in
  the cart and decremented on sale.
- **Payments**: `PaymentGateway` interface + `MockPaymentGateway`; card data is processed
  in memory and never stored.
- **Analytics**: `AnalyticsService` computes revenue, order count, average order value and
  low stock from **frozen price snapshots** (historic figures stay correct when prices change).
- **SEO**: clean URLs, meta/OpenGraph tags, dynamic `/sitemap.xml`, `/robots.txt`.
- **Security**: session login + CSRF for the site, JWT for the API, BCrypt passwords,
  role-based access to `/admin`, HTML auto-escaping in FreeMarker.

### Package map
```
api/          REST interfaces (OpenAPI-annotated)       web/        server-rendered controllers
controllers/  REST implementations                      web/forms/  form-backing beans
services/     business logic                            analytics/  sales summary
repositories/ Spring Data repositories                  payments/   gateway abstraction
entities/     JPA entities (Product, Cart, Order…)      configs/    security, seeding
dtos/mappers/ MapStruct DTOs                            templates/  FreeMarker views
```

---

## 4. Swapping in a real payment provider (Stripe example)

1. Add the SDK: `com.stripe:stripe-java`.
2. Implement `PaymentGateway`:
   ```java
   @Component
   @ConditionalOnProperty(name = "app.payment.provider", havingValue = "stripe")
   class StripePaymentGateway implements PaymentGateway {
       public PaymentResult charge(PaymentRequest r) {
           // create a PaymentIntent using a tokenised PaymentMethod id — never a raw PAN
       }
       public String providerName() { return "stripe"; }
   }
   ```
3. On the checkout page, replace the raw card inputs with **Stripe Elements** and submit
   the resulting token; the raw card number never reaches our servers.
4. Set `APP_PAYMENT_PROVIDER=stripe` plus `STRIPE_SECRET_KEY` / `STRIPE_PUBLISHABLE_KEY`.
5. Add a signed **webhook** endpoint to confirm asynchronous events
   (`payment_intent.succeeded`) before fulfilling.

`PaymentService`/checkout only depend on the interface, so no other code changes.

---

## 5. Hosting this on your infrastructure

**Recommended (VPS / cloud VM):**
1. Install JDK 17+, MySQL 8, Nginx.
2. Build `./mvnw -DskipTests package` → `target/*.jar`.
3. Run under systemd (`ExecStart=/usr/bin/java -jar /opt/lego/app.jar`) with env vars for
   secrets; keep MySQL on the same private network.
4. Nginx reverse-proxies `:443 → :8080`, terminates TLS, and serves `/css`, `/js`,
   `/images` from disk with long cache headers.
5. Point `app.base-url` at your public HTTPS URL so the sitemap/canonical links are correct.
6. Back up MySQL nightly; keep the JAR + `application.properties` (or env file) in version control.

Alternatively use the included `Dockerfile` / `compose.yaml` on any container host.

---

## 6. Handover checklist

- [x] JWT secret is externalised as `app.jwt.secret` (env `APP_JWT_SECRET`) — set a long random value per environment.
- [ ] Set a strong `APP_ADMIN_PASSWORD` and disable `APP_SEED_ENABLED`.
- [ ] Create a least-privilege MySQL user (no root).
- [ ] Enable HTTPS (HSTS) and set `server.forward-headers-strategy=framework` behind Nginx.
- [ ] Configure SMTP + order/return emails (not included).
- [ ] Wire the real payment provider and its webhook.
- [ ] Add GA4 / Plausible for behavioural analytics.
- [ ] Set up nightly DB backups and uptime monitoring.

---

## 7. Roadmap / not yet production-ready

| Item | Notes |
| --- | --- |
| **Product variants** | The model has one price per product. Variants (size/colour) need a `Variant` entity with its own SKU, price and stock, plus cart/order references by variant id. |
| **Real payments** | Mock gateway only; see §4. No refunds yet. |
| **Behavioural analytics** | Sales analytics are native; use GA4/Plausible for sessions, funnels and heatmaps. |
| **Transactional email** | No SMTP wiring for receipts, shipping or returns yet. |
| **Returns workflow** | `/returns` is a policy + mailto; no RMA entity or refund state machine. |
| ~~Guest checkout~~ | **Supported** — the delivery address is snapshotted onto the order as JSON, so no user account is required. |
| **Search** | SQL `LIKE` search; consider a proper index for large catalogues. |
| **Image uploads** | Admin accepts an image URL; add object-storage upload (S3) for self-service. |
| **Rate limiting / audit log** | Add at the edge (Nginx/Bucket4j) and a security audit trail. |

---

## 8. Support model (recommended)

| Window | Coverage | Response target |
| --- | --- | --- |
| Days 0–14 (hypercare) | Bug fixes, checkout/payment monitoring, deploy issues | Same business day |
| Ongoing | Two sprints of enhancements per month, security patching, dependency updates | 1 business day for critical, 3 for normal |
| Quarterly | Backups restore drill, dependency/CVE review, performance check | Scheduled |

Deliverables each release: changelog, updated tests, deployment notes.
