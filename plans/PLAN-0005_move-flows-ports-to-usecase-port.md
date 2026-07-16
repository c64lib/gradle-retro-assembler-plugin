# Feature: Move `flows` port interfaces from `domain/port` to `usecase/port`

**Plan ID**: PLAN-0005
**Issue**: #159
**Status**: implemented
**Created**: 2026-07-16
**Last Updated**: 2026-07-16

## 1. Feature Description

### Original Issue Description

> ## Problem
>
> Flows port interfaces live at `flows/src/main/kotlin/.../flows/domain/port/` (`AssemblyPort`, `CharpadPort`, `ExomizerPort`, …), whereas every other domain places ports under `usecase/port`.
>
> ## Impact
>
> Inconsistent port location complicates the "ports sit next to use cases" convention stated in the architecture docs ([§8.1](../doc/arc42/08_crosscutting_concepts.md)).
>
> ## Suggested fix
>
> Decide the canonical port location and align — either move the flows ports to `usecase/port`, or explicitly document the flows-as-orchestrator exception if there's a good reason for it to differ.
>
> ## Source
>
> Identified and verified against code while writing the arc42 technical documentation (§11 Risks and Technical Debt, item D4). See [`doc/arc42/11_risks_and_technical_debt.md`](../doc/arc42/11_risks_and_technical_debt.md).

### Overview

The `flows` domain's 8 port interfaces (`AssemblyPort`, `CharpadPort`, `CommandPort`, `DasmAssemblyPort`, `ExomizerPort`, `GoattrackerPort`, `ImagePort`, `SpritepadPort`) live under `flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/port/`, while every other domain in the project places ports under `usecase/port` (e.g. `compilers/kickass/.../usecase/port/KickAssemblePort.kt`). This is a pure structural rename: move the 8 port files (and their directory) to `flows/src/main/kotlin/com/github/c64lib/rbt/flows/usecase/port/`, update their `package` declarations, and fix every import/reference across the 21 consuming files. No behavioral change.

### Requirements
- Move all 8 port interface files from `flows/src/main/kotlin/.../flows/domain/port/` to `flows/src/main/kotlin/.../flows/usecase/port/`.
- Update each file's `package` declaration from `com.github.c64lib.rbt.flows.domain.port` to `com.github.c64lib.rbt.flows.usecase.port`.
- Update every import statement across the 21 consuming files (domain steps, tests, out-adapters, in-adapter) to the new package.
- Fix the one fully-qualified-name reference in `ImageTask.kt:70` that isn't a plain `import` line.
- Update arc42 documentation (`05_building_block_view.md`, `building-blocks/flows.md`, `11_risks_and_technical_debt.md` item D4) to reflect the resolved state.
- No behavioral change: interface names, method signatures, and semantics stay identical.

### Success Criteria
- No `.kt` file anywhere in the repo references `com.github.c64lib.rbt.flows.domain.port`.
- All 8 port files live under `flows/src/main/kotlin/com/github/c64lib/rbt/flows/usecase/port/`.
- `./gradlew build` passes for `flows`, all `flows/adapters/out/*` submodules, `flows/adapters/in/gradle`, and `infra/gradle`.
- `./gradlew detekt` shows no new violations introduced by the move.
- arc42 docs no longer describe flows ports as living under `domain/port`, and D4 is marked resolved.

## 2. Root Cause Analysis

The `flows` domain was authored as an orchestrator layer coordinating other domains' use cases, and its ports were placed under `domain/port` — plausibly by analogy with "ports are part of the domain boundary" rather than following the repo-wide `usecase/port` convention used everywhere else. It compiles and works correctly either way, so nothing forced alignment until the arc42 documentation effort (item D4) surfaced the inconsistency.

### Current State
All 8 ports share `package com.github.c64lib.rbt.flows.domain.port`, one interface per file, under `flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/port/`:
- `AssemblyPort.kt`, `CharpadPort.kt`, `CommandPort.kt`, `DasmAssemblyPort.kt`, `ExomizerPort.kt`, `GoattrackerPort.kt`, `ImagePort.kt`, `SpritepadPort.kt`

