package career;

import java.util.*;
import static career.Models.*;

/** Input shared by source strategies; blank text requests lookup rather than a pasted posting. */
public record JobDescriptionRequest(String title,String company,String country,String text,String adzunaUrl) {
    public JobDescriptionRequest {
        title=required(title,"Title");company=required(company,"Company");country=JobDescriptionIntake.country(country);
        Objects.requireNonNull(text,"Description input");Objects.requireNonNull(adzunaUrl,"Attribution");
    }
    public JobDescriptionRequest withExcerpt(JobDescription excerpt){return new JobDescriptionRequest(excerpt.title(),excerpt.company(),excerpt.country(),excerpt.text(),excerpt.adzunaUrl());}
    public JobDescription description(String content,SourceLabel label){
        String deadline=label==SourceLabel.USER_PASTED||label==SourceLabel.ADZUNA_EXCERPT?JobDescriptionIntake.explicitDeadline(content):"";
        return new JobDescription(id(),title,company,country,required(content,"Job description"),label,adzunaUrl,deadline.isEmpty()?null:java.time.LocalDate.parse(deadline));
    }
}
