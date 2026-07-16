---
id: MET-0017
date: 2026-07-16 11:05
requested_by: Maciej Małecki
executed_by: AI
---

## Summary

Strengthen the `plan` and `execute` skills so an accepted plan gets a feature branch and execution ships the work in order — commit → push → PR → issue sync → issue close — before the linked issue is closed.

## Purpose

These skills previously produced code and closed issues without a reliable branch/PR discipline: a plan could be executed straight on `develop`, and the execute skill offered to close the linked issue before the work was pushed or a pull request existed. Strengthening them enforces the intended review workflow — an accepted plan gets a feature branch, execution happens on that branch, and the work is shipped in a strict order (commit → push → PR → issue sync → issue close) so an issue is only ever closed once its changes are on a branch and up for review.

## Affected Files

- `.claude/skills/plan/SKILL.md`
- `.claude/skills/execute/SKILL.md`

## Original Prompt

> strengthen plan and execute skills. plan should propose creating a feature branch, execute should push onto branch and create pr before proposing closing of the issue