21 files reference these ports:
- **Domain steps** (8): `AssembleStep.kt`, `CharpadStep.kt`, `CommandStep.kt`, `DasmStep.kt`, `ExomizerStep.kt`, `GoattrackerStep.kt`, `ImageStep.kt`, `SpritepadStep.kt` — each imports its own port.
- **Tests** (3): `AssembleStepTest.kt`, `CharpadStepTest.kt`, `ExomizerStepTest.kt` — import the port directly (other step tests do not).
- **Out-adapters** (5): `flows/adapters/out/{charpad,spritepad,image,goattracker,exomizer}/.../*Adapter.kt`.
- **In-adapter** (5): `DasmPortAdapter.kt`, `KickAssemblerPortAdapter.kt`, `GradleCommandPortAdapter.kt`, `FlowExomizerAdapter.kt` (also imports the unrelated `crunchers.exomizer.usecase.port.ExecuteExomizerPort` — distinct interface, unaffected), and `ImageTask.kt:70` — a fully-qualified cast (`... as com.github.c64lib.rbt.flows.domain.port.ImagePort`), not an `import` line, easy to miss with a naive import-only search.

`infra/gradle` has no direct references to any flows domain port — it only depends on `flows` via `compileOnly` project dependencies and imports `FlowTasksGenerator`/`FlowsExtension` from the in-adapter package.

Documentation currently states the old location explicitly:
- `doc/arc42/05_building_block_view.md:56` — "`flows` defines its own domain ports (...) "
- `doc/arc42/building-blocks/flows.md:19` — "All ports live under `.../flows/domain/port/`"
- `doc/arc42/building-blocks/flows.md:32` — explicit note pointing at D4 as the tracked inconsistency
- `doc/arc42/11_risks_and_technical_debt.md:12` — D4 row describing exactly this issue

### Desired State
All 8 flows ports live under `.../flows/usecase/port/` with package `com.github.c64lib.rbt.flows.usecase.port`, matching every other domain; all 21 consumers updated; arc42 docs reflect the new location and D4 is marked resolved (following the D2 "✅ RESOLVED" pattern from PLAN-0004).

### Gap Analysis
- Move 8 files to the new directory tree, fix their `package` declarations.
- Fix imports in 8 domain steps, 3 tests, 5 out-adapters, 4 in-adapter files (import lines) + 1 in-adapter file (FQN cast in `ImageTask.kt`).
- Update 3 documentation locations across 2 files (plus fix the D4 risk-register row).
- No `build.gradle.kts` changes expected — this is a pure Kotlin package move within the existing `flows` module boundary (confirmed by the earlier PLAN-0003/PLAN-0004 pattern of "no build.gradle.kts references package paths literally").

## 3. Relevant Code Parts

### Existing Components
- **8 port interfaces** — `flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/port/{AssemblyPort,CharpadPort,CommandPort,DasmAssemblyPort,ExomizerPort,GoattrackerPort,ImagePort,SpritepadPort}.kt`
  - Purpose: technology-hiding interfaces the flows orchestrator uses to delegate to each downstream domain (compilers, processors, crunchers).
  - Change: move to `flows/src/main/kotlin/com/github/c64lib/rbt/flows/usecase/port/`, update `package` declaration (line 25 in each file).
- **8 domain step classes** — `flows/src/main/kotlin/.../flows/domain/steps/*.kt`
  - Change: update the single port import in each (line ~30–33 depending on file).
- **3 step test files** — `flows/src/test/kotlin/.../flows/domain/steps/{AssembleStepTest,CharpadStepTest,ExomizerStepTest}.kt`
  - Change: update the port import (line ~29–32).
- **5 out-adapters** — `flows/adapters/out/{charpad,spritepad,image,goattracker,exomizer}/src/main/kotlin/.../*Adapter.kt`
  - Change: update the port import (one per file).
