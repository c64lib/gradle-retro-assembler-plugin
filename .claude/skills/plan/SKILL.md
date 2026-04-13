---
description: Create, update, and list structured development action plans in plans/. Syncs plan content to linked GitHub issues.
user-invocable: true
---

# Plan Skill: Mechanical Plan File I/O

This skill handles all mechanical file operations for development action plans: creating new plans from the template, updating existing plans, and maintaining the `plans/README.md` index. The planner agent delegates all file I/O here.

## Trigger

This skill is invoked by the planner agent whenever it needs to:
- **Create** a new plan file
- **Update** an existing plan file (add/remove steps, mark progress, apply answered questions)
- **List** all plans via the index

Users can also invoke this skill directly (e.g., `/plan list`, `/plan create`).

---

## Operations

### OPERATION: CREATE

Create a new plan from the canonical template.

#### Step 1 — Determine the Next Plan ID

Run:
```bash
ls plans/ 2>/dev/null | grep -E '^PLAN-[0-9]+_' | sort | tail -1
```

- Extract the numeric part (e.g., `PLAN-0001` → `0001`)
- Increment by 1 and zero-pad to 4 digits
- If no plans exist, start at `PLAN-0001`

#### Step 2 — Derive the Filename

Filename format:
```
plans/PLAN-{nnnn}_{feature-short-name}.md
```

Where `{feature-short-name}` is a kebab-case slug provided by the planner agent (e.g., `pipeline-dsl-parallel-execution`).

#### Step 3 — Read the Template

Read `.claude/templates/plan.template.md` and fill in:
- `{nnnn}` → zero-padded plan ID
- `{issue-number}` → GitHub issue number (or `N/A` if not linked)
- `{Feature Name}` → human-readable feature name
- `{YYYY-MM-DD}` → today's date

Leave all other `{placeholder}` fields as-is for the planner agent to fill in.

#### Step 4 — Write the Plan File

Write the filled template to `plans/PLAN-{nnnn}_{feature-short-name}.md`.

#### Step 5 — Update the Index

Read `plans/README.md`. If it does not exist, create it:

```markdown
# Plans Index

Structured development action plans for this project.
Plans are permanent artifacts — do not delete, only mark as `completed` or `cancelled`.

| ID | Date | Status | Title | Issue |
|----|------|--------|-------|-------|
```

Append a new row:
```
| [PLAN-{nnnn}](PLAN-{nnnn}_{slug}.md) | {YYYY-MM-DD} | Planning | {Feature Name} | #{issue-number} |
```

If not linked to an issue, use `—` for the Issue column.

#### Step 6 — Sync to GitHub Issue (if linked)

If the plan is linked to a GitHub issue:

1. Read the current issue body via `mcp__github__issue_read` (owner: `c64lib`, repo: `gradle-retro-assembler-plugin`).
2. Incorporate the original issue description into the plan's **Section 1 (Feature Description / Overview)** — prepend it before any AI-generated content in that section, preserving the user's original wording.
3. Replace the issue body with the full plan content using `mcp__github__issue_write`.

Report to the caller: "Plan PLAN-{nnnn} written to `plans/PLAN-{nnnn}_{slug}.md`, index updated, and issue #{issue-number} body replaced with plan content."

---

### OPERATION: UPDATE

Update an existing plan file.

#### Step 1 — Locate the Plan

The caller (planner agent) provides the plan file path. If ambiguous, list `plans/` and ask.

#### Step 2 — Read the Full Plan

Read the entire plan file before making any changes.

#### Step 3 — Apply the Updates

Apply the changes provided by the planner agent. Common update types:

**Answered questions:**
- Move from `### Unresolved Questions` to `### Self-Reflection Questions`
- Format: `- **Q**: {question}\n  - **A**: {answer}`

**Design decisions:**
- Add under the decision entry:
  ```
  - **Chosen**: {option}
  - **Rationale**: {why}
  ```

**Status updates:**
- Update the `**Status**` field at the top
- Mark completed steps with `- [x]`
- Add `**Last Updated**: {YYYY-MM-DD}` after Status

**Revision history:**
- Add or update Section 10 at the end (before the final note):
  ```markdown
  ## 10. Revision History

  | Date | Updated By | Changes |
  |------|------------|---------|
  | {YYYY-MM-DD} | AI Agent | {Brief description of changes} |
  ```

#### Step 4 — Update the Index

If the plan status changed, update the `Status` column in `plans/README.md` for this plan's row.

#### Step 5 — Sync to GitHub Issue (if linked)

If the plan has a linked issue (check `**Issue**:` field at the top):
- Replace the issue body with the updated plan content using `mcp__github__issue_write`.

Report to the caller: "Plan `{path}` updated, index updated, and issue #{issue-number} body synced."

---

### OPERATION: LIST

List all plans from the index.

#### Step 1 — Read the Index

Read `plans/README.md`. If it does not exist, report: "No plans found. The `plans/` directory is empty."

#### Step 2 — Present the Table

Return the full index table to the caller.

---

## Backporting Support

If the user indicates this is a historical plan (e.g., "log what we planned two weeks ago"), the caller may supply a custom date. Use that date instead of today's date for the `**Created**` field and the index row. All other steps are identical.

---

## Storage Rules

- Plans live in `plans/` with sequential `PLAN-nnnn` IDs — never in `.ai/`
- Plans are **permanent artifacts** — never delete a plan file; only update its status to `completed` or `cancelled`
- Existing `.ai/` plans are **not migrated** — leave them in place
- `plans/README.md` is the authoritative index — always update it on create and status-change updates

---

## Summary

| Operation | Input | Output |
|-----------|-------|--------|
| CREATE | issue number, feature slug, feature name | `plans/PLAN-nnnn_slug.md` created, index updated, issue synced |
| UPDATE | plan path, change set | plan file updated, index updated (if status changed), issue synced |
| LIST | — | index table from `plans/README.md` |
