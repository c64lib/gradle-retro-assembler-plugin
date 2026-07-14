---
id: MET-0002
date: 2026-07-14 23:16
requested_by: Maciej Małecki
executed_by: AI
---

## Summary

Add a `build` skill that wraps this project's Gradle build, exposes every task the root build script declares, and routes all Gradle invocations through a Haiku subagent.

## Purpose

Give the project a single, convention-following entry point for build operations. The skill documents every task the root `build.gradle.kts` exposes (lifecycle, verification, formatting, publishing, and per-module addressing) and routes every actual `./gradlew` invocation through a spawned Haiku subagent — mirroring the git-utils/gh-utils execution pattern — while deferring structured diagnostic runs to the existing `check` and `test` skills.

## Affected Files

- `.claude/skills/build/SKILL.md`

## Original Prompt

> create new skill called build that wraps around gradle build of this project. It should expose all tasks of the original build script and once called reroute everything via subagent using Haiku
