package career;

public interface LLMClient {
    String generate(String prompt, ModelTier tier);
}
