package career;

public final class ExpandedEstimateSource implements JobDescriptionSource {
    private final Ai ai;
    public ExpandedEstimateSource(Ai ai){this.ai=ai;}
    public SourceLabel label(){return SourceLabel.EXPANDED_ESTIMATE;}
    public boolean needsApproval(){return true;}
    public JobDescription fetch(JobDescriptionRequest request){
        return request.description(ai.text("Expand the partial excerpt into an explicitly ESTIMATED job description for the supplied title, company and country. Do not invent a deadline or claim this is official.",request,ModelTier.SONNET),label());
    }
}
