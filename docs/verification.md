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
