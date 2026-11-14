package career;

import java.util.*;
import static career.Models.*;

public final class MockInterviewService {
    private final Ai ai;private final SqliteJobRepository repository;private final MemoryManager memory;
    public MockInterviewService(Ai ai,SqliteStore store,MemoryManager memory){this.ai=ai;this.repository=new SqliteJobRepository(store);this.memory=memory;}
    public InterviewSession start(String jobId,boolean restart){
        InterviewSession existing=load(jobId);if(existing!=null&&!restart)return existing;
        var q=repository.findQuestions(jobId);if(q==null)throw new IllegalStateException("Generate interview questions first");
        InterviewSession session=new InterviewSession(jobId,q.questions(),new LinkedHashMap<>(),new LinkedHashMap<>(),new LinkedHashMap<>(),new HashSet<>(),false);if(restart)repository.restartInterview(session);else save(session);return session;
    }
    public InterviewSession load(String jobId){return repository.findInterview(jobId);}
    public String nextQuestion(InterviewSession s){
        if(s.finished())return "Interview ended. Request the final feedback report.";
        for(var q:s.questions()){
            if(!s.answers().containsKey(q.id()))return q.text();
            if(!s.decided().contains(q.id())){
                var decision=ai.decide("Would one clarifying follow-up help? Question: "+q.text()+" Answer: "+s.answers().get(q.id()),List.of("followup","next"));
                if(decision.choice().equals("followup"))s.followups().put(q.id(),ai.text("Write one concise clarification question, no feedback.",Map.of("question",q.text(),"answer",s.answers().get(q.id())),ModelTier.HAIKU));
                s.decided().add(q.id());save(s);
            }
            if(s.followups().containsKey(q.id())&&!s.followupAnswers().containsKey(q.id()))return s.followups().get(q.id());
        }
        save(new InterviewSession(s.jobId(),s.questions(),s.answers(),s.followups(),s.followupAnswers(),s.decided(),true));return "Interview complete. Request final feedback.";
    }
    public String answer(String jobId,String answer){
        InterviewSession s=load(jobId);if(s==null||s.finished())throw new IllegalStateException("Start an interview first");answer=required(answer,"Answer");
        for(var q:s.questions()){
            if(!s.answers().containsKey(q.id())){s.answers().put(q.id(),answer);s.questions().set(s.questions().indexOf(q),q.answered(answer));save(s);return nextQuestion(s);}
            if(!s.decided().contains(q.id()))throw new IllegalStateException("Continue interview to resolve pending follow-up before answering");
            if(s.followups().containsKey(q.id())&&!s.followupAnswers().containsKey(q.id())){s.followupAnswers().put(q.id(),answer);save(s);return nextQuestion(s);}
        }throw new IllegalStateException("All questions answered");
    }
    public void stop(String jobId){InterviewSession s=load(jobId);if(s==null)throw new IllegalStateException("No saved interview");save(s);}
    public FeedbackReport feedback(String jobId,boolean partialApproved){
        InterviewSession s=load(jobId);if(s==null||s.answers().isEmpty())throw new IllegalStateException("No answers to evaluate");
        if(!s.finished()&&!partialApproved)throw new IllegalStateException("Feedback withheld until completion; stop and approve partial feedback");
        FeedbackReport report=ai.ask("Evaluate ONLY answered interview questions and any answered follow-ups. Return {\"perAnswer\":[\"one feedback string per answered base question in order\"],\"summary\":\"...\",\"weaknesses\":[\"...\"]}. Do not treat unattempted questions as failures.",s,ModelTier.SONNET,m->{
            var per=Json.strings(m,"perAnswer");if(per.size()!=s.answers().size())throw new IllegalArgumentException("Feedback count does not match answers");return new FeedbackReport(jobId,per,Json.text(m,"summary"),Json.strings(m,"weaknesses"));
        });
        repository.save(report);for(String w:report.weaknesses())memory.save("weaknesses",w);return report;
    }
    private void save(InterviewSession s){repository.save(s);}
}
