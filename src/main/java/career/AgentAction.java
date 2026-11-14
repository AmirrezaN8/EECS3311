package career;

public interface AgentAction {
    String name();
    String describe();
    Object execute();
    record FeatureCommand(String name,String describe,java.util.function.Supplier<Object> operation) implements AgentAction {
        public Object execute(){return operation.get();}
    }
}
