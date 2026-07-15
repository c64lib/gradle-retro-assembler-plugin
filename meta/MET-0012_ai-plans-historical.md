---
id: MET-0012
date: 2026-07-15 16:14
requested_by: Maciej Małecki
executed_by: AI
---

## Summary

Added a "Development Plans" section to CLAUDE.md establishing `plans/` as the authoritative plan system and directing Claude to treat all legacy `.ai/` plans as historical, potentially stale records; done alongside migrating the issue #135 plan into `plans/PLAN-0001`.

## Purpose

The `plans/` system (managed by the `plan`/`execute` skills with the canonical status lifecycle) is now the living plan system, while the older `.ai/` directory holds ~25 pre-existing plans in a different format. Without explicit guidance, Claude could read a stale `.ai/` plan as current truth. The new CLAUDE.md section makes the boundary authoritative: `plans/` is current and skill-managed; every `.ai/` plan is a historical record that is not synced with the codebase, must be verified against actual code before use, must not be updated in place, and must not be migrated wholesale. The user asked to migrate only the #135 plan (now `plans/PLAN-0001_pipeline-dsl-parallel-execution.md`, seeded `draft` because it still has three unresolved questions) and to have all other `.ai/` plans treated as historical/potentially stale — this CLAUDE.md rule encodes the latter.

## Affected Files

- `CLAUDE.md` (new "Development Plans" section)
- `plans/PLAN-0001_pipeline-dsl-parallel-execution.md` (new — migrated #135 plan; project artifact, not a Claude asset)
- `plans/README.md` (new — plans index; project artifact)

## Original Prompt

> migrate plan 135 and only that one.  ensure that all old plans in previous location are treated by claude as historical and potentialy stale
