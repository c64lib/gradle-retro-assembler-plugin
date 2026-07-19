---
id: MET-0020
date: 2026-07-19 13:53
requested_by: Maciej Małecki
executed_by: AI
---

## Summary

Moved the adversarial-challenge offer from execute-time to plan acceptance, recording the outcome in a new `**Challenge**:` plan header field (PLAN-0014, issue #186).

## Purpose

The adversarial challenge is most valuable when a plan is fresh and maximally revisable — at acceptance, not after execution has begun. This change makes the `plan` skill offer the challenge (optionally, not as a gate) during the `→ accepted` transition and persists whether/when it ran in a machine-checkable `**Challenge**:` header field. The `execute` skill becomes a fallback: it re-offers the challenge only when the plan shows none was run (field starts with `not run`, missing treated as `not run`), and writes the outcome back so an already-vetted plan is not re-challenged. The four field values form a closed enum owned canonically by the `plan` skill's Status Lifecycle.

## Affected Files

- `.claude/templates/plan.template.md`
- `.claude/skills/plan/SKILL.md`
- `.claude/skills/execute/SKILL.md`

## Original Prompt

> Currently adv challenge is proposed before execution of the plan, but actually it make sense to propose it as optional gate during plan acceptance. So when executing it should only be proposed if not done already. Register challenge execution in the plan document itself
