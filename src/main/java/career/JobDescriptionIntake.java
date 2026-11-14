package career;

import java.util.*;
import java.util.regex.*;
import static career.Models.*;

public final class JobDescriptionIntake {
    private final Ai ai;private final AdzunaTool adzuna;private final SqliteJobRepository repository;private final ApprovalGate gate;
    public JobDescriptionIntake(Ai ai,AdzunaTool adzuna,SqliteStore store,ApprovalGate gate){this.ai=ai;this.adzuna=adzuna;this.repository=new SqliteJobRepository(store);this.gate=gate;}
    public JobDescription add(String title,String company,String country,String text){return save(title,company,country,new PastedTextSource(),text,"");}
    public JobDescription find(String title,String company,String country){
        title=required(title,"Title");company=required(company,"Company");country=country(country);
        List<SearchResult> hits;String failure="";
        try{hits=adzuna.search(title,company,country);}catch(RuntimeException e){hits=List.of();failure=e.getMessage();}
        if(!hits.isEmpty()){
            List<String> indices=java.util.stream.IntStream.range(0,hits.size()).mapToObj(Integer::toString).toList();
            int selected=Integer.parseInt(ai.decide("Select closest job to "+title+" at "+company+". Listings: "+Json.write(Documents.encode(hits)),indices).choice());
            SearchResult hit=hits.get(selected);
            if(gate.review("Adzuna partial excerpt (not a full posting):\n"+hit.snippet()+"\nApprove generating an EXPANDED ESTIMATE?",false))return save(title,company,country,new ExpandedEstimateSource(ai),hit.snippet(),"Jobs by Adzuna: "+hit.url());
            if(!gate.review("Keep the labeled Adzuna partial excerpt instead?\n"+hit.snippet(),false))throw new IllegalStateException("Description rejected; paste a posting instead");
            return save(title,company,country,new AdzunaExcerptSource(),hit.snippet(),"Jobs by Adzuna: "+hit.url());
        }
        if(!gate.review((failure.isBlank()?"No Adzuna listings.":failure)+"\nGenerate a FULL ESTIMATE for "+title+" at "+company+"? This will not be a verified posting.",false))throw new IllegalStateException("Estimate rejected; paste a posting instead");
        return save(title,company,country,new FullEstimateSource(ai),title+" at "+company+" in "+country,"");
    }
    private JobDescription save(String title,String company,String country,JobDescriptionSource source,String context,String attribution){
        title=required(title,"Title");company=required(company,"Company");country=country(country);
        String text=required(source.fetch(context),"Job description");
        String deadline=(source.label()==SourceLabel.USER_PASTED||source.label()==SourceLabel.ADZUNA_EXCERPT)?explicitDeadline(text):"";
        JobDescription job=new JobDescription(id(),title,company,country,text,source.label(),attribution,deadline.isBlank()?null:java.time.LocalDate.parse(deadline));repository.saveAndSelect(job);return job;
    }
    static String country(String c){c=required(c,"Country").toUpperCase(Locale.ROOT);if(c.equals("CANADA"))c="CA";if(c.equals("USA"))c="US";if(!Set.of("CA","US").contains(c))throw new IllegalArgumentException("Country must be CA or US");return c;}
    static String explicitDeadline(String text){Matcher m=Pattern.compile("(?im)^.*(?:deadline|apply by|closing date)\\s*:?\\s*(\\d{4}-\\d{2}-\\d{2}).*$").matcher(text);if(!m.find())return "";try{return java.time.LocalDate.parse(m.group(1)).toString();}catch(Exception e){return "";}}
    public static final class PastedTextSource implements JobDescriptionSource {public SourceLabel label(){return SourceLabel.USER_PASTED;}public boolean needsApproval(){return false;}public String fetch(String s){return s;}}
    public static final class AdzunaExcerptSource implements JobDescriptionSource {public SourceLabel label(){return SourceLabel.ADZUNA_EXCERPT;}public boolean needsApproval(){return false;}public String fetch(String s){return s;}}
    public static final class ExpandedEstimateSource implements JobDescriptionSource {
        private final Ai ai;public ExpandedEstimateSource(Ai ai){this.ai=ai;}public SourceLabel label(){return SourceLabel.EXPANDED_ESTIMATE;}public boolean needsApproval(){return true;}
        public String fetch(String s){return ai.text("Expand this partial posting into an explicitly ESTIMATED job description. Do not invent a deadline or claim this is official.",s,ModelTier.SONNET);}
    }
    public static final class FullEstimateSource implements JobDescriptionSource {
        private final Ai ai;public FullEstimateSource(Ai ai){this.ai=ai;}public SourceLabel label(){return SourceLabel.FULL_ESTIMATE;}public boolean needsApproval(){return true;}
        public String fetch(String s){return ai.text("Write an explicitly ESTIMATED job description, not an actual posting. No invented deadline.",s,ModelTier.SONNET);}
    }
}
