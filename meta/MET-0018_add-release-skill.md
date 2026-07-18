---
id: MET-0018
date: 2026-07-18 15:14
requested_by: Maciej Małecki
executed_by: AI
---

## Summary

Replaced the release-notes-only `/release` command with a full interactive `release` skill covering version choice, release-note generation, `CHANGES.adoc` update, merge develop→master, and semver tagging to trigger Gradle Plugin Portal publication.

## Purpose

To replace the narrow release-notes-only `/release` command with a full guided release workflow, so cutting a version — version choice, release-note generation, `CHANGES.adoc` update, merging develop→master via a merge commit, and pushing the semver tag that triggers Gradle Plugin Portal publication — follows one consistent, human-gated procedure that encodes this project's tag-driven publish mechanics (no `build.gradle.kts` version bump, merge-commit only, tag push as the point of no return).

## Affected Files

- `.claude/skills/release/SKILL.md` (added)
- `.claude/commands/release.md` (deleted)

## Original Prompt

> based on release command create a release skill that will lead me thru release process including choosing release version, generating release notes, merging to master via merge commit, tagging to trigger release to gradle plugins portal
