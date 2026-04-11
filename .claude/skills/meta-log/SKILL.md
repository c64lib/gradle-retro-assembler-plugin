---
description: Log changes to Claude Code internal assets (CLAUDE.md, .claude/) as structured Markdown audit entries in meta/
user-invocable: true
---

# Meta-Log Skill: Audit Trail for Claude Code Internal Assets

**IMPORTANT: This skill fires automatically before you create, modify, or delete any Claude Code internal asset.** Read the trigger conditions carefully.

## When This Skill Fires (Auto-Trigger Conditions)

This skill fires **BEFORE** you create, modify, or delete any of these files or directories:

- `CLAUDE.md`
- Any file inside `.claude/agents/`
- Any file inside `.claude/skills/`
- Any file inside `.claude/commands/`
- `.claude/settings.json`
- `.claude/settings.local.json`

**This skill does NOT fire for:**
- Files inside `.claude/templates/`
- Any other file or directory outside the scope above

See CLAUDE.md section "Claude Code Asset Governance" for the standing instruction that triggers this skill.

---

## Procedure: Create a Meta-Log Entry

Follow these steps in order. Do not skip steps.

### Step 1: Determine Who Initiated This Change

Inspect the conversation context to determine who initiated the change:

- **"human"**: The user explicitly requested this change in their message
- **"AI"**: You (Claude) are making this change autonomously, unprompted by the user
- **"human + AI"**: The user requested it broadly, and you are elaborating or executing their request

Record this determination for use in Step 4.

### Step 2: Determine the Date

Default behavior:
- Use the **current date and time** in ISO 8601 format: `YYYY-MM-DDTHH:MM:SS±HH:MM` (e.g., `2026-04-11T15:23:45+02:00`)
- Or in a readable format: `YYYY-MM-DD HH:MM (timezone)` (e.g., `2026-04-11 15:23 +0200`)

**If the user indicates this is a backport** (e.g., "log what I did two weeks ago"):

1. Extract the affected file paths from the change you are about to make
2. Run the git command:
   ```bash
   git log --format="%ai %h %s" -- <space-separated file paths> | head -10
   ```
3. Present the output to the user in a code block
4. Ask: "Which commit date should I use for this log entry? Pick the date or tell me which commit."
5. User responds with a date or commit SHA
6. Use that date

**If git log returns no results**, ask the user directly: "I couldn't find a git history for the affected files. What date should I use for this backport?"

### Step 3: Determine the Next Sequential ID

Run this command to find the highest existing meta-log ID:

```bash
ls meta/ 2>/dev/null | grep -E '^MET-[0-9]+_' | sort | tail -1
```

- Extract the numeric part of the filename (e.g., `MET-0001` → `0001`)
- Increment by 1 and zero-pad to 4 digits
- If no files exist or `meta/` is absent, start at `MET-0001`

Record this ID for use in Steps 4 and 6.

### Step 4: Draft the Log Entry

Construct the entry using the ID, date, who-initiated, and affected files from earlier steps.

**Slug derivation**: Create a short kebab-case slug from the one-sentence summary. Use at most 5 words, all lowercase. Examples:
- "Add planner agent" → `add-planner-agent`
- "Update CLAUDE.md with flows patterns" → `update-flows-patterns`

**Entry structure** (copy this template and fill in the fields):

```markdown
# MET-nnnn — One-sentence summary

**Date**: YYYY-MM-DD HH:MM (timezone)
**Initiated by**: human | AI | human + AI
**Affected files**:
- relative/path/to/file1
- relative/path/to/file2

## Summary

One-sentence description of the change.

## Purpose / Deliberation

Two to four sentences explaining:
- Why this change was made
- What problem it solves
- What improvement it brings

## Original User Prompt

> [Exact verbatim quote of the user's message that triggered this change]

If no user prompt (AI-initiated), write:

> N/A — AI-initiated

If the user's prompt is very long, quote the most relevant excerpt.
```

### Step 5: Present the Draft for User Confirmation

Show the draft entry to the user in a Markdown code block. Then ask:

> Does this log entry look correct? Reply **'yes'** to confirm and write it, or tell me what to change.

**User responses:**

- **"yes"** or any affirmative: Proceed to Step 6 (write the entry)
- **"change X"** or feedback: Revise the entry and re-present it
- **"skip"** or "don't log": Ask once for confirmation: "Are you sure you want to skip logging this change?" 
  - If user confirms skip: Skip Step 6 and go directly to Step 8 (Resume)
  - If user changes their mind: Proceed to Step 6

