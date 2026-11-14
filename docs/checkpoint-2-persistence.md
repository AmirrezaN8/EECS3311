# Checkpoint 2 — domain entities and typed SQLite repositories

Status: IMPLEMENTED AND OFFLINE-VERIFIED. This checkpoint restores domain/storage responsibilities from Stage1.md and uml.uxf; it does not claim completion of later feature/UI corrections.

## What changed

Domain entities are now named top-level Java records rather than nested members of Models. `Job` is restored to `JobDescription`; `Skill` represents name/level and `ResumeProfile.getSkills()` returns typed skills. The other planned entities are ResumeVersion, ResumeChange, MemoryFact, SkillGapReport, SkillAssessment, GapGroup, CompanySummary, InterviewQuestion, FeedbackReport, Checklist, ChecklistItem and SourceLabel. InterviewSession, InterviewQuestionSet and SearchResult support saved progress and tool results. Models now holds only shared input/identity utilities.

New/updated fields participate in real behavior: memory edit timestamps, version/report creation timestamps, checklist identity/deadline, skill names/levels, and job-linked question answers. Import/extraction does not invent proficiency: a skill without a supplied level is UNSPECIFIED. New timestamps use the actual current time; old records without timestamps retain null (unknown), not fabricated historical dates.

| Planned repository | Actual responsibilities and participating methods |
|---|---|
| Repository<T> | save(entity), findById(id), findAll(), delete(id); missing IDs return null |
| SqliteJobRepository | Job CRUD, current/select/saveAndSelect; save/find skill gap, sourced company summary, questions, interview and feedback; consolidated results and traces; validate job links; archive interview/feedback and restart atomically |
| SqliteResumeRepository | Immutable base profile save/current/saveAndSelect; evidence-supported typed skills; save/find separately job/base-linked versions; reject orphan versions or deletion of a base used by versions |
| SqliteMemoryRepository | Fact CRUD, normalized duplicate detection, findByText; actual MemoryManager calls these methods |
| SqliteChecklistRepository | Checklist CRUD, job/deadline validation, stable checklist IDs, tick/untick/progress persistence |

All feature services and AgentController now use these repository responsibilities. No feature service or UI directly reads/writes generic storage documents. Documents remains an explicit record codec; SqliteStore remains the shared low-level SQLite transaction/transport layer rather than replacing the four repositories. Runtime SQLite access still uses the installed sqlite3 executable; this checkpoint does not claim a JDBC Connection implementation. Existing stored identity strings are retained for compatibility with existing records and CLI references; dates use Java Instant/LocalDate rather than the UML's illustrative Date types. These representation choices do not remove domain relationships or feature responsibilities and must remain accurately described in the final implementation report.

## Existing data compatibility

No schema reset, production database rewrite or personal-data inspection was performed. Existing document kinds and identifiers remain valid. Compatibility conversion occurs in memory while decoding:

- Old job `source`/`attribution` -> sourceLabel/adzunaUrl; old blank date -> absent deadline.
- String skill names -> Skill(name, UNSPECIFIED).
- Missing creation/update timestamps -> null (unknown).
- Old assessment `skill` -> skillName; old feedback `perAnswer` -> perAnswerFeedback.
- Old checklist without an entity ID -> stable existing job ID; ticks retained.
- Old questions without their own job link inherit the saved aggregate's job; answers are restored from saved progress maps.

Reads do not rewrite old records. Subsequent explicit edits save the new typed shape. Corrupt records fail visibly and remain untouched rather than being overwritten with empty defaults. Relationship validation is performed in the repositories; SQLite transactions make multi-record selections/restarts atomic. This is a local single-user design, not a claim of database-level foreign-key enforcement against arbitrary external SQLite writers.

## Data-preservation rules

Base resume contents and existing versions cannot be overwritten through their save methods. Jobs with preparation work and base resumes used by saved versions cannot be deleted, preventing orphans. Invalid selections cannot replace valid selected records. Imported profile + selected-pointer saves are one transaction. Interview restart archives the prior session and its feedback, creates the new attempt and clears current feedback in one transaction, preventing old feedback from appearing to describe a new attempt.

## Executed checks

`sh scripts/build.sh`: PASS, 233 assertions total (61 API contract fixtures + 79 domain/repository assertions + 93 existing core/service checks). The DOCTYPE diagnostic comes from the intentional hostile DOCX fixture.

Repository checks use disposable temporary SQLite databases only:
- CRUD and selected-record behavior for the four repositories; immutable base/versions; normalized fact deduplication and edit dates.
- Required job/base links; supported checklist deadlines; source evidence requirements; question job identity; interview answer/follow-up links; feedback count.
- Checklist stable identity, tick/untick, progress and duplicate-item rejection.
- Backward-compatible fixtures for jobs, profiles, facts, gaps, versions, questions, interviews, feedback and checklists; snapshots confirm unchanged stored documents after reads.
- SQLite trigger-injected failures prove transaction rollback for job/profile selection and interview/feedback archive/restart.
- A separate JVM reopened the fixture database and checked selected job, typed skills, base/version relationship, memory, checklist ticks and typed interview answers.
- Production compilation with `javac --release 21 -Xlint:all` passed without warnings before documentation finalization; original Stage1.md/uml.uxf hashes match the baseline.

Separate real CLI launches against a temporary database also passed: `job add` followed by `job show` returned the same saved JobDescription/source/deadline; `history add` followed by `history show` retained the fact and its actual edit timestamp. The temporary database was removed afterward.

No paid API call was required or made by these tests. Checkpoint 1's live provider evidence remains the user's reported successful Jev/Haiku/Sonnet checks. Maven/JUnit/PDFBox and desktop UI interaction remain unverified in this environment.

## Commit / recording checkpoint

This is a coherent code milestone. Because the initial source tree has not yet been committed, the first implementation commit needs all required Java source/build/test dependencies, including checkpoints 1 and 2. Follow docs/commit-guide.md; preserve current dates, review staged filenames and do not push without approval. career.properties and docs/user-decisions.md are ignored and must stay out of the commit.

No F01–F10 feature recording is requested yet: a persistence test is supporting verification, not a substitute for the actual GUI/CLI feature demonstration. Next implementation checkpoint is F01/F02, including named commands/tools and the first fully verified user-facing vertical slice. F05 rewriting, full F10 planner/state behavior and remaining interface work remain open as previously audited.
