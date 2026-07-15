---
id: MET-0005
date: 2026-07-15 13:53
requested_by: Maciej Małecki
executed_by: AI
---

## Summary

Added the `e2e-test` skill that wraps end-to-end testing of the locally deployed plugin against the tony game project used as a real-world test harness.

## Purpose

The tony project (`C:/Users/maciek/prj/cbm/tony`) consumes the plugin as `1.8.1-SNAPSHOT` from mavenLocal and exercises a wide surface of the flows DSL, making it the de facto e2e harness for plugin changes. This skill standardises that loop: publish the current plugin state to mavenLocal (via the `build` skill), run tony's `flows` (or a caller-chosen) Gradle task through a Haiku subagent, verify representative output artifacts, and report. Paths and version are deliberately hardcoded for now; generalization is planned later.

## Affected Files

- `.claude/skills/e2e-test/SKILL.md`

## Original Prompt

> Create a skill in plugin repo that will wrap around running tests (e2e) on tony repo. Make paths hardcoded in the skill (we will generalize it later).
