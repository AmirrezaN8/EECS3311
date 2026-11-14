import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;

/** Reproducible, dependency-free text report with a vector UML appendix. */
public class ReportPdf {
    private static final List<String> streams=new ArrayList<>();
    private static String ascii(String s){return s.replace("—","-").replace("–","-").replace("→","->").replace("‘","'").replace("’","'").replace("“","\"").replace("”","\"").replaceAll("[^\\x20-\\x7E]","?");}
    private static String quote(String s){return "("+ascii(s).replace("\\","\\\\").replace("(","\\(").replace(")","\\)")+")";}
    private static void text(StringBuilder b,double x,double y,String s,int size,String font){b.append("BT /").append(font).append(' ').append(size).append(" Tf ").append(x).append(' ').append(y).append(" Td ").append(quote(s)).append(" Tj ET\n");}
    private static StringBuilder page(int number,String heading){StringBuilder b=new StringBuilder("0.15 0.2 0.28 rg\n");text(b,42,766,heading,10,"F2");b.append("0.75 G 42 754 m 570 754 l S 0 g\n");text(b,42,24,"EECS3311 | Career Preparation Agent | November 14, 2026",8,"F2");text(b,535,24,Integer.toString(number),8,"F2");return b;}
    private static void line(StringBuilder b,int x1,int y1,int x2,int y2,boolean dashed){
        b.append(dashed?"[3 3] 0 d\n":"[] 0 d\n").append("0.3 G ").append(x1).append(' ').append(y1).append(" m ").append(x2).append(' ').append(y2).append(" l S\n[] 0 d\n");
        double angle=Math.atan2(y2-y1,x2-x1);for(double a:new double[]{angle+.45,angle-.45})b.append(x2).append(' ').append(y2).append(" m ").append(x2-7*Math.cos(a)).append(' ').append(y2-7*Math.sin(a)).append(" l S\n");
    }
    private static void box(StringBuilder b,int x,int y,String name,String... details){
        b.append("0.96 0.97 0.99 rg ").append(x).append(' ').append(y).append(" 164 92 re f 0.3 G ").append(x).append(' ').append(y).append(" 164 92 re S 0 g\n");
        text(b,x+6,y+76,name,10,"F2");b.append(x).append(' ').append(y+64).append(" m ").append(x+164).append(' ').append(y+64).append(" l S\n");int row=y+49;for(String d:details){text(b,x+6,row,d,8,"F1");row-=12;}
    }
    private static void diagram(){
        StringBuilder b=page(streams.size()+1,"Implemented UML - selected classes and runtime relationships");
        int[] x={42,224,406};int[] y={628,466,304,142};
        box(b,x[0],y[0],"CliApp","+ run(args)","+ askApproval()", "CLI entry point");
        box(b,x[1],y[0],"AgentController","+ runFeature()","+ handleGoal()","Facade for both UIs");
        box(b,x[2],y[0],"SwingGui","+ submitGoal()","+ showChecklist()","SwingWorker / EDT");
        box(b,x[0],y[1],"Planner","- decisions","+ chooseNext()","returns AgentAction");
        box(b,x[1],y[1],"AgentSession","- state: AgentState","+ handleInput()","~ setState()");
        box(b,x[2],y[1],"AgentEventPublisher","+ attach(observer)","+ publish(event)","ProgressObserver callbacks");
        box(b,x[0],y[2],"<<interface>> DecisionClient","+ decide(question,","  options): Decision");
        box(b,x[1],y[2],"<<interface>> AgentAction","+ name()","+ describe()","+ execute()");
        box(b,x[2],y[2],"<<interface>> AgentState","+ advance(session)","State transition behavior");
        box(b,x[0],y[3],"Ai","+ ask(..., validate)","+ decide()","uses LLMClient adapter");
        box(b,x[1],y[3],"FeatureCommand","- operation: Supplier","+ execute()","captures service operation");
        box(b,x[2],y[3],"Concrete states","Idle / Planning","AwaitingApproval","Executing / Finished");
        line(b,206,674,224,674,false);line(b,406,674,388,674,false);
        line(b,270,628,130,558,false);line(b,306,628,306,558,false);line(b,348,628,490,558,false);
        line(b,124,466,124,396,false);line(b,124,234,124,304,true);
        line(b,306,466,306,396,false);line(b,306,234,306,304,true);
        line(b,358,466,450,396,false);line(b,488,234,488,304,true);
        text(b,42,111,"Solid arrow: uses/owns/selects. Dashed arrow: implements interface.",9,"F2");
        text(b,42,94,"Service/domain/parser details: docs/implemented-class-diagram.puml",9,"F2");
        text(b,42,77,"Sequence and use-case sources are supplied beside the full class diagram.",9,"F2");
        streams.add(b.toString());
    }
    public static void main(String[] args)throws Exception {
        if(args.length!=2)throw new IllegalArgumentException("Usage: java scripts/ReportPdf.java source.md report.pdf");
        List<String> lines=Files.readAllLines(Path.of(args[0]));StringBuilder b=null;int y=0;
        for(String original:lines){
            String s=ascii(original);boolean heading=s.startsWith("#");String clean=s.replaceFirst("^#+\\s*","");
            List<String> wrapped=new ArrayList<>();if(clean.isEmpty())wrapped.add("");else while(!clean.isEmpty()){
                int length=Math.min(heading?80:96,clean.length()),cut=length;
                if(clean.length()>length){int space=clean.lastIndexOf(' ',length);if(space>20)cut=space;}
                wrapped.add(clean.substring(0,cut));clean=clean.substring(cut).stripLeading();
            }
            int needed=wrapped.size()*13+(heading?10:0);
            if(b==null||y-needed<48){if(b!=null)streams.add(b.toString());b=page(streams.size()+1,"Stage 2 Implementation Report");y=730;}
            if(heading)y-=8;
            for(String line:wrapped){if(y<48){streams.add(b.toString());b=page(streams.size()+1,"Stage 2 Implementation Report");y=730;}text(b,42,y,line,heading?12:9,heading?"F2":"F1");y-=13;}
        }
        if(b!=null)streams.add(b.toString());diagram();
        List<byte[]> objects=new ArrayList<>();objects.add("<< /Type /Catalog /Pages 2 0 R >>".getBytes(StandardCharsets.US_ASCII));
        StringBuilder kids=new StringBuilder();for(int i=0;i<streams.size();i++)kids.append(5+i*2).append(" 0 R ");
        objects.add(("<< /Type /Pages /Count "+streams.size()+" /Kids [ "+kids+"] >>").getBytes(StandardCharsets.US_ASCII));
        objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Courier >>".getBytes(StandardCharsets.US_ASCII));
        objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>".getBytes(StandardCharsets.US_ASCII));
        for(int i=0;i<streams.size();i++){
            objects.add(("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents "+(6+i*2)+" 0 R >>").getBytes(StandardCharsets.US_ASCII));
            byte[] data=streams.get(i).getBytes(StandardCharsets.US_ASCII);ByteArrayOutputStream content=new ByteArrayOutputStream();content.write(("<< /Length "+data.length+" >>\nstream\n").getBytes(StandardCharsets.US_ASCII));content.write(data);content.write("endstream".getBytes(StandardCharsets.US_ASCII));objects.add(content.toByteArray());
        }
        ByteArrayOutputStream pdf=new ByteArrayOutputStream();pdf.write("%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII));List<Integer> offsets=new ArrayList<>();
        for(int i=0;i<objects.size();i++){offsets.add(pdf.size());pdf.write(((i+1)+" 0 obj\n").getBytes(StandardCharsets.US_ASCII));pdf.write(objects.get(i));pdf.write("\nendobj\n".getBytes(StandardCharsets.US_ASCII));}
        int xref=pdf.size();pdf.write(("xref\n0 "+(objects.size()+1)+"\n0000000000 65535 f \n").getBytes(StandardCharsets.US_ASCII));for(int offset:offsets)pdf.write(String.format(Locale.ROOT,"%010d 00000 n \n",offset).getBytes(StandardCharsets.US_ASCII));
        pdf.write(("trailer\n<< /Size "+(objects.size()+1)+" /Root 1 0 R >>\nstartxref\n"+xref+"\n%%EOF\n").getBytes(StandardCharsets.US_ASCII));Files.write(Path.of(args[1]),pdf.toByteArray());
        System.out.println("Generated "+args[1]+": "+streams.size()+" pages, "+pdf.size()+" bytes");
    }
}
