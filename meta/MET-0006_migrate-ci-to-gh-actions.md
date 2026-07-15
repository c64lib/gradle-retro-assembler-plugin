---
id: MET-0006
date: 2026-07-15 14:11
requested_by: Maciej Małecki
executed_by: AI
---

## Summary

Updated the CLAUDE.md Quality Metrics section, replacing "CircleCI Integration" with "GitHub Actions Integration" as part of migrating CI from CircleCI to GitHub Actions.

## Purpose

The repository's CI pipeline was migrated from CircleCI to GitHub Actions (new workflows in `.github/workflows/`, removal of `.circleci/`). CLAUDE.md documented how quality reports are collected in CircleCI, so it had to be updated to describe the new GitHub Actions workflows and artifact names to keep Claude's project guidance accurate.

## Affected Files

- `CLAUDE.md`

## Original Prompt

> migrate from circleci to gh actions.
