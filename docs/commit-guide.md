# Current commit checkpoint

Checkpoints 1 and 2 are implemented: real Jev/Claude API adapters (with user-run live connectivity evidence), named domain entities and typed SQLite repositories (233 offline assertions passing). Other Stage 1 feature/architecture corrections remain in progress; this is an honest development snapshot, not a final submission.

The initial source tree is still untracked. A buildable first implementation commit must include its source dependencies together. The earlier five-group bulk commit instructions are superseded.

Run from the project folder in your own Terminal:

```sh
git status --short --branch
git remote -v
git add .gitignore README.md pom.xml career.properties.example src scripts docs stage2-report.pdf
git diff --cached --name-only
```

Review the staged list. It must NOT contain `career.properties`, real keys, personal databases, `target/`, or `docs/user-decisions.md`. The local configuration and personal note are ignored. The report is explicitly marked working/preliminary because later alignment work is pending.

Then make the genuine current-date commit:

```sh
git commit -m "feat: add Java core with Jev and Claude adapters and typed SQLite repositories"
```

No push has been authorized. Before pushing, verify origin is `https://github.com/AmirrezaN8/EECS3311.git`, confirm the intended branch and obtain approval. The coding session previously could not write `.git/index.lock`; no successful commit is claimed until actual Git output confirms one.

Future commits follow completed feature/test checkpoints, with bug fixes committed when appropriate. There is no prescribed total commit count. Report-only commits do not count toward the professor's meaningful code-commit minimum. Current commits cannot repair the three past weekly windows; do not backdate or fabricate history. Update final report commit hashes/links from actual Git history.
