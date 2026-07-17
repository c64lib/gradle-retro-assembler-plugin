# Feature: Document the `crunchers` domain in `doc/kb/domain.md`

**Plan ID**: PLAN-0009
**Issue**: #156
**Status**: accepted
**Last Updated**: 2026-07-17
**Created**: 2026-07-17

## 1. Feature Description

### Original Issue

> ## Problem
>
> `doc/kb/domain.md` lists the project's bounded contexts/domains but omits `crunchers`, even though `crunchers/exomizer` exists in code with `CrunchMemUseCase`/`CrunchRawUseCase`.
>
> ## Impact
>
> Agents/contributors reading only the knowledge base can miss an entire bounded context.
>
> ## Suggested fix
>
> Add `crunchers` to `doc/kb/domain.md` (or point the kb at the complete inventory in [`doc/arc42/05_building_block_view.md`](../doc/arc42/05_building_block_view.md)).
>
> ## Source
>
> Identified and verified against code while writing the arc42 technical documentation (§11 Risks and Technical Debt, item D1). See [`doc/arc42/11_risks_and_technical_debt.md`](../doc/arc42/11_risks_and_technical_debt.md).

### Overview
`doc/kb/domain.md` is a short, per-domain-paragraph knowledge-base note. It lists `compilers`, `dependencies`, `emulators`, `testing`, `processors` (with its four subdomains), and `flows` — but has no paragraph for `crunchers`, even though the domain exists in code (`crunchers/exomizer`) and is already documented in the arc42 set. Add a `crunchers` paragraph, in the same one-paragraph-per-domain style as the rest of the file, and cross-reference the arc42 building-block view for the authoritative, diagrammed inventory.

### Requirements
- `doc/kb/domain.md` gains a paragraph describing the `crunchers` domain and its `exomizer` subdomain, matching the file's existing prose style (see `processors` paragraph for the subdomain pattern).
- The new paragraph is consistent with how `crunchers` is already described in `doc/arc42/05_building_block_view.md` (an asset-compression domain used by `flows`, depending on `shared`).
- Once fixed, item D1 in `doc/arc42/11_risks_and_technical_debt.md` should be marked resolved (following the same pattern used for D6 in that table), since this plan removes the gap it documents.

### Success Criteria
- Reading only `doc/kb/domain.md` (without the arc42 set) is enough to learn that `crunchers` is a bounded context, what it does, and that `exomizer` is its current subdomain.
- No other content in `doc/kb/domain.md` is altered beyond the addition.
- `doc/arc42/11_risks_and_technical_debt.md` D1 row reflects resolution, consistent with the D6 precedent.

## 2. Root Cause Analysis

This is a documentation-completeness gap, not a code defect. `doc/kb/domain.md` predates the `crunchers` domain's extraction/introduction and was never updated when `crunchers/exomizer` was added. The arc42 documentation effort (which produced `doc/arc42/`) already re-inventoried all domains and correctly includes `crunchers`, and explicitly flagged this exact gap as D1 while intentionally leaving the kb file unfixed (arc42 work was scoped to be documentation-only and revert-safe per `doc/arc42/11_risks_and_technical_debt.md:29`).

### Current State
`doc/kb/domain.md:1-22` describes six domains: `compilers`, `dependencies`, `emulators`, `testing`, `processors` (+ 4 subdomains), `flows`. `crunchers` is absent entirely.

### Desired State
`doc/kb/domain.md` describes seven domains, adding a `crunchers` paragraph (with its `exomizer` subdomain) in the same style as the existing entries.

### Gap Analysis
One paragraph needs to be added to `doc/kb/domain.md`, sourced from the already-correct arc42 description, plus a one-line resolution note in the risks/tech-debt table for traceability.

## 3. Relevant Code Parts

