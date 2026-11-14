package career;

import java.util.Objects;

public final class GetJobDescriptionAction implements AgentAction {
    private final JobDescriptionIntake intake;private final JobDescriptionRequest request;
    public GetJobDescriptionAction(JobDescriptionIntake intake,JobDescriptionRequest request){this.intake=Objects.requireNonNull(intake);this.request=Objects.requireNonNull(request);}
    public String name(){return "get description";}
    public String describe(){return "get the description for "+request.title()+" at "+request.company();}
    public JobDescription execute(){return intake.getDescription(request);}
}
