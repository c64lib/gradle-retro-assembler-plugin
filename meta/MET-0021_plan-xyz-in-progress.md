---
id: MET-0021
date: 2026-07-19 20:47
requested_by: Maciej Małecki
executed_by: AI
---

## Summary

Add `plan #xyz` issue-driven shorthand to the plan skill and offer to move the linked issue to In Progress on the c64lib project board when planning starts.

## Purpose

Streamline the user's habitual GitHub-issue-driven planning flow. Establishes `plan #xyz` as an explicit shorthand that reads issue `xyz` and drives planning from its description (no re-asking for issue number or spec), and adds an offer at planning start to move the linked issue to "In Progress" on the c64lib GitHub Projects board — degrading gracefully with a scope-fix instruction (`gh auth refresh -s read:project,project`) and a manual fallback when the token lacks project scopes. Following an adversarial `/challenge` review, all issue read/write and board operations use the `gh` CLI (`gh issue view`/`gh issue edit`, `gh project list`/`item-list`/`field-list`/`item-edit`) — consistent with the sibling `gh-utils`/`execute`/`release` skills — replacing the earlier `mcp__github__*` references (that MCP server is not available in this environment); the pre-existing `mcp__github__issue_read`/`issue_write` references in the CREATE Step 6 and UPDATE Step 7 sync steps were swept to `gh` in the same edit. The board-move success path spells out the concrete project → item → field/option id discovery sequence rather than assuming the issue number suffices.

## Affected Files

- `.claude/skills/plan/SKILL.md`

## Original Prompt

> enhance plan and execute workflow.  usually I ask to plan an issue from gh, then I ask "plan #xyz" which just means read issue number xyz and start planning according to provided description. Once planning is started,  issue should be turned to in progress, at least this should be proposed to user
