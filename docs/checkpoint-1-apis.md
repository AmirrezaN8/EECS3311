# Checkpoint 1 — restore the Stage 1 AI boundaries

Status: IMPLEMENTED AND OFFLINE-VERIFIED; live API connectivity gate PASSED in the user’s Terminal (user-reported output). This is implementation checkpoint 1, not a rewrite of the original Stage 1 design.

## Scope and actual routes

Stage1.md sections 1.1, F01–F04/F06–F10 and Adapter explanation, plus the original UML, require separate constrained-decision and generation providers.

- Decisions: service/planner -> Ai retry coordinator -> DecisionClient -> JevAdapter -> JevApi -> `POST https://api.typesafe.ai/v1/systemone`.
- Generation: service -> Ai validation/retry coordinator -> LLMClient -> ClaudeAdapter -> AnthropicApi -> `POST https://api.anthropic.com/v1/messages`.
- `ModelTier.HAIKU`: `claude-haiku-4-5-20251001`; `ModelTier.SONNET`: `claude-sonnet-5-5`. Jev uses documented `jev-latest` alias. No provider/model fallback is present.
- `Decision` retains the chosen label, full option probability map and distinct provider confidence; validates finite values, bounds and normalized probabilities. ResponseParser additionally enforces the exact requested option set.
- PromptBuilder marks source context as data. ResponseParser invokes feature-specific format/evidence validators. Ai retries failures at most once and honors interruption.
- The generic Ollama/compatible LLMAdapter was removed, including its obsolete protocol tests. Existing feature generation callers now name their original HAIKU or SONNET roles.

All existing decision callers were checked: JobDescriptionIntake, MemoryManager, SkillGapService, CompanyResearchService, MockInterviewService and Planner. They route through Jev. Resume extraction, estimates, research query/summary, prompt/question preparation, interview follow-ups/feedback, checklist ordering and tailoring generation route through their original Claude tiers.

## Verified provider documentation

Checked November 14, 2026:
- Jev request/response contract and credentials: https://docs.typesafe.ai/introduction/quickstart
- Typed Choice/probabilities/confidence: https://docs.typesafe.ai/primitives/choice
- API reference: https://docs.typesafe.ai/api/system-one
- Anthropic Messages API: https://platform.claude.com/docs/en/api/messages/create
- Anthropic version/authentication details: https://platform.claude.com/docs/en/api/migration-2026-10-21
- Haiku 4.5 exact ID: https://platform.claude.com/docs/en/models/haiku-4-5/overview
- Sonnet 5.5 exact ID: https://platform.claude.com/docs/en/models/sonnet-5-5/overview

The planned Sonnet 5.5 name is documented; no replacement model is necessary based on this check. Documentation availability is not proof of access by the user's account. Claude requests use the documented 2026-10-21 API version, bearer authentication and top-level system prompt, with a 4096 output-token limit. Incomplete/refused/tool-use responses are rejected; thinking blocks are not displayed as application output.

## Checks performed

`sh scripts/build.sh`: PASS, 154 assertions total. Of these, 61 verify restored provider boundaries with a fixture transport intercepting HTTP before networking. Checks include exact request schemas/authentication/routing, Jev probability/confidence validation, malformed/missing output, Claude text/thinking handling, incomplete/refused outputs, absent credentials, no cross-provider fallback, one retry, cancellation and schema validation. Existing service/core/end-to-end fixtures continue to pass. JUnit delegates to the same suite, but Maven/JUnit execution remains unverified here.

`java -cp target/career-agent.jar career.ApiCheck all`: NOT VERIFIED, exit 1. Both local API key fields are empty. Each requested check reports the missing local key; no live inference was attempted. The ignored personal decisions file was not staged or committed. No Git commit/push was made.

## User-run live verification

The user subsequently ran `java -cp target/career-agent.jar career.ApiCheck all` in their own macOS Terminal and supplied this output:

```text
PASS Jev (jev-latest): Java; confidence=1.0
PASS Claude claude-haiku-4-5-20251001: This is an API connectivity check confirming the system is operational.
PASS Claude claude-sonnet-5-5: Yes, this is an API connectivity check, and the connection is working.
```

This is user-run evidence, not an independently observed agent-side network execution. It passes this checkpoint's live connectivity gate for Jev and both planned Claude models. It does not establish full F01–F10 behavior, factual quality, desktop GUI operation, live Adzuna/search integration, or assessed model accuracy. Actual token/cost totals were not supplied. No credentials were included in the shared output.

The earlier missing-key results above describe the initial attempt; the later agent-side attempt after key configuration failed DNS resolution for both provider hostnames. The user’s Terminal succeeded outside that restricted environment.

## Reproducing the live checks

1. Open ignored `career.properties` locally. An empty safe copy was created only if it did not already exist; existing contents are preserved.
2. Put a TypeSafe key in `TYPESAFE_API_KEY` and an Anthropic key in `ANTHROPIC_API_KEY`. Obtain them from the providers' consoles. Keep keys out of chat and recordings; API calls may consume provider credits.
3. Run `sh scripts/build.sh`.
4. Run `java -cp target/career-agent.jar career.ApiCheck all`. Alternatively check `jev`, `haiku` and `sonnet` individually. The smoke input contains no personal resume/history data.
5. Report PASS/NOT VERIFIED messages with credentials excluded. If a provider/model/account is unavailable, resolve that specific issue without substituting another provider.

No F01–F10 video is ready solely from this boundary checkpoint. Record actual feature segments when their feature checkpoint and real dependencies pass. A short API check may be supplementary evidence but does not replace GUI/CLI feature demos.

Commit reminder: this is a coherent offline-tested code milestone, and the live gate now has user-run PASS evidence. The initial source tree is untracked, so review its source dependencies before staging. Do not run the superseded bulk commit guide or make misleading completion claims. Review a buildable initial commit with its required source dependencies, then use the agreed checkpoint commits. Do not commit `career.properties` or `docs/user-decisions.md`.

## Remaining design work

Named feature commands, ToolManager/Tool/ToolResult, separate typed repositories/domain details, full State input/approval API, F05 actual rewriting/side-by-side review, F10 full action choices/approval semantics and other audited feature/UI details are later checkpoints. This change restores API boundaries only and does not claim those gaps are fixed. The working report/PDF is prominently marked preliminary; its historical sections require final reconciliation.
