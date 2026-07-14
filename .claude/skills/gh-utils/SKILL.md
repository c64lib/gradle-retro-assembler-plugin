---
name: gh-utils
description: Standardise GitHub operations in this repo by wrapping the gh CLI. Headline operation is creating a pull request from the current feature/<issue>-<slug> branch into develop; also covers viewing/listing PRs and checking PR/CI status. Delegates the actual gh commands to a spawned Haiku subagent. Use when asked to open a pull request, view/list PRs, or check PR status.
allowed-tools: Agent Bash Read Grep
---

# gh-utils

Wraps the `gh` CLI to make GitHub operations in this repo follow one convention. As with `git-utils`, the **main agent decides what to do** (open a PR, list PRs, check status) and drafts any PR content; a **spawned Haiku subagent runs the mechanical `gh` commands** and reports back (e.g. the created PR URL).

**Boundary with `git-utils`:** `git-utils` owns local operations — branch, pull, commit, push. `gh-utils` owns GitHub-side operations — pull requests and other `gh` tasks. PR creation assumes the feature branch is already **pushed**; if it is not, hand off to `git-utils` for the push first, then create the PR here.

## How execution works: spawn a Haiku subagent

For every operation that runs `gh` commands, spawn a subagent on the **Haiku** model via the `Agent` tool:

```
Agent(
  subagent_type: "general-purpose",
  model: "haiku",
  run_in_background: false,
  description: "<short label>",
  prompt: "<the exact gh command(s) to run, plus what to report back>"
)
```

The main agent works out the operation and drafts any content (PR title/body) using the rules below, then gives the subagent a precise, self-contained task: the literal `gh` command(s) and the exact output to return (e.g. the PR URL). The subagent executes; it does not decide titles, bodies, or base branches. Run synchronously (`run_in_background: false`) and relay the result to the user.

## Operations

### 1. Create a pull request (headline)

- Create a PR from the current **`feature/<issue>-<slug>`** branch into **`develop`** (this repo's integration branch; `develop`→`master` release merges are done separately).
- Precondition: the branch is pushed to `origin` with upstream set. If not, hand off to `git-utils` to push first.
- The main agent drafts the **title** (concise, imperative) and the **body** (what changed and why; reference the GitHub issue number for issue-driven work, e.g. `Closes #142`). The body ends with the required generated-with trailer (per environment conventions):
  ```
  🤖 Generated with [Claude Code](https://claude.com/claude-code)
  ```
- Subagent command (typical):
  ```
  gh pr create --base develop --head feature/<issue>-<slug> --title "<title>" --body "<body>"
  ```
  Report the created PR URL. If a PR already exists for the branch, report that instead of creating a duplicate.

### 2. View / list PRs

- View the current branch's PR or a specific one:
  ```
  gh pr view [<number>] [--web]
  gh pr list
  ```
  Report the PR summary or the list.

### 3. Check PR / CI status

- Check the current PR's checks and status:
  ```
  gh pr checks [<number>]
  gh pr status
  ```
  Report pass/fail of checks and the review state.

### 4. Other typical `gh` tasks (menu, not exhaustive)

- `gh pr diff [<number>]` — inspect the PR diff.
- `gh repo view [--web]` — open/inspect the repo.
- `gh issue list` / `gh issue view <number>` — issues, when relevant.

Treat this as a documented menu: run the specific `gh` command the task needs via the subagent, and report its output.

## Guardrails

- **Never merge or close a PR** (`gh pr merge`, `gh pr close`) without an explicit request from the user.
- **Base branch is `develop`.** Confirm the PR targets `develop` unless the user names another base (e.g. a `develop`→`master` release PR into `master`).
- **Do not create a PR from `develop` or `master` itself** — PRs come from a `feature/<issue>-<slug>` branch.
- **Do not create a duplicate PR** — if one already exists for the branch, report it.
- If a `gh` command fails (e.g. not authenticated, branch not pushed), report the failure and what would unblock it — do not retry blindly.
