package career;

import java.nio.file.Path;

@FunctionalInterface public interface ResumeParser {String extractText(Path source) throws Exception;}
