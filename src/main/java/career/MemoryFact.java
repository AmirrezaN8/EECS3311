package career;

import java.time.Instant;

public record MemoryFact(String id, String category, String text, Instant updatedAt) {
    public MemoryFact {
        id=Models.required(id,"Fact ID");category=Models.required(category,"Fact category");
        text=Models.required(text,"Fact text");
    }
    public MemoryFact edit(String newText){return new MemoryFact(id,category,newText,Instant.now());}
}
