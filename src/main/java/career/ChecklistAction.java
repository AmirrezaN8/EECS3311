package career;

import java.util.function.Supplier;

public final class ChecklistAction implements AgentAction {
    private final ChecklistService service;private final Supplier<JobDescription> job;
    public ChecklistAction(ChecklistService service,Supplier<JobDescription> job){this.service=service;this.job=job;}
    public String name(){return "checklist";}
    public String describe(){return "build prioritized preparation checklist";}
    public Checklist execute(){return service.build(job.get());}
}
