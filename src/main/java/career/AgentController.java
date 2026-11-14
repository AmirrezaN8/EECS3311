package career;

import java.nio.file.*;
import java.util.*;
import static career.Models.*;

/** Shared facade used by both entry points; all business behavior stays behind this boundary. */
public final class AgentController {
    public interface UserInteraction extends ApprovalGate.ApprovalPrompter {String readText(String prompt);}
    private final SqliteJobRepository jobs;private final SqliteResumeRepository resumeRepository;private final SqliteChecklistRepository checklistRepository;private final Ai ai;private final ApprovalGate gate;private final UserInteraction ui;
    public final AgentEventPublisher events;
    private final ResumeImporter resumes;private final JobDescriptionIntake intake;private final MemoryManager memory;
    private final SkillGapService gaps;private final ResumeTailorService tailor;private final CompanyResearchService company;
    private final InterviewPrepService prep;private final MockInterviewService interview;private final ChecklistService checklist;
    private volatile boolean cancelled;
    public AgentController(Config c,UserInteraction ui){this(new SqliteStore(c.data()),new Ai(new ClaudeAdapter(new AnthropicApi(c)),new JevAdapter(new JevApi(c))),new AdzunaTool(c,new HttpTransport()),new WebSearchTool(c,new HttpTransport()),ui,new AgentEventPublisher());}
    public AgentController(SqliteStore store,Ai ai,AdzunaTool adzuna,WebSearchTool search,UserInteraction ui,AgentEventPublisher events){
        this.jobs=new SqliteJobRepository(store);this.resumeRepository=new SqliteResumeRepository(store);this.checklistRepository=new SqliteChecklistRepository(store);this.ai=ai;this.ui=ui;this.events=events;gate=new ApprovalGate(ui);
        resumes=new ResumeImporter(ai,store);intake=new JobDescriptionIntake(ai,adzuna,store,gate);memory=new MemoryManager(ai,store,gate,events);
        gaps=new SkillGapService(ai,store);tailor=new ResumeTailorService(ai,store,gate);company=new CompanyResearchService(ai,search,store);
        prep=new InterviewPrepService(ai,store);interview=new MockInterviewService(ai,store,memory);checklist=new ChecklistService(ai,store);
    }
    public JobDescription currentJob(){return jobs.current();}
    public void stop(){cancelled=true;events.publish("stop","Stop requested; completed work remains saved");}
    private Map<String,AgentAction> actions(){
        Map<String,AgentAction> map=new LinkedHashMap<>();
        map.put("gap",new AgentAction.FeatureCommand("gap","identify skill gaps",()->gaps.identify(currentJob(),resumes.current())));
        map.put("tailor",new AgentAction.FeatureCommand("tailor","propose and review evidence-preserving resume edits",()->tailor.tailor(currentJob(),resumes.current())));
        map.put("company",new AgentAction.FeatureCommand("company","research sourced company goals and values",()->company.research(currentJob())));
        map.put("interview prep",new AgentAction.FeatureCommand("interview prep","generate five tailored interview questions",()->prep.prepare(currentJob(),resumes.current(),memory.loadFacts())));
        map.put("checklist build",new AgentAction.FeatureCommand("checklist","build prioritized preparation checklist",()->checklist.build(currentJob())));
        return map;
    }
    public Object runFeature(String command,List<String> args){
        command=command.strip();Map<String,AgentAction> commands=actions();
        if(commands.containsKey(command)){
            if(!args.isEmpty())throw new IllegalArgumentException("This command takes no arguments");
            Object result=commands.get(command).execute();events.publish("completed",command);
            if(command.equals("interview prep")&&gate.review("Questions saved. Start a typed mock interview now?",false))return Map.of("questions",result,"interview",startMockInterview(false));
            if(command.equals("checklist build"))offerMissingSteps();return result;
        }
        return switch(command){
            case "job add" -> {count(args,4);yield intake.add(args.get(0),args.get(1),args.get(2),readInput(args.get(3)));}
            case "job find" -> {count(args,3);yield intake.find(args.get(0),args.get(1),args.get(2));}
            case "job show" -> {count(args,0);yield currentJob();}
            case "job list" -> {count(args,0);yield jobs.findAll();}
            case "job select" -> {count(args,1);jobs.select(args.getFirst());yield currentJob();}
            case "resume import" -> {count(args,1);yield resumes.importResume(Path.of(args.getFirst()));}
            case "resume paste" -> {count(args,1);yield resumes.importText(readInput(args.getFirst()));}
            case "resume show" -> {count(args,0);yield resumes.current();}
            case "resume versions" -> {count(args,0);yield resumeRepository.findVersions(currentJob().id());}
            case "history show" -> {count(args,0);yield memory.loadFacts();}
            case "history add" -> {count(args,2);yield memory.save(args.get(0),args.get(1));}
            case "history edit" -> {count(args,2);memory.editFact(args.get(0),args.get(1));yield "Fact updated";}
            case "history delete" -> {count(args,1);if(!gate.review("Delete this saved fact? "+args.getFirst(),false))yield "Deletion cancelled";memory.delete(args.getFirst());yield "Fact deleted";}
            case "chat" -> {count(args,1);memory.recordTurn(args.getFirst());yield "Message processed for useful career facts; see history and any memory warning.";}
            case "interview start" -> {count(args,0);yield startMockInterview(false);}
            case "interview restart" -> {count(args,0);if(!gate.review("Archive current interview and start a new attempt?",false))yield "Cancelled";yield startMockInterview(true);}
            case "interview answer" -> {count(args,1);yield interview.answer(currentJob().id(),args.getFirst());}
            case "interview stop" -> {count(args,0);interview.stop(currentJob().id());if(gate.review("Progress saved. Generate feedback for answered questions?",false))yield interview.feedback(currentJob().id(),true);yield "Stopped; saved answers remain available";}
            case "interview feedback" -> {count(args,0);var s=interview.load(currentJob().id());boolean partial=s!=null&&!s.finished()&&gate.review("End this interview and evaluate only answered questions?",false);if(partial)interview.stop(currentJob().id());yield interview.feedback(currentJob().id(),partial);}
            case "checklist show" -> {count(args,0);var list=checklistRepository.findById(currentJob().id());if(list==null)throw new IllegalStateException("Build checklist first");yield list;}
            case "checklist tick" -> {if(args.size()<1||args.size()>2)throw new IllegalArgumentException("Use checklist tick ITEM_ID [true|false]");if(args.size()==2&&!Set.of("true","false").contains(args.get(1)))throw new IllegalArgumentException("Use true or false");yield checklist.tick(currentJob().id(),args.getFirst(),args.size()==1||Boolean.parseBoolean(args.get(1)));}
            case "prepare" -> {if(args.size()>1)throw new IllegalArgumentException("Use prepare [goal]");yield handleGoal(args.isEmpty()?"prepare me for this job":args.getFirst());}
            case "results" -> {count(args,0);yield jobs.results(currentJob().id());}
            default -> throw new IllegalArgumentException("Unknown command: "+command+". Use help.");
        };
    }
    public String startMockInterview(boolean restart){return interview.nextQuestion(interview.start(currentJob().id(),restart));}
    public List<String> handleGoal(String goal){
        goal=required(goal,"Goal");cancelled=false;memory.recordTurn(goal);
        try{currentJob();}catch(IllegalStateException e){
            if(!gate.review("No selected job. Paste a job description now?",false))throw e;
            intake.add(required(ui.readText("Job title"),"Title"),required(ui.readText("Company"),"Company"),required(ui.readText("Country (CA or US)"),"Country"),required(ui.readText("Paste job description"),"Job description"));
        }
        try{resumes.current();}catch(IllegalStateException e){
            if(!gate.review("No resume saved. Paste it now?",false))throw e;
            resumes.importText(required(ui.readText("Paste resume text"),"Resume"));
        }
        if(memory.loadFacts().isEmpty()){
            String fact=ui.readText("No history yet. Optionally describe a target role or weakness (blank to skip)");if(fact!=null&&!fact.isBlank())memory.recordTurn(fact);
        }
        JobDescription job=currentJob();List<AgentAction> planned=new ArrayList<>(actions().values());
        String requestedGoal=goal;
        AgentSession session=new AgentSession(new Planner(ai),gate,events,planned,()->"Goal: "+requestedGoal+" Job: "+Json.write(Documents.encode(job))+" Resume: "+Json.write(Documents.encode(resumes.current()))+" Memory: "+Json.write(Documents.encode(memory.loadFacts()))+" Saved preparation results: "+Json.write(Documents.encode(runFeature("results",List.of()))),()->cancelled);
        List<String> trace=session.handleInput();boolean interrupted=Thread.interrupted();
        try{jobs.saveTrace(id(),Map.of("at",java.time.Instant.now().toString(),"jobId",job.id(),"goal",goal,"steps",trace));}
        finally{if(interrupted)Thread.currentThread().interrupt();}
        return trace;
    }
    private void offerMissingSteps(){
        String job=currentJob().id();Map<String,String> related=Map.of("gap","gap","company","company","questions","interview prep");
        for(var entry:related.entrySet())if(!jobs.hasResult(job,entry.getKey())&&gate.review("Partial checklist: run missing feature '"+entry.getValue()+"' now?",false))try{runFeature(entry.getValue(),List.of());}catch(RuntimeException e){events.publish("step-error",e.getMessage());}
    }
    private static void count(List<String> args,int count){if(args.size()!=count)throw new IllegalArgumentException("Expected "+count+" argument(s); quote text or use @path for job/resume text");}
    private static String readInput(String arg){if(!arg.startsWith("@"))return arg;try{Path p=Path.of(arg.substring(1));if(Files.size(p)>400_000)throw new IllegalArgumentException("Input text file too large");return Files.readString(p);}catch(java.io.IOException e){throw new IllegalArgumentException("Cannot read text file",e);}}
}
