---
id: MET-0013
date: 2026-07-15 16:23
requested_by: Maciej Małecki
executed_by: AI
---

## Summary

Added a user-invocable `challenge` skill that adversarially stress-tests this repo's plans, executions/code changes, docs, and other changes, adapted from the `adversary-challenge` skill in the maciejmalecki/research repo.

## Purpose

The user asked for an adversarial challenge skill based on the research repo (`../../maciejmalecki/research`), which contains a well-designed `adversary-challenge` skill for stress-testing research claims/surveys. This skill ports that skill's philosophy — steelman before you strike, specific and falsifiable objections, severity ratings, no fabricated counter-evidence, critique-only (never edit the target), constructive close — to this codebase's artifact types. Its four modes map onto what this repo actually produces: (A) red-team a `plans/PLAN-nnnn` plan or a design decision, (B) devil's-advocate an execution/code change or approach (inspecting the real git/PR diff), (C) adversarial review of a document (CLAUDE.md, README, asciidoc, a skill), and (D) challenge an arbitrary change or claim. It grounds critique in this repo's CLAUDE.md standards (hexagonal architecture, coverage targets, plan status lifecycle, conventions), is read-only, delegates any Gradle verification to the build skill, and explicitly hands off fixes to /code-review, /simplify, or /plan update rather than editing the target itself.

## Affected Files

- `.claude/skills/challenge/SKILL.md` (new — adversarial challenge skill)

## Original Prompt

> based on ../../maciejmalecki/research, create adversarial challenge skill to challenge plans, executions, docs and other changes in this repo
