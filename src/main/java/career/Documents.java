package career;

import java.lang.reflect.*;
import java.util.*;

/** Explicitly typed record mapping for SQLite documents, never arbitrary class names from storage. */
public final class Documents {
    private Documents() {}
    public static Object encode(Object v){
        if(v==null||v instanceof String||v instanceof Number||v instanceof Boolean)return v;
        if(v instanceof Enum<?> e)return e.name();
        if(v instanceof java.time.Instant || v instanceof java.time.LocalDate)return v.toString();
        if(v instanceof Collection<?> l)return l.stream().map(Documents::encode).toList();
        if(v instanceof Map<?,?> m){Map<String,Object> out=new LinkedHashMap<>();m.forEach((k,x)->out.put(k.toString(),encode(x)));return out;}
        if(v.getClass().isRecord()){
            Map<String,Object> out=new LinkedHashMap<>();
            for(RecordComponent c:v.getClass().getRecordComponents())try{out.put(c.getName(),encode(c.getAccessor().invoke(v)));}catch(Exception e){throw new IllegalArgumentException("Cannot encode record",e);}
            return out;
        }
        throw new IllegalArgumentException("Unsupported document");
    }
    public static <T> T read(SqliteStore store,String kind,String id,Class<T> type){Map<String,Object> m=store.find(kind,id);return m==null?null:type.cast(decode(m,type));}
    public static void save(SqliteStore store,String kind,String id,Object v){store.save(kind,id,encode(v));}
    public static <T> List<T> all(SqliteStore store,String kind,Class<T> type){return store.all(kind).stream().map(m->type.cast(decode(m,type))).toList();}
    private static void alias(Map<String,Object> data,String current,String legacy){if(!data.containsKey(current)&&data.containsKey(legacy))data.put(current,data.get(legacy));}
    @SuppressWarnings({"unchecked","rawtypes"})
    static Object decode(Object value,Type type){
        if(type instanceof ParameterizedType p){
            if(p.getRawType()==Map.class){Map<String,Object> out=new LinkedHashMap<>();Json.object(value).forEach((k,v)->out.put(k,decode(v,p.getActualTypeArguments()[1])));return out;}
            if(!(value instanceof List<?> l))throw new IllegalArgumentException("Stored array missing");
            List<Object> out=l.stream().map(v->decode(v,p.getActualTypeArguments()[0])).toList();
            return p.getRawType()==Set.class?new HashSet<>(out):out;
        }
        Class<?> c=(Class<?>)type;
        if(c==Skill.class&&value instanceof String name)return new Skill(name,"UNSPECIFIED");
        // Legacy records may lack dates; keep them unknown rather than inventing history.
        if(c==java.time.Instant.class)return value==null?null:java.time.Instant.parse((String)value);
        if(c==java.time.LocalDate.class)return value==null||value.equals("")?null:java.time.LocalDate.parse((String)value);
        if(c.isEnum())return Enum.valueOf((Class)c,(String)value);
        if(c==int.class)return ((Number)value).intValue();
        if(c==double.class)return ((Number)value).doubleValue();
        if(c==boolean.class||c==String.class)return value;
        if(c.isRecord())try{
            Map<String,Object> m=new LinkedHashMap<>(Json.object(value));
            if(c==JobDescription.class){alias(m,"sourceLabel","source");alias(m,"adzunaUrl","attribution");}
            if(c==SkillAssessment.class)alias(m,"skillName","skill");
            if(c==FeedbackReport.class)alias(m,"perAnswerFeedback","perAnswer");
            if(c==Checklist.class){m.putIfAbsent("id",m.get("jobId"));m.putIfAbsent("deadline",null);}
            if(c==InterviewQuestion.class){m.putIfAbsent("jobId","");m.putIfAbsent("answer","");}
            RecordComponent[] fields=c.getRecordComponents();Object[] args=new Object[fields.length];Class<?>[] types=new Class<?>[fields.length];
            for(int i=0;i<fields.length;i++){args[i]=decode(m.get(fields[i].getName()),fields[i].getGenericType());types[i]=fields[i].getType();}
            return c.getDeclaredConstructor(types).newInstance(args);
        }catch(ReflectiveOperationException e){throw new IllegalArgumentException("Stored document incompatible: "+c.getSimpleName(),e);}
        throw new IllegalArgumentException("Unsupported stored type");
    }
}
