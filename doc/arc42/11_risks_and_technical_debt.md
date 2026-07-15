# 11. Risks and Technical Debt

This is an **honest debt register**. Every item below was verified against the current code (path/line evidence given) before being recorded. Each carries a suggested follow-up so the documentation drives cleanup rather than hiding known issues. No item here is fixed by this documentation effort — [PLAN-0002](../../plans/PLAN-0002_arc42-technical-documentation.md) is documentation-only.

## 11.1 Technical debt (verified)

| # | Item | Evidence | Impact | Suggested follow-up |
|---|------|----------|--------|---------------------|
| D1 | **`crunchers` domain missing from the knowledge base** | `doc/kb/domain.md` lists the domains but omits `crunchers` (`crunchers/exomizer` exists in code with `CrunchMemUseCase`/`CrunchRawUseCase`) | Agents/contributors reading only the kb can miss an entire bounded context | Issue: add `crunchers` to `doc/kb/domain.md` (or point kb at this arc42 §5) |
| D2 | **Inconsistent base package in `spritepad`** | `processors/spritepad` sources use package `com.c64lib.rbt.processors.spritepad...` (e.g. `ProcessSpritepadUseCase.kt`, `SPD4Processor.kt`) while every other domain uses `com.github.c64lib.rbt...` | Breaks the otherwise uniform package convention; surprises tooling and readers | Issue: rename `spritepad` package to `com.github.c64lib.rbt.processors.spritepad` |
| D3 | **`fllter` typo in a shared package name** | `shared/gradle/.../shared/gradle/fllter/` (contains `BinaryInterleaver.kt`, `Nybbler.kt`) — should be `filter` | Cosmetic but permanent-looking; every import repeats the typo | Issue: rename `fllter` → `filter` (touches importers in `shared:gradle` and infra) |
| D4 | **Flows ports live under `domain/port`, not `usecase/port`** | Flows port interfaces are at `flows/src/main/kotlin/.../flows/domain/port/` (`AssemblyPort`, `CharpadPort`, `ExomizerPort`, …), whereas other domains place ports under `usecase/port` | Inconsistent port location complicates the "ports sit next to use cases" rule stated in [§8.1](08_crosscutting_concepts.md) | Issue: decide the canonical port location and align (either move flows ports or document the flows-as-orchestrator exception) |
| D5 | **Single large `afterEvaluate` wiring block** | `RetroAssemblerPlugin.kt` is ~223 lines with all use-case construction and task wiring inline in one `afterEvaluate` | Growing composition root; harder to read as domains are added (a known trade-off of manual DI — see [AD-4](09_architecture_decisions.md)) | Issue: extract per-domain wiring helpers while keeping DI manual and grep-able |
| D6 | **Stale concept paper** | `doc/concept/*.adoc` covers only ~4 arc42-style chapters and predates `flows`, `crunchers`, and `dasm` | Readers may treat it as current and miss recent subsystems | Issue: mark `doc/concept/` as historical and point it at this arc42 set (this §11 and [README](README.md) already flag it) |

> The flows exception set was also confirmed while documenting: `StepValidationException`/`StepExecutionException` (`Flow.kt`) **and** `FlowValidationException` (`FlowDependencyGraph.kt`) — all three exist and are described in [§8.5](08_crosscutting_concepts.md#85-error-handling). This is not debt; it is recorded here because the plan preview named only two.

## 11.2 Risks

| # | Risk | Likelihood | Impact | Mitigation |
|---|------|------------|--------|------------|
| R1 | **Documentation drifts from code** as domains are added/changed | High | Medium | Inventories are table- and path-anchored for easy diffing; the maintainer note in [§8](08_crosscutting_concepts.md) and [`CLAUDE.md`](../../CLAUDE.md) instructs updating arc42 §5/§8 on architectural change |
| R2 | **Mermaid diagrams become too large to read** (50+ modules) | Medium | Medium | Diagrams are layered (§5 L1 domains only, L2 per-domain hexagons); no single mega-diagram |
| R3 | **External toolchain contract changes** (Kick Assembler / VICE / Exomizer / DASM CLI or formats) | Medium | High | Toolchain invocation is isolated in outbound adapters, so a change is contained to one adapter; version pinning (D-none — see [§10](10_quality_requirements.md) Q3) reduces surprise upgrades |
| R4 | **Scope creep into fixing D1–D6 while documenting** | Medium | Medium | Explicitly out of scope for PLAN-0002; each is filed as a follow-up issue instead of a code change here |

## 11.3 Notes

Items D1–D6 are deliberately **not** fixed in the arc42 documentation branch to keep it review-scoped and revert-safe (documentation-only). Opening the suggested follow-up issues is the intended next step after this doc set merges.
