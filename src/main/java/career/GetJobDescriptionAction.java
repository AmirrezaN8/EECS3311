package career;

import java.util.Objects;

public final class GetJobDescriptionAction implements AgentAction {
    private final JobDescriptionIntake intake;private final java.util.function.Supplier<JobDescriptionRequest> request;
    public GetJobDescriptionAction(JobDescriptionIntake intake,JobDescriptionRequest request){this.intake=Objects.requireNonNull(intake);Objects.requireNonNull(request);this.request=()->request;}
    public GetJobDescriptionAction(JobDescriptionIntake intake,java.util.function.Supplier<JobDescriptionRequest> request){this.intake=Objects.requireNonNull(intake);this.request=Objects.requireNonNull(request);}
    public String name(){return "get description";}
    public String describe(){return "get and save a job description";}
    public JobDescription execute(){return intake.getDescription(request.get());}
}
