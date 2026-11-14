package career;

import java.nio.file.*;
import java.util.*;
import static career.Models.*;

public final class ResumeImporter {
    private final Ai ai;private final SqliteResumeRepository repository;private final ResumeParserFactory factory=new ResumeParserFactory();
    public ResumeImporter(Ai ai,SqliteStore store){this.ai=ai;this.repository=new SqliteResumeRepository(store);}
    public ResumeProfile importResume(Path path){
        try{
            if(!Files.isRegularFile(path)||Files.size(path)>10_000_000)throw new IllegalArgumentException("Select a regular resume file under 10 MB");
            return structure(extract(factory.createParser(path),path.toString()));
        }catch(RuntimeException e){throw new IllegalArgumentException("Resume import failed: "+e.getMessage()+". Paste readable text instead.",e);}
        catch(java.io.IOException e){throw new IllegalArgumentException("Cannot read resume file; paste readable text instead.",e);}
    }
    public ResumeProfile importText(String text){return structure(extract(factory.createParser("pasted"),text));}
    private static String extract(ResumeParser parser,String source){
        try{return required(parser.extractText(source),"Readable resume text");}
        catch(RuntimeException e){throw e;}
        catch(Exception e){throw new IllegalArgumentException("Resume file is corrupt or unreadable; paste readable text instead",e);}
    }
    private ResumeProfile structure(String text){
        ResumeProfile profile;
        try{
            profile=ai.ask("Extract only explicitly supported resume content. Return arrays of strings: {\"skills\":[],\"education\":[],\"experience\":[],\"projects\":[],\"certifications\":[]}. Copy each item as a phrase occurring literally in the resume, without inventing qualifications. Do not infer skill proficiency.",text,ModelTier.HAIKU,m->{
                List<String> skills=supported(m,"skills",text);return new ResumeProfile(id(),text,skills.stream().map(name->new Skill(name,"UNSPECIFIED")).toList(),supported(m,"education",text),supported(m,"experience",text),supported(m,"projects",text),supported(m,"certifications",text),"");
            });
        }catch(RuntimeException e){
            if(Thread.currentThread().isInterrupted())throw e;
            profile=new ResumeProfile(id(),text,List.of(),List.of(),List.of(),List.of(),List.of(),"Raw text saved; AI extraction failed: "+e.getMessage());
        }
        repository.saveAndSelect(profile);return profile;
    }
    private static List<String> supported(Map<String,Object> result,String section,String text){
        List<String> items=Json.strings(result,section);for(String item:items)if(!normalize(text).contains(normalize(item)))throw new IllegalArgumentException("Unsupported extracted "+section+" item");
        return items.stream().distinct().toList();
    }
    public ResumeProfile current(){return repository.current();}
}
