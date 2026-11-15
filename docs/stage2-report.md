# EECS3311 Stage 2 — Implementation Report

DOCX/GUI UPDATE: The user supplied successful real DOCX import output with all expected sections and an empty warning, and confirmed basic GUI operation. This is user-run evidence, not exhaustive GUI coverage. Pasted job intake and Markdown/DOCX extraction are ready to record within that verified scope. Adzuna/estimate/PDF/legacy Word branches remain pending; no video links or new checkpoint-3 commit are claimed.

LIVE RERUN UPDATE: After the parser correction and a passing 325-assertion build, the user reported successful real Markdown resume extraction: all expected source-supported sections populated, no inferred proficiency and an empty warning. F01 pasted intake and F02 Markdown extraction have user-run success evidence. DOCX/PDF/legacy Word, Adzuna/estimate and desktop paths remain pending. The precise original model response remains unknown; no video or new checkpoint-3 commit is claimed.

LIVE DEFECT UPDATE: The user confirmed a passing 301-assertion build and successful F01 pasted intake, but F02 raw fallback with Invalid JSON number. Its exact live model response is unknown. A valid JSON code-block fixture reproduced the error; the parser now accepts exactly one such wrapper, retains strict syntax/schema/evidence checks, sends validator errors into the bounded retry and marks raw fallback with a warning event. The expanded suite passes 325 assertions; successful live F02 rerun is still pending. These are actual failure/fix observations, not a claim of verified live extraction.

LATEST CHECKPOINT 3: F01/F02 now run through GetJobDescriptionAction/ImportResumeAction, named source/parser strategies and ToolManager/Tool/ToolResult. Resume extraction validates source evidence for all five sections. Explicit Adzuna JSON negotiation and approved estimate failure recovery are tested. Current build: 301 offline assertions; dependency-backed PDF/legacy Word, live feature paths and desktop walkthrough remain pending. Safe fictional sample inputs and a local recording gate are supplied in docs/checkpoint-3-inputs.md and samples/. Implementation commit 99ca4c9 now exists (Java core/API/repository milestone); no new checkpoint-3 commit or video is claimed. Earlier report text is historical until final reconciliation.

CURRENT CHECKPOINT UPDATE: Named top-level domain entities and SqliteJobRepository, SqliteResumeRepository, SqliteMemoryRepository and SqliteChecklistRepository are restored and used by the services/facade. Typed skills, creation/edit dates, checklist identity/deadline and job-linked question answers are implemented. Compatibility reads preserve older records; atomic selection and interview/feedback restart are tested. Current build: 233 offline assertions pass, including 79 domain/repository checks and a separate-JVM restart. See docs/checkpoint-2-persistence.md. Later command/tool/state/F05/F10/UI corrections remain pending. Existing string identity storage and native sqlite3 transport are retained and documented; no JDBC implementation is claimed.

WORKING REPORT — NOT FINAL SUBMISSION. This original implementation snapshot predates strict Stage 1 corrections. Checkpoint 1 has since restored JevAdapter/JevApi, ClaudeAdapter/AnthropicApi, Decision probabilities, ModelTier.HAIKU/SONNET, PromptBuilder and ResponseParser. The generic provider adapter is removed. Current verification: 154 offline assertions pass; API smoke checks report missing keys. Sections below still include historical architecture descriptions and must be reconciled with all checkpoint changes before submission. See docs/checkpoint-1-apis.md for current provider evidence.

Career Preparation AI Agent
Repository: https://github.com/AmirrezaN8/EECS3311
Prepared for Amirreza N8 | November 14, 2026 | Toronto local time
Status: Java implementation with verified offline core; live integrations and desktop demonstration pending.

## 1. Project overview and implementation status

The application helps new graduates prepare applications in Canada and the United States. It accepts job postings and resumes, remembers career facts, identifies skill gaps, proposes conservative tailored resume edits, researches employers, generates questions, runs typed interviews, and tracks preparation. A bounded agent coordinates these services rather than merely forwarding a chat prompt.

Implementation baseline: Stage1.md was read in full and uml.uxf panel attributes were extracted. The repository initially contained only these design files and .gitattributes. Both original documents remain byte-for-byte unchanged. The UMLet file contains class diagrams, but the use-case/sequence diagrams mentioned in the Markdown are not present as corresponding UMLet elements. Updated class, use-case and sequence sources accompany this report. No Stage 2 instruction PDF was supplied; compliance is assessed against the user's supplied requirements, pending comparison with that PDF.

