package sn.haybtech.sdk;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Webhook verification utility for Java.
 */
public class Webhook {
    private static final long TOLERANCE = 300; // 5 minutes
    private static final long MAX_PAYLOAD_SIZE = 1024 * 1024; // 1MB
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Map<String, Object> constructEvent(String payload, String signatureHeader, String secret) throws Exception {
        if (payload == null || signatureHeader == null || secret == null) {
            throw new IllegalArgumentException("Missing required parameters.");
        }

        if (payload.length() > MAX_PAYLOAD_SIZE) {
            throw new IllegalArgumentException("Payload exceeds maximum allowed size.");
        }

        // Parse header: t=123,v1=abc
        Map<String, String> parts = new HashMap<>();
        for (String part : signatureHeader.split(",")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2) {
                parts.put(kv[0].trim(), kv[1].trim());
            }
        }

        if (!parts.containsKey("t") || !parts.containsKey("v1")) {
            throw new IllegalArgumentException("Malformed signature header.");
        }

        long timestamp = Long.parseLong(parts.get("t"));
        String receivedSig = parts.get("v1");

        // Replay protection
        long now = System.currentTimeMillis() / 1000;
        if (Math.abs(now - timestamp) > TOLERANCE) {
            throw new RuntimeException("Webhook signature expired (replay protection).");
        }

        // Compute expected signature
        String signedPayload = timestamp + "." + payload;
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);

        byte[] hash = sha256_HMAC.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        String expectedSig = hexString.toString();

        // Constant-time comparison
        if (!MessageDigest.isEqual(expectedSig.getBytes(StandardCharsets.UTF_8), receivedSig.getBytes(StandardCharsets.UTF_8))) {
            throw new RuntimeException("Invalid webhook signature.");
        }

        return objectMapper.readValue(payload, Map.class);
    }
}
