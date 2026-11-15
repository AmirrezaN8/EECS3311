package career;

import java.util.*;
import static career.Models.*;

public final class SkillGapService {
    private final Ai ai;private final SqliteJobRepository repository;
    public SkillGapService(Ai ai,SqliteStore store){this.ai=ai;this.repository=new SqliteJobRepository(store);}
    public SkillGapReport identify(JobDescription job,ResumeProfile profile){
        Objects.requireNonNull(job,"Job description is required");Objects.requireNonNull(profile,"Resume is required");
        List<String> skills=ai.ask("Extract required skill names occurring literally in the job. Return {\"skills\":[\"...\"]}.",job.text(),ModelTier.HAIKU,m->{
            List<String> list=Json.strings(m,"skills");if(list.size()>40)throw new IllegalArgumentException("Too many skills");
            for(String s:list)if(!containsSkill(job.text(),s))throw new IllegalArgumentException("Skill not present in posting");return list.stream().map(Models::normalize).distinct().toList();
        });
        List<SkillAssessment> assessments=new ArrayList<>();
        for(String s:skills){
            var d=ai.decide("Classify required skill "+s+" using only this resume evidence: "+profile.rawText()+". STRONG=demonstrated usage, DECENT=mentioned but limited evidence, MISSING=no evidence.",List.of("MISSING","STRONG","DECENT"));
            boolean supported=containsSkill(profile.rawText(),s);GapGroup g=supported?GapGroup.valueOf(d.choice()):GapGroup.MISSING;
            assessments.add(new SkillAssessment(s,g,supported?d.confidence():Math.min(d.confidence(),0.74),supported?"Mentioned in base resume":"No literal evidence in base resume; verify synonyms manually"));
        }
        SkillGapReport report=new SkillGapReport(job.id(),java.time.Instant.now(),assessments);repository.save(report);return report;
    }
    private static boolean containsSkill(String text,String skill){
        return java.util.regex.Pattern.compile("(?<![\\p{L}\\p{N}_])"+java.util.regex.Pattern.quote(normalize(skill))+"(?![\\p{L}\\p{N}_])").matcher(normalize(text)).find();
    }
}
