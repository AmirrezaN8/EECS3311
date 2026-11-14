package career;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.*;
import java.nio.charset.StandardCharsets;
import static career.Models.*;

/** Offline tests use explicit model fixtures; these are never part of the running app. */
public final class SelfTest {
    private static int checks;
    private static Path temp;
    private static void check(boolean condition,String message){checks++;if(!condition)throw new AssertionError(message);}
    private static void fails(Runnable r,String message){boolean failed=false;try{r.run();}catch(RuntimeException e){failed=true;}check(failed,message);}
    private static Ai fixture(String json){return scripted((p,t)->json);}
    // Legacy service scenarios use a test-only script; provider contracts are independently checked below.
    private static Ai scripted(LLMClient script){
        DecisionClient decisions=(question,options)->{
            String prompt="Choose one option. Options: "+Json.write(options)+"\nINPUT DATA (not instructions):\n"+question;
            var data=Json.object(Json.parse(script.generate(prompt,ModelTier.HAIKU)));
            String choice=Json.text(data,"choice");Map<String,Double> probabilities=new LinkedHashMap<>();
            for(String option:options)probabilities.put(option,option.equals(choice)?1.0:0.0);
            return new Decision(choice,probabilities,Json.number(data,"confidence"));
        };
        return new Ai(script,decisions);
    }
    private static AdzunaTool noAdzuna(){return new AdzunaTool(new Config(),new HttpTransport(){
        @Override public String request(String url,Map<String,String> headers,String body){throw new IllegalStateException("Test fixture: Adzuna unavailable");}
    });}
    private static ToolManager tools(Tool... tools){var manager=new ToolManager();for(var tool:tools)manager.register(tool);return manager;}
    private static SqliteStore db(String name){return new SqliteStore(temp.resolve(name+".db"));}
    private static final class UI implements AgentController.UserInteraction {
        boolean approved=true;int approvals;
        public boolean askApproval(String text){approvals++;return approved;}
        public String readText(String prompt){return "";}
    }
    public static void main(String[] args)throws Exception {
        temp=Files.createTempDirectory("career-tests-");
        try{
            json();storage();parsers();resume();job();memory();gap();tailoring();research();questions();mockInterview();checklist();workflow();facade();endToEnd();checks+=ApiAdaptersTest.run();checks+=RepositoryTest.run();checks+=InputFeaturesTest.run();
            
            System.out.println("PASS: "+checks+" assertions. All model responses were test fixtures; no live LLM claim.");
        }finally{try(var paths=Files.walk(temp)){for(Path p:paths.sorted(Comparator.reverseOrder()).toList())Files.deleteIfExists(p);}}
    }
    private static void json(){
        Object x=Map.of("text","quotes \" slash \\ emoji 🍁\n", "array",List.of(true,2,"x"));
        check(Json.write(Json.parse(Json.write(x))).equals(Json.write(x)),"JSON roundtrip");
        for(String s:List.of("{\"x\":1,\"x\":2}","[1,]","true false","01","\"bad\ntext\"","1e9999","{","[", "{\"x\":}","\"\\q\""))fails(()->Json.parse(s),"reject malformed JSON: "+s);
        fails(()->Json.parse("[".repeat(70)+"]".repeat(70)),"depth bound");
        fails(()->Json.bool(Map.of("x","true"),"x"),"strict boolean");
        check(CommandLine.tokenize("job add 'Software Developer' \"A Company\" CA 'Java SQL'").size()==6,"CLI quoting");
        check(CommandLine.tokenize("history add user ''").getLast().isEmpty(),"empty argument");fails(()->CommandLine.tokenize("'bad"),"unclosed CLI quote");
    }
    private static void storage(){
        SqliteStore store=db("store");String id="id'; DROP TABLE documents; --";
        store.save("test",id,Map.of("text","O'Connor\nUnicode 🍁"));
        check(Json.text(store.find("test",id),"text").equals("O'Connor\nUnicode 🍁"),"SQL injection-safe unicode persistence");
        check(new SqliteStore(temp.resolve("store.db")).find("test",id)!=null,"restart persistence");
        store.save("test",id,Map.of("text","updated"));check(store.all("test").size()==1,"upsert");
        store.delete("test",id);check(store.find("test",id)==null,"delete");
        var p=new ResumeProfile("r","Java",List.of(new Skill("Java","UNSPECIFIED")),List.of(),List.of("work"),List.of(),List.of(),"");Documents.save(store,"resume","r",p);
        check(Documents.read(store,"resume","r",ResumeProfile.class).equals(p),"typed records roundtrip");
    }
    private static void parsers()throws Exception {
        ResumeParserFactory f=new ResumeParserFactory();Path text=temp.resolve("resume.MD");Files.writeString(text,"# Java developer");check(f.createParser(text).extractText(text.toString()).contains("Java"),"Markdown import");
        Path doc=temp.resolve("resume.docx");writeDocx(doc,"<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body><w:p><w:r><w:t>Java developer</w:t></w:r></w:p></w:body></w:document>");
        check(f.createParser(doc).extractText(doc.toString()).strip().equals("Java developer"),"DOCX extraction");
        Path malicious=temp.resolve("xxe.docx");writeDocx(malicious,"<!DOCTYPE foo [<!ENTITY xxe SYSTEM 'file:///etc/passwd'>]><w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:p>&xxe;</w:p></w:document>");
        fails(()->{try{f.createParser(malicious).extractText(malicious.toString());}catch(Exception e){throw new IllegalArgumentException(e);}},"DOCX XXE rejected");
        check(f.createParser(Path.of("r.doc")) instanceof DocResumeParser,"legacy Word strategy selected");
        check(f.createParser(Path.of("r.pdf")) instanceof PdfResumeParser,"PDF strategy selected");
    }
    private static void writeDocx(Path p,String xml)throws Exception {try(var zip=new ZipOutputStream(Files.newOutputStream(p))){zip.putNextEntry(new ZipEntry("word/document.xml"));zip.write(xml.getBytes(StandardCharsets.UTF_8));zip.closeEntry();}}
    private static void resume(){
        SqliteStore s=db("resume");Ai ai=fixture("{\"skills\":[\"Java\"],\"education\":[\"BSc\"],\"experience\":[],\"projects\":[],\"certifications\":[]}");
        ResumeImporter importer=new ResumeImporter(ai,s);var p=importer.importText("Java BSc");check(p.getSkills().stream().map(Skill::name).toList().equals(List.of("Java")),"resume extraction");check(importer.current().equals(p),"resume persisted");
        var fallback=new ResumeImporter(fixture("bad"),s).importText("Original resume text");check(fallback.rawText().equals("Original resume text")&&!fallback.warning().isBlank(),"raw fallback labeled");
        var fabricated=new ResumeImporter(fixture("{\"skills\":[\"Python\"],\"education\":[],\"experience\":[],\"projects\":[],\"certifications\":[]}"),s).importText("Java");check(fabricated.skills().isEmpty()&&!fabricated.warning().isBlank(),"fabricated skill rejected");
        fails(()->importer.importText("  "),"blank resume rejected");
        AtomicInteger calls=new AtomicInteger();Ai retry=scripted((prompt,t)->calls.incrementAndGet()==1?"broken":"{\"text\":\"ok\"}");check(retry.text("task","data",ModelTier.HAIKU).equals("ok")&&calls.get()==2,"malformed model retries once");
        fails(()->fixture("{\"choice\":\"evil\",\"confidence\":1}").decide("choose",List.of("yes","no")),"decision bounded");
    }
    private static void job(){
        SqliteStore s=db("job");UI ui=new UI();ui.approved=false;JobDescriptionIntake intake=new JobDescriptionIntake(fixture("{\"text\":\"estimated Java role\"}"),tools(noAdzuna()),s,new ApprovalGate(ui));
        JobDescription job=intake.add("Developer","Acme","Canada","Java SQL\nApply by: 2026-12-01");check(job.country().equals("CA")&&job.deadline().equals(java.time.LocalDate.of(2026,12,1))&&job.sourceLabel()==SourceLabel.USER_PASTED,"job input provenance deadline");
        check(JobDescriptionIntake.explicitDeadline("2026-12-01 team meeting").isEmpty(),"no guessed deadline");check(JobDescriptionIntake.explicitDeadline("Deadline: 2026-02-30").isEmpty(),"invalid deadline");
        fails(()->intake.add("x","y","GB","z"),"country validation");
        // Lookup transport is intercepted even if local credentials are configured.
        {fails(()->intake.find("Developer","Acme","CA"),"estimate refusal respected");ui.approved=true;JobDescription estimate=intake.find("Developer","Acme","US");check(estimate.isEstimate()&&estimate.deadline()==null,"approved full estimate labeled");}
    }
    private static void memory(){
        SqliteStore s=db("memory");UI ui=new UI();MemoryManager m=new MemoryManager(fixture("bad"),s,new ApprovalGate(ui),new AgentEventPublisher());
        MemoryFact f=m.save("skills","Java");check(m.save("skills"," java ").id().equals(f.id())&&m.loadFacts().size()==1,"fact dedup");m.editFact(f.id(),"Java and SQL");check(m.loadFacts().getFirst().text().contains("SQL"),"fact edit");m.recordTurn("I know Java");check(m.loadFacts().size()==1,"failed AI memory preserves existing facts");m.delete(f.id());check(m.loadFacts().isEmpty(),"fact delete");
        fails(()->m.editFact("missing","text"),"unknown fact not overwritten");
    }
    private static JobDescription sampleJob(){return new JobDescription("job","Developer","Acme","CA","Java SQL",SourceLabel.USER_PASTED,"",null);}
    private static SqliteStore preparedDb(String name){var store=db(name);new SqliteJobRepository(store).save(sampleJob());new SqliteResumeRepository(store).save(profile());return store;}
    private static ResumeProfile profile(){return new ResumeProfile("base","Developed Java applications",List.of(new Skill("Java","UNSPECIFIED")),List.of(),List.of(),List.of(),List.of(),"");}
    private static void gap(){
        SqliteStore s=preparedDb("gap");Ai a=scripted((p,t)->p.startsWith("Extract")?"{\"skills\":[\"SQL\",\"Java\"]}":"{\"choice\":\"STRONG\",\"confidence\":0.95}");
        var report=new SkillGapService(a,s).identify(sampleJob(),profile());check(report.groupedByPriority().getFirst().group()==GapGroup.MISSING,"missing skills first");check(report.assessments().getFirst().uncertain(),"unsupported positive classification forced missing uncertain");check(report.assessments().getLast().group()==GapGroup.STRONG,"supported strength");
    }
    private static void tailoring(){
        check(ResumeTailorService.safeChange(profile().rawText(),"Developed Java applications","Java applications Developed"),"supported reordered edit");
        check(!ResumeTailorService.safeChange(profile().rawText(),profile().rawText(),"Developed Python applications"),"new skill rejected");check(!ResumeTailorService.safeChange("Java Java","Java","JAVA"),"ambiguous match rejected");check(!ResumeTailorService.safeChange("Built 2 apps","Built 2 apps","Built 20 apps"),"invented metric rejected");
        SqliteStore s=preparedDb("tailor");UI ui=new UI();String response="{\"changes\":[{\"original\":\"Developed Java applications\",\"revised\":\"Java applications Developed\",\"reason\":\"Emphasize Java\"}]}";
        ResumeTailorService service=new ResumeTailorService(fixture(response),s,new ApprovalGate(ui));var version=service.tailor(sampleJob(),profile());check(version.changes().getFirst().accepted()&&!version.text().equals(profile().rawText()),"accepted edit saved");check(profile().rawText().equals("Developed Java applications"),"original unchanged");ui.approved=false;check(service.tailor(sampleJob(),profile()).text().equals(profile().rawText()),"rejected edit not applied");
    }
    private static void research(){
        SqliteStore s=preparedDb("research");AtomicInteger searches=new AtomicInteger();WebSearchTool empty=new WebSearchTool(new Config(),new HttpTransport()){public List<SearchResult> search(String q){searches.incrementAndGet();return List.of();}};
        fails(()->new CompanyResearchService(fixture("{\"text\":\"Acme values\"}"),tools(empty),s).research(sampleJob()),"empty search does not invent summary");check(searches.get()==2&&s.find("company","job")==null,"bounded research retries");
        WebSearchTool found=new WebSearchTool(new Config(),new HttpTransport()){public List<SearchResult> search(String q){return List.of(new SearchResult("Acme values","https://example.com/values","Acme values collaboration"));}};
        Ai a=scripted((p,t)->p.startsWith("Choose")?"{\"choice\":\"relevant\",\"confidence\":0.9}":p.startsWith("Summarize")?"{\"goals\":\"not stated\",\"values\":\"Collaboration https://example.com/values\"}":"{\"text\":\"Acme values\"}");
        check(new CompanyResearchService(a,tools(found),s).research(sampleJob()).sources().size()==1,"sourced company result");
    }
    private static String questionJson(){return "{\"questions\":[\"Explain Java?\",\"Describe a project?\",\"How do you test?\",\"How do you collaborate?\",\"Why this role?\"]}";}
    private static void questions(){
        SqliteStore s=preparedDb("questions");Ai a=scripted((p,t)->p.startsWith("Create tailored")?"{\"text\":\"Ask relevant questions\"}":questionJson());var q=new InterviewPrepService(a,s).prepare(sampleJob(),profile(),List.of());check(q.size()==5&&s.find("questions","job")!=null,"five questions saved");
        fails(()->new InterviewPrepService(fixture("{\"text\":\"prompt\",\"questions\":[\"only one\"]}"),s).prepare(sampleJob(),profile(),List.of()),"invalid question count rejected");
    }
    private static List<InterviewQuestion> questionList(){return List.of(new InterviewQuestion("q1","job","Explain Java?",""),new InterviewQuestion("q2","job","Project?",""));}
    private static void mockInterview(){
        SqliteStore s=preparedDb("interview");Documents.save(s,"questions","job",new InterviewQuestionSet("job",questionList()));UI ui=new UI();
        Ai a=scripted((p,t)->p.startsWith("Choose")?"{\"choice\":\"followup\",\"confidence\":0.8}":p.startsWith("Write one")?"{\"text\":\"What did you personally do?\"}":"{\"perAnswer\":[\"Add specific evidence\",\"Good example\"],\"summary\":\"Practice detail\",\"weaknesses\":[\"Explain personal contribution\"]}");
        MemoryManager m=new MemoryManager(a,s,new ApprovalGate(ui),new AgentEventPublisher());MockInterviewService service=new MockInterviewService(a,s,m);
        check(service.nextQuestion(service.start("job",false)).equals("Explain Java?"),"first question");
        check(service.answer("job","I used Java").equals("What did you personally do?"),"follow-up returned without feedback");check(service.load("job").followups().size()==1,"one follow-up");
        fails(()->service.feedback("job",false),"no mid-interview feedback");service.stop("job");check(service.nextQuestion(service.start("job",false)).equals("What did you personally do?"),"paused interview resumes follow-up");
        check(service.answer("job","I implemented the parser").equals("Project?"),"move after follow-up");service.answer("job","A career app");check(service.answer("job","I wrote tests").contains("complete"),"complete after final follow-up");
        check(service.load("job").finished()&&service.load("job").followups().size()==2,"completion persisted with max one followup per base");check(service.feedback("job",false).perAnswerFeedback().size()==2&&m.loadFacts().size()==1,"final feedback and weaknesses persisted");
        service.start("job",true);check(s.all("interview-archive").size()==1,"restart archives answers");
        service.answer("job","short answer");Ai partial=fixture("{\"perAnswer\":[\"More detail\"],\"summary\":\"Partial only\",\"weaknesses\":[]}");var partialService=new MockInterviewService(partial,s,m);check(partialService.feedback("job",true).perAnswerFeedback().size()==1,"approved partial feedback");
    }
    private static void checklist(){
        SqliteStore s=preparedDb("checklist");ChecklistService service=new ChecklistService(fixture("bad"),s);var list=service.build(sampleJob());check(list.items().size()>=4&&list.progress()==0,"offline partial checklist");check(list.items().stream().noneMatch(i->i.label().contains("deadline")),"no invented deadline");
        var updated=service.tick("job",list.items().getFirst().id(),true);check(updated.progress()>0,"progress updated");check(service.build(sampleJob()).items().getFirst().done(),"rebuild preserves ticks");fails(()->service.tick("job","unknown",true),"invalid item rejected");check(service.tick("job",list.items().getFirst().id(),false).progress()==0,"untick saved");
    }
    private static void workflow(){
        AtomicInteger executed=new AtomicInteger();AgentEventPublisher events=new AgentEventPublisher();List<String> states=new ArrayList<>();events.attach(e->states.add(e.message()));events.attach(e->{throw new IllegalStateException("bad view");});
        Planner planner=new Planner((q,options)->new Decision(options.getFirst(),options.stream().collect(java.util.stream.Collectors.toMap(o->o,o->o.equals(options.getFirst())?1.0:0.0)),1));UI ui=new UI();
        var actions=List.<AgentAction>of(new AgentAction.FeatureCommand("gap","gaps",()->{executed.incrementAndGet();return "ok";}),new AgentAction.FeatureCommand("company","research",()->{throw new IllegalStateException("unavailable");}),new AgentAction.FeatureCommand("checklist","checklist",()->{executed.incrementAndGet();return "ok";}));
        List<String> trace=new AgentSession(planner,new ApprovalGate(ui),events,actions,"goal",()->false).handleInput();
        check(executed.get()==2&&trace.stream().anyMatch(t->t.contains("step-error")),"workflow continues after failed tool");check(ui.approvals==3&&states.contains("AwaitingApprovalState")&&states.contains("ExecutingState")&&states.contains("FinishedState"),"real state transitions and approvals");
        ui.approved=false;executed.set(0);new AgentSession(planner,new ApprovalGate(ui),events,actions,"goal",()->false).handleInput();check(executed.get()==0,"declined actions never execute");
        ui.approved=true;new AgentSession(planner,new ApprovalGate(ui),events,actions,"goal",()->true).handleInput();check(executed.get()==0,"stop prevents execution");
        AtomicInteger n=new AtomicInteger();List<AgentAction> many=new ArrayList<>();for(int i=0;i<20;i++)many.add(new AgentAction.FeatureCommand("step"+i,"step",()->n.incrementAndGet()));new AgentSession(planner,new ApprovalGate(ui),events,many,"goal",()->false).handleInput();check(n.get()==10,"hard workflow step cap");
    }
    private static void facade(){
        SqliteStore s=db("facade");UI ui=new UI();ui.approved=false;
        Ai ai=scripted((p,t)->{
            if(p.startsWith("Extract only"))return "{\"skills\":[\"Java\"],\"education\":[],\"experience\":[],\"projects\":[],\"certifications\":[]}";
            if(p.startsWith("Choose"))return "{\"choice\":\"skip\",\"confidence\":0.9}";
            return "bad";
        });
        var c=new AgentController(s,ai,noAdzuna(),new WebSearchTool(new Config(),new HttpTransport()),ui,new AgentEventPublisher());
        fails(c::currentJob,"missing input explained");c.runFeature("job add",List.of("Developer","Acme","CA","Java"));c.runFeature("resume paste",List.of("Developed Java applications"));
        c.runFeature("history add",List.of("target","Developer"));check(((List<?>)c.runFeature("history show",List.of())).size()==1,"shared facade history");check(c.runFeature("checklist build",List.of()) instanceof Checklist,"shared facade checklist");
        fails(()->c.runFeature("job select",List.of("unknown")),"job select validated");fails(()->c.runFeature("checklist tick",List.of("x","bad")),"boolean validated");fails(()->c.runFeature("unknown",List.of()),"unknown command");
        check(CommandLine.request(CommandLine.tokenize("job show")).command().equals("job show"),"CLI routes shared feature");
    }

