package career;

public record Skill(String name, String level) {
    public Skill { name=Models.required(name,"Skill name");level=Models.required(level,"Skill level"); }
}
