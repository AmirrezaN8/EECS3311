package career;

import java.nio.file.*;
import java.util.Properties;

public final class Config {
    private final Properties local=new Properties();
    public Config(){Path p=Path.of("career.properties");if(Files.exists(p))try(var in=Files.newInputStream(p)){local.load(in);}catch(Exception e){throw new IllegalStateException("Cannot read career.properties",e);}}
    public String get(String key,String fallback){String e=System.getenv(key);return e!=null?e:local.getProperty(key,fallback);}
    public Path data(){return Path.of(get("CAREER_DATA","data/career.db"));}
}
