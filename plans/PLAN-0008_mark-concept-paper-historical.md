# Feature: Mark doc/concept as historical, point to arc42 docs

**Plan ID**: PLAN-0008
**Issue**: #161
**Status**: accepted
**Created**: 2026-07-17
**Last Updated**: 2026-07-17

## 1. Feature Description

### Original Issue Description

## Problem

`doc/concept/*.adoc` covers only ~4 arc42-style chapters and predates the `flows`, `crunchers`, and `dasm` subsystems.

## Impact

Readers may treat the concept paper as current and miss recent subsystems entirely.

## Suggested fix

Mark `doc/concept/` as historical and point it at the complete, current architecture reference in [`doc/arc42/`](../doc/arc42/README.md).

## Source

Identified and verified while writing the arc42 technical documentation (§11 Risks and Technical Debt, item D6). See [`doc/arc42/11_risks_and_technical_debt.md`](../doc/arc42/11_risks_and_technical_debt.md). Note: the arc42 README and §11 already flag this staleness.

### Overview
`doc/concept/index.adoc` and its four included `.adoc` chapters (`_02_overview`, `_03_processes`, `_04_requirements`, `_10_architecture`) are an early, partial concept paper. It predates the `flows`, `crunchers` (processors/image, charpad, spritepad, goattracker), and `dasm` subsystems, and is no longer maintained. `doc/arc42/` (12 sections, current) is the authoritative architecture reference and already links back to `doc/concept/` as "earlier, partial" (`doc/arc42/README.md:25`). The concept paper itself carries no such pointer or historical marker, so a reader who lands there via search or an old link has no signal that it's stale and no way forward to the current docs.

### Requirements
- Add a clear, prominent "historical / superseded" notice at the top of `doc/concept/index.adoc` (the AsciiDoc entry point) that links to `doc/arc42/README.md`.
- Do not delete or restructure the existing concept-paper content — this is a labeling/pointer change, not a content rewrite.
- Close the loop with `doc/arc42/11_risks_and_technical_debt.md` item D6, which currently describes this exact fix as outstanding.

### Success Criteria
- Anyone opening `doc/concept/index.adoc` (rendered or raw) immediately sees it is historical and is pointed to `doc/arc42/README.md`.
- D6 in `doc/arc42/11_risks_and_technical_debt.md` reflects that the fix has been applied (or the row is removed/updated per the resolution convention used for closed items in that table).
- No existing content is lost; the change is additive/labeling only.

## 2. Root Cause Analysis

The `doc/arc42/` set (12 sections, built under PLAN-0002) fully superseded `doc/concept/` but the two directories were never cross-linked from the older side. `doc/arc42/README.md:25` and `doc/arc42/11_risks_and_technical_debt.md:14` (item D6) both already document this gap as known debt — this plan is the follow-up issue that D6 anticipates.

### Current State
- `doc/concept/index.adoc:10` has a generic `WARNING: This document is under development!` — not a "this is stale, go here instead" notice.
- No file in `doc/concept/` references `doc/arc42/`.
- `doc/arc42/README.md` and `doc/arc42/11_risks_and_technical_debt.md` reference `doc/concept/` as partial/superseded, but that's a one-way pointer.

### Desired State
- `doc/concept/index.adoc` opens with a clear historical/superseded banner linking to `doc/arc42/README.md`, replacing or supplementing the existing "under development" warning.
- D6's row in `doc/arc42/11_risks_and_technical_debt.md` is updated to reflect resolution.

### Gap Analysis
- Add an AsciiDoc `WARNING` (or similar admonition) block at the top of `doc/concept/index.adoc` pointing to `doc/arc42/README.md`.
- Update `doc/arc42/11_risks_and_technical_debt.md` D6 row to mark it resolved (exact convention TBD — see Unresolved Questions).

## 3. Relevant Code Parts

### Existing Components
- **`doc/concept/index.adoc`**: AsciiDoc entry point that includes the four concept chapters.
  - Location: `doc/concept/index.adoc`
  - Purpose: Primary landing point for the concept paper; where the historical notice must go.
  - Integration Point: Insert/replace admonition block near line 10, before the `include::` directives.
- **`doc/arc42/README.md`**: Current arc42 index; already links to `doc/concept/` as superseded (line 25).
  - Location: `doc/arc42/README.md`
  - Purpose: Target of the new pointer from `doc/concept/index.adoc`.
