package sn.haybtech.sdk;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import com.fasterxml.jackson.databind.ObjectMapper;
import sn.haybtech.sdk.resources.Payments;
import sn.haybtech.sdk.resources.Webhooks;

/**
 * HayBTech Java Client - Hardened for maximum security.
 */
public class HayBTechClient {
    private final String secretKey;
    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public final Payments payments;
    public final Webhooks webhooks;

    public HayBTechClient(String secretKey) {
        this(secretKey, "https://api.haybtech.com/v1");
    }

    public HayBTechClient(String secretKey, String baseUrl) {
        if (secretKey == null || !secretKey.startsWith("sk_")) {
            throw new IllegalArgumentException("Invalid secret key. Expected 'sk_live_...' or 'sk_test_...'.");
        }

        // Prevent CRLF injection
        if (secretKey.contains("\r") || secretKey.contains("\n")) {
            throw new IllegalArgumentException("Invalid secret key: contains forbidden characters.");
        }

        this.secretKey = secretKey;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.objectMapper = new ObjectMapper();
        
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        this.payments = new Payments(this);
        this.webhooks = new Webhooks(this);
    }

    public boolean isTestMode() {
        return secretKey.startsWith("sk_test_");
    }

    /**
     * Security: Mask secret key in toString() to prevent log leakage.
     */
    @Override
    public String toString() {
        String masked = secretKey.substring(0, 6) + "..." + secretKey.substring(secretKey.length() - 4);
        return "HayBTechClient[baseUrl=" + baseUrl + ", testMode=" + isTestMode() + ", secretKey=" + masked + "]";
    }

    public Map<String, Object> request(String method, String path, Object body) throws Exception {
        return request(method, path, body, Map.of());
    }

    public Map<String, Object> request(String method, String path, Object body, Map<String, String> extraHeaders) throws Exception {
        String url = baseUrl + path.replaceFirst("^/", "");
        
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + secretKey)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("X-Request-ID", UUID.randomUUID().toString())
                .header("User-Agent", "HayBTech-Java-SDK/1.0.0");

        extraHeaders.forEach(requestBuilder::header);

        if (body != null) {
            requestBuilder.method(method, HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
        } else {
            requestBuilder.method(method, HttpRequest.BodyPublishers.noBody());
        }

        HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        
        Map<String, Object> data = objectMapper.readValue(response.body(), Map.class);
        
        if (response.statusCode() >= 400) {
            Map<String, Object> error = (Map<String, Object>) data.get("error");
            String message = error != null ? (String) error.get("message") : "Unknown API Error";
            throw new RuntimeException("HayBTech API Error (" + response.statusCode() + "): " + message);
        }

        return data;
    }
}
