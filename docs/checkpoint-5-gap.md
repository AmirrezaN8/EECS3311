# Checkpoint 5 — F04 Skill-Gap Identification

Scope: Stage1.md F04/UC04. SkillGapService uses Haiku to extract source-supported required skills, normalizes/deduplicates names, asks Jev to classify each against the base resume, and saves a job-linked SkillGapReport. GapGroup priority is MISSING, STRONG, DECENT. Both CLI and Swing use AgentController and Display.

Changes: direct gap requests now reuse the goal workflow's approved missing-job/resume recovery. Display shows all three groups, including empty groups, and an explicit uncertain flag for each assessment, both for immediate reports and saved results. Literal evidence checks use letter/number boundaries so JavaScript alone cannot establish Java evidence or SQLite establish SQL evidence. No schema migration or dependency was added.

Limit: synonym/alias recognition is conservative; absent literal evidence forces MISSING with an uncertainty marker, even if Jev proposes a positive result. Inspect these results manually. Empty required-skill extraction is displayed as three empty groups rather than invented requirements. Actual provider classifications can vary; do not promise fixed STRONG/DECENT placements.

## Live check and recording

Use a separate database and fictional checked-in samples:

```sh
export CAREER_DATA=data/f04-demo.db
java -jar target/career-agent.jar job add "Junior Java Developer" "Example Company" CA @samples/job-description.txt
java -jar target/career-agent.jar resume import samples/resume.md
java -jar target/career-agent.jar gap
java -jar target/career-agent.jar results
java -jar target/career-agent.jar --gui
```

Confirm resume extraction has an empty warning before treating the structured import as successful. The posting requires Java, SQL, Git, JUnit and Docker; the sample resume supplies evidence for the first four, with no Docker evidence. Expect Docker under MISSING, supported skills classified by Jev, groups in the specified order, confidence and explicit uncertainty. `results` in a separate process checks persistence without regenerating the report. In GUI Preparation, click Identify skill gaps and show the same grouped presentation, then Saved results. Outputs may vary with real services.

For the missing-input alternative, use a fresh database path not already containing a selected job/resume, run `gap`, and decline the offer to paste a description. Do not delete existing databases for this demonstration. To finish, `unset CAREER_DATA` in that Terminal. Keep credentials/private data off screen.

Explain: Haiku extracts requirements, Jev assesses resume evidence, conservative validation marks absent literal evidence uncertain, and SQLite saves results for the selected job. Record F04 only after the real provider and GUI checks pass; log actual video links/timestamps in demo-guide.md. No live F04 success is claimed here.
