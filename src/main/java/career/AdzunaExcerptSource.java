package career;

import java.util.*;

public final class AdzunaExcerptSource implements JobDescriptionSource {
    private final ToolManager tools;
    private final DecisionClient decisions;
    public AdzunaExcerptSource(ToolManager tools,DecisionClient decisions){this.tools=tools;this.decisions=decisions;}
    public SourceLabel label(){return SourceLabel.ADZUNA_EXCERPT;}
    public boolean needsApproval(){return false;}
    public JobDescription fetch(JobDescriptionRequest request){
        List<SearchResult> hits=tools.executeTool("adzuna",Map.of("title",request.title(),"company",request.company(),"country",request.country())).requireData();
        if(hits.isEmpty())throw new IllegalStateException("No Adzuna listings found");
        List<String> indices=java.util.stream.IntStream.range(0,hits.size()).mapToObj(Integer::toString).toList();
        Decision decision=decisions.decide("Select the closest listing for "+request.title()+" at "+request.company()+". These are partial excerpts, not full postings: "+Json.write(Documents.encode(hits)),indices);
        if(!indices.contains(decision.choice()))throw new IllegalArgumentException("Invalid listing selection");
        SearchResult hit=hits.get(Integer.parseInt(decision.choice()));
        return new JobDescriptionRequest(request.title(),request.company(),request.country(),hit.snippet(),hit.url()).description(hit.snippet(),label());
    }
}
