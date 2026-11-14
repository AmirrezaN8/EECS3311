package career;

import java.util.List;

public interface DecisionClient {
    Decision decide(String question, List<String> options);
}
