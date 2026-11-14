package career;

public final class PastedTextSource implements JobDescriptionSource {
    public SourceLabel label(){return SourceLabel.USER_PASTED;}
    public boolean needsApproval(){return false;}
    public JobDescription fetch(JobDescriptionRequest request){return request.description(request.text(),label());}
}