Architecture: Java 21-compatible source, built with Temurin 27. SwingGui and CliApp share AgentController. Domain data consists of named top-level records; Models contains shared input/identity utilities. Feature services use Ai for validated generation/decision coordination, the four planned repositories for entity persistence, Documents for record conversion and SqliteStore for low-level SQLite transactions. No web frontend, Python core or JavaScript core exists. Maven configuration includes PDFBox, Apache POI and JUnit; an offline JDK-only build and assert-based test runner avoid blocked downloads.

JevAdapter/JevApi implement DecisionClient through the TypeSafe API; ClaudeAdapter/AnthropicApi implement LLMClient for Haiku 4.5 and Sonnet 5.5. Ai coordinates bounded validation/retry across these separate providers. The user reported successful real connectivity checks for all three planned model roles. Invalid JSON, invalid choice, invalid count and unsupported evidence trigger at most one retry. Service/credential failures are visible. No canned AI result is shipped in production. Explicit model fixtures are limited to tests.

Working offline functionality verified: posting entry/selection/persistence, DOCX/Markdown/text extraction, raw resume retention on AI failure, memory CRUD/deduplication, deterministic edit validation, partial checklist/progress, JSON and error boundaries. AI-dependent service paths and the complete orchestrated preparation/interview workflow passed with labeled fixtures. Live model quality, provider connectivity, live company research and job search are NOT claimed verified.

GUI: job/profile/history/preparation/interview/checklist tabs; typed inputs, readable results, approval dialogs, progress events, check boxes and percentage progress. Long work runs in SwingWorker; dialogs use the Swing event thread. CLI supports interactive and one-shot commands. Both call the same facade. Desktop GUI interaction is pending; compilation and graceful headless startup were verified.

Known limits: PDF parsing requires the Maven build and remains unverified here. Legacy binary Word .doc has a POI strategy requiring packaged dependencies and valid-sample verification; scanned and locked PDFs need pasted text. Tailoring supports a bounded set of word-form/connective changes while preserving substantive tokens; it rejects broader prose rewrites. Token preservation is not a proof of semantic equivalence, so user review remains essential. Skill matching is conservative/literal and marks uncertain evidence. Automatic deadlines recognize explicitly labeled ISO dates only. Research summarizes search snippets, not entire crawled sites. Local SQLite data is unencrypted. Videos have not been recorded. The Git weekly requirements are currently unmet.

## 2. Feature-to-implementation mapping

All ten Stage 1 features have application paths. 'Implemented' here describes connected code plus specified verification; it does not mean a live external service has been exercised. Modified scope is called out explicitly. All file names below are under src/main/java/career/ unless stated otherwise. Source links and commands are also indexed in docs/demo-guide.md.

### F01 — Job description intake

Reference: Stage1.md F01 / UC01. Status: Implemented and offline-tested; live lookup/estimation walkthrough pending. Type: hybrid.
Files/classes: GetJobDescriptionAction, JobDescriptionIntake, JobDescriptionRequest, JobDescriptionSource and PastedTextSource/AdzunaExcerptSource/ExpandedEstimateSource/FullEstimateSource; ToolManager/Tool/ToolResult/AdzunaTool; JevAdapter, ClaudeAdapter; SqliteJobRepository; JobDescription/SourceLabel.
Methods: AgentController.runFeature(), GetJobDescriptionAction.execute(), JobDescriptionIntake.getDescription(), JobDescriptionSource.fetch()/label()/needsApproval(), ToolManager.executeTool(), AdzunaTool.execute()/search(), SqliteJobRepository.saveAndSelect().
Flow: GUI/CLI input -> named command -> validated request -> pasted or tool-backed source -> Jev fixed-option listing selection -> labeled partial excerpt/source attribution -> consent for Sonnet expanded/full estimate -> atomic labeled save and selection. Declined/failed paths retain prior work, offer the original partial excerpt if appropriate or instruct paste. Adzuna/tool and model retries are bounded; save failures are not presented as model failures.
Patterns: Command, Strategy, Facade, Adapter. Tools register centrally. Country restricted to CA/US; automatic posting deadlines still have the documented ISO-only limitation pending later deadline work.
Verification: actual command/service/tool paths under fixtures cover input labels, attribution, selection, query/JSON format, retries, all approval/rejection/fallback paths, cancellation and preserved selections. Real Adzuna/desktop walkthrough is pending. Demo: NOT RECORDED; URL/TIMESTAMP TBD; GUI Job and CLI job commands.

