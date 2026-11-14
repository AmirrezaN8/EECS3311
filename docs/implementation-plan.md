# Stage 2 implementation plan

Historical initial plan: provider/consolidation changes below were superseded by strict Stage 1 fidelity. Use stage1-delivery-checkpoints.md and checkpoint-1-apis.md for current work.

Baseline: Stage1.md read in full; all panel attributes of uml.uxf extracted and inspected.
The UML file contains class diagrams; the claimed UC/SD diagrams are not actually present as UMLet use-case/sequence elements. Preserve both originals.
No Stage 2 PDF was supplied. The user's supplied instructions define the Stage 2 checklist pending PDF comparison.

Users: new graduates applying in Canada/US. Both Swing and CLI call AgentController.

| Requirement | Implementation / participants | Patterns | Verification |
|---|---|---|---|
| F01 / UC01 | JobDescriptionIntake, JobDescriptionSource strategies, AdzunaTool, ApprovalGate, Job | Strategy, Command, Facade | pasted intake, labeled estimate approval/rejection, local HTTP fixture |
| F02 / UC02 | ResumeImporter, ResumeParserFactory, PDF/DOCX/Markdown/text parsers, ResumeProfile | Factory, Strategy, Adapter | file fixtures, malformed model fallback, restart persistence |
| F03 / UC03 | MemoryManager, MemoryFact, SqliteStore | Facade, Adapter | dedup/edit/delete/conflict approval |
| F04 / UC04 | SkillGapService, SkillAssessment | Command, Facade | evidence validation, ordered groups, uncertainty |
| F05 / UC05 | ResumeTailorService, ResumeChange, ResumeVersion | Command | deterministic evidence boundary, per-change approval, original preserved |
| F06 / UC06 | CompanyResearchService, WebSearchTool, sourced summary | Command, Adapter | source filtering/no-result/retry |
| F07 / UC07 | InterviewPrepService, InterviewQuestion | Command, Adapter | exact count, malformed retry, saved questions |
| F08 / UC08 | MockInterviewService, InterviewSession, FeedbackReport | Facade, Adapter | one follow-up maximum, resume, feedback only at end, partial feedback |
| F09 / UC09 | ChecklistService, ChecklistItem | Command | partial checklist, explicit deadline, progress, persisted ticks |
| F10 / UC10 | Planner, AgentAction, AgentSession states, AgentEventPublisher, ApprovalGate | all workflow patterns | bounded plan, approvals, failures, stop, trace |

Architecture changes and reasons:
- Swing retained as originally designed (rather than adding JavaFX).
- Ollama local LLM and optional OpenAI-compatible hosted endpoint replace speculative Jev/Claude versions. One adapter supplies validated constrained decisions through a separate DecisionClient; no simulated AI fallback.
- Java 21 source compatibility; installed JDK 27 used for compilation.
- Maven unavailable and Maven Central DNS blocked. Provide pom.xml plus dependency-free javac build/check. Native macOS sqlite3 is invoked through a Java persistence adapter instead of unavailable JDBC driver; data remains SQLite, with JSON document records and transactions.
- PDFBox is an optional Maven runtime dependency for PDF extraction; DOCX uses JDK ZIP/XML with external entities disabled. Dependency-free build reports missing PDFBox clearly and offers pasted text. Legacy binary .doc is explicitly unsupported.
- Consolidate typed domain records in Models and SQLite repositories in one store rather than four identical CRUD wrappers. Keep meaningful service and pattern roles.
- Use conservative extractive resume tailoring: proposed revised text may only rearrange existing words/numbers. This provides a deterministic anti-fabrication guarantee at the cost of rejecting more ambitious rewrites.

Reach: Java domain, persistence, AI/network adapters, services, shared facade, commands/state/events, CLI, Swing, configuration, automated self-checks, Maven/JUnit entry, build scripts, feature guide, report source/PDF and updated diagrams.
Trust boundaries: model output schema/evidence, file sizes and XML parsing, SQL encoding, source labels, credentials, approvals, HTTP deadlines, saved user data. No overwrite of Stage 1 or original resumes. No push without approval.

Commit plan (current dates only): plan/build foundation; core services; agent and interfaces; verification fixes; report/submission artifacts. Git writes may be restricted by the session; report actual results only.
