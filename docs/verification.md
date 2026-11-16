# Verification record — November 14, 2026 (Toronto)

## Current: user-run DOCX and basic GUI check

The user supplied successful live DOCX import output with all expected sections populated and an empty warning, and stated the GUI worked. Together with prior evidence, pasted F01 intake and real Markdown/DOCX F02 extraction have passed. GUI evidence is a basic user confirmation, not exhaustive screen/error/cancellation coverage. No recording has been supplied. Adzuna/estimate and PDF/legacy Word checks remain pending.

## Earlier: successful user-run live Markdown extraction

After rebuilding the parser fix, the user reported a passing 325-assertion suite and successful real `resume import samples/resume.md`: Java/SQL/Git/JUnit, education, both experience items, project and certification were populated, with an empty warning. This is user-run evidence for the Markdown/Haiku extraction path, not an independently observed agent-side request. The preceding pasted F01 path also succeeded. DOCX/PDF/legacy Word, Adzuna/estimate and desktop checks remain pending. No new commit/push/video is claimed.

## Earlier: user-reported resume extraction failure and parser regression fix

User-run build passed 301 assertions and pasted F01 intake succeeded. Live F02 import failed with `Invalid JSON number` and correctly retained raw text; it is not marked live-verified. A Markdown-wrapped valid JSON fixture reproduced that exact parser error, but the original live response was not captured, so its exact format remains unknown.

ResponseParser.validate now accepts one complete JSON code block, keeps strict syntax/schema/evidence validation, and rejects extra prose/multiple blocks. Ai passes the actual previous validation error into the retry prompt. Unknown JSON tokens no longer get misleading numeric diagnostics. AgentController emits warning for a raw-fallback resume result. No model response/resume content is logged in parse diagnostics.

`sh scripts/build.sh`: PASS, 325 assertions (79 API/response, 79 repository, 74 input-feature, 93 prior service/core). Tests include the real adapter/importer/facade/repository route under a fenced-response fixture. Live rerun is pending. The two dependency-related SKIP notices remain honest and unchanged. No new commit/push/video is claimed.

## Previous: F01/F02 checkpoint

`sh scripts/build.sh` passes 301 assertions (61 API, 79 repositories, 68 input-feature and 93 prior service/core checks). This includes actual named commands/tool routing, consent/fallback behavior, explicit JSON negotiation, resume-section evidence, file/XML/input errors and non-destructive rejection/cancellation. The old hostile XML diagnostic is now suppressed by an explicit error handler while parsing still fails safely. The suite explicitly prints SKIP for real PDF/legacy Word paths lacking dependencies/samples. See checkpoint-3-inputs.md. An actual deterministic CLI job add/show smoke check also passed in separate processes against a temporary database, retaining content/source/deadline. Live AI/tool F01/F02 walkthrough, Maven package and desktop interaction remain pending; no new commit or video is claimed.

## Previous: domain and repository checkpoint

`sh scripts/build.sh` passes 233 assertions (61 API contract fixtures, 79 domain/repository checks and 93 core/service checks). The four named repositories are in actual feature use. Temporary legacy fixtures verify non-destructive reads; SQLite trigger-injected failures verify rollback; a separate JVM verifies saved data/relationships after process restart. No user's database was inspected or changed for these checks. See checkpoint-2-persistence.md for details. Separate CLI processes using a temporary database passed job add/show and history add/show, retaining IDs, source labels, deadline and fact timestamp. Personal configuration and decision notes remain ignored; no commit/push is claimed.

## Previous: API alignment checkpoint

Later user-run verification: the user supplied PASS output from `career.ApiCheck all` in their own Terminal for Jev (`jev-latest`, Java, confidence 1.0), Claude Haiku 4.5 (`claude-haiku-4-5-20251001`) and Claude Sonnet 5.5 (`claude-sonnet-5-5`). This passes the API connectivity gate based on user-reported results; the agent did not independently observe those requests. The agent-side attempt after configuration could not resolve either API hostname. Feature-level live verification is still pending. No token/cost data was supplied.

