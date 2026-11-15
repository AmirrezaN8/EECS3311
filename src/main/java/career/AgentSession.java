package career;

import java.util.*;

/** State objects own the workflow transitions; approval/execution cannot occur in the wrong stage. */
public final class AgentSession {
    public interface AgentState {
        void onInput(AgentSession s);
        default void onApproval(AgentSession s,boolean approved){throw new IllegalStateException("No action is awaiting approval");}
    }
    private AgentState state=new IdleState();
    private final Planner planner;private final ApprovalGate gate;private final AgentEventPublisher events;
    private final List<AgentAction> remaining;private final List<String> trace=new ArrayList<>();private final java.util.function.Supplier<String> context;
    private final java.util.function.BooleanSupplier cancelled;
    private AgentAction pending;private int steps;private String outcome="Finished";
    private final java.util.function.Predicate<AgentAction> requested;
    public AgentSession(Planner planner,ApprovalGate gate,AgentEventPublisher events,List<AgentAction> actions,String context,java.util.function.BooleanSupplier cancelled){this(planner,gate,events,actions,()->context,cancelled);}
    public AgentSession(Planner planner,ApprovalGate gate,AgentEventPublisher events,List<AgentAction> actions,java.util.function.Supplier<String> context,java.util.function.BooleanSupplier cancelled){this(planner,gate,events,actions,context,cancelled,action->false);}
    public AgentSession(Planner planner,ApprovalGate gate,AgentEventPublisher events,List<AgentAction> actions,java.util.function.Supplier<String> context,java.util.function.BooleanSupplier cancelled,java.util.function.Predicate<AgentAction> requested){this.planner=planner;this.gate=gate;this.events=events;this.remaining=new ArrayList<>(actions);this.context=context;this.cancelled=cancelled;this.requested=requested;}
    public List<String> handleInput(){while(!(state instanceof FinishedState)){if(cancelled.getAsBoolean()||Thread.currentThread().isInterrupted()){outcome="Stopped by user";setState(new FinishedState());break;}state.onInput(this);}if(cancelled.getAsBoolean()||Thread.currentThread().isInterrupted())outcome="Stopped by user";state.onInput(this);return List.copyOf(trace);}
    public void handleApproval(boolean approved){state.onApproval(this,approved);}
    void setState(AgentState next){state=next;note("state",next.getClass().getSimpleName());}
    private void note(String type,String text){trace.add(type+": "+text);events.publish(type,text);}
    public static final class IdleState implements AgentState {public void onInput(AgentSession s){s.setState(new PlanningState());}}
    public static final class PlanningState implements AgentState {
        public void onInput(AgentSession s){
            if(s.remaining.isEmpty()||s.steps>=10){s.outcome=s.steps>=10?"Step limit reached (10)":"No remaining steps";s.setState(new FinishedState());return;}
            try{s.pending=s.planner.chooseNext(s.context.get()+" Trace: "+s.trace,List.copyOf(s.remaining));}
            catch(RuntimeException e){s.outcome="Planner failed; remaining work skipped";s.note("planner-error",e.getMessage()+"; remaining actions not executed");s.setState(new FinishedState());return;}
            if(s.pending==null){s.note("decision","Planner chose done");s.outcome="Planner chose done";s.setState(new FinishedState());return;}
            s.steps++;s.note("decision","Selected "+s.pending.name());
            boolean requested=false;
            try{requested=s.requested.test(s.pending);}catch(RuntimeException e){s.note("approval-warning","Could not establish explicit request; asking approval: "+e.getMessage());}
            if(requested){s.note("requested",s.pending.name());s.setState(new ExecutingState());}else s.setState(new AwaitingApprovalState());
        }
    }
    public static final class AwaitingApprovalState implements AgentState {
        public void onInput(AgentSession s){
            s.handleApproval(s.gate.review("Agent proposes: "+s.pending.describe()+". Run this step?",false));
        }
        @Override public void onApproval(AgentSession s,boolean approved){
            if(approved)s.setState(new ExecutingState());
            else{s.note("declined",s.pending.name());s.remaining.remove(s.pending);s.setState(new PlanningState());}
        }
    }
    public static final class ExecutingState implements AgentState {
        public void onInput(AgentSession s){
            ActionResult result;
            try{Object value=s.pending.execute();if(value instanceof ResumeProfile profile&&!profile.warning().isBlank())s.note("warning",profile.warning());result=new ActionResult(true,s.pending.name());}
            catch(RuntimeException e){result=new ActionResult(false,s.pending.name()+": "+e.getMessage());}
            s.note(result.success()?"completed":"step-error",result.message());s.remaining.remove(s.pending);
            boolean complete=result.success()&&s.pending.name().equals("checklist");if(complete)s.outcome="Checklist produced";
            s.setState(complete?new FinishedState():new PlanningState());
        }
    }
    public static final class FinishedState implements AgentState {public void onInput(AgentSession s){s.note("finished",s.outcome);}}
}
