---
description: Create, update, and list structured development action plans in plans/. Runs the full interactive planning workflow (codebase analysis, clarifying questions, refinement) and handles all plan file I/O, index maintenance, and GitHub issue sync. "plan #xyz" reads GitHub issue xyz and starts planning from its description; when planning begins on a linked issue, offers to move it to In Progress on the c64lib project board. On acceptance, offers to create the plan's feature branch. Invoke for "/plan", "plan #123", "create a plan", "update the plan", "list plans".
user-invocable: true
---

# Plan Skill

This skill owns the complete development-action-plan lifecycle for this project: the **interactive planning workflow** (analysis, clarifying questions, refinement) and the **mechanical file I/O** (create/update/list, index maintenance, GitHub issue sync). It is the single entry point — invoke it directly as `/plan`, `/plan update`, or `/plan list`.

## Trigger

Invoke when the user wants to:
- **Create** a new development action plan for a feature or fix
- **Update** an existing plan (change scope, answer open questions, record decisions, mark progress)
- **List** all plans via the index

Dispatch by intent: a new feature/fix → CREATE; a reference to an existing plan → UPDATE; "list/show plans" → LIST. Ask only if genuinely ambiguous.

**`plan #xyz` shorthand.** When the user says **"plan #xyz"** (or "plan issue xyz" / "/plan #xyz"), this is a CREATE keyed to GitHub issue `xyz`: read that issue and start planning from its description. Do not ask for the issue number or the task specification — they come from the issue. Concretely, in CREATE Step 1, treat `xyz` as the **existing issue number** and derive the **task specification** from the issue body (title + description); still ask for a feature short name if one can't be sensibly derived from the issue title. Then, per CREATE Step 1a, propose moving the issue to **In Progress** on the project board.

---

## Status Lifecycle

Every plan carries a `**Status**` field whose value is one of exactly these five, and the `plans/README.md` index `Status` column mirrors it verbatim:

| Status | Meaning | Kind |
|--------|---------|------|
| `draft` | Newly created; still being written/refined. May have unresolved questions. | active |
| `accepted` | Reviewed and approved; ready to implement. **All open questions must be answered to reach this state.** | active |
| `in progress` | Implementation underway (steps being executed). | active |
| `implemented` | Work is done and merged. | terminal / historical |
| `rejected` | Abandoned; will not be implemented. | terminal / historical |

Normal flow: `draft → accepted → in progress → implemented`. A plan may go to `rejected` from any active state.

**Terminal states are historical.** Once a plan is `implemented` or `rejected` it is a historical record: it is not kept in sync with the codebase and **is allowed to go stale**. Do not "freshen" or re-verify terminal plans, and do not re-open one — create a new plan instead. Terminal plans are never deleted (see Storage Rules); they stay in `plans/` and the index as history.

**Acceptance gate (`→ accepted`).** A plan may only transition to `accepted` when its `### Unresolved Questions` list is empty (every question answered and moved to `### Self-Reflection Questions`). If any remain, do not set `accepted` — resolve them first (via the UPDATE rules), then retry. On acceptance, also offer the adversarial challenge (see UPDATE Step 4b), ask the user whether the plan should be copied onto the linked GitHub issue's description (see UPDATE Step 4), and offer to create the feature branch the work will be implemented on (see UPDATE Step 4a).

---

## Challenge Field

Every plan also carries a `**Challenge**:` header field recording whether the adversarial **`challenge`** review (mode A — red-team the plan) has been run. **This section is the single, canonical source of truth for the `**Challenge**:` vocabulary** — the plan template, the `execute` skill, and any other asset must mirror it, not redefine it.

Its value is one of exactly these four (a **closed enum**, mirroring how `**Status**` is defined):

| Value | Meaning |
|-------|---------|
| `not run` | No challenge has been run yet. This is the default on creation. |
| `passed {YYYY-MM-DD}` | A challenge was run and the plan needed no changes. |
| `revised {YYYY-MM-DD}` | A challenge was run and the plan was updated to address its findings. |
| `waived {YYYY-MM-DD}` | The challenge was explicitly offered and declined. |

