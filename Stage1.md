## 1.1 Project Description

**What problem does your project solve?**

New graduates face a difficult job market. To apply you have to tailor a resume to each posting, work out what each employer expects, and prepare for interviews. The workload is overwhelming.

**Who are the target users?**

New graduates in the US and Canada who are preparing job applications.

**What can the agent do?**

For a goal like "prepare me for this job", the agent:

* gets the job description: pasted by the user or found through Adzuna (shown as a labeled partial excerpt), expanded into a labeled estimate, or generated as a labeled estimate.
* loads the user's saved history, and asks questions if there is none.
* extracts and stores the resume, identifies skill gaps, and tailors the resume without hallucinating skills.
* researches the company's goals and values through web search.
* prepares interview questions and runs a typed mock interview, with feedback at the end.
* builds a saved, prioritized checklist with progress tracking.

It asks for approval before any action the user did not request.

**Why is an AI agent appropriate for this problem?**

A deterministic software will overgeneralize answers and it's not tailored to the user's specific situation. An AI agent here decides if the history recorded from the user is useful and whether to extract information from them by asking related questions. And it chooses what to do next and which tools to use to reach the goals so the answers are not generic.

**Which AI/LLM model(s) do you plan to use?**

* Jev for making decisions between fixed options, since it's faster and cheaper than a regular LLM.
* Claude Haiku 4.5 for text generation tasks.
* Claude Sonnet 5.5 for harder reasoning tasks.

**How will the AI model interact with the rest of the software system?**

Our Java application uses a Swing GUI and a CLI and both call the AgentController.

The controller coordinates a planner and a memory manager and also a tool manager.

We use Jev through DecisionClient and Haiku and Sonnet through LLMClient.

Adzuna lookup and websearch as tools.

Store data in small SQLite database through repo classes.

## 1.2 Feature Specification

### F01 — Job Description Intake

The user pastes a job description in the GUI, or enters a job title, company and country (US or Canada) and clicks Find for me. If nothing was pasted, the agent searches Adzuna, Jev picks the closest listing, and the excerpt is shown labeled as partial. With the user's approval, Sonnet 5.5 expands it into a labeled estimate, or writes a fully estimated description if Adzuna finds nothing. Each fallback needs approval, and the approved description is saved with its source label and the Adzuna attribution. If Adzuna is down or a model call fails, the system retries once, then offers paste or an estimate. CLI: career job add / career job find. Hybrid, agentic.

### F02 — Resume Import and Skill Extraction

The user imports a resume (PDF, Word, Markdown or pasted text) through the GUI. A format-specific parser extracts the text, Haiku 4.5 structures it into skills, education, experience, projects and certifications, and the full profile is saved to memory as the user's base. If the file is scanned, corrupt or password protected, the user is asked to paste the text instead. If the model output is malformed, it retries once, then stores the raw text. CLI: career resume import. Hybrid.

### F03 — User History Memory

After each chat turn, Jev decides whether the user's message contains something worth saving (skills, target roles, weaknesses, previous actions), and Haiku 4.5 writes it as a short fact. The fact is deduplicated and saved without asking approval. The user can view and edit all saved facts in the My History panel. If saving fails, the turn is skipped without losing data, and conflicting facts are put to the user. CLI: career history show / edit. Hybrid.

### F04 — Skill-Gap Identification

The user opens the Skill Gap tab on a job. Haiku 4.5 extracts the required skills from the job description, the names are normalized, and Jev classifies each one against the stored profile. Results are shown in three ordered groups: completely missing skills, strong skills, and decent skills that could improve. Low-confidence results are marked uncertain. If the resume or job description is missing, the system offers to get it first. CLI: career gap. Hybrid.

### F05 — Resume Tailoring

The agent rewrites the resume's wording to match the job description and shows the original and tailored text side by side, with each change explained. A deterministic validator rejects any change that adds a skill the user does not have, and the user accepts or rejects each change. Accepted changes are saved as a new resume version linked to the job. CLI: career tailor. Hybrid.

