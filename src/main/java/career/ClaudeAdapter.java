package career;

import java.util.*;

public final class ClaudeAdapter implements LLMClient {
    public static final String HAIKU_MODEL = "claude-haiku-4-5-20251001";
    public static final String SONNET_MODEL = "claude-sonnet-5-5";
    private final AnthropicApi api;
    public ClaudeAdapter(AnthropicApi api) { this.api = api; }

    @Override public String generate(String prompt, ModelTier tier) {
        Objects.requireNonNull(tier, "Model tier");
        String model = tier == ModelTier.HAIKU ? HAIKU_MODEL : SONNET_MODEL;
        String response = api.postMessages(Json.write(Map.of(
            "model", model,
            "max_tokens", 4096,
            "system", PromptBuilder.SYSTEM,
            "messages", List.of(Map.of("role", "user", "content", Models.required(prompt, "Prompt"))))));
        Map<String, Object> message = ResponseParser.parseJson(response);
        if (!"message".equals(Json.text(message, "type")) || !"assistant".equals(Json.text(message, "role")))
            throw new IllegalArgumentException("Invalid Claude message envelope");
        if (!"end_turn".equals(Json.text(message, "stop_reason")))
            throw new IllegalStateException("Claude response did not complete normally; no partial output accepted");
        StringBuilder text = new StringBuilder();
        for (Object value : Json.list(message, "content")) {
            Map<String, Object> block = Json.object(value);
            switch (Json.text(block, "type")) {
                case "text" -> text.append(Json.text(block, "text"));
                case "thinking", "redacted_thinking" -> { /* Internal reasoning is not application output. */ }
                default -> throw new IllegalArgumentException("Unexpected Claude content type");
            }
        }
        return Models.required(text.toString(), "Claude text output");
    }
}
