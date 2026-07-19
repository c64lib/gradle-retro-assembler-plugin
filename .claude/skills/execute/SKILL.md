---
description: Implement a development action plan created by the plan skill. Locates a plan in plans/, offers an adversarial challenge review before executing, ensures work happens on a feature branch, lets the user pick scope (all / a phase / specific steps / a range) and an engagement mode (per-step, per-phase, or autonomous), executes each step following the repo's architecture, verifies deliverables, records progress back into the plan via the plan skill, keeps a per-plan execution log (plans/EXEC-nnnn_{slug}.md) with all deviations, and afterwards ships the work in order — commit and push onto the branch, open a pull request, sync the linked issue description, then close the issue. Invoke for "/execute", "execute the plan", "implement phase N", "run the action plan".
user-invocable: true
allowed-tools: Agent Skill Bash Read Edit Write Grep Glob AskUserQuestion TodoWrite
---

# Execute Skill

Drives the implementation of a development action plan produced by the **`plan`** skill. This skill owns execution; it does **not** create or restructure plans — it reads a plan from `plans/`, implements the selected steps, and delegates all plan-file writeback to the `plan` skill's UPDATE operation. It is the single user-invocable entry point: `/execute`.

## Trigger

Invoke when the user wants to implement work described in an existing plan: "/execute", "execute the plan", "implement Phase 2", "run steps 1.1–1.3", "start building the plan".

If no plan exists yet, this is a planning task — hand off to the `plan` skill (`/plan`) instead.

---

## Plan source and format

Plans live in **`plans/PLAN-nnnn_{slug}.md`** and follow the canonical template
(`.claude/templates/plan.template.md`). The parts this skill reads:

- Header: `**Plan ID**`, `**Issue**`, `**Status**`, `**Challenge**` (whether an adversarial challenge was run — see Step 2).
- **Section 5 — Implementation Plan**: `### Phase N: {name}` blocks, each containing numbered `**Step N.M**: {action}` entries with `Files:`, `Description:`, `Testing:` fields and a `**Phase N Deliverable**` line.

This skill targets `plans/` only. It does not read or execute legacy `.ai/` plans.

---

## Execution log (owned by this skill)

Every executed plan gets a companion **execution log** next to it:

```
plans/EXEC-{nnnn}_{slug}.md        (same nnnn and slug as PLAN-{nnnn}_{slug}.md)
```

- Based on the template `.claude/templates/exec.template.md` — follow it exactly (header, Execution Sessions, Deviations from Plan, Follow-ups).
- **Created by this skill** when execution of a plan starts for the first time; later runs append a new `### Session` block, never rewrite past sessions.
- Records, per session: scope, engagement mode, outcome, and a per-step table (result, verification evidence, commit SHAs/notes).
- Records **all deviations found during execution** in Section 2: different files touched than planned, changed approach, unplanned extra work (environment or tooling fixes, quirks discovered), steps merged or reordered, plan assumptions that proved wrong. Write a deviation down at the moment it happens, not retroactively at the end.
- The plan file keeps only concise per-step completion markers (via the `plan` skill); the narrative of *how* execution went lives in the exec log.
- **Indexing**: the exec log is linked from the *same row* as its plan in `plans/README.md`, in the `Exec` column (`—` until the log exists). This skill fills that cell when it creates the log.

The exec log and its index cell are the one exception to the "all plan I/O goes through the `plan` skill" rule — this skill owns them and writes them directly.

---

## Workflow

### Step 1 — Locate the plan

1. List `plans/` (or read `plans/README.md`). If empty, report there are no plans and offer to create one via `/plan`.
2. Use the current git branch (`feature/{issue}-{slug}`) as a strong hint to auto-suggest the matching plan by issue number/slug.
3. Confirm the target with the user via `AskUserQuestion` if there is any ambiguity; accept an explicit path otherwise.
4. Read the full plan file before doing anything else.

### Step 2 — Summarise, parse, and check plan status

Read the plan's `**Status**` header. Execution is only appropriate for an **`accepted`** or already **`in progress`** plan:
- `draft` — not yet approved. Do not execute; tell the user the plan must be accepted first (via `/plan update` → `accepted`, which requires all open questions answered), and stop.
- `implemented` or `rejected` — terminal/historical. Do not execute; if the user wants further work, they should create a new plan.

Parse Section 5 into phases and steps. Present a summary:
- Each phase name and its steps (`N.M`), with each step's deliverable and testing note.
- Current status of each step, inferred from checkboxes / completion markers already in the plan (pending / completed / skipped / blocked).