### F06 — Company Goals and Values Research

The user clicks Research on a job's Company tab. Haiku 4.5 builds a search query such as "Google SWE goals and values 2026", the web search tool returns results, and Jev marks each result relevant or not. If none are relevant, the agent reformulates and retries up to a limit, then summarizes only the goals and values from relevant results. If nothing relevant is found, it says so and writes no invented summary. CLI: career company. Hybrid, agentic.

### F07 — Interview Question Preparation

The user clicks Generate on the Interview Prep tab. Sonnet 5.5 writes a prompt tailored to the user's profile, the job and the company, and Haiku 4.5 generates the questions (default 5). The questions are checked for count and format, then saved. The agent then offers to start a mock interview, which needs approval. Malformed output is retried once. CLI: career interview prep. AI-based.

### F08 — Mock Interview with Answer Feedback

The user starts a mock interview and answers the prepared questions one by one by typing. After each answer, Jev decides whether to ask a follow-up (at most one per question, written by Haiku 4.5) or move on. No feedback is shown during the interview. When it ends, Sonnet 5.5 judges all answers and produces a feedback report, and the weaknesses are saved to history. If the user quits midway, progress is saved and feedback is offered for the answered questions. CLI: career interview start. AI-based, agentic.

### F09 — Preparation Checklist

The agent builds a checklist from the skill gaps, company summary, interview questions and resume match status, and Sonnet 5.5 orders the items by priority: resume match, deadline, skills to learn, interview prep and company summary. A deadline is added only if the user's posting states one. The checklist panel in the GUI shows tick boxes and a progress indicator, and ticking an item saves it. If inputs are missing, a partial checklist is built and the missing features are offered, with approval. CLI: career checklist show / tick. Hybrid.

### F10 — Prepare Me For This Job

The user types a goal such as "prepare me for this job" in the GUI chat. The agent loads memory, and Jev repeatedly chooses the next action from a fixed list (get the description, import the resume, skill gap, tailor resume, company research, interview prep, checklist, done). Steps the user asked for run directly, and any other step is proposed with Yes and No buttons through the approval gate. The loop ends when the checklist is produced or the user stops. If a tool fails, the agent retries or skips the step and explains why, and a step limit prevents endless loops. CLI: career prepare. Hybrid, agentic.

## 1.3 Design Pattern Explanations

### Facade

**Participating classes:** AgentController, SwingGui, CliApp.

**Problem addressed:** The GUI and CLI should not need to know how the planner, memory, approvals, tools, AI clients and repositories work together.

**Rationale:** AgentController gives both interfaces one place to call. This keeps the interface code simpler and keeps the agent logic behind the controller.

### Command

**Participating classes:** AgentAction, GetJobDescriptionAction, ImportResumeAction, SkillGapAction, TailorResumeAction, CompanyResearchAction, InterviewPrepAction, ChecklistAction.

**Problem addressed:** The planner needs to choose between different actions and run them in the same way without having separate logic for every feature.

**Rationale:** Each step is represented as an AgentAction with execute(). The planner can choose an action and the controller can run it without depending on the concrete action type.

### Strategy

**Participating classes:** JobDescriptionSource, PastedTextSource, AdzunaExcerptSource, ExpandedEstimateSource, FullEstimateSource, ResumeParser, PdfResumeParser, DocxResumeParser, MarkdownResumeParser, PastedTextResumeParser.

**Problem addressed:** Job descriptions can come from different sources, and resumes can come in different formats.

**Rationale:** Each source or parser follows the same interface. JobDescriptionIntake and ResumeImporter can switch between implementations without changing their main workflow.

### Factory

**Participating classes:** ResumeParserFactory, ResumeParser, PdfResumeParser, DocxResumeParser, MarkdownResumeParser, PastedTextResumeParser.

**Problem addressed:** ResumeImporter should not contain file-type checks and directly create every concrete parser.

**Rationale:** ResumeParserFactory.createParser() chooses the correct parser in one place. Adding another supported format later does not require changing the importer workflow.