- `sh scripts/build.sh` — PASS: 154 assertions, including 61 Jev/Claude API-boundary assertions and 93 service/core assertions. The intentionally hostile DOCX fixture's DOCTYPE diagnostic remains expected.
- Offline provider fixtures intercept HTTP to verify endpoints, authentication, model IDs, request/envelope formats, probabilities, confidence, errors, retry, cancellation and routing. They never call real providers. Adzuna test transport is also intercepted even if local keys exist.
- `java -cp target/career-agent.jar career.ApiCheck all` — NOT VERIFIED (exit 1): TYPESAFE_API_KEY and ANTHROPIC_API_KEY absent. No paid/live request was made; no fallback occurred.
- Production defaults now instantiate JevAdapter/JevApi and ClaudeAdapter/AnthropicApi. LLMAdapter was deleted; clean compilation removes its stale bytecode.
- Original Stage1.md/uml.uxf unchanged. Personal decisions note and local career.properties remain ignored.
- See checkpoint-1-apis.md for setup and the later user-run live PASS evidence. Earlier verification below is historical, not certification of current API access.

## Earlier initial implementation checks

Environment: macOS, Temurin JDK 27; `javac --release 21`; sqlite3 3.51.0. Maven absent. Maven Central DNS resolution failed in this sandbox. No credentials or local model were provisioned by the user.

Executed:
- `sh scripts/build.sh` — PASS, 97 assertion checks. Compiles all production Java, builds `target/career-agent.jar`, compiles/runs SelfTest. The intentionally hostile DOCX fixture prints a DOCTYPE rejection diagnostic; that is an expected passing security check.
- `java -jar target/career-agent.jar help` — PASS, complete CLI help.
- `java -Djava.awt.headless=true -jar target/career-agent.jar --gui` — PASS, clear desktop-display requirement with no crash. This is not an interactive GUI test.
- Separate one-shot CLI `job add` and `job show` launches against a temporary SQLite file — PASS, identical saved job/source/deadline returned.
- `file stage2-report.pdf` — PDF 1.4, 11 pages; generated cross-reference offsets/header/end marker checked.
- `git diff --check` — no whitespace errors in tracked differences; new files separately compiled/inspected.
- Optional HTTP fixture run — BLOCKED: `java.net.SocketException: Operation not permitted` on localhost bind. It is not counted among the 97 passing assertions.

Suite coverage: strict JSON grammar/duplicates/depth, CLI quoting, SQL-safe UTF-8 data and upsert/restart/delete, typed record mapping, DOCX text and XXE rejection, factory selection, unsupported legacy Word errors, resume structure/fallback/evidence, constrained AI choices and one retry, pasted job labels/country/ISO deadlines, approved/rejected estimate fallback, memory dedup/edit/delete/error isolation, gap grouping and unsupported-skill uncertainty, accepted/rejected tailoring with numeric protection, company relevance/no-source bounded retry, question count/format, one follow-up per question, persisted interview pause/resume/completion/archive and partial feedback, partial checklist/ticks/rebuild, state transitions, approval rejection, failure continuation, stop and ten-step cap, shared facade errors, LLM envelope parsing, full preparation-to-interview-to-feedback-to-restart workflow using explicit test fixtures.

Defects found and fixed: JSON round trips initially changed integer `2` to `2.0`; codec now retains integer tokens when representable. Early interview stop initially prevented resume; stop now persists a paused session and partial feedback remains separately approved. Planner now reloads saved results between decisions. GUI prevents overlapping work while a cancelled background task unwinds.

Not executed: live Ollama or hosted LLM inference, real Adzuna/Brave requests, HTTP fixture integration, Maven resolution/JUnit runner, PDFBox PDF extraction, manual desktop GUI interaction, videos. These are remaining checks, not claimed successes. PDF report generation and structural inspection are recorded in the editable report after completion.

Original file SHA-256 checksums:
- Stage1.md: `8410f72c69c04b4c47488567a03431afa7e327a92f828dda6f424c780fc3959f`
- uml.uxf: `1e2c6417ac77a738dbf9b3166bfb1b8bb7c861ec96c6f39a4c26ad832c1e0ee4`

Only basic Stage 2 verification is claimed. This is not a claim of Stage 3 acceptance, exhaustive security testing, or assessed model quality.

## F03 memory checkpoint — November 14, 2026

`sh scripts/build.sh` passes 350 offline assertions after memory validation/outcome events and focused acceptance checks, including a SQLite trigger that rejects writes while existing facts remain readable. `git diff --check` passes. See [checkpoint-4-memory.md](checkpoint-4-memory.md) for scope, limits and the pending real-provider/GUI walkthrough. PDFBox and valid legacy Word integration checks remain explicitly skipped in this dependency-free build. No F03 live or video success is claimed.

