package career;

import java.util.*;
import static career.Models.*;

public final class InterviewPrepService {
    private final Ai ai;private final SqliteJobRepository repository;
    public InterviewPrepService(Ai ai,SqliteStore store){this.ai=ai;this.repository=new SqliteJobRepository(store);}
    public List<InterviewQuestion> prepare(JobDescription job,ResumeProfile profile,List<MemoryFact> memory){
        CompanySummary company=repository.findCompany(job.id());
        String prompt=ai.text("Create tailored instructions for generating five varied interview questions. Use the job, profile and company evidence; do not answer the questions.",Map.of("job",job,"profile",profile,"company",company==null?"not researched":company,"history",memory),ModelTier.SONNET);
        List<InterviewQuestion> questions=ai.ask("Generate exactly five distinct, nonempty interview questions. Return {\"questions\":[\"...\"]}. Tailoring instructions: "+prompt,Map.of("job",job,"profile",profile),ModelTier.HAIKU,m->{
            List<String> q=Json.strings(m,"questions");if(q.size()!=5||q.stream().map(Models::normalize).distinct().count()!=5)throw new IllegalArgumentException("Expected five unique questions");
            return q.stream().map(s->new InterviewQuestion(id(),job.id(),required(s,"Question"),"")).toList();
        });
        repository.saveQuestions(job.id(),questions);return questions;
    }
}
