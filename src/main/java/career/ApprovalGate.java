package career;

public final class ApprovalGate {
    @FunctionalInterface public interface ApprovalPrompter {boolean askApproval(String description);}
    private final ApprovalPrompter prompter;
    public ApprovalGate(ApprovalPrompter prompter){this.prompter=prompter;}
    public boolean review(String description,boolean userRequested){return userRequested||prompter.askApproval(description);}
}
