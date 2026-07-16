# Feature: Rename `processors/spritepad` package root from `com.c64lib` to `com.github.c64lib`

**Plan ID**: PLAN-0004
**Issue**: #157
**Status**: implemented
**Created**: 2026-07-16
**Last Updated**: 2026-07-16

## 1. Feature Description

### Original Issue Description

> ## Problem
>
> `processors/spritepad` sources use base package `com.c64lib.rbt.processors.spritepad...` (e.g. `ProcessSpritepadUseCase.kt`, `SPD4Processor.kt`), while every other domain in the project uses `com.github.c64lib.rbt...`.
>
> ## Impact
>
> Breaks the otherwise uniform package convention; surprises tooling and readers, and is inconsistent with the rest of the codebase.
>
> ## Suggested fix
>
> Rename the `spritepad` package to `com.github.c64lib.rbt.processors.spritepad` to match every other domain.
>
> ## Source
>
> Identified and verified against code while writing the arc42 technical documentation (§11 Risks and Technical Debt, item D2). See [`doc/arc42/11_risks_and_technical_debt.md`](../doc/arc42/11_risks_and_technical_debt.md).

### Overview

The `processors/spritepad` module's main and test source trees (excluding its `adapters/in/gradle` submodule, which is already correctly namespaced) use the non-conventional package root `com.c64lib.rbt.processors.spritepad`, instead of the project-wide `com.github.c64lib.rbt...` convention every other domain follows. This is a pure rename: move source files (and their containing directories, since Kotlin convention nests packages as directories) to the correctly-rooted package tree, fix internal cross-references within `spritepad`, and fix the 4 import lines in the 3 external consumer files that reference the old root.

### Requirements
- Move all main/test source files under `processors/spritepad/src/{main,test}/kotlin/com/c64lib/rbt/processors/spritepad/...` to the equivalent path under `com/github/c64lib/rbt/processors/spritepad/...`.
- Update every `package com.c64lib.rbt.processors.spritepad...` declaration to `package com.github.c64lib.rbt.processors.spritepad...`.
- Update every `import com.c64lib.rbt.processors.spritepad...` statement — both internal (within `spritepad`) and external (in `adapters/in/gradle`, `flows/adapters/out/spritepad`) — to the `com.github.c64lib...` root.
- Update the two documentation references (`doc/arc42/11_risks_and_technical_debt.md` item D2, `doc/arc42/building-blocks/processors.md` note) to reflect the resolved state.
- No behavioral change: class names, public API, and semantics stay identical.

### Success Criteria
- No occurrences of the literal `com.c64lib` package root remain anywhere in `.kt` source (main or test) in the repository.
- `./gradlew build` (compile + test) passes for `processors:spritepad`, `processors:spritepad:adapters:in:gradle`, `flows:adapters:out:spritepad`, and `infra:gradle`.
- `./gradlew detekt` shows no new violations introduced by the rename.

## 2. Root Cause Analysis

The `spritepad` processor's domain/usecase source was originally authored (or ported) using `com.c64lib` as the root, inconsistent with the rest of the project's `com.github.c64lib` convention. It compiles fine as valid Kotlin, so nothing forced a fix. The module's own `adapters/in/gradle` submodule was set up correctly later, creating the same kind of internal split seen in the `fllter`→`filter` rename (PLAN-0003): one part of a module follows convention, the other doesn't.

### Current State
- Main source root: `com.c64lib.rbt.processors.spritepad` in `processors/spritepad/src/main/kotlin/com/c64lib/rbt/processors/spritepad/`:
  - `domain/InvalidSPDFormatException.kt` — `package com.c64lib.rbt.processors.spritepad.domain`
  - `domain/SpriteProducer.kt` — `package com.c64lib.rbt.processors.spritepad.domain`
  - `usecase/ProcessSpritepadUseCase.kt` — `package com.c64lib.rbt.processors.spritepad.usecase`, imports `InvalidSPDFormatException`, `SpriteProducer`, and `SPD4Processor` from the `com.c64lib` root
  - `usecase/spd4/SPD4Processor.kt` — `package com.c64lib.rbt.processors.spritepad.usecase.spd4`, imports `ProcessSpritepadUseCase` and `SPDProcessor` from the `com.c64lib` root
