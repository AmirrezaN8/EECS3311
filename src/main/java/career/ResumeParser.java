package career;

/** File parsers receive a path string; the pasted strategy receives literal resume text. */
@FunctionalInterface public interface ResumeParser { String extractText(String source) throws Exception; }
