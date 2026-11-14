package career;

import java.util.*;
import java.util.regex.*;
import static career.Models.*;

public final class ResumeTailorService {
    private final Ai ai;private final SqliteResumeRepository repository;private final ApprovalGate gate;
    public ResumeTailorService(Ai ai,SqliteStore store,ApprovalGate gate){this.ai=ai;this.repository=new SqliteResumeRepository(store);this.gate=gate;}
    public ResumeVersion tailor(JobDescription job,ResumeProfile profile){
        List<ResumeChange> proposed=ai.ask("Propose up to five conservative resume edits to emphasize relevant experience. Return {\"changes\":[{\"original\":\"exact unique substring from resume\",\"revised\":\"reordered wording\",\"reason\":\"why\"}]}. Revised text must use ONLY words and numbers already in its original substring, with the same word counts. Do not add skills or claims. Reordering clauses is acceptable.",Map.of("resume",profile.rawText(),"job",job.text()),ModelTier.SONNET,m->{
            List<ResumeChange> changes=new ArrayList<>();for(Object o:Json.list(m,"changes")){var c=Json.object(o);String old=Json.text(c,"original"),next=Json.text(c,"revised"),reason=Json.text(c,"reason");
                if(!safeChange(profile.rawText(),old,next))throw new IllegalArgumentException("Rejected unsupported or ambiguous resume change");changes.add(new ResumeChange(old,next,reason,false));}
            if(changes.size()>5)throw new IllegalArgumentException("Too many changes");return changes;
        });
        String text=profile.rawText();List<ResumeChange> reviewed=new ArrayList<>();
        for(var c:proposed){if(!safeChange(text,c.original(),c.revised()))continue;
            boolean accepted=gate.review("Resume edit:\nORIGINAL: "+c.original()+"\nREVISED: "+c.revised()+"\nREASON: "+c.reason()+"\nAccept this change?",false);
            if(accepted)text=text.replace(c.original(),c.revised());reviewed.add(new ResumeChange(c.original(),c.revised(),c.reason(),accepted));
        }
        ResumeVersion version=new ResumeVersion(id(),job.id(),profile.id(),java.time.Instant.now(),text,reviewed);repository.save(version);return version;
    }
    // shortcut: extractive edits only; add a human-reviewed evidence model before allowing new wording.
    public static boolean safeChange(String base,String original,String revised){
        if(original.isBlank()||revised.isBlank()||original.equals(revised))return false;
        int at=base.indexOf(original);if(at<0||base.indexOf(original,at+1)>=0)return false;
        return tokens(original).equals(tokens(revised));
    }
    private static Map<String,Integer> tokens(String s){
        Matcher m=Pattern.compile("[\\p{L}\\p{N}+#.]+",Pattern.UNICODE_CHARACTER_CLASS).matcher(s.toLowerCase(Locale.ROOT));Map<String,Integer> out=new TreeMap<>();while(m.find())out.merge(m.group(),1,Integer::sum);return out;
    }
}
