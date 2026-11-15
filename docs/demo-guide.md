# Demonstration recording checklist

No videos have been recorded or uploaded by this implementation session. Replace every **NOT RECORDED** cell with your real unlisted/public video URL and actual timestamp. Do not submit placeholders as if they were evidence.

Before recording: build, configure the planned Jev/Claude APIs and required feature tool keys, use a sample resume without private information, and verify the chosen model returns acceptable structured output. Keep the configuration file and keys off screen. Use both GUI and CLI in the overall demonstration.

| Feature | Action and evidence to capture | Interface | Video / timestamp |
|---|---|---|---|
| F01 | Save pasted Canadian/US posting; show source label and stored job. If keys available, find an Adzuna excerpt and reject/approve an explicitly labeled estimate. | GUI Job + CLI job show | NOT RECORDED / TBD |
| F02 | Import DOCX/Markdown (PDF with Maven build); show raw text and structured sections; demonstrate paste alternative on unsupported input. | GUI Resume | NOT RECORDED / TBD |
| F03 | Remember a personal career message; show deduplication; edit/delete by fact ID. | GUI My History + CLI history show | NOT RECORDED / TBD |
| F04 | Run gap analysis and show MISSING/STRONG/DECENT ordering and uncertainty/evidence labels. | GUI Preparation | NOT RECORDED / TBD |
| F05 | Compare original/revised/reason in approval dialog, accept one edit and reject another if offered, show saved version and preserved base. Conservative wording changes use validated word forms; broader paraphrases are rejected. | GUI Preparation + CLI resume versions | NOT RECORDED / TBD |
| F06 | Run live research; show source URLs and summary; demonstrate missing-key/no-result failure clearly. A failed request alone is not evidence of live research working. | GUI Preparation | NOT RECORDED / TBD |
| F07 | Generate exactly five personalized questions; decline/accept the separate mock-interview offer. | GUI Preparation | NOT RECORDED / TBD |
| F08 | Answer one question; show at most one follow-up. Stop without feedback, continue saved answers, finish and show final report and saved weaknesses. | GUI Mock Interview + CLI interview commands | NOT RECORDED / TBD |
| F09 | Build partial/full checklist; show that deadlines come only from a labeled ISO date in posting; tick and restart to show persistence and progress. | GUI Checklist | NOT RECORDED / TBD |
| F10 | Submit goal, show planner decisions, Yes/No approvals, rejected and failed steps, final checklist and saved action trace. | GUI Preparation + CLI prepare | NOT RECORDED / TBD |

Supplementary recording: `sh scripts/build.sh` output; CLI help and one-shot invocation; app restart retaining current job/profile/history/checklist. Only call integrations verified after actually running them. Record limitations honestly rather than masking failures.

## F01/F02 recording gate

Code-level acceptance checks pass for checkpoint 3. First complete the actual local walkthrough in checkpoint-3-inputs.md; do not record a claimed success from fixture-only results. F01 live Adzuna lookup needs its local app ID/key, and F02 structured extraction must have an empty warning. PDF/legacy Word need packaged dependency checks. Use samples/ for non-sensitive inputs and a separate demo database.

Once those checks pass, record two labeled segments:
1. **F01 — Job intake:** paste/save/show source; real lookup partial excerpt plus Jobs by Adzuna URL; decline/approve an estimate; show that rejection preserves the previous job. Explain Jev chooses a listing and Sonnet estimates only with consent.
2. **F02 — Resume import:** import Markdown/DOCX and PDF if verified, show skills/all sections and preserved source; show one corrupt/unsupported-file or raw-fallback alternative honestly. Explain Haiku extracts source-supported facts, not invented qualifications.

Use CLI plus GUI across the segments. No URLs/timestamps have been recorded yet; replace placeholders only after actual recording. Later material UI changes require updating affected footage.

## Current recording reminder

The user has confirmed pasted F01 intake, real Markdown/DOCX F02 extraction (empty warnings) and basic GUI operation. Record these verified paths now if the interface is stable enough; use the fictional sample files and demo database. Label the segments F01 and F02, show invocation/input/results, and demonstrate one real error/alternative path. Do not present Adzuna lookup, estimates, PDF or binary Word as successful until separately exercised. Add later footage for those paths; rerecord if material GUI changes make the recording inaccurate. No recordings have been provided yet.

F03 live verification and recording steps: [checkpoint-4-memory.md](checkpoint-4-memory.md). Offline checks pass; real Jev/Claude and GUI walkthrough remain pending.

F04 live verification and recording steps: [checkpoint-5-gap.md](checkpoint-5-gap.md). Offline checks pass; real-provider and GUI walkthrough pending.

F05 live verification and recording steps: [checkpoint-5-tailoring.md](checkpoint-5-tailoring.md). Real-provider and GUI review checks remain pending.

F06 live verification and recording steps: [checkpoint-6-company.md](checkpoint-6-company.md). Use a real company with an explicitly labeled demo posting. Offline checks pass; live Brave/Jev/Claude and GUI checks remain pending.

F07 live verification and recording steps: [checkpoint-6-questions.md](checkpoint-6-questions.md). Both model stages receive full evidence; live personalization, GUI and separate consent checks remain pending.

F08 live verification and recording steps: [checkpoint-7-interview.md](checkpoint-7-interview.md). Final feedback now appears automatically after completion; weakness-save failure warns without hiding the report. Real-model and GUI checks remain pending.
