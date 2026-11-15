# Checkpoint 4 — F03 User History Memory

Implementation follows Stage1.md F03/UC03: AgentController routes chat turns and goals to MemoryManager; Jev chooses save/skip, Haiku extracts one short fact, SQLite retains it, and CLI/GUI expose show/edit/delete. Existing history is included in planner context and interview preparation. Conflicting facts require approval and are kept alongside existing facts when accepted; rejection leaves history intact.

AI fact extraction now validates the four planned categories and requires a source phrase from the user's message, matching the existing resume extraction approach. This rejects unsupported wording and may skip valid paraphrases after one retry. Saved/skipped/warning events explain the outcome. Direct user-entered facts and edits retain their existing behavior.

Offline acceptance checks cover irrelevant messages without generation, useful facts without approval, Haiku tier, normalized duplicates, conflict approval/rejection, unsupported text, invalid categories, malformed output, empty messages, edit/delete/restart, unknown IDs, and actual SQLite write failure without loss of existing facts. All provider responses in these checks are fixtures. Live Jev/Claude behavior and desktop interaction remain unverified for F03.

## Live walkthrough

Use fictional facts and a separate demo database. Keep API keys and private history off screen.

```sh
sh scripts/build.sh
export CAREER_DATA=data/f03-demo.db
java -jar target/career-agent.jar chat "I know Java and SQL"
java -jar target/career-agent.jar history show
java -jar target/career-agent.jar chat "I know Java and SQL"
java -jar target/career-agent.jar history show
java -jar target/career-agent.jar chat "Hello"
java -jar target/career-agent.jar chat "I do not know Java or SQL"
```

Expect a saved source-supported fact with a skills category, no duplicate if the same fact is extracted again, and no new fact for the greeting. Model wording can vary; normalized duplicates are skipped locally, and Jev compares other candidates for conflict, equivalent meaning or new information. Semantic comparison remains model-dependent. If Jev flags the contradictory message, decline once and confirm existing history remains; repeat and accept to demonstrate saving alongside the original. Actual decisions must be observed rather than assumed.

Copy an actual ID from `history show` for these commands:

```sh
java -jar target/career-agent.jar history edit ACTUAL_FACT_ID "I know Java, SQL and Git"
java -jar target/career-agent.jar history show
java -jar target/career-agent.jar history delete ACTUAL_FACT_ID
java -jar target/career-agent.jar history show
java -jar target/career-agent.jar --gui
```

Separate CLI processes demonstrate persistence after restart. In GUI My History, check Remember message (AI), Show history, Edit fact and Delete fact with actual IDs. Confirm delete approval and updated history. Run `unset CAREER_DATA` afterward.

Record F03 only after this real-provider walkthrough succeeds. Show inputs, saved output, Jev's save/skip role, Haiku's extraction role, edit/delete/restart and an alternative path. Add the actual accessible video URL/timestamps to docs/demo-guide.md. No live success or recording is claimed here.

## November 15 live evidence and duplicate correction

User-provided Terminal output confirms a real skills fact was saved and loaded in a separate process. A contradictory message triggered approval; declining published memory-skipped and retained history. The repeated positive message produced both “know Java and SQL” and “I know Java and SQL”, exposing the limit of exact normalized comparison. The existing Jev comparison now includes a duplicate option for equivalent wording, with conflict taking priority. A fixture regression checks that equivalent wording is skipped without approval. Existing saved facts are preserved; no automatic cleanup was performed. Real-provider duplicate handling must be rerun with the rebuilt JAR.

## GUI target-role skip — November 15

The user reported memory-skipped for `i am targeting junior java developer roles` in the GUI. This is a useful target-role fact and should be eligible for saving. The save/skip prompt now explicitly defines all four Stage 1 categories, includes aspirations/desired roles, and contrasts examples with greetings. Jev still owns the decision; no keyword override or simulated success was added. The real GUI retry remains pending after rebuilding and restarting the running GUI.

User follow-up confirmed successful deletion/edit and retrieval across CLI processes. After the prompt update and GUI restart, supplied output shows a saved target-role fact (`junior java developer roles`, category `target roles`) alongside the edited skills fact. This establishes GUI save/view success based on user evidence; semantic duplicate and accepted-conflict live checks remain open. No recording URL was supplied.
