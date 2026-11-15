package career;

import java.util.*;
import static career.Models.*;

public final class CompanyResearchService {
    private final Ai ai;private final ToolManager tools;private final SqliteJobRepository repository;
    public CompanyResearchService(Ai ai,ToolManager tools,SqliteStore store){this.ai=ai;this.tools=tools;this.repository=new SqliteJobRepository(store);}
    public CompanySummary research(JobDescription job){
        List<SearchResult> relevant=new ArrayList<>();String query=ai.text("Write a concise web-search query for company goals and values.",job.company()+" "+job.title()+" "+java.time.Year.now(),ModelTier.HAIKU);
        for(int attempt=0;attempt<2&&relevant.isEmpty();attempt++){
            if(attempt>0)query=ai.text("Reformulate search; previous query yielded no relevant sources.",query+" Company: "+job.company(),ModelTier.HAIKU);
            for(var source:tools.executeTool("web-search",Map.of("query",query)).requireData())if(ai.decide("Is this source specifically relevant to "+job.company()+" goals or values? "+Json.write(Documents.encode(source)),List.of("relevant","irrelevant")).choice().equals("relevant"))relevant.add(source);
        }
        if(relevant.isEmpty())throw new IllegalStateException("No relevant company sources found; no summary generated");
        var sources=List.copyOf(relevant);
        CompanySummary summary=ai.ask("Summarize goals and values ONLY from supplied source snippets. Cite their URLs inline. Each nonempty goals/values section must include an exact supplied source URL. Use exactly 'not stated' when absent. Never invent a URL. Return {\"goals\":\"...\",\"values\":\"...\"}.",sources,ModelTier.HAIKU,m->new CompanySummary(job.id(),cited(Json.text(m,"goals"),sources),cited(Json.text(m,"values"),sources),sources));
        repository.save(summary);return summary;
    }
    private static String cited(String text,List<SearchResult> sources){
        text=required(text,"Sourced company summary");
        if(normalize(text).equals("not stated"))return text;
        Set<String> allowed=new HashSet<>();for(var source:sources)allowed.add(source.url());
        var urls=java.util.regex.Pattern.compile("https?://[^\\s<>\\[\\]\"']+").matcher(text);
        boolean cited=false;
        while(urls.find()){
            String url=urls.group();
            while(!allowed.contains(url)&&url.matches(".*[.,;:!)]$"))url=url.substring(0,url.length()-1);
            if(!allowed.contains(url))throw new IllegalArgumentException("Summary cites a URL outside supplied sources");
            cited=true;
        }
        if(!cited)throw new IllegalArgumentException("Company claims require a supplied source URL or 'not stated'");
        return text;
    }

}