### State

**Participating classes:** AgentSession, AgentState, IdleState, PlanningState, AwaitingApprovalState, ExecutingState, FinishedState.

**Problem addressed:** The main agent loop behaves differently depending on whether it is waiting for input, planning, waiting for approval, running a step, or finished.

**Rationale:** Each state handles the behavior for that stage instead of putting one large set of if statements inside AgentSession. setState() changes the current behavior as the workflow moves forward.

### Observer

**Participating classes:** AgentEventPublisher, ProgressObserver, SwingGui, AgentEvent.

**Problem addressed:** The agent needs to report progress without being directly tied to how the GUI displays it.

**Rationale:** AgentEventPublisher sends events to observers. The agent can report the same progress events even if another interface is added later.

### Adapter

**Participating classes:** DecisionClient, JevAdapter, JevApi, LLMClient, ClaudeAdapter, AnthropicApi.

**Problem addressed:** The agent should not depend on the Jev and Anthropic APIs directly.

**Rationale:** JevAdapter and ClaudeAdapter implement DecisionClient and LLMClient and call the external APIs. The rest of the system only uses the two interfaces, so a model or API can be replaced without changing the agent code.

## 1.4 Use-Case Diagram

The use-case diagram covers the 10 main features. The User is the main actor. Adzuna and Web Search are external systems used by the related features, and the AI services are used for decision and generation tasks.

The use-case diagram is included in `uml.uxf` under **Use-Case Diagram**.

## 1.5 Detailed Use-Case Descriptions

### UC01 — Get Job Description

**Primary actor:** User  
**Supporting actors:** Adzuna, AI services  
**Preconditions:** The application is running.  
**Trigger:** The user pastes a job description or enters a title, company and country and chooses Find for me.  
**Main flow:**

1. The user provides pasted text or job search information.
2. AgentController receives the request and runs GetJobDescriptionAction.
3. JobDescriptionIntake uses the appropriate JobDescriptionSource.
4. Pasted text is used directly, or AdzunaTool finds listings and DecisionClient chooses the closest one.
5. If only a partial description is available, ApprovalGate asks before an expanded estimate is generated.
6. The accepted JobDescription is labeled with its source and saved by SqliteJobRepository.

**Alternative flows:** Adzuna fails; the system retries once and offers pasted text or an estimate. A model failure is retried once. The user can reject an estimated description.  
**Postconditions:** A JobDescription is saved for the job, or no description is saved if the user rejects the available options.

### UC02 — Import Resume

**Primary actor:** User  
**Supporting actor:** AI service  
**Preconditions:** The application is running and the user has a resume file or pasted resume text.  
**Trigger:** The user imports a resume.  
**Main flow:**

1. The user selects a PDF, Word, Markdown file or pastes text.
2. ImportResumeAction calls ResumeImporter.importResume().
3. ResumeParserFactory.createParser() selects the correct ResumeParser.
4. The parser extracts the text.
5. LLMClient structures the text into skills, education, experience, projects and certifications.
6. The ResumeProfile is saved by SqliteResumeRepository.

**Alternative flows:** If the file is scanned, corrupt or password protected, the user is asked to paste the text. If structured model output is malformed, the system retries once and then stores the raw text.  
**Postconditions:** The user's base ResumeProfile is saved.

### UC03 — Manage My History

**Primary actor:** User  
**Supporting actor:** AI service  
**Preconditions:** The application is running.  
**Trigger:** A chat turn finishes, or the user opens My History.  
**Main flow:**

1. AgentController sends the completed turn to MemoryManager.
2. DecisionClient decides whether the message contains a useful fact.
3. If it does, LLMClient writes a short MemoryFact.
4. MemoryManager deduplicates the fact and SqliteMemoryRepository saves it.
5. The user can view saved facts with findAll().
6. The user can edit or delete a saved fact.

**Alternative flows:** If saving fails, the turn continues and the fact is skipped. Conflicting facts are shown to the user instead of silently replacing each other.  
**Postconditions:** Useful history is stored and can be viewed, edited or deleted.

