# HayBTech Java SDK

Official Java SDK for the HayBTech Payment Gateway API -- mobile payments across West Africa .

[![Maven Central](https://img.shields.io/maven-central/v/sn.haybtech/haybtech-sdk.svg)](https://search.maven.org/artifact/sn.haybtech/haybtech-sdk)
[![Java](https://img.shields.io/badge/java-11+-blue.svg)](https://www.oracle.com/java/technologies/downloads/)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

---

## Intégration par IA (Prompt pour Marchands)

Si vous utilisez un assistant IA (comme Cursor, GitHub Copilot, ChatGPT, Claude, etc.), vous pouvez copier-coller le prompt suivant pour intégrer ce SDK de A à Z dans votre projet :

```text
Agis en tant qu'expert en développement Java avec Spring Boot. Je souhaite intégrer le SDK Java officiel de HayBTech (`sn.haybtech:haybtech-sdk`) pour accepter des paiements mobiles (Wave, Orange Money, etc.) sur mon site de A à Z.

Voici ma stack technique actuelle :
- Persistance : [ex: Spring Data JPA avec PostgreSQL, Hibernate]
- Modèle de commande : [décrivez brièvement votre entité Order]

Tâches à accomplir dans le code généré :
1. **Configuration** : Configurer `HayBTechClient` comme un Bean Spring en injectant la clé d'API depuis `application.properties` ou `application.yml`.
2. **Checkout Controller** : Créer un `@RestController` avec un endpoint `/api/checkout`. Il doit récupérer les données de la commande, instancier la requête avec `client.payments.create(...)` en lui passant la référence, le montant, la devise (XOF), et les URLs de redirection (success_url, failed_url, callback_url), et retourner l'URL de paiement.
3. **Webhook Controller** : Créer un endpoint POST `/api/webhook` acceptant les notifications de paiement. Il doit :
   - Accepter le payload brut (`@RequestBody String payload`) et le header `X-HayBTech-Signature` (ou `X-HayB-Signature`).
   - Utiliser `Webhook.constructEvent(payload, signature, secret)` pour s'assurer que le message provient bien de HayBTech.
   - Traiter de manière idempotente les statuts `payment.success` (pour marquer la commande comme payée) et `payment.failed` (pour marquer comme échouée) en base de données.
   - Renvoyer un `ResponseEntity.ok("OK")`.
4. **Gestion des Erreurs** : Mettre en place un `@ControllerAdvice` ou des blocs catch locaux pour intercepter les exceptions d'API et retourner les statuts HTTP adéquats sans divulguer d'informations système sensibles.

Génère un code propre, utilisant les standards Java modernes, entièrement commenté et prêt à être intégré.
```

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

MIT License

