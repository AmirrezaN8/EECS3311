package career;

import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.*;
import java.io.*;

/** F01/F02 acceptance checks with explicit provider/tool fixtures and disposable input/database files. */
public final class InputFeaturesTest {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static void fails(Runnable operation,String message){boolean failed=false;try{operation.run();}catch(RuntimeException e){failed=true;}check(failed,message);}
    private static final class Approvals implements ApprovalGate.ApprovalPrompter {
        final Deque<Boolean> answers=new ArrayDeque<>();final List<String> prompts=new ArrayList<>();
        Approvals(Boolean... answers){this.answers.addAll(List.of(answers));}
        public boolean askApproval(String prompt){prompts.add(prompt);if(answers.isEmpty())throw new AssertionError("Unexpected approval: "+prompt);return answers.removeFirst();}
    }
    private static final class FixedTool implements Tool {
        final String name;final List<SearchResult> hits;int calls;boolean fail;
        FixedTool(String name,List<SearchResult> hits){this.name=name;this.hits=hits;}
        public String getName(){return name;}
        public ToolResult execute(Map<String,String> args){calls++;if(fail)throw new IllegalStateException("fixture search failure");return new ToolResult(true,hits,"");}
    }
    private static ToolManager manager(Tool... tools){ToolManager manager=new ToolManager();for(Tool tool:tools)manager.register(tool);return manager;}
    private static final List<SearchResult> HITS=List.of(new SearchResult("Developer","https://example.com/job/1","Java partial first"),new SearchResult("Junior Developer","https://example.com/job/2","Java SQL partial second"));
    private static Ai ai(LLMClient text,DecisionClient decisions){return new Ai(text,decisions);}
    private static Decision choose(List<String> options,String choice){Map<String,Double> probabilities=new LinkedHashMap<>();for(String option:options)probabilities.put(option,option.equals(choice)?1.0:0.0);return new Decision(choice,probabilities,1);}
    private static Ai never(){return ai((p,t)->{throw new AssertionError("Unexpected text generation");},(q,o)->{throw new AssertionError("Unexpected decision request");});}
    private static JobDescriptionRequest pasted(String text){return new JobDescriptionRequest("Developer","Acme","CA",text,"");}
    private static String profileJson(){return "{\"skills\":[\"Java\",\"SQL\"],\"education\":[\"BSc\"],\"experience\":[\"Developed Java applications\"],\"projects\":[\"Career application\"],\"certifications\":[\"Java certificate\"]}";}
    private static final String RESUME="BSc\nSkills: Java SQL\nDeveloped Java applications\nCareer application\nJava certificate";
    public static int run()throws Exception {
        checks=0;Path temp=Files.createTempDirectory("career-input-tests-");
        try{tools();jobIntake(temp);resumeIntake(temp);wrappedLiveShape(temp);optionalPdf(temp);legacyWord(temp);}
        finally{try(var paths=Files.walk(temp)){for(Path p:paths.sorted(Comparator.reverseOrder()).toList())Files.deleteIfExists(p);}}
        System.out.println("PASS: "+checks+" F01/F02 assertions (test-only models/tools; live feature checks separate).");return checks;
    }
    private static void tools(){
        FixedTool fixture=new FixedTool("fixture",HITS);ToolManager tools=manager(fixture);
        check(tools.executeTool("fixture",Map.of()).requireData().equals(HITS)&&fixture.calls==1,"registered tool actually executes");
        fails(()->tools.register(fixture),"duplicate registration rejected");fails(()->tools.executeTool("unknown",Map.of()),"unknown tool rejected");fixture.fail=true;
        ToolResult failed=tools.executeTool("fixture",Map.of());check(!failed.success()&&failed.message().contains("fixture search failure"),"tool error returned transparently");fails(failed::requireData,"failed tool cannot masquerade as empty successful data");
        AtomicInteger requests=new AtomicInteger();List<String> urls=new ArrayList<>();
        HttpTransport transport=new HttpTransport(){public String request(String url,Map<String,String> headers,String body){requests.incrementAndGet();urls.add(url);check(headers.get("Accept").equals("application/json"),"Adzuna explicitly requests JSON rather than default JSONP");return "{\"results\":[{\"title\":\"Developer\",\"redirect_url\":\"https://example.com/job\",\"description\":\"partial Java excerpt\"}]}";}};
        ToolManager liveShape=manager(new AdzunaTool("fixture-app","fixture-key",transport));
        check(liveShape.executeTool("adzuna",Map.of("title","Java Developer","company","A & B","country","CA")).requireData().size()==1,"real Adzuna tool decodes fixture schema");
        check(urls.getFirst().contains("/ca/search/1?")&&urls.getFirst().contains("what=Java+Developer+A+%26+B"),"Canadian search and query encoding");
        liveShape.executeTool("adzuna",Map.of("title","Developer","company","Acme","country","US"));check(urls.getLast().contains("/us/search/1?"),"US search route");
        int before=requests.get();check(!liveShape.executeTool("adzuna",Map.of("title","Developer","company","Acme","country","GB")).success()&&requests.get()==before,"invalid country rejected before HTTP");
        HttpTransport broken=new HttpTransport(){public String request(String url,Map<String,String> headers,String body){requests.incrementAndGet();throw new IllegalStateException("fixture service unavailable");}};
        before=requests.get();check(!manager(new AdzunaTool("id","key",broken)).executeTool("adzuna",Map.of("title","Developer","company","Acme","country","CA")).success()&&requests.get()-before==2,"Adzuna retries exactly once");
        before=requests.get();check(!manager(new AdzunaTool("","",broken)).executeTool("adzuna",Map.of("title","Developer","company","Acme","country","CA")).success()&&requests.get()==before,"absent Adzuna keys send no request");
        FixedTool notCalled=new FixedTool("stopped",List.of());Thread.currentThread().interrupt();try{fails(()->manager(notCalled).executeTool("stopped",Map.of()),"cancelled tool not started");}finally{Thread.interrupted();}check(notCalled.calls==0,"cancellation does not call external tool");
    }
    private static void jobIntake(Path temp)throws Exception {
        var store=new SqliteStore(temp.resolve("jobs.db"));var jobs=new SqliteJobRepository(store);Approvals none=new Approvals();FixedTool search=new FixedTool("adzuna",HITS);
        var intake=new JobDescriptionIntake(never(),manager(search),store,new ApprovalGate(none));
        JobDescription original=new GetJobDescriptionAction(intake,pasted("Java SQL\nApply by: 2026-12-01")).execute();
        check(original.sourceLabel()==SourceLabel.USER_PASTED&&original.deadline().toString().equals("2026-12-01"),"pasted command saves official source and explicit date");
        check(jobs.current().equals(original)&&search.calls==0&&none.prompts.isEmpty(),"pasted path saves through repository without tools/AI/approval");
        fails(()->intake.add("Developer","Acme","GB","Java"),"invalid country before any external work");fails(()->intake.add("Developer","Acme","CA"," "),"blank pasted input not converted to estimate");
        AtomicInteger decisions=new AtomicInteger();Ai chooseSecond=ai((p,t)->{throw new AssertionError("Declined expansion must not invoke Claude");},(q,o)->{decisions.incrementAndGet();return choose(o,"1");});
        Approvals keep=new Approvals(false,true);var partial=new JobDescriptionIntake(chooseSecond,manager(search),store,new ApprovalGate(keep)).find("Developer","Acme","CA");
        check(partial.text().equals("Java SQL partial second")&&partial.sourceLabel()==SourceLabel.ADZUNA_EXCERPT,"Jev selects closest excerpt by fixed option");
        check(decisions.get()==1&&partial.adzunaUrl().equals("https://example.com/job/2"),"one Jev decision and actual source URL retained");
        check(keep.prompts.getFirst().contains("partial")&&keep.prompts.getFirst().contains("Jobs by Adzuna"),"partial label/attribution shown before estimate approval");
        Approvals reject=new Approvals(false,false);fails(()->new JobDescriptionIntake(chooseSecond,manager(search),store,new ApprovalGate(reject)).find("Developer","Acme","CA"),"rejecting both choices saves nothing");check(jobs.current().equals(partial),"rejected intake retains selected job");
        AtomicInteger generated=new AtomicInteger();Ai expanding=ai((prompt,tier)->{generated.incrementAndGet();check(tier==ModelTier.SONNET,"estimates use planned Sonnet tier");check(prompt.contains("Acme")&&prompt.contains("Java SQL partial second"),"expansion receives role/company/country plus excerpt");return "{\"text\":\"ESTIMATED Java developer role\"}";},(q,o)->choose(o,"1"));
        Approvals expand=new Approvals(true);var estimate=new JobDescriptionIntake(expanding,manager(search),store,new ApprovalGate(expand)).find("Developer","Acme","CA");
        check(estimate.sourceLabel()==SourceLabel.EXPANDED_ESTIMATE&&estimate.deadline()==null&&estimate.adzunaUrl().equals(partial.adzunaUrl()),"expanded estimate labeled and linked, no invented deadline");check(generated.get()==1&&expand.prompts.size()==1,"approved expansion generates exactly once");
        FixedTool empty=new FixedTool("adzuna",List.of());AtomicInteger fullCalls=new AtomicInteger();Ai full=ai((p,t)->{fullCalls.incrementAndGet();check(t==ModelTier.SONNET&&p.contains("Acme")&&p.contains("CA"),"full estimate has grounded request context");return "{\"text\":\"FULL ESTIMATE of a Java role\"}";},(q,o)->{throw new AssertionError("No listing to classify");});
        Approvals fullApproval=new Approvals(true);var fullEstimate=new JobDescriptionIntake(full,manager(empty),store,new ApprovalGate(fullApproval)).find("Developer","Acme","CA");check(fullEstimate.sourceLabel()==SourceLabel.FULL_ESTIMATE&&fullCalls.get()==1,"no-result approved full estimate");
        Approvals decline=new Approvals(false);fails(()->new JobDescriptionIntake(never(),manager(empty),store,new ApprovalGate(decline)).find("Developer","Acme","CA"),"unrequested full estimate not generated");check(jobs.current().equals(fullEstimate),"declined fallback preserves existing selected job");
        AtomicInteger failures=new AtomicInteger();Ai failedExpansion=ai((p,t)->{failures.incrementAndGet();return "malformed";},(q,o)->choose(o,"0"));Approvals preserve=new Approvals(true,true);
        var recovered=new JobDescriptionIntake(failedExpansion,manager(search),store,new ApprovalGate(preserve)).find("Developer","Acme","CA");check(failures.get()==2&&recovered.sourceLabel()==SourceLabel.ADZUNA_EXCERPT,"model failure retries once and approved original excerpt retained");
        check(preserve.prompts.getLast().contains("failed"),"fallback explains expansion failure");
        Approvals failedFull=new Approvals(true);failures.set(0);fails(()->new JobDescriptionIntake(failedExpansion,manager(empty),store,new ApprovalGate(failedFull)).find("Developer","Acme","CA"),"failed full estimate not persisted");check(failures.get()==2&&jobs.current().equals(recovered),"failed generated fallback preserves selected job");
        FixedTool unavailable=new FixedTool("adzuna",List.of());unavailable.fail=true;Approvals offline=new Approvals(true);var fallback=new JobDescriptionIntake(full,manager(unavailable),store,new ApprovalGate(offline)).find("Developer","Acme","CA");check(fallback.isEstimate()&&offline.prompts.getFirst().contains("failure"),"tool failure explained before approved estimate");
        Thread.currentThread().interrupt();try{fails(()->new JobDescriptionIntake(never(),manager(search),store,new ApprovalGate(new Approvals())).find("Developer","Acme","CA"),"cancelled search does not prompt for fallback");}finally{Thread.interrupted();}check(jobs.current().equals(fallback),"cancelled intake preserves selected job");
        RepositoryTest.sql(temp.resolve("jobs.db"),"CREATE TRIGGER reject_job BEFORE INSERT ON documents WHEN NEW.kind='job' BEGIN SELECT RAISE(ABORT,'fixture write failure'); END;");
        Approvals writeFailure=new Approvals(true);int generatedBefore=generated.get();
        fails(()->new JobDescriptionIntake(expanding,manager(search),store,new ApprovalGate(writeFailure)).find("Developer","Acme","CA"),"storage failure is not misreported as a model failure or retried as another source");
        check(writeFailure.prompts.size()==1&&generated.get()==generatedBefore+1&&jobs.current().equals(fallback),"failed write preserves selection without an unrelated fallback approval");
        RepositoryTest.sql(temp.resolve("jobs.db"),"DROP TRIGGER reject_job;");
    }
    private static void resumeIntake(Path temp)throws Exception {
        var store=new SqliteStore(temp.resolve("resumes.db"));AtomicInteger calls=new AtomicInteger();Ai extraction=ai((prompt,tier)->{calls.incrementAndGet();check(tier==ModelTier.HAIKU,"profile extraction uses Haiku");return profileJson();},(q,o)->{throw new AssertionError("Resume extraction does not use Jev");});
        var importer=new ResumeImporter(extraction,store);var repository=new SqliteResumeRepository(store);
        var pasted=new ImportResumeAction(importer,RESUME).execute();check(pasted.getSkills().size()==2&&pasted.education().equals(List.of("BSc"))&&pasted.projects().equals(List.of("Career application"))&&pasted.certifications().equals(List.of("Java certificate")),"all profile sections extracted and persisted through pasted strategy");
        check(pasted.getSkills().stream().allMatch(s->s.level().equals("UNSPECIFIED")),"no inferred skill proficiency");
        Path markdown=temp.resolve("resume.MD");Files.writeString(markdown,RESUME);var imported=new ImportResumeAction(importer,markdown).execute();check(imported.rawText().equals(RESUME)&&repository.findAll().size()==2&&repository.findById(pasted.id()).equals(pasted),"file command preserves old base and selects new profile");
        Path docx=temp.resolve("resume.docx");writeDocx(docx,"<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body><w:p><w:r><w:t>"+RESUME+"</w:t></w:r></w:p></w:body></w:document>");
        check(importer.importResume(docx).warning().isEmpty(),"DOCX command extracts supported structured profile");String selected=importer.current().id();int before=calls.get();
        fails(()->importer.importText(" "),"blank resume rejected before inference");fails(()->importer.importResume(temp.resolve("missing.pdf")),"missing file rejected");fails(()->importer.importResume(temp),"directory input rejected");
        Path oversized=temp.resolve("large.txt");try(var file=new RandomAccessFile(oversized.toFile(),"rw")){file.setLength(10_000_001);}fails(()->importer.importResume(oversized),"oversized file rejected");
        Path invalid=temp.resolve("invalid.txt");Files.write(invalid,new byte[]{(byte)0xc3,(byte)0x28});fails(()->importer.importResume(invalid),"invalid UTF-8 rejected");
        Path corrupt=temp.resolve("corrupt.docx");Files.writeString(corrupt,"not a Word document");fails(()->importer.importResume(corrupt),"corrupt DOCX rejected");
        Path xml=temp.resolve("hostile.docx");writeDocx(xml,"<!DOCTYPE foo [<!ENTITY steal SYSTEM 'file:///etc/passwd'>]><w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:p>&steal;</w:p></w:document>");fails(()->importer.importResume(xml),"external entity extraction rejected");
        Path unknown=temp.resolve("resume.rtf");Files.writeString(unknown,"text");fails(()->importer.importResume(unknown),"unsupported format rejected");check(calls.get()==before&&importer.current().id().equals(selected),"bad file inputs neither call AI nor replace existing profile");
        AtomicInteger retry=new AtomicInteger();Ai malformed=ai((p,t)->{retry.incrementAndGet();return "malformed";},(q,o)->{throw new AssertionError("No decisions expected");});var raw=new ResumeImporter(malformed,store).importText(RESUME);check(retry.get()==2&&raw.rawText().equals(RESUME)&&!raw.warning().isEmpty()&&raw.getSkills().isEmpty(),"malformed inference retries once then clearly saves raw text");
        Ai invented=ai((p,t)->profileJson().replace("BSc","Invented PhD"),(q,o)->{throw new AssertionError("No decisions expected");});check(!new ResumeImporter(invented,store).importText(RESUME).warning().isEmpty(),"invented education rejected, raw resume retained");
        int count=repository.findAll().size();Thread.currentThread().interrupt();try{fails(()->importer.importText(RESUME),"cancelled extraction is not converted to raw-fallback success");}finally{Thread.interrupted();}check(repository.findAll().size()==count,"cancelled extraction writes no profile");
        ResumeParserFactory factory=new ResumeParserFactory();check(factory.createParser("pasted") instanceof PastedTextResumeParser&&factory.createParser("pasted") instanceof ResumeParser,"pasted parser shares the strategy contract");check(factory.createParser("doc") instanceof DocResumeParser&&factory.createParser("pdf") instanceof PdfResumeParser,"Word/PDF factory strategies present");
    }

