package career;

public record ChecklistItem(String id, String label, int priority, boolean done) {
    public ChecklistItem { id=Models.required(id,"Checklist item ID");label=Models.required(label,"Checklist label");if(priority<1)throw new IllegalArgumentException("Priority must be positive"); }
    public ChecklistItem tick(boolean value){return new ChecklistItem(id,label,priority,value);}
}
