---
id: MET-0004
date: 2026-07-14 23:59
requested_by: Maciej Małecki
executed_by: AI
---

## Summary

Modified the test skill to delegate all Gradle execution to the build skill (Haiku subagent), keeping only result analysis on the main agent.

## Purpose

The `test` skill previously ran `./gradlew` directly on the main agent, duplicating the Haiku-subagent delegation convention that the `build` skill already established. This change makes the `test` skill route all Gradle execution through the `build` skill so the slow, mechanical work stays off the main agent, while the `test` skill retains sole ownership of its distinctive value — collecting results and producing detailed failure diagnostics. The goal is a single source of truth for "how we run Gradle" (the `build` skill) and no duplicated subagent logic across skills.

## Affected Files

- `.claude/skills/test/SKILL.md`

## Original Prompt

> modify tests skill so it uses similar reroute to Haiku logic as build skill. preferably reuse build skill in tests skill