    private static void endToEnd(){
        SqliteStore s=db("e2e");UI ui=new UI();
        Ai ai=scripted((p,t)->{
            if(p.startsWith("Choose one")){
                int begin=p.indexOf("Options: ")+9,end=p.indexOf("\nINPUT DATA");
                @SuppressWarnings("unchecked") List<String> options=(List<String>)(List<?>)Json.parse(p.substring(begin,end));
                String choice=options.contains("skip")?"skip":options.contains("next")?"next":options.contains("STRONG")?"STRONG":options.getFirst();
                return Json.write(Map.of("choice",choice,"confidence",0.9));
            }
            if(p.startsWith("Extract only"))return "{\"skills\":[\"Java\"],\"education\":[],\"experience\":[],\"projects\":[],\"certifications\":[]}";
            if(p.startsWith("Extract required"))return "{\"skills\":[\"Java\",\"SQL\"]}";
            if(p.startsWith("Propose up"))return "{\"changes\":[{\"original\":\"Developed Java applications\",\"revised\":\"Java applications Developed\",\"reason\":\"Emphasis\"}]}";
            if(p.startsWith("Generate exactly"))return questionJson();
            if(p.startsWith("Evaluate ONLY"))return "{\"perAnswer\":[\"One\",\"Two\",\"Three\",\"Four\",\"Five\"],\"summary\":\"Practice evidence\",\"weaknesses\":[\"Specific examples\"]}";
            return "{\"text\":\"Fixture instruction\"}";
        });
        WebSearchTool search=new WebSearchTool(new Config(),new HttpTransport()){public List<SearchResult> search(String q){return List.of();}};
        var c=new AgentController(s,ai,noAdzuna(),search,ui,new AgentEventPublisher());
        c.runFeature("job add",List.of("Developer","Acme","CA","Java SQL"));c.runFeature("resume paste",List.of("Developed Java applications"));c.runFeature("history add",List.of("target","Developer"));
        List<String> trace=c.handleGoal("Prepare me for this job");
        check(trace.stream().anyMatch(x->x.equals("completed: gap"))&&trace.stream().anyMatch(x->x.equals("completed: tailor"))&&trace.stream().anyMatch(x->x.equals("completed: checklist")),"full orchestrated services reached checklist");
        check(trace.stream().anyMatch(x->x.startsWith("step-error: company")),"research failure visible in end-to-end trace");
        check(s.all("trace").size()==1&&s.find("checklist",c.currentJob().id())!=null,"workflow trace and preparation persisted");
        c.runFeature("interview start",List.of());for(int i=0;i<5;i++)c.runFeature("interview answer",List.of("I implemented a Java project and verified it"));
        check(c.runFeature("interview feedback",List.of()) instanceof FeedbackReport,"end-to-end interview final feedback");
        Checklist list=(Checklist)c.runFeature("checklist show",List.of());c.runFeature("checklist tick",List.of(list.items().getFirst().id()));
        AgentController restarted=new AgentController(new SqliteStore(temp.resolve("e2e.db")),ai,noAdzuna(),search,ui,new AgentEventPublisher());
        check(((Checklist)restarted.runFeature("checklist show",List.of())).progress()>0,"end-to-end progress survives app restart");
    }

}
