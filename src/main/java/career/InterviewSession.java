package career;

import java.util.*;

public record InterviewSession(String jobId, List<InterviewQuestion> questions, Map<String,String> answers,
                               Map<String,String> followups, Map<String,String> followupAnswers,
                               Set<String> decided, boolean finished) {
    public InterviewSession {
        jobId=Models.required(jobId,"Job ID");
        List<InterviewQuestion> linked=new ArrayList<>();
        for(var q:questions) {
            if(!q.jobId().isEmpty()&&!q.jobId().equals(jobId))throw new IllegalArgumentException("Interview question belongs to another job");
            String answer=answers.getOrDefault(q.id(),q.answer());
            if(!q.answer().isEmpty()&&!q.answer().equals(answer))throw new IllegalArgumentException("Conflicting interview answer records");
            linked.add(new InterviewQuestion(q.id(),jobId,q.text(),answer));
        }
        questions=linked;answers=new LinkedHashMap<>(answers);followups=new LinkedHashMap<>(followups);
        followupAnswers=new LinkedHashMap<>(followupAnswers);decided=new HashSet<>(decided);
    }
    public InterviewSession copy(){return new InterviewSession(jobId,questions,answers,followups,followupAnswers,decided,finished);}
}