The challenge is **offered, never forced** — it is not a gate on acceptance or execution. New plans start at `not run`. The field is written at acceptance (UPDATE Step 4b) or, as a fallback, during execution (the `execute` skill writes it back after a fallback challenge). Findings are summarised in the plan's `### Adversarial Challenge` subsection (section 4). The `execute` skill re-offers the challenge only when the field **starts with `not run`** (a missing field is treated as `not run`), so an already-challenged plan is not re-challenged by default.

---

## OPERATION: CREATE

Produce a comprehensive, refined plan and persist it.

### Step 1 — Gather the essentials

Collect (use `AskUserQuestion` for anything not already supplied):

1. **Issue number** — the GitHub issue this addresses. If the user has not supplied one, do not silently default to unlinked — **ask** (via `AskUserQuestion`) whether the work maps to an existing issue, and offer these options:
   - **Existing issue** — the user provides the number (or gave it via the `plan #xyz` shorthand); use it and sync per Step 6. **Read the issue body now** with `gh issue view {n} --repo c64lib/gradle-retro-assembler-plugin --json title,body` — the issue's title and description are the primary source for the task specification below. Then proceed to Step 1a (propose In Progress).
   - **Create a new issue** — if the user picks this, create one via `gh issue create` (owner `c64lib`, repo `gradle-retro-assembler-plugin`) with a concise title and a short body derived from the task specification, then use the returned number as the plan's linked issue. Confirm the drafted title/body with the user before creating it. Then proceed to Step 1a.
   - **No issue (`N/A`)** — proceed unlinked only when the user explicitly chooses this. Skip Step 1a (no issue to move).
2. **Feature short name** — a kebab-case slug (e.g. `pipeline-dsl-parallel-execution`). When keyed to an issue, derive a sensible default from the issue title and confirm it; only ask outright if none can be derived.
3. **Task specification** — what needs to be built or fixed. When keyed to an existing issue, this comes from the **issue body** just read; do not re-ask the user for it (ask only to clarify genuine gaps).

### Step 1a — Propose moving the issue to In Progress (linked issues only)

As soon as planning begins on an existing (or freshly created) issue, the issue should reflect that work has started. **Only when the plan is linked to an issue**, before diving into analysis:

1. Ask the user (via `AskUserQuestion`) **"Move issue #{issue-number} to In Progress on the c64lib project board now?"**
2. If the user accepts, move the issue's project-board **Status** field to **In Progress** using the `gh project` CLI (the GitHub Projects API, which requires the `read:project` **and** `project` token scopes). The issue number is **not** enough — you must resolve the project, the item, and the field/option ids first. Run, in order:
   1. `gh project list --owner c64lib` → pick the target project; note its **number**. (If exactly one project exists, use it; if several, ask the user which board; if none, tell the user there is no board to update and skip.)
   2. `gh project item-list {project-number} --owner c64lib --format json` → find the item whose `content.number` equals `{issue-number}`; note its **item id**. (If the issue is not yet on the board, add it with `gh project item-add {project-number} --owner c64lib --url {issue-url}` and re-list.)
   3. `gh project field-list {project-number} --owner c64lib --format json` → find the single-select **Status** field; note its **field id** and the **option id** of the `In Progress` option (match by name).
   4. `gh project item-edit --project-id {project-id} --id {item-id} --field-id {field-id} --single-select-option-id {option-id}` to set the status, then confirm the new status to the user. (`gh project item-list`/`field-list` also expose the project **node id** needed for `--project-id`.)
   - **If any of these fail on missing scopes** (the current token has only `repo`/`read:org`/etc.), do **not** block planning. Tell the user exactly how to enable it:
     ```
     gh auth refresh -s read:project,project
     ```
     Then offer to retry the move after they've refreshed, and note they can alternatively move the card manually on the board. Proceed with planning either way.