- Test source root: `processors/spritepad/src/test/kotlin/com/c64lib/rbt/processors/spritepad/usecase/ProcessSpritepadUseCaseTest.kt` — 4 imports from the `com.c64lib` root
- Already-correct sibling submodule: `processors/spritepad/adapters/in/gradle/.../Spritepad.kt` uses `package com.github.c64lib.rbt.processors.spritepad.adapters.in.gradle` but has 2 imports still pointing at the old `com.c64lib` root (`SpriteProducer`, `ProcessSpritepadUseCase`)
- External consumers in `flows/adapters/out/spritepad`:
  - `SpritepadAdapter.kt` — imports `ProcessSpritepadUseCase` from `com.c64lib`
  - `SpritepadOutputProducerFactory.kt` — imports `SpriteProducer` from `com.c64lib`
- `infra/gradle`'s `RetroAssemblerPlugin.kt` only references the already-correctly-namespaced `Spritepad` task class by simple name (imported from `com.github.c64lib...`) — it never imports the `com.c64lib` domain/usecase FQNs directly, so it needs no source edits, though it stays in the verification/build scope as a downstream consumer of the module graph.
- Documentation: `doc/arc42/11_risks_and_technical_debt.md` (item D2, line 10) and `doc/arc42/building-blocks/processors.md` (line 48) both document this exact inconsistency and will need to reflect resolution.
- Build scripts: `processors/spritepad/build.gradle.kts` and `processors/spritepad/adapters/in/gradle/build.gradle.kts` contain no literal `com.c64lib` references — their `group` coordinates already use `com.github.c64lib.retro-assembler...`; only project-path dependency declarations (`project(":processors:spritepad")`) which are unaffected by a package rename.

### Desired State
All `spritepad` source (main, test, and its `adapters/in/gradle` submodule) rooted consistently under `com.github.c64lib.rbt...`; no importer anywhere references `com.c64lib`; arc42 docs reflect the resolved state.

### Gap Analysis
- Move 4 main-source files + 1 test file to new directory trees rooted at `com/github/c64lib/...` (8 directory levels of nesting change: `domain/`, `usecase/`, `usecase/spd4/`).
- Fix `package` declarations in those 5 files.
- Fix internal imports in `ProcessSpritepadUseCase.kt` (3 imports) and `SPD4Processor.kt` (2 imports).
- Fix test imports in `ProcessSpritepadUseCaseTest.kt` (4 imports).
- Fix external imports in `Spritepad.kt` (2), `SpritepadAdapter.kt` (1), `SpritepadOutputProducerFactory.kt` (1) — 4 import lines across 3 files, none require a file move (all three already live in correctly-rooted directories).
- Update 2 documentation files to mark D2 resolved.
- No `build.gradle.kts` or `infra/gradle` source edits required — confirmed no literal package-path references beyond the KDoc/import cases already listed.

## 3. Relevant Code Parts

### Existing Components
- **`InvalidSPDFormatException.kt`** — `processors/spritepad/src/main/kotlin/com/c64lib/rbt/processors/spritepad/domain/InvalidSPDFormatException.kt`
  - Purpose: domain exception for malformed SPD input.
  - Change: move to `.../com/github/c64lib/rbt/processors/spritepad/domain/InvalidSPDFormatException.kt`, update `package` declaration (line 25). No imports to fix (file has none).
- **`SpriteProducer.kt`** — `processors/spritepad/src/main/kotlin/com/c64lib/rbt/processors/spritepad/domain/SpriteProducer.kt`
  - Purpose: binary producer for sprite data ranges.
  - Change: move to the `com/github/c64lib` tree, update `package` declaration (line 25). Existing `shared.*` imports (lines 27–29) are already `com.github.c64lib` rooted — untouched.
- **`ProcessSpritepadUseCase.kt`** — `processors/spritepad/src/main/kotlin/com/c64lib/rbt/processors/spritepad/usecase/ProcessSpritepadUseCase.kt`
  - Purpose: entry-point use case dispatching to the correct SPD version processor.
  - Change: move to the `com/github/c64lib` tree, update `package` declaration (line 25) and 3 internal imports (lines 27–29: `InvalidSPDFormatException`, `SpriteProducer`, `SPD4Processor`).
