package career;

import java.util.function.Supplier;

public final class CompanyResearchAction implements AgentAction {
    private final CompanyResearchService service;private final Supplier<JobDescription> job;
    public CompanyResearchAction(CompanyResearchService service,Supplier<JobDescription> job){this.service=service;this.job=job;}
    public String name(){return "company";}
    public String describe(){return "research sourced company goals and values";}
    public CompanySummary execute(){return service.research(job.get());}
}
