# EECS3311 Stage 2 — Implementation Report

CURRENT CHECKPOINT UPDATE: Named top-level domain entities and SqliteJobRepository, SqliteResumeRepository, SqliteMemoryRepository and SqliteChecklistRepository are restored and used by the services/facade. Typed skills, creation/edit dates, checklist identity/deadline and job-linked question answers are implemented. Compatibility reads preserve older records; atomic selection and interview/feedback restart are tested. Current build: 233 offline assertions pass, including 79 domain/repository checks and a separate-JVM restart. See docs/checkpoint-2-persistence.md. Later command/tool/state/F05/F10/UI corrections remain pending. Existing string identity storage and native sqlite3 transport are retained and documented; no JDBC implementation is claimed.

WORKING REPORT — NOT FINAL SUBMISSION. This original implementation snapshot predates strict Stage 1 corrections. Checkpoint 1 has since restored JevAdapter/JevApi, ClaudeAdapter/AnthropicApi, Decision probabilities, ModelTier.HAIKU/SONNET, PromptBuilder and ResponseParser. The generic provider adapter is removed. Current verification: 154 offline assertions pass; API smoke checks report missing keys. Sections below still include historical architecture descriptions and must be reconciled with all checkpoint changes before submission. See docs/checkpoint-1-apis.md for current provider evidence.

Career Preparation AI Agent
Repository: https://github.com/AmirrezaN8/EECS3311
Prepared for Amirreza N8 | November 14, 2026 | Toronto local time
Status: Java implementation with verified offline core; live integrations and desktop demonstration pending.

## 1. Project overview and implementation status

The application helps new graduates prepare applications in Canada and the United States. It accepts job postings and resumes, remembers career facts, identifies skill gaps, proposes conservative tailored resume edits, researches employers, generates questions, runs typed interviews, and tracks preparation. A bounded agent coordinates these services rather than merely forwarding a chat prompt.

Implementation baseline: Stage1.md was read in full and uml.uxf panel attributes were extracted. The repository initially contained only these design files and .gitattributes. Both original documents remain byte-for-byte unchanged. The UMLet file contains class diagrams, but the use-case/sequence diagrams mentioned in the Markdown are not present as corresponding UMLet elements. Updated class, use-case and sequence sources accompany this report. No Stage 2 instruction PDF was supplied; compliance is assessed against the user's supplied requirements, pending comparison with that PDF.

Architecture: Java 21-compatible source, built with Temurin 27. SwingGui and CliApp share AgentController. Domain data consists of typed records in Models.java. Feature services use Ai for validated JSON generation/decisions, Documents for explicit record conversion and SqliteStore for durable SQLite documents. No web frontend, Python core or JavaScript core exists. Maven configuration includes PDFBox and JUnit; an offline JDK-only build and assert-based test runner avoid blocked downloads.

LLMAdapter communicates with an actual configured Ollama chat endpoint or HTTPS OpenAI-compatible chat/completions endpoint. Local inference avoids a mandatory paid subscription. Ai implements DecisionClient with bounded choices and confidence checks. Invalid JSON, invalid choice, invalid count and unsupported evidence trigger at most one retry. Service/credential failures are visible. No canned AI result is shipped in production. Explicit model fixtures are limited to tests.

Working offline functionality verified: posting entry/selection/persistence, DOCX/Markdown/text extraction, raw resume retention on AI failure, memory CRUD/deduplication, deterministic edit validation, partial checklist/progress, JSON and error boundaries. AI-dependent service paths and the complete orchestrated preparation/interview workflow passed with labeled fixtures. Live model quality, provider connectivity, live company research and job search are NOT claimed verified.

GUI: job/profile/history/preparation/interview/checklist tabs; typed inputs, readable results, approval dialogs, progress events, check boxes and percentage progress. Long work runs in SwingWorker; dialogs use the Swing event thread. CLI supports interactive and one-shot commands. Both call the same facade. Desktop GUI interaction is pending; compilation and graceful headless startup were verified.

Known limits: PDF parsing requires the Maven build and remains unverified here. Legacy binary Word .doc, scanned and locked PDFs need pasted text. Tailoring permits only reordered existing tokens; it rejects broader prose rewrites. Token preservation is not a proof of semantic equivalence, so user review remains essential. Skill matching is conservative/literal and marks uncertain evidence. Automatic deadlines recognize explicitly labeled ISO dates only. Research summarizes search snippets, not entire crawled sites. Local SQLite data is unencrypted. Videos have not been recorded. The Git weekly requirements are currently unmet.

## 2. Feature-to-implementation mapping

