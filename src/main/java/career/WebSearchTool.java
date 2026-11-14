package career;

import java.util.*;
import static career.Models.*;

public class WebSearchTool implements Tool {
    private final String key;private final HttpTransport http;
    public WebSearchTool(Config config,HttpTransport http){this(config.get("BRAVE_SEARCH_API_KEY",""),http);}
    public WebSearchTool(String key,HttpTransport http){this.key=key;this.http=http;}
    public String getName(){return "web-search";}
    public ToolResult execute(Map<String,String> args){return new ToolResult(true,search(Models.required(args.get("query"),"Search query")),"");}
    public List<SearchResult> search(String query){
        if(key.isBlank())throw new IllegalStateException("Company research needs BRAVE_SEARCH_API_KEY; no summary invented");
        var response=Json.object(Json.parse(http.request("https://api.search.brave.com/res/v1/web/search?q="+HttpTransport.query(query)+"&count=5",Map.of("X-Subscription-Token",key,"Accept","application/json"),null)));
        if(!response.containsKey("web"))return List.of();List<SearchResult> out=new ArrayList<>();
        for(Object v:Json.list(Json.object(response.get("web")),"results")){
            var m=Json.object(v);String url=Json.text(m,"url");if(!url.startsWith("https://"))continue;
            out.add(new SearchResult(Json.text(m,"title"),url,Json.text(m,"description")));
        }return out;
    }
}