### UC04 — Identify Skill Gaps

**Primary actor:** User  
**Supporting actor:** AI service  
**Preconditions:** A job description and resume profile exist.  
**Trigger:** The user opens the Skill Gap tab or runs career gap.  
**Main flow:**

1. AgentController runs SkillGapAction.
2. SkillGapService.identify() extracts required skills from the JobDescription.
3. Skill names are normalized.
4. DecisionClient compares each required skill against the stored ResumeProfile.
5. SkillGapReport places each SkillAssessment into MISSING, STRONG or DECENT.
6. groupedByPriority() orders the results and the report is saved.

**Alternative flows:** If the resume or job description is missing, the system offers to get the missing input first. Low-confidence results are shown as uncertain.  
**Postconditions:** A saved SkillGapReport is available for the job.

### UC05 — Tailor Resume

**Primary actor:** User  
**Supporting actor:** AI service  
**Preconditions:** A ResumeProfile and JobDescription exist.  
**Trigger:** The user chooses Tailor Resume or runs career tailor.  
**Main flow:**

1. AgentController runs TailorResumeAction.
2. ResumeTailorService.tailor() creates proposed ResumeChange items for the job.
3. The service checks proposed changes against ResumeProfile.getSkills().
4. Any change that adds a skill the user does not have is rejected.
5. The original and proposed wording are shown side by side with the reason for each change.
6. The user accepts or rejects each change.
7. Accepted changes are saved as a new ResumeVersion linked to the job.

**Alternative flows:** If required data is missing, the related feature is offered first. Invalid AI output is not accepted as a resume change.  
**Postconditions:** A new approved ResumeVersion is saved, while the original resume remains unchanged.

### UC06 — Research Company

**Primary actor:** User  
**Supporting actors:** Web Search, AI services  
**Preconditions:** A job and company name exist.  
**Trigger:** The user clicks Research on the Company tab or runs career company.  
**Main flow:**

1. AgentController runs CompanyResearchAction.
2. CompanyResearchService builds a search query for the company, role, goals and values.
3. WebSearchTool returns search results.
4. DecisionClient marks results relevant or not relevant.
5. If needed, the service reformulates the query and retries up to its limit.
6. LLMClient summarizes only the relevant results into a CompanySummary.
7. SqliteJobRepository saves the summary.

**Alternative flows:** If no relevant sources are found, the system says so and does not create an invented summary.  
**Postconditions:** A sourced CompanySummary is saved, or the feature ends with no summary if relevant information was not found.

### UC07 — Prepare Interview Questions

**Primary actor:** User  
**Supporting actor:** AI service  
**Preconditions:** A job exists and enough profile/job information is available to prepare questions.  
**Trigger:** The user clicks Generate on Interview Prep or runs career interview prep.  
**Main flow:**

1. AgentController runs InterviewPrepAction.
2. InterviewPrepService.prepare() uses the user profile, job and company information.
3. Sonnet creates the tailored generation prompt.
4. Haiku generates the interview questions, with 5 by default.
5. The result is checked for count and format.
6. The InterviewQuestion objects are saved.
7. The system offers to start a mock interview through ApprovalGate.

**Alternative flows:** Malformed model output is retried once. If the user rejects the mock interview offer, the saved questions remain available.  
**Postconditions:** A saved interview-question set is available for the job.

### UC08 — Take Mock Interview

**Primary actor:** User  
**Supporting actor:** AI service  
**Preconditions:** Interview questions have been prepared.  
**Trigger:** The user starts a mock interview.  
**Main flow:**

1. SwingGui or CliApp calls AgentController.startMockInterview().
2. The controller shows one saved InterviewQuestion at a time.
3. The user types an answer.
4. DecisionClient decides whether one follow-up question is needed.
5. If needed, LLMClient generates the follow-up and the user answers it.
6. The process continues until the prepared questions are finished.
7. LLMClient generates the final FeedbackReport from all answers.
8. The report is saved, and weaknesses are passed to MemoryManager for history.

