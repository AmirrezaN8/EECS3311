# Checkpoint 3 — F01 job intake and F02 resume import

Status: IMPLEMENTED AND OFFLINE-VERIFIED; live feature walkthrough and dependency-packaged file checks still pending. The original Stage 1 files remain unchanged.

## Restored participants and actual execution

F01: GUI/CLI -> AgentController.runFeature -> GetJobDescriptionAction.execute -> JobDescriptionIntake.getDescription -> JobDescriptionSource strategy -> SqliteJobRepository.saveAndSelect. PastedTextSource, AdzunaExcerptSource, ExpandedEstimateSource and FullEstimateSource are named top-level classes implementing the shared source contract. fetch(request) now returns the labeled JobDescription, not an unlabeled string. A shared JobDescriptionRequest carries title/company/country/text/source URL through these strategies.

Lookup: ToolManager registers AdzunaTool and WebSearchTool as real Tool implementations. AdzunaExcerptSource executes the registered Adzuna tool; Jev chooses a fixed listing index. A partial excerpt is shown with attribution. Only approval permits Sonnet expansion/full estimation. Declining expansion can retain an approved partial excerpt. Exhausted model retries preserve the prior selected job and either offer the original partial excerpt or explain the paste alternative. Database-save failures are not mislabeled as model-generation failures. needsApproval() participates in the approval gate.

Adzuna uses the documented JSON Accept header, encoded query parameters, CA/US endpoints and one retry. The source URL is retained in adzunaUrl; the approval text identifies 'Jobs by Adzuna'. API credentials are never printed. SearchResult remains the existing title/URL/snippet result type; requested title/company metadata is retained in the job and the selected excerpt/source remain visibly partial.

F02: GUI/CLI -> ImportResumeAction.execute -> ResumeImporter.importResume/importText -> ResumeParserFactory.createParser -> format-specific ResumeParser.extractText(String source) -> Haiku structure/validation -> SqliteResumeRepository.saveAndSelect. Pasted text now implements the SAME parser interface as file parsers. The parser interprets the source as literal text for pasted input and a path string for file input.

Named parsers: PastedTextResumeParser, MarkdownResumeParser (also UTF-8 text), DocxResumeParser, PdfResumeParser. DocResumeParser additionally covers Word 97-2003 binary input through Apache POI; it is a supporting strategy for the originally promised Word input, not a replacement feature. PDFBox/POI are runtime Maven dependencies; reflective invocation preserves the dependency-free build. Missing dependencies produce an explicit paste alternative, never fake extraction.

Haiku outputs all five sections. Each returned skill/education/experience/project/certification fragment must occur in the normalized source text; unsupported invented qualifications fail validation. Malformed/unsupported model output retries once, then retains raw text with a visible warning as Stage 1 specifies. Invalid/corrupt/missing/oversized file input and cancellation do not replace the selected profile. Structured extraction uses HAIKU only; estimate generation uses SONNET only; listing selection uses Jev.

ToolManager also routes the existing company-research search call so the tool architecture is actually shared. Its feature-specific redesign/verification remains checkpoint 6. Other named commands, full agent state/input/approval semantics and the full action list remain later checkpoints.

## Build and dependency choices

`sh scripts/build.sh` builds the JDK-only app and runs the offline suite. `mvn test package` builds the dependency-inclusive package for PDF and binary Word import. Maven is unavailable and network downloads are blocked in this agent environment; neither a successful Maven package nor PDFBox/POI runtime extraction is claimed here.

The dependency versions in pom.xml were checked against the official Apache release pages: PDFBox 3.0.8 and POI 6.0.0. PDFBox uses Loader.loadPDF and PDFTextStripper; Word 97-2003 uses WordExtractor(InputStream). JDK ZIP/XML handles DOCX with external entities/DOCTYPE disabled and a decompressed XML size limit. Maven Shade excludes dependency signature files and does not generate a dependency-reduced POM in the source folder.

Official references checked:
- https://developer.adzuna.com/overview
- https://developer.adzuna.com/docs/search
- https://pdfbox.apache.org/3.0/migration.html
- https://pdfbox.apache.org/download.html
- https://poi.apache.org/components/document/quick-guide.html
- https://poi.apache.org/download.html

## Executed verification

`sh scripts/build.sh`: PASS, 301 assertions (61 API boundaries + 79 domain/repositories + 68 F01/F02 + 93 existing core/service checks). Provider/tool outputs are explicit test fixtures. No inference or external service is called by this suite.

New checks include: registered tool execution, duplicate/unknown tools, transparent failures, exact JSON negotiation, country/query encoding, Adzuna retry, no HTTP without keys, cancellation, deterministic pasted intake, Jev listing choice, attribution/partial labels, approved/rejected expanded/full estimates, malformed generation retry and approved excerpt recovery, unchanged selection on rejection/failure, storage failure distinguished from model failure, named command execution, all structured resume sections, no inferred proficiency, source-evidence validation, profile retention, bad UTF-8/ZIP/XML, missing/directory/oversized input, raw fallback and cancellation.

The checked-in sample DOCX and Markdown were also parsed directly and produced matching fictional resume text. Separate actual CLI processes ran the named job command with the sample posting and reloaded identical saved content, USER_PASTED source and 2026-12-01 deadline; the temporary database was then removed. Headless GUI startup was checked; this is not a desktop interaction test.

The suite prints explicit SKIP notices for real PDF extraction/locked/scanned fixtures when PDFBox is unavailable, and for valid binary Word extraction without its dependency/sample. These are NOT included as successful parser-integration claims. With Maven dependencies, the same suite exercises a generated text PDF, no-readable-text PDF, corrupt PDF and protected PDF. A valid legacy .doc still needs a non-sensitive real sample walkthrough.

