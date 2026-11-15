# Checkpoint 9 — F10 Autonomous Preparation Workflow

Stage1.md F10/UC10: AgentController handles the goal as a memory turn, constructs the seven named commands and runs AgentSession. Planner uses Jev to choose from get description, import resume, gap, tailor, company, interview prep, checklist and done. Intake/import are now lazy planned actions inside the loop rather than mandatory work before planning. Existing saved inputs/results and memory are reread for each planning step.

Planner separately asks Jev whether the selected action was explicitly requested by the goal. Generic preparation is proposed work; an explicit action or clear synonym is requested. Requested steps execute without a redundant step approval; proposed steps use ApprovalGate. If request classification fails, the session asks approval rather than treating it as authorization. This interpretation remains model-dependent and needs live verification. Individual resume-edit/estimate approvals remain in their feature services. Starting an actual mock interview is outside this planner's seven-action list.

All seven actions now have their planned named classes. SkillGapAction is restored; other named commands remain in real use. Intake can collect a pasted description or an explicit LOOKUP request for Adzuna. Agent-driven resume intake accepts pasted text or @path to a UTF-8 text file; the normal Resume/CLI import interface continues to handle PDF/Word/etc. No file-format behavior was removed.

AgentState exposes onInput/onApproval responsibilities, and AgentSession.handleApproval delegates to the active state. Idle, Planning, AwaitingApproval, Executing and Finished transitions publish events. ActionResult records success/message for execution outcomes. A failed step is explained and skipped after the feature's own bounded retries. Declined work is removed from the current run; no repeated approval loop is forced.

Checklist is eligible at any planning step, without a forced last-position rule. A successful checklist ends the run; a failed checklist can be skipped while other work continues. Done, exhausted actions, the ten-selected-step limit, planner failure and cancellation produce explicit finished events. Stop is checked at state boundaries and interruption is propagated through the existing services. A completed or stopped run saves a timestamped trace; when no job has yet been selected, the session trace has an empty jobId rather than requiring an invented job. Nonempty trace job IDs still require real saved jobs.

Offline build passes 480 assertions. Coverage includes all eight initial provider options, done, early checklist completion, failed checklist continuation, explicit/proposed consent, classifier-failure fallback, invalid-state approval, stop during approval, planner failure, ten-step termination, empty-database planned intake/import with dependent services, saved results/trace, and done before intake with an unlinked trace. Existing checks cover feature error handling, observers, persistence and end-to-end preparation/interview behavior. These are provider fixtures, not a live planner demonstration.

## Live setup

Use a dedicated database so the agent has useful preparation work to perform without replacing other demos. This is an explicitly fictional posting for a real company, not a real vacancy:

```sh
export CAREER_DATA=data/f10-demo.db
java -jar target/career-agent.jar job add "Software Developer" "Microsoft" CA "FICTIONAL DEMO POSTING. Required skills: Java, SQL, Git, JUnit and Docker. Apply by: December 1, 2026."
java -jar target/career-agent.jar resume import samples/resume.md
java -jar target/career-agent.jar --gui
```

Confirm posting deadline is 2026-12-01 and resume warning is empty. Jev/Claude keys are required; Brave enables optional company research. Do not expose configuration or keys.

## Main GUI walkthrough

1. In Preparation, enter `Prepare me for this job using my saved job and resume` in Goal and click Prepare me.
2. Observe Planning/decision events and approval dialogs. Approve useful optional steps. Decline reimport/intake if proposed unnecessarily; existing inputs should be reused. You may decline tailoring or any other optional step to demonstrate rejection.
3. If reviewing tailored edits, accept/reject individually based on evidence. Do not assume approving an overall step approves every edit.
4. Watch completed/declined/step-error events and return to planning. The actual sequence is chosen by Jev; do not claim a fixed sequence or force unneeded operations for the recording.
5. When the planner chooses checklist or done, show the finished reason. If done is chosen before useful work despite incomplete preparation, report the trace rather than claiming success.
6. Open Saved results and Checklist / Show saved checklist when a checklist was produced. Close/reopen to verify saved outputs.

For direct-request behavior, run a goal such as `Run a fresh skill gap analysis, then build my preparation checklist using the saved job and resume`. Verify requested events and absence of redundant step approval for explicitly named work. If other work is proposed, it still needs consent. Do not infer consent from a changed goal that has not been submitted.

For stop, begin another goal and press Stop / cancel during active work. Confirm no later actions start and previously saved results remain accessible. A request already in progress may be interrupted; the UI can show cancellation while the worker finishes cancellation/trace persistence. Do not repeatedly start overlapping runs.

For planner-driven intake/import, optionally use a fresh database and submit an explicit goal to collect the job description and resume before analysis. Enter real sample text or @samples text paths when asked. Existing fixture checks do not substitute for this live alternative.

CLI equivalent:

```sh
java -jar target/career-agent.jar prepare "Prepare me for this job using my saved job and resume"
java -jar target/career-agent.jar results
```

Individual features persist results immediately. If a tool fails, inspect the skip explanation and remaining outputs. No fabricated result should be presented as a success.

## Demo and commit

Title: F10 — Autonomous Job Preparation with Approvals.
Description: Demonstrates an AI planner selecting preparation steps, requesting consent for proposed work, reporting progress and failures, and saving preparation outputs with a checklist or explicit completion.

Record goal, actual selected steps, one optional rejection, useful accepted work, finish reason and saved outputs. Record a direct-request or stop alternative separately if it makes the main video too long. Never claim an unobserved planner branch. Add actual accessible video URLs/timestamps to demo-guide.md.

Commit after live checks: `feat: restore full preparation planner and approval states`. F09 is already committed as c0fbb75. No F10 commit or push made here. This is the final feature checkpoint, not submission completion: video URLs, remaining live branches, packaged file parsing, refreshed report/PDF and final repository checks remain separate work.

## User-run F10 evidence

The user supplied a generic-goal trace selecting gap then checklist through AwaitingApprovalState/ExecutingState and ending with Checklist produced. Supplied checklist output contains the 2026-12-01 deadline, skill practice, remaining preparation tasks and progress. A second supplied trace shows requested: gap and requested: checklist with direct execution and successful completion. The user subsequently reported the F10 demo done. No video URL/timestamps were supplied; live rejection, done and cancellation branches remain unverified.
