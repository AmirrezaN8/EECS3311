package career;

import java.util.*;

public record FeedbackReport(String jobId, List<String> perAnswerFeedback, String summary, List<String> weaknesses) {
    public FeedbackReport { jobId=Models.required(jobId,"Job ID");perAnswerFeedback=List.copyOf(perAnswerFeedback);summary=Models.required(summary,"Feedback summary");weaknesses=List.copyOf(weaknesses); }
}
