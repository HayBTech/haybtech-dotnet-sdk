# HayBTech Java SDK

Official Java SDK for the HayBTech Payment Gateway API -- mobile payments across West Africa .

[![Maven Central](https://img.shields.io/maven-central/v/sn.haybtech/haybtech-sdk.svg)](https://search.maven.org/artifact/sn.haybtech/haybtech-sdk)
[![Java](https://img.shields.io/badge/java-11+-blue.svg)](https://www.oracle.com/java/technologies/downloads/)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

---

## Installation

### Maven

```xml
<dependency>
    <groupId>sn.haybtech</groupId>
    <artifactId>haybtech-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```gradle
implementation 'sn.haybtech:haybtech-sdk:1.0.0'
```

---

## Quick Start

Initialize the client with your secret key (`sk_live_...` or `sk_test_...`):

```java
import sn.haybtech.sdk.HayBTechClient;
import java.util.Map;

HayBTechClient client = new HayBTechClient("sk_test_your_key");

// Initiate a payment
try {
    Map<String, Object> response = client.payments.create(Map.of(
        "merchant_ref", "ORDER-12345",
        "amount", 5000,
        "currency", "XOF",
        "success_url", "https://mysite.com/success",
        "failed_url", "https://mysite.com/failed",
        "callback_url", "https://mysite.com/webhook"
    ));

    String paymentUrl = (String) ((Map) response.get("data")).get("payment_url");
    System.out.println("Payment URL: " + paymentUrl);
} catch (Exception e) {
    e.printStackTrace();
}
```

---

## Webhooks (Spring Boot)

Securely verify incoming webhooks in a `@RestController`:

```java
import sn.haybtech.sdk.Webhook;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
public class WebhookController {

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-HayBTech-Signature") String signature) {

        String secret = "whsec_...";

        try {
            Map<String, Object> event = Webhook.constructEvent(payload, signature, secret);
            
            switch ((String) event.get("event")) {
                case "payment.success":
                    // Update order status
                    break;
                case "payment.failed":
                    // Handle failure
                    break;
            }
            
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.status(403).body("Invalid Signature");
        }
    }
}
```

---

## Available Events

| Event                     | Description              |
|:--------------------------|:-------------------------|
| `payment.success`         | Payment confirmed        |
| `payment.failed`          | Payment failed           |
| `payment.cancelled`       | Cancelled by customer    |
| `payment.expired`         | Payment timed out        |
| `payout.success`          | Payout completed         |
| `payout.failed`           | Payout failed            |
| `refund.success`          | Refund processed         |


---

## Error Handling

```java
try {
    Map<String, Object> response = client.payments.create(params);
} catch (IllegalArgumentException e) {
    // Invalid secret key format
} catch (Exception e) {
    // API error with HTTP status code in message
    // e.g., "HayBTech API Error (422): {"error":"insufficient_funds"}"
    System.err.println(e.getMessage());
}
```

---

## Test Mode

```java
HayBTechClient client = new HayBTechClient("sk_test_..."); // No real charges
```

---


---

## Security Features

This SDK is built for **Maximum Security**:

- **Zero External HTTP Dependencies**: Uses native `java.net.http.HttpClient` (Java 11+).
- **Secret Masking**: Keys are automatically masked in `toString()` to avoid accidental log exposure.
- **Memory Protection**: Webhook payloads are capped at 1 MB to prevent DoS.
- **Timing Attack Resistance**: Uses `MessageDigest.isEqual()` for constant-time signature verification.
- **Replay Protection**: Webhook timestamps are validated within a 5-minute tolerance window.
- **CRLF Guard**: Prevents HTTP header injection via malformed keys.

---

## API Resources

- `client.payments` -- Create, retrieve, list, and verify transactions.

---

MIT License