Loop until the user confirms with "yes" or explicitly skips.

### Step 6: Write the Meta-Log Entry and Update the Index

#### Part A: Create the directory (if needed)

If `meta/` does not exist, create it.

#### Part B: Write the entry file

Write the confirmed entry to file:

```
meta/MET-nnnn_slug.md
```

Where `nnnn` is the 4-digit ID from Step 3, and `slug` is the kebab-case slug from Step 4.

#### Part C: Update the index file

1. Check if `meta/README.md` exists
2. If it does NOT exist, create it with this content:
   ```markdown
   # Meta Log Index

   Tracks all changes to Claude Code internal assets in this repository
   (CLAUDE.md, .claude/agents/, .claude/skills/, .claude/commands/, .claude/settings.*).

   Entries are stored as `meta/MET-nnnn_slug.md`.

   | ID | Date | Initiated by | Summary |
   |----|------|--------------|---------|
   ```

3. If it exists, read the entire file and append a new row to the table:
   ```markdown
   | [MET-nnnn](MET-nnnn_slug.md) | YYYY-MM-DD | human/AI/human+AI | One-sentence summary |
   ```

4. Write the updated file

### Step 7: Resume the Original Task

Inform the user:

> Meta-log entry **MET-nnnn** written to `meta/MET-nnnn_slug.md` and index updated. Proceeding with the original task.

Then proceed immediately to create, modify, or delete the original file(s) that triggered this skill.

---

## Examples

### Example 1: Adding a new skill

**User message**: "Create a new skill called 'analyze' that reviews code quality"

**Trigger**: You are about to create `.claude/skills/analyze/SKILL.md`

**Step 1**: "human" (directly requested)

**Step 2**: Current date/time: `2026-04-11 16:45 +0200`

**Step 3**: Last ID is `MET-0001`, next is `MET-0002`

**Step 4**: Draft entry:
```markdown
# MET-0002 — Create analyze skill for code quality review

**Date**: 2026-04-11 16:45 +0200
**Initiated by**: human
**Affected files**:
- .claude/skills/analyze/SKILL.md

## Summary

Create a new skill called 'analyze' that reviews code quality.

## Purpose / Deliberation

The analyze skill provides automated code quality reviews, helping identify issues and improvements. It complements the existing check and test skills.

## Original User Prompt

> Create a new skill called 'analyze' that reviews code quality
```

**Step 5**: User replies "yes"

**Step 6**: Write `meta/MET-0002_create-analyze-skill.md`, update `meta/README.md`

**Step 7**: Resume and create the skill

### Example 2: Backporting an agent change

**User message**: "Backport the planner agent addition to the log"

**Trigger**: You are about to create or modify `meta/MET-0003_*.md` as part of logging

**Step 2**: Run `git log --format="%ai %h %s" -- .claude/agents/planner.md`:
```
2026-03-29 17:23:45 +0200 abc1234 plan added
2026-03-20 09:15:30 +0200 def5678 Implement planner agent
```

Present this and ask which date to use. User picks `2026-03-29`.

Continue with date `2026-03-29 17:23 +0200` and "AI" for initiated-by (the agent was auto-deployed).

---

## Edge Cases

**What if the user is modifying multiple Claude Code assets at once?**
- If the changes are tightly related (e.g., updating a skill and CLAUDE.md together), create a **single log entry** with all affected files listed
- If the changes are unrelated, create **separate entries** (run this skill once per logical change)
- When in doubt, ask the user: "Should I log these as one change or separate entries?"

**What if a file is in both the original prompt and the files I'm about to change?**
- Use the original prompt verbatim, even if it doesn't perfectly describe all the affected files
- The "Affected files" section lists everything that actually changed

**What if the slug is ambiguous (multiple summaries sound similar)?**
- Make the slug more specific (add a noun or action): `add-agent` → `add-planner-agent`
- The slug just needs to be memorable enough to scan the file listing

---

## Summary

This skill ensures every change to Claude Code internal assets leaves an audit trail:

1. ✅ **Auto-triggers** before the change
2. ✅ **Detects who initiated** (human, AI, or both)
3. ✅ **Drafts an entry** with date, files, and context
4. ✅ **Waits for user confirmation** before writing
5. ✅ **Writes the entry file** and **updates the index**
6. ✅ **Resumes** the original task

The meta/ folder becomes the single source of truth for how the Claude Code setup has evolved.
