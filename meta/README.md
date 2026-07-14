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
