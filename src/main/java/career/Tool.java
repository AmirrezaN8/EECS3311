package career;

import java.util.Map;

public interface Tool {
    String getName();
    ToolResult execute(Map<String,String> arguments);
}
