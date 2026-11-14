# Verification record — November 14, 2026 (Toronto)

## Current: domain and repository checkpoint

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