### F02 — Resume import and skill extraction

Reference: Stage1.md F02 / UC02. Status: Implemented and offline-tested; dependency-backed PDF/legacy Word and live extraction walkthrough pending. Type: hybrid.
Files/classes: ImportResumeAction, ResumeImporter, ResumeParserFactory, ResumeParser; top-level PastedTextResumeParser/MarkdownResumeParser/DocxResumeParser/PdfResumeParser/DocResumeParser; ResumeProfile/Skill, ClaudeAdapter, SqliteResumeRepository.
Methods: ImportResumeAction.execute(), importResume(), importText(), createParser(), extractText(String source), ResumeProfile.getSkills(), SqliteResumeRepository.saveAndSelect().
Flow: GUI/CLI file/paste -> named command -> shared parser contract selected by factory -> readable text -> Haiku extraction of five sections with literal source-evidence validation -> atomic new base/selection save. Unsupported model output retries once then stores raw text with warning. Bad files/cancellation do not replace the selected profile. Original profiles remain preserved. No proficiency is invented.
Patterns: Command, Factory, Strategy, Facade, Adapter. Supporting DocResumeParser covers originally promised Word 97-2003 input using POI; PDF uses PDFBox. Missing offline dependencies are explicitly reported, never simulated.
Verification: Markdown/DOCX/pasted extraction, malicious XML rejection, invalid UTF-8, missing/directory/oversized/corrupt files, all sections, invented education rejection, retry/raw fallback and retention. Maven suite includes conditional actual text/blank/corrupt/protected PDF tests; those are skipped here without dependencies. Valid binary Word sample and live extraction remain pending. Demo: NOT RECORDED; URL/TIMESTAMP TBD; GUI Resume plus CLI resume commands.

### F03 — User history memory

Reference: F03, UC03. Status: Implemented. Type: hybrid.
Files/classes: MemoryManager.java, Models.MemoryFact, AgentController.java, SqliteStore.java.
Methods: recordTurn(), loadFacts(), save(), editFact(), delete(); facade chat/history commands and handleGoal().
Flow: career chat/goal -> constrained save/skip decision -> short structured fact -> deduplication -> contradiction decision against existing facts -> user approval for a suspected conflict -> save. Explicit history operations work without AI. Failed automatic memory operations publish a warning while leaving existing facts intact. A conflict can be saved alongside prior facts only with approval, allowing manual review rather than silent replacement.
Patterns: Facade, Adapter, Observer for warnings.
Verification: normalized deduplication, editing, deletion, unknown-ID errors and failed-AI isolation. Automatic semantic conflict detection is not live-model verified. Demo: NOT RECORDED; URL/TIMESTAMP TBD; My History and CLI history show.

### F04 — Skill-gap identification

Reference: F04, UC04. Status: Implemented with conservative evidence matching. Type: hybrid.
Files/classes: SkillGapService.java; Models.SkillGapReport, SkillAssessment, GapGroup; Ai.java.
Methods: identify(), groupedByPriority(), uncertain(), decide().
Flow: selected posting/profile -> model extracts literal posting skill names -> normalize/deduplicate -> constrained MISSING/STRONG/DECENT classification -> force unsupported positive claims to uncertain MISSING -> ordered saved report. Missing inputs generate clear import/add instructions. Strong/decency decisions rely on model assessment of the raw resume evidence.
Patterns: Command through FeatureCommand, Facade, Adapter.
Verification: missing-first ordering, demonstrated strength, forced uncertain classification without evidence. Literal matching can miss synonyms; uncertainty is explicitly shown. Demo: NOT RECORDED; URL/TIMESTAMP TBD; GUI Preparation or CLI gap.

### F05 — Resume tailoring

