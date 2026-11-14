package career;

import java.util.*;

/** State objects own the workflow transitions; approval/execution cannot occur in the wrong stage. */
public final class AgentSession {
    public interface AgentState {void advance(AgentSession s);}
    private AgentState state=new IdleState();
    private final Planner planner;private final ApprovalGate gate;private final AgentEventPublisher events;
    private final List<AgentAction> remaining;private final List<String> trace=new ArrayList<>();private final java.util.function.Supplier<String> context;
    private final java.util.function.BooleanSupplier cancelled;
    private AgentAction pending;private int steps;private boolean stopped;
    public AgentSession(Planner planner,ApprovalGate gate,AgentEventPublisher events,List<AgentAction> actions,String context,java.util.function.BooleanSupplier cancelled){this(planner,gate,events,actions,()->context,cancelled);}
    public AgentSession(Planner planner,ApprovalGate gate,AgentEventPublisher events,List<AgentAction> actions,java.util.function.Supplier<String> context,java.util.function.BooleanSupplier cancelled){this.planner=planner;this.gate=gate;this.events=events;this.remaining=new ArrayList<>(actions);this.context=context;this.cancelled=cancelled;}
    public List<String> handleInput(){while(!(state instanceof FinishedState)){if(cancelled.getAsBoolean()||Thread.currentThread().isInterrupted()){stopped=true;setState(new FinishedState());break;}state.advance(this);}return List.copyOf(trace);}
    void setState(AgentState next){state=next;note("state",next.getClass().getSimpleName());}
    private void note(String type,String text){trace.add(type+": "+text);events.publish(type,text);}
    public static final class IdleState implements AgentState {public void advance(AgentSession s){s.setState(new PlanningState());}}
    public static final class PlanningState implements AgentState {
        public void advance(AgentSession s){
            if(s.remaining.isEmpty()||s.steps>=10){s.setState(new FinishedState());return;}
            List<AgentAction> eligible=s.remaining.stream().filter(a->!a.name().equals("checklist")||s.remaining.size()==1).toList();
            try{s.pending=s.planner.chooseNext(s.context.get()+" Trace: "+s.trace,eligible);}
            catch(RuntimeException e){s.note("planner-error",e.getMessage()+"; remaining actions not executed");s.setState(new FinishedState());return;}
            if(s.pending==null){s.setState(new FinishedState());return;}
            s.steps++;s.note("decision","Selected "+s.pending.name());s.setState(new AwaitingApprovalState());
        }
    }
    public static final class AwaitingApprovalState implements AgentState {
        public void advance(AgentSession s){
            if(s.gate.review("Agent proposes: "+s.pending.describe()+". Run this step?",false))s.setState(new ExecutingState());
            else{s.note("declined",s.pending.name());s.remaining.remove(s.pending);s.setState(new PlanningState());}
        }
    }
    public static final class ExecutingState implements AgentState {
        public void advance(AgentSession s){
            try{s.pending.execute();s.note("completed",s.pending.name());}
            catch(RuntimeException e){s.note("step-error",s.pending.name()+": "+e.getMessage());}
            s.remaining.remove(s.pending);s.setState(s.pending.name().equals("checklist")?new FinishedState():new PlanningState());
        }
    }
    public static final class FinishedState implements AgentState {public void advance(AgentSession s){s.note("finished",s.stopped?"Stopped by user":"Finished");}}
}
