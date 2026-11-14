package career;

import java.time.Instant;
import java.util.*;

public record SkillGapReport(String jobId, Instant createdAt, List<SkillAssessment> assessments) {
    public SkillGapReport { jobId=Models.required(jobId,"Job ID");assessments=List.copyOf(assessments); }
    public List<SkillAssessment> groupedByPriority(){return assessments.stream().sorted(Comparator.comparing(SkillAssessment::group)).toList();}
}