## F03 live follow-up and F04 — November 15, 2026

User-provided F03 output confirms conflict rejection, exact duplicate skipping, approved deletion, editing and retrieval across CLI processes. GUI target-role extraction initially skipped a useful aspiration; after clarifying the Jev prompt and restarting the GUI, the user supplied memory-saved and history output with category target roles. Semantic duplicate and accepted-conflict branches remain unverified live. No video URL supplied.

F04 now offers missing-input recovery, displays ordered groups and explicit uncertainty for direct/saved output, and rejects substring-only skill evidence. `sh scripts/build.sh` passes 365 offline assertions, including normalization/deduplication, all three groups, nested saved-report presentation, uncertainty, linked persistence, unsupported extraction preserving prior reports, misleading substrings and accepted/declined input recovery. `git diff --check` passes. See checkpoint-5-gap.md for live steps and evidence limits. Real F04 providers/desktop remain unverified; PDF/legacy Word dependency skips remain.

## F04 live result and F05 checkpoint — November 15, 2026

User-provided F04 output confirms successful real sample import, required-skill analysis with Docker MISSING, Java/JUnit STRONG, SQL/Git DECENT, explicit uncertainty, and saved results with the original timestamp. The user confirmed the saved-result output came from GUI. Live missing-input recovery is not yet evidenced.

F05 now supports bounded word-form rewriting, the named TailorResumeAction, structured per-change CLI/GUI approval, overlap rejection and missing-input recovery. `sh scripts/build.sh` passes 382 offline assertions, including supported prose changes, invented proficiency/skills/numbers, negation/technical punctuation, separate accept/reject, base preservation, linked restart persistence, overlapping-proposal retry with no approval/new version and missing-input consent. `git diff --check` passes. See checkpoint-5-tailoring.md. Live Claude rewriting and desktop comparison remain pending. The PDF report is not refreshed yet.

## F06 company research — November 15, 2026

`sh scripts/build.sh` passes 404 offline assertions after restoring CompanyResearchAction and adding source-citation validation and query/URL boundary checks. Acceptance covers relevant filtering, bounded reformulation, supplied-only source context, missing/invented citations and retry, all-irrelevant results, HTTP request fixtures, unsafe URLs, missing keys, outages, restart persistence and preservation of previous results. `git diff --check` passes. Real-provider and desktop checks remain pending; see checkpoint-6-company.md. The existing F03–F05 commit is cfe23e8; no new commit/push made here.

## F06 live evidence and F07 checkpoint — November 15, 2026

User-provided F06 Terminal output confirms missing-key handling followed by successful Microsoft research with cited goals and values explicitly not stated. Saved-result/GUI research evidence and video URLs have not been supplied. F06 is committed as 8ca6813.

F07 now uses InterviewPrepAction and passes full job/profile/company/history context to both reasoning and generation stages. `sh scripts/build.sh` passes 423 offline assertions. New checks verify model tiers/order, context, linked unanswered questions, restart persistence, invalid JSON/count/duplicate/empty/non-string retry without overwriting prior results, valid retry recovery, and independent mock-interview consent (decline retains questions without a session; acceptance starts one). `git diff --check` passes. Live F07 generation/personalization, GUI and consent checks remain pending. See checkpoint-6-questions.md. PDF report regeneration remains deferred until the submission refresh.

## F08 mock interviews — November 15, 2026

F07 is committed as e5bc049. F08 now generates final feedback automatically after the completing answer through the shared facade, and isolates weakness-memory failures from saved feedback. `sh scripts/build.sh` passes 443 offline assertions, including blank-answer preservation, no mid-interview generation, resume across controllers, declined/approved partial feedback, completion and persisted report, real SQLite memory-write failure, invalid-count retry preserving an existing report, archived restart and recovery of pending Jev decisions after a saved answer. `git diff --check` passes. See checkpoint-7-interview.md for actual live steps; no real-model/desktop F08 success is claimed. PDFBox/legacy Word dependency skips remain.

F08 follow-up: the user reported the walkthrough works. Record this as basic user-reported live success, not detailed evidence for every alternative branch. No video URL/timestamp supplied.

## F09 preparation checklist — November 15, 2026

