package career;

import java.util.*;
import static career.Models.*;

public final class MemoryManager {
    private final Ai ai;private final SqliteMemoryRepository repository;private final ApprovalGate gate;private final AgentEventPublisher events;
    public MemoryManager(Ai ai,SqliteStore store,ApprovalGate gate,AgentEventPublisher events){this.ai=ai;this.repository=new SqliteMemoryRepository(store);this.gate=gate;this.events=events;}
    public List<MemoryFact> loadFacts(){return repository.findAll();}
    public MemoryFact save(String category,String text){
        category=required(category,"Category");text=required(text,"Fact");
        MemoryFact existing=repository.findByText(text);if(existing!=null)return existing;
        MemoryFact f=new MemoryFact(id(),category,text,java.time.Instant.now());repository.save(f);return f;
    }
    public void editFact(String id,String text){MemoryFact f=repository.findById(id);if(f==null)throw new IllegalArgumentException("Unknown fact ID");repository.save(f.edit(required(text,"Fact")));}
    public void delete(String id){if(repository.findById(id)==null)throw new IllegalArgumentException("Unknown fact ID");repository.delete(id);}
    public void recordTurn(String message){
        try{
            if(!ai.decide("Does this message contain a useful personal career fact? "+message,List.of("save","skip")).choice().equals("save"))return;
            MemoryFact candidate=ai.ask("Summarize one explicitly stated personal fact. Return {\"category\":\"skills|target roles|weaknesses|actions\",\"text\":\"short fact\"}.",message,ModelTier.HAIKU,m->new MemoryFact(id(),Json.text(m,"category"),Json.text(m,"text"),java.time.Instant.now()));
            List<MemoryFact> facts=loadFacts();
            if(facts.stream().anyMatch(f->normalize(f.text()).equals(normalize(candidate.text()))))return;
            if(!facts.isEmpty()&&ai.decide("Does new fact contradict existing facts? New: "+candidate.text()+" Existing: "+Json.write(Documents.encode(facts)),List.of("conflict","compatible")).choice().equals("conflict")){
                if(!gate.review("Potential conflict: "+candidate.text()+"\nExisting history: "+Json.write(Documents.encode(facts))+"\nSave alongside existing facts for review?",false))return;
            }
            save(candidate.category(),candidate.text());
        }catch(RuntimeException e){events.publish("memory-warning","Fact not saved; message/workflow continues: "+e.getMessage());}
    }
}
