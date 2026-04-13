# Stockzeno WMS Backend

Spring Boot 3.x + Java 21 backend for the Stockzeno Warehouse Management System.

## Local Setup

1. Start infrastructure:
   ```bash
   docker compose up -d
   ```
2. Copy environment defaults:
   ```bash
   cp .env.example .env
   ```
3. Run the app:
   ```bash
   ./mvnw spring-boot:run
   ```

Swagger UI: `http://localhost:8080/docs`

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

## Modules (planned)
- Authentication + RBAC
- Inventory + batch management
- Audit logging
- Analytics + reorder suggestions
- Webhooks + notifications
