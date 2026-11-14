package career;

import java.util.List;
import java.util.Objects;

public record ToolResult(boolean success, List<SearchResult> data, String message) {
    public ToolResult { data=List.copyOf(data);Objects.requireNonNull(message);if(!success&&message.isBlank())throw new IllegalArgumentException("Failed tools require an explanation"); }
    public List<SearchResult> requireData(){if(!success)throw new IllegalStateException(message);return data;}
}
