package career;

import java.util.*;
import static career.Models.*;

public final class SqliteChecklistRepository implements Repository<Checklist> {
    private final SqliteStore store;
    public SqliteChecklistRepository(SqliteStore store){this.store=store;}
    @Override public void save(Checklist list){synchronized(store){
        JobDescription job=new SqliteJobRepository(store).require(list.jobId());
        if(list.deadline()!=null&&!list.deadline().equals(job.deadline()))throw new IllegalArgumentException("Checklist deadline is not supported by saved posting");
        Checklist old=findById(list.jobId());if(old!=null&&!old.id().equals(list.id()))throw new IllegalArgumentException("Keep the existing checklist identity when rebuilding");
        Documents.save(store,"checklist",list.jobId(),list);
    }}
    /** Stage 1 UI identifies a checklist by job; also accept its own stable entity ID for CRUD. */
    @Override public Checklist findById(String id){
        required(id,"Checklist/job ID");Checklist direct=Documents.read(store,"checklist",id,Checklist.class);
        if(direct!=null)return direct;return findAll().stream().filter(c->c.id().equals(id)).findFirst().orElse(null);
    }
    @Override public List<Checklist> findAll(){return Documents.all(store,"checklist",Checklist.class);}
    @Override public void delete(String id){synchronized(store){Checklist list=findById(id);if(list==null)throw new IllegalArgumentException("Unknown checklist ID");store.delete("checklist",list.jobId());}}
    public Checklist tick(String jobId,String itemId,boolean done){synchronized(store){
        Checklist list=findById(jobId);if(list==null)throw new IllegalStateException("Build checklist first");
        if(list.items().stream().noneMatch(i->i.id().equals(itemId)))throw new IllegalArgumentException("Unknown checklist item ID");
        Checklist updated=new Checklist(list.id(),list.jobId(),list.deadline(),list.items().stream().map(i->i.id().equals(itemId)?i.tick(done):i).toList());save(updated);return updated;
    }}
}