`sh scripts/build.sh` passes 464 offline assertions after ChecklistAction restoration, first-show creation, progress display, approved-work refresh, stable missing-feature offers and strict explicit date parsing. New checks cover written/ISO date forms and invalid/ambiguous/conflicting inputs; task/deadline priority; reasoning tier; tick/untick/restart/rebuild identity; invalid-ID preservation; initial show/declined offers; no repeated offers on saved show; and refresh after approved research. `git diff --check` passes. Live prioritization/GUI/persistence remains pending user evidence. See checkpoint-8-checklist.md. Existing F08 commit is 642b229; no F09 commit made here.

## F10 autonomous workflow — November 15, 2026

`sh scripts/build.sh` passes 480 offline assertions after full planner action restoration, done, request classification, state-owned approval handling, explicit finish outcomes and unlinked pre-intake session traces. Focused checks cover all eight initial options, early checklist completion, requested/proposed consent, classifier-failure fallback, invalid approval state, stop during approval, failed-step/planner outcomes, ten-step limit, planned intake/import from an empty database, linked outputs/trace and done without a job. Nonempty trace job IDs still require saved jobs. `git diff --check` passes. Real planner decisions, input collection, GUI cancellation and recordings remain pending; see checkpoint-9-agent.md. F09 is committed as c0fbb75. No F10 commit/push made here.

F10 user evidence: generic-goal trace confirms proposed gap/checklist actions with approval/execution states and successful checklist finish. Supplied checklist includes an explicit deadline, practice and remaining tasks. Explicit-goal trace confirms requested gap/checklist execute without overall step approval. User reports F10 recording done; no URL/timestamps supplied. Live decline/done/cancellation branches remain unverified.


## Final-submission Step 2 — F01–F09 video review, November 15, 2026

The supplied EECS3311demos playlist contains nine unlisted videos mapping to F01–F09. All were retrieved without cookies/account login, downloaded into temporary storage and inspected through sampled frames. Whole-audio volume analysis reports mean/max approximately -91 dB for all nine, with no available captions; narration/feature-ID/AI explanation requirements are therefore not fully established. Actual GUI and CLI footage is present. Side-by-side tailoring approval at 00:34 and separate mock-interview consent at 00:28 were directly inspected. Coverage is limited to observed actions, with specific unverified branches in demo-guide.md. The user explicitly deferred F10 because it is not uploaded. Direct links/start timestamps were added to demo-guide.md and report section 7; no application code, PDF, videos, commits or remote publication changed in this step.


## Final-submission README organization — November 15, 2026

Read all five pages of Project-Stage2-Instruction (4).pdf; its SHA-256 matches the previously audited (3) copy. README.md now serves as the reviewer entry point: report/source/demo/diagram links, F01–F09 links in author-confirmed upload order, F10 upload pending, all ten source paths, seven pattern roles, prerequisites/installation, safe local configuration, separate offline/Maven builds, GUI/CLI launch, example inputs, troubleshooting and accurate deficient weekly windows. Historical 325-assertion and uncommitted-feature claims were removed. Pending report/AI-cost/diagram/video/package evidence is explicitly distinguished from complete artifacts. Local Markdown targets, nine video mappings, shell-block syntax and configuration-field coverage passed; CLI help and git diff --check passed. The earlier clean-snapshot audit passed 480 assertions; those tests were not needlessly repeated for this documentation-only change. No application code, report PDF, external publication or Git history was changed. Maven/parser verification and final report generation remain separate tasks.


## Final-submission editable report reconciliation — November 15, 2026

Replaced historical checkpoint narrative in docs/stage2-report.md with one current seven-section report tied to implementation commit 3cefcf9. It maps all ten original features to actual source responsibilities/methods, data/AI/approval flows, patterns, verified evidence and nine real timestamped demonstrations, with F10 upload pending. Seven pattern roles and substantive Stage 1 differences are documented, including raw-source bounded tailoring, explicit memory entry/no forced empty-history interview, SQLite CLI/record representations and command return signatures. Section 6 links all ten actual commits, records zero qualifying commits in each Toronto reporting window and the official single five-point deduction. Required AI usage totals are explicit author-input fields, not invented values. README report/history references were aligned. Checks passed for seven section headings, ten feature subsections, existing relative source targets/methods, all real commit links, nine correct video mappings, stale-claim removal and git diff --check. No application code, diagrams, report PDF, commits or remote publication changed; PDF production and missing evidence remain separate tasks.


## Author-reported cost accounting — November 15, 2026