All ten Stage 1 features have application paths. 'Implemented' here describes connected code plus specified verification; it does not mean a live external service has been exercised. Modified scope is called out explicitly. All file names below are under src/main/java/career/ unless stated otherwise. Source links and commands are also indexed in docs/demo-guide.md.

### F01 — Job description intake

Reference: Stage1.md F01, UC01, SD01 reference. Status: Implemented with provider/consolidation changes. Type: hybrid.
Files/classes: JobDescriptionIntake.java; JobDescriptionSource.java; AdzunaTool.java; Models.Job/SourceLabel; AgentController.java; SqliteStore.java.
Methods: JobDescriptionIntake.add(), find(), save(), explicitDeadline(); JobDescriptionSource.fetch()/label()/needsApproval(); AdzunaTool.search(); AgentController.runFeature().
Flow: GUI Job form or CLI job add/find -> facade -> intake/source strategy -> Adzuna lookup with retry -> constrained listing selection -> explicit approval for expanded/full estimate -> persist labeled Job and attribution. Rejected expansion may retain an approved partial excerpt. No estimate is silently treated as an official posting. Missing credentials permit approved full-estimate fallback or instruct the user to paste.
Patterns: Facade, Strategy, Adapter; the facade dispatches intake directly rather than allocating a separate named GetJobDescriptionAction.
Verification: pasted source/country/deadline/persistence and approved/rejected estimate tests. Real Adzuna selection remains unverified. Demo: NOT RECORDED; URL/TIMESTAMP TBD; GUI Job and CLI job show.

### F02 — Resume import and skill extraction

Reference: F02, UC02. Status: Implemented; PDF path pending dependency/runtime verification. Type: hybrid.
Files/classes: ResumeImporter.java, ResumeParser.java, ResumeParserFactory.java (PdfResumeParser, DocxResumeParser, MarkdownResumeParser, PastedTextResumeParser), Models.ResumeProfile, Ai.java.
Methods: importResume(), importText(), current(), createParser(), extractText().
Flow: file/paste -> format strategy -> text -> strict model extraction into skills, education, experience, projects, certifications -> new base record and selected-profile pointer. Extracted skills must occur in the source text. After two model failures raw text is saved with a visible warning. Original profiles are retained across imports.
Patterns: Factory, Strategy, Adapter, Facade. A pasted-text helper accepts a String, while file strategies implement ResumeParser(Path).
Verification: Markdown/DOCX extraction, external-entity rejection, format selection, invalid/blank input, evidence rejection, labeled raw fallback and persistence. PDFBox extraction is not exercised. Demo: NOT RECORDED; URL/TIMESTAMP TBD; GUI Resume.

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

Reference: F05, UC05. Status: Modified: conservative extractive tailoring rather than unrestricted rewording. Type: hybrid.
Files/classes: ResumeTailorService.java; Models.ResumeChange, ResumeVersion; ApprovalGate.java.
Methods: tailor(), safeChange(), tokens(), review().
Flow: profile/posting -> proposed edits with original/revised/reason -> deterministic unique-substring and token-multiset validation -> per-change user accept/reject dialog -> save a new job/base-linked version. Changed numbers/new skill tokens are rejected. Previously stored original resume is never overwritten. Each approval displays both texts together; GUI uses a scrollable comparison dialog rather than two permanent columns.
Patterns: Command, Facade, Adapter.
Verification: accepted/rejected edits, unsupported skill and invented-number rejection, ambiguous repeated substring rejection and original preservation. Review is still needed to catch meaning changes from reordered words. Demo: NOT RECORDED; URL/TIMESTAMP TBD; GUI tailoring dialog and CLI resume versions.

### F06 — Company goals and values research

Reference: F06, UC06. Status: Implemented; live service pending. Type: hybrid/agentic.
Files/classes: CompanyResearchService.java; WebSearchTool.java; Models.CompanySummary, SearchResult; Ai.java.
Methods: research(), search(), decide(), ask().
Flow: model query -> Brave search snippets -> per-source relevant/irrelevant decision -> reformulated query if empty, maximum two searches -> sourced goals/values summary -> persist sources alongside result. Missing configuration or no relevant sources yields an error and no new summary. Stored older evidence is retained rather than overwritten with invented content.
Patterns: Command, Adapter, Facade.
Verification: bounded empty-result retry/no invented summary and relevant-source storage with explicit fixtures. Factual summarization/relevance quality and live Brave connectivity are unverified. Demo: NOT RECORDED; URL/TIMESTAMP TBD; GUI Research/company CLI.

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

