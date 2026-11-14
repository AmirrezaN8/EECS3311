package career;

import java.util.Map;

/** Anthropic Messages API transport, independent of model-tier/application logic. */
public final class AnthropicApi {
    public static final String ENDPOINT = "https://api.anthropic.com/v1/messages";
    public static final String VERSION = "2026-10-21";
    private final String key;
    private final HttpTransport http;

    public AnthropicApi(Config config) { this(config.get("ANTHROPIC_API_KEY", ""), new HttpTransport()); }
    public AnthropicApi(String key, HttpTransport http) { this.key = key; this.http = http; }

    public String postMessages(String json) {
        if (key.isBlank()) throw new IllegalStateException("Set ANTHROPIC_API_KEY in local career.properties or the environment; Claude is not configured");
        return http.request(ENDPOINT, Map.of("Authorization", "Bearer " + key, "anthropic-version", VERSION), json);
    }
}