Reference: F05, UC05. Status: Bounded wording rewrites implemented; live model and GUI verification pending. Type: hybrid.
Files/classes: TailorResumeAction.java; ResumeTailorService.java; ResumeChange.java; ResumeVersion.java; ApprovalGate.java; SwingGui.java.
Methods: execute(), tailor(), safeChange(), tokens(), reviewResumeChange().
Flow: profile/posting -> proposed edits with original/revised/reason -> deterministic unique-substring, bounded word-form/token-count and overlap validation -> per-change user accept/reject dialog -> save a new job/base-linked version. Changed numbers/new skill tokens are rejected. Previously stored original resume is never overwritten. Each approval displays both texts together; GUI uses original/proposed columns within each approval dialog.
Patterns: Command, Facade, Adapter.
Verification: accepted/rejected edits, unsupported skill and invented-number rejection, ambiguous repeated substring rejection and original preservation. Review is still needed to catch meaning changes from reordered words. Demo: NOT RECORDED; URL/TIMESTAMP TBD; GUI tailoring dialog and CLI resume versions.

### F06 — Company goals and values research

Reference: F06, UC06. Status: Implemented; live service pending. Type: hybrid/agentic.
Files/classes: CompanyResearchAction.java; CompanyResearchService.java; WebSearchTool.java; CompanySummary.java; SearchResult.java; Ai.java.
Methods: execute(), research(), search(), cited(), decide(), ask().
Flow: model query -> Brave search snippets -> per-source relevant/irrelevant decision -> reformulated query if empty, maximum two searches -> sourced goals/values summary -> persist sources alongside result. Missing configuration or no relevant sources yields an error and no new summary. Stored older evidence is retained rather than overwritten with invented content.
Patterns: Command, Adapter, Facade.
Verification: 404 total offline assertions; F06 checks include bounded empty/irrelevant-result retry, relevant-only context, source-link provenance, missing/invented citations, URL/query validation, outages preserving prior results and restart persistence with explicit fixtures. Citation validation does not prove entailment. Factual summarization/relevance quality and live Brave connectivity are unverified. Demo: NOT RECORDED; URL/TIMESTAMP TBD; GUI Research/company CLI.

### F07 — Interview question preparation

Reference: F07, UC07. Status: Implemented. Type: AI-based.
Files/classes: InterviewPrepService.java and QuestionSet; Models.InterviewQuestion; AgentController.java.
Methods: prepare(); facade interview prep and startMockInterview().
Flow: job/profile/company/history -> reasoning-tier generation instructions -> fast-tier exactly five distinct questions -> validation/retry -> persistence -> separate approval to start an interview. A rejected interview offer leaves the questions saved.
Patterns: Command, Adapter, Facade.
Verification: five unique questions accepted/saved; invalid counts rejected; integrated interview flow tested with fixtures. Personalization quality requires a live model check. Demo: NOT RECORDED; URL/TIMESTAMP TBD; GUI Preparation.

### F08 — Mock interview with feedback

Reference: F08, UC08. Status: Implemented. Type: AI-based/agentic.
Files/classes: MockInterviewService.java; Models.InterviewSession, FeedbackReport; MemoryManager.java.
Methods: start(), nextQuestion(), answer(), stop(), feedback(); facade startMockInterview().
Flow: load saved questions/session -> one question -> persist answer immediately -> constrained follow-up/next choice -> at most one generated follow-up per base question -> persist follow-up answer -> continue. No feedback is requested mid-interview. End/approved early feedback -> evaluate only answered questions -> validate count -> save final report and weaknesses. Stop pauses and preserves progress; start continues; restart archives the previous session.
Patterns: Facade, Adapter; persistent interview data is separate from the main agent's State pattern.
Verification: full and partial feedback, suppression during interview, follow-up limit, pause/resume across saved state, completion, restart archive and weakness storage. Demo: NOT RECORDED; URL/TIMESTAMP TBD; GUI Mock Interview plus CLI continuation.

### F09 — Preparation checklist

Reference: F09, UC09. Status: Implemented with ISO-deadline limitation and deterministic fallback. Type: hybrid.
Files/classes: ChecklistService.java; Models.Checklist, ChecklistItem; SwingGui.java.
Methods: build(), tick(), Checklist.progress(), ChecklistItem.tick(), SwingGui.showChecklist().
Flow: read saved gap/company/questions/versions and explicit posting deadline -> useful partial/full task set -> AI prioritizes unchanged labels -> deterministic priority fallback on AI failure -> save checklist preserving ticks/IDs of unchanged tasks. GUI renders tick boxes; CLI tick/untick updates persistent progress. Partial checklist command offers approved missing features. After those complete, rebuild refreshes task labels.
Patterns: Command, Facade, Adapter.
Verification: partial offline checklist, no invented deadline, item-ID validation, tick/untick, progress and preservation on rebuild/restart. Non-ISO deadlines require manual tracking. Demo: NOT RECORDED; URL/TIMESTAMP TBD; GUI Checklist and CLI checklist show.