- **4 in-adapter files with import lines** — `flows/adapters/in/gradle/src/main/kotlin/.../{assembly/DasmPortAdapter.kt, assembly/KickAssemblerPortAdapter.kt, command/GradleCommandPortAdapter.kt, port/FlowExomizerAdapter.kt}`
  - Change: update the flows port import; `FlowExomizerAdapter.kt` also imports the unrelated `ExecuteExomizerPort` from `crunchers.exomizer` — leave that import untouched.
- **`ImageTask.kt`** — `flows/adapters/in/gradle/src/main/kotlin/.../tasks/ImageTask.kt:70`
  - Change: update the fully-qualified cast `com.github.c64lib.rbt.flows.domain.port.ImagePort` → `com.github.c64lib.rbt.flows.usecase.port.ImagePort`. No `import` line exists for this reference, so it must be caught separately from an import-only search.
- **Documentation** — `doc/arc42/05_building_block_view.md:56`, `doc/arc42/building-blocks/flows.md:19,21-30,32`, `doc/arc42/11_risks_and_technical_debt.md:12` (D4 row)
  - Change: update prose and the port-location table to reference `usecase/port`; mark D4 resolved with a pointer to this plan/issue, mirroring the D2 pattern from PLAN-0004.

### Architecture Alignment
- **Domain**: `flows` (orchestrator domain) — no orchestration logic changes, pure package/location rename.
- **Use Cases**: none renamed; only the ports these steps rely on move to sit alongside the (implicit) use-case layer per convention.
- **Ports**: all 8 flows ports relocate; interface names and method signatures unchanged.
- **Adapters**: `flows/adapters/in/gradle` and all 5 `flows/adapters/out/*` submodules consume the moved ports via updated imports only — no adapter logic changes.

### Dependencies
None — self-contained rename within the existing `flows` module and its adapter submodules. `infra/gradle`'s `compileOnly` project-path dependencies on `flows` and its adapters are unaffected since they reference Gradle project paths, not Kotlin package names.

## 4. Questions and Clarifications

### Self-Reflection Questions
- **Q**: Move the ports, or document a flows-as-orchestrator exception instead?
  - **A**: Move them. Confirmed with the user — aligning with the established `usecase/port` convention used by every other domain is preferred over carving out a documented exception, since there's no compelling architectural reason for flows to differ.
- **Q**: Does `infra/gradle` need any source-level edits?
  - **A**: No. It depends on `flows` only via `compileOnly` project dependencies and imports the in-adapter's `FlowTasksGenerator`/`FlowsExtension` classes by simple name, never any domain port FQN directly.
- **Q**: Are there any non-`import`-line references that a naive search would miss?
  - **A**: Yes — `ImageTask.kt:70` has a fully-qualified-name cast (`as com.github.c64lib.rbt.flows.domain.port.ImagePort`) with no corresponding `import` statement. Any find/replace approach must grep for the FQN pattern, not just lines starting with `import`.
  - Related possible pitfall: `FlowExomizerAdapter.kt` lives in a package literally named `...adapters.in.gradle.port` — an unrelated adapter package that happens to end in `.port`. Replacements must match on the full `com.github.c64lib.rbt.flows.domain.port` prefix, not a bare trailing `.port` segment, to avoid false positives.
- **Q**: Do the port interfaces have any visibility modifiers or directory/package mismatches that complicate the move?
  - **A**: No. All 8 are implicitly `public` (Kotlin default), and directory path matches package declaration exactly in all 8 files today — so the move requires both a physical file relocation and a `package` declaration edit; neither alone suffices.

### Unresolved Questions
None — scope is fully determined via Explore-agent analysis and the design decision has been confirmed with the user.

### Design Decisions
- **Decision**: Move flows ports to `usecase/port` vs. document a flows-as-orchestrator exception.
  - **Options**: (A) Move all 8 ports to `usecase/port`, matching every other domain. (B) Keep `domain/port` and document why flows differs.
  - **Chosen**: Option A — move the ports.
  - **Rationale**: User confirmed alignment with the existing repo-wide convention is preferred; there is no architectural justification found for flows needing a different port location, and keeping the inconsistency only postpones confusion for future readers.