**Alternative flows:** If the user quits midway, answered progress is saved and the system offers feedback for the completed answers. No answer-by-answer feedback is shown during the interview.  
**Postconditions:** Interview progress and, when completed or requested, a FeedbackReport are saved.

### UC09 — View Preparation Checklist

**Primary actor:** User  
**Supporting actor:** AI service  
**Preconditions:** At least one useful preparation input exists for the job.  
**Trigger:** The user opens the checklist or runs career checklist show.  
**Main flow:**

1. AgentController runs ChecklistAction.
2. ChecklistService.build() reads available skill-gap, company, interview and resume-match information.
3. The checklist items are ordered by priority.
4. A posting deadline is included only when it exists in the job description.
5. SqliteChecklistRepository saves the Checklist.
6. SwingGui.showChecklist() displays the items and progress.
7. When the user ticks an item, ChecklistItem.tick() updates it, the repository saves it and Checklist.progress() returns the new progress.

**Alternative flows:** If some inputs are missing, a partial checklist is built and the system offers the related missing features with approval.  
**Postconditions:** The saved checklist reflects the user's current preparation progress.

### UC10 — Prepare For Job

**Primary actor:** User  
**Supporting actors:** Adzuna, Web Search, AI services  
**Preconditions:** The application is running.  
**Trigger:** The user enters a goal such as "prepare me for this job".  
**Main flow:**

1. SwingGui.submitGoal() or the CLI sends the goal to AgentController.handleGoal().
2. AgentSession.handleInput() moves the session into planning.
3. Planner.chooseNext() uses DecisionClient to choose the next AgentAction from the fixed action list.
4. If the action was not directly requested, ApprovalGate.review() asks the user first.
5. An approved AgentAction.execute() runs the feature step.
6. AgentSession.setState() moves between planning, approval and execution states.
7. AgentEventPublisher.notifyObservers() reports progress to the interface.
8. The loop repeats until the preparation checklist is produced, the planner chooses done, the step limit is reached, or the user stops.

**Alternative flows:** A failed tool or model call is retried where the feature allows it, or the step is skipped with an explanation. A rejected optional action returns the session to planning.  
**Postconditions:** The completed preparation work is saved and the AgentSession reaches FinishedState when the workflow ends.

## 1.6 Sequence Diagrams

### SD

Included in `uml.uxf` 



Feature-to-Design Traceability Table

