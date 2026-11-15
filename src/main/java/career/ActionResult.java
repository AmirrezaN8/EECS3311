package career;

public record ActionResult(boolean success,String message) {
    public ActionResult {message=Models.required(message,"Action result message");}
}
