---
name: git-utils
description: Standardise routine local git operations in this repo by wrapping the git CLI. Covers feature-branch creation as feature/<issue>-<slug>, rebase-only pulls, commit messages that follow this repo's domain-based convention with proper commit granularity (related changes in one commit, unrelated changes in separate commits), and push on demand. Delegates the actual git commands to a spawned Haiku subagent. Use when asked to create a feature branch, pull, commit, or push.
allowed-tools: Agent Bash Read Grep
---

# git-utils

Wraps the `git` CLI to make routine local version-control operations in this repo follow one convention. The **main agent decides what to do** (which operation, how to group commits, what the branch is called and how to phrase the commit); a **spawned Haiku subagent runs the mechanical git commands** and reports back. This keeps fast, deterministic git work off the main agent while the skill supplies the recipe and the guardrails.

**Boundary with `gh-utils`:** `git-utils` owns local operations — branch, pull, commit, push. `gh-utils` owns GitHub-side operations — pull requests and other `gh` tasks. When a task needs both (e.g. "open a PR"), `git-utils` pushes the branch and hands off to `gh-utils` for the PR.

## How execution works: spawn a Haiku subagent

For every operation that runs git commands, spawn a subagent on the **Haiku** model via the `Agent` tool:

```
Agent(
  subagent_type: "general-purpose",
  model: "haiku",
  run_in_background: false,
  description: "<short label>",
  prompt: "<the exact git commands to run, plus what to report back>"
)
```

The main agent's job before spawning:
1. Work out the operation and its parameters (branch name, commit message, commit grouping) using the rules below.
2. Inspect state where a decision is needed (`git status --short`, `git diff`) — this can be done directly or handed to the subagent to report first.
3. Give the subagent a precise, self-contained task: the literal commands to run and the exact output to return (e.g. the new branch name, the commit SHAs, the push result). The subagent executes; it does not make grouping, naming, or message decisions.

Relay the subagent's result to the user. Run synchronously (`run_in_background: false`) so the result is available immediately.

## Operations

### 1. Create a feature branch — `feature/<issue>-<slug>`

- Branch name is **`feature/<issue>-<slug>`**, matching this repo's existing branches (e.g. `feature/142-code-coverage-phase-1`, `feature/135-pipeline-dsl-parallel-execution`):
  - `<issue>` is the GitHub issue number the work addresses. If there is no issue, omit it and use a plain `feature/<slug>` — but prefer an issue number when one exists.
  - `<slug>` is a kebab-case, ASCII-lowercase descriptor of the change (e.g. `pipeline-dsl-parallel-execution`, `code-coverage-phase-1`).
- Branch from an **up-to-date `develop`**, not from the current feature branch, unless the user explicitly asks to branch from somewhere else. `develop` is this repo's integration branch (feature PRs target `develop`; `develop`→`master` is the release merge).
- Subagent commands (typical):
  ```
  git checkout develop
  git pull --rebase
  git checkout -b feature/<issue>-<slug>
  ```
  Report the created branch name. Upstream is set on first push (see §4).

### 2. Pull — always rebase

- Pull is **always** `git pull --rebase`. Never a merge-pull; do not create merge commits from pulling.
  ```
  git pull --rebase
  ```
- If the rebase hits conflicts, stop and report — do not attempt to auto-resolve. The user resolves, then the subagent continues with `git rebase --continue`.

### 3. Commit — one commit per logical change

- Commit messages follow this repo's convention (see `CLAUDE.md` → *Commit Message Guidelines*): describe what changed and which domain was affected. For changes in a domain directory, mention the domain (e.g. `compilers`, `processors`, `flows`, `infra`). Subject is concise and imperative; a body explains what and why when the change is non-trivial.
  - Format example: `Update KickAssembleUseCase in compilers domain to support additional output formats`.
- **All changes that belong to one logical change go into a single commit.** Do not split a coherent change across several commits.
- **Two or more unrelated changes are committed separately** — one commit each. Before committing, inspect `git status --short` and `git diff`; if the working tree mixes unrelated changes, group the files by logical change and stage/commit each group on its own (`git add <group>` then `git commit`), repeating per group.
- The main agent decides the grouping and message and passes them to the subagent as explicit `git add`/`git commit` command sets; the subagent does not decide what is "related".
- Commit messages end with the repository's required trailers (per environment conventions):
  ```
  Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>
  Claude-Session: <session URL>
  ```
- Example subagent task for two unrelated changes:
  ```
  git add path/a1 path/a2
  git commit -m "<message A + trailers>"
  git add path/b1
  git commit -m "<message B + trailers>"
  ```
  Report each commit SHA and subject.

### 4. Push — on demand only

- **Never push automatically** after a commit. Push only when the user explicitly asks.
- On the **first** push of a branch, set upstream:
  ```
  git push -u origin feature/<issue>-<slug>
  ```
  Thereafter a plain `git push`. Report the push result (and the PR-creation URL git prints, if any).

## Guardrails

- **Never force-push** (`--force`/`--force-with-lease`) or **amend** (`--amend`) unless the user explicitly asks. If history needs rewriting, surface it and wait.
- **Never commit unrelated changes together** — separate them per §3.
- **Never push without an explicit request** (§4).
- **Branch from `develop`** for new feature branches (§1), not from the current branch, unless told otherwise.
- **Do not skip hooks** (`--no-verify`) or bypass signing unless the user asks.
- If a git command fails, report the failure and stop — do not retry blindly or work around it.