### Existing Components
- **`doc/kb/domain.md`**: The file to fix — currently 22 lines, one paragraph per domain.
  - Location: `doc/kb/domain.md`
  - Purpose: Lightweight knowledge-base reference to the project's bounded contexts.
  - Integration Point: Insert a new paragraph, most naturally after the `flows` paragraph (end of file) or after `processors` (grouping it near other asset-related domains) — see Design Decisions below.
- **`doc/arc42/05_building_block_view.md`**: Authoritative, already-correct description of `crunchers` (lines 26, 39, 49, 56, 74, 77, 88) — source of truth for what to say about the domain.
  - Location: `doc/arc42/05_building_block_view.md`
  - Purpose: Confirms `crunchers` = `:crunchers:exomizer` + `:crunchers:exomizer:adapters:in:gradle`; depends on `shared`; is delegated to by `flows` via ports; note at line 77 explicitly calls out the `doc/kb/domain.md` gap this plan fixes.
- **`doc/arc42/11_risks_and_technical_debt.md`**: Tracks this gap as debt item D1 (line 9).
  - Location: `doc/arc42/11_risks_and_technical_debt.md`
  - Purpose: Should be updated to mark D1 resolved once this plan ships, following the D6-resolved pattern (line 14) used for PLAN-0008.
- **`crunchers/exomizer/**`**: The actual domain code being documented (`CrunchMemUseCase.kt`, `CrunchRawUseCase.kt`, `ExecuteExomizerPort.kt`, Gradle adapters `CrunchMem.kt`/`CrunchRaw.kt`).
  - Location: `crunchers/exomizer/src/main/kotlin/...`
  - Purpose: Ground truth for what the domain does (compresses/crunches binaries via the Exomizer tool, exposed as Gradle tasks and consumable from `flows`).

### Architecture Alignment
- **Domain**: Documentation only — no domain code changes. Touches `doc/kb/` (legacy-but-still-read notes) and `doc/arc42/` (current authoritative docs, tech-debt tracking table).
- **Use Cases**: None created/modified.
- **Ports**: None.
- **Adapters**: None.

### Dependencies
- None — pure Markdown edit, no build/test impact.

## 4. Questions and Clarifications

### Self-Reflection Questions
- **Q**: Does `crunchers` have subdomains like `processors` does?
  - **A**: Currently only one, `exomizer` (per `doc/arc42/05_building_block_view.md:74`, `:crunchers:exomizer` and its gradle adapter). The paragraph should follow the `processors` pattern (domain sentence + subdomain sentence) so it reads consistently if more crunchers are added later.
- **Q**: Should this plan also fix D1 in the risks/tech-debt doc?
  - **A**: Yes — PLAN-0008 already established the precedent of marking a resolved debt item inline (D6, `doc/arc42/11_risks_and_technical_debt.md:14`) rather than deleting the row, preserving history. D1 should follow the same pattern once `doc/kb/domain.md` is fixed.
