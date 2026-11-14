package career;

import java.util.*;

public record CompanySummary(String jobId, String goals, String values, List<SearchResult> sources) {
    public CompanySummary {
        jobId=Models.required(jobId,"Job ID");goals=Models.required(goals,"Company goals");values=Models.required(values,"Company values");sources=List.copyOf(sources);
    }
}
