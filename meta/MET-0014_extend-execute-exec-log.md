---
id: MET-0014
date: 2026-07-15 18:44
requested_by: Maciej Małecki
executed_by: AI
---

## Summary

Extended the `execute` skill with a per-plan execution log (`plans/EXEC-nnnn_{slug}.md`, own template, indexed in the plan's row) and an explicit offer to update the linked issue's description with the actual plan after execution.

## Purpose

Execution runs previously left their narrative only in chat and in terse plan checkboxes, and the linked GitHub issue was synced implicitly. The execution log gives each plan a durable record of how execution actually went — sessions, per-step results, and above all every deviation from the plan as written (changed approach, unplanned work, tooling fixes, wrong assumptions) — following its own template (`.claude/templates/exec.template.md`) and linked from the same `plans/README.md` row as its plan via a new `Exec` column. The issue sync becomes an explicit user choice instead of a silent side effect.

## Affected Files

- `.claude/skills/execute/SKILL.md`
- `.claude/skills/plan/SKILL.md` (index gains the `Exec` column; exec logs stay execute-skill-owned)
- `.claude/templates/exec.template.md` (new)
- `plans/README.md` (index migrated to the new column layout)

## Original Prompt

> extend execute skill with two things: firstly offer user an update of the related issue description with actual plan, secondly to store execution log in separate file named after plan: exec-xxxx-slug.md after plan-xxxx-slug.md. exec file should be indexed in the same row of plan log. exec should log execution and also note all deviations found during execution. exec should follow its own template
