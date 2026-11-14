package career;

import java.util.*;
import static career.Models.*;

public final class AdzunaTool implements Tool {
    private final String appId,key;private final HttpTransport http;
    public AdzunaTool(Config config,HttpTransport http){this(config.get("ADZUNA_APP_ID",""),config.get("ADZUNA_APP_KEY",""),http);}
    public AdzunaTool(String appId,String key,HttpTransport http){this.appId=appId;this.key=key;this.http=http;}
    public String getName(){return "adzuna";}
    public ToolResult execute(Map<String,String> args){return new ToolResult(true,search(Models.required(args.get("title"),"Title"),Models.required(args.get("company"),"Company"),JobDescriptionIntake.country(args.get("country"))),"");}
    public List<SearchResult> search(String title,String company,String country){
        country=JobDescriptionIntake.country(country);String app=appId;
        if(app.isBlank()||key.isBlank())throw new IllegalStateException("Adzuna credentials missing; paste a posting or approve an estimate");
        String url="https://api.adzuna.com/v1/api/jobs/"+(country.equals("CA")?"ca":"us")+"/search/1?app_id="+HttpTransport.query(app)+"&app_key="+HttpTransport.query(key)+"&results_per_page=5&what="+HttpTransport.query(title+" "+company);
        RuntimeException last=null;
        for(int i=0;i<2;i++)try{
            var result=Json.object(Json.parse(http.request(url,Map.of("Accept","application/json"),null)));List<SearchResult> rows=new ArrayList<>();
            for(Object v:Json.list(result,"results")){var m=Json.object(v);rows.add(new SearchResult(Json.text(m,"title"),Json.text(m,"redirect_url"),Json.text(m,"description")));}return rows;
        }catch(RuntimeException e){if(Thread.currentThread().isInterrupted())throw e;last=e;}
        throw new IllegalStateException("Adzuna failed after retry; paste a posting or approve an estimate",last);
    }
}
