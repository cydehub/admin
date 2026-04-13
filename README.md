# Stockzeno WMS Backend

Spring Boot 3.x + Java 21 backend (with a static SPA dashboard) for the Stockzeno Warehouse Management System.

## Local Setup (no Docker required)

1. Install prerequisites:
   - **Java 21** (Temurin or equivalent)
   - **PostgreSQL 16**

2. Create the database/user (defaults used by the app):
   ```sql
   CREATE USER stockzeno WITH PASSWORD 'stockzeno';
   CREATE DATABASE stockzeno OWNER stockzeno;
   ```

3. Run the app:
   ```bash
   mvn spring-boot:run
   ```

### URLs
- Landing page: `http://localhost:8080/`
- Dashboard: `http://localhost:8080/app.html`
- Swagger UI: `http://localhost:8080/docs`

### Seed Admin
- Email: `admin@stockzeno.local`
- Password: `ChangeMe123!`

### Redis (optional)
Redis is disabled by default for local dev. If you want to enable it, re-enable Redis auto-config in
`src/main/resources/application.yml` and set `spring.cache.type=redis`.

## Webhook signature verification

Webhook deliveries include an `X-Webhook-Signature` header when the endpoint has a secret configured.
The value format is:

```
sha256=<hex-encoded-hmac>
```

Where the HMAC is calculated with the endpoint secret and the **raw request body**:

```
hmac = HMAC_SHA256(secret, rawBody)
signature = "sha256=" + hex(hmac)
```

If no secret is configured for the endpoint, the signature header is omitted.

Example (Java):

```java
Mac mac = Mac.getInstance("HmacSHA256");
mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
byte[] digest = mac.doFinal(rawBodyBytes);
String signature = "sha256=" + HexFormat.of().formatHex(digest);
```

Example (Node.js):

```js
import crypto from "crypto";

const hmac = crypto.createHmac("sha256", secret).update(rawBody).digest("hex");
const signature = `sha256=${hmac}`;
```

Example (Python):

```python
import hmac
import hashlib

digest = hmac.new(secret.encode("utf-8"), raw_body, hashlib.sha256).hexdigest()
signature = f"sha256={digest}"
```

## Modules
- Authentication + RBAC
- Inventory + batch management
- Location hierarchy (warehouse/building/aisle/shelf/bin)
- Audit logging
- Analytics + reorder suggestions
- Webhooks + notifications
