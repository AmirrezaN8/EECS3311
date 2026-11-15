# Checkpoint 5 — F05 Resume Tailoring

Stage1.md F05/UC05 requires real wording changes, deterministic rejection of unsupported skills, original/proposed/reason review, independent acceptance and a new job-linked version with the base preserved.

TailorResumeAction now participates in the shared command/planner map. ResumeTailorService uses the reasoning model for up to five proposals. Its validator requires a unique source passage, preserves substantive token counts, technical names, numbers and negations, and allows a small set of connective words and common word forms. For example, `Developed Java applications` can become `Java application development`. Unknown technical words such as Docker cannot be added to that passage. Overlapping proposals fail validation before approval. Invalid output retries once, then fails without creating a version.

ApprovalGate delegates structured ResumeChange review to the interface: CLI shows ORIGINAL/REVISED/REASON with y/N; Swing shows read-only original/proposed columns plus a reason with Yes/No. Each change is independently reviewed. Accepted edits become a separate immutable ResumeVersion linked to the job and base profile. Rejected edits remain unchanged; the version records review outcomes. Direct tailoring offers the same approved missing-job/resume recovery as gap/prepare.

Limit: this is conservative morphological rewriting, not broad free-form paraphrasing. New substantive vocabulary remains rejected, even when a human would consider it equivalent. Token preservation cannot prove that reordered text keeps the same meaning; per-edit human review remains necessary. The validator does not claim a general semantic anti-fabrication proof.

## Live check

Continue in the existing F04 sample database so no new imports are necessary:

```sh
export CAREER_DATA=data/f04-demo.db
java -jar target/career-agent.jar tailor
```

Review each actual proposal. Confirm original text, different improved wording and a job-related reason. Accept one supported edit with `y`; reject another with `n` if offered. The model may offer fewer proposals or no safe improvements. Empty proposals or failed validation are not successful evidence of rewriting. If it fails, share the error; do not claim live success from fixtures.

Then run:

```sh
java -jar target/career-agent.jar resume versions
java -jar target/career-agent.jar resume show
java -jar target/career-agent.jar --gui
```

Show the accepted wording in the saved version, accepted/rejected flags and links to the job/base. Show that resume show still contains the original text. Separate CLI processes verify persistence. In GUI Preparation, click Tailor resume and confirm the side-by-side approval dialog is readable; use Yes/No to review each change. In Resume, click Tailored versions and Show profile to compare saved results with the preserved base. A fresh GUI process is needed after rebuilding.

For missing-input recovery, use a fresh disposable CAREER_DATA path, invoke tailor, decline the input offer, then restore CAREER_DATA=data/f04-demo.db. Keep keys and private data off screen. No unsupported edit should be accepted merely to make the video look successful.

## Recording

F05 — AI Resume Tailoring. Demonstrate an actual wording improvement and explanation, independent approval/rejection, persisted version and preserved original, using GUI review and CLI retrieval. Explain the reasoning-model generation, deterministic evidence limits and user approval. Add real video URLs/timestamps to demo-guide.md after recording. Live generation, GUI rendering and alternative paths remain unverified here.
