package career;

import java.util.*;
import static career.Models.*;

public class WebSearchTool {
    private final Config config;private final HttpTransport http;
    public WebSearchTool(Config config,HttpTransport http){this.config=config;this.http=http;}
    public List<SearchResult> search(String query){
        String key=config.get("BRAVE_SEARCH_API_KEY","");if(key.isBlank())throw new IllegalStateException("Company research needs BRAVE_SEARCH_API_KEY; no summary invented");
        var response=Json.object(Json.parse(http.request("https://api.search.brave.com/res/v1/web/search?q="+HttpTransport.query(query)+"&count=5",Map.of("X-Subscription-Token",key,"Accept","application/json"),null)));
        if(!response.containsKey("web"))return List.of();List<SearchResult> out=new ArrayList<>();
        for(Object v:Json.list(Json.object(response.get("web")),"results")){
            var m=Json.object(v);String url=Json.text(m,"url");if(!url.startsWith("https://"))continue;
            out.add(new SearchResult(Json.text(m,"title"),url,Json.text(m,"description")));
        }return out;
    }
}
