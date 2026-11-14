package career;

import java.util.*;
import java.util.function.Function;

/** Shared structured-response validation; feature services supply their own schema/evidence checks. */
public final class ResponseParser {
    private ResponseParser() {}
    public static Map<String, Object> parseJson(String text) { return Json.object(Json.parse(text)); }
    public static <T> T validate(String text, Function<Map<String, Object>, T> schema) {
        return schema.apply(parseJson(text));
    }
    public static void validateOptions(List<String> options) {
        Objects.requireNonNull(options, "Decision options");
        if (options.isEmpty() || options.stream().anyMatch(o -> o == null || o.isBlank())
            || new HashSet<>(options).size() != options.size())
            throw new IllegalArgumentException("Decision options must be nonempty, distinct labels");
    }
    public static Decision validateDecision(Decision decision, List<String> options) {
        if (!options.contains(decision.choice()) || !decision.probabilities().keySet().equals(new HashSet<>(options)))
            throw new IllegalArgumentException("Jev decision does not match the requested options");
        return decision;
    }
}
