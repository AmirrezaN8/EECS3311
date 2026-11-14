package career;

import java.util.*;

public record InterviewQuestionSet(String jobId, List<InterviewQuestion> questions) {
    public InterviewQuestionSet { jobId=Models.required(jobId,"Job ID");questions=List.copyOf(questions); }
}
