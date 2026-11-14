package career;

import java.util.*;

/** Explicit opt-in smoke checks using nonpersonal input; never run by the build or offline tests. */
public final class ApiCheck {
    private ApiCheck() {}
    public static void main(String[] args) {
        if (args.length != 1 || !Set.of("jev", "haiku", "sonnet", "all").contains(args[0])) {
            System.err.println("Usage: java -cp target/career-agent.jar career.ApiCheck jev|haiku|sonnet|all");
            System.exit(2);
        }
        Config config = new Config();
        Ai ai = new Ai(new ClaudeAdapter(new AnthropicApi(config)), new JevAdapter(new JevApi(config)));
        boolean failed = false;
        for (String provider : args[0].equals("all") ? List.of("jev", "haiku", "sonnet") : List.of(args[0])) {
            try {
                if (provider.equals("jev")) {
                    Decision d = ai.decide("The candidate explicitly states: I have Java experience. Which skill is stated?",
                        List.of("Java", "Python"));
                    if (!d.choice().equals("Java")) throw new IllegalStateException("Unexpected decision for smoke-check input");
                    System.out.println("PASS Jev (" + JevAdapter.MODEL + "): " + d.choice() + "; confidence=" + d.confidence());
                } else {
                    ModelTier tier = provider.equals("haiku") ? ModelTier.HAIKU : ModelTier.SONNET;
                    String text = ai.text("Confirm that this is an API connectivity check with a short sentence.", "No personal data.", tier);
                    System.out.println("PASS Claude " + (tier == ModelTier.HAIKU ? ClaudeAdapter.HAIKU_MODEL : ClaudeAdapter.SONNET_MODEL) + ": " + text);
                }
            } catch (RuntimeException e) {
                failed = true;
                System.err.println("NOT VERIFIED " + provider + ": " + e.getMessage());
            }
        }
        if (failed) System.exit(1);
    }
}
