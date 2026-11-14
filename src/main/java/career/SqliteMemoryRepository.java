package career;

import java.util.*;
import static career.Models.*;

public final class SqliteMemoryRepository implements Repository<MemoryFact> {
    private final SqliteStore store;
    public SqliteMemoryRepository(SqliteStore store){this.store=store;}
    @Override public void save(MemoryFact fact){synchronized(store){
        for(var existing:findAll())if(!existing.id().equals(fact.id())&&normalize(existing.text()).equals(normalize(fact.text())))throw new IllegalArgumentException("This fact is already saved");
        Documents.save(store,"memory",fact.id(),fact);
    }}
    @Override public MemoryFact findById(String id){return Documents.read(store,"memory",required(id,"Fact ID"),MemoryFact.class);}
    @Override public List<MemoryFact> findAll(){return Documents.all(store,"memory",MemoryFact.class);}
    public MemoryFact findByText(String text){return findAll().stream().filter(f->normalize(f.text()).equals(normalize(text))).findFirst().orElse(null);}
    @Override public void delete(String id){synchronized(store){if(findById(id)==null)throw new IllegalArgumentException("Unknown fact ID");store.delete("memory",id);}}
}