## User-run walkthrough and extraction defect

The user subsequently reported:
- local build PASS with 301 assertions;
- actual F01 pasted sample command succeeded with USER_PASTED source and 2026-12-01 deadline;
- actual F02 Markdown import retained raw text but failed AI extraction after two attempts with `Invalid JSON number`; all structured sections were empty. This is NOT a successful live F02 demonstration.

The same parser error was reproduced using valid JSON wrapped in a Markdown code block. The original model response was not captured, so a code fence is a plausible, not confirmed, explanation for that particular live failure.

Fix: ResponseParser.validate accepts a plain object or one complete JSON code block while wire JSON parsing, full JSON syntax, schema and source-evidence checks stay strict. Surrounding prose, multiple blocks, incomplete/invalid JSON and wrong schemas are rejected. Retry prompts now include the actual validation error. Unknown JSON tokens report a value/position error rather than incorrectly labeling every non-JSON prefix a malformed number. Raw-fallback imports publish a warning event instead of completed.

After this fix, `sh scripts/build.sh` passes 325 assertions (79 API/response checks + 79 repositories + 74 F01/F02 + 93 previous service checks). Regression tests exercise the real adapter/importer/facade/repository path with a fenced model-response fixture and verify persisted structure, failure warning, bounded retry and preservation of prior profiles. Actual user-side F02 rerun remains pending; Adzuna/Maven/PDF/legacy Word/desktop checks also remain pending.

## Successful user-run F02 rerun

After rebuilding, the user supplied PASS output for all 325 offline assertions and actual `resume import samples/resume.md` output from their Terminal. The selected profile contained Java, SQL, Git and JUnit (levels UNSPECIFIED), BSc in Computer Science, both source-supported experience entries, the career application project and Java course certificate, with an empty warning. This passes the live Markdown extraction path based on user-provided evidence. No exact raw Claude response was captured, so the earlier failure's precise response format remains unconfirmed despite successful regression handling and rerun.

Together with the prior pasted job result, F01 pasted intake and F02 Markdown extraction now have user-run success evidence. DOCX live extraction, PDF/legacy Word dependency/runtime checks, Adzuna lookup/estimate branches and desktop GUI checks remain pending. No new checkpoint-3 commit, push or video is claimed.


## User-run DOCX and GUI confirmation

The user supplied successful real `resume import samples/resume.docx` output: Java/SQL/Git/JUnit, BSc education, both experience items, the project and certification, with an empty warning. This passes the DOCX/Haiku extraction path based on user-provided Terminal output. The user also stated "gui worked"; record that as a basic user-reported desktop GUI check, not exhaustive testing of every screen/error/cancellation path. No video URL/timestamp has been supplied.

Verified recording scope now includes F01 pasted intake and F02 Markdown/DOCX import, using CLI and GUI where demonstrated. Adzuna lookup, approved estimate branches, PDF/legacy Word runtime checks and all-screen GUI acceptance remain open. Record only what is actually exercised; the remaining F01/F02 branches still require evidence before claiming the whole feature fully verified.

## Reproduce and continue the local live walkthrough

Use a separate demo database so these inputs do not replace your selected real job/profile:

```sh
sh scripts/build.sh
export CAREER_DATA=data/demo.db
java -jar target/career-agent.jar job add "Junior Java Developer" "Example Company" CA @samples/job-description.txt
java -jar target/career-agent.jar job show
java -jar target/career-agent.jar resume import samples/resume.md
java -jar target/career-agent.jar resume show
java -jar target/career-agent.jar resume import samples/resume.docx
```

Expected F01: USER_PASTED source, sample posting, explicit 2026-12-01 deadline. Expected F02: nonempty source-supported skills/sections and empty warning. Raw-text fallback is useful error handling but is NOT proof of successful live structured extraction. Output varies with the real model; no exact AI answer is promised.

For live F01 lookup, enter ADZUNA_APP_ID/ADZUNA_APP_KEY only in ignored career.properties. Use an actual employer/title, for example replacing the values in:

```sh
java -jar target/career-agent.jar job find "Software Developer" "ACTUAL EMPLOYER" CA
```

A real lookup may return no listings. Verify the labeled partial excerpt/source URL when listings exist; decline expansion once and accept the excerpt, then separately approve an estimate to exercise Sonnet. A missing-key/no-results case demonstrates an alternative path, not successful live search. Never show local configuration or keys on screen.

For the packaged file paths:

```sh
mvn test package
java -jar target/career-agent-2.0.0.jar resume import samples/resume.pdf
java -jar target/career-agent-2.0.0.jar --gui
```

For GUI, use Job to save a pasted posting or Find via Adzuna, and Resume to paste/import files. Check actual responsiveness and errors. When finished using the demo database, run `unset CAREER_DATA` in that Terminal to return to your normal configuration.

## Recording and commit gate

Do not call these features live-verified until the above actual runs pass. Once they do, record F01 and F02 using docs/demo-guide.md, with source labels, inputs/outputs, AI roles and an error/alternative path. These can be two segments in one video; keep credentials/private data off screen. If the GUI later changes materially, rerecord the affected segment.

No video or new commit was created here. The preceding implementation commit 99ca4c9 is present and the local branch was synchronized with origin/main when this checkpoint began; no new push was performed. After local feature checks, this coherent change can use `feat: restore Stage 1 job intake and resume import workflows`. Do not claim Maven/PDF/Word/live lookup/desktop checks passed until actual evidence exists.
