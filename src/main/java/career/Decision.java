package career;

import java.util.Map;

/** Provider confidence is distinct from the probability of the selected option. */
public record Decision(String choice, Map<String, Double> probabilities, double confidence) {
    public Decision {
        choice = Models.required(choice, "Decision choice");
        probabilities = Map.copyOf(probabilities);
        if (!Double.isFinite(confidence) || confidence < 0 || confidence > 1)
            throw new IllegalArgumentException("Invalid decision confidence");
        if (!probabilities.containsKey(choice)) throw new IllegalArgumentException("Choice has no probability");
        double sum = 0;
        for (double p : probabilities.values()) {
            if (!Double.isFinite(p) || p < 0 || p > 1) throw new IllegalArgumentException("Invalid option probability");
            sum += p;
        }
        if (Math.abs(sum - 1) > 0.001) throw new IllegalArgumentException("Decision probabilities must sum to one");
    }
}
