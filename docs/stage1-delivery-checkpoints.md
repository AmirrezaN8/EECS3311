# Stage 1 delivery checkpoints — no design substitutions

Authority: Stage1.md and uml.uxf define the application. The official Stage 2 PDF defines implementation/submission/verification obligations. This plan supersedes the earlier generic-provider/consolidation recommendations; it does not claim that current code already satisfies these checkpoints.

## Official requirements versus project choices

| Topic | Official Stage 2 PDF | Approved repository design |
|---|---|---|
| Build | Working application, complete supporting code, dependency/build files and README covering prerequisites, installation, configuration and GUI/CLI launch (pp. 1, 5). No specific Maven/Gradle/JDK version or build command mandated. | Java application; Swing GUI and independent CLI calling AgentController. Maven is supporting tooling, not an approved application redesign. |
| Verification | Basic verification, important defects/fixes, remaining limitations recorded in report section 6 (p. 4). Features must actually work; mockups, empty methods, hard-coded demonstration responses and disconnected classes do not count (p. 1). | Feature alternative/error flows and validators in F01–F10/UC01–UC10 provide the concrete acceptance checks. |
| Later testing | Full deterministic testing and KUMA agent-validation remain Stage 3 obligations under the Project Instructions (p. 4). No Stage 2 test-count, coverage percentage, JUnit mandate or testing exemption is specified. | Small regression checks per implemented behavior; fixture tests do not establish live provider success. |
| GUI/CLI | Both required, exposing major functionality; collectively demonstrated working in videos (pp. 1, 4). No widget toolkit/layout mandated. | SwingGui and CliApp share AgentController. Job intake, import, history, gap, tailoring, company, prep, interview and checklist screens/workflows; main goal/chat entry with approval buttons and progress. |
| AI | At least one integrated LLM and meaningful reasoning/planning/tool/memory/decision behavior; cheap/free models allowed (p. 1). No explicit local/cloud restriction. | Jev constrained decisions through DecisionClient/JevAdapter/JevApi; Claude generation/reasoning through LLMClient/ClaudeAdapter/AnthropicApi. Haiku 4.5 and Sonnet 5.5 are the model names proposed in Stage 1. |
| Patterns | Five distinct meaningful course patterns minimum (pp. 1, 4). | Preserve all seven: Facade, Command, Strategy, Factory, State, Observer, Adapter, with planned participants. |
| Persistence/tools | No particular database/search provider imposed by this Stage 2 PDF. | Small SQLite database and repository classes; Adzuna and web search through ToolManager. |
| Evidence | Every feature needs a real running video segment, ID/name/input/output/AI explanation and relevant alternative behavior; accessible timestamped links (p. 4). | Record evidence against each F01–F10 and the participating classes/methods. |

The Project Instructions and Stage 1 Instructions remain additional baseline documents (p. 1). Those official instruction documents have not been supplied; do not infer missing obligations from their absence.

## Checkpoint 0 — Restore baseline mapping and reproducible build

Reach: Stage1.md/uml.uxf traceability; build configuration; safe configuration; source layout; original-file protection. Inventory every proposed class/interface/relationship and map it to a source file or an explicitly unresolved item. Existing code is reusable only if it preserves the planned role/behavior.

Checks: compile production code on the selected JDK; package it; document reproducible launch commands; ensure Stage1.md and uml.uxf are unchanged; ensure real secret/data files are ignored. Check actual official Jev and Anthropic contracts/model identifiers before adapter implementation. If a named model is unavailable, stop that dependent choice and report it—do not invent an ID or silently substitute.

Gate: a reproducible build plus approved-design inventory. Supporting tooling may change without replacing application responsibilities. Do not count a skeleton as a finished feature.

## Checkpoint 1 — Real API boundaries (Adapter)

Reach: DecisionClient, Decision, JevAdapter, JevApi, LLMClient, ClaudeAdapter, AnthropicApi, ModelTier, PromptBuilder, ResponseParser. Check every caller of decisions and text generation; separate those paths rather than routing both through the same generic LLM.

