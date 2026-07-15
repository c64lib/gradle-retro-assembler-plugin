---
description: Implement a development action plan created by the plan skill. Locates a plan in plans/, lets the user pick scope (all / a phase / specific steps / a range) and an engagement mode (per-step, per-phase, or autonomous), executes each step following the repo's architecture, verifies deliverables, and records progress back into the plan via the plan skill. Invoke for "/execute", "execute the plan", "implement phase N", "run the action plan".
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

Create a `TodoWrite` list with one entry per in-scope step. Then, for each step in order:

1. Mark it `in_progress` in the todo list. Show the step's action, deliverable, and testing note.
2. Implement the change, following `CLAUDE.md`: hexagonal architecture (ports hide technology, use cases are single `apply`-method classes), add a new module as a `compileOnly` dependency in `infra/gradle`, and use the Gradle Workers API for parallelism. Reference real code with `file_path:line_number`.
3. **Verify the deliverable** using the step's `Testing:` note. Do not run Gradle inline — delegate:
   - Compile / full build / a specific Gradle task → **`build`** skill.
   - "Run the tests / report failures" → **`test`** skill.
   - End-to-end behaviour against the tony harness → **`e2e-test`** skill.
   - Confirm the change actually works at runtime (non-trivial product changes) → **`verify`** skill.
   Report the verification outcome; on failure, surface the failing task and the relevant error lines with `file:line`.
4. Mark the step `completed` once verified.
5. Honour the engagement mode: after each step (per-step) or each completed phase (per-phase), ask whether to continue / stop / skip the next item; in autonomous mode continue without asking.

### Step 6 — Handle blockers

If a step fails or can't be completed: document what went wrong (with error output), and ask the user to **retry**, **change approach and retry**, or **skip**. Record skipped/blocked steps with their reason for the writeback. Never leave a step half-done and silently move on.

### Step 7 — Record progress (delegate to the `plan` skill)

After the run (completed or stopped), write results back by invoking the **`plan`** skill's UPDATE operation:

```
Skill(skill: "plan", args: "update")
```

Provide it the plan path and the change set so it updates the plan consistently: mark completed steps `- [x]`, annotate skipped/blocked steps with reasons, add a Revision-History row, update the `plans/README.md` index if status changed, and sync the linked GitHub issue. Include the correct **plan-level Status transition** (canonical values from the `plan` skill's Status Lifecycle): set `in progress` when execution begins on an `accepted` plan, and `implemented` once every step in the plan is complete. Leave it `in progress` if steps remain. Do **not** hand-edit the plan file or the index directly — that is the `plan` skill's job, so plan I/O stays in one place.

### Step 8 — Summarise and offer follow-ups

Report: steps completed, steps skipped/blocked (with reasons), steps still pending, and the verification results. Then offer git follow-ups by delegating to the existing skills — commit via **`git-utils`**, open a PR via **`gh-utils`** — only if the user wants them. Never commit or push automatically.

---

## Guardrails

- **Never create or restructure a plan** — that's the `plan` skill. This skill only executes and records progress.
- **All plan-file writes go through the `plan` skill's UPDATE**, not direct edits.
- **Never run `./gradlew` inline** — delegate every Gradle run to `build` / `test` / `e2e-test`.
- **Stay within the selected scope** — implement only the steps the user chose.
- **Never commit or push without an explicit request**; delegate git to `git-utils` / `gh-utils`.
- If a step's instructions are ambiguous, ask before implementing — don't guess.
