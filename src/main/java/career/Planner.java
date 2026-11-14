package career;

import java.util.*;

public final class Planner {
    private final DecisionClient decisions;
    public Planner(DecisionClient decisions){this.decisions=decisions;}
    public AgentAction chooseNext(String context,List<AgentAction> actions){
        if(actions.isEmpty())return null;
        String chosen=decisions.decide("Choose the next job preparation step. "+context+" Available actions: "+actions.stream().map(a->a.name()+": "+a.describe()).toList(),actions.stream().map(AgentAction::name).toList()).choice();
        return actions.stream().filter(a->a.name().equals(chosen)).findFirst().orElseThrow();
    }
}
