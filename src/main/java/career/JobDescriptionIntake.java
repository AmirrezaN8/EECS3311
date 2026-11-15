package career;

import java.util.*;
import java.util.regex.*;
import static career.Models.*;

public final class JobDescriptionIntake {
    private final Ai ai;private final ToolManager tools;private final SqliteJobRepository repository;private final ApprovalGate gate;
    public JobDescriptionIntake(Ai ai,ToolManager tools,SqliteStore store,ApprovalGate gate){this.ai=ai;this.tools=tools;this.repository=new SqliteJobRepository(store);this.gate=gate;}
    public JobDescription add(String title,String company,String country,String text){
        return getDescription(new JobDescriptionRequest(title,company,country,required(text,"Pasted description"),""));
    }
    public JobDescription find(String title,String company,String country){return getDescription(new JobDescriptionRequest(title,company,country,"",""));}
    public JobDescription getDescription(JobDescriptionRequest request){
        if(!request.text().isBlank())return save(new PastedTextSource().fetch(request));
        JobDescription excerpt;
        try{excerpt=new AdzunaExcerptSource(tools,ai).fetch(request);}
        catch(RuntimeException e){
            if(Thread.currentThread().isInterrupted())throw e;
            FullEstimateSource source=new FullEstimateSource(ai);
            if(!approve(source,e.getMessage()+". Generate a FULL ESTIMATE for "+request.title()+" at "+request.company()+" in "+request.country()+"? Decline to paste an actual posting instead."))
                throw new IllegalStateException("No description saved. Paste an actual posting with job add or the Job screen.");
            JobDescription estimate;
            try{estimate=source.fetch(request);}
            catch(RuntimeException failure){throw new IllegalStateException("Estimate failed after its retry; no description saved. Paste an actual posting instead.",failure);}
            return save(estimate);
        }
        ExpandedEstimateSource source=new ExpandedEstimateSource(ai);
        String explanation="Adzuna partial excerpt (not a full posting):\n"+excerpt.text()+"\nJobs by Adzuna: "+excerpt.adzunaUrl()+"\nGenerate a labeled EXPANDED ESTIMATE?";
        if(approve(source,explanation)){
            JobDescription estimate;
            try{estimate=source.fetch(request.withExcerpt(excerpt));}
            catch(RuntimeException e){
                if(Thread.currentThread().isInterrupted())throw e;
                if(!gate.review("Expansion failed after its retry. Keep the original labeled Adzuna partial excerpt instead?",false))throw new IllegalStateException("No description saved; paste an actual posting instead.",e);
                return save(excerpt);
            }
            return save(estimate);
        }
        if(!gate.review("Keep this labeled Adzuna partial excerpt?\n"+excerpt.text()+"\nJobs by Adzuna: "+excerpt.adzunaUrl(),false))throw new IllegalStateException("Description rejected; paste an actual posting instead.");
        return save(excerpt);
    }
    private boolean approve(JobDescriptionSource source,String explanation){return !source.needsApproval()||gate.review(explanation,false);}
    private JobDescription save(JobDescription job){repository.saveAndSelect(job);return job;}
    static String country(String c){c=required(c,"Country").toUpperCase(Locale.ROOT);if(c.equals("CANADA"))c="CA";if(c.equals("USA"))c="US";if(!Set.of("CA","US").contains(c))throw new IllegalArgumentException("Country must be CA or US");return c;}
    static String explicitDeadline(String text){
        Matcher matches=Pattern.compile("(?i)(?:deadline|apply by|closing date)\\s*:?\\s*(\\d{4}-\\d{2}-\\d{2}|[a-z]{3,9}\\s+\\d{1,2},?\\s+\\d{4}|\\d{1,2}\\s+[a-z]{3,9}\\s+\\d{4})\\b").matcher(text);
        Set<String> dates=new HashSet<>();
        while(matches.find()){
            String candidate=matches.group(1).replace(",","").replaceAll("\\s+"," ");
            for(String pattern:List.of("uuuu-MM-dd","MMMM d uuuu","MMM d uuuu","d MMMM uuuu","d MMM uuuu")){
                var format=new java.time.format.DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(pattern).toFormatter(Locale.ENGLISH).withResolverStyle(java.time.format.ResolverStyle.STRICT);
                try{dates.add(java.time.LocalDate.parse(candidate,format).toString());break;}
                catch(java.time.format.DateTimeParseException ignored){/* Try the next explicit format. */}
            }
        }
        return dates.size()==1?dates.iterator().next():"";
    }
}
