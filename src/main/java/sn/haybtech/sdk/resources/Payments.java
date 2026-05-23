package sn.haybtech.sdk.resources;

import sn.haybtech.sdk.HayBTechClient;
import java.util.Map;

public class Payments {
    private final HayBTechClient client;

    public Payments(HayBTechClient client) {
        this.client = client;
    }

    public Map<String, Object> create(Map<String, Object> params) throws Exception {
        return create(params, "");
    }

    public Map<String, Object> create(Map<String, Object> params, String idempotencyKey) throws Exception {
        Map<String, String> headers = idempotencyKey.isEmpty() ? Map.of() : Map.of("Idempotency-Key", idempotencyKey);
        return client.request("POST", "payments", params, headers);
    }

    public Map<String, Object> retrieve(String id) throws Exception {
        return client.request("GET", "payments/" + id, null);
    }

    public Map<String, Object> list(Map<String, String> params) throws Exception {
        // Simple query param builder could be added here
        return client.request("GET", "payments", null);
    }

    public Map<String, Object> verify(String id) throws Exception {
        return client.request("POST", "payments/" + id + "/verify", null);
    }
}
