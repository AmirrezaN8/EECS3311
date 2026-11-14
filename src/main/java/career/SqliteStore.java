package career;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** Low-level SQLite transactions for the typed repositories, using macOS sqlite3. */
public final class SqliteStore {
    private final Path file;
    public SqliteStore(Path file) {
        this.file=file.toAbsolutePath();
        try{Files.createDirectories(this.file.getParent());}catch(Exception e){throw new IllegalStateException("Cannot create data directory",e);}
        sql("PRAGMA journal_mode=WAL; CREATE TABLE IF NOT EXISTS documents(kind TEXT NOT NULL,id TEXT NOT NULL,data TEXT NOT NULL,PRIMARY KEY(kind,id));");
        try{Files.setPosixFilePermissions(this.file,java.nio.file.attribute.PosixFilePermissions.fromString("rw-------"));}catch(UnsupportedOperationException ignored){}catch(java.io.IOException e){throw new IllegalStateException("Cannot restrict permissions on personal data",e);}
    }
    private static String literal(String text){return "CAST(X'"+HexFormat.of().formatHex(text.getBytes(StandardCharsets.UTF_8))+"' AS TEXT)";}
    public record Write(String kind,String id,Object data) {}
    public synchronized void save(String kind,String id,Object data){saveAll(List.of(new Write(kind,id,data)));}
    /** A selected entity and its pointer, or an interview and its archive, commit together. */
    public synchronized void saveAll(List<Write> writes){saveAll(writes,Map.of());}
    public synchronized void saveAll(List<Write> writes,Map<String,String> deletes){
        StringBuilder command=new StringBuilder("BEGIN IMMEDIATE; ");
        for(var write:writes)command.append("INSERT INTO documents VALUES(").append(literal(write.kind())).append(',').append(literal(write.id())).append(',').append(literal(Json.write(write.data()))).append(") ON CONFLICT(kind,id) DO UPDATE SET data=excluded.data; ");
        deletes.forEach((kind,id)->command.append("DELETE FROM documents WHERE kind=").append(literal(kind)).append(" AND id=").append(literal(id)).append("; "));
        sql(command.append("COMMIT;").toString());
    }
    public synchronized void deleteAll(Map<String,String> keys){saveAll(List.of(),keys);}
    public synchronized Map<String,Object> find(String kind,String id){
        String out=sql("SELECT hex(data) FROM documents WHERE kind="+literal(kind)+" AND id="+literal(id)+";").strip();
        return out.isEmpty()?null:Json.object(Json.parse(new String(HexFormat.of().parseHex(out),StandardCharsets.UTF_8)));
    }
    public synchronized List<Map<String,Object>> all(String kind){
        String out=sql("SELECT hex(data) FROM documents WHERE kind="+literal(kind)+" ORDER BY rowid;").strip();
        if(out.isEmpty())return List.of();
        return out.lines().map(h->Json.object(Json.parse(new String(HexFormat.of().parseHex(h),StandardCharsets.UTF_8)))).toList();
    }
    public synchronized void delete(String kind,String id){sql("DELETE FROM documents WHERE kind="+literal(kind)+" AND id="+literal(id)+";");}
    private String sql(String command){
        Path output=null;Process p=null;
        try {
            output=Files.createTempFile("career-sqlite-", ".out");
            p=new ProcessBuilder("sqlite3","-batch","-bail","-cmd",".timeout 5000",file.toString()).redirectErrorStream(true).redirectOutput(output.toFile()).start();
            try(var in=p.getOutputStream()){in.write(command.getBytes(StandardCharsets.UTF_8));}
            if(!p.waitFor(15,TimeUnit.SECONDS)){p.destroyForcibly();throw new IllegalStateException("SQLite operation timed out");}
            String result=Files.readString(output);
            if(p.exitValue()!=0)throw new IllegalStateException("SQLite could not save/read data; check database permissions and integrity");
            return result;
        }catch(InterruptedException e){Thread.currentThread().interrupt();if(p!=null)p.destroyForcibly();throw new IllegalStateException("Storage operation interrupted",e);}
        catch(java.io.IOException e){throw new IllegalStateException("SQLite unavailable. Install sqlite3 or check CAREER_DATA permissions",e);}
        finally{if(output!=null)try{Files.deleteIfExists(output);}catch(Exception ignored){}}
    }
}
