package career;

import java.nio.file.*;
import java.util.Locale;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilderFactory;

public final class ResumeParserFactory {
    public ResumeParser createParser(Path path){
        String name=path.getFileName().toString().toLowerCase(Locale.ROOT);
        if(name.endsWith(".pdf"))return new PdfResumeParser();
        if(name.endsWith(".docx"))return new DocxResumeParser();
        if(name.endsWith(".md")||name.endsWith(".txt"))return new MarkdownResumeParser();
        throw new IllegalArgumentException("Supported formats: PDF, DOCX, Markdown, UTF-8 text. Paste legacy .doc content as text.");
    }
    public static final class MarkdownResumeParser implements ResumeParser {
        public String extractText(Path p)throws Exception{return Files.readString(p);}
    }
    public static final class PastedTextResumeParser {
        public String extractText(String text){return Models.required(text,"Resume text");}
    }
    public static final class DocxResumeParser implements ResumeParser {
        public String extractText(Path p)throws Exception{
            try(ZipFile zip=new ZipFile(p.toFile())){
                var entry=zip.getEntry("word/document.xml");if(entry==null)throw new IllegalArgumentException("Not a Word DOCX document");
                try(var in=zip.getInputStream(entry)){
                    byte[] xml=in.readNBytes(2_000_001);if(xml.length>2_000_000)throw new IllegalArgumentException("DOCX content too large");
                    var f=DocumentBuilderFactory.newInstance();f.setNamespaceAware(true);f.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);f.setFeature("http://xml.org/sax/features/external-general-entities",false);f.setFeature("http://xml.org/sax/features/external-parameter-entities",false);f.setXIncludeAware(false);f.setExpandEntityReferences(false);
                    var doc=f.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(xml));var paragraphs=doc.getElementsByTagNameNS("http://schemas.openxmlformats.org/wordprocessingml/2006/main","p");StringBuilder b=new StringBuilder();
                    for(int i=0;i<paragraphs.getLength();i++)b.append(paragraphs.item(i).getTextContent()).append('\n');return b.toString();
                }
            }
        }
    }
    public static final class PdfResumeParser implements ResumeParser {
        public String extractText(Path p)throws Exception{
            try{
                Object doc=Class.forName("org.apache.pdfbox.Loader").getMethod("loadPDF",java.io.File.class).invoke(null,p.toFile());
                try{
                    if((boolean)doc.getClass().getMethod("isEncrypted").invoke(doc))throw new IllegalArgumentException("Password-protected PDF: paste text instead");
                    Class<?> stripper=Class.forName("org.apache.pdfbox.text.PDFTextStripper");
                    return (String)stripper.getMethod("getText",Class.forName("org.apache.pdfbox.pdmodel.PDDocument")).invoke(stripper.getConstructor().newInstance(),doc);
                }finally{((AutoCloseable)doc).close();}
            }catch(ClassNotFoundException e){throw new IllegalArgumentException("PDF import needs the Maven-packaged build with PDFBox; paste resume text in this offline build",e);}
        }
    }
}