### F10 — Prepare me for this job

Reference: F10, UC10. Status: Implemented with consolidated command classes. Type: hybrid/agentic.
Files/classes: AgentController.java; Planner.java; AgentAction.java/FeatureCommand; AgentSession.java and five state implementations; ApprovalGate.java; AgentEventPublisher.java.
Methods: handleGoal(), chooseNext(), execute(), handleInput(), AgentState.advance(), setState(), publish(), review().
Flow: remember goal and retrieve history -> explicitly obtain missing job/profile and optionally a personal fact -> planner selects among eligible steps using current persisted results/profile/history -> approval -> command execution -> event/state transition -> remove attempted action -> repeat. Checklist is eligible only after other steps have been attempted. Failures/rejections are recorded and skipped. Cancellation or ten-step cap ends the loop. Final timestamped trace is saved in SQLite and returned. The workflow does not auto-apply edits or automatically launch a mock interview.
Patterns: Facade, Command, State, Observer, Adapter.
Verification: state transitions, visible tool failure continuation, rejected actions never executing, stop, ten-step bound, full service orchestration to persisted checklist and feedback under fixture models. Planner behavior is not live-model verified. Demo: NOT RECORDED; URL/TIMESTAMP TBD; GUI goal or CLI prepare.

Added supporting operations (not counted as extra features): selection/listing of stored jobs, inspection of outputs/versions, direct fact addition, resume of paused interviews, archived restarts, checklist untick, CLI help and cancellation. They reuse the above services and are demonstrated within those feature segments.

## 3. Implemented design patterns

Seven distinct patterns participate in real application behavior. Domain records and the restored F01/F02 strategies/commands are top-level classes; the main agent states remain nested pending their planned input/approval checkpoint. Source: docs/implemented-class-diagram.puml. A readable vector class diagram is included after the report text in the PDF. Updated use-case and sequence diagram sources: docs/implemented-use-cases.puml and docs/implemented-sequence.puml.

Facade: AgentController is the facade; SwingGui and CliApp are clients. runFeature(), handleGoal() and startMockInterview() coordinate services/persistence/approvals. Both interfaces invoke the same operations. This matches Stage 1 and avoids duplicate business logic.

Command: AgentAction defines name()/describe()/execute(). FeatureCommand stores a service Supplier<Object> and is instantiated by AgentController.actions() for gap, tailoring, company research, interview prep and checklist. Planner chooses an AgentAction and ExecutingState invokes execute(). GetJobDescriptionAction and ImportResumeAction are restored named commands in actual GUI/CLI execution. The remaining five operations still use FeatureCommand pending their respective feature checkpoints. This preserves uniform planner execution without one trivial forwarding class per feature.

Strategy: ResumeParser.extractText(Path) has PDF, DOCX and Markdown/text strategies chosen by the importer. JobDescriptionSource.fetch()/label()/needsApproval() has pasted, excerpt, expanded estimate and full estimate strategies. Intake selects the strategy after user approvals. Source strategies encapsulate retrieval/generation differences, while parser strategies encapsulate format differences. PastedTextResumeParser implements the same interface as file strategies and is selected by the factory using the pasted input type.

Factory: ResumeParserFactory.createParser(Path) selects PdfResumeParser, DocxResumeParser or MarkdownResumeParser. ResumeImporter calls the returned interface and does not implement extension-switching itself. This directly preserves the proposed factory role. The same factory also chooses PastedTextResumeParser and the supporting legacy DocResumeParser. Unknown extensions are rejected.

State: AgentSession holds AgentState; IdleState, PlanningState, AwaitingApprovalState, ExecutingState and FinishedState each implement advance(session). Planning owns selection, approval owns consent/rejection, execution owns result/failure transition. setState() publishes transitions. Session drives state behavior with a bounded loop instead of implementing all behavior in a single switch. advance() replaces the Stage 1 onInput/onApproval pair because approval is synchronous at the shared interaction boundary.