Checks without credentials: request/response fixtures for each actual provider contract; fixed-option and probability/confidence validation; malformed response; invalid credentials; timeout/service failure; retry once where Stage 1 specifies. Fixtures remain test-only.

Live gate: local keys supplied outside Git; one real Jev fixed-choice decision; one real Haiku generation; one real reasoning-model call using the verified planned ID. Record the actual provider/model and outcome. Without credentials or a valid planned model, mark this gate blocked rather than passed; independent work may continue.

## Checkpoint 2 — SQLite domain and repositories

Reach: Repository, SqliteJobRepository, SqliteResumeRepository, SqliteMemoryRepository, SqliteChecklistRepository and the planned domain entities/relationships. Preserve existing local data when migrating current storage; do not delete user data to simplify the refactor.

Checks: save/load/update/delete; UTF-8 content; database restart; invalid IDs; failed-save behavior; job-linked outputs; immutable original base resumes and separate versions. Repositories perform real entity responsibilities, not unused aliases around an unrelated design.

Gate: repository contract checks and app restart retain the correct entities and links.

## Checkpoint 3 — F01 job intake and F02 resume import (first usable vertical slice)

Reach: GetJobDescriptionAction, ImportResumeAction, JobDescriptionIntake, all four JobDescriptionSource implementations, AdzunaTool, Tool/ToolManager/ToolResult, ResumeImporter, ResumeParserFactory, all planned ResumeParser implementations, ApprovalGate and AgentController.

Checks: pasted posting; title/company/CA-or-US lookup; closest-listing decision; partial label/Adzuna attribution; separate estimate approval/rejection; retries and paste alternative. Import PDF, Word, Markdown and pasted text; profile sections; scanned/corrupt/locked input; malformed-model retry/raw-text preservation. Verify Word-format expectations explicitly rather than silently narrowing them.

Gate: CLI can save an actual posting/profile and reload them after restart; live lookup/extraction checked separately from fixtures. Capture F01/F02 evidence when each is verified.

## Checkpoint 4 — F03 memory and history

Reach: MemoryManager, MemoryFact, memory repository, chat-turn integration, DecisionClient and LLMClient roles, My History/CLI history operations.

Checks: useful versus irrelevant message; Jev save decision; Haiku fact generation; normalized deduplication; conflict shown to user; view/edit/delete; persistence failure does not destroy existing facts or abort the turn; history survives restart. The goal field alone must not replace the planned chat/history workflow.

Gate: facts from actual chat turns are visible/editable and loaded for subsequent personalization.

## Checkpoint 5 — F04 gap analysis and F05 actual resume tailoring

Reach: SkillGapAction/Service, Skill/SkillAssessment/SkillGapReport/GapGroup; TailorResumeAction/Service, ResumeChange, ResumeVersion; source profile, posting, approval and repository calls.

Checks: required-skill extraction, normalization, Jev classification into MISSING/STRONG/DECENT, priority order and uncertainty. Missing inputs offer the specified recovery path. Tailoring genuinely rewrites wording; deterministic validator rejects unsupported skills; side-by-side original/revised/reason; individual accept/reject; accepted version linked to job with base preserved. A token-permutation-only implementation does not pass.

Gate: a real supported wording improvement can be accepted, an unsupported-skill edit rejected and the persisted version reopened.

## Checkpoint 6 — F06 company research and F07 question preparation

Reach: CompanyResearchAction/Service, WebSearchTool via ToolManager, CompanySummary; InterviewPrepAction/Service, InterviewQuestion and repositories.

Checks: Haiku query, real search results, Jev relevance filtering, bounded reformulation, sources and no invented summary when none qualify. Reasoning-model prompt -> Haiku questions; default five; exact format/count validation/retry; save; separate consent before mock interview.

Gate: sourced company result and personalized question set shown using actual configured services. No-result/error fixtures also pass.

## Checkpoint 7 — F08 mock interviews

