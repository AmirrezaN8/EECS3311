# Checkpoint 8 — F09 Preparation Checklist

Stage1.md F09/UC09: ChecklistAction participates in the shared command map. ChecklistService reads resume version status, explicit posting deadline, skill assessments, interview questions and company research. Sonnet orders unchanged labels; validation rejects added/removed labels, and existing deterministic priority order is retained if model ordering fails. Thread cancellation is propagated. Partial inputs yield useful missing-work tasks rather than invented outputs.

The priority sequence is resume match/review, explicit deadline, skills, interview preparation and company. The facade offers missing tailoring, gap, questions and company work in a stable order with approval. Declining preserves the partial checklist; approved successful work triggers a rebuild before returning, so labels reflect the new results. `checklist show` creates the first checklist if none exists, then later show operations simply retrieve it without repeating offers.

Checklist identity, IDs and ticks remain stable for unchanged labels on rebuild. Tick/untick and integer percentage persist. GUI uses the existing checkboxes/progress bar; shared textual output now includes progress, including nested saved results. A task whose label changes after preparation changes is a new task and does not inherit an unrelated old completion tick.

Posting intake now recognizes explicitly labeled ISO dates and unambiguous English month dates (`December 1, 2026`, `1 Dec 2026`, etc.) using strict calendar parsing. Dates without a deadline/apply-by/closing-date label, invalid dates, ambiguous numeric dates and conflicting distinct deadlines are not inferred. Existing stored jobs are not silently changed; reimport a posting to apply the improved date parser. Estimate-source deadlines remain excluded by the existing source policy.

Offline build passes 464 assertions. New checks include date forms/invalid/ambiguous/conflicting labels, deadline task and priority, partial coverage, Sonnet tier, visible progress, tick/untick/restart, identity and tick retention on rebuild, invalid-ID preservation, first show creation, no repeated offers on saved show, and returned/persisted refresh after approved research. Real F09 ordering and desktop checks remain pending evidence.

## Live check and recording

Use the existing F04 sample database, which contains an actual pasted fictional posting with a deadline, resume, skill analysis and accepted tailored version:

```sh
export CAREER_DATA=data/f04-demo.db
java -jar target/career-agent.jar checklist build
```

Enter `n` for missing-feature offers during this short partial-checklist demonstration. No additional work runs without approval. Expect resume review, the saved posting's 2026-12-01 deadline, practice items from the gap analysis, interview preparation and company tasks, with progress initially reflecting any earlier saved ticks. Model ordering must be inspected; no exact wording/order beyond the requested priorities is promised.

```sh
java -jar target/career-agent.jar checklist show
java -jar target/career-agent.jar --gui
```

In GUI Checklist:
1. Click Show saved checklist. Confirm the same tasks and percentage.
2. Tick one item and show increased progress; untick it and show decreased progress.
3. Tick it again, close/reopen the app, and Show saved checklist. Confirm the tick and percentage persisted.
4. Click Build / refresh checklist, decline missing-feature offers, and confirm ticks remain on unchanged tasks.

Optional full-input alternative: approve a missing feature, complete any necessary input/consent dialogs, then confirm the returned checklist changes the completed preparation label to review/practice rather than leaving a stale missing-work task. Do not claim live coverage of this path unless actually performed. A partial checklist is an intended outcome, not a failed full-input demonstration.

CLI `checklist tick ITEM_ID true|false` also persists ticks, but GUI is easier for recording and avoids copying IDs. Keep API keys/private data off screen. Finish with `unset CAREER_DATA` if returning to normal data.

## Demo and commit

Title: F09 — Persistent Preparation Checklist.
Description: Builds a prioritized checklist from available job preparation, includes an explicit posting deadline, offers missing features with approval, and preserves completion progress across restarts.

Record partial checklist build/declined offers, tasks/deadline, ticking and unticking, restart persistence and rebuild preservation. Explain Sonnet's prioritization role and SQLite storage. Add real accessible video links/timestamps to demo-guide.md. No live success claimed here.

Commit after the walkthrough: `feat: complete prioritized checklists with persistent progress`. F08 is already committed as 642b229. No F09 commit/push made here. Report PDF refresh remains pending submission preparation.