Observer: AgentEventPublisher holds ProgressObserver callbacks; attach()/detach()/publish() manage subscriptions and notifications. AgentSession and MemoryManager publish progress/warnings without knowing GUI widgets. CliApp prints events, while SwingGui schedules updates onto the event thread. Failing observers cannot abort saved work. This preserves Stage 1, using lambdas rather than making the entire UI class implement a marker role.

Adapter: JevAdapter implements DecisionClient and translates typed Choice requests/responses through JevApi. ClaudeAdapter implements LLMClient and translates HAIKU/SONNET generation to AnthropicApi Messages requests. Ai coordinates retries and schema/evidence validation without substituting providers. HttpTransport centralizes HTTP handling. This restores the Stage 1 provider boundaries; real connectivity has user-run PASS evidence, while full feature behavior remains separately verified.

Relationships visible in the class diagram: UI clients depend on facade; facade creates command objects and a session; session owns a state and uses planner/gate/events; planner depends on DecisionClient and selects AgentAction; Ai coordinates DecisionClient/LLMClient; JevAdapter realizes DecisionClient and ClaudeAdapter realizes LLMClient; services use typed domain records and the four planned repositories backed by Documents/SqliteStore; importer uses parser factory/strategy; intake uses source strategies. Domain compositions include report-assessments, version-changes, summary-sources, interview-questions and checklist-items.

## 4. Changes and rationale

Original design -> implemented change -> reason -> impact:

Initial generic-provider deviation -> restored JevAdapter/JevApi plus ClaudeAdapter/AnthropicApi and HAIKU/SONNET roles -> preserve the approved Stage 1 plan using verified official API contracts/model IDs -> no generic provider fallback; user-run live connectivity passed for the planned models.

Separate JevApi/AnthropicApi and ToolManager/PromptBuilder/ResponseParser wrappers -> HttpTransport, typed tools and Ai.ask() validation -> keep real responsibilities without duplicate forwarding layers -> provider/tool error handling and prompt validation remain centralized; no unused wrapper classes are claimed.

Initial consolidated-store deviation -> restored SqliteJobRepository/SqliteResumeRepository/SqliteMemoryRepository/SqliteChecklistRepository with typed Repository contract -> preserve Stage 1 responsibilities and entity links -> services now use repositories; existing data remains readable through compatibility decoding, using native sqlite3 for low-level access. Each save is transactional, but a multi-step workflow is intentionally not one transaction; completed steps survive later failures.

Initial command consolidation -> restored GetJobDescriptionAction and ImportResumeAction for F01/F02, with remaining FeatureCommand replacements pending later checkpoints -> preserve named Stage 1 participants incrementally without breaking running services -> input commands are now real facade paths; full command/state/planner alignment is not yet claimed.

AgentState onInput/onApproval -> state-specific advance() -> use the same blocking approval abstraction from CLI and background GUI -> preserves five runtime stages and allows typed shared interaction callbacks. Core application work remains off the Swing event thread.

Broad free-form resume rewriting -> bounded word-form changes with unique original match, technical/numeric token preservation and per-edit consent -> conservative deterministic validation at the cost of reduced wording flexibility. Live rewriting remains unverified; human review is still needed for semantic changes.

Word/PDF input promise -> shared parser interface with top-level DOCX/Markdown/pasted/PDF strategies and supporting DocResumeParser for binary Word -> use Apache PDFBox/POI instead of handwritten binary parsing -> offline build verifies DOCX/Markdown/paste; PDF and binary Word require Maven dependencies and remaining runtime checks. The supporting .doc class implements the original Word feature rather than adding a separate feature.

Unspecified posting deadline format -> explicitly labeled valid ISO dates only -> avoid inventing or guessing deadlines -> partial deadline automation, documented manual tracking for other formats.

Side-by-side tailoring review -> original/proposed columns and reason in each GUI approval dialog -> independent accept/reject decisions, shared with CLI review through ApprovalGate.

Checklist solely model-prioritized -> model ordering validated as an exact permutation, with deterministic fallback -> keep useful preparation work available without AI -> fallback never presents fabricated LLM content and preserves checked progress on rebuild.

Implied separate use-case/sequence artifacts -> supplied explicit updated PlantUML sources -> UMLet source only contains class elements -> original files preserved and implemented flow documented. The PDF vector diagram is a selected-class view; the PlantUML source provides fuller service/domain coverage.

