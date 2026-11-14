package career;

import java.time.LocalDate;
import java.util.*;

public record Checklist(String id, String jobId, LocalDate deadline, List<ChecklistItem> items) {
    public Checklist { id=Models.required(id,"Checklist ID");jobId=Models.required(jobId,"Job ID");items=List.copyOf(items);if(items.stream().map(ChecklistItem::id).distinct().count()!=items.size())throw new IllegalArgumentException("Duplicate checklist item IDs"); }
    public int progress(){return items.isEmpty()?0:(int)(100*items.stream().filter(ChecklistItem::done).count()/items.size());}
}
