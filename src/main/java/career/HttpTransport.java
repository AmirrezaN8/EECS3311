package career;

import java.net.*;
import java.net.http.*;
import java.time.Duration;
import java.util.Map;

public class HttpTransport {
    private final HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).followRedirects(HttpClient.Redirect.NEVER).build();
    public String request(String url,Map<String,String> headers,String body){
        URI uri=URI.create(url);
        if(!"https".equals(uri.getScheme()) && !("http".equals(uri.getScheme()) && java.util.Set.of("localhost","127.0.0.1","[::1]").contains(uri.getHost())))throw new IllegalArgumentException("Service URL must use HTTPS (HTTP allowed for localhost only)");
        if(uri.getUserInfo()!=null)throw new IllegalArgumentException("Do not put credentials in service URLs");
        HttpRequest.Builder b=HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(90));headers.forEach(b::header);
        if(body==null)b.GET();else b.header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(body));
        try{
            HttpResponse<java.io.InputStream> response=client.send(b.build(),HttpResponse.BodyHandlers.ofInputStream());
            try(var in=response.body()){
                if(response.statusCode()<200||response.statusCode()>=300)throw new IllegalStateException("Service HTTP "+response.statusCode()+"; check credentials, model and quota");
                byte[] bytes=in.readNBytes(4_000_001);if(bytes.length>4_000_000)throw new IllegalStateException("Service response too large");
                return new String(bytes,java.nio.charset.StandardCharsets.UTF_8);
            }
        }catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException("Request cancelled",e);}
        catch(java.io.IOException e){throw new IllegalStateException("Service unavailable; check connection and configuration",e);}
    }
    public static String query(String s){return URLEncoder.encode(s,java.nio.charset.StandardCharsets.UTF_8);}
}
