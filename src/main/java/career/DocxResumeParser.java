package career;

import java.nio.file.*;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilderFactory;

public final class DocxResumeParser implements ResumeParser {
    public String extractText(String source)throws Exception {
        try(ZipFile zip=new ZipFile(Path.of(source).toFile())){
            var entry=zip.getEntry("word/document.xml");if(entry==null)throw new IllegalArgumentException("Not a Word DOCX document");
            try(var in=zip.getInputStream(entry)){
                byte[] xml=in.readNBytes(2_000_001);if(xml.length>2_000_000)throw new IllegalArgumentException("DOCX content too large");
                var factory=DocumentBuilderFactory.newInstance();factory.setNamespaceAware(true);
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
                factory.setFeature("http://xml.org/sax/features/external-general-entities",false);
                factory.setFeature("http://xml.org/sax/features/external-parameter-entities",false);
                factory.setXIncludeAware(false);factory.setExpandEntityReferences(false);
                var builder=factory.newDocumentBuilder();
                builder.setErrorHandler(new org.xml.sax.helpers.DefaultHandler(){
                    @Override public void error(org.xml.sax.SAXParseException e)throws org.xml.sax.SAXException{throw e;}
                    @Override public void fatalError(org.xml.sax.SAXParseException e)throws org.xml.sax.SAXException{throw e;}
                });
                var document=builder.parse(new java.io.ByteArrayInputStream(xml));
                var paragraphs=document.getElementsByTagNameNS("http://schemas.openxmlformats.org/wordprocessingml/2006/main","p");
                StringBuilder text=new StringBuilder();for(int i=0;i<paragraphs.getLength();i++)text.append(paragraphs.item(i).getTextContent()).append('\n');
                return Models.required(text.toString(),"DOCX resume text");
            }
        }
    }
}
