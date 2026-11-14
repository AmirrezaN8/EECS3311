# Official Stage 2 / Stage 1 alignment review

Reviewed November 14, 2026 against all five pages of `/Users/amir/Downloads/Project-Stage2-Instruction (3).pdf`, Stage1.md, README.md and the current implementation. This is an audit, not a claim that the findings are fixed.

The user's controlling instruction is now strict fidelity to the approved Stage 1 plan. Treat architectural substitutions as defects to correct, rather than relying on the professor's general permission to document changes.

## Checkpoint 2 update

Named top-level domain classes and the four typed repositories are now restored and in actual service/facade use. Compatibility decoding preserves older saved records; transactional selection/restart and entity links have offline checks. The suite now passes 233 assertions. Existing string IDs and sqlite3 transport remain representation choices documented in checkpoint-2-persistence.md; no JDBC Connection implementation is claimed. The original deviation table below is historical; command/tool/state/F05/F10/UI corrections remain open.

## Checkpoint 1 update

Jev/Claude API boundaries, planned model IDs, Decision probabilities and HAIKU/SONNET roles have now been restored. Generic-provider integration was removed. Offline contract/regression checks pass; real API checks report missing local keys. The deviation table below records the original audit, not a claim that all rows remain unchanged. Non-provider corrections remain open. See checkpoint-1-apis.md.

## What the official PDF actually requires

- Page 1, sections 1–2: implement the Stage 1 design with traceable features/classes/interactions/patterns; GUI, CLI, at least ten meaningful features, five distinct meaningful course patterns, one real LLM, meaningful agent behavior. It says cheap/free models may be used. It does NOT explicitly require cloud hosting or prohibit local inference.
- Page 2, section 3: documented feature/pattern changes are permitted by the assignment. The user's stricter preference overrides taking advantage of that permission.
- Pages 2–3: five meaningful implementation/test commits in EACH specified period, Toronto dates. Report-only commits do not count. A single five-point deduction applies if one or more periods fail.
- Pages 3–4: seven report sections, honest verification, updated UML and video mapping for every feature. Stage 3 testing/KUMA is not a Stage 2 requirement.
- Pages 4–5: actual running GUI and CLI in recordings, accessible timestamped links, secret protection, reproducible prerequisites/config/build/sample inputs, code and report pushed to the same public repo.
- Pages 1 and 5: November 10, 2026 deadline. Current work is November 14. Late-submission acceptance/penalties are not specified here; do not infer them from the separate Git penalty.
- The general Project Instructions and Stage 1 Instructions are also authoritative (page 1); those official instruction documents have not been supplied, so this audit cannot certify requirements unique to them.

## Deviations from the approved Stage 1 plan

| Area | Stage 1 baseline | Current implementation | Required correction for strict fidelity |
|---|---|---|---|
| AI providers | Jev via DecisionClient/JevAdapter/JevApi; Claude via LLMClient/ClaudeAdapter/AnthropicApi | Ai implements decisions through the same configurable LLM; default Ollama; no Jev/Claude-specific adapters | Restore separate genuine Jev and Anthropic integrations; no substitution for missing credentials |
| Model roles | Haiku 4.5 generation; Sonnet 5.5 reasoning; Jev constrained decisions | Generic FAST/REASONING tiers; decisions routed to FAST LLM | Preserve roles and verify actual provider model IDs. If a planned version is unavailable, flag that specific blocker rather than invent an ID or silently substitute |
| Command classes | Seven named actions including intake/import | One FeatureCommand for five operations; intake/import directly dispatched | Restore the named Stage 1 command participants and route actual feature execution through them |
| Tools | ToolManager, Tool, ToolResult with Adzuna and web search | Direct typed tool calls | Restore the designed tool coordination and actual tool registrations |
| Persistence | Repository plus SqliteJob/Resume/Memory/ChecklistRepository | One SQLite JSON-document store driven by sqlite3 subprocess | Restore typed repository responsibilities and planned relationships; maintain saved-data compatibility/migration if schema changes |
| Domain | JobDescription, Skill, planned report/version entities | Job record, string skills, consolidated nested records | Align important named domain types/interfaces with UML and traceability |
| State | AgentState.onInput/onApproval, session.handleInput/handleApproval | advance-only state loop with synchronous approval | Restore planned input/approval state responsibilities without freezing GUI |
| F05 tailoring | Actual wording rewrite, deterministic unsupported-skill validator, side-by-side review | Only original-token permutations; stacked approval text | Implement real supported rewording with skill/evidence validation and side-by-side review; current permutation-only behavior is insufficiently faithful |
| F10 planner | Chooses description/import/gap/tailor/company/questions/checklist/done; explicitly requested steps run directly | Description/import occur outside planner; only five planner actions; checklist forced last; no done choice; all planner steps require approval | Restore fixed action set, done behavior, and distinction between requested and proposed steps |
| Missing-input handling | Gap/tailoring offer to obtain missing resume/posting | Individual features generally throw an instruction; prepare has a separate input path | Route approved missing-input recovery through the same feature workflow |
| F03 chat/history | Useful facts considered after each chat turn | Explicit chat and goal messages only; GUI has goal/fact fields rather than a general chat flow | Restore the intended chat entry and turn-memory collaboration |
| F09 deadlines | Include deadline if stated in posting | Only explicitly labeled YYYY-MM-DD dates recognized | Support the promised posting deadline scope without guessing unstated dates |
| F02 file import | PDF, Word, Markdown, pasted text | DOCX/text tested; PDFBox code present but unverified; binary .doc rejected | Verify Maven/PDF path and resolve intended Word scope against Stage 1; do not count untested parsing as verified |

Java, Swing/CLI with a shared facade, ten connected feature paths, approval hooks, SQLite persistence and seven pattern concepts broadly align. This is useful existing work, but does not establish exact Stage 1 fidelity.

## Submission gaps independent of architectural alignment

1. Live model/Jev/Anthropic/Adzuna/search behavior has not been verified. Offline fixtures cannot establish successful API integration.
2. Desktop GUI walkthrough, Maven packaging and PDF import remain unverified.
3. No demonstration videos exist; placeholders do not meet the final requirement.
4. Source changes are uncommitted/unpushed in the inspected local checkout. Public remote contents/accessibility have not been reverified in this audit.
5. Only two October 5 commits are present locally; zero in the three specified windows. New commits cannot repair those periods. The earlier six-new-commit estimate was a workflow estimate, not an assignment requirement; report-only commits are not meaningful code commits under section 4.
6. The existing report/PDF were written before this official-PDF review and contain superseded recommendations/absence-of-PDF statements. They must be regenerated after strict alignment is implemented and genuinely verified.
7. Model availability and Jev's actual request/response API must be checked against official provider documentation before implementing adapters. This audit makes no claim that the named Sonnet version is available.

## Correction sequence

First restore the Stage 1 API boundaries and provider roles; then restore named commands/tools/repositories/domain/state relationships; then fix F05/F10 and remaining feature deviations; run regression checks; configure credentials locally; verify live paths and desktop GUI; update UML/README/report; record real demos; make honest current-date commits and obtain approval before pushing. Do not commit API keys or backdate history. No application code was changed during this review.
