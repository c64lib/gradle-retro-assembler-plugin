---
id: MET-0016
date: 2026-07-16 09:42
requested_by: Maciej Małecki
executed_by: AI
---

## Summary

Enhance the plan skill to prompt for issue linkage (existing / create new / none) and the execute skill to offer an adversarial challenge before execution and to offer closing the linked issue after it is synced with the plan.

## Purpose

These enhancements tighten the plan→execute→close lifecycle so GitHub issues stay linked and current. The plan skill now actively prompts for issue linkage (existing, newly created, or explicitly none) instead of silently defaulting to unlinked, ensuring plans are traceable to issues from creation. The execute skill gains two touchpoints: an optional adversarial `challenge` review before execution to catch plan weaknesses early, and an offer to close the linked issue once it has been synced with the final plan and all steps are implemented — completing the workflow without requiring manual issue cleanup.

## Affected Files

- `.claude/skills/plan/SKILL.md`
- `.claude/skills/execute/SKILL.md`

## Original Prompt

> enhance plan skill so it asks for an issue or offers to create new one when creating new plan. enhance execute skill so it offers to run adversarial challenge and to close associated issue after it is updated with plan
