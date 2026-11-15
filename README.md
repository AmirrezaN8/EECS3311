# Career Preparation AI Agent — EECS3311 Stage 2

**Alignment status:** The implementation restores the planned Jev/Claude boundaries, typed repositories, input strategies, ToolManager and all seven named preparation commands. F03–F09 have focused offline checks; available user-run evidence and remaining live checks are recorded in [verification](docs/verification.md). F10 now supports all seven planner actions plus done, requested/proposed approvals, state progress and saved traces; its [live walkthrough](docs/checkpoint-9-agent.md) remains pending. Packaged parser checks, remaining live branches, video links and the final report/PDF refresh still need completion. This is not yet a submission-ready snapshot.

A Java application for graduates applying in Canada and the US. Swing and an independent CLI share an `AgentController` facade, SQLite memory, preparation services and a bounded, approval-driven AI workflow. The original [Stage1.md](Stage1.md) and [uml.uxf](uml.uxf) are preserved.

## Run locally on macOS

Requires JDK 21+ and `sqlite3` (`java -version`, `sqlite3 --version`). The implementation was compiled with JDK 27 using Java 21 source/API compatibility.

```sh
sh scripts/build.sh
java -jar target/career-agent.jar
java -jar target/career-agent.jar --gui
```

`build.sh` builds the offline JAR and runs the assertion-based verification suite. It does not download dependencies. For PDF/binary Word extraction and JUnit integration, install Maven separately, then:

```sh
mvn test package
java -jar target/career-agent-2.0.0.jar --gui
```

The Maven package includes PDFBox 3.0.8 and Apache POI 6.0.0. Maven/dependency resolution and PDF parsing could not be verified in this sandbox; PDF import in the offline JAR gives an explicit paste-text alternative. DOCX, Markdown and UTF-8 text import work without Maven. Legacy binary `.doc` uses POI in the Maven package and remains unverified with a valid sample; scanned/locked PDFs require pasted text. No Python or JavaScript application code is used.

## Configure real AI

Copy `career.properties.example` to `career.properties`, or export the corresponding environment variables. Java reads the properties file directly; it does **not** implicitly load `.env` files. Environment variables override properties.

Stage 1 uses two separate API providers:

| Role | Integration | Configured model |
|---|---|---|
| Constrained decisions | `DecisionClient → JevAdapter → JevApi` | `jev-latest` |
| Text generation | `LLMClient → ClaudeAdapter → AnthropicApi` | `claude-haiku-4-5-20251001` |
| Harder reasoning | Same Claude integration, `ModelTier.SONNET` | `claude-sonnet-5-5` |

The generic Ollama/OpenAI-compatible adapter has been removed. No provider fallback or fabricated response is used. Model IDs follow the approved Stage 1 choices and the official documentation checked for this checkpoint. Actual access still depends on your provider account.

A local `career.properties` file with empty key fields has been prepared if one did not already exist. Otherwise copy the safe example:

```sh
cp -n career.properties.example career.properties
chmod 600 career.properties
```