The author supplied $0.16 Claude usage, very small Jev usage with the balance still displaying $5/$5 available, $0.01 Brave usage and a regular $20/month ChatGPT subscription used for building the project. Report section 5 records Claude as one combined provider total (no per-model split), Brave as reported usage, Jev as an unquantified amount below the dashboard's displayed precision, and the subscription separately from metered application costs. The known metered subtotal is $0.17 excluding Jev. Currency/tax, actual token totals, exact Codex serving variant and project subscription allocation were not supplied; no values are fabricated or inferred. Figures are author-reported, not independently inspected invoices. README accounting status was aligned. No API requests, application code, PDF, commits or publication changed.


## Author-reported request/token totals — November 15, 2026

The author supplied Brave 3 requests; Jev 53 requests and 27,043 total tokens; Claude 26,837 input and 14,053 output tokens. Report section 5 records 40,890 combined Claude tokens and 67,933 combined Claude/Jev runtime tokens, separate from unknown development-session usage. Existing costs remain $0.16 combined Claude, $0.01 Brave, unquantified Jev and $20/month ChatGPT subscription. Claude request/model split, Jev input/output split, development tokens and exact Jev charge were not inferred. README accounting status was aligned; arithmetic and whitespace checks passed. Figures remain author-reported; no provider calls, PDF changes, commit or push performed.

## Implemented UML sources and pictures — November 15, 2026

Reconciled the full class, use-case and F10 sequence sources with the current Java implementation; added a compact runtime class overview. Corrected state callbacks, command/controller return signatures, lazy intake/import, direct versus proposed work, bounded wording edits and termination/trace behavior. Rendered all four diagrams to PNG and SVG using PlantUML 1.2026.10 with headless Java and embedded Smetana (no Graphviz). README and editable report now link the images and sources; original Stage1.md and uml.uxf are preserved. Syntax checks, image/XML validation, visual review, local diagram-link checks and git diff --check passed. No application changes, live API calls, report PDF generation, commits or push occurred. The detailed class diagram requires zoom; the compact overview supplies the readable entry point. PDF/generator refresh remains the next separate task.

## Refreshed implementation report PDF — November 15, 2026

Generated stage2-report.pdf from the current seven-section Markdown report using scripts/report.py (Python standard library plus installed headless Chrome). Replaced the obsolete hand-drawn Java generator with a compatibility launcher. The PDF includes current class/use-case images and two overlapping A3 sequence sheets, clickable repository/video references, actual Git-window findings and author-reported AI usage; missing evidence remains disclosed. PDFKit parsed 25 pages and verified all seven sections and the F10 upload warning. Visually reviewed the cover, class overview and both sequence sheets; renderer self-checks cover tables, links, sequence panels and evidence disclosure. git diff --check passed. No application code, paid API calls, commits, push or external submission. Runtime tests were not repeated for documentation-only work; F10, video explanation/coverage and unreported accounting remain unresolved.

## F10 upload review — November 15, 2026

The playlist now contains ten accessible videos. Retrieved [F10 — 00:00](https://www.youtube.com/watch?v=NZzGRP7kGIw&t=0s) without account cookies/login, downloaded temporary review copies and inspected readable frames: requested gap/checklist execution, Planning/Executing/Finished states and Checklist produced at 00:21; checklist output at 00:29. Audio mean/max is approximately -90.3 dB with no uploaded/automatic captions. Generic filename title and empty description leave feature-ID/purpose/AI explanation unresolved. Approval/rejection, early done and cancellation are not claimed from this clip. README, demo index, F10 checkpoint and current report now link the upload; historical audit entries above describe the earlier nine-video snapshot. No application changes or provider calls.

## Final publication checks — November 15, 2026

Author authorized final cleanup, commit and push, and accepted the existing video title/description. A fresh temporary source snapshot passed all 480 offline assertions without local configuration or live provider calls. Local README/demo/report links, all ten video mappings, whitespace and a scoped high-confidence credential-pattern scan passed. Final PDF regeneration succeeded; PDFKit parsed 25 pages and verified report sections, usage totals, the clickable F10 URL and absence of stale upload warnings. Historical development-token/billing totals are unavailable through the session tools and remain disclosed rather than estimated. Maven is not installed; optional PDFBox/valid legacy Word runtime checks remain skipped. Course-portal submission requires access to the author's authenticated course system, which is not connected here.