- **Decision**: Single-phase vs. multi-phase execution.
  - **Options**: (A) Single phase covering all file moves + import fixes + doc updates, mirroring PLAN-0003/PLAN-0004's approach. (B) Split by module boundary (domain steps first, then adapters, then docs).
  - **Recommendation**: Option A — single phase. The full change touches ~30 files (8 moved + ~21 import-only fixes + 3 doc locations) with no intermediate state worth preserving as a separate merge; this exactly mirrors the already-executed PLAN-0004 rename in scope and shape.

## 5. Implementation Plan

### Phase 1: Move flows ports to `usecase/port` and fix all references (single deliverable)
**Goal**: Relocate all 8 flows port interfaces to `usecase/port`, update every consumer, and align documentation — with zero behavioral change.

1. **Step 1.1**: Move the 8 port interface files and fix their package declarations
   - Files:
     - `flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/port/{AssemblyPort,CharpadPort,CommandPort,DasmAssemblyPort,ExomizerPort,GoattrackerPort,ImagePort,SpritepadPort}.kt` → same names under `flows/src/main/kotlin/com/github/c64lib/rbt/flows/usecase/port/`
   - Description: create the `usecase/port` directory tree, move each file into it, update each file's `package` declaration from `com.github.c64lib.rbt.flows.domain.port` to `com.github.c64lib.rbt.flows.usecase.port`. Remove the now-empty `domain/port/` directory once confirmed empty.
   - Testing: files compile as part of step 1.4.

2. **Step 1.2**: Fix imports in domain steps and their tests
   - Files:
     - `flows/src/main/kotlin/.../flows/domain/steps/{AssembleStep,CharpadStep,CommandStep,DasmStep,ExomizerStep,GoattrackerStep,ImageStep,SpritepadStep}.kt`
     - `flows/src/test/kotlin/.../flows/domain/steps/{AssembleStepTest,CharpadStepTest,ExomizerStepTest}.kt`
   - Description: update each file's port import from `com.github.c64lib.rbt.flows.domain.port.*` to `com.github.c64lib.rbt.flows.usecase.port.*`.
   - Testing: files compile as part of step 1.4.

3. **Step 1.3**: Fix imports and the FQN reference in adapters
   - Files:
     - `flows/adapters/out/{charpad,spritepad,image,goattracker,exomizer}/src/main/kotlin/.../*Adapter.kt`
     - `flows/adapters/in/gradle/src/main/kotlin/.../{assembly/DasmPortAdapter.kt, assembly/KickAssemblerPortAdapter.kt, command/GradleCommandPortAdapter.kt, port/FlowExomizerAdapter.kt}`
     - `flows/adapters/in/gradle/src/main/kotlin/.../tasks/ImageTask.kt` (fully-qualified cast at line 70, not an import line)
   - Description: update each import from `com.github.c64lib.rbt.flows.domain.port.*` to `com.github.c64lib.rbt.flows.usecase.port.*`; in `ImageTask.kt`, update the FQN cast directly. Leave `FlowExomizerAdapter.kt`'s unrelated `crunchers.exomizer.usecase.port.ExecuteExomizerPort` import untouched.
   - Testing: repo-wide grep for the literal `flows.domain.port` (as a Kotlin package reference) returns zero matches after this step.

4. **Step 1.4**: Verify build and tests
   - Files: none (verification only)
   - Description: run `./gradlew :flows:build :flows:adapters:in:gradle:build :flows:adapters:out:charpad:build :flows:adapters:out:spritepad:build :flows:adapters:out:image:build :flows:adapters:out:goattracker:build :flows:adapters:out:exomizer:build :infra:gradle:build` (or full `./gradlew build`).
   - Testing: build is green; `./gradlew detekt` shows no new violations.

