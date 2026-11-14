package career;

import static career.Models.*;

public interface JobDescriptionSource {
    SourceLabel label();
    boolean needsApproval();
    String fetch(String context);
}
