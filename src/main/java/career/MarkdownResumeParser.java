package career;

import java.nio.file.*;

public final class MarkdownResumeParser implements ResumeParser {
    public String extractText(String source)throws Exception{return Models.required(Files.readString(Path.of(source)),"Resume text");}
}
