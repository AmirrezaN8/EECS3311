package career;

import java.util.*;
import static career.Models.*;

public final class AdzunaTool {
    private final Config config;private final HttpTransport http;
    public AdzunaTool(Config config,HttpTransport http){this.config=config;this.http=http;}
    public List<SearchResult> search(String title,String company,String country){
        String app=config.get("ADZUNA_APP_ID",""),key=config.get("ADZUNA_APP_KEY","");
        if(app.isBlank()||key.isBlank())throw new IllegalStateException("Adzuna credentials missing; paste a posting or approve an estimate");
        String url="https://api.adzuna.com/v1/api/jobs/"+(country.equals("CA")?"ca":"us")+"/search/1?app_id="+HttpTransport.query(app)+"&app_key="+HttpTransport.query(key)+"&results_per_page=5&what="+HttpTransport.query(title+" "+company);
        RuntimeException last=null;
        for(int i=0;i<2;i++)try{
            var result=Json.object(Json.parse(http.request(url,Map.of(),null)));List<SearchResult> rows=new ArrayList<>();
            for(Object v:Json.list(result,"results")){var m=Json.object(v);rows.add(new SearchResult(Json.text(m,"title"),Json.text(m,"redirect_url"),Json.text(m,"description")));}return rows;
        }catch(RuntimeException e){last=e;}
        throw new IllegalStateException("Adzuna failed after retry; paste a posting or approve an estimate",last);
    }
}
