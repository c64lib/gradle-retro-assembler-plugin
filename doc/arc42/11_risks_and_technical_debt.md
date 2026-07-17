# 11. Risks and Technical Debt

This is an **honest debt register**. Every item below was verified against the current code (path/line evidence given) before being recorded. Each carries a suggested follow-up so the documentation drives cleanup rather than hiding known issues. No item here is fixed by this documentation effort — [PLAN-0002](../../plans/PLAN-0002_arc42-technical-documentation.md) is documentation-only.

## 11.1 Technical debt (verified)

| # | Item | Evidence | Impact | Suggested follow-up |
|---|------|----------|--------|---------------------|
| D1 | **`crunchers` domain missing from the knowledge base** | `doc/kb/domain.md` lists the domains but omits `crunchers` (`crunchers/exomizer` exists in code with `CrunchMemUseCase`/`CrunchRawUseCase`) | Agents/contributors reading only the kb can miss an entire bounded context | Issue: add `crunchers` to `doc/kb/domain.md` (or point kb at this arc42 §5) |
| D2 | **Inconsistent base package in `spritepad`** ✅ **RESOLVED** | `processors/spritepad` sources previously used package `com.c64lib.rbt.processors.spritepad...` while other domains used `com.github.c64lib.rbt...` — now all `spritepad` source (main, test, `adapters/in/gradle`) and all consumers in `flows/adapters/out/spritepad` use the uniform `com.github.c64lib.rbt.processors.spritepad...` root | Was: breaks package convention. Now: uniform across all domains | Resolved via [PLAN-0004](../../plans/PLAN-0004_rename-spritepad-package-root.md) issue #157 |
| D3 | **`fllter` typo in a shared package name** | `shared/gradle/.../shared/gradle/fllter/` (contains `BinaryInterleaver.kt`, `Nybbler.kt`) — should be `filter` | Cosmetic but permanent-looking; every import repeats the typo | Issue: rename `fllter` → `filter` (touches importers in `shared:gradle` and infra) |
| D4 | **Flows ports live under `domain/port`, not `usecase/port`** ✅ **RESOLVED** | Flows port interfaces previously lived at `flows/src/main/kotlin/.../flows/domain/port/` (`AssemblyPort`, `CharpadPort`, `ExomizerPort`, …), whereas other domains place ports under `usecase/port` — now all flows ports are at `flows/src/main/kotlin/.../flows/usecase/port/` | Was: inconsistent port location complicates the "ports sit next to use cases" rule. Now: uniform across all domains | Resolved via [PLAN-0005](../../plans/PLAN-0005_move-flows-ports-to-usecase-port.md) issue #159 |
| D5 | **Single large `afterEvaluate` wiring block** ✅ **RESOLVED** | `RetroAssemblerPlugin.kt` previously had all use-case construction and task wiring inline in one ~110-line `afterEvaluate` block — now split into six private per-domain wiring helpers (`wireDependencies`, `wirePreprocess`, `wireSources`, `wireSpecAndTest`, `wireBuild`, `wireFlows`) that `apply()` orchestrates, threading task/settings handles between them | Was: growing composition root, harder to read as domains are added. Now: each domain's wiring is a small, named, independently readable function; DI stays manual and grep-able (still honors [AD-4](09_architecture_decisions.md)) | Resolved via [PLAN-0006](../../plans/PLAN-0006_extract-plugin-wiring-helpers.md) issue #160 |
| D6 | **Stale concept paper** ✅ **RESOLVED** | `doc/concept/*.adoc` covers only ~4 arc42-style chapters and predates `flows`, `crunchers`, and `dasm` | Was: readers may treat it as current and miss recent subsystems. Now: `doc/concept/index.adoc` carries a historical/superseded notice pointing to this arc42 set | Resolved via [PLAN-0008](../../plans/PLAN-0008_mark-concept-paper-historical.md) issue #161 |

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
