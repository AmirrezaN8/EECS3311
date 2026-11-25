# Stage 2 Demonstration Index

The original demonstration links are listed below. I recorded this supplementary playlist after the deadline. If late evidence can be considered, I would appreciate your taking a look. The playlist contains updated F01–F10 recordings, including F01’s demonstration of the same job-intake task in Swing and then in the standalone CLI. [Supplementary Stage 2 demo playlist](https://www.youtube.com/playlist?list=PLTA01MSBttJc)

Original playlist: [EECS3311demos](https://www.youtube.com/playlist?list=PLWFiN1jmW3sI).

Checked November 15, 2026. The playlist and its nine F01–F09 videos were retrieved without account cookies or signing in. All nine are unlisted and accessible by link; downloaded review copies contain actual screen video and audio streams. The newly added 39-second F10 upload was also retrieved without login and reviewed at readable resolution; all ten videos are now accessible.

Each entry is a dedicated feature recording, so 00:00 links to the whole feature demonstration. These are actual start timestamps, not invented internal segment boundaries. Descriptions below state what was observed in sampled footage, not every behavior in the source code.

| Feature | Direct video / start | Interface observed | Observed application evidence |
|---|---|---|---|
| F01 — Job Description Intake | [Watch from 00:00](https://www.youtube.com/watch?v=qXdWoMGQRNM&t=0s) | GUI | Pasted posting entry/save, USER_PASTED source, explicit deadline and missing-title validation. |
| F02 — Resume Import and Skill Extraction | [Watch from 00:00](https://www.youtube.com/watch?v=3hR3z8x4h0c&t=0s) | CLI | DOCX and Markdown import, structured profile output, missing-file error and retained profile. |
| F03 — User History Memory | [Watch from 00:00](https://www.youtube.com/watch?v=k6f0MA-zz0g&t=0s) | GUI; prior CLI output visible | Saved history, AI duplicate skipping, missing fact-ID validation and approved deletion. |
| F04 — Skill-Gap Identification | [Watch from 00:00](https://www.youtube.com/watch?v=O3Qmxmvgtns&t=0s) | CLI and GUI | Posting/resume context, gap invocation, ordered groups, uncertainty flags and GUI saved results. |
| F05 — Resume Tailoring | [Watch from 00:00](https://www.youtube.com/watch?v=WQtftW-NVN8&t=0s) | GUI | Original profile, side-by-side proposal/reason, acceptance, saved version and original profile inspection. |
| F06 — Company Goals and Values Research | [Watch from 00:00](https://www.youtube.com/watch?v=r0O5peysyWs&t=0s) | CLI and GUI | Microsoft company input/research, goals with source URLs, explicit not-stated values and GUI retrieval. |
| F07 — Interview Question Preparation | [Watch from 00:00](https://www.youtube.com/watch?v=q5oq_tFzdRQ&t=0s) | GUI; prior CLI output visible | Generation of five questions, separate mock-interview approval dialog and retrieval after reopening. |
| F08 — Mock Interview with Feedback | [Watch from 00:00](https://www.youtube.com/watch?v=PTBeonTQZ9A&t=0s) | GUI | New interview attempt, progression through typed questions/answers and end-of-interview feedback/weaknesses. |
| F09 — Preparation Checklist | [Watch from 00:00](https://www.youtube.com/watch?v=mvviBAHe5yw&t=0s) | GUI | Partial-checklist consent, deadline/tasks, ticking with percentage progress, reopening and retained tick. |
| F10 — Prepare Me For This Job | [F10 — 00:00](https://www.youtube.com/watch?v=NZzGRP7kGIw&t=0s) | GUI | Requested gap/checklist execution, Planning/Executing/Finished states, Checklist produced, and checklist output. Trace visible at 00:21; output visible at 00:29. |

## Review findings and limits

- **GUI and CLI coverage verified:** F01 and F03–F09 show Swing; F02 shows CLI invocation/results, and F04/F06 show CLI and GUI.
- **Explanation gap across all nine:** FFmpeg measured mean and maximum audio levels of approximately -91 dB for every complete downloaded audio track (effectively silent). YouTube reported no uploaded or automatic captions. Titles/descriptions identify the feature name and sometimes explain AI, but an audible explanation or explanatory on-screen text was not established in the sampled footage. The official Stage 2 instructions, p. 4, require each demonstration to identify its feature ID/name and briefly explain its purpose/AI role. Do not mark that requirement fully verified from a description alone. Brief explanation cards or narration can be added to the existing footage; a complete re-recording is not required.
- **F01 scope:** pasted intake and input validation are shown. Adzuna selection, attribution and approved estimates are not established by this recording.
- **F02 scope:** DOCX/Markdown extraction and the missing-file alternative are shown. PDF/legacy Word and scanned/locked input are not established by this recording.
- **F03 scope:** duplicate skipping and deletion are visible; a fresh AI fact save and conflict rejection were not established in the sampled recording. Prior CLI history visible behind the GUI is not treated as proof those actions ran during this video.
- **F05 scope:** side-by-side proposal/reason is visible at 00:34, accepted result at 00:36. Rejection of an edit is not established by the sampled recording.
- **F07 scope:** mock-interview consent is visible at 00:28; the subsequent question list and reopened results are visible. This is not a demonstration of a completed mock interview.
- **F08 scope:** real question progression and final feedback are visible. Optional follow-ups, pause/resume and approved partial-feedback behavior are not established by the sampled recording. Short placeholder answers demonstrate the application flow, not interview performance.
- **F09 scope:** consent and checklist ticking/persistence are visible. Rebuild preservation after a later rebuild is not established by the sampled recording.
- **F10:** [00:21](https://www.youtube.com/watch?v=NZzGRP7kGIw&t=21s) shows requested gap/checklist execution and Checklist produced; [00:29](https://www.youtube.com/watch?v=NZzGRP7kGIw&t=29s) shows checklist output. Proposed-step approval/rejection, cancellation and early done are not established by this recording. Its title remains a generic screen-recording filename, description is empty, audio mean/max is approximately -90.3 dB, and no captions are available. The author accepts the existing title and description; no metadata edits are planned. Upload availability is verified; the recording alone does not establish the full explanation requirement.
- **Secrets/readability:** no API keys were identified in the inspected frames. This was sampled visual review, not exhaustive frame-by-frame credential certification. Review the original videos before final submission and keep configuration/private data off screen.

The review used metadata extraction, actual temporary video downloads, sampled frames across each clip, additional close inspection of F05/F07 dialogs and whole-audio volume analysis. It did not infer every claimed behavior from video titles/descriptions. Review copies and extracted frames remain outside the repository in temporary storage; they are not submission assets.

## Source walkthroughs

[F01/F02](checkpoint-3-inputs.md), [F03](checkpoint-4-memory.md), [F04](checkpoint-5-gap.md), [F05](checkpoint-5-tailoring.md), [F06](checkpoint-6-company.md), [F07](checkpoint-6-questions.md), [F08](checkpoint-7-interview.md), [F09](checkpoint-8-checklist.md), [F10](checkpoint-9-agent.md).

Supporting commands (job selection, output/version inspection, direct fact entry, interview restart and untick) are part of the original F01–F10 workflows, not extra counted features. If a substantial new feature is added later, add a dedicated demonstration or labeled segment.

## Stage 2 narrated demonstration (3 minutes 30 seconds)

Before recording, build with `sh scripts/build.sh`. From the repository root, run the following in one terminal so both interfaces use a disposable database. Keep that terminal open for the CLI segment. Open `samples/add-job.task` in a text editor for reference.

```sh
export CAREER_DATA="$(mktemp -d)/demo.db"
java -jar target/career-agent.jar --gui
```

| Time | On screen | Narration |
|---|---|---|
| 0:00–0:25 | Show the sample task next to the Job tab. Highlight its title, company, country and posting text. | “I’ll save this fictional job twice. The first time I’ll enter it in Swing. The second time I’ll give the standalone JAR exactly the same inputs in a file.” |
| 0:25–1:15 | Enter Junior Java Developer, Example Company and CA. Paste the final quoted argument from the sample without its quotation marks. Click **Save pasted posting**. | “This is the pasted-posting path. The title and company identify the role, and the description supplies the requirements. I can complete intake without a job search or a model call.” |
| 1:15–1:50 | Click **Show current job**. Point to its title, source label, description and ID. Close Swing. | “The selected record contains the Java and SQL text I supplied. Its source label tells us where that text came from. I’ll use these fields to check the terminal result.” |
| 1:50–2:40 | In the same terminal run `cat samples/add-job.task`, then `java -jar target/career-agent.jar -input samples/add-job.task`. Pause on the result. | “The file contains one CLI command. Quotes keep the multiword arguments together. This invocation saves the posting again and exits without waiting for keyboard input.” |
| 2:40–3:30 | Run `java -jar target/career-agent.jar job show`. Point to the selected ID from the CLI save and compare its title, company, country, source and description with the GUI result. | “This is a fresh process reading the selection from SQLite. Both interfaces completed job intake through AgentController. The matching posting fields are the evidence, and the same task file can now drive a behavioural test.” |
