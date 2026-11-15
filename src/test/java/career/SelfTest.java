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
            json();storage();memoryAcceptance();parsers();resume();job();memory();gap();gapAcceptance();tailoring();tailoringAcceptance();research();researchAcceptance();questions();mockInterview();checklist();workflow();facade();endToEnd();checks+=ApiAdaptersTest.run();checks+=RepositoryTest.run();checks+=InputFeaturesTest.run();
            
            System.out.println("PASS: "+checks+" assertions. All model responses were test fixtures; no live LLM claim.");
        }finally{try(var paths=Files.walk(temp)){for(Path p:paths.sorted(Comparator.reverseOrder()).toList())Files.deleteIfExists(p);}}
    }
    private static void memoryAcceptance()throws Exception {
        SqliteStore store=db("memory-acceptance");UI ui=new UI();var events=new AgentEventPublisher();
        List<String> notices=new ArrayList<>();events.attach(e->notices.add(e.type()));
        AtomicInteger generations=new AtomicInteger();
        String[] output={"{\"category\":\"skills\",\"text\":\"I know Java\"}"};
        String[] choice={"save","compatible"};
        Ai ai=new Ai((p,t)->{generations.incrementAndGet();check(t==ModelTier.HAIKU,"memory uses Haiku");return output[0];},(q,options)->{
            String selected=options.contains("save")?choice[0]:choice[1];Map<String,Double> probabilities=new LinkedHashMap<>();
            for(String option:options)probabilities.put(option,option.equals(selected)?1.0:0.0);
            return new Decision(selected,probabilities,1);
        });
        MemoryManager memory=new MemoryManager(ai,store,new ApprovalGate(ui),events);
        choice[0]="skip";memory.recordTurn("Hello");check(generations.get()==0&&memory.loadFacts().isEmpty(),"irrelevant turn skips generation");
        choice[0]="save";memory.recordTurn("I know Java");
        check(memory.loadFacts().size()==1&&ui.approvals==0&&notices.contains("memory-saved"),"useful turn saved without approval");
        memory.recordTurn(" I KNOW   JAVA ");check(memory.loadFacts().size()==1,"normalized AI fact deduplicated");
        output[0]="{\"category\":\"skills\",\"text\":\"know Java\"}";choice[1]="duplicate";
        memory.recordTurn("I know Java");check(memory.loadFacts().size()==1&&ui.approvals==0,"equivalent wording skips save without approval");
        String id=memory.loadFacts().getFirst().id();
        output[0]="{\"category\":\"skills\",\"text\":\"I do not know Java\"}";choice[1]="conflict";ui.approved=false;
        memory.recordTurn("I do not know Java");check(memory.loadFacts().size()==1&&ui.approvals==1,"conflict rejection retains history");
        ui.approved=true;memory.recordTurn("I do not know Java");check(memory.loadFacts().size()==2&&ui.approvals==2,"approved conflict saved alongside original");
        output[0]="{\"category\":\"skills\",\"text\":\"Expert in Rust\"}";
        int before=generations.get();memory.recordTurn("I am learning SQL");check(generations.get()-before==2&&memory.loadFacts().size()==2&&notices.contains("memory-warning"),"unsupported fact retries once then skips");
        output[0]="{\"category\":\"invented\",\"text\":\"SQL\"}";
        memory.recordTurn("SQL");check(memory.loadFacts().size()==2,"invalid category rejected");
        output[0]="not JSON";memory.recordTurn("SQL");check(memory.loadFacts().size()==2,"malformed output preserves facts");
        before=generations.get();memory.recordTurn(" ");check(generations.get()==before,"empty turn rejected before generation");
        memory.editFact(id,"I know Java and SQL");
        var reopened=new MemoryManager(ai,new SqliteStore(temp.resolve("memory-acceptance.db")),new ApprovalGate(ui),events);
        check(reopened.loadFacts().stream().anyMatch(f->f.id().equals(id)&&f.text().equals("I know Java and SQL")),"edited fact survives restart");
        reopened.delete(id);check(reopened.loadFacts().size()==1,"fact deletion persists");
        fails(()->reopened.editFact("unknown","SQL"),"unknown fact cannot be edited");
        Process trigger=new ProcessBuilder("sqlite3",temp.resolve("memory-acceptance.db").toString(),
            "CREATE TRIGGER fail_memory BEFORE INSERT ON documents WHEN NEW.kind='memory' BEGIN SELECT RAISE(ABORT,'fixture write failure'); END;").start();
        check(trigger.waitFor()==0,"write-failure fixture installed");
        output[0]="{\"category\":\"skills\",\"text\":\"I know SQL\"}";choice[1]="compatible";notices.clear();
        reopened.recordTurn("I know SQL");
        check(reopened.loadFacts().size()==1&&notices.contains("memory-warning"),"failed database save warns and preserves prior facts without aborting turn");
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
    private static void gapAcceptance(){
        SqliteStore store=preparedDb("gap-acceptance");
        Ai ai=scripted((p,t)->{
            if(p.startsWith("Extract"))return "{\"skills\":[\"SQL\",\"Java\",\" java \" ,\"Git\"]}";
            if(p.contains("required skill sql"))return "{\"choice\":\"MISSING\",\"confidence\":0.95}";
            if(p.contains("required skill git"))return "{\"choice\":\"DECENT\",\"confidence\":0.6}";
            return "{\"choice\":\"STRONG\",\"confidence\":0.95}";
        });
        JobDescription job=new JobDescription("job","Developer","Acme","CA","Java SQL Git",SourceLabel.USER_PASTED,"",null);
        var profile=new ResumeProfile("base","Developed Java applications; Git",List.of(new Skill("Java","UNSPECIFIED")),List.of(),List.of(),List.of(),List.of(),"");
        var report=new SkillGapService(ai,store).identify(job,profile);
        check(report.assessments().size()==3,"required skill names normalized and deduplicated");
        check(report.groupedByPriority().stream().map(SkillAssessment::group).toList().equals(List.of(GapGroup.MISSING,GapGroup.STRONG,GapGroup.DECENT)),"all three gap groups in required order");
        String display=Display.format(Map.of("gap",report));
        check(display.indexOf("MISSING:")<display.indexOf("STRONG:")&&display.indexOf("STRONG:")<display.indexOf("DECENT:"),"saved and direct reports display ordered groups");
        check(display.contains("uncertain: true")&&display.contains("uncertain: false"),"uncertainty displayed explicitly");
        check(new SqliteJobRepository(store).findSkillGap(job.id()).equals(report),"gap report persists with job link");
        var misleading=new ResumeProfile("base","JavaScript and SQLite",List.of(),List.of(),List.of(),List.of(),List.of(),"");
        var cautious=new SkillGapService(ai,store).identify(job,misleading);
        check(cautious.assessments().stream().allMatch(a->a.group()==GapGroup.MISSING&&a.uncertain()),"skill substrings do not invent resume evidence");
        Ai unsupported=fixture("{\"skills\":[\"Python\"]}");
        fails(()->new SkillGapService(unsupported,store).identify(job,profile),"unsupported extracted skill rejected after retry");
        check(new SqliteJobRepository(store).findSkillGap(job.id()).equals(cautious),"failed extraction preserves saved report");
        UI decline=new UI();decline.approved=false;
        var missing=new AgentController(db("gap-missing"),ai,noAdzuna(),new WebSearchTool(new Config(),new HttpTransport()),decline,new AgentEventPublisher());
        fails(()->missing.runFeature("gap",List.of()),"missing job recovery may be declined");check(decline.approvals==1,"gap offers missing job intake");
        var noResumeStore=db("gap-no-resume");new SqliteJobRepository(noResumeStore).saveAndSelect(job);
        var noResume=new AgentController(noResumeStore,ai,noAdzuna(),new WebSearchTool(new Config(),new HttpTransport()),decline,new AgentEventPublisher());
        fails(()->noResume.runFeature("gap",List.of()),"missing resume recovery may be declined");check(decline.approvals==2,"gap offers missing resume intake");
        Deque<String> inputs=new ArrayDeque<>(List.of("Developer","Acme","CA","Java SQL Git","Developed Java applications; Git"));
        AtomicInteger approvals=new AtomicInteger();
        var accepting=new AgentController.UserInteraction(){
            public boolean askApproval(String prompt){approvals.incrementAndGet();return true;}
            public String readText(String prompt){return inputs.removeFirst();}
        };
        Ai recovery=scripted((p,t)->p.startsWith("Extract only")?"{\"skills\":[\"Java\",\"Git\"],\"education\":[],\"experience\":[],\"projects\":[],\"certifications\":[]}":p.startsWith("Extract")?"{\"skills\":[\"Java\",\"SQL\",\"Git\"]}":"{\"choice\":\"STRONG\",\"confidence\":0.9}");
        var recovered=new AgentController(db("gap-recovery"),recovery,noAdzuna(),new WebSearchTool(new Config(),new HttpTransport()),accepting,new AgentEventPublisher());
        check(recovered.runFeature("gap",List.of()) instanceof SkillGapReport&&approvals.get()==2&&inputs.isEmpty(),"approved job and resume recovery completes actual gap command");
    }
    private static void tailoring(){
        check(ResumeTailorService.safeChange(profile().rawText(),"Developed Java applications","Java applications Developed"),"supported reordered edit");
        check(!ResumeTailorService.safeChange(profile().rawText(),profile().rawText(),"Developed Python applications"),"new skill rejected");check(!ResumeTailorService.safeChange("Java Java","Java","JAVA"),"ambiguous match rejected");check(!ResumeTailorService.safeChange("Built 2 apps","Built 2 apps","Built 20 apps"),"invented metric rejected");
        SqliteStore s=preparedDb("tailor");UI ui=new UI();String response="{\"changes\":[{\"original\":\"Developed Java applications\",\"revised\":\"Java applications Developed\",\"reason\":\"Emphasize Java\"}]}";
        ResumeTailorService service=new ResumeTailorService(fixture(response),s,new ApprovalGate(ui));var version=service.tailor(sampleJob(),profile());check(version.changes().getFirst().accepted()&&!version.text().equals(profile().rawText()),"accepted edit saved");check(profile().rawText().equals("Developed Java applications"),"original unchanged");ui.approved=false;check(service.tailor(sampleJob(),profile()).text().equals(profile().rawText()),"rejected edit not applied");
    }
    private static void tailoringAcceptance(){
        String original="Developed Java applications";
        check(ResumeTailorService.safeChange(original,original,"Java application development"),"genuine wording improvement accepted");
        check(ResumeTailorService.safeChange("Tested Java applications.","Tested Java applications.","Testing of Java applications"),"bounded word forms and punctuation supported");
        check(!ResumeTailorService.safeChange(original,original,"Expert Java application development"),"new proficiency claim rejected");
        check(!ResumeTailorService.safeChange(original,original,"Java application development with Docker"),"new technical skill rejected");
        check(!ResumeTailorService.safeChange("Did not test Java","Did not test Java","Did test Java"),"negation cannot be removed");
        check(!ResumeTailorService.safeChange("Built 2 applications","Built 2 applications","Built 20 applications"),"invented quantity rejected");
        check(!ResumeTailorService.safeChange("C++ applications","C++ applications","C# applications"),"technical punctuation preserved");
        check(!ResumeTailorService.safeChange("---","---","..."),"punctuation alone is not a rewrite");
        var store=preparedDb("tailor-acceptance");var repository=new SqliteResumeRepository(store);
        var base=new ResumeProfile("tailor-base","Developed Java applications. Tested Java applications.",List.of(new Skill("Java","UNSPECIFIED")),List.of(),List.of(),List.of(),List.of(),"");repository.saveAndSelect(base);
        List<ResumeChange> offered=new ArrayList<>();
        ApprovalGate.ApprovalPrompter reviewer=new ApprovalGate.ApprovalPrompter(){
            public boolean askApproval(String prompt){throw new AssertionError("Expected structured resume comparison");}
            public boolean reviewResumeChange(ResumeChange change){offered.add(change);return offered.size()==1;}
        };
        String response=Json.write(Map.of("changes",List.of(
            Map.of("original","Developed Java applications.","revised","Java application development.","reason","Lead with relevant Java experience"),
            Map.of("original","Tested Java applications.","revised","Testing of Java applications.","reason","Emphasize testing"))));
        Ai ai=new Ai((prompt,tier)->{check(tier==ModelTier.SONNET,"tailoring uses reasoning model");return response;},(q,o)->{throw new AssertionError("No decision required for user review");});
        var version=new TailorResumeAction(new ResumeTailorService(ai,store,new ApprovalGate(reviewer)),SelfTest::sampleJob,()->base).execute();
        check(offered.size()==2&&version.changes().getFirst().accepted()&&!version.changes().getLast().accepted(),"each change receives an independent review");
        check(version.text().equals("Java application development. Tested Java applications."),"only accepted wording applied");
        check(repository.current().equals(base),"selected base resume remains unchanged");
        var reopened=new SqliteResumeRepository(new SqliteStore(temp.resolve("tailor-acceptance.db")));
        check(reopened.findVersions("job").contains(version)&&version.baseResumeId().equals(base.id()),"tailored version persists with job and base links");
        AtomicInteger attempts=new AtomicInteger();Ai overlapping=new Ai((p,t)->{attempts.incrementAndGet();return Json.write(Map.of("changes",List.of(
            Map.of("original","Developed Java applications.","revised","Java application development.","reason","One"),
            Map.of("original","Developed Java applications","revised","Java application development","reason","Two"))));},(q,o)->{throw new AssertionError("No decision expected");});
        int before=offered.size();
        fails(()->new ResumeTailorService(overlapping,store,new ApprovalGate(reviewer)).tailor(sampleJob(),base),"overlapping or ambiguous proposals rejected");
        check(attempts.get()==2&&offered.size()==before&&reopened.findVersions("job").size()==1,"invalid proposal retries once without approval or new version");
        UI decline=new UI();decline.approved=false;
        var controller=new AgentController(db("tailor-missing"),ai,noAdzuna(),new WebSearchTool(new Config(),new HttpTransport()),decline,new AgentEventPublisher());
        fails(()->controller.runFeature("tailor",List.of()),"missing tailoring inputs offer recovery");check(decline.approvals==1,"tailoring asks before collecting missing job");
    }
    private static void research(){
        SqliteStore s=preparedDb("research");AtomicInteger searches=new AtomicInteger();WebSearchTool empty=new WebSearchTool(new Config(),new HttpTransport()){public List<SearchResult> search(String q){searches.incrementAndGet();return List.of();}};
        fails(()->new CompanyResearchService(fixture("{\"text\":\"Acme values\"}"),tools(empty),s).research(sampleJob()),"empty search does not invent summary");check(searches.get()==2&&s.find("company","job")==null,"bounded research retries");
        WebSearchTool found=new WebSearchTool(new Config(),new HttpTransport()){public List<SearchResult> search(String q){return List.of(new SearchResult("Acme values","https://example.com/values","Acme values collaboration"));}};
        Ai a=scripted((p,t)->p.startsWith("Choose")?"{\"choice\":\"relevant\",\"confidence\":0.9}":p.startsWith("Summarize")?"{\"goals\":\"not stated\",\"values\":\"Collaboration https://example.com/values\"}":"{\"text\":\"Acme values\"}");
        check(new CompanyResearchService(a,tools(found),s).research(sampleJob()).sources().size()==1,"sourced company result");
    }
    private static void researchAcceptance(){
        var store=preparedDb("research-acceptance");
        var relevant=new SearchResult("Acme values","https://example.com/values","Acme values collaboration");
        var irrelevant=new SearchResult("Other company","https://example.org/other","Unrelated business");
        List<String> queries=new ArrayList<>();List<String> prompts=new ArrayList<>();
        WebSearchTool search=new WebSearchTool("",new HttpTransport()){
            public List<SearchResult> search(String query){queries.add(query);return queries.size()==1?List.of(irrelevant):List.of(relevant,irrelevant);}
        };
        Ai ai=new Ai((prompt,tier)->{
            check(tier==ModelTier.HAIKU,"company research uses Haiku");prompts.add(prompt);
            return prompt.startsWith("Summarize")?"{\"goals\":\"not stated\",\"values\":\"Collaboration [source](https://example.com/values).\"}":prompt.startsWith("Reformulate")?"{\"text\":\"Acme official values\"}":"{\"text\":\"Acme goals and values\"}";
        },(question,options)->{
            String choice=question.contains("Other company")?"irrelevant":"relevant";
            return new Decision(choice,Map.of("relevant",choice.equals("relevant")?1.0:0.0,"irrelevant",choice.equals("irrelevant")?1.0:0.0),1);
        });
        var service=new CompanyResearchService(ai,tools(search),store);
        var summary=new CompanyResearchAction(service,SelfTest::sampleJob).execute();
        check(queries.equals(List.of("Acme goals and values","Acme official values")),"no relevant results triggers one reformulated search");
        check(summary.sources().equals(List.of(relevant)),"irrelevant results excluded from saved summary");
        String summaryPrompt=prompts.stream().filter(p->p.startsWith("Summarize")).findFirst().orElseThrow();
        check(!summaryPrompt.contains("Other company")&&summaryPrompt.contains("Acme values collaboration"),"generation receives only relevant evidence");
        check(new SqliteJobRepository(new SqliteStore(temp.resolve("research-acceptance.db"))).findCompany("job").equals(summary),"sourced summary survives restart");
        WebSearchTool found=new WebSearchTool("",new HttpTransport()){public List<SearchResult> search(String query){return List.of(relevant);}};
        for(String values:List.of("Collaboration","Collaboration https://invented.example/values","Collaboration https://example.com/values/unsupported")){
            AtomicInteger generations=new AtomicInteger();
            Ai invalid=new Ai((p,t)->{if(p.startsWith("Summarize")){generations.incrementAndGet();return Json.write(Map.of("goals","not stated","values",values));}return "{\"text\":\"Acme values\"}";},(q,o)->new Decision("relevant",Map.of("relevant",1.0,"irrelevant",0.0),1));
            fails(()->new CompanyResearchService(invalid,tools(found),store).research(sampleJob()),"missing or invented citation rejected");
            check(generations.get()==2&&new SqliteJobRepository(store).findCompany("job").equals(summary),"invalid summary retries once and preserves previous result");
        }
        AtomicInteger searches=new AtomicInteger(),summaries=new AtomicInteger();
        WebSearchTool unrelated=new WebSearchTool("",new HttpTransport()){public List<SearchResult> search(String q){searches.incrementAndGet();return List.of(irrelevant);}};
        Ai reject=new Ai((p,t)->{if(p.startsWith("Summarize"))summaries.incrementAndGet();return "{\"text\":\"Acme official values\"}";},(q,o)->new Decision("irrelevant",Map.of("relevant",0.0,"irrelevant",1.0),1));
        fails(()->new CompanyResearchService(reject,tools(unrelated),store).research(sampleJob()),"all irrelevant sources stop research");
        check(searches.get()==2&&summaries.get()==0&&new SqliteJobRepository(store).findCompany("job").equals(summary),"no relevant evidence never generates or replaces summary");
        AtomicInteger requests=new AtomicInteger();List<String> urls=new ArrayList<>();
        HttpTransport transport=new HttpTransport(){public String request(String url,Map<String,String> headers,String body){
            requests.incrementAndGet();urls.add(url);check(headers.get("X-Subscription-Token").equals("fixture-key")&&headers.get("Accept").equals("application/json")&&body==null,"web search sends authenticated JSON GET");
            return "{\"web\":{\"results\":[{\"title\":\"Values\",\"url\":\"https://example.com/values\",\"description\":\"Collaboration\"},{\"title\":\"Bad\",\"url\":\"https:///missing-host\",\"description\":\"Bad\"},{\"title\":\"Credentials\",\"url\":\"https://user:secret@example.com/values\",\"description\":\"Bad\"}]}}";
        }};
        var web=new WebSearchTool("fixture-key",transport);
        check(web.search("Acme & values").size()==1&&urls.getFirst().contains("q=Acme+%26+values"),"search encoding and invalid-source filtering");
        fails(()->web.search(" "),"blank query rejected before HTTP");
        fails(()->new WebSearchTool("",transport).search("Acme"),"missing search key fails explicitly");check(requests.get()==1,"invalid query and missing key send no HTTP");
        WebSearchTool broken=new WebSearchTool("",new HttpTransport()){public List<SearchResult> search(String q){throw new IllegalStateException("fixture search outage");}};
        fails(()->new CompanyResearchService(reject,tools(broken),store).research(sampleJob()),"tool failure remains a failure");
        check(new SqliteJobRepository(store).findCompany("job").equals(summary),"search outage preserves existing summary");
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