3. If the user declines, leave the board untouched and continue.

This is an explicit offer, never automatic — never move the issue silently, and never block planning on the board update or on missing scopes.

### Step 2 — Analyse the codebase

Do the research before writing the plan — do not guess:

1. Use the **Explore** agent to map the affected domain modules, current architecture/patterns, related existing code, and test structure.
2. Read the actual relevant files to understand conventions and integration points.
3. Review `CLAUDE.md` for architectural guidelines (hexagonal architecture, use-case/port patterns, `infra/gradle` `compileOnly` rule, Gradle Workers API for parallelism).
4. Reference real code locations as `file_path:line_number`.

### Step 3 — Determine the next plan ID

Run:
```bash
ls plans/ 2>/dev/null | grep -E '^PLAN-[0-9]+_' | sort | tail -1
```
Extract the numeric part, increment, zero-pad to 4 digits. If none exist, start at `PLAN-0001`.

### Step 4 — Write the plan from the template

Read `.claude/templates/plan.template.md`, fill in the header placeholders (`{nnnn}`, `{issue-number}`, `{Feature Name}`, `{YYYY-MM-DD}`), and complete the body sections from the Step 2 analysis. New plans keep the template's `**Challenge**: not run` header field and its `### Adversarial Challenge` subsection (section 4) as-is — a challenge has not been run yet (see *Challenge Field*). Write to:
```
plans/PLAN-{nnnn}_{feature-short-name}.md
```
The template is the canonical structure — follow it exactly (Feature Description, Root Cause Analysis, Relevant Code Parts, Questions/Decisions, phased Implementation Plan with mergeable deliverables, Testing Strategy, Risks, Documentation, Rollout).

### Step 5 — Update the index

Read `plans/README.md`. If missing, create it:
```markdown
# Plans Index

Structured development action plans for this project.
Plans are permanent artifacts — do not delete. Terminal plans (`implemented`, `rejected`) are kept as history.

| ID | Date | Status | Title | Issue | Exec |
|----|------|--------|-------|-------|------|
```
Append (new plans always start as `draft`):
```
| [PLAN-{nnnn}](PLAN-{nnnn}_{slug}.md) | {YYYY-MM-DD} | draft | {Feature Name} | #{issue-number} | — |
```
Use `—` in the Issue column when unlinked. The `Exec` column stays `—` until the `execute` skill creates the plan's execution log (`plans/EXEC-{nnnn}_{slug}.md`); that skill owns the exec log and fills this cell with its link — this skill never writes exec logs, it only preserves the column.

### Step 6 — Sync to the GitHub issue (if linked)

1. Read the current issue body with `gh issue view {issue-number} --repo c64lib/gradle-retro-assembler-plugin --json title,body`.
2. Preserve the user's original issue description by prepending it into the plan's **Section 1** before any generated content.
3. Replace the issue body with the full plan via `gh issue edit {issue-number} --repo c64lib/gradle-retro-assembler-plugin --body-file {file}` (write the plan body to a temp file to avoid shell-quoting issues).

### Step 7 — Interactive refinement (continuous architectural Q&A)

Refine the plan through an open-ended, section-by-section dialogue — act as an architect, not a form-filler. Do not stop after one pass; keep the conversation going until the user signals they are done.

Run this loop:

1. Present a plan section (or the freshly surfaced **Unresolved Questions** / **Design Decisions**).
2. Ask **2–3 specific, focused questions** about it — one aspect at a time — that uncover hidden requirements, module boundaries, port interfaces, data flow, and scalability/performance/maintainability/testing concerns. Prefer `AskUserQuestion`.
3. Incorporate each answer immediately via the UPDATE rules (below): move resolved items to answered, record decisions as chosen option + rationale, and propagate the change to every affected section (implementation steps, architecture alignment, risks, testing) so the plan stays consistent.
4. Ask follow-ups when an answer raises new considerations; move to the next section otherwise.
5. **Continue until the user explicitly indicates they are satisfied** ("that's good", "I'm satisfied", "let's proceed", "stop asking", "that's enough"). Only then conclude.