- **Q**: Is `doc/kb/domain.md` still an actively maintained file, or should this plan instead just point readers to arc42 (per the issue's alternative suggestion)?
  - **A**: The issue offers two options — add the paragraph, or point the kb at arc42's complete inventory. Given the kb file's existing style is self-contained per-domain prose (not a redirect), and every other domain already has its own paragraph there, adding the `crunchers` paragraph directly keeps the file internally consistent. A cross-reference to arc42 can be added as a supplementary "see also" note without abandoning the existing style.

### Unresolved Questions
_(none — resolved during planning; see below)_

### Design Decisions
- **Decision**: Where in the file to place the new `crunchers` paragraph, and whether to add a cross-reference to arc42.
  - **Options**:
    - A. Append after the `flows` paragraph (end of file, matches insertion-order-of-discovery).
    - B. Insert after the `processors` paragraph, grouping it with other asset-related domains (`processors` extracts assets, `crunchers` compresses them — a natural pipeline pairing).
    - C. Either placement, plus append a short "See also" line pointing to `doc/arc42/05_building_block_view.md` for the full, diagrammed domain inventory.
  - **Recommendation**: B + C — place `crunchers` right after `processors` (reflects the real pipeline: processors extract assets → crunchers compress them → flows orchestrates both), and add a brief closing cross-reference to the arc42 building-block view so the kb file stays lightweight while pointing to the authoritative source for anyone wanting the full picture.

## 5. Implementation Plan

### Phase 1: Documentation fix (single mergeable deliverable)
**Goal**: Close the `crunchers` documentation gap in the knowledge base and record the resolution in the tech-debt tracker.

1. **Step 1.1**: Add the `crunchers` paragraph to `doc/kb/domain.md`
   - Files: `doc/kb/domain.md`
   - Description: Insert a new paragraph immediately after the `processors`-domain paragraph (after line 17, before the `flows` paragraph), describing `crunchers` as the domain responsible for compressing/crunching binary assets, with `exomizer` as its current subdomain (wrapping the Exomizer compression tool). Match the existing prose style exactly (one domain sentence, one subdomain sentence, no headers/bullets). Append a short closing line at the end of the file pointing to `doc/arc42/05_building_block_view.md` for the complete, diagrammed domain inventory.
   - Testing: Manual read-through; confirm the new paragraph reads consistently with neighboring paragraphs and accurately reflects `crunchers/exomizer/src/main/kotlin/.../usecase/{CrunchMemUseCase,CrunchRawUseCase}.kt`.

2. **Step 1.2**: Mark D1 resolved in the risks/tech-debt table
   - Files: `doc/arc42/11_risks_and_technical_debt.md`
   - Description: Update the D1 row (line 9) to follow the D6 precedent (line 14) — append "✅ **RESOLVED**" to the item name and update the "Suggested follow-up" cell to reference this plan/issue, e.g. `Resolved via PLAN-0009 issue #156`.
   - Testing: Manual read-through; verify table formatting stays intact (pipe alignment not required, GitHub-flavored Markdown renders regardless).

**Phase 1 Deliverable**: `doc/kb/domain.md` documents all seven domains including `crunchers`; `doc/arc42/11_risks_and_technical_debt.md` D1 is marked resolved. Fully mergeable as a single documentation-only PR.

## 6. Testing Strategy

### Unit Tests
- Not applicable — no code changes.

### Integration Tests
- Not applicable.

### Manual Testing
- Read `doc/kb/domain.md` end-to-end after the edit to confirm consistent tone/style and factual accuracy against `crunchers/exomizer` source code.
- Confirm the arc42 cross-reference link resolves correctly (relative path from `doc/kb/domain.md` to `doc/arc42/05_building_block_view.md`).
- Spot-check `doc/arc42/11_risks_and_technical_debt.md` renders correctly (no broken table) after the D1 edit.

## 7. Risks and Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Paragraph drifts out of sync with code again if `crunchers` gains new subdomains later | Low | Medium | Cross-reference to arc42 §5 (kept current per `CLAUDE.md` architecture-doc-update rule) gives readers a fallback source of truth |
| Wording inconsistency with the rest of the file | Low | Low | Follow the `processors` paragraph as a direct style template |

## 8. Documentation Updates

- [x] This plan *is* the documentation update.
- [ ] Update `doc/kb/domain.md` (Phase 1, Step 1.1)
- [ ] Update `doc/arc42/11_risks_and_technical_debt.md` D1 status (Phase 1, Step 1.2)
- [ ] No CLAUDE.md changes needed (no architecture/pattern change, purely closing a known doc gap)

## 9. Rollout Plan

1. Single documentation-only commit/PR; no build, test, or runtime impact — safe to merge immediately after review.
2. Nothing to monitor post-merge.
3. Rollback: revert the commit if wording needs rework; no data or state involved.

## 10. Revision History

| Date | Updated By | Changes |
|------|------------|---------|
| 2026-07-17 | AI Agent | Plan created and refined (placement + arc42 cross-reference, D1 tech-debt resolution) via interactive Q&A; both decisions confirmed matching original recommendations. Status set to accepted. |

---

**Note**: This plan should be reviewed and approved before implementation begins.