## 5. AI tools and collaboration log

Actual tool: OpenAI Codex agent in this coding conversation, identified by the environment as based on GPT-6. Exact serving model variant, token accounting and monetary cost are unavailable. No usage/cost values have been invented. The coding environment used shell tools, javac/java, sqlite3, generated Java sources and offline assertion checks. No human manual coding was requested; the user supplied the assignment direction and repository.

Tasks performed by the agent: read Stage1.md and UML attributes; map F01-F10; choose configurable AI adapter and small Java architecture; implement domain/storage/parsers/services/state/command/event/facade/UI/CLI; write fixtures and tests; fix observed failures; write report, diagrams, examples and recording instructions; generate PDF. No sub-agents were used.

Representative actual interactions:
1. User: 'I want to take one of my github repos and continue it in this folder.' Agent inspected folder/Git setup; cloning by the session was blocked by permissions. User cloned it; subsequent inspection found origin/main and Stage 1 documents.
2. User supplied the GitHub URL and requested future implementation and commit guidance. Agent requested assignment requirements before coding.
3. User supplied the Stage 2 master prompt requiring Java, ten features, shared GUI/CLI, real LLM, patterns, tests and truthful reporting. Agent read all Stage 1 material and created implementation-plan.md.
4. Agent reported missing Maven/network access and blocked Git lock writes, then implemented a dependency-free Java build while retaining Maven/PDFBox configuration.
5. Verification exposed integer round-trip and paused-interview defects; agent changed the codec and interview lifecycle and reran checks.
6. Agent disclosed that live integrations, HTTP fixtures and a desktop GUI walkthrough were unverified, rather than substituting demonstration outputs.

Application-model usage is distinct from coding-tool usage: no live application LLM request was successfully verified in this session. Test responses are clearly identified fixtures. Runtime hosted-provider billing is controlled by the user's configuration; actual runtime token/cost data is unavailable.

## 6. Development and verification summary

Actual repository history inspected: three commits exist at this checkpoint; later uncommitted F01/F02 changes are not represented as a completed commit.
- 10360c8, October 5, 2026, 16:30:56 -04:00: Initial commit. Link: https://github.com/AmirrezaN8/EECS3311/commit/10360c8
- e037277, October 5, 2026, 16:46:39 -04:00: stage 1 project plan and uml in .uxf format built with umlet in eclipse. Link: https://github.com/AmirrezaN8/EECS3311/commit/e037277

- 99ca4c9, November 14, 2026, 17:49:04 -05:00: feat: add Java core with Jev and Claude adapters and typed SQLite repositories. Link: https://github.com/AmirrezaN8/EECS3311/commit/99ca4c9

Toronto weekly reporting summary:
- October 20-26, 2026: 0 meaningful code commits; requirement of 5 NOT MET.
- October 27-November 2, 2026: 0 meaningful code commits; requirement of 5 NOT MET.
- November 3-9, 2026: 0 meaningful code commits; requirement of 5 NOT MET.

The supplied instructions specify a single five-point deduction when one or more windows miss the minimum. No historical activity was fabricated/backdated, and no history was rewritten. Current implementation changes were produced November 14, 2026. The initial agent-side Git write was rejected because the session could not create .git/index.lock. The user subsequently made implementation commit 99ca4c9 in their own Terminal; origin/main was synchronized with it when checkpoint 3 began. No F01/F02 checkpoint commit or push has yet been made. docs/commit-guide.md now follows actual completed milestones rather than a prescribed total count. Update final hashes/links from actual history.

Executed build: sh scripts/build.sh. Result: production sources compiled with javac --release 21; executable target/career-agent.jar built; SelfTest compiled and passed 301 assertions, with explicit dependency-related skips outside successful runtime claims. Tests used sqlite3 and labeled model/tool fixtures. JUnit entry OfflineTest delegates to the same checks but Maven/JUnit execution was unavailable.

Verification covered: strict JSON/CLI parsing; UTF-8 SQL-safe persistence and restart; typed record mapping; DOCX and malicious XML; parser selection; AI malformed retry and choice validation; source labels/countries/deadlines; raw profile fallback; memory CRUD/dedup/error isolation; skill evidence/grouping; reviewed token-preserving edits; bounded no-source research; exactly five questions; interview follow-up/feedback/pause/restart; partial checklist/ticks; real State/Command workflow approvals, stop, cap and failed-step continuation; LLM envelope decoding; full fixture-driven preparation -> interview -> feedback -> checklist -> app restart.