5. **Step 1.5**: Update documentation
   - Files:
     - `doc/arc42/05_building_block_view.md` (line 56)
     - `doc/arc42/building-blocks/flows.md` (lines 19, 21-30 port table, 32)
     - `doc/arc42/11_risks_and_technical_debt.md` (D4 row, line 12)
   - Description: update prose and the port-location table in `flows.md` to reference `usecase/port`; remove the now-stale inconsistency note (line 32); mark D4 resolved in the risk register, referencing this plan/issue, following the D2 "✅ RESOLVED" pattern established in PLAN-0004.
   - Testing: manual review — no remaining doc references to `flows/.../domain/port/`.

**Phase 1 Deliverable**: All 8 flows ports relocated to `usecase/port`; every one of the ~21 consuming files updated; `flows`, its 5 out-adapters, its in-adapter, and `infra/gradle` all compile and pass tests; arc42 docs reflect the resolved state; single mergeable change.

## 6. Testing Strategy

### Unit Tests
- No new tests needed — existing step tests (`AssembleStepTest`, `CharpadStepTest`, `ExomizerStepTest`, and all other step tests) must continue to pass unchanged; only import paths change.

### Integration Tests
- None specific to this change; rely on existing `flows`, `flows/adapters/out/*`, `flows/adapters/in/gradle`, and `infra/gradle` test suites plus the full `./gradlew build`.

### Manual Testing
- Repo-wide grep for the literal `flows.domain.port` (`git grep -n "flows\.domain\.port"`) returns no results after the change, in both `.kt` sources and `doc/` markdown.
- Optionally run the `e2e-test` skill against the tony project to confirm flows still execute correctly end-to-end, though as a compile-time-only rename this is not expected to surface runtime issues.

## 7. Risks and Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Missed reference to the old `domain.port` package (e.g. the `ImageTask.kt` FQN cast, or an unlisted usage) | Low | Low | Final repo-wide grep for `flows.domain.port` before considering the change complete; the Explore agent already enumerated all 21 consuming files plus the one FQN-only reference |
| Confusing `FlowExomizerAdapter.kt`'s unrelated `crunchers.exomizer.usecase.port.ExecuteExomizerPort` import with the flows `ExomizerPort` being moved | Low | Low | Match replacements on the full `com.github.c64lib.rbt.flows.domain.port` prefix, not a bare `.port` segment; verify `FlowExomizerAdapter.kt` still imports both ports correctly (one moved, one untouched) after the edit |
| Directory move leaves stray empty directories under the old `domain/port/` tree | Low | Low | Explicitly verify and remove the empty `domain/port/` directory after the move; confirm via `git status` that no orphaned empty dirs remain |

## 8. Documentation Updates

- [ ] Mark item D4 in `doc/arc42/11_risks_and_technical_debt.md` as resolved, referencing this plan/issue (Step 1.5).
- [ ] Update `doc/arc42/building-blocks/flows.md` port table and prose to reference `usecase/port`; remove the stale inconsistency note (Step 1.5).
- [ ] Update `doc/arc42/05_building_block_view.md:56` prose (Step 1.5).
- [x] No CLAUDE.md changes needed — no new patterns introduced; this move brings flows into line with the pattern CLAUDE.md already documents.
- [x] No inline documentation changes needed beyond the package declarations themselves.

## 9. Rollout Plan

1. Execute as a single commit/PR touching the ~30 files identified in Phase 1 (8 moved, ~21 import-only fixes, 3 doc locations).
2. Monitor: standard CI build (`.github/workflows/build.yml`) must pass — compile + test across `flows`, its 5 out-adapters, its in-adapter, and `infra/gradle`.
3. Rollback: trivial — revert the single commit; no data migration or external state involved.

## 10. Revision History

| Date | Updated By | Changes |
|------|------------|---------|
| 2026-07-16 | AI Agent | Plan created via Explore-agent analysis; design decision (move vs. document exception) confirmed with user as "move"; no unresolved questions remain. |
| 2026-07-16 | AI Agent | Execution completed: all 5 steps (Phase 1) finished successfully. All 8 port files moved to usecase/port, all 21 consuming files updated, build passed, docs updated. Status → implemented. |

---

**Note**: This plan should be reviewed and approved before implementation begins.