- **`SPD4Processor.kt`** — `processors/spritepad/src/main/kotlin/com/c64lib/rbt/processors/spritepad/usecase/spd4/SPD4Processor.kt`
  - Purpose: SPD v4/v5 format processor.
  - Change: move to the `com/github/c64lib` tree, update `package` declaration (line 25) and 2 internal imports (lines 27–28: `ProcessSpritepadUseCase`, `SPDProcessor`).
- **`ProcessSpritepadUseCaseTest.kt`** — `processors/spritepad/src/test/kotlin/com/c64lib/rbt/processors/spritepad/usecase/ProcessSpritepadUseCaseTest.kt`
  - Change: move to the `com/github/c64lib` test tree, update 4 imports (lines 25–28).
- **`Spritepad.kt`** — `processors/spritepad/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/processors/spritepad/adapters/in/gradle/Spritepad.kt`
  - Change: fix 2 imports only (`SpriteProducer`, `ProcessSpritepadUseCase`); file does not move — already correctly rooted.
- **`SpritepadAdapter.kt`** — `flows/adapters/out/spritepad/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/out/spritepad/SpritepadAdapter.kt`
  - Change: fix 1 import (`ProcessSpritepadUseCase`); no move.
- **`SpritepadOutputProducerFactory.kt`** — `flows/adapters/out/spritepad/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/out/spritepad/SpritepadOutputProducerFactory.kt`
  - Change: fix 1 import (`SpriteProducer`); no move.

### Architecture Alignment
- **Domain**: `processors/spritepad` (processor domain) — no domain logic change, pure package rename.
- **Use Cases**: `ProcessSpritepadUseCase` unchanged in behavior; only its package/import paths move.
- **Ports**: none affected — `SPDProcessor` is an internal interface, unaffected beyond its package path.
- **Adapters**: `processors/spritepad/adapters/in/gradle` (inbound Gradle DSL) and `flows/adapters/out/spritepad` (outbound flows adapter) both consume the renamed classes via import only.

### Dependencies
None — self-contained rename within existing module boundaries. `infra/gradle`'s existing `compileOnly` project-path dependencies on `:processors:spritepad` and `:processors:spritepad:adapters:in:gradle` (build.gradle.kts lines 117–118) are unaffected since they reference Gradle project paths, not Kotlin package names.

## 4. Questions and Clarifications

### Self-Reflection Questions
- **Q**: Does `infra/gradle` need any source-level edits?
  - **A**: No. `RetroAssemblerPlugin.kt` only imports the `Spritepad` task class (already `com.github.c64lib` rooted) by simple name; it never references the `com.c64lib` domain/usecase FQNs directly. `infra/gradle`'s `compileOnly` project-path dependencies are unaffected by a package rename since they're Gradle project paths, not Kotlin package identifiers. It remains in the verification scope only as a downstream consumer of the overall build graph.
- **Q**: Do any `build.gradle.kts` files reference the `com.c64lib` package literally (e.g. in sourceSets, manifest, or resource filtering)?
  - **A**: No. Both `processors/spritepad/build.gradle.kts` and `processors/spritepad/adapters/in/gradle/build.gradle.kts` rely purely on convention plugins and the physical `src/main/kotlin` directory layout; their `group` coordinates already use `com.github.c64lib.retro-assembler...`. No literal package-path strings need updating.
- **Q**: Are there hidden `import com.c64lib` aliases (`as X`) anywhere that a plain literal-replace might miss?
  - **A**: No — the Explore agent's repo-wide grep for the bare prefix `com.c64lib.rbt` found exactly the same file set enumerated above, with no `as`-aliased imports.
- **Q**: Does the directory structure need to move, or is this just a package-declaration edit?
  - **A**: Directories must move too. Kotlin/Gradle convention nests source files in directories matching their package (`com/c64lib/rbt/processors/spritepad/...`), and the Explore agent confirmed the physical directory tree mirrors the incorrect package exactly — same situation as PLAN-0003's `fllter`→`filter` rename, but this time spanning main *and* test trees with deeper nesting (`domain/`, `usecase/`, `usecase/spd4/`).

### Unresolved Questions
None — this is a small, fully-scoped mechanical rename with no open design questions, mirroring the already-executed PLAN-0003 pattern.

