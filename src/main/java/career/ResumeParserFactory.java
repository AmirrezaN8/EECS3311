package career;

import java.nio.file.Path;
import java.util.Locale;

public final class ResumeParserFactory {
    public ResumeParser createParser(Path path){String name=path.getFileName().toString();int dot=name.lastIndexOf('.');return createParser(dot<0?"":name.substring(dot+1));}
    public ResumeParser createParser(String fileType){
        return switch(fileType.toLowerCase(Locale.ROOT)){
            case "pdf" -> new PdfResumeParser();
            case "docx" -> new DocxResumeParser();
            case "doc" -> new DocResumeParser();
            case "md","txt" -> new MarkdownResumeParser();
            case "pasted" -> new PastedTextResumeParser();
            default -> throw new IllegalArgumentException("Supported resume inputs: PDF, DOCX, DOC, Markdown, UTF-8 text, or pasted text");
        };
    }
}
