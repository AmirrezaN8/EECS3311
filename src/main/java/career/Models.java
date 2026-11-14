package career;

import java.util.*;

public final class Models {
    private Models() {}
    public static String id(){return UUID.randomUUID().toString();}
    public static String required(String s,String label){if(s==null||s.isBlank())throw new IllegalArgumentException(label+" is required");if(s.length()>100_000)throw new IllegalArgumentException(label+" is too long (100,000 characters max)");return s.strip();}
    public static String normalize(String s){return s.strip().toLowerCase(Locale.ROOT).replaceAll("\\s+"," ");}
}
