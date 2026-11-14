package career;

import java.util.*;

/** Small strict JSON codec for HTTP and persisted documents; no Java object deserialization. */
public final class Json {
    private Json() {}
    public static Object parse(String input) {
        if (input == null || input.length() > 4_000_000) throw new IllegalArgumentException("JSON missing or too large");
        Parser p = new Parser(input); Object v = p.value(0); p.space();
        if (p.i != input.length()) throw new IllegalArgumentException("Trailing JSON content");
        return v;
    }
    @SuppressWarnings("unchecked")
    public static Map<String,Object> object(Object v) {
        if (!(v instanceof Map<?,?>)) throw new IllegalArgumentException("Expected JSON object");
        return (Map<String,Object>)v;
    }
    public static String text(Map<String,Object> m, String k) {
        Object v=m.get(k); if (!(v instanceof String s) || s.isBlank()) throw new IllegalArgumentException("Missing text: "+k);
        return s;
    }
    public static List<Object> list(Map<String,Object> m,String k) {
        Object v=m.get(k); if (!(v instanceof List<?> l)) throw new IllegalArgumentException("Missing array: "+k);
        return new ArrayList<>(l);
    }
    public static List<String> strings(Map<String,Object> m,String k) {
        return list(m,k).stream().map(v->{if (!(v instanceof String s) || s.isBlank()) throw new IllegalArgumentException("Invalid "+k); return s;}).toList();
    }
    public static double number(Map<String,Object> m,String k) {
        if (!(m.get(k) instanceof Number n)) throw new IllegalArgumentException("Missing number: "+k); return n.doubleValue();
    }
    public static boolean bool(Map<String,Object> m,String k) {
        if (!(m.get(k) instanceof Boolean b)) throw new IllegalArgumentException("Missing boolean: "+k); return b;
    }
    public static String write(Object v) {
        if(v==null) return "null";
        if(v instanceof String s) {
            StringBuilder b=new StringBuilder("\"");
            for(char c:s.toCharArray()) switch(c) {
                case '"' -> b.append("\\\""); case '\\' -> b.append("\\\\");
                case '\n' -> b.append("\\n"); case '\r' -> b.append("\\r"); case '\t' -> b.append("\\t");
                default -> {if(c<32) b.append(String.format("\\u%04x",(int)c)); else b.append(c);}
            }
            return b.append('"').toString();
        }
        if(v instanceof Boolean || v instanceof Number) return v.toString();
        if(v instanceof Map<?,?> m) return "{"+String.join(",",m.entrySet().stream().map(e->write(e.getKey().toString())+":"+write(e.getValue())).toList())+"}";
        if(v instanceof Collection<?> l) return "["+String.join(",",l.stream().map(Json::write).toList())+"]";
        throw new IllegalArgumentException("Unsupported JSON type: "+v.getClass());
    }
    private static final class Parser {
        final String s; int i;
        Parser(String s){this.s=s;}
        void space(){while(i<s.length() && " \t\r\n".indexOf(s.charAt(i))>=0)i++;}
        char take(){if(i>=s.length()) throw new IllegalArgumentException("Truncated JSON");return s.charAt(i++);}
        void expect(char c){space();if(take()!=c)throw new IllegalArgumentException("Expected "+c);}
        Object value(int depth){
            if(depth>64)throw new IllegalArgumentException("JSON nesting too deep"); space();
            if(i>=s.length())throw new IllegalArgumentException("Empty JSON"); char c=s.charAt(i);
            if(c=='"')return string();
            if(c=='{'){i++;Map<String,Object> m=new LinkedHashMap<>();space();if(i<s.length()&&s.charAt(i)=='}'){i++;return m;}
                do{space();String k=string();expect(':');Object v=value(depth+1);if(m.containsKey(k))throw new IllegalArgumentException("Duplicate key");m.put(k,v);space();c=take();if(c=='}')return m;if(c!=',')throw new IllegalArgumentException("Expected comma");}while(true);}
            if(c=='['){i++;List<Object> l=new ArrayList<>();space();if(i<s.length()&&s.charAt(i)==']'){i++;return l;}
                do{l.add(value(depth+1));space();c=take();if(c==']')return l;if(c!=',')throw new IllegalArgumentException("Expected comma");}while(true);}
            for(String literal:List.of("true","false","null"))if(s.startsWith(literal,i)){i+=literal.length();return literal.equals("null")?null:literal.equals("true");}
            if("-0123456789".indexOf(c)<0)throw new IllegalArgumentException("Expected a JSON value at character "+i);
            int start=i;while(i<s.length()&&"0123456789eE+-.".indexOf(s.charAt(i))>=0)i++;
            String n=s.substring(start,i);if(!n.matches("-?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+-]?[0-9]+)?"))throw new IllegalArgumentException("Invalid JSON number");
            if(!n.contains(".")&&!n.contains("e")&&!n.contains("E")){try{return Long.valueOf(n);}catch(NumberFormatException ignored){}}
            double d=Double.parseDouble(n);if(!Double.isFinite(d))throw new IllegalArgumentException("Nonfinite number");return d;
        }
        String string(){
            if(take()!='"')throw new IllegalArgumentException("Expected string");StringBuilder b=new StringBuilder();
            while(true){char c=take();if(c=='"')return b.toString();if(c<32)throw new IllegalArgumentException("Control character");
                if(c=='\\'){c=take();switch(c){case '"','\\','/'->b.append(c);case 'b'->b.append('\b');case 'f'->b.append('\f');case 'n'->b.append('\n');case 'r'->b.append('\r');case 't'->b.append('\t');case 'u'->{if(i+4>s.length())throw new IllegalArgumentException("Truncated unicode");b.append((char)Integer.parseInt(s.substring(i,i+4),16));i+=4;}default->throw new IllegalArgumentException("Invalid escape");}}
                else b.append(c);
            }
        }
    }
}
