---
id: MET-0011
date: 2026-07-15 16:08
requested_by: Maciej Małecki
executed_by: AI
---

## Summary

Removed the `designer` and `developer` agents as redundant with the `plan` and `execute` skills, and folded the designer's continuous architectural Q&A behaviour into the plan skill's refinement step.

## Purpose

The `designer` and `developer` agents were the older, less capable generation of the same two concepts the skills now own: `designer` ≈ `plan` (interactive planning) and `developer` ≈ `execute` (plan execution). Both agents only printed/executed plans ad hoc — no persistence, no `plans/` files, no status lifecycle, no index or GitHub sync, and no composition with the build/test/verify skills — and their whole premise (a designer→developer handoff) was superseded. `developer` was a strict subset of the `execute` skill, so it was removed outright. `designer`'s one distinct capability — a continuous, section-by-section architectural Q&A loop that keeps asking until the user is satisfied — was preserved by rewriting the plan skill's CREATE Step 7 (Interactive refinement) to run that same open-ended loop (2–3 focused questions per section, immediate consistent propagation, continue until the user signals done) instead of a single-pass question round. Nothing referenced either agent outside their own files.

## Affected Files

- `.claude/agents/designer.md` (deleted)
- `.claude/agents/developer.md` (deleted)
- `.claude/skills/plan/SKILL.md` (CREATE Step 7 rewritten as continuous architectural Q&A, preserving the designer's refinement behaviour)

## Original Prompt

> go with recommendations