Seven distinct patterns participate in real application behavior. The runnable implementation uses nested record/state types where that avoids file-only boilerplate. Source: docs/implemented-class-diagram.puml. A readable vector class diagram is included after the report text in the PDF. Updated use-case and sequence diagram sources: docs/implemented-use-cases.puml and docs/implemented-sequence.puml.

Facade: AgentController is the facade; SwingGui and CliApp are clients. runFeature(), handleGoal() and startMockInterview() coordinate services/persistence/approvals. Both interfaces invoke the same operations. This matches Stage 1 and avoids duplicate business logic.

Command: AgentAction defines name()/describe()/execute(). FeatureCommand stores a service Supplier<Object> and is instantiated by AgentController.actions() for gap, tailoring, company research, interview prep and checklist. Planner chooses an AgentAction and ExecutingState invokes execute(). Unlike Stage 1's seven separate concrete classes, the same meaningful command implementation captures five operations; intake/import are direct input-bound facade calls. This preserves uniform planner execution without one trivial forwarding class per feature.

Strategy: ResumeParser.extractText(Path) has PDF, DOCX and Markdown/text strategies chosen by the importer. JobDescriptionSource.fetch()/label()/needsApproval() has pasted, excerpt, expanded estimate and full estimate strategies. Intake selects the strategy after user approvals. Source strategies encapsulate retrieval/generation differences, while parser strategies encapsulate format differences. Pasted resume input has a String helper instead of pretending to be a path.

Factory: ResumeParserFactory.createParser(Path) selects PdfResumeParser, DocxResumeParser or MarkdownResumeParser. ResumeImporter calls the returned interface and does not implement extension-switching itself. This directly preserves the proposed factory role. Unknown extensions are rejected.

State: AgentSession holds AgentState; IdleState, PlanningState, AwaitingApprovalState, ExecutingState and FinishedState each implement advance(session). Planning owns selection, approval owns consent/rejection, execution owns result/failure transition. setState() publishes transitions. Session drives state behavior with a bounded loop instead of implementing all behavior in a single switch. advance() replaces the Stage 1 onInput/onApproval pair because approval is synchronous at the shared interaction boundary.

Observer: AgentEventPublisher holds ProgressObserver callbacks; attach()/detach()/publish() manage subscriptions and notifications. AgentSession and MemoryManager publish progress/warnings without knowing GUI widgets. CliApp prints events, while SwingGui schedules updates onto the event thread. Failing observers cannot abort saved work. This preserves Stage 1, using lambdas rather than making the entire UI class implement a marker role.

Adapter: LLMAdapter implements LLMClient, translating real Ollama/compatible request and response envelopes. Ai adapts structured generations to DecisionClient's fixed-choice decisions. HttpTransport centralizes timeouts/status/secure endpoint handling. The application does not depend on Jev/Anthropic-specific API types. The change makes model selection configurable and removes unverified speculative model-version requirements.

Relationships visible in the class diagram: UI clients depend on facade; facade creates command objects and a session; session owns a state and uses planner/gate/events; planner depends on DecisionClient and selects AgentAction; Ai realizes DecisionClient and uses LLMClient; LLMAdapter realizes LLMClient; services use typed domain records and Documents/SqliteStore; importer uses parser factory/strategy; intake uses source strategies. Domain compositions include report-assessments, version-changes, summary-sources, interview-questions and checklist-items.

## 4. Changes and rationale

Original design -> implemented change -> reason -> impact:

Jev decisions + named Claude Haiku/Sonnet versions -> Ai constrained decisions plus configurable LLMAdapter fast/reasoning tiers -> avoid reliance on unavailable/unverified provider/model names and permit local inference -> same application capabilities and Adapter boundary; latency/quality depend on configured model. No claimed Jev/Claude integration.

Separate JevApi/AnthropicApi and ToolManager/PromptBuilder/ResponseParser wrappers -> HttpTransport, typed tools and Ai.ask() validation -> keep real responsibilities without duplicate forwarding layers -> provider/tool error handling and prompt validation remain centralized; no unused wrapper classes are claimed.

Four Sqlite repositories and generic Object CRUD -> one kind/id SQLite document store plus explicit typed Documents mapping -> minimize repetitive CRUD and avoid unavailable JDBC download -> same local persistence and record relationships, with sqlite3 executable required. Each save is transactional, but a multi-step workflow is intentionally not one transaction; completed steps survive later failures.

Separate concrete command classes for each service -> actual FeatureCommand objects capturing service calls, with intake/import directly dispatched -> fewer one-method classes -> planner still selects/executes commands polymorphically; design mapping explicitly records the consolidation.

AgentState onInput/onApproval -> state-specific advance() -> use the same blocking approval abstraction from CLI and background GUI -> preserves five runtime stages and allows typed shared interaction callbacks. Core application work remains off the Swing event thread.

