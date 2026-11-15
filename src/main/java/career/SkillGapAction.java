package career;

import java.util.function.Supplier;

public final class SkillGapAction implements AgentAction {
    private final SkillGapService service;private final Supplier<JobDescription> job;private final Supplier<ResumeProfile> resume;
    public SkillGapAction(SkillGapService service,Supplier<JobDescription> job,Supplier<ResumeProfile> resume){this.service=service;this.job=job;this.resume=resume;}
    public String name(){return "gap";}
    public String describe(){return "identify skill gaps";}
    public SkillGapReport execute(){return service.identify(job.get(),resume.get());}
}
