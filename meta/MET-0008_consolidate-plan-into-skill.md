---
id: MET-0008
date: 2026-07-15 15:37
requested_by: Maciej Małecki
executed_by: AI
---

## Summary

Folded the interactive planning workflow into the `plan` skill as a user-invocable front-end and removed the redundant `/plan`, `/plan-update`, `/h-plan`, and `/h-plan-update` slash commands (plus the `create-plan` and `create-plan-update` metaprompts).

## Purpose

The repository had three overlapping generations of plan tooling doing the same job: the `plan` skill (newest — `plans/PLAN-nnnn`, indexed, GitHub-issue-synced) and two slash-command families (`/plan` + `/plan-update` and `/h-plan` + `/h-plan-update`) writing to `.ai/` with no index or issue sync. The skill was a functional superset but was scoped only as a mechanical file-I/O delegate for a non-existent `planner` agent, so it omitted the interactive planning behaviour (Explore-based codebase analysis, AskUserQuestion refinement loop). We consolidated on the skill: merged that interactive workflow into it, made it the single user-invocable entry point, and deleted the redundant commands to remove confusion and duplication. Dangling references in `h-execute` and the `create-execute`/`create-fixme` metaprompts were repointed at the skill.

## Affected Files

- `.claude/skills/plan/SKILL.md` (rewritten: interactive CREATE/UPDATE/LIST workflow, user-invocable framing)
- `.claude/commands/plan.md` (deleted)
- `.claude/commands/plan-update.md` (deleted)
- `.claude/commands/h-plan.md` (deleted)
- `.claude/commands/h-plan-update.md` (deleted)
- `.claude/metaprompts/create-plan.md` (deleted)
- `.claude/metaprompts/create-plan-update.md` (deleted)
- `.claude/commands/h-execute.md` (repointed references to the plan skill)
- `.claude/metaprompts/create-execute.md` (repointed reference)
- `.claude/metaprompts/create-fixme.md` (repointed reference)

## Original Prompt

> go preferred, remove slash commands, change plan skill into user invokable