Broad free-form resume rewriting -> token-preserving extractive reordering with unique original match and per-edit consent -> provide an enforceable deterministic protection against new skill names/numbers -> reduced wording flexibility; Stage 1 F05 is marked Modified rather than overstated. Human review is still needed for semantic changes.

All Word/PDF formats implied -> DOCX through safe JDK XML/ZIP; PDFBox via Maven; .doc/scanned/locked input directed to paste -> avoid hand-written fragile PDF/binary Word parsing -> offline build supports text/DOCX/Markdown now; PDF functionality requires dependency build and remains unverified.

Unspecified posting deadline format -> explicitly labeled valid ISO dates only -> avoid inventing or guessing deadlines -> partial deadline automation, documented manual tracking for other formats.

Permanent side-by-side tailoring columns -> original/revised/reason together in approval dialog -> one shared per-change approval mechanism -> equivalent review/accept/reject flow with a layout difference.

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

Actual repository history inspected: only two commits exist at the time of this report.
- 10360c8, October 5, 2026, 16:30:56 -04:00: Initial commit. Link: https://github.com/AmirrezaN8/EECS3311/commit/10360c8
- e037277, October 5, 2026, 16:46:39 -04:00: stage 1 project plan and uml in .uxf format built with umlet in eclipse. Link: https://github.com/AmirrezaN8/EECS3311/commit/e037277

Toronto weekly reporting summary:
- October 20-26, 2026: 0 meaningful code commits; requirement of 5 NOT MET.
- October 27-November 2, 2026: 0 meaningful code commits; requirement of 5 NOT MET.
- November 3-9, 2026: 0 meaningful code commits; requirement of 5 NOT MET.

The supplied instructions specify a single five-point deduction when one or more windows miss the minimum. No historical activity was fabricated/backdated, and no history was rewritten. Current implementation changes were produced November 14, 2026. Git add/commit was attempted and rejected because the session cannot create .git/index.lock. Therefore there are no new implementation commit hashes or links to claim. docs/commit-guide.md supplies five logical current-date commit groups for the user to review/run. After genuine commits, update this section from actual Git history and regenerate the PDF. No push was performed or authorized.

Executed build: sh scripts/build.sh. Result: production sources compiled with javac --release 21; executable target/career-agent.jar built; SelfTest compiled and passed 97 assertions. Tests used sqlite3 and labeled model/tool fixtures. JUnit entry OfflineTest delegates to the same checks but Maven/JUnit execution was unavailable.

Verification covered: strict JSON/CLI parsing; UTF-8 SQL-safe persistence and restart; typed record mapping; DOCX and malicious XML; parser selection; AI malformed retry and choice validation; source labels/countries/deadlines; raw profile fallback; memory CRUD/dedup/error isolation; skill evidence/grouping; reviewed token-preserving edits; bounded no-source research; exactly five questions; interview follow-up/feedback/pause/restart; partial checklist/ticks; real State/Command workflow approvals, stop, cap and failed-step continuation; LLM envelope decoding; full fixture-driven preparation -> interview -> feedback -> checklist -> app restart.

Additional executed checks: separate CLI job add/show processes returned the same saved job/source/deadline; CLI help output correct; headless Swing startup provides a clear display requirement and exits without a crash; git diff --check showed no tracked whitespace errors. Original Stage 1 checksums are recorded in docs/verification.md.

Important defects found/fixed: integer JSON round trips lost integer form; parser now preserves representable integer tokens. Interview stop initially marked unfinished sessions irreversibly finished; stop now persists a paused session, with feedback approval handled separately. Planner context initially used a snapshot; it now reads current saved preparation results at each decision. GUI prevents overlapping new work while cancelled background work unwinds.

Blocked/unverified checks: optional HTTP fixture run failed at localhost bind with SocketException: Operation not permitted; it is not counted as a passing network test. Live Ollama/hosted inference, Adzuna/Brave requests, Maven packaging/dependency resolution, PDFBox extraction and interactive GUI behavior were not verified. Report PDF was generated from this Markdown using scripts/ReportPdf.java; structural checks confirm PDF header, page objects, cross-reference offsets and end marker. Visual page-by-page review was not performed.

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
Ollama API: https://github.com/ollama/ollama/blob/main/docs/api.md
Adzuna API overview: https://developer.adzuna.com/overview
Brave Search API: https://api-dashboard.search.brave.com/app/documentation/web-search

End of editable report. The PDF adds a selected-class UML view rendered directly as vector boxes/arrows. Full class, use-case and sequence diagram sources accompany the submission.
