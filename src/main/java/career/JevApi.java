package career;

import java.util.Map;

/** TypeSafe System One HTTP boundary. Never sends decisions through a text LLM. */
public final class JevApi {
    public static final String ENDPOINT = "https://api.typesafe.ai/v1/systemone";
    private final String key;
    private final HttpTransport http;

    public JevApi(Config config) { this(config.get("TYPESAFE_API_KEY", ""), new HttpTransport()); }
    public JevApi(String key, HttpTransport http) { this.key = key; this.http = http; }

    public String postSystemOne(String json) {
        if (key.isBlank()) throw new IllegalStateException("Set TYPESAFE_API_KEY in local career.properties or the environment; Jev is not configured");
        return http.request(ENDPOINT, Map.of("Authorization", "Bearer " + key), json);
    }
}