Additional executed checks: separate CLI job add/show processes returned the same saved job/source/deadline; CLI help output correct; headless Swing startup provides a clear display requirement and exits without a crash; git diff --check showed no tracked whitespace errors. Original Stage 1 checksums are recorded in docs/verification.md.

Important defects found/fixed: integer JSON round trips lost integer form; parser now preserves representable integer tokens. Interview stop initially marked unfinished sessions irreversibly finished; stop now persists a paused session, with feedback approval handled separately. Planner context initially used a snapshot; it now reads current saved preparation results at each decision. GUI prevents overlapping new work while cancelled background work unwinds.

Blocked/unverified checks: optional HTTP fixture run failed at localhost bind with SocketException: Operation not permitted; it is not counted as a passing network test. Full live Claude/Jev application workflows, Adzuna/Brave requests, Maven packaging/dependency resolution, PDFBox extraction and interactive GUI behavior were not verified. Report PDF was generated from this Markdown using scripts/ReportPdf.java; structural checks confirm PDF header, page objects, cross-reference offsets and end marker. Visual page-by-page review was not performed.

No claim is made of exhaustive Stage 3 testing. Remaining checks before submission: real-model feature walkthrough; live job/company service checks if those are to be demonstrated; Maven/PDF import; desktop GUI operation and cancellation; record/upload videos and replace placeholders; compare official Stage 2 PDF; genuine commits and report update. These do not require pretending implementation already passed external acceptance.

## 7. Demonstration index

No video links or timestamps exist yet. The following placeholders are intentional and must be replaced with real recordings. See docs/demo-guide.md for exact actions and evidence.

F01 | Job intake/labels/estimate consent | GUI Job + CLI job show | NOT RECORDED | URL TBD | timestamp TBD
F02 | File/paste resume and extraction | GUI Resume | NOT RECORDED | URL TBD | timestamp TBD
F03 | Remember, show, edit, delete facts | My History + CLI | NOT RECORDED | URL TBD | timestamp TBD
F04 | Ordered gap groups and uncertainty | GUI Preparation / CLI gap | NOT RECORDED | URL TBD | timestamp TBD
F05 | Original/revised/reason, accept/reject, version | GUI Preparation + CLI | NOT RECORDED | URL TBD | timestamp TBD
F06 | Live sourced company research, no-result handling | GUI Preparation | NOT RECORDED | URL TBD | timestamp TBD
F07 | Five personalized questions and interview offer | GUI Preparation | NOT RECORDED | URL TBD | timestamp TBD
F08 | Typed answers, follow-up, pause/resume, feedback | GUI Mock Interview + CLI | NOT RECORDED | URL TBD | timestamp TBD
F09 | Partial/full checklist, ticks and restart progress | GUI Checklist + CLI | NOT RECORDED | URL TBD | timestamp TBD
F10 | Goal, decisions, approvals, failures, trace, checklist | GUI Preparation + CLI | NOT RECORDED | URL TBD | timestamp TBD

Recording order: build and configure services; use non-sensitive sample inputs; show GUI job/resume/history; execute gap/tailor/research/questions; complete an interview; tick checklist; restart to show persistence; run a separate autonomous workflow and one CLI operation. Keep API keys and private resume information off screen. A missing-credential error demonstrates error handling, not a successful live integration.

Source references consulted for API contracts:
Jev API: https://docs.typesafe.ai/introduction/quickstart
Anthropic Messages API: https://platform.claude.com/docs/en/api/messages/create
Adzuna API overview: https://developer.adzuna.com/overview
Brave Search API: https://api-dashboard.search.brave.com/app/documentation/web-search

End of editable report. The PDF adds a selected-class UML view rendered directly as vector boxes/arrows. Full class, use-case and sequence diagram sources accompany the submission.

November 15 F05 checkpoint: TailorResumeAction, bounded word-form rewriting, overlap rejection, shared missing-input recovery and structured side-by-side GUI review are implemented. See docs/checkpoint-5-tailoring.md for evidence and live checks. The report PDF has not yet been refreshed for these checkpoint changes.
