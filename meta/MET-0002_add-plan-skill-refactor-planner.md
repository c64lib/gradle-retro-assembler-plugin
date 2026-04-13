# MET-0002 — Add plan skill and refactor planner agent

**Date**: 2026-04-13
**Initiated by**: human
**Affected files**:
- .claude/skills/plan/SKILL.md
- .claude/templates/plan.template.md
- .claude/agents/planner.md

## Summary

Add a `plan` skill for mechanical plan file I/O, extract a canonical plan template, and refactor the planner agent to delegate file operations to the skill.

## Purpose / Deliberation

The existing planner agent mixes persona behaviour with inline file I/O, making plan operations hard to reuse independently. Splitting file operations into a dedicated `plan` skill and extracting the plan document structure into a template provides a reusable, testable foundation. The skill also syncs plans to GitHub issues and maintains a `plans/README.md` index, giving the project a structured, auditable plan archive.

## Original User Prompt

> Pick #146 and execute it