Reach: AgentController.startMockInterview(), question/answer progress, Jev decision, Haiku follow-up, reasoning-model FeedbackReport, memory persistence.

Checks: one typed question at a time; at most one follow-up per base question; answer/progress saved; no feedback during the interview; final evaluation; weaknesses saved; quitting retains progress and offers feedback for answered questions. Check app restart during an unfinished attempt.

Gate: complete real interview plus quit/partial-feedback path demonstrated without exposing per-answer feedback early.

## Checkpoint 8 — F09 checklist

Reach: ChecklistAction/Service, Checklist, ChecklistItem, checklist repository, posting deadline and previous preparation outputs.

Checks: full/partial inputs, resume-match/deadline/skills/interview/company priority, reasoning-model ordering, no invented deadline, tick/untick and progress persistence; missing-feature offers go through approval. Preserve supported posting deadline behavior rather than silently restricting it to ISO-only dates.

Gate: useful saved checklist with matching progress after restart and missing-input consent behavior.

## Checkpoint 9 — F10 autonomous agent loop (Command/State/Observer)

Reach: Planner; all seven named AgentActions; ActionResult; ToolManager; AgentSession/AgentState with Idle, Planning, AwaitingApproval, Executing, Finished; ApprovalPrompter/ApprovalGate; AgentEvent/Publisher/ProgressObserver; facade.

Checks: Jev chooses from the full planned list (description, resume, gap, tailor, company, interview prep, checklist, done). Explicitly requested actions run directly; unrequested actions require consent. Verify approval rejection, missing-input steps, done, failures/retry/skip, stop, step limit, progress notifications, memory retrieval and saved results. Do not force a smaller fixed workflow while claiming the original planner.

Gate: end-to-end goal reaches useful saved preparation with explainable decisions; all seven patterns participate in actual behavior.

## Checkpoint 10 — Complete Swing GUI and independent CLI

The facade/CLI vertical slice starts at checkpoint 3; this checkpoint finishes every interface path. GUI construction can progress after shared services stabilize; it must never create a second business-logic implementation.

Reach: SwingGui, CliApp and planned event/approval interfaces. GUI covers the approved goal/chat, intake/import, history, gap, tailoring review, company, interview preparation, typed mock interview and checklist workflows. CLI retains Stage 1 career commands.

Checks: major features reachable in both interfaces; clear input/output/errors; side-by-side resume review; approvals and progress; background network work does not freeze Swing; UI updates on event thread; cancellation/window close retain saved data; CLI works independently. Test actual desktop interaction, not only compilation/headless startup.

Gate: clean launch from documented commands and actual GUI/CLI workflow recordings.

## Checkpoint 11 — Submission evidence and genuine commits

Reach: README, safe configuration, build/dependency files, reproducible sample inputs, report source/PDF, updated UML, feature demos, actual Git history.

Checks: rerun the build/regression suite after changes; run real provider/tool smoke checks; clean-start and restart walkthrough; exact class/method/feature mapping; ten accessible feature video segments; both interfaces represented; actual tool/model/cost data or explicit unavailable values; accurately report unverified behavior and missing historical commit windows. Rebuild the PDF from the final code's facts.

Gate: user reviews the destination/branch and approves a push; then verify actual public repository contents and video access before submitting its URL. Do not promise instructor acceptance or invent late-submission rules.

## Commit timing

Commit a coherent implementation/test checkpoint after its checks pass; GUI/CLI work can have additional meaningful fixes. There is no fixed total count now that strict-alignment corrections are required. Do not commit a deliberately broken intermediate dependency state. Documentation-only commits are useful but do not count as the PDF's meaningful code commits. Historical weekly requirements cannot be repaired with current-date commits or artificially splitting changes.

No provider has to be connected before fixture-level checks, but a feature's external integration must not be marked live-verified until the corresponding real API call succeeds. Existing 97 offline assertions are previous verification evidence, not certification of restored Stage 1 classes or providers.
