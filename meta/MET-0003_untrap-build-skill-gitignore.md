---
id: MET-0003
date: 2026-07-14 23:18
requested_by: Maciej Małecki
executed_by: AI
---

## Summary

Add `.gitignore` negation rules so the `.claude/skills/build/` skill folder stays tracked despite the `**/build/` Gradle-output ignore pattern.

## Purpose

The existing `**/build/` rule (intended for Gradle output directories) also matches the new `.claude/skills/build/` skill folder, which would silently exclude it from future `git add` operations. Negation rules keep the skill under version control and remove the trap.

## Affected Files

- `.gitignore`

## Original Prompt

> yes
>
> (follow-up confirming the assistant's suggestion to add a `!.claude/skills/build/` negation rule to `.gitignore`, alongside pushing the build-skill branch and opening a PR)
