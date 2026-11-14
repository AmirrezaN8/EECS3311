package career;

import java.util.*;
import java.util.function.Function;

/** Shared structured-response validation; feature services supply their own schema/evidence checks. */
public final class ResponseParser {
    private ResponseParser() {}
    public static Map<String, Object> parseJson(String text) { return Json.object(Json.parse(text)); }
    public static <T> T validate(String text, Function<Map<String, Object>, T> schema) {
        if(text==null||text.length()>4_000_000)throw new IllegalArgumentException("Model output missing or too large");
        String json=text.strip();
        if(json.startsWith("```")){
            int newline=json.indexOf('\n');
            String header=newline<0?"":json.substring(0,newline).stripTrailing();
            if((!header.equals("```")&&!header.equalsIgnoreCase("```json"))||!json.endsWith("```")||json.length()<newline+4)
                throw new IllegalArgumentException("Expected one complete JSON code block without surrounding commentary");
            json=json.substring(newline+1,json.length()-3).strip();
        }
        if(!json.startsWith("{"))throw new IllegalArgumentException("Expected a JSON object from the model; received "+(json.isEmpty()?"empty output":"non-object text")+" (response content not logged)");
        return schema.apply(parseJson(json));
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
