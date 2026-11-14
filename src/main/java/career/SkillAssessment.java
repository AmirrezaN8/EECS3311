package career;

import java.util.Objects;

public record SkillAssessment(String skillName, GapGroup group, double confidence, String evidence) {
    public SkillAssessment {
        skillName=Models.required(skillName,"Assessed skill");Objects.requireNonNull(group,"Skill group");
        if(!Double.isFinite(confidence)||confidence<0||confidence>1)throw new IllegalArgumentException("Invalid skill confidence");
        evidence=Models.required(evidence,"Skill evidence");
    }
    public boolean uncertain(){return confidence<0.75;}
}
