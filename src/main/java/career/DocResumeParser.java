package career;

import java.nio.file.*;

/** Legacy Word 97-2003 input uses Apache POI rather than a handwritten binary parser. */
public final class DocResumeParser implements ResumeParser {
    public String extractText(String source)throws Exception {
        try(var input=Files.newInputStream(Path.of(source))){
            Class<?> type=Class.forName("org.apache.poi.hwpf.extractor.WordExtractor");
            Object extractor=type.getConstructor(java.io.InputStream.class).newInstance(input);
            try{return Models.required((String)type.getMethod("getText").invoke(extractor),"Word resume text");}
            finally{((AutoCloseable)extractor).close();}
        }catch(ClassNotFoundException e){throw new IllegalArgumentException("Legacy Word .doc import requires the Maven-packaged build with Apache POI; paste text in this offline build",e);}
        catch(java.lang.reflect.InvocationTargetException e){throw new IllegalArgumentException("Word document is corrupt, locked, or unreadable; paste readable text instead",e.getCause());}
    }
}
