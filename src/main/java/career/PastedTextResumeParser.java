package career;

public final class PastedTextResumeParser implements ResumeParser {
    public String extractText(String source){return Models.required(source,"Resume text");}
}
