package career;

import java.util.*;
import java.util.regex.*;
import static career.Models.*;

public final class ResumeTailorService {
    private final Ai ai;private final SqliteResumeRepository repository;private final ApprovalGate gate;
    public ResumeTailorService(Ai ai,SqliteStore store,ApprovalGate gate){this.ai=ai;this.repository=new SqliteResumeRepository(store);this.gate=gate;}
    public ResumeVersion tailor(JobDescription job,ResumeProfile profile){
        List<ResumeChange> proposed=ai.ask("Propose up to five conservative wording improvements that emphasize job-relevant resume evidence. Return {\"changes\":[{\"original\":\"exact unique substring from resume\",\"revised\":\"improved wording\",\"reason\":\"why this helps for the job\"}]}. Preserve all substantive words and their counts, technical names, numbers and negations. You may reorder clauses, change capitalization/punctuation, add/remove only a/an/the/of/with/using, and use these word forms interchangeably: develop/developed/developing/development; application/applications; test/tests/testing/tested; collaborate/collaborated/collaborating/collaboration. Example: Developed Java applications -> Java application development. Do not invent skills, responsibilities or achievements. Changes must not overlap.",Map.of("resume",profile.rawText(),"job",job.text()),ModelTier.SONNET,m->{
            List<ResumeChange> changes=new ArrayList<>();for(Object o:Json.list(m,"changes")){var c=Json.object(o);String old=Json.text(c,"original"),next=Json.text(c,"revised"),reason=Json.text(c,"reason");
                if(!safeChange(profile.rawText(),old,next))throw new IllegalArgumentException("Rejected unsupported or ambiguous resume change");changes.add(new ResumeChange(old,next,reason,false));}
            if(changes.size()>5)throw new IllegalArgumentException("Too many changes");
            for(int i=0;i<changes.size();i++)for(int j=0;j<i;j++){
                int a=profile.rawText().indexOf(changes.get(i).original()),b=profile.rawText().indexOf(changes.get(j).original());
                if(a<b+changes.get(j).original().length()&&b<a+changes.get(i).original().length())throw new IllegalArgumentException("Resume edits overlap; propose separate source passages");
            }
            return changes;
        });
        String text=profile.rawText();List<ResumeChange> reviewed=new ArrayList<>();
        for(var c:proposed){if(!safeChange(text,c.original(),c.revised()))continue;
            boolean accepted=gate.reviewResumeChange(c);
            if(accepted)text=text.replace(c.original(),c.revised());reviewed.add(new ResumeChange(c.original(),c.revised(),c.reason(),accepted));
        }
        ResumeVersion version=new ResumeVersion(id(),job.id(),profile.id(),java.time.Instant.now(),text,reviewed);repository.save(version);return version;
    }
    // shortcut: limited word forms only; expand evidence validation before supporting broader paraphrases.
    public static boolean safeChange(String base,String original,String revised){
        if(original.isBlank()||revised.isBlank()||original.equals(revised))return false;
        int at=base.indexOf(original);if(at<0||base.indexOf(original,at+1)>=0)return false;
        return !tokens(original).isEmpty()&&tokens(original).equals(tokens(revised));
    }
    private static Map<String,Integer> tokens(String s){
        Matcher m=Pattern.compile("[\\p{L}\\p{N}+#.]+",Pattern.UNICODE_CHARACTER_CLASS).matcher(s.toLowerCase(Locale.ROOT));Map<String,Integer> out=new TreeMap<>();
        while(m.find()){
            String word=m.group().replaceAll("[.]+$","");
            if(word.isEmpty()||Set.of("a","an","the","of","with","using").contains(word))continue;
            word=switch(word){
                case "developed","developing","development" -> "develop";
                case "applications" -> "application";
                case "tests","testing","tested" -> "test";
                case "collaborated","collaborating","collaboration" -> "collaborate";
                default -> word;
            };
            out.merge(word,1,Integer::sum);
        }
        return out;
    }
}
