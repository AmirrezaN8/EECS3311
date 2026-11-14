package career;

import java.util.*;
import java.util.function.Function;

/** Shared validation/retry coordinator; text and decisions have separate Stage 1 provider boundaries. */
public final class Ai implements DecisionClient {
    private final LLMClient client;
    private final DecisionClient decisions;
    public Ai(LLMClient client, DecisionClient decisions) {
        this.client = Objects.requireNonNull(client);
        this.decisions = Objects.requireNonNull(decisions);
    }
    public <T> T ask(String task, Object context, ModelTier tier, Function<Map<String, Object>, T> schema) {
        return retry("Claude", attempt -> ResponseParser.validate(client.generate(
            PromptBuilder.build(task, context, attempt > 0), tier), schema));
    }
    @Override public Decision decide(String question, List<String> options) {
        ResponseParser.validateOptions(options);
        List<String> allowed = List.copyOf(options);
        return retry("Jev", attempt -> ResponseParser.validateDecision(decisions.decide(question, allowed), allowed));
    }
    private static <T> T retry(String provider, Function<Integer, T> operation) {
        RuntimeException last = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            if (Thread.currentThread().isInterrupted()) throw new IllegalStateException("Operation cancelled");
            try { return operation.apply(attempt); }
            catch (RuntimeException e) { last = e; }
        }
        throw new IllegalStateException(provider + " request failed after two attempts: " + last.getMessage(), last);
    }
    public String text(String instruction, Object context, ModelTier tier) {
        return ask(instruction + " Return {\"text\":\"...\"}.", context, tier,
            m -> Models.required(Json.text(m, "text"), "AI text"));
    }
}