**Offer an adversarial challenge (only if not already challenged)**: the primary place to red-team a plan is at acceptance (the `plan` skill's `→ accepted` transition). This skill offers it only as a **fallback**, when the plan shows no challenge was run. Read the plan's `**Challenge**:` header field (the canonical four-value enum is defined in the `plan` skill's *Challenge Field* section):

- If the field **starts with `not run`**, or the field is **missing** (treat missing as `not run` — fail safe toward offering), offer the challenge: ask the user (via `AskUserQuestion`) whether to run an adversarial **`challenge`** review first (mode A — red-team the plan).
- If the field starts with `passed`/`revised`/`waived`, a challenge was already handled at acceptance — note that and **skip the offer** (the user may still request one explicitly).

If the user accepts the offer, invoke `Skill(skill: "challenge", ...)` on the plan, relay its findings, and let the user decide whether to revise the plan (hand back to `/plan update`) or proceed with execution as-is. **Close the loop**: after a fallback challenge runs, record the outcome by delegating to the `plan` skill's UPDATE to set `**Challenge**:` to `revised {date}` (plan was updated to address findings), `passed {date}` (run, no changes needed), or `waived {date}` (offered again and declined) — do not hand-edit the field directly (all plan-file writes go through the `plan` skill). Without this write-back the field stays `not run` and this offer would recur on every run.

This is an explicit offer, not a default — never run the challenge silently, and never block execution on it.

### Step 3 — Choose scope

Ask (via `AskUserQuestion`) what to execute:
- **All** pending steps
- A **single phase** (e.g. "Phase 1")
- **Specific steps** (e.g. "1.1, 1.2, 2.1")
- A **range** (e.g. "1.1 to 2.3")

Parse the answer and echo back the exact ordered list of steps that will run.

### Step 4 — Choose engagement mode

Ask (via `AskUserQuestion`):
- **Per-step** — pause for confirmation after each step
- **Per-phase** — pause for confirmation after each phase
- **Autonomous** — run the whole scope without pausing

### Step 5 — Execute

**Ensure a feature branch first.** Check the current git branch. If it is not the plan's `feature/{issue}-{slug}` branch (and not another feature branch the user has directed you to use), do not implement onto `develop`/`master`: ask the user (via `AskUserQuestion`) whether to create `feature/{issue}-{slug}` now, and if they accept, delegate to the **`git-utils`** skill to create it (branched from an up-to-date `develop`). The `plan` skill may already have created this branch on acceptance; if so, just confirm you are on it. Never run `git` directly.

Open the execution log: if `plans/EXEC-{nnnn}_{slug}.md` does not exist, create it from `.claude/templates/exec.template.md` and fill the `Exec` cell of the plan's row in `plans/README.md`; then append a new `### Session` block with the chosen scope and mode.

Create a `TodoWrite` list with one entry per in-scope step. Then, for each step in order:

1. Mark it `in_progress` in the todo list. Show the step's action, deliverable, and testing note.
2. Implement the change, following `CLAUDE.md`: hexagonal architecture (ports hide technology, use cases are single `apply`-method classes), add a new module as a `compileOnly` dependency in `infra/gradle`, and use the Gradle Workers API for parallelism. Reference real code with `file_path:line_number`.
3. **Verify the deliverable** using the step's `Testing:` note. Do not run Gradle inline — delegate:
   - Compile / full build / a specific Gradle task → **`build`** skill.
   - "Run the tests / report failures" → **`test`** skill.
   - End-to-end behaviour against the tony harness → **`e2e-test`** skill.
   - Confirm the change actually works at runtime (non-trivial product changes) → **`verify`** skill.
   Report the verification outcome; on failure, surface the failing task and the relevant error lines with `file:line`.
4. Mark the step `completed` once verified, and add its row to the current session's step table in the exec log (result, verification evidence, notes).
5. **Log deviations as they happen**: whenever reality departs from the plan as written — different files, changed approach, unplanned work, tooling/environment fixes, wrong plan assumptions — append a row to the exec log's *Deviations from Plan* table immediately.
6. Honour the engagement mode: after each step (per-step) or each completed phase (per-phase), ask whether to continue / stop / skip the next item; in autonomous mode continue without asking.

### Step 6 — Handle blockers

If a step fails or can't be completed: document what went wrong (with error output), and ask the user to **retry**, **change approach and retry**, or **skip**. Record skipped/blocked steps with their reason for the writeback, and log the blocker (and any approach change) in the exec log — a changed approach is a deviation. Never leave a step half-done and silently move on.

### Step 7 — Record progress (delegate to the `plan` skill)

After the run (completed or stopped), write results back by invoking the **`plan`** skill's UPDATE operation:

```
Skill(skill: "plan", args: "update")
```

Provide it the plan path and the change set so it updates the plan consistently: mark completed steps `- [x]`, annotate skipped/blocked steps with reasons, add a Revision-History row, and update the `plans/README.md` index if status changed. Include the correct **plan-level Status transition** (canonical values from the `plan` skill's Status Lifecycle): set `in progress` when execution begins on an `accepted` plan, and `implemented` once every step in the plan is complete. Leave it `in progress` if steps remain. Do **not** hand-edit the plan file or the index status directly — that is the `plan` skill's job, so plan I/O stays in one place (the exec log and its index cell are this skill's own, see *Execution log*).

Also close out the exec log for this run: set the session's Outcome, update `**Last Updated**` and `**State**`, and make sure Sections 2 (Deviations) and 3 (Follow-ups) reflect everything encountered ("None" is an acceptable entry).

### Step 8 — Ship the work: commit, push, PR, then close the issue

Once the in-scope work is recorded, drive it toward a reviewable, closeable state **in this order**. Each hand-off to git/GitHub is an explicit offer via `AskUserQuestion` — never commit, push, open a PR, sync, or close silently.

1. **Commit & push onto the feature branch.** Offer to commit the changes and push them, delegating to the **`git-utils`** skill (which commits per this repo's convention and, on push, sets upstream on `feature/{issue}-{slug}`). Do not run `git` directly. If the user declines, stop here — the remaining steps depend on pushed work.
2. **Create the pull request.** After the branch is pushed, offer to open a PR into `develop` by delegating to the **`gh-utils`** skill. Draft a title and body that reference the linked issue (e.g. `Closes #{issue}`). Report the PR URL. If a PR already exists for the branch, report that instead of creating a duplicate.
3. **Sync the issue description.** If the plan is linked to a GitHub issue, offer to update the issue's description with the actual, post-execution plan content (via the `plan` skill's issue sync / `gh issue edit`). Never sync silently.
4. **Close the issue.** Only after **(a)** a PR exists for the branch (step 2 accepted, or one already open), **(b)** the issue has been synced with the plan (step 3 accepted and pushed), **and (c)** the plan is fully `implemented` (every step complete), offer to close the linked issue. If the user accepts, close it via `gh issue close <number> -c "<comment>"` with a short comment referencing the completed work and the PR URL. Do not offer to close while steps remain pending, no PR exists, or the sync was declined.

### Step 9 — Summarise

Report: steps completed, steps skipped/blocked (with reasons), steps still pending, the verification results, and any deviations logged (point at the exec log). Note which ship steps (commit, push, PR, issue sync, issue close) were done, declined, or left pending, with the PR URL when one was created.

---

## Guardrails

- **Never create or restructure a plan** — that's the `plan` skill. This skill only executes and records progress.
- **All plan-file writes go through the `plan` skill's UPDATE**, not direct edits — except the exec log (`plans/EXEC-*.md`) and its `Exec` index cell, which this skill owns and writes directly.
- **Keep the exec log honest and current** — session logged when execution starts, step rows as steps finish, deviations the moment they occur.
- **Implement on a feature branch, not `develop`/`master`** — ensure `feature/{issue}-{slug}` exists (create via `git-utils` if needed) before executing steps — see Step 5.
- **Ship in order: commit → push → PR → issue sync → issue close** (Step 8). Each is an explicit `AskUserQuestion` offer; never do any of them silently or automatically.
- **Never update the linked issue's description without asking** — the issue sync in Step 8 is an explicit offer, not a default.
- **Never close the linked issue without asking**, and only offer to close it once a PR exists, the issue has been synced with the plan, and the plan is fully `implemented` — see Step 8.
- **The adversarial challenge is an offer, not a gate** — and only a **fallback** offer: in Step 2, offer it only when the plan's `**Challenge**:` field starts with `not run` (missing ⇒ `not run`); skip it when a challenge was already handled at acceptance. After a fallback challenge runs, write the `**Challenge**:` field back via the `plan` skill. Never run the challenge silently, and never block execution on it.
- **Never run `./gradlew` inline** — delegate every Gradle run to `build` / `test` / `e2e-test`.
- **Stay within the selected scope** — implement only the steps the user chose.
- **Never run `git`/`gh` directly** — delegate all branch/commit/push/PR/status work to `git-utils` and `gh-utils`.
- If a step's instructions are ambiguous, ask before implementing — don't guess.