|Feature|Description|Type|Related Use Case|Classes|Key Methods|Sequence Diagram|Design Pattern(s)|
|-|-|-|-|-|-|-|-|
|F01|Job description intake|Hybrid|UC01 Get Job Description|SwingGui, CliApp, AgentController, ApprovalGate, Planner, GetJobDescriptionAction, JobDescriptionIntake, JobDescriptionSource, PastedTextSource, AdzunaExcerptSource, ExpandedEstimateSource, FullEstimateSource, AdzunaTool, DecisionClient, LLMClient, SqliteJobRepository, JobDescription, SourceLabel|runFeature(), review(), execute(), getDescription(), label(), needsApproval(), fetch(), save()|SD01|Command, Strategy, Facade|
|F02|Resume import and skill extraction|Hybrid|UC02 Import Resume|SwingGui, CliApp, AgentController, ApprovalGate, Planner, ImportResumeAction, ResumeImporter, ResumeParserFactory, ResumeParser, PdfResumeParser, DocxResumeParser, MarkdownResumeParser, PastedTextResumeParser, LLMClient, SqliteResumeRepository, ResumeProfile, Skill|runFeature(), review(), execute(), importResume(), importText(), createParser(), extractText(), save(), getSkills()|SD02|Command, Factory, Strategy, Facade|
|F03|User history memory|Hybrid|UC03 Manage My History|SwingGui, CliApp, AgentController, MemoryManager, SqliteMemoryRepository, MemoryFact|handleGoal(), save(), findAll(), edit(), delete()|SD03|Facade|
|F04|Skill-gap identification|Hybrid|UC04 Identify Skill Gaps|SwingGui, CliApp, AgentController, ApprovalGate, Planner, SkillGapAction, SkillGapService, SkillGapReport, SkillAssessment, GapGroup, JobDescription, SqliteJobRepository|runFeature(), review(), execute(), identify(), groupedByPriority(), save()|SD04|Command, Facade|
|F05|Resume tailoring|Hybrid|UC05 Tailor Resume|SwingGui, CliApp, AgentController, ApprovalGate, Planner, TailorResumeAction, ResumeTailorService, ResumeVersion, ResumeChange, ResumeProfile, JobDescription, SqliteResumeRepository|runFeature(), review(), execute(), tailor(), getSkills(), save()|SD05|Command, Facade|
|F06|Company goals and values research|Hybrid|UC06 Research Company|SwingGui, CliApp, AgentController, ApprovalGate, Planner, CompanyResearchAction, CompanyResearchService, CompanySummary, SqliteJobRepository|runFeature(), review(), execute(), research(), save()|SD06|Command, Facade|
|F07|Interview question preparation|AI-based|UC07 Prepare Interview Questions|SwingGui, CliApp, AgentController, ApprovalGate, Planner, InterviewPrepAction, InterviewPrepService, InterviewQuestion, SqliteJobRepository|runFeature(), review(), execute(), prepare(), save()|SD07|Command, Facade|
|F08|Mock interview with answer feedback|AI-based|UC08 Take Mock Interview|SwingGui, CliApp, AgentController, InterviewQuestion, FeedbackReport, SqliteJobRepository|startMockInterview(), save()|SD08|Facade|
|F09|Preparation checklist|Hybrid|UC09 View Preparation Checklist|SwingGui, CliApp, AgentController, ApprovalGate, Planner, ChecklistAction, ChecklistService, Checklist, ChecklistItem, JobDescription, SqliteChecklistRepository|showChecklist(), runFeature(), review(), execute(), build(), progress(), tick(), save()|SD09|Command, Facade|
|F10|Prepare me for this job (main loop)|Hybrid|UC10 Prepare For Job|SwingGui, CliApp, ProgressObserver, ApprovalPrompter, AgentController, Planner, DecisionClient, AgentAction, ActionResult, ApprovalGate, AgentSession, AgentState, IdleState, PlanningState, AwaitingApprovalState, ExecutingState, FinishedState, AgentEventPublisher, AgentEvent|submitGoal(), handleGoal(), plan(), chooseNext(), review(), askApproval(), execute(), handleInput(), handleApproval(), setState(), onInput(), onApproval(), attach(), notifyObservers(), onEvent()|SD10|Facade, Command, State, Observer|

## How Each Feature Is Realized

### F01 — Job Description Intake

**Related Use Case:** UC01 — Get Job Description
**Related Sequence Diagram:** SD01 — Get Job Description

**Classes involved:**

* SwingGui / CliApp — take the pasted text or the job title, company, and country.
* AgentController — receives the request.
* ApprovalGate — asks the user to approve partial or estimated descriptions.
* GetJobDescriptionAction — the command that runs the intake.
* JobDescriptionIntake — tries the sources one by one.
* JobDescriptionSource — one strategy per way of getting a description (pasted, Adzuna, expanded estimate, full estimate).
* JobDescription — the result, with its SourceLabel.
* SqliteJobRepository — saves it.

**Important methods:**

* AgentController.runFeature()
* ApprovalGate.review()
* GetJobDescriptionAction.execute()
* JobDescriptionIntake.getDescription()
* JobDescriptionSource.fetch()
* SqliteJobRepository.save()

**Execution:** The user pastes a description or asks the agent to find one. AgentController.runFeature() checks with ApprovalGate.review() and then runs GetJobDescriptionAction.execute(). The action calls getDescription(), which tries each source's fetch() in order. If a source needs approval, the user is asked first. The accepted JobDescription is saved with its source label.

### F02 — Resume Import and Skill Extraction

