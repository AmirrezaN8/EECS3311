# Checkpoint 6 — F07 Interview Question Preparation

Stage1.md F07/UC07: InterviewPrepAction participates in AgentController's shared command/planner map. InterviewPrepService loads company research and passes job, base profile, company evidence (or an explicit not-researched marker) and saved history to Sonnet for tailored instructions, then to Haiku for generation. Both model calls now receive the same full evidence context; the Haiku call also receives Sonnet's instructions.

The output schema is an array of exactly five nonblank strings with distinct normalized texts. The service assigns unique IDs, links each question to the job, leaves answers empty, and saves the question set only after validation. Each model operation retries once on failure. Invalid output does not overwrite previously saved questions. Question quality and semantic distinctness still require human review; normalized comparison covers case/whitespace, not paraphrases. Imperative interview prompts such as “Describe a project” are valid; a question mark is not mandatory.

After a direct CLI/GUI preparation request saves questions, AgentController separately offers to start a mock interview. No leaves the questions saved and creates no session. Yes starts/resumes the interview. F08 owns interview continuation/feedback; this checkpoint does not claim those paths live-verified.

## Live check

Continue with the real-company research database from F06. It has a job and company summary but may not yet have a resume, so import the fictional sample:

```sh
export CAREER_DATA=data/f06-demo.db
java -jar target/career-agent.jar resume import samples/resume.md
java -jar target/career-agent.jar interview prep
```

Confirm the resume warning is empty. When prompted “Questions saved. Start a typed mock interview now?”, enter `n`. Expect five distinct source-grounded questions, each with a unique ID, the selected job ID and an empty answer. Do not expect exact wording; inspect whether questions fit the Java/SQL posting, demonstrated resume experience and company evidence without treating unclaimed skills as possessed qualifications.

```sh
java -jar target/career-agent.jar results
java -jar target/career-agent.jar --gui
```

The separate results command demonstrates persistence without regeneration. In GUI Preparation, click Saved results to show the question set. Then click Generate interview questions, select No at the mock-interview offer, and inspect the five saved questions. CLI and GUI generation replace the prepared question set for that job; they do not amend the original resume. Avoid regenerating questions during a real interview attempt; existing interview sessions retain their own question snapshot.

For a separate consent-acceptance check, repeat preparation and approve the interview offer. Confirm a first question is displayed. Full interview behavior remains the next checkpoint. A rejection/error should be shown as observed, not presented as successful generation. Keep credentials/private facts off screen.

## Demo and commit

Title: F07 — Personalized AI Interview Questions.
Description: Generates five personalized interview questions from the job, resume, company research and saved history, checks their format, saves them, and asks permission before starting a mock interview.

Record sample resume import if needed, generation, rejection of immediate mock interview, five questions, and retrieval of saved questions in GUI. Explain Sonnet's instruction-planning role, Haiku generation and separate user consent. Add actual accessible video URL/timestamps to demo-guide.md. Live generation, GUI and consent checks remain pending until user evidence is supplied.

Commit after live checks: `feat: ground interview questions in company evidence and history`. No commit or push made here. F06 is already committed as 8ca6813.
