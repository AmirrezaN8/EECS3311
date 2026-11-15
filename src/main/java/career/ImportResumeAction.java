package career;

import java.nio.file.Path;
import java.util.Objects;

public final class ImportResumeAction implements AgentAction {
    private final ResumeImporter importer;private final Path file;private final java.util.function.Supplier<String> text;
    public ImportResumeAction(ResumeImporter importer,Path file){this.importer=Objects.requireNonNull(importer);this.file=Objects.requireNonNull(file);this.text=null;}
    public ImportResumeAction(ResumeImporter importer,String text){this.importer=Objects.requireNonNull(importer);this.file=null;Objects.requireNonNull(text);this.text=()->text;}
    public ImportResumeAction(ResumeImporter importer,java.util.function.Supplier<String> text){this.importer=Objects.requireNonNull(importer);this.file=null;this.text=Objects.requireNonNull(text);}
    public String name(){return "import resume";}
    public String describe(){return "import the resume and extract its supported profile";}
    public ResumeProfile execute(){return file==null?importer.importText(text.get()):importer.importResume(file);}
}