Keep grounded in the real codebase throughout — reference actual classes, patterns, and `file_path:line_number` locations, and hold to the hexagonal-architecture constraints (ports isolate technology, single-`apply` use cases, `infra/gradle` `compileOnly` for new modules, Gradle Workers API for parallelism, coverage targets). Acknowledge trade-offs rather than prescribing.

When the user is done, summarise the final plan, remind them it is still `draft` (acceptance requires all Unresolved Questions answered — see *Status Lifecycle*), and suggest next steps (accept via `/plan update`, then `/execute`).

Report: "Plan PLAN-{nnnn} written to `plans/PLAN-{nnnn}_{slug}.md`, index updated" (and, if linked, "issue #{issue-number} body replaced with plan content").

---

## OPERATION: UPDATE

Update an existing plan, keeping every affected section consistent.

### Step 1 — Locate the plan

Identify the target plan. Use the current branch name (`feature/{issue}-{slug}`) as a hint, or list `plans/` and ask via `AskUserQuestion` if ambiguous. If the plan genuinely doesn't exist, offer to CREATE one instead.

### Step 2 — Read the full plan

Read the entire file before changing anything.

### Step 3 — Determine the update scope

Ask (via `AskUserQuestion`) what is changing if not already clear. Common scopes: specification/requirement changes, answered open questions, added acceptance criteria, refined execution steps, testing-strategy changes, architecture refinements, status/progress. If the input is vague, ask targeted follow-ups before proceeding.

### Step 4 — Apply the updates consistently

Apply the change **and propagate its impact** across all relevant sections — an answer that changes approach must update the implementation steps, architecture alignment, risks, and testing as needed.

**Answered questions:**
- Move from `### Unresolved Questions` to `### Self-Reflection Questions`
- Format: `- **Q**: {question}` / `  - **A**: {answer}`

**Design decisions:**
```
- **Chosen**: {option}
- **Rationale**: {why}
```

**Status transitions:**
- Set the `**Status**` field to one of the five canonical values (see *Status Lifecycle*); add `**Last Updated**: {YYYY-MM-DD}`.
- **`→ accepted`:** first confirm `### Unresolved Questions` is empty. If any remain, refuse the transition and resolve them (answer + move to Self-Reflection) before setting `accepted`. On success, ask the user (via `AskUserQuestion`) **"Copy this accepted plan onto issue #{issue-number}'s description?"** — if yes and the plan is linked, replace the issue body with the full plan content in Step 7; if no, leave the issue untouched. (For unlinked plans, skip the prompt.)
- **`→ in progress`:** mark steps `- [x]` as they complete.
- **`→ implemented` / `→ rejected`:** these are terminal. Record the outcome, then treat the plan as historical — no further syncing or freshening. Do not transition out of a terminal state; create a new plan instead.

Preserve historical context — don't delete answered questions or superseded content unless explicitly asked. Maintain the template's exact section structure and numbering.

### Step 4a — Propose the feature branch (on `→ accepted`)

An accepted plan is ready to implement, so it should have a feature branch to be implemented on. **Only on the `→ accepted` transition**, after the acceptance gate passes:

1. Check the current git branch. If it is already the plan's `feature/{issue}-{slug}` branch, note that and skip creation.
2. Otherwise ask the user (via `AskUserQuestion`) **"Create the feature branch `feature/{issue}-{slug}` for this plan now?"** — deriving `{issue}` from the plan's linked issue (omit it for unlinked plans → `feature/{slug}`) and `{slug}` from the plan's feature short name.
3. If the user accepts, delegate branch creation to the **`git-utils`** skill (which branches from an up-to-date `develop` as `feature/{issue}-{slug}`). Do not run `git` directly.
4. If the user declines, leave the branch as-is; the `execute` skill can still create it later.

