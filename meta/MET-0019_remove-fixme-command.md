---
id: MET-0019
date: 2026-07-18 17:12
requested_by: Maciej Małecki
executed_by: AI
---

## Summary

Removed the `/fixme` slash command and its generator metaprompt, both tied to the abandoned `.ai/` legacy plan system.

## Purpose

To remove the `/fixme` command and its generator because they were tied to the abandoned `.ai/` legacy plan system — the command's whole job was to edit `.ai/` plans, which CLAUDE.md now forbids — and its error-diagnosis-and-record function is already covered, in the authoritative `plans/`+`EXEC-nnnn` system, by the `execute` skill's failure handling (retry / change approach / skip, with deviations logged to the exec log). Keeping it risked steering work into stale files it should not touch.

## Affected Files

- `.claude/commands/fixme.md` (deleted)
- `.claude/metaprompts/create-fixme.md` (deleted)

## Original Prompt

> check if fixme command brings any value or should it be also deleted
