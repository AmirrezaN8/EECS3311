package career;

public record ResumeChange(String original, String revised, String reason, boolean accepted) {
    public ResumeChange { original=Models.required(original,"Original text");revised=Models.required(revised,"Revised text");reason=Models.required(reason,"Change reason"); }
}
