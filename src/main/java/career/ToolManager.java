package career;

import java.util.*;

public final class ToolManager {
    private final Map<String,Tool> tools=new LinkedHashMap<>();
    public void register(Tool tool){String name=Models.required(tool.getName(),"Tool name");if(tools.putIfAbsent(name,tool)!=null)throw new IllegalArgumentException("Tool already registered: "+name);}
    public ToolResult executeTool(String name,Map<String,String> arguments){
        Tool tool=tools.get(name);if(tool==null)throw new IllegalArgumentException("Unknown tool: "+name);
        if(Thread.currentThread().isInterrupted())throw new IllegalStateException("Tool operation cancelled");
        try{return Objects.requireNonNull(tool.execute(Map.copyOf(arguments)),"Missing tool result");}
        catch(RuntimeException e){if(Thread.currentThread().isInterrupted())throw e;return new ToolResult(false,List.of(),name+": "+e.getMessage());}
    }
}
