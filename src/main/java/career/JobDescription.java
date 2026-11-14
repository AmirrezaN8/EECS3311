package career;

import java.time.LocalDate;
import java.util.*;
import static career.Models.*;

public record JobDescription(String id, String title, String company, String country, String text,
                             SourceLabel sourceLabel, String adzunaUrl, LocalDate deadline) {
    public JobDescription {
        id=required(id,"Job ID");title=required(title,"Job title");company=required(company,"Company");
        text=required(text,"Job description");country=required(country,"Country");
        if(!Set.of("CA","US").contains(country))throw new IllegalArgumentException("Country must be CA or US");
        Objects.requireNonNull(sourceLabel,"Source label");Objects.requireNonNull(adzunaUrl,"Attribution");
        if((sourceLabel==SourceLabel.FULL_ESTIMATE||sourceLabel==SourceLabel.EXPANDED_ESTIMATE)&&deadline!=null)
            throw new IllegalArgumentException("Estimated descriptions cannot establish a verified posting deadline");
    }
    public boolean isEstimate(){return sourceLabel==SourceLabel.EXPANDED_ESTIMATE||sourceLabel==SourceLabel.FULL_ESTIMATE;}
}