**Related Use Case:** UC02 — Import Resume
**Related Sequence Diagram:** SD02 — Import Resume

**Classes involved:**

* SwingGui / CliApp — take the file or pasted text.
* AgentController — receives the request.
* ImportResumeAction — the command that runs the import.
* ResumeImporter — coordinates the import.
* ResumeParserFactory — picks the parser for the file type.
* ResumeParser — extracts text (PDF, DOCX, Markdown, or pasted text).
* LLMClient — structures the text.
* ResumeProfile — the result, with its Skill list.
* SqliteResumeRepository — saves it.

**Important methods:**

* ImportResumeAction.execute()
* ResumeImporter.importResume()
* ResumeParserFactory.createParser()
* ResumeParser.extractText()
* ResumeProfile.getSkills()
* SqliteResumeRepository.save()

**Execution:** The user imports a resume. ImportResumeAction.execute() calls importResume() on ResumeImporter. The importer asks ResumeParserFactory.createParser() for the right parser, and the parser's extractText() reads the file. The text goes to LLMClient, which structures it into a ResumeProfile with skills. The profile is saved.

### F03 — User History Memory

**Related Use Case:** UC03 — Manage My History
**Related Sequence Diagram:** SD03 — Manage My History

**Classes involved:**

* SwingGui / CliApp — show and edit saved facts.
* AgentController — handles each chat turn.
* MemoryManager — manages what the agent remembers.
* MemoryFact — one saved fact.
* SqliteMemoryRepository — stores the facts.

**Important methods:**

* AgentController.handleGoal()
* SqliteMemoryRepository.save()
* SqliteMemoryRepository.findAll()
* MemoryFact.edit()
* SqliteMemoryRepository.delete()

**Execution:** After each chat turn, handleGoal() lets MemoryManager keep any useful fact as a MemoryFact, and save() stores it. To see history, the interface reads the facts with findAll(). To change one, edit() updates the text and save() stores it again. delete() removes a fact.

### F04 — Skill-Gap Identification

**Related Use Case:** UC04 — Identify Skill Gaps
**Related Sequence Diagram:** SD04 — Identify Skill Gaps

**Classes involved:**

* AgentController — receives the request.
* SkillGapAction — the command that runs it.
* SkillGapService — builds the report.
* SkillGapReport — the report, with one SkillAssessment per skill.
* GapGroup — MISSING, STRONG, or DECENT.
* SqliteJobRepository — saves the report.

**Important methods:**

* SkillGapAction.execute()
* SkillGapService.identify()
* SkillGapReport.groupedByPriority()
* SqliteJobRepository.save()

**Execution:** SkillGapAction.execute() calls identify() for the job. The service returns a SkillGapReport where every skill is in a group. groupedByPriority() puts them in display order, and the report is saved.

### F05 — Resume Tailoring

**Related Use Case:** UC05 — Tailor Resume
**Related Sequence Diagram:** SD05 — Tailor Resume

**Classes involved:**

* AgentController — receives the request.
* TailorResumeAction — the command that runs it.
* ResumeTailorService — produces the tailored version.
* ResumeVersion — the new version, made of ResumeChange items.
* ResumeProfile — the original resume and its skills.
* SqliteResumeRepository — saves the version.

**Important methods:**

* TailorResumeAction.execute()
* ResumeTailorService.tailor()
* ResumeProfile.getSkills()
* SqliteResumeRepository.save()

**Execution:** TailorResumeAction.execute() calls tailor() for the job. The service checks changes against the skills from getSkills() and returns a ResumeVersion. Each ResumeChange shows the original, the revised text, and the reason. The user accepts or rejects each change, and the version is saved.

### F06 — Company Goals and Values Research

**Related Use Case:** UC06 — Research Company
**Related Sequence Diagram:** SD06 — Research Company

**Classes involved:**

* AgentController — receives the request.
* CompanyResearchAction — the command that runs it.
* CompanyResearchService — does the research.
* CompanySummary — the result (goals and values).
* SqliteJobRepository — saves it.

