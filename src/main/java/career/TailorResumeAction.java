package career;

import java.util.function.Supplier;

public final class TailorResumeAction implements AgentAction {
    private final ResumeTailorService service;private final Supplier<JobDescription> job;private final Supplier<ResumeProfile> resume;
    public TailorResumeAction(ResumeTailorService service,Supplier<JobDescription> job,Supplier<ResumeProfile> resume){this.service=service;this.job=job;this.resume=resume;}
    public String name(){return "tailor";}
    public String describe(){return "propose and review evidence-preserving resume edits";}
    public ResumeVersion execute(){return service.tailor(job.get(),resume.get());}
}