This is an explicit offer, not a gate — never create the branch silently, and never block acceptance on it.

### Step 4b — Offer the adversarial challenge (on `→ accepted`)

An accepted plan is the point of maximum revisability, so it is the natural place to red-team it. **Only on the `→ accepted` transition**, after the acceptance gate passes and before finalising acceptance:

1. If the plan's `**Challenge**:` field already starts with `passed`/`revised`/`waived`, a challenge was already handled — note that and skip the offer.
2. Otherwise ask the user (via `AskUserQuestion`) **"Run an adversarial challenge (red-team) on this plan before accepting?"**
3. If the user accepts, invoke `Skill(skill: "challenge", ...)` on the plan (mode A — red-team the plan), relay the findings, and let the user choose to **revise** the plan (apply the findings via these same UPDATE rules) or **accept as-is**:
   - If the plan was revised to address findings, set `**Challenge**: revised {YYYY-MM-DD}`.
   - If it was run and needed no changes, set `**Challenge**: passed {YYYY-MM-DD}`.
   - Summarise the findings (and how they were addressed) in the `### Adversarial Challenge` subsection (section 4).
4. If the user declines, set `**Challenge**: waived {YYYY-MM-DD}` and note "declined at acceptance" in the `### Adversarial Challenge` subsection.

Use only the canonical four values (see *Challenge Field*). This is an explicit offer, **not a gate** — never run the challenge silently, and never block acceptance on it. The `challenge` skill only critiques; this skill records the outcome.

### Step 5 — Revision history

Add/append Section 10 (before the final note):
```markdown
## 10. Revision History

| Date | Updated By | Changes |
|------|------------|---------|
| {YYYY-MM-DD} | AI Agent | {Brief description of changes} |
```

### Step 6 — Update the index

If status changed, update the `Status` column for this plan's row in `plans/README.md`.

### Step 7 — Sync to the GitHub issue (if linked)

Replace the linked issue body with the updated plan via `gh issue edit {issue-number} --repo c64lib/gradle-retro-assembler-plugin --body-file {file}` (write the plan body to a temp file first) **only when the update warrants it**:
- On an **acceptance** transition, honour the Step 4 prompt: copy onto the issue only if the user said yes.
- For other content updates to a non-terminal plan, sync as before.
- For **terminal** plans (`implemented`, `rejected`), do not push further updates to the issue — they are historical.

### Step 8 — Present changes

Show what changed (by section) and any cascading updates, confirm with the user, and suggest next steps.

Report: "Plan `{path}` updated, index updated" (and, if linked, "issue #{issue-number} body synced").

---

## OPERATION: LIST

1. Read `plans/README.md`. If missing, report: "No plans found. The `plans/` directory is empty."
2. Return the full index table.

---

## Backporting Support

If the user is logging a historical plan, accept a custom date and use it for the `**Created**` field and the index row instead of today's date. All other steps are identical.

---

## Storage Rules

- Plans live in `plans/` with sequential `PLAN-nnnn` IDs — never in `.ai/`.
- Plans are **permanent artifacts** — never delete a plan file. A plan reaches end-of-life by moving to a terminal status (`implemented` or `rejected`), not by deletion.
- **Terminal plans are historical and may go stale** — do not keep them in sync with the codebase or re-verify them; do not re-open one (create a new plan instead).
- Existing `.ai/` plans are **not migrated** — leave them in place.
- `plans/README.md` is the authoritative index — always update it on create and status-change updates.

---

## Summary

| Operation | Input | Output |
|-----------|-------|--------|
| CREATE | issue number, feature slug, spec | analysed + refined `plans/PLAN-nnnn_slug.md`, index updated, issue synced; on a linked issue, offers to move it to In Progress on the board (`plan #xyz` reads the issue as the spec) |
| UPDATE | plan path, change set | plan file updated (consistently), index updated (if status changed), issue synced; on `→ accepted`, offers the feature branch |
| LIST | — | index table from `plans/README.md` |
