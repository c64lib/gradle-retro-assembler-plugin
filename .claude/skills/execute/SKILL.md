---
description: Implement a development action plan created by the plan skill. Locates a plan in plans/, offers an adversarial challenge review before executing, lets the user pick scope (all / a phase / specific steps / a range) and an engagement mode (per-step, per-phase, or autonomous), executes each step following the repo's architecture, verifies deliverables, records progress back into the plan via the plan skill, keeps a per-plan execution log (plans/EXEC-nnnn_{slug}.md) with all deviations, and afterwards offers to sync the linked issue description and then close the issue. Invoke for "/execute", "execute the plan", "implement phase N", "run the action plan".
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

- Header: `**Plan ID**`, `**Issue**`, `**Status**`.
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

**Offer an adversarial challenge**: before executing, ask the user (via `AskUserQuestion`) whether to run an adversarial **`challenge`** review of the plan first (mode A — red-team the plan). If they accept, invoke `Skill(skill: "challenge", ...)` on the plan, relay its findings, and let the user decide whether to revise the plan (hand back to `/plan update`) or proceed with execution as-is. If they decline, continue. This is an explicit offer, not a default — never run the challenge silently, and never block execution on it.

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

**Offer the issue sync**: if the plan is linked to a GitHub issue, ask the user (via `AskUserQuestion`) whether to update the issue's description with the actual, post-execution plan content. Only if they accept, have the update pushed to the issue (via the `plan` skill's issue sync / `gh issue edit`). Never sync the issue silently.

**Offer to close the issue**: only after the issue has been updated with the plan (i.e. the sync above was accepted and pushed) **and** the plan is fully `implemented` (every step complete), ask the user (via `AskUserQuestion`) whether to close the linked issue. If they accept, close it via `gh issue close <number> -c "<comment>"` with a short comment referencing the completed work (and the PR when one exists). Never close the issue silently, and do not offer to close it while steps remain pending or the sync was declined.

### Step 8 — Summarise and offer follow-ups

Report: steps completed, steps skipped/blocked (with reasons), steps still pending, the verification results, and any deviations logged (point at the exec log). Then offer git follow-ups by delegating to the existing skills — commit via **`git-utils`**, open a PR via **`gh-utils`** — only if the user wants them. Never commit or push automatically.

---

## Guardrails

- **Never create or restructure a plan** — that's the `plan` skill. This skill only executes and records progress.
- **All plan-file writes go through the `plan` skill's UPDATE**, not direct edits — except the exec log (`plans/EXEC-*.md`) and its `Exec` index cell, which this skill owns and writes directly.
- **Keep the exec log honest and current** — session logged when execution starts, step rows as steps finish, deviations the moment they occur.
- **Never update the linked issue's description without asking** — the issue sync in Step 7 is an explicit offer, not a default.
- **Never close the linked issue without asking**, and only offer to close it once the issue has been updated with the plan and the plan is fully `implemented` — see Step 7.
- **The adversarial challenge is an offer, not a gate** — offer it in Step 2, never run it silently, and never block execution on it.
- **Never run `./gradlew` inline** — delegate every Gradle run to `build` / `test` / `e2e-test`.
- **Stay within the selected scope** — implement only the steps the user chose.
- **Never commit or push without an explicit request**; delegate git to `git-utils` / `gh-utils`.
- If a step's instructions are ambiguous, ask before implementing — don't guess.
