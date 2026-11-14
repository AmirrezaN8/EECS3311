package career;

import java.time.Instant;
import java.util.*;

public record ResumeVersion(String id, String jobId, String baseResumeId, Instant createdAt,
                            String text, List<ResumeChange> changes) {
    public ResumeVersion {
        id=Models.required(id,"Version ID");jobId=Models.required(jobId,"Job ID");baseResumeId=Models.required(baseResumeId,"Base resume ID");
        text=Models.required(text,"Version text");changes=List.copyOf(changes);
    }
}