### Design Decisions
- **Decision**: Single-phase vs. multi-phase execution.
  - **Options**: (A) Single phase covering all file moves + import fixes + doc updates, mirroring PLAN-0003's approach. (B) Split into "core module (spritepad + tests)" then "external consumers (adapters/in/gradle, flows adapter)" then "docs".
  - **Recommendation**: Option A — single phase. The full change touches 10 files (5 moved, 3 import-only fixes, 2 doc updates) with no intermediate state worth preserving as a separate merge; splitting only adds ceremony for a rename this contained.
- **Decision**: Whether to keep or remove the arc42 D2 risk-register entry once resolved.
  - **Options**: (A) Remove the D2 row from `doc/arc42/11_risks_and_technical_debt.md` entirely now that it's fixed. (B) Keep the row but mark it "Resolved" with a reference to this plan/issue, preserving historical context.
  - **Recommendation**: Option B — mark resolved rather than delete, consistent with treating the risk register as a living history rather than a pure backlog; also update `doc/arc42/building-blocks/processors.md` to drop the now-stale inconsistency note.

## 5. Implementation Plan

### Phase 1: Rename spritepad package root and fix all references (single deliverable)
**Goal**: Eliminate every occurrence of the `com.c64lib` package root from `processors/spritepad` and its consumers while preserving behavior.

1. **Step 1.1**: Move main-source files to the correctly-rooted package tree
   - Files:
     - `processors/spritepad/src/main/kotlin/com/c64lib/rbt/processors/spritepad/domain/InvalidSPDFormatException.kt` → `processors/spritepad/src/main/kotlin/com/github/c64lib/rbt/processors/spritepad/domain/InvalidSPDFormatException.kt`
     - `processors/spritepad/src/main/kotlin/com/c64lib/rbt/processors/spritepad/domain/SpriteProducer.kt` → `processors/spritepad/src/main/kotlin/com/github/c64lib/rbt/processors/spritepad/domain/SpriteProducer.kt`
     - `processors/spritepad/src/main/kotlin/com/c64lib/rbt/processors/spritepad/usecase/ProcessSpritepadUseCase.kt` → `processors/spritepad/src/main/kotlin/com/github/c64lib/rbt/processors/spritepad/usecase/ProcessSpritepadUseCase.kt`
     - `processors/spritepad/src/main/kotlin/com/c64lib/rbt/processors/spritepad/usecase/spd4/SPD4Processor.kt` → `processors/spritepad/src/main/kotlin/com/github/c64lib/rbt/processors/spritepad/usecase/spd4/SPD4Processor.kt`
   - Description: create the `com/github/c64lib/...` directory tree, move (or recreate) each file into it, update each file's `package` line from `com.c64lib.rbt.processors.spritepad...` to `com.github.c64lib.rbt.processors.spritepad...`, and fix internal imports: `ProcessSpritepadUseCase.kt`'s imports of `InvalidSPDFormatException`, `SpriteProducer`, `SPD4Processor`; `SPD4Processor.kt`'s imports of `ProcessSpritepadUseCase`, `SPDProcessor`. Remove the now-empty `com/c64lib/` directory tree once confirmed empty.
   - Testing: files compile as part of step 1.3.

2. **Step 1.2**: Move test source and fix external consumer imports
   - Files:
     - `processors/spritepad/src/test/kotlin/com/c64lib/rbt/processors/spritepad/usecase/ProcessSpritepadUseCaseTest.kt` → `processors/spritepad/src/test/kotlin/com/github/c64lib/rbt/processors/spritepad/usecase/ProcessSpritepadUseCaseTest.kt`
     - `processors/spritepad/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/processors/spritepad/adapters/in/gradle/Spritepad.kt` (import-only, no move)
     - `flows/adapters/out/spritepad/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/out/spritepad/SpritepadAdapter.kt` (import-only, no move)
     - `flows/adapters/out/spritepad/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/out/spritepad/SpritepadOutputProducerFactory.kt` (import-only, no move)
   - Description: move the test file into the `com/github/c64lib` test tree and fix its 4 imports; fix the 2 imports in `Spritepad.kt` and 1 import each in `SpritepadAdapter.kt` and `SpritepadOutputProducerFactory.kt` — replacing `com.c64lib.rbt.processors.spritepad...` with `com.github.c64lib.rbt.processors.spritepad...` in every case. Remove the now-empty `com/c64lib/` test directory tree.
   - Testing: grep for the literal `com.c64lib` (excluding `com.github.c64lib`) repo-wide returns zero matches in `.kt` files after this step.

