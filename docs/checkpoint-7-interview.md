# Checkpoint 7 — F08 Mock Interview and Feedback

Stage1.md F08/UC08: AgentController starts or resumes a saved interview; MockInterviewService shows one saved question at a time. Each submitted base answer is persisted before Jev decides followup/next. At most one Haiku follow-up is stored per base question. Follow-up answers and decision progress persist. No per-answer feedback is generated during normal progression.

The facade now generates and returns Sonnet feedback automatically when the final answer/follow-up completes the session. Feedback count must match answered base questions; malformed output retries once and cannot overwrite a valid previous report. The saved feedback remains available even if adding weaknesses to history fails: MemoryManager publishes a memory-warning for those failures. Base answers and the report are preserved independently.

Stop and save retains progress and offers partial feedback with separate approval. Declining leaves no new report and allows continuation. Accepting evaluates only answered base questions, without treating unattempted questions as failures, and retains progress. New attempt archives previous answers/report before resetting. If a decision service fails after an answer is saved, Start / continue resolves the pending decision instead of asking to resubmit that base answer.

Offline build passes 443 assertions. F08 checks cover full interview/follow-up limit, blank answer rejection, no premature feedback, new-controller resume, declined/approved partial evaluation, automatic final report, actual SQLite weakness-save failure, invalid feedback count preserving prior report, restart archive and pending-decision recovery. Fixtures establish behavior, not real model feedback quality or GUI interaction.

## Live check and demo

Use the same database as F07, which already has prepared questions:

```sh
export CAREER_DATA=data/f06-demo.db
java -jar target/career-agent.jar --gui
```

In Mock Interview:
1. Click Start / continue. Confirm one question is displayed. If an old completed attempt exists, use New attempt and approve it.
2. Type an answer matching the displayed question, using only the fictional sample resume evidence, then Submit answer. Replace the answer-field text for each submission.
3. If a follow-up appears, answer it. Expect at most one follow-up for each base question. Do not assume a follow-up will be generated for every answer.
4. Show that progression presents the next question rather than coaching/feedback.
5. Click Stop and save and decline partial feedback. Close/reopen the app, then Start / continue; confirm the unanswered question or pending follow-up resumes.
6. Finish the remaining prepared questions/follow-ups. On completion, the final report appears automatically. Inspect per-answer feedback, summary and weaknesses.
7. In Preparation, click Saved results to show the retained interview and report. In My History, click Show history to inspect any saved weaknesses.

Do not force identical answers to every question. Use short fictional answers tailored to what is asked, such as Java development, JUnit tests, Git collaboration or the Java/SQL project. A statement that no further experience is supplied is preferable to inventing resume accomplishments. Keep API keys/private history off screen.

For early-feedback alternative: start a New attempt (archives completed work), answer one base question and any offered follow-up, then Stop and save and approve partial feedback. Confirm the report evaluates only attempted base questions and the answered progress remains saved. Existing feedback from an earlier attempt is archived on New attempt; do not present it as freshly generated output.

A provider error does not mean the saved answer was lost. Use Start / continue after a follow-up-decision error. If automatic final feedback fails, answers/completion remain saved; retry with Final feedback or CLI `interview feedback`.

CLI equivalents: `interview start`, `interview answer "ANSWER"`, `interview stop`, `interview feedback`, `interview restart`, `results`, `history show`. These are independent invocations of `java -jar target/career-agent.jar`.

## Recording and commit

Title: F08 — AI Mock Interview and Final Feedback.
Description: Demonstrates a typed interview with saved questions, optional AI follow-ups, pause/resume, final feedback after completion and saved weaknesses. Include approved early feedback only when actually demonstrated.

Record the main flow and at least the pause/decline/resume alternative; optionally capture early approved feedback in a second segment. Add real accessible video URLs/timestamps to demo-guide.md. Real F08 decisions, follow-ups, feedback and GUI paths remain pending evidence.

Commit after the live check: `feat: complete mock interviews with automatic final feedback`. F07 is already committed as e5bc049. No F08 commit/push made here. The report PDF remains pending the submission refresh.

## User-run confirmation

The user reported that the F08 walkthrough works. This is basic user-reported live success; no detailed transcript, screenshot or video URL was supplied to verify each follow-up, pause/resume, partial-feedback or GUI branch individually. Offline checks remain the evidence for those specific branches unless separately demonstrated.
