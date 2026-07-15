---
id: MET-0009
date: 2026-07-15 15:46
requested_by: Maciej Małecki
executed_by: AI
---

## Summary

Added a single user-invocable `execute` skill that consolidates the `/execute` and `/h-execute` slash commands and works with plans maintained by the `plan` skill; removed both commands and the `create-execute` metaprompt.

## Purpose

Execution tooling had two overlapping slash commands (`/execute` and `/h-execute`) that both drove implementation of an action plan but assumed the legacy `.ai/` plan format and hand-edited the plan file to record progress. The plan lifecycle now lives in the user-invocable `plan` skill (`plans/PLAN-nnnn`, indexed, GitHub-issue-synced). We consolidated execution into one discoverable `execute` skill that: reads plans from `plans/` using the canonical template structure (Section 5 Implementation Plan, Phase/Step N.M with Files/Description/Testing), lets the user pick scope and engagement mode, executes steps per the repo's hexagonal-architecture rules, verifies deliverables by delegating to the build/test/e2e-test/verify skills instead of running Gradle inline, and records progress back through the `plan` skill's UPDATE operation rather than editing plan files directly. This removes duplication, keeps all plan I/O in the plan skill, and gives a single `/execute` entry point. The remaining `/fixme` command and the `create-fixme` metaprompt had their references repointed at the new skill.

## Affected Files

- `.claude/skills/execute/SKILL.md` (new — consolidated, user-invocable execute skill)
- `.claude/commands/execute.md` (deleted)
- `.claude/commands/h-execute.md` (deleted)
- `.claude/metaprompts/create-execute.md` (deleted)
- `.claude/metaprompts/create-fixme.md` (repointed reference to the execute skill)

## Original Prompt

> migrate *execute slash commands into single discoverable, user invokable execute skill.  it should work with plans maintained by plan skill
