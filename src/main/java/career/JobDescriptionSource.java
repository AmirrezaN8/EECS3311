package career;

public interface JobDescriptionSource {
    SourceLabel label();
    boolean needsApproval();
    JobDescription fetch(JobDescriptionRequest request);
}
