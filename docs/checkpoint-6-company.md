# Checkpoint 6 — F06 Company Goals and Values Research

Stage1.md F06/UC06: CompanyResearchAction calls CompanyResearchService through the shared command map. Haiku creates the company/role/year query; WebSearchTool executes through ToolManager; Jev filters each result. If no relevant results remain, Haiku reformulates once and search/filtering runs again. The service generates a summary only when relevant evidence exists and saves it against the selected job.

The generated goals and values sections must each cite an exact supplied source URL or contain exactly `not stated`. Unknown/missing citations are rejected and retried once. Markdown links and punctuation are accepted; an invented URL or extra path is not. This verifies citation provenance, not whether every statement is entailed by its cited snippet. The prompt limits generation to supplied snippets; human source review remains necessary. No page crawling was added.

WebSearchTool validates a nonblank query before requesting, uses the existing authenticated Brave GET contract, and excludes malformed HTTPS URLs and credential-bearing source URLs. Search errors remain errors; they do not create an empty-success summary or replace previous work. No key values are printed in this workflow.

Offline build passes 404 assertions. New acceptance checks cover Haiku roles, Jev relevance filtering, reformulation, relevant-only generation context, source links, restart persistence, missing/invented/extended citations with retry, all-irrelevant results, real search-adapter request shape with intercepted HTTP, encoding, unsafe source filtering, missing keys, blank queries, tool outage and preservation of existing summaries. These are fixtures; no live Brave/Jev/Claude or desktop F06 success is claimed.

## Live check

Use a real public company for research. The earlier Example Company sample is fictional and unsuitable for a successful search demonstration. This new job below is explicitly a fictional demonstration posting, not a claim of a real vacancy. No resume import is required for company research.

```sh
export CAREER_DATA=data/f06-demo.db
java -jar target/career-agent.jar job add "Software Developer" "Microsoft" CA "FICTIONAL DEMO POSTING for a Software Developer role at Microsoft. Required skills: Java and SQL."
java -jar target/career-agent.jar company
java -jar target/career-agent.jar results
java -jar target/career-agent.jar --gui
```

Needs configured Jev/Claude and BRAVE_SEARCH_API_KEY in ignored career.properties or the environment. A missing-key error is an alternative path, not successful research. Never display the configuration file or keys in the recording. If the key is absent, configure it locally before rerunning.

Expected: nonempty relevant sources with HTTPS URLs, sourced goals/values or explicit `not stated`, and the same saved company result from the separate results process. Check actual URLs/snippets against the claims. No exact summary text is promised. In GUI Preparation, click Saved results to show the retained company result, then Research company to verify the live GUI path.

A no-relevant-results outcome is a valid alternative but does not establish a successful sourced summary. If encountered, show the explanation and confirm no invented summary is created; previous saved results are intentionally retained and must not be represented as freshly generated output. Automated checks exercise this path without relying on unpredictable live search results. Finish with `unset CAREER_DATA` if returning to normal data.

## Demo and commit

Title: F06 — AI Company Goals and Values Research.
Show the selected real company and labeled demo posting, run research, inspect goals/values and sources, reopen saved results in GUI, and explain query generation, relevance decisions and source-limited summarization. Record actual alternative behavior only when observed. Add real accessible video URL/timestamps to demo-guide.md.

Commit after the live walkthrough with `feat: validate sourced company research and restore its command`. No commit or push is performed by this checkpoint document. F03–F05 are already committed as cfe23e8. GUI F05 comparison/rejection evidence and video URLs remain outstanding unless separately supplied.