**Important methods:**

* CompanyResearchAction.execute()
* CompanyResearchService.research()
* SqliteJobRepository.save()

**Execution:** CompanyResearchAction.execute() calls research() for the job. The service returns a CompanySummary with the company's goals and values, which is saved.

### F07 — Interview Question Preparation

**Related Use Case:** UC07 — Prepare Interview Questions
**Related Sequence Diagram:** SD07 — Prepare Interview Questions

**Classes involved:**

* AgentController — receives the request.
* InterviewPrepAction — the command that runs it.
* InterviewPrepService — generates the questions.
* InterviewQuestion — one question, with space for the answer.
* SqliteJobRepository — saves them.

**Important methods:**

* InterviewPrepAction.execute()
* InterviewPrepService.prepare()
* SqliteJobRepository.save()

**Execution:** InterviewPrepAction.execute() calls prepare() for the job. The service returns a list of InterviewQuestion objects, which are saved.

### F08 — Mock Interview with Answer Feedback

**Related Use Case:** UC08 — Take Mock Interview
**Related Sequence Diagram:** SD08 — Take Mock Interview

**Classes involved:**

* SwingGui / CliApp — show each question and take the typed answer.
* AgentController — runs the interview.
* InterviewQuestion — holds each question and its answer.
* FeedbackReport — feedback for each answer, plus a summary.
* SqliteJobRepository — saves the results.

**Important methods:**

* AgentController.startMockInterview()
* SqliteJobRepository.save()

**Execution:** The interface calls startMockInterview() for the job. The controller shows the saved questions one at a time and stores each typed answer. Only after the last answer is a FeedbackReport created and saved, so the user sees no feedback during the interview.

### F09 — Preparation Checklist

**Related Use Case:** UC09 — View Preparation Checklist
**Related Sequence Diagram:** SD09 — View Preparation Checklist

**Classes involved:**

* SwingGui / CliApp — show the checklist and take tick actions.
* AgentController — receives the request.
* ChecklistAction — the command that runs it.
* ChecklistService — builds the checklist.
* Checklist — the list, with a progress value.
* ChecklistItem — one task, with a priority and a done flag.
* SqliteChecklistRepository — stores it.

**Important methods:**

* ChecklistAction.execute()
* ChecklistService.build()
* SwingGui.showChecklist()
* ChecklistItem.tick()
* Checklist.progress()
* SqliteChecklistRepository.save()

**Execution:** ChecklistAction.execute() calls build(), which creates a Checklist whose items are ordered by priority. It is saved and shown with showChecklist(). When the user ticks an item, tick() marks it done, the change is saved, and progress() gives the new progress value.

### F10 — Prepare Me For This Job

**Related Use Case:** UC10 — Prepare For Job
**Related Sequence Diagram:** SD10 — Prepare For Job

**Classes involved:**

* SwingGui / CliApp — take the goal and show progress and approval questions.
* AgentController — the single entry point.
* AgentSession — holds the goal and the current AgentState.
* AgentState — five states (Idle, Planning, AwaitingApproval, Executing, Finished) that decide how input is handled.
* Planner — chooses the next AgentAction.
* AgentAction — one step to run, returning an ActionResult.
* ApprovalGate — asks the user to approve steps they did not request.
* AgentEventPublisher — sends progress events to the interface.

**Important methods:**

* SwingGui.submitGoal()
* AgentController.handleGoal()
* AgentSession.handleInput()
* Planner.chooseNext()
* ApprovalGate.review()
* AgentAction.execute()
* AgentSession.setState()
* AgentEventPublisher.notifyObservers()

**Execution:** The user types a goal, and submitGoal() sends it to handleGoal(), which passes it to the session's handleInput(). The current state decides what to do. In the planning state, chooseNext() picks the next action. If the user did not ask for that step, review() asks for approval first. Approved steps run with execute(). Each change of stage goes through setState(), and notifyObservers() tells the interface about progress. This repeats until the session reaches FinishedState.

