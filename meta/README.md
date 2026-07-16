# Meta Changelog

Audit trail of deliberate changes to Claude Code internal assets in this repository — rules (`CLAUDE.md`), agents (`.claude/agents/`), skills (`.claude/skills/`), commands (`.claude/commands/`), and configuration (`.claude/settings.json`).

Each entry is a separate Markdown file named `MET-nnnn_<slug>.md`, based on the template at [`.claude/templates/changelog-entry.md`](../.claude/templates/changelog-entry.md).

## Index

| ID | Date | Who | Summary |
|----|------|-----|---------|
| [MET-0001](MET-0001_port-claude-skills.md) | 2026-07-14 | Maciej Małecki | Port claude-meta-changelog, git-utils, and gh-utils skills plus a changelog entry template |
| [MET-0002](MET-0002_add-build-skill.md) | 2026-07-14 | Maciej Małecki | Add a build skill wrapping the Gradle build, routing tasks through a Haiku subagent |
| [MET-0003](MET-0003_untrap-build-skill-gitignore.md) | 2026-07-14 | Maciej Małecki | Keep .claude/skills/build/ tracked despite the **/build/ gitignore rule |
| [MET-0004](MET-0004_test-skill-reuse-build.md) | 2026-07-14 | Maciej Małecki | Modify the test skill to delegate Gradle execution to the build skill (Haiku subagent), keeping analysis on the main agent |
| [MET-0005](MET-0005_add-e2e-test-skill.md) | 2026-07-15 | Maciej Małecki | Add an e2e-test skill wrapping end-to-end plugin testing against the tony harness project (hardcoded paths) |
| [MET-0006](MET-0006_migrate-ci-to-gh-actions.md) | 2026-07-15 | Maciej Małecki | Update CLAUDE.md Quality Metrics section for the CircleCI → GitHub Actions migration |
| [MET-0007](MET-0007_cherry-pick-plan-skill.md) | 2026-07-15 | Maciej Małecki | Reset branch 135 to develop and cherry-pick only the plan skill + plan.template.md from its divergent Claude assets |
| [MET-0008](MET-0008_consolidate-plan-into-skill.md) | 2026-07-15 | Maciej Małecki | Fold interactive planning into the user-invocable plan skill; remove /plan, /plan-update, /h-plan, /h-plan-update commands and their metaprompts |
| [MET-0009](MET-0009_consolidate-execute-into-skill.md) | 2026-07-15 | Maciej Małecki | Add a user-invocable execute skill consolidating /execute + /h-execute, working with plan-skill plans; remove both commands and the create-execute metaprompt |
| [MET-0010](MET-0010_plan-status-lifecycle.md) | 2026-07-15 | Maciej Małecki | Define canonical plan status lifecycle (draft/accepted/in progress/implemented/rejected) with acceptance gate + issue-copy prompt and terminal-historical handling across plan/execute skills and template |
| [MET-0011](MET-0011_remove-designer-developer-agents.md) | 2026-07-15 | Maciej Małecki | Remove redundant designer/developer agents; fold designer's continuous architectural Q&A into the plan skill's refinement step |
| [MET-0012](MET-0012_ai-plans-historical.md) | 2026-07-15 | Maciej Małecki | Add CLAUDE.md Development Plans section (plans/ authoritative, .ai/ historical/stale); migrate only issue #135 plan into plans/PLAN-0001 |
| [MET-0013](MET-0013_add-challenge-skill.md) | 2026-07-15 | Maciej Małecki | Add user-invocable challenge skill (adversarial stress-test of plans/executions/docs/changes), adapted from the research repo's adversary-challenge skill |
| [MET-0014](MET-0014_extend-execute-exec-log.md) | 2026-07-15 | Maciej Małecki | Extend execute skill with per-plan execution log (EXEC-nnnn file + template + Exec index column) and an explicit post-execution issue-description sync offer |
| [MET-0015](MET-0015_claude-md-arc42-pointer.md) | 2026-07-15 | Maciej Małecki | Add an Architecture reference pointer in CLAUDE.md to the arc42 docs with a maintenance instruction (PLAN-0002 Step 3.5) |
| [MET-0016](MET-0016_plan-issue-execute-challenge-close.md) | 2026-07-16 | Maciej Małecki | Enhance plan skill to prompt for issue linkage (existing/create/none) and execute skill to offer an adversarial challenge before executing and to offer closing the linked issue after syncing the plan |
