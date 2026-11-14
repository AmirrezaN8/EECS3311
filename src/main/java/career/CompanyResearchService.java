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
        CompanySummary summary=ai.ask("Summarize goals and values ONLY from supplied source snippets. Cite their URLs inline. Say 'not stated' when absent. Return {\"goals\":\"...\",\"values\":\"...\"}.",sources,ModelTier.HAIKU,m->new CompanySummary(job.id(),Json.text(m,"goals"),Json.text(m,"values"),sources));
        repository.save(summary);return summary;
    }
}
