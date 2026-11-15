package career;

import java.util.*;
import static career.Models.*;

/** Owns job selection and all job-linked preparation/interview results. */
public final class SqliteJobRepository implements Repository<JobDescription> {
    private final SqliteStore store;
    private static final List<String> RESULT_KINDS=List.of("gap","company","questions","interview","feedback","checklist");
    public SqliteJobRepository(SqliteStore store){this.store=store;}
    @Override public void save(JobDescription job){Documents.save(store,"job",job.id(),job);}
    public void saveAndSelect(JobDescription job){store.saveAll(List.of(new SqliteStore.Write("job",job.id(),Documents.encode(job)),new SqliteStore.Write("settings","job",Map.of("id",job.id()))));}
    @Override public JobDescription findById(String id){return Documents.read(store,"job",required(id,"Job ID"),JobDescription.class);}
    @Override public List<JobDescription> findAll(){return Documents.all(store,"job",JobDescription.class);}
    public JobDescription require(String id){JobDescription job=findById(id);if(job==null)throw new IllegalArgumentException("Unknown job ID");return job;}
    public void select(String id){synchronized(store){require(id);store.save("settings","job",Map.of("id",id));}}
    public JobDescription current(){var selected=store.find("settings","job");if(selected==null)throw new IllegalStateException("Add or find a job first");JobDescription job=findById(Json.text(selected,"id"));if(job==null)throw new IllegalStateException("Selected job no longer exists; select another saved job");return job;}
    @Override public void delete(String id){synchronized(store){
        require(id);
        boolean output=RESULT_KINDS.stream().anyMatch(kind->store.find(kind,id)!=null);
        boolean linked=store.all("version").stream().anyMatch(v->id.equals(v.get("jobId")))||store.all("interview-archive").stream().anyMatch(v->id.equals(v.get("jobId")))||store.all("trace").stream().anyMatch(v->id.equals(v.get("jobId")));
        if(output||linked)throw new IllegalStateException("Job has saved preparation work; deletion would orphan it");
        Map<String,String> keys=new LinkedHashMap<>();keys.put("job",id);var selected=store.find("settings","job");if(selected!=null&&id.equals(selected.get("id")))keys.put("settings","job");store.deleteAll(keys);
    }}
    public void save(SkillGapReport report){synchronized(store){require(report.jobId());if(report.assessments().stream().map(a->normalize(a.skillName())).distinct().count()!=report.assessments().size())throw new IllegalArgumentException("Duplicate skill assessments");Documents.save(store,"gap",report.jobId(),report);}}
    public SkillGapReport findSkillGap(String jobId){return Documents.read(store,"gap",jobId,SkillGapReport.class);}
    public void save(CompanySummary summary){synchronized(store){require(summary.jobId());if(summary.sources().isEmpty())throw new IllegalArgumentException("Company summary requires evidence sources");for(var source:summary.sources()){var uri=java.net.URI.create(required(source.url(),"Source URL"));if(!Set.of("https","http").contains(uri.getScheme())||uri.getHost()==null)throw new IllegalArgumentException("Invalid research source URL");}Documents.save(store,"company",summary.jobId(),summary);}}
    public CompanySummary findCompany(String jobId){return Documents.read(store,"company",jobId,CompanySummary.class);}
    public void saveQuestions(String jobId,List<InterviewQuestion> questions){synchronized(store){
        require(jobId);if(questions.isEmpty()||questions.stream().map(InterviewQuestion::id).distinct().count()!=questions.size())throw new IllegalArgumentException("Questions require unique IDs");
        for(var q:questions)if(!q.jobId().equals(jobId)||!q.answer().isEmpty())throw new IllegalArgumentException("Prepared question must belong to this job and be unanswered");
        Documents.save(store,"questions",jobId,new InterviewQuestionSet(jobId,questions));
    }}
    public InterviewQuestionSet findQuestions(String jobId){
        var set=Documents.read(store,"questions",jobId,InterviewQuestionSet.class);if(set==null)return null;
        if(!set.jobId().equals(jobId))throw new IllegalArgumentException("Stored question set belongs to another job");
        List<InterviewQuestion> linked=set.questions().stream().map(q->{if(!q.jobId().isEmpty()&&!q.jobId().equals(jobId))throw new IllegalArgumentException("Stored question belongs to another job");return new InterviewQuestion(q.id(),jobId,q.text(),q.answer());}).toList();
        return new InterviewQuestionSet(set.jobId(),linked);
    }
    public void save(InterviewSession session){synchronized(store){
        require(session.jobId());validateInterview(session);
        Documents.save(store,"interview",session.jobId(),session.copy());
    }}
    private static void validateInterview(InterviewSession s){
        Set<String> ids=new HashSet<>();for(var q:s.questions()){if(!ids.add(q.id())||!q.jobId().equals(s.jobId()))throw new IllegalArgumentException("Invalid interview question relationship");}
        if(!ids.containsAll(s.answers().keySet())||!s.answers().keySet().containsAll(s.followups().keySet())||!s.followups().keySet().containsAll(s.followupAnswers().keySet())||!s.answers().keySet().containsAll(s.decided()))throw new IllegalArgumentException("Interview progress references an unknown or unanswered question");
        for(String answer:s.answers().values())required(answer,"Answer");for(String answer:s.followupAnswers().values())required(answer,"Follow-up answer");
    }
    public InterviewSession findInterview(String jobId){return Documents.read(store,"interview",jobId,InterviewSession.class);}
    public void restartInterview(InterviewSession session){synchronized(store){
        require(session.jobId());validateInterview(session);List<SqliteStore.Write> writes=new ArrayList<>();var old=findInterview(session.jobId());
        if(old!=null){String archiveId=id();writes.add(new SqliteStore.Write("interview-archive",archiveId,Documents.encode(old)));var feedback=findFeedback(session.jobId());if(feedback!=null)writes.add(new SqliteStore.Write("feedback-archive",archiveId,Documents.encode(feedback)));}
        writes.add(new SqliteStore.Write("interview",session.jobId(),Documents.encode(session)));store.saveAll(writes,Map.of("feedback",session.jobId()));
    }}
    public void save(FeedbackReport report){synchronized(store){require(report.jobId());var session=findInterview(report.jobId());if(session==null||session.answers().isEmpty()||report.perAnswerFeedback().size()!=session.answers().size())throw new IllegalArgumentException("Feedback must match a saved answered interview");Documents.save(store,"feedback",report.jobId(),report);}}
    public FeedbackReport findFeedback(String jobId){return Documents.read(store,"feedback",jobId,FeedbackReport.class);}
    public void saveTrace(String traceId,Map<String,Object> trace){synchronized(store){if(!(trace.get("jobId") instanceof String jobId))throw new IllegalArgumentException("Trace job ID must be text");if(!jobId.isBlank())require(jobId);store.save("trace",required(traceId,"Trace ID"),trace);}}
    public boolean hasResult(String jobId,String kind){if(!RESULT_KINDS.contains(kind))throw new IllegalArgumentException("Unknown result kind");return store.find(kind,jobId)!=null;}
    public Map<String,Object> results(String jobId){require(jobId);Map<String,Object> out=new LinkedHashMap<>();
        Object[] values={findSkillGap(jobId),findCompany(jobId),findQuestions(jobId),findInterview(jobId),findFeedback(jobId),Documents.read(store,"checklist",jobId,Checklist.class)};
        for(int i=0;i<values.length;i++)if(values[i]!=null)out.put(RESULT_KINDS.get(i),values[i]);return out;
    }
}