    private static void wrappedLiveShape(Path temp)throws Exception {
        var store=new SqliteStore(temp.resolve("wrapped.db"));AtomicInteger requests=new AtomicInteger();String[] content={"```json\n"+profileJson()+"\n```"};
        HttpTransport transport=new HttpTransport(){public String request(String url,Map<String,String> headers,String body){requests.incrementAndGet();return Json.write(Map.of("type","message","role","assistant","stop_reason","end_turn","content",List.of(Map.of("type","text","text",content[0]))));}};
        Ai ai=new Ai(new ClaudeAdapter(new AnthropicApi("test-only-key",transport)),(q,o)->{throw new AssertionError("Extraction must not invoke decisions");});
        AgentController.UserInteraction ui=new AgentController.UserInteraction(){public boolean askApproval(String prompt){throw new AssertionError("No input approval expected");}public String readText(String prompt){throw new AssertionError("No additional input expected");}};
        AgentEventPublisher events=new AgentEventPublisher();List<String> types=new ArrayList<>();events.attach(e->types.add(e.type()));
        var controller=new AgentController(store,ai,new AdzunaTool("","",transport),new WebSearchTool("",transport),ui,events);
        Path file=temp.resolve("wrapped-resume.md");Files.writeString(file,RESUME);
        ResumeProfile profile=(ResumeProfile)controller.runFeature("resume import",List.of(file.toString()));
        check(profile.warning().isEmpty()&&profile.getSkills().size()==2&&profile.education().equals(List.of("BSc")),"fenced Claude response extracts actual profile through shared facade");
        check(requests.get()==1&&types.equals(List.of("completed")),"valid wrapped output completes without wasteful retry");
        check(((ResumeProfile)controller.runFeature("resume show",List.of())).id().equals(profile.id()),"structured wrapped output persists as selected profile");
        content[0]="This is not JSON";types.clear();ResumeProfile fallback=(ResumeProfile)controller.runFeature("resume import",List.of(file.toString()));
        check(!fallback.warning().isBlank()&&fallback.getSkills().isEmpty()&&fallback.rawText().equals(RESUME),"invalid prose still produces explicitly labeled raw fallback");
        check(requests.get()==3&&types.equals(List.of("warning")),"failed extraction retries once and reports warning rather than completed");
        check(new SqliteResumeRepository(store).findById(profile.id()).equals(profile),"failed subsequent extraction preserves earlier structured base");
    }
    private static void writeDocx(Path path,String xml)throws Exception {try(var zip=new ZipOutputStream(Files.newOutputStream(path))){zip.putNextEntry(new ZipEntry("word/document.xml"));zip.write(xml.getBytes(StandardCharsets.UTF_8));zip.closeEntry();}}
    private static void optionalPdf(Path temp)throws Exception {
        Path pdf=temp.resolve("resume.pdf");writePdf(pdf,"Java SQL resume");
        try{Class.forName("org.apache.pdfbox.Loader");}
        catch(ClassNotFoundException e){
            fails(()->{try{new PdfResumeParser().extractText(pdf.toString());}catch(Exception failure){throw new IllegalArgumentException(failure);}},"missing PDFBox is an explicit error, never fake extraction");
            System.out.println("SKIP: real PDF extraction/locked/scanned fixture checks require the Maven PDFBox classpath.");return;
        }
        check(new PdfResumeParser().extractText(pdf.toString()).contains("Java SQL resume"),"actual PDFBox extracts generated PDF fixture");
        Path blank=temp.resolve("scanned-like.pdf");writePdf(blank,"");fails(()->{try{new PdfResumeParser().extractText(blank.toString());}catch(Exception e){throw new IllegalArgumentException(e);}},"PDF with no readable text is rejected");
        Path corrupt=temp.resolve("corrupt.pdf");Files.writeString(corrupt,"not a PDF");fails(()->{try{new PdfResumeParser().extractText(corrupt.toString());}catch(Exception e){throw new IllegalArgumentException(e);}},"corrupt PDF rejected");
        Object document=Class.forName("org.apache.pdfbox.Loader").getMethod("loadPDF",java.io.File.class).invoke(null,pdf.toFile());Path locked=temp.resolve("locked.pdf");
        try{
            Class<?> access=Class.forName("org.apache.pdfbox.pdmodel.encryption.AccessPermission");Class<?> policyType=Class.forName("org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy");
            Object policy=policyType.getConstructor(String.class,String.class,access).newInstance("fixture-owner","fixture-user",access.getConstructor().newInstance());policyType.getMethod("setEncryptionKeyLength",int.class).invoke(policy,128);
            document.getClass().getMethod("protect",Class.forName("org.apache.pdfbox.pdmodel.encryption.ProtectionPolicy")).invoke(document,policy);document.getClass().getMethod("save",java.io.File.class).invoke(document,locked.toFile());
        }finally{((AutoCloseable)document).close();}
        fails(()->{try{new PdfResumeParser().extractText(locked.toString());}catch(Exception e){throw new IllegalArgumentException(e);}},"password-protected PDF rejected");
    }
    private static void legacyWord(Path temp)throws Exception {
        Path corrupt=temp.resolve("corrupt.doc");Files.writeString(corrupt,"not a binary Word file");
        fails(()->{try{new DocResumeParser().extractText(corrupt.toString());}catch(Exception e){throw new IllegalArgumentException(e);}},"invalid legacy Word input does not return fake text");
        try{Class.forName("org.apache.poi.hwpf.extractor.WordExtractor");System.out.println("NOTE: legacy Word rejection checked; valid .doc import still requires a real sample walkthrough.");}
        catch(ClassNotFoundException e){System.out.println("SKIP: valid legacy Word .doc extraction requires Maven Apache POI and a real sample.");}
    }
    private static void writePdf(Path path,String text)throws Exception {
        String content="BT /F1 12 Tf 40 700 Td ("+text+") Tj ET\n";
        List<String> objects=List.of("<< /Type /Catalog /Pages 2 0 R >>","<< /Type /Pages /Kids [3 0 R] /Count 1 >>","<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>","<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>","<< /Length "+content.getBytes(StandardCharsets.US_ASCII).length+" >>\nstream\n"+content+"endstream");
        ByteArrayOutputStream out=new ByteArrayOutputStream();out.write("%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII));List<Integer> offsets=new ArrayList<>();
        for(int i=0;i<objects.size();i++){offsets.add(out.size());out.write(((i+1)+" 0 obj\n"+objects.get(i)+"\nendobj\n").getBytes(StandardCharsets.US_ASCII));}int xref=out.size();out.write("xref\n0 6\n0000000000 65535 f \n".getBytes(StandardCharsets.US_ASCII));for(int offset:offsets)out.write(String.format(Locale.ROOT,"%010d 00000 n \n",offset).getBytes(StandardCharsets.US_ASCII));out.write(("trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n"+xref+"\n%%EOF\n").getBytes(StandardCharsets.US_ASCII));Files.write(path,out.toByteArray());
    }
}