- **`doc/arc42/11_risks_and_technical_debt.md`**: Contains the D6 debt-item row that names this exact fix.
  - Location: `doc/arc42/11_risks_and_technical_debt.md:14`
  - Purpose: Should be updated once the fix lands so the risk register stays accurate (per `CLAUDE.md`'s instruction to keep arc42 docs authoritative).

### Architecture Alignment
- **Domain**: Documentation only — no domain module, use case, port, or adapter changes.
- **Use Cases**: None.
- **Ports**: None.
- **Adapters**: None.

### Dependencies
- None (pure documentation edit, no build/toolchain impact). Note `documentation.yml` regenerates AsciiDoc docs on `master` — confirm the concept paper is part of that pipeline or standalone.

## 4. Questions and Clarifications

### Self-Reflection Questions
- **Q**: Does `doc/concept/` participate in the `documentation.yml` GitHub Actions generation/deploy pipeline?
  - **A**: Not verified yet in codebase analysis for this plan — needs a quick check of `.github/workflows/documentation.yml` during implementation, but since this is a content-only AsciiDoc edit (no structural/include changes), it's low risk either way.
- **Q**: Should the four included chapter files (`_02_overview.adoc` etc.) individually get a notice too, or is the `index.adoc` banner sufficient?
  - **A**: `index.adoc` is the sole entry point (all chapters are `include::`d into it via `{includedir}`), so a single banner at the top of `index.adoc` is sufficient and avoids duplicating the same notice four times. Direct navigation to an individual chapter file outside the rendered doc is an edge case not worth covering.
- **Q**: How should D6 in `doc/arc42/11_risks_and_technical_debt.md` be marked resolved?
  - **A**: The table already has an established convention from D2/D4/D5: append `✅ **RESOLVED**` to the title cell, rewrite the description cell as "Was: {old problem}. Now: {resolved state}", and replace the last column with `Resolved via [PLAN-0008](../../plans/PLAN-0008_mark-concept-paper-historical.md) issue #161`. No need to remove the row — resolved D-items are kept as history, consistent with D2/D4/D5.

### Unresolved Questions
{None outstanding.}

### Design Decisions
- **Decision**: Banner placement and format in `doc/concept/index.adoc`.
  - **Chosen**: Replace the existing `WARNING: This document is under development!` line with a single `WARNING` admonition stating the document is historical/superseded and linking to `link:../arc42/README.md[doc/arc42/]`.
  - **Rationale**: The "under development" warning is stale in spirit too (the doc isn't being developed, it's superseded), so a single clear historical notice is less confusing than two stacked admonitions. Confirmed by user.

## 5. Implementation Plan

### Phase 1: Add historical notice and close the loop (single deliverable)
**Goal**: Make `doc/concept/` unambiguously marked as historical and pointed at `doc/arc42/`, and update the risk register that tracks this as debt.

1. **Step 1.1**: Add historical/superseded banner to `doc/concept/index.adoc`
   - Files: `doc/concept/index.adoc`
   - Description: Replace line 10 (`WARNING: This document is under development!`) with:
     `WARNING: This document is historical and superseded. It predates the flows, crunchers, and dasm subsystems and is no longer maintained. See the current, complete architecture reference at link:../arc42/README.md[doc/arc42/].`
   - Testing: Visual check of rendered AsciiDoc (if a local AsciiDoc renderer/preview is available) or careful manual review of markup syntax; confirm relative link path resolves correctly given `doc/concept/` → `doc/arc42/README.md`.

2. **Step 1.2**: Update D6 in the arc42 risk register
   - Files: `doc/arc42/11_risks_and_technical_debt.md`
   - Description: Update the D6 row (line 14) following the D2/D4/D5 convention: append `✅ **RESOLVED**` to the title cell, rewrite the description as "Was: {original D6 problem}. Now: {resolved state — doc/concept/index.adoc carries a historical notice pointing to doc/arc42/}", and set the last column to `Resolved via [PLAN-0008](../../plans/PLAN-0008_mark-concept-paper-historical.md) issue #161`.
   - Testing: Manual review; confirm the table still renders correctly as Markdown.

3. **Step 1.3**: Verify no other doc cross-references need updating
   - Files: grep for `doc/concept` references across `doc/`, `README.md`, `CLAUDE.md`
   - Description: Confirm `doc/arc42/README.md:25` wording is still accurate after the change (it already says "superseded" - probably no change needed there), and check no other doc points into `doc/concept/` expecting current content.
   - Testing: `grep -rn "doc/concept" doc/ README.md CLAUDE.md` (or ripgrep equivalent) and manual review of any hits.

**Phase 1 Deliverable**: `doc/concept/index.adoc` carries a clear historical notice linking to `doc/arc42/README.md`; `doc/arc42/11_risks_and_technical_debt.md` D6 reflects resolution. Fully mergeable as a single documentation-only PR.

## 6. Testing Strategy

### Unit Tests
- None applicable — documentation-only change.

### Integration Tests
- None applicable.

### Manual Testing
- Render or visually inspect `doc/concept/index.adoc` to confirm AsciiDoc admonition syntax is valid and the link to `doc/arc42/README.md` resolves.
- Confirm `doc/arc42/11_risks_and_technical_debt.md` table markdown still renders correctly (no broken table syntax).
- Grep-check for any other stale cross-references into `doc/concept/`.

## 7. Risks and Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Broken AsciiDoc admonition/link syntax | Low | Low | Manual review; keep syntax consistent with existing `WARNING:` usage already in the file |
| `documentation.yml` CI pipeline processes `doc/concept/` in a way sensitive to this edit | Low | Low | Check workflow file during implementation (Step 1.3); change is content-only, no structural/include changes |
| D6 table edit breaks Markdown table formatting | Low | Low | Careful manual diff review before commit |

## 8. Documentation Updates

- [x] Update relevant architectural docs — this plan's entire deliverable *is* the doc update (`doc/concept/index.adoc`, `doc/arc42/11_risks_and_technical_debt.md`)
- [ ] Update README if needed — not expected, but verify per Step 1.3
- [ ] Update CLAUDE.md if adding new patterns — not applicable, no new pattern introduced

## 9. Rollout Plan

1. Documentation-only change; merge via normal PR flow (feature branch → PR → develop → master). No runtime/build impact, no release coordination needed.
2. Nothing to monitor post-merge beyond normal doc review.
3. Rollback: trivial revert of the single commit/PR if the wording or link is found to be wrong.

## 10. Revision History

| Date | Updated By | Changes |
|------|------------|---------|
| 2026-07-17 | AI Agent | Initial plan created and synced to issue #161 |
| 2026-07-17 | AI Agent | Resolved D6-update-convention question (follows D2/D4/D5 pattern); resolved banner-wording question (replace existing WARNING); status moved draft → accepted |

---

**Note**: This plan should be reviewed and approved before implementation begins.
