package career;

public final class ApprovalGate {
    @FunctionalInterface public interface ApprovalPrompter {
        boolean askApproval(String description);
        default boolean reviewResumeChange(ResumeChange change){return askApproval("Resume edit:\nORIGINAL: "+change.original()+"\nREVISED: "+change.revised()+"\nREASON: "+change.reason()+"\nAccept this change?");}
    }
    private final ApprovalPrompter prompter;
    public ApprovalGate(ApprovalPrompter prompter){this.prompter=prompter;}
    public boolean reviewResumeChange(ResumeChange change){return prompter.reviewResumeChange(change);}
    public boolean review(String description,boolean userRequested){return userRequested||prompter.askApproval(description);}
}
