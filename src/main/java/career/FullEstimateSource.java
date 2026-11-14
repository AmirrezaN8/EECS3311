package career;

public final class FullEstimateSource implements JobDescriptionSource {
    private final Ai ai;
    public FullEstimateSource(Ai ai){this.ai=ai;}
    public SourceLabel label(){return SourceLabel.FULL_ESTIMATE;}
    public boolean needsApproval(){return true;}
    public JobDescription fetch(JobDescriptionRequest request){
        return request.description(ai.text("Write an explicitly ESTIMATED job description for the supplied title, company and country, not an actual posting. Do not invent a deadline.",request,ModelTier.SONNET),label());
    }
}
