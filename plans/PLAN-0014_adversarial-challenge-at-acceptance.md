# Feature: Move adversarial challenge to plan acceptance

**Plan ID**: PLAN-0014
**Issue**: #186
**Status**: implemented
**Created**: 2026-07-19
**Last Updated**: 2026-07-19
**Challenge**: revised 2026-07-19

## 1. Feature Description

### Overview
The adversarial `challenge` review is currently offered only at *execution* time
(by the `execute` skill, Step 2). This plan moves the primary offer to *plan
acceptance* time (in the `plan` skill's `→ accepted` transition), records whether
a challenge was run directly in the plan document via a new `**Challenge**:`
header field, and makes the `execute` skill offer the challenge only when the
plan shows it has not already been done.

### Requirements
- The `plan` skill offers an **optional** adversarial challenge during the
  `→ accepted` transition, after all Unresolved Questions are resolved and before
  finalizing acceptance. It is an offer, not a hard gate — acceptance proceeds
  whether the user runs it or declines.
- Whether/when a challenge was run is recorded in the plan via a new
  `**Challenge**:` header field (values below), plus a short summary of findings.
- The `execute` skill offers the challenge **only if the plan indicates it was not
  already run** (the `**Challenge**:` field starts with `not run`, or is missing).
  If a challenge was already done, execute does not re-offer by default. When a
  challenge *is* run at execute-time, execute writes the field back (via the `plan`
  skill), symmetrically with the acceptance path.
- The `**Challenge**:` values are a **closed four-value enum** (`not run`,
  `passed {date}`, `revised {date}`, `waived {date}`) owned canonically by the
  `plan` skill's Status Lifecycle.
- The canonical plan template carries the new `**Challenge**:` header field and a
  sanctioned `### Adversarial Challenge` findings subsection.

### Success Criteria
- New plans created by `/plan` include a `**Challenge**: not run` header field.
- Accepting a plan via `/plan update` offers the challenge and records the outcome
  in the `**Challenge**:` field and a findings summary.
- `/execute` skips its challenge offer when the plan's `**Challenge**:` field shows
  a challenge was already run, and still offers it when the field is `not run`.
- Documentation (skill files, template) is internally consistent about where the
  challenge is offered.

## 2. Root Cause Analysis

This is a workflow/documentation change to Claude Code assets, not application
code. No Kotlin/Gradle code is touched.

### Current State
- `execute` skill **Step 2** (`.claude/skills/execute/SKILL.md:69`) offers the
  adversarial challenge before execution, every time, with no memory of whether a
  challenge already happened.
- The `plan` skill's `→ accepted` transition
  (`.claude/skills/plan/SKILL.md`, Status Lifecycle + UPDATE Step 4) has an
  acceptance gate (all Unresolved Questions must be answered) and offers issue-body
  copy + feature-branch creation, but **does not** offer a challenge.
- The plan template (`.claude/templates/plan.template.md`) header has
  `Plan ID`, `Issue`, `Status`, `Created` — **no** challenge field.
- Nothing in the plan document records whether a challenge was ever run.

### Desired State
- The challenge is primarily offered at **acceptance** (where the plan is
  finalized and most amenable to revision), and its outcome is **persisted** in the
  plan document.
- `execute` becomes a **fallback** offer: it only proposes the challenge when the
  plan shows none was run, avoiding redundant re-challenging of an already-vetted
  plan.

### Gap Analysis
1. Add a `**Challenge**:` header field to the template.
2. Add a challenge offer + outcome recording to the `plan` skill's acceptance flow.
3. Make the `execute` skill's challenge offer conditional on the `**Challenge**:`
   field.
4. Keep all three assets internally consistent.

## 3. Relevant Code Parts

### Existing Components
- **plan skill**: `.claude/skills/plan/SKILL.md`
  - Status Lifecycle table + acceptance-gate paragraph.
  - UPDATE Step 4 (`→ accepted` handling) and Step 4a (feature-branch offer).
  - CREATE Step 4 (fills header placeholders from the template) and Step 7
    (interactive refinement / conclusion).
  - Integration Point: add the challenge offer as part of the `→ accepted`
    transition, delegating to `Skill(skill: "challenge")`, and write the
    `**Challenge**:` field.
- **execute skill**: `.claude/skills/execute/SKILL.md:69`
  - Step 2 currently offers the challenge unconditionally.
  - Integration Point: gate the offer on the plan's `**Challenge**:` field.
- **plan template**: `.claude/templates/plan.template.md:1-6`
  - Header block. Integration Point: add `**Challenge**: not run` line.
- **challenge skill**: `.claude/skills/challenge/SKILL.md`
  - Mode A (red-team a plan). Unchanged — it remains a pure critique skill; it does
    not write the plan. The `plan`/`execute` skills record the outcome.

### Architecture Alignment
- **Domain**: N/A — Claude Code harness assets (skills + templates). No hexagonal
  code, no `infra/gradle` dependency, no Gradle tasks.
- This change is subject to the `claude-meta-changelog` skill: editing
  `.claude/skills/` and `.claude/templates/` should be logged as a MET-nnnn entry
  in `meta/` during execution.

### Dependencies
- `challenge` skill (invoked from `plan` acceptance and, conditionally, from
  `execute`).
- `claude-meta-changelog` skill (to log the asset changes on execution).

## 4. Questions and Clarifications

### Self-Reflection Questions
- **Q**: How should a completed challenge be recorded in the plan document?
  - **A**: Via a new `**Challenge**:` header field near `**Status**`, with a short
    findings summary subsection under section 4. Chosen over a subsection-only
    approach because a header field is trivially machine-checkable by the `execute`
    skill when deciding whether to re-offer.
- **Q**: When should the challenge be offered during acceptance, and is it a gate?
  - **A**: Offered on the `→ accepted` transition **after** Unresolved Questions
    are resolved, as an **optional** offer — not a hard gate. Acceptance proceeds
    whether the user runs it, revises after it, or declines. This matches the
    existing "offer, not a gate" pattern already used for the challenge in execute
    and for issue-sync/branch-creation in the plan skill.
- **Q**: What values does the `**Challenge**:` field take?
  - **A**: `not run` (default on creation), `passed {YYYY-MM-DD}` (ran, no changes
    needed), `revised {YYYY-MM-DD}` (ran, plan updated to address findings), and
    `waived {YYYY-MM-DD}` (user explicitly declined at acceptance). `execute`
    re-offers only when the value is `not run`.
- **Q**: Does the `challenge` skill itself need changes?
  - **A**: No. It stays a pure critique skill. The recording of the outcome is done
    by the `plan` skill (at acceptance) or the `execute` skill (fallback), not by
    `challenge`.
- **Q**: If a challenge is run during `/execute` (the fallback path), who writes
    the `**Challenge**:` field, and when? (Challenge finding #1 — close the loop.)
  - **A**: The `execute` skill closes the loop symmetrically with acceptance: after
    a fallback challenge runs at execute-time, `execute` writes the field back via
    the `plan` skill's UPDATE (`passed`/`revised`/`waived {date}`). Without this the
    field stays `not run` and `execute` re-offers the challenge on every run — the
    headline benefit ("execute doesn't re-challenge an already-vetted plan") would
    be only half-delivered.
- **Q**: Is the `**Challenge**:` vocabulary a closed enum, and who owns it? How
    does `execute` match it? (Challenge finding #2 — pin the vocabulary.)
  - **A**: Yes — exactly four values (`not run`, `passed {date}`, `revised {date}`,
    `waived {date}`), a **closed enum** owned canonically by the `plan` skill's
    Status Lifecycle section (the single source of truth, mirroring how `Status`
    is defined). `execute`'s match rule is **"field value starts with `not run`
    ⇒ offer; anything else ⇒ skip"**, and a **missing field is treated as
    `not run`**. This fails safe toward *offering* when a value is absent or
    malformed.
- **Q**: Where does the challenge-findings summary live, given the template's fixed
    section structure? (Challenge finding #3 — resolve the subsection.)
  - **A**: The `### Adversarial Challenge` subsection is added to the **plan
    template** (section 4) in Phase 1, so it is a sanctioned part of the canonical
    structure rather than an ad-hoc subsection injected at acceptance. This keeps
    the `plan` skill's "exact template structure and numbering" rule intact.

### Unresolved Questions
(none)

### Design Decisions
- **Decision**: How to record challenge status in the plan.
  - **Chosen**: New `**Challenge**:` header field + findings summary subsection.
  - **Rationale**: Machine-checkable by `execute`, visible at a glance, consistent
    with the existing header-field style (`Status`, `Issue`, `Created`).
- **Decision**: Acceptance offer strength.
  - **Chosen**: Optional offer, not a hard gate.
  - **Rationale**: Preserves flow; consistent with existing offer patterns; avoids
    forcing a challenge on trivial plans.
- **Decision**: Ownership and matching of the `**Challenge**:` vocabulary.
  - **Chosen**: Closed enum of four values, canonically defined in the `plan`
    skill's Status Lifecycle; `execute` matches "starts with `not run` ⇒ offer,
    else skip"; missing field ⇒ `not run`.
  - **Rationale**: One authoritative source prevents drift; a starts-with match
    that treats absent/malformed values as `not run` fails safe toward offering,
    the conservative direction.
- **Decision**: Home of the findings summary.
  - **Chosen**: Add a sanctioned `### Adversarial Challenge` subsection to the plan
    template's section 4.
  - **Rationale**: Keeps the template the single canonical structure; avoids the
    `plan` skill injecting a subsection the template does not declare.
- **Decision**: Symmetry between acceptance and execute recording.
  - **Chosen**: The `execute` skill writes the field back (via the `plan` skill)
    after a fallback challenge, mirroring the acceptance path.
  - **Rationale**: Without write-back the field never leaves `not run`, so
    `execute` would re-offer forever — defeating the feature's purpose.

## 5. Implementation Plan

### Phase 1: Template + plan-skill acceptance offer (challenge recorded at acceptance)
**Goal**: New plans carry the field, and acceptance offers + records the challenge.
**Status**: ✅ Completed 2026-07-19 (Steps 1.1–1.3).

1. **[x] Step 1.1**: Add the `**Challenge**:` header field **and** the sanctioned
   findings subsection to the plan template.
   - Files: `.claude/templates/plan.template.md`
   - Description: Insert `**Challenge**: not run` into the header block (after
     `**Status**: draft`). Keep placeholder consistent with CREATE Step 4. Also add
     a `### Adversarial Challenge` subsection to **section 4** (Questions and
     Clarifications) as a declared part of the canonical structure, so recording
     findings at acceptance does not violate the template-structure rule (finding
     #3). The subsection holds the challenge status + a short findings summary.
   - Testing: Read the template back; confirm the header block is well-formed and
     section 4 declares the `### Adversarial Challenge` subsection.

2. **[x] Step 1.2**: Teach the `plan` skill's CREATE flow to emit the field and define
   the closed enum.
   - Files: `.claude/skills/plan/SKILL.md`
   - Description: In CREATE Step 4, note that the header now includes
     `**Challenge**: not run` on new plans. Add the field to the Status Lifecycle
     documentation as the **canonical, closed enum** of exactly four values —
     `not run`, `passed {YYYY-MM-DD}`, `revised {YYYY-MM-DD}`,
     `waived {YYYY-MM-DD}` — with each value's meaning (finding #2). State that
     this section is the single source of truth for the `**Challenge**:`
     vocabulary, mirroring how `**Status**` is defined.
   - Testing: Re-read the skill; confirm CREATE emits the field and the Status
     Lifecycle defines the closed four-value enum as canonical.

3. **[x] Step 1.3**: Add the optional challenge offer to the `→ accepted` transition.
   - Files: `.claude/skills/plan/SKILL.md`
   - Description: In UPDATE Step 4 (`→ accepted` handling), after the
     Unresolved-Questions gate passes and before/alongside the issue-copy and
     branch offers, add: offer (via `AskUserQuestion`) to run the adversarial
     `challenge` (mode A) on the plan. If accepted, invoke
     `Skill(skill: "challenge")`, relay findings, let the user revise (loop back
     into UPDATE) or accept as-is; then set `**Challenge**:` to
     `passed {date}` or `revised {date}`. If declined, set `waived {date}`.
     Record findings in the template's `### Adversarial Challenge` subsection
     (added in Step 1.1). Note this is an **offer, not a gate**.
   - Testing: Walk the acceptance path mentally against the skill text; confirm the
     field is always set on `→ accepted` and acceptance is never blocked by it.

**Phase 1 Deliverable**: Template + `plan` skill fully support recording a
challenge at acceptance. Mergeable on its own (execute still offers
unconditionally, which is harmless — worst case a redundant offer).

### Phase 2: Execute-skill conditional offer + guardrails
**Goal**: `execute` offers the challenge only when not already run.
**Status**: ✅ Completed 2026-07-19 (Steps 2.1–2.3).

1. **[x] Step 2.1**: Make the execute Step 2 offer conditional, with a precise match
   rule.
   - Files: `.claude/skills/execute/SKILL.md`
   - Description: Update Step 2 so the challenge offer is made **only if** the
     plan's `**Challenge**:` header field **value starts with `not run`**, and a
     **missing field is treated as `not run`** (finding #2 — fails safe toward
     offering). If the value starts with anything else (`passed`/`revised`/
     `waived`), note that a challenge was already handled at acceptance and skip the
     offer (still allow the user to request one explicitly).
   - Testing: Re-read Step 2 and the Guardrails list; confirm the starts-with match,
     the missing-field fallback, and that the guardrail wording matches.

2. **[x] Step 2.2**: Close the execute-side loop — write the field back after a
   fallback challenge.
   - Files: `.claude/skills/execute/SKILL.md`
   - Description: When the user accepts the execute-time challenge offer and it
     runs, `execute` must record the outcome by delegating to the `plan` skill's
     UPDATE to set `**Challenge**:` to `passed`/`revised`/`waived {date}` (finding
     #1). This mirrors the acceptance path and prevents `execute` from re-offering
     the challenge on every subsequent run. Keep this write going through the
     `plan` skill (not a direct edit), consistent with the "all plan-file writes go
     through the `plan` skill" guardrail.
   - Testing: Walk the execute path mentally; confirm a challenge run at
     execute-time flips the field away from `not run` via the `plan` skill.

3. **[x] Step 2.3**: Reconcile the guardrail bullet.
   - Files: `.claude/skills/execute/SKILL.md`
   - Description: Update the "adversarial challenge is an offer, not a gate"
     guardrail (`SKILL.md:148`) to reflect the new conditional behavior (offer only
     when the field starts with `not run`) and the write-back loop.
   - Testing: Read the Guardrails; confirm consistency with Steps 2.1–2.2.

**Phase 2 Deliverable**: `execute` no longer re-challenges an already-vetted plan,
and a challenge run at execute-time is persisted back into the plan.

### Phase 3: Consistency pass + changelog
**Goal**: All assets consistent; change logged.
**Status**: ✅ Completed 2026-07-19 (Steps 3.1–3.2).

1. **[x] Step 3.1**: Cross-check the three assets for consistent wording of the field
   values and the offer semantics.
   - Files: `.claude/skills/plan/SKILL.md`, `.claude/skills/execute/SKILL.md`,
     `.claude/templates/plan.template.md`
   - Description: Ensure the closed four-value enum, its meanings, the "canonical
     source = plan skill Status Lifecycle" statement, and the execute "starts with
     `not run`" match rule are described consistently wherever referenced. The plan
     skill is the single source of truth; the other assets must not contradict it.
   - Testing: Grep for `Challenge`/`not run` across the three files; confirm the
     value vocabulary and match rule are aligned.

2. **[x] Step 3.2**: Log the change via the `claude-meta-changelog` skill.
   - Files: `meta/MET-nnnn_*.md` (created by the skill)
   - Description: Invoke `claude-meta-changelog` to record the skill/template edits.
   - Testing: Confirm a MET entry exists describing the change.

**Phase 3 Deliverable**: Fully consistent, changelogged workflow update.

## 6. Testing Strategy

### Unit Tests
- N/A — no code. Verification is by reading the edited skill/template text and
  mentally walking the CREATE, `→ accepted`, and `/execute` paths.

### Integration Tests
- N/A.

### Manual Testing
- Create a throwaway plan via `/plan`; confirm `**Challenge**: not run` and the
  `### Adversarial Challenge` subsection appear.
- Resolve questions and accept via `/plan update`; confirm the challenge is
  offered and the field + findings summary are written.
- Run `/execute` on that plan; confirm the challenge offer is skipped because the
  field starts with `passed`/`revised`/`waived`.
- Repeat `/execute` on a plan whose field is `not run` (or missing); confirm the
  offer appears, and after running it confirm the field is written back (no longer
  `not run`).

## 7. Risks and Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Existing plans lack the `**Challenge**:` field; execute can't read it | Low | Medium | Step 2.1 treats a missing field as `not run` (execute offers as before) — match rule "starts with `not run` ⇒ offer" fails safe |
| Field-value vocabulary drifts between the three assets | Low | Medium | Plan skill's Status Lifecycle is the single canonical source; Phase 3 consistency pass + grep check |
| Execute-time challenge leaves field at `not run`, re-offering forever | Medium | Medium | Step 2.2 closes the loop — execute writes the field back via the plan skill after a fallback challenge |
| Users perceive the acceptance offer as a mandatory gate | Low | Low | Explicit "offer, not a gate" wording in the plan skill, mirroring existing patterns |
| Challenge that passed at acceptance goes stale if plan is heavily revised while `in progress` | Low | Low | User can always request a fresh challenge explicitly at execute-time (Step 2.1) |

## 8. Documentation Updates

- [ ] `.claude/templates/plan.template.md` — add `**Challenge**:` header field +
      `### Adversarial Challenge` subsection in section 4
- [ ] `.claude/skills/plan/SKILL.md` — CREATE emits field; Status Lifecycle defines
      the canonical closed four-value enum; `→ accepted` offers + records the
      challenge
- [ ] `.claude/skills/execute/SKILL.md` — Step 2 conditional (starts-with `not run`
      match), execute-side write-back loop, Guardrail reconciled
- [ ] `meta/MET-nnnn` — changelog entry (via `claude-meta-changelog`)
- [ ] No CLAUDE.md change expected (no new architectural pattern in app code)

## 9. Rollout Plan

1. Merge as a normal feature branch → develop; assets take effect immediately for
   subsequent `/plan` and `/execute` invocations.
2. No runtime monitoring — these are harness workflow assets.
3. Rollback: revert the skill/template edits; `execute`'s prior unconditional
   offer and the template without the field are fully backward-compatible.

## 10. Revision History

| Date | Updated By | Changes |
|------|------------|---------|
| 2026-07-19 | AI Agent | Accepted plan (no unresolved questions); status draft → accepted. |
| 2026-07-19 | AI Agent | Folded in 3 adversarial-challenge findings: (1) close the execute-side write-back loop (new Step 2.2); (2) declare the four `Challenge` values a closed enum owned by the plan skill, execute matches "starts with `not run`", missing ⇒ `not run` (Steps 1.2, 2.1, 3.1); (3) add the `### Adversarial Challenge` subsection to the template in Phase 1 (Step 1.1). Propagated to sections 4, 5, 6, 7, 8. |
| 2026-07-19 | AI Agent | Executed all 8 steps (autonomous). Status accepted → in progress → implemented. Edited plan template + plan/execute skills; logged MET-0020. One deviation (extra edit to execute "Plan source" header list) recorded in [EXEC-0014](EXEC-0014_adversarial-challenge-at-acceptance.md). |

---

**Note**: This plan should be reviewed and approved before implementation begins.
