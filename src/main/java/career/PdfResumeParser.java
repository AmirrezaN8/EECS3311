package career;

import java.nio.file.*;

/** PDFBox is supplied by the Maven package; the offline build reports its absence explicitly. */
public final class PdfResumeParser implements ResumeParser {
    public String extractText(String source)throws Exception {
        try{
            Object document=Class.forName("org.apache.pdfbox.Loader").getMethod("loadPDF",java.io.File.class).invoke(null,Path.of(source).toFile());
            try{
                if((boolean)document.getClass().getMethod("isEncrypted").invoke(document))throw new IllegalArgumentException("Password-protected PDF; paste readable text instead");
                Class<?> stripper=Class.forName("org.apache.pdfbox.text.PDFTextStripper");
                Object extractor=stripper.getConstructor().newInstance();stripper.getMethod("setSortByPosition",boolean.class).invoke(extractor,true);
                String text=(String)stripper.getMethod("getText",Class.forName("org.apache.pdfbox.pdmodel.PDDocument")).invoke(extractor,document);
                if(text.isBlank())throw new IllegalArgumentException("PDF contains no readable text (possibly scanned); paste text instead");return Models.required(text,"PDF resume text");
            }finally{((AutoCloseable)document).close();}
        }catch(ClassNotFoundException e){throw new IllegalArgumentException("PDF import requires the Maven-packaged build with PDFBox; paste resume text in this offline build",e);}
        catch(java.lang.reflect.InvocationTargetException e){throw new IllegalArgumentException("PDF is corrupt, locked, or unreadable; paste readable text instead",e.getCause());}
    }
}