Enter `TYPESAFE_API_KEY` from the [TypeSafe console](https://console.typesafe.ai) and `ANTHROPIC_API_KEY` from the [Claude console](https://platform.claude.com) in that **ignored local file**, or set those environment variables. Never paste keys into chat, command arguments, reports or recordings. Provider usage may require credits; no model/account/billing setup is automated. Relevant user documents are sent to the corresponding configured services when you invoke AI features.

After building, explicitly run the nonpersonal live smoke checks (these are not run by the build):

```sh
java -cp target/career-agent.jar career.ApiCheck jev
java -cp target/career-agent.jar career.ApiCheck haiku
java -cp target/career-agent.jar career.ApiCheck sonnet
# Or run all three:
java -cp target/career-agent.jar career.ApiCheck all
```

Successful calls print PASS. Missing keys, invalid responses or failed services print NOT VERIFIED and return a nonzero exit status. A configured but invalid key does not trigger any model substitution. Each request is retried at most once. Do not record/show the local configuration file.

Also configure `ADZUNA_APP_ID` and `ADZUNA_APP_KEY` for F01 job lookup, and `BRAVE_SEARCH_API_KEY` for F06 research. These are planned integrations, not omitted features; those feature paths require real service verification in their respective checkpoints. Pasted job descriptions do not need Adzuna credentials.

Contracts: [Jev Choice](https://docs.typesafe.ai/primitives/choice), [Jev quick start](https://docs.typesafe.ai/introduction/quickstart), [Anthropic Messages](https://platform.claude.com/docs/en/api/messages/create), [Haiku 4.5](https://platform.claude.com/docs/en/models/haiku-4-5/overview), [Sonnet 5.5](https://platform.claude.com/docs/en/models/sonnet-5-5/overview), [Adzuna](https://developer.adzuna.com/overview), [Brave Search](https://api-dashboard.search.brave.com/app/documentation/web-search).

## First workflow

In the interactive CLI, enter:

```text
job add "Junior Developer" "Example Company" CA "Java and SQL required. Apply by: 2026-12-01"
resume paste "Developed Java applications. BSc in Computer Science."
history add target "Junior developer roles in Canada"
gap
tailor
company
interview prep
interview start
interview answer "I implemented a Java project and tested it."
checklist build
checklist show
prepare "Prepare me for this job"
```

Use your actual posting/resume for meaningful results. Quoted arguments can contain spaces. Job/resume text can use `@path` to a UTF-8 file. Shell one-shot commands also work, e.g. `java -jar target/career-agent.jar job show`. `help` lists every operation. CLI approval requires explicitly entering `y`; closing input declines. `interview start` continues saved progress; `interview restart` archives an old attempt. Feedback is withheld during the interview unless partial feedback is explicitly approved. `results` displays saved outputs, `resume versions` the reviewed versions.

GUI: save a posting in **Job**, save/import a resume in **Resume**, then use **Preparation**. **My History** supports view/add/edit/delete. **Mock Interview** handles one typed question at a time. **Checklist** supplies persisted tick boxes and percentage progress. AI/storage work runs in a `SwingWorker`; approval/input dialogs run on the Swing event thread. Stop cancels outstanding work and retains completed saves.

## Verification and limitations

`sh scripts/build.sh`: **325 passing assertions**, including 79 Jev/Claude/response checks, 79 domain/repository checks, 74 F01/F02 checks and 93 core/service/workflow checks. Provider checks intercept HTTP using explicit fixtures; service scenarios use test-only scripted clients. No network connection, live model, or paid request is made by this suite, even when local credentials exist.

Checked: exact API endpoints/authentication/model routing, Jev probabilities/confidence, malformed and incomplete output rejection, missing credentials, separate provider routing, no fallback, one retry, cancellation, structured prompt/response validation, existing core behavior and restart persistence. The old generic-provider HTTP fixture suite was removed with its obsolete adapter. The user subsequently ran `ApiCheck all` successfully in their Terminal for Jev and both planned Claude models; see [the recorded user-run evidence](docs/checkpoint-1-apis.md). Agent-side networking is blocked by DNS restrictions. User-run pasted job intake and real Haiku Markdown/DOCX extraction have now passed (populated sections, empty warning); the user also confirmed basic GUI operation. Other live feature workflows still need verification.

Headless GUI startup exits gracefully; desktop interaction has not been manually exercised. Maven/JUnit/PDFBox, full live AI feature workflows and search integrations remain unverified here. The three provider connectivity checks have user-run PASS evidence.
F01/F02 use their planned named commands and shared tools/strategies. All extracted resume sections are checked against the original text. Model JSON may be plain or inside one complete JSON code block; extra prose/ambiguous output remains invalid. Retry prompts include the validation failure. A raw-only import publishes a warning, not completed. Use the fictional [sample inputs](samples/README.md) and [local walkthrough](docs/checkpoint-3-inputs.md) to exercise real paths. PDF/Word dependency checks print SKIP when unavailable; they are not counted as successful integrations.

Tailoring permits conservative wording changes through a small validated set of word forms and connective words, with unique substring matching, overlap rejection and per-edit approval. Technical names, substantive word counts and numbers must be preserved. GUI review shows original/proposed text side by side. This does not prove semantic equivalence; review every edit. See [F05 verification and live steps](docs/checkpoint-5-tailoring.md). Skill evidence uses literal normalized matches and flags absent evidence uncertain; synonyms may need manual review. Automatic deadlines accept explicitly labeled ISO dates and unambiguous English month dates. Ambiguous numeric dates, conflicting deadlines and unsupported wording remain visible in the posting for manual review. AI-assisted checklist ordering falls back to a useful deterministic order when unavailable.

Data is in ignored `data/career.db` by default (`CAREER_DATA` overrides), with an owner-readable/writable database file, WAL transactions and no Java object deserialization. The four typed repositories retain original resumes, separate job/base-linked versions, memory and checklists. Legacy records remain readable without destructive migration. Entity/selection changes and interview restarts are transactional; restart archives both old answers and feedback. A separate-JVM fixture verifies restart persistence. The application uses `sqlite3` rather than a JDBC driver; repositories enforce the intended single-user entity relationships. Personal data is unencrypted; keep the data folder private and make backups. Do not delete the database while the app is running. API keys are never included in committed examples or HTTP error bodies.

## Submission artifacts

- [Implementation plan](docs/implementation-plan.md)
- [Editable report](docs/stage2-report.md) and [PDF report](stage2-report.pdf)
- [Updated UML](docs/implemented-class-diagram.puml), [use cases](docs/implemented-use-cases.puml), [workflow sequence](docs/implemented-sequence.puml)
- [Feature recording checklist](docs/demo-guide.md)
- [Actual verification log](docs/verification.md) and [commit guide](docs/commit-guide.md)

No demo videos or historical activity have been fabricated. The official Stage 2 PDF has since been reviewed; the earlier report/PDF must be updated after the alignment corrections. All three specified weekly Git windows currently have zero commits. The user subsequently committed the initial milestone as `99ca4c9` and synchronized it with origin/main. F01/F02 checkpoint changes are not yet committed; no agent-side push is claimed.

Regenerate the report after editing its Markdown source:

```sh
java scripts/ReportPdf.java docs/stage2-report.md stage2-report.pdf
```

F06 company research requires Brave search credentials and a real company. Summary sections must cite supplied search-result URLs or say `not stated`; citation validation does not prove factual entailment. See [company research verification](docs/checkpoint-6-company.md) for the live walkthrough and evidence limits.

See [F09 checklist verification](docs/checkpoint-8-checklist.md) for first-show creation, approved missing-work refresh, saved progress and deadline parsing.