3. **Step 1.3**: Verify build and tests
   - Files: none (verification only)
   - Description: run `./gradlew :processors:spritepad:build :processors:spritepad:adapters:in:gradle:build :flows:adapters:out:spritepad:build :infra:gradle:build` (or full `./gradlew build`) to confirm compilation and tests pass.
   - Testing: build is green; `./gradlew detekt` shows no new violations.

4. **Step 1.4**: Update documentation
   - Files:
     - `doc/arc42/11_risks_and_technical_debt.md` (item D2, line 10)
     - `doc/arc42/building-blocks/processors.md` (line 48)
   - Description: mark the D2 risk-register entry as resolved (referencing this plan/issue) rather than deleting it; remove the now-stale inconsistency note from `processors.md`.
   - Testing: manual review — no literal `com.c64lib` references remain in `doc/`.

**Phase 1 Deliverable**: `com.c64lib` package root fully eliminated from `processors/spritepad` and all its consumers; `processors:spritepad`, its `adapters/in/gradle` submodule, `flows:adapters:out:spritepad`, and `infra:gradle` all compile and pass tests; documentation reflects the resolved state; single mergeable change.

## 6. Testing Strategy

### Unit Tests
- No new tests needed — `ProcessSpritepadUseCaseTest.kt` already covers `ProcessSpritepadUseCase`; it must continue to pass unchanged (only its package/import paths change).

### Integration Tests
- None specific to this change; rely on existing `processors:spritepad`, `flows:adapters:out:spritepad`, and `infra:gradle` test suites plus the full `./gradlew build`.

### Manual Testing
- Repo-wide grep for the literal `com.c64lib` (`git grep -n "com\.c64lib\."` filtered to exclude `com.github.c64lib`) returns no results after the change.
- Optionally run the `e2e-test` skill against the tony project if there's any concern about runtime classpath issues from the Spritepad Gradle DSL task, though as a compile-time-only rename this is not expected to be necessary.

## 7. Risks and Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Missed importer reference to `com.c64lib` elsewhere (e.g. generated docs, KDoc links, other domains) | Low | Low | Final repo-wide grep for the literal `com.c64lib` before considering the change complete; the Explore agent already confirmed the full file set exhaustively |
| Binary/API compatibility concern for external consumers of `processors:spritepad` as a library | Low | Low | This plugin does not publish `processors:spritepad`'s internal packages as a library API; only the Gradle plugin's public DSL surface matters, and internal domain/usecase package names are not part of that public contract |
| Directory move introduces stray leftover empty directories under the old `com/c64lib/` tree | Low | Low | Explicitly verify and remove empty `com/c64lib/` directories after each move step; confirm via `git status` that no orphaned empty dirs remain (Git does not track empty directories, so this is mostly a local hygiene check) |

## 8. Documentation Updates

- [ ] Mark item D2 in `doc/arc42/11_risks_and_technical_debt.md` as resolved, referencing this plan/issue (Step 1.4).
- [ ] Remove the now-stale inconsistency note in `doc/arc42/building-blocks/processors.md` (Step 1.4).
- [x] No CLAUDE.md changes needed — no new patterns introduced.
- [x] No inline documentation changes needed beyond the package declarations themselves.

## 9. Rollout Plan

1. Execute as a single commit/PR touching the 10 files identified in Phase 1 (5 moved: 4 main + 1 test, 3 import-only fixes, 2 doc updates).
2. Monitor: standard CI build (`.github/workflows/build.yml`) must pass — compile + test across all modules, particularly `processors:spritepad`, its `adapters/in/gradle` submodule, `flows:adapters:out:spritepad`, and `infra:gradle`.
3. Rollback: trivial — revert the single commit; no data migration or external state involved.

## 10. Revision History

| Date | Updated By | Changes |
|------|------------|---------|
| 2026-07-16 | AI Agent | Plan created and accepted; no unresolved questions, mechanical rename scope confirmed via Explore agent analysis. |
| 2026-07-16 | AI Agent | Execution completed: all 4 steps finished successfully. Spritepad package renamed, imports fixed, builds green, docs updated. Status → implemented. |

---

**Note**: This plan should be reviewed and approved before implementation begins.
