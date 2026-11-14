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
            return importText(factory.createParser(path).extractText(path));
        }catch(Exception e){throw new IllegalArgumentException("Resume import failed: "+e.getMessage()+". Paste readable text instead.",e);}
    }
    public ResumeProfile importText(String input){
        String text=new ResumeParserFactory.PastedTextResumeParser().extractText(input);ResumeProfile profile;
        try{
            profile=ai.ask("Extract only explicitly supported resume content. Return arrays of strings: {\"skills\":[],\"education\":[],\"experience\":[],\"projects\":[],\"certifications\":[]}. Each skill must occur literally in the supplied text.",text,ModelTier.HAIKU,m->{
                List<String> skills=Json.strings(m,"skills");for(String s:skills)if(!normalize(text).contains(normalize(s)))throw new IllegalArgumentException("Unsupported extracted skill");
                return new ResumeProfile(id(),text,skills.stream().map(name->new Skill(name,"UNSPECIFIED")).toList(),Json.strings(m,"education"),Json.strings(m,"experience"),Json.strings(m,"projects"),Json.strings(m,"certifications"),"");
            });
        }catch(RuntimeException e){profile=new ResumeProfile(id(),text,List.of(),List.of(),List.of(),List.of(),List.of(),"Raw text saved; AI extraction failed: "+e.getMessage());}
        repository.saveAndSelect(profile);return profile;
    }
    public ResumeProfile current(){return repository.current();}
}
