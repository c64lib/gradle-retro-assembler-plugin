---
name: claude-meta-changelog
description: >
  Log changes to Claude Code internal assets as structured MET-nnnn entries in `meta/`.
  AUTOMATICALLY trigger — without waiting for an explicit user request — whenever the user's
  prompt asks to create, modify, or delete any of: `CLAUDE.md`, `.claude/agents/`,
  `.claude/skills/`, `.claude/commands/`, `.claude/settings.json`.
  Also trigger when the user explicitly asks to backport or migrate a past change.
---

# Claude Meta Changelog Skill

Maintains an audit trail of deliberate changes to Claude Code internal assets. Each change is
captured as a separate Markdown file in `meta/`, indexed in `meta/README.md`.

---

## When to Trigger

Fire **automatically** (as part of the same response that makes the change) whenever the user's
prompt requests a change to any Claude Code internal asset:

| Asset | Examples |
|-------|---------|
| `CLAUDE.md` | Add/edit/remove rules, instructions, context |
| `.claude/agents/*.md` | Create, update, or delete a custom agent |
| `.claude/skills/**` | Create, update, or delete a skill |
| `.claude/commands/**` | Create, update, or delete a slash command |
| `.claude/settings.json` | Change hooks, permissions, or other config |

Also trigger when the user explicitly requests **backporting** — logging a past change
retroactively. In that case, accept a user-supplied date/time instead of the current timestamp.

Do **not** trigger for changes to project source code, build files, or any folder other than the
Claude internal assets listed above.

---

## Entry ID Scheme

Each entry is assigned a sequential `MET-nnnn` identifier:

```
Glob("meta/MET-*.md") → find highest nnnn → next = nnnn + 1
If meta/ is empty or has no MET-*.md files → start at 0001
```

---

## File Naming

```
meta/MET-nnnn_<slug>.md
```

- `<slug>` is auto-derived from the one-sentence summary
- Lowercase, hyphen-separated, **max 25 characters** (truncate cleanly at a word boundary)
- Example: `MET-0001_add-git-utils-skill.md`

---

## Entry Format

Base every entry on `.claude/templates/changelog-entry.md`:

```markdown
---
id: MET-nnnn
date: YYYY-MM-DD HH:MM
requested_by: <git username of the person who made the request>
executed_by: <git username or "AI">
---

## Summary

<One-sentence description of what changed.>

## Purpose

<AI-proposed interpretation of why this change was made — confirmed or edited by the user before writing.>

## Affected Files

- `<file or folder path>`

## Original Prompt

> <Verbatim copy of the user's prompt that triggered this change.>
```

**Field rules:**
- `date` — on Windows/PowerShell run `Get-Date -Format "yyyy-MM-dd HH:mm"`; on the Bash tool run `date +"%Y-%m-%d %H:%M"`. For backports use the user-supplied date.
- `requested_by` — git username of the human who wrote the prompt (run `git config user.name`)
- `executed_by` — `AI` when Claude performed the change; git username when the human did it themselves; both names comma-separated when both were involved
- `Original Prompt` — quoted verbatim, exactly as the user typed it

---

## Purpose Field: Propose and Confirm

Do **not** silently write the Purpose. Instead:

1. Draft a one-paragraph interpretation of *why* the change was requested (infer from context, task history, prior conversation)
2. Present it to the user: *"Here's the purpose I'll log — want to adjust it before I write the entry?"*
3. Wait for confirmation or edits, then write the final entry

Skip the confirmation step only if the user has already stated the purpose explicitly in their prompt.

---

## Index: meta/README.md

After writing each entry, append a row to the index table in `meta/README.md`:

```markdown
| [MET-nnnn](MET-nnnn_<slug>.md) | YYYY-MM-DD | <requested_by> | <one-sentence summary> |
```

**Columns:** ID (linked) | Date | Who | Summary

If `meta/README.md` does not exist, create it first:

```markdown
# Meta Changelog

Audit trail of deliberate changes to Claude Code internal assets in this repository — rules (`CLAUDE.md`), agents (`.claude/agents/`), skills (`.claude/skills/`), commands (`.claude/commands/`), and configuration (`.claude/settings.json`).

Each entry is a separate Markdown file named `MET-nnnn_<slug>.md`, based on the template at [`.claude/templates/changelog-entry.md`](../.claude/templates/changelog-entry.md).

## Index

| ID | Date | Who | Summary |
|----|------|-----|---------|
```

---

## Workflow

```
1. Detect that user's prompt touches a Claude internal asset
2. Perform the requested change (write/edit the file)
3. Glob("meta/MET-*.md") → determine next MET-nnnn
4. Get timestamp (Get-Date on PowerShell, or date on Bash)
5. run `git config user.name` → get requester name
6. Draft Purpose paragraph → present to user for confirmation
7. On confirmation: Write("meta/MET-nnnn_<slug>.md") using template
8. Append row to meta/README.md index
```

---

## Backport / Migration

When the user says "log a past change" or "backport this to the changelog":

1. Ask for (or accept from prompt): the original date/time, the affected files, the original prompt (if available)
2. Use the user-supplied date in the `date` field instead of the current timestamp
3. Note `[backported]` at the end of the Summary line
4. Otherwise follow the same workflow

---

## Tips

| Situation | Action |
|-----------|--------|
| Multiple files changed in one prompt | List all in Affected Files; one entry per prompt |
| User's prompt is ambiguous about scope | Complete the change, then log what was actually modified |
| Purpose is already stated in prompt | Use it directly; skip the confirmation step |
| Skill creates `meta/` for the first time | Write `meta/README.md` before the first entry |
