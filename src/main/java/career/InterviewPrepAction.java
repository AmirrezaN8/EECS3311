package career;

import java.util.List;
import java.util.function.Supplier;

public final class InterviewPrepAction implements AgentAction {
    private final InterviewPrepService service;private final Supplier<JobDescription> job;private final Supplier<ResumeProfile> resume;private final Supplier<List<MemoryFact>> memory;
    public InterviewPrepAction(InterviewPrepService service,Supplier<JobDescription> job,Supplier<ResumeProfile> resume,Supplier<List<MemoryFact>> memory){this.service=service;this.job=job;this.resume=resume;this.memory=memory;}
    public String name(){return "interview prep";}
    public String describe(){return "generate five tailored interview questions";}
    public List<InterviewQuestion> execute(){return service.prepare(job.get(),resume.get(),memory.get());}
}
