---
id: MET-0010
date: 2026-07-15 16:01
requested_by: Maciej Małecki
executed_by: AI
---

## Summary

Defined a canonical five-value plan status lifecycle (`draft`, `accepted`, `in progress`, `implemented`, `rejected`) across the plan skill, plan template, and execute skill, with an acceptance gate and terminal-plan handling.

## Purpose

The plan feature had a `**Status**` field but no defined vocabulary — the template seeded `Planning` while the skill's storage rules mentioned `completed`/`cancelled`, and neither matched the intended lifecycle. This change makes status a formal, enumerated lifecycle and encodes the user's rules:

- **Canonical values**: `draft → accepted → in progress → implemented`, with `rejected` reachable from any active state. The template now seeds new plans as `draft` and the index column mirrors the value verbatim.
- **Acceptance gate**: a plan may only become `accepted` when its Unresolved Questions list is empty (all questions answered and moved to Self-Reflection). During acceptance the user is asked whether the plan should be copied onto the linked GitHub issue's description; the issue is only overwritten if they say yes.
- **Terminal/historical states**: `implemented` and `rejected` are terminal — the plan becomes a historical record that is allowed to go stale, is never re-synced/re-verified or re-opened (a new plan is created instead), and is never deleted.
- **Execute skill alignment**: execution is gated to `accepted`/`in progress` plans (refuses `draft` and terminal plans), sets `in progress` on start, and sets `implemented` once every step is complete — all via the plan skill's UPDATE so plan I/O stays centralized.

## Affected Files

- `.claude/skills/plan/SKILL.md` (added Status Lifecycle section; wired acceptance gate, transitions, index seed, storage rules, issue-sync conditions)
- `.claude/templates/plan.template.md` (seed status `Planning` → `draft`)
- `.claude/skills/execute/SKILL.md` (status precondition check; canonical plan-level status transitions on start/completion)

## Original Prompt

> yes; also ensure that plans to become accepted must have all open questions answered, during acceptance user should be asked if plan should be copied onto current issue description.  plans that are implemented or rejected should be treated solely as historical and can become stale
