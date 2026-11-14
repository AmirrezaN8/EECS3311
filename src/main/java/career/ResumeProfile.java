package career;

import java.util.*;

public record ResumeProfile(String id, String rawText, List<Skill> skills, List<String> education,
                            List<String> experience, List<String> projects, List<String> certifications, String warning) {
    public ResumeProfile {
        id=Models.required(id,"Resume ID");rawText=Models.required(rawText,"Resume text");
        skills=List.copyOf(skills);education=List.copyOf(education);experience=List.copyOf(experience);
        projects=List.copyOf(projects);certifications=List.copyOf(certifications);Objects.requireNonNull(warning,"Warning");
    }
    public List<Skill> getSkills(){return skills;}
}
