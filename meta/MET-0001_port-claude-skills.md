---
id: MET-0001
date: 2026-07-14 23:05
requested_by: Maciej Małecki
executed_by: AI
---

## Summary

Port three skills — `claude-meta-changelog`, `git-utils`, and `gh-utils` — from the `maciejmalecki/research` repo, plus a changelog entry template.

## Purpose

Bring three domain-agnostic engineering skills from the sibling `maciejmalecki/research` repo into this Gradle plugin project — an audit-trail changelog for Claude internal assets, plus standardized local git and GitHub-PR wrappers — adapted to this repo's `develop` base branch, `feature/<issue>-<slug>` branch naming, and domain-based commit conventions.

## Affected Files

- `.claude/skills/claude-meta-changelog/SKILL.md`
- `.claude/skills/git-utils/SKILL.md`
- `.claude/skills/gh-utils/SKILL.md`
- `.claude/templates/changelog-entry.md`

## Original Prompt

> port meta changelog first, then git and gh utils,
