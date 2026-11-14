package career;

import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** Runs only against disposable SQLite fixtures; never opens the user's database or calls an API. */
public final class RepositoryTest {
    private static int checks;
    private static final Instant NOW=Instant.parse("2026-11-14T12:00:00Z");
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static void fails(Runnable operation,String message){boolean failed=false;try{operation.run();}catch(RuntimeException e){failed=true;}check(failed,message);}
    private static JobDescription job(String id){return new JobDescription(id,"Developer","Acme","CA","Java SQL\nApply by: 2026-12-01",SourceLabel.USER_PASTED,"",LocalDate.of(2026,12,1));}
    private static ResumeProfile profile(String id){return new ResumeProfile(id,"Developed Java applications",List.of(new Skill("Java","UNSPECIFIED")),List.of("BSc"),List.of("Project work"),List.of(),List.of(),"");}
    private static Checklist checklist(String id,String jobId){return new Checklist(id,jobId,LocalDate.of(2026,12,1),List.of(new ChecklistItem("item","Practice SQL",1,false)));}
    public static int run()throws Exception {
        checks=0;Path root=Files.createTempDirectory("career-repository-tests-");
        try{crud(root.resolve("crud.db"));relationships(root.resolve("relations.db"));legacy(root.resolve("legacy.db"));atomicFailure(root.resolve("atomic.db"));restart(root.resolve("restart.db"));}
        finally{try(var paths=Files.walk(root)){for(Path p:paths.sorted(Comparator.reverseOrder()).toList())Files.deleteIfExists(p);}}
        System.out.println("PASS: "+checks+" domain/repository assertions (temporary SQLite databases only).");return checks;
    }
    private static void crud(Path path){
        SqliteStore store=new SqliteStore(path);SqliteJobRepository jobs=new SqliteJobRepository(store);
        Repository<JobDescription> contract=jobs;contract.save(job("job"));check(contract.findById("job").equals(job("job")),"job CRUD contract");
        jobs.saveAndSelect(job("selected"));check(jobs.current().id().equals("selected"),"job selected atomically");
        jobs.select("job");check(jobs.current().id().equals("job")&&jobs.findAll().size()==2,"saved job selection and listing");
        fails(()->jobs.select("absent"),"invalid selection rejected");check(jobs.current().id().equals("job"),"failed selection preserves current job");
        JobDescription updated=new JobDescription("job","Senior Developer","Acme","CA",job("job").text(),SourceLabel.USER_PASTED,"",job("job").deadline());jobs.save(updated);check(jobs.findById("job").title().equals("Senior Developer"),"job update");
        jobs.delete("selected");check(jobs.findById("selected")==null&&jobs.current().id().equals("job"),"delete unselected job preserves selected job");
        jobs.delete("job");check(jobs.findAll().isEmpty(),"job deleted");fails(jobs::current,"deleting selected job clears pointer");fails(()->jobs.delete("absent"),"unknown job deletion rejected");
        var resumes=new SqliteResumeRepository(store);Repository<ResumeProfile> resumeContract=resumes;resumeContract.save(profile("base"));resumes.saveAndSelect(profile("second"));check(resumes.current().id().equals("second")&&resumes.findAll().size()==2,"resume CRUD and selection");
        ResumeProfile altered=new ResumeProfile("base","Invented Python experience",List.of(),List.of(),List.of(),List.of(),List.of(),"");fails(()->resumes.save(altered),"base profile overwrite rejected");check(resumes.findById("base").equals(profile("base")),"base resume preserved");
        ResumeProfile unsupported=new ResumeProfile("fake","Java",List.of(new Skill("Python","UNSPECIFIED")),List.of(),List.of(),List.of(),List.of(),"");fails(()->resumes.save(unsupported),"unsupported profile skill rejected");check(resumes.findById("fake")==null,"unsupported profile not saved");
        resumes.delete("base");check(resumes.findById("base")==null&&resumes.current().id().equals("second"),"unselected profile deletion");resumes.delete("second");fails(resumes::current,"selected profile removal clears pointer");
        var memory=new SqliteMemoryRepository(store);Repository<MemoryFact> memoryContract=memory;var fact=new MemoryFact("fact","skills","Java",NOW);memoryContract.save(fact);check(memory.findById("fact").equals(fact),"memory CRUD");
        fails(()->memory.save(new MemoryFact("duplicate","skills"," JAVA ",NOW)),"normalized duplicate facts rejected");check(memory.findAll().size()==1,"duplicate did not add row");
        var edited=fact.edit("Java and SQL");memory.save(edited);check(memory.findById("fact").text().equals("Java and SQL")&&!edited.updatedAt().equals(NOW),"memory edit and timestamp");
        check(memory.findByText("java AND sql").id().equals("fact"),"normalized fact lookup");memory.delete("fact");check(memory.findAll().isEmpty(),"memory deleted");fails(()->memory.delete("fact"),"missing fact deletion rejected");
        jobs.save(job("job"));var lists=new SqliteChecklistRepository(store);Repository<Checklist> listContract=lists;listContract.save(checklist("list","job"));check(lists.findById("list").equals(lists.findById("job")),"checklist by stable ID and job ID");
        check(lists.tick("job","item",true).progress()==100,"persisted checklist progress");check(lists.tick("job","item",false).progress()==0,"persisted untick");fails(()->lists.tick("job","absent",true),"unknown tick rejected");check(lists.findAll().size()==1,"checklist listing");lists.delete("list");check(lists.findById("job")==null,"checklist deletion");
        var empty=new Checklist("empty","job",null,List.of());check(empty.progress()==0,"empty checklist progress");
        fails(()->new Checklist("duplicate","job",null,List.of(new ChecklistItem("x","one",1,false),new ChecklistItem("x","two",2,false))),"duplicate checklist identities rejected");
        fails(()->new SkillAssessment("Java",GapGroup.STRONG,Double.NaN,"fixture"),"invalid assessment confidence rejected");
    }
    private static void relationships(Path path){
        var store=new SqliteStore(path);var jobs=new SqliteJobRepository(store);var resumes=new SqliteResumeRepository(store);var lists=new SqliteChecklistRepository(store);
        var gap=new SkillGapReport("job",NOW,List.of(new SkillAssessment("SQL",GapGroup.MISSING,0.8,"No SQL evidence")));
        fails(()->jobs.save(gap),"orphan gap rejected");check(store.find("gap","job")==null,"orphan gap not written");jobs.save(job("job"));resumes.save(profile("base"));jobs.save(gap);check(jobs.findSkillGap("job").equals(gap),"linked gap saved");
        var version=new ResumeVersion("version","job","base",NOW,"Java applications Developed",List.of(new ResumeChange("Developed Java applications","Java applications Developed","Emphasize Java",true)));
        resumes.save(version);check(resumes.findVersion("version").equals(version)&&resumes.findVersions("job").size()==1,"version linked to job and base");
        fails(()->resumes.save(new ResumeVersion("bad","missing","base",NOW,"text",List.of())),"version missing job rejected");fails(()->resumes.save(new ResumeVersion("bad","job","missing",NOW,"text",List.of())),"version missing base rejected");
        fails(()->resumes.delete("base"),"base with versions cannot be deleted");fails(()->jobs.delete("job"),"job with work cannot be deleted");check(resumes.findById("base")!=null&&jobs.findById("job")!=null,"parents preserved after rejected deletion");
        fails(()->lists.save(checklist("orphan","missing")),"orphan checklist rejected");
        fails(()->lists.save(new Checklist("invented","job",LocalDate.of(2030,1,1),List.of())),"unsupported checklist deadline rejected");lists.save(checklist("list","job"));
        fails(()->lists.save(checklist("replacement","job")),"checklist identity cannot silently change");
        fails(()->jobs.save(new CompanySummary("job","Goals","Values",List.of())),"summary without evidence rejected");
        var company=new CompanySummary("job","Goals","Values",List.of(new SearchResult("Acme values","https://example.com","Evidence")));jobs.save(company);check(jobs.findCompany("job").equals(company),"company sources saved");
        fails(()->jobs.saveQuestions("job",List.of(new InterviewQuestion("q","other","Why Java?",""))),"cross-job question rejected");
        var question=new InterviewQuestion("q","job","Why Java?","");jobs.saveQuestions("job",List.of(question));check(jobs.findQuestions("job").questions().getFirst().jobId().equals("job"),"questions linked to job");
        var session=new InterviewSession("job",List.of(question),Map.of("q","I used Java"),Map.of(),Map.of(),Set.of(),false);jobs.save(session);check(jobs.findInterview("job").questions().getFirst().answer().equals("I used Java"),"typed question answer persisted with progress");
        fails(()->jobs.save(new InterviewSession("job",List.of(question),Map.of("unknown","answer"),Map.of(),Map.of(),Set.of(),false)),"unknown interview answer rejected");
        fails(()->jobs.save(new FeedbackReport("job",List.of("one","two"),"summary",List.of())),"feedback count relationship enforced");
        var feedback=new FeedbackReport("job",List.of("Use evidence"),"Practice",List.of("Specific examples"));jobs.save(feedback);check(jobs.findFeedback("job").equals(feedback),"feedback linked to answered session");
        jobs.restartInterview(new InterviewSession("job",List.of(question),Map.of(),Map.of(),Map.of(),Set.of(),false));check(store.all("interview-archive").size()==1&&jobs.findInterview("job").answers().isEmpty(),"restart archives old progress");
        check(jobs.findFeedback("job")==null&&store.all("feedback-archive").size()==1,"restart archives feedback instead of showing it for a new attempt");
        check(jobs.results("job").keySet().containsAll(Set.of("gap","company","questions","interview","checklist")),"typed consolidated results");
        fails(()->jobs.saveTrace("trace",Map.of("jobId","unknown")),"orphan trace rejected");
    }
    private static void legacy(Path path){
        var store=new SqliteStore(path);
        Map<String,Object> legacyJob=new LinkedHashMap<>();legacyJob.put("id","legacy-job");legacyJob.put("title","Developer");legacyJob.put("company","Acme");legacyJob.put("country","CA");legacyJob.put("text","Java");legacyJob.put("source","USER_PASTED");legacyJob.put("attribution","old source label");legacyJob.put("deadline","");store.save("job","legacy-job",legacyJob);
        Map<String,Object> legacyResume=Map.of("id","legacy-base","rawText","Java SQL","skills",List.of("Java","SQL"),"education",List.of(),"experience",List.of(),"projects",List.of(),"certifications",List.of(),"warning","");store.save("resume","legacy-base",legacyResume);
        store.save("settings","job",Map.of("id","legacy-job"));store.save("settings","resume",Map.of("id","legacy-base"));
        store.save("memory","legacy-fact",Map.of("id","legacy-fact","category","skills","text","Java"));
        store.save("gap","legacy-job",Map.of("jobId","legacy-job","assessments",List.of(Map.of("skill","SQL","group","MISSING","confidence",0.6,"evidence","not in resume"))));
        store.save("version","legacy-version",Map.of("id","legacy-version","jobId","legacy-job","baseResumeId","legacy-base","text","Java SQL","changes",List.of()));
        store.save("questions","legacy-job",Map.of("jobId","legacy-job","questions",List.of(Map.of("id","q","text","Explain Java?"))));
        store.save("interview","legacy-job",Map.of("jobId","legacy-job","questions",List.of(Map.of("id","q","text","Explain Java?")),"answers",Map.of("q","I used Java"),"followups",Map.of(),"followupAnswers",Map.of(),"decided",List.of("q"),"finished",false));
        store.save("feedback","legacy-job",Map.of("jobId","legacy-job","perAnswer",List.of("Add evidence"),"summary","Practice","weaknesses",List.of()));
        store.save("checklist","legacy-job",Map.of("jobId","legacy-job","items",List.of(Map.of("id","item","label","Practice SQL","priority",1,"done",true))));
        Map<String,String> before=snapshot(store);var jobs=new SqliteJobRepository(store);var resumes=new SqliteResumeRepository(store);var memory=new SqliteMemoryRepository(store);var lists=new SqliteChecklistRepository(store);
        check(jobs.current().id().equals("legacy-job")&&jobs.current().sourceLabel()==SourceLabel.USER_PASTED&&jobs.current().adzunaUrl().equals("old source label")&&jobs.current().deadline()==null,"legacy source fields and selection readable");
        check(resumes.current().getSkills().equals(List.of(new Skill("Java","UNSPECIFIED"),new Skill("SQL","UNSPECIFIED"))),"legacy string skills become typed skills without guessed levels");
        check(memory.findById("legacy-fact").updatedAt()==null,"unknown legacy timestamp explicitly represented");
        check(jobs.findSkillGap("legacy-job").assessments().getFirst().skillName().equals("SQL"),"legacy assessment field alias");
        check(resumes.findVersion("legacy-version").createdAt()==null,"legacy version retained without invented creation date");
        check(jobs.findQuestions("legacy-job").questions().getFirst().jobId().equals("legacy-job"),"legacy question linked from aggregate");
        check(jobs.findInterview("legacy-job").questions().getFirst().answer().equals("I used Java"),"legacy interview answers restored from saved map");
        check(jobs.findFeedback("legacy-job").perAnswerFeedback().equals(List.of("Add evidence")),"legacy feedback field alias");
        check(lists.findById("legacy-job").progress()==100&&lists.findById("legacy-job").id().equals("legacy-job"),"legacy checklist identity and ticks retained");
        check(before.equals(snapshot(store)),"all compatibility reads leave stored document bytes unchanged");
        lists.tick("legacy-job","item",false);check(lists.findById("legacy-job").progress()==0,"legacy checklist remains editable");
        memory.save(memory.findById("legacy-fact").edit("Java and SQL"));check(memory.findById("legacy-fact").text().equals("Java and SQL"),"legacy fact remains editable");
        store.save("resume","corrupt",Map.of("id","corrupt","rawText","text"));fails(()->resumes.findById("corrupt"),"corrupt record reported without repair overwrite");check(store.find("resume","corrupt").size()==2,"corrupt record left untouched");
    }
    private static Map<String,String> snapshot(SqliteStore store){Map<String,String> out=new TreeMap<>();for(String kind:List.of("job","resume","settings","memory","gap","version","questions","interview","feedback","checklist"))out.put(kind,Json.write(store.all(kind)));return out;}
    private static void atomicFailure(Path path)throws Exception {
        var store=new SqliteStore(path);var jobs=new SqliteJobRepository(store);var resumes=new SqliteResumeRepository(store);jobs.saveAndSelect(job("old"));resumes.saveAndSelect(profile("old-base"));
        sql(path,"CREATE TRIGGER reject_selection BEFORE UPDATE ON documents WHEN NEW.kind='settings' BEGIN SELECT RAISE(ABORT,'fixture failure'); END;");
        fails(()->jobs.saveAndSelect(job("new")),"failed job pointer save aborts transaction");check(jobs.findById("new")==null&&jobs.current().id().equals("old"),"failed job selection rolls back row and pointer");
        fails(()->resumes.saveAndSelect(profile("new-base")),"failed resume pointer save aborts transaction");check(resumes.findById("new-base")==null&&resumes.current().id().equals("old-base"),"failed import rolls back row and preserves previous base selection");
        sql(path,"DROP TRIGGER reject_selection;");
        var q=new InterviewQuestion("q","old","Why Java?","");jobs.save(new InterviewSession("old",List.of(q),Map.of("q","Saved answer"),Map.of(),Map.of(),Set.of(),false));
        jobs.save(new FeedbackReport("old",List.of("Saved feedback"),"Summary",List.of()));
        sql(path,"CREATE TRIGGER reject_interview BEFORE INSERT ON documents WHEN NEW.kind='interview' BEGIN SELECT RAISE(ABORT,'fixture failure'); END;");
        fails(()->jobs.restartInterview(new InterviewSession("old",List.of(q),Map.of(),Map.of(),Map.of(),Set.of(),false)),"failed interview restart reported");
        check(jobs.findInterview("old").answers().get("q").equals("Saved answer")&&store.all("interview-archive").isEmpty(),"failed restart preserves answers and rolls back archive write");check(jobs.findFeedback("old").perAnswerFeedback().equals(List.of("Saved feedback"))&&store.all("feedback-archive").isEmpty(),"failed restart retains current feedback and rolls back feedback archive");sql(path,"DROP TRIGGER reject_interview;");
    }
    private static void sql(Path path,String sql)throws Exception {
        Process process=new ProcessBuilder("sqlite3","-batch","-bail",path.toString()).redirectErrorStream(true).start();try(var out=process.getOutputStream()){out.write(sql.getBytes(java.nio.charset.StandardCharsets.UTF_8));}
        if(!process.waitFor(10,TimeUnit.SECONDS)){process.destroyForcibly();throw new AssertionError("SQLite fixture setup timed out");}if(process.exitValue()!=0)throw new AssertionError("SQLite fixture setup failed");
    }
    private static void restart(Path path)throws Exception {
        var store=new SqliteStore(path);var jobs=new SqliteJobRepository(store);var resumes=new SqliteResumeRepository(store);var memory=new SqliteMemoryRepository(store);var lists=new SqliteChecklistRepository(store);
        jobs.saveAndSelect(job("job"));resumes.saveAndSelect(profile("base"));memory.save(new MemoryFact("fact","skills","Java",NOW));lists.save(checklist("list","job"));lists.tick("job","item",true);
        resumes.save(new ResumeVersion("version","job","base",NOW,"Java applications Developed",List.of()));
        var q=new InterviewQuestion("q","job","Explain Java?","");jobs.saveQuestions("job",List.of(q));jobs.save(new InterviewSession("job",List.of(q),Map.of("q","Saved answer"),Map.of(),Map.of(),Set.of("q"),false));
        var javaExecutable=Path.of(System.getProperty("java.home"),"bin","java").toString();Process process=new ProcessBuilder(javaExecutable,"-cp",System.getProperty("java.class.path"),"career.RepositoryTest","restart-check",path.toString()).redirectErrorStream(true).start();
        if(!process.waitFor(20,TimeUnit.SECONDS)){process.destroyForcibly();throw new AssertionError("Fresh-process restart timed out");}
        String output=new String(process.getInputStream().readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);check(process.exitValue()==0&&output.contains("RESTART PASS"),"all repository data loaded in separate JVM: "+output);
    }
    public static void main(String[] args)throws Exception {
        if(args.length!=2||!args[0].equals("restart-check"))throw new IllegalArgumentException("Internal restart-check requires fixture path");
        var store=new SqliteStore(Path.of(args[1]));var jobs=new SqliteJobRepository(store);var resumes=new SqliteResumeRepository(store);
        check(jobs.current().id().equals("job"),"job survives fresh JVM");check(resumes.current().getSkills().getFirst().name().equals("Java"),"typed profile survives fresh JVM");
        check(resumes.findVersion("version").baseResumeId().equals("base"),"version relationship survives");check(new SqliteMemoryRepository(store).findById("fact").text().equals("Java"),"memory survives");check(new SqliteChecklistRepository(store).findById("job").progress()==100,"checklist progress survives");check(jobs.findInterview("job").questions().getFirst().answer().equals("Saved answer"),"typed interview progress survives");
        System.out.println("RESTART PASS");
    }
}
