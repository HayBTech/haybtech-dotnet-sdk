package sn.haybtech.sdk.resources;

import sn.haybtech.sdk.HayBTechClient;
import java.util.Map;
import java.util.HashMap;

public class Webhooks {
    private final HayBTechClient client;

    public Webhooks(HayBTechClient client) {
        this.client = client;
    }

    public Map<String, Object> all() throws Exception {
        return client.request("GET", "webhooks", null);
    }

    public Map<String, Object> create(Map<String, Object> params) throws Exception {
        return client.request("POST", "webhooks", params);
    }

    public Map<String, Object> reveal(String id, String otp) throws Exception {
        Map<String, Object> body = new HashMap<>();
        if (otp != null) body.put("otp", otp);
        return client.request("POST", "webhooks/" + id + "/reveal", body);
    }

    public Map<String, Object> delete(String id, String otp) throws Exception {
        Map<String, String> headers = new HashMap<>();
        if (otp != null) headers.put("X-OTP", otp);
        return client.request("DELETE", "webhooks/" + id, null, headers);
    }
}
