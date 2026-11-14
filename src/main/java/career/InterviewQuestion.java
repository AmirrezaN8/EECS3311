package career;

import java.util.Objects;

public record InterviewQuestion(String id, String jobId, String text, String answer) {
    public InterviewQuestion { id=Models.required(id,"Question ID");Objects.requireNonNull(jobId);text=Models.required(text,"Question");Objects.requireNonNull(answer); }
    public InterviewQuestion answered(String value){return new InterviewQuestion(id,jobId,text,Models.required(value,"Answer"));}
}
