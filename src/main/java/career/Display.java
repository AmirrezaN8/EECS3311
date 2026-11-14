package career;

import java.util.*;

public final class Display {
    private Display() {}
    public static String format(Object value){StringBuilder out=new StringBuilder();append(out,Documents.encode(value),0);return out.toString();}
    private static void append(StringBuilder b,Object v,int depth){
        String indent="  ".repeat(Math.min(depth,20));
        if(v instanceof Map<?,?> m){for(var e:m.entrySet()){b.append(indent).append(e.getKey()).append(": ");if(e.getValue() instanceof Map<?,?>||e.getValue() instanceof Collection<?>){b.append('\n');append(b,e.getValue(),depth+1);}else b.append(e.getValue()).append('\n');}}
        else if(v instanceof Collection<?> l){if(l.isEmpty())b.append(indent).append("(none)\n");for(Object item:l){b.append(indent).append("- ");if(item instanceof Map<?,?>||item instanceof Collection<?>){b.append('\n');append(b,item,depth+1);}else b.append(item).append('\n');}}
        else b.append(indent).append(v).append('\n');
    }
}
