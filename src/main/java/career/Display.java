package career;

import java.util.*;

public final class Display {
    private Display() {}
    public static String format(Object value){StringBuilder out=new StringBuilder();append(out,value,0);return out.toString();}
    private static void append(StringBuilder b,Object v,int depth){
        if(v instanceof SkillGapReport report){
            Map<String,Object> output=new LinkedHashMap<>();output.put("jobId",report.jobId());output.put("createdAt",report.createdAt());
            for(GapGroup group:GapGroup.values())output.put(group.name(),report.groupedByPriority().stream().filter(a->a.group()==group).map(a->{
                Map<String,Object> item=new LinkedHashMap<>(Json.object(Documents.encode(a)));item.put("uncertain",a.uncertain());return item;
            }).toList());
            v=output;
        }else if(!(v instanceof Map<?,?>)&&!(v instanceof Collection<?>))v=Documents.encode(v);
        String indent="  ".repeat(Math.min(depth,20));
        if(v instanceof Map<?,?> m){for(var e:m.entrySet()){b.append(indent).append(e.getKey()).append(": ");if(e.getValue() instanceof Map<?,?>||e.getValue() instanceof Collection<?>||e.getValue()!=null&&e.getValue().getClass().isRecord()){b.append('\n');append(b,e.getValue(),depth+1);}else b.append(e.getValue()).append('\n');}}
        else if(v instanceof Collection<?> l){if(l.isEmpty())b.append(indent).append("(none)\n");for(Object item:l){b.append(indent).append("- ");if(item instanceof Map<?,?>||item instanceof Collection<?>||item!=null&&item.getClass().isRecord()){b.append('\n');append(b,item,depth+1);}else b.append(item).append('\n');}}
        else b.append(indent).append(v).append('\n');
    }
}
