# Feature: Rename `fllter` package to `filter`

**Plan ID**: PLAN-0003
**Issue**: #158
**Status**: implemented
**Created**: 2026-07-16
**Last Updated**: 2026-07-16

## 1. Feature Description

### Original Issue Description

> ## Problem
>
> The shared package `shared/gradle/.../shared/gradle/fllter/` (containing `BinaryInterleaver.kt`, `Nybbler.kt`) has a typo — should be `filter`.
>
> ## Impact
>
> Cosmetic but permanent-looking; every import of these classes repeats the typo.
>
> ## Suggested fix
>
> Rename `fllter` → `filter`. This touches importers in `shared:gradle` and `infra`.
>
> ## Source
>
> Identified and verified against code while writing the arc42 technical documentation (§11 Risks and Technical Debt, item D3). See [`doc/arc42/11_risks_and_technical_debt.md`](../doc/arc42/11_risks_and_technical_debt.md).

### Overview

The `shared/gradle` module's main source tree has a package typo: `com.github.c64lib.rbt.shared.gradle.fllter` (should be `filter`). It holds two classes, `BinaryInterleaver` and `Nybbler`. Notably, the corresponding **test** package for these same classes is already correctly named `filter` (`shared/gradle/src/test/kotlin/.../shared/gradle/filter/`) — only the `import` statements inside those test files still reference the misspelled `fllter` package. This is a pure rename: move the two main-source files to a correctly-named package, update their `package` declarations, and fix every importer.

### Requirements
- Rename directory `shared/gradle/src/main/kotlin/com/github/c64lib/rbt/shared/gradle/fllter/` to `.../filter/`.
- Update the `package` declaration in `BinaryInterleaver.kt` and `Nybbler.kt` from `...shared.gradle.fllter` to `...shared.gradle.filter`.
- Update every `import ...shared.gradle.fllter....` statement to `...shared.gradle.filter....` across the codebase.
- No behavioral change: class names, public API, and semantics stay identical.

### Success Criteria
- No occurrences of `fllter` remain anywhere in `.kt` source (main or test) or build files.
- `./gradlew build` (compile + test) passes for `shared:gradle` and all downstream modules that import these classes.
- `./gradlew detekt` shows no new violations introduced by the rename.

## 2. Root Cause Analysis

Simple original typo when the package was created; never caught because "fllter" still compiles as a valid (if misspelled) identifier. The test package was set up correctly at some point, creating an inconsistency where main source and test source for the same classes live under differently-spelled packages.

### Current State
- Main source: package `com.github.c64lib.rbt.shared.gradle.fllter` in `shared/gradle/src/main/kotlin/.../shared/gradle/fllter/`
  - `BinaryInterleaver.kt`
  - `Nybbler.kt`
- Test source: package `com.github.c64lib.rbt.shared.gradle.filter` (already correct) in `shared/gradle/src/test/kotlin/.../shared/gradle/filter/`
  - `BinaryInterleaverTest.kt` — imports `...fllter.BinaryInterleaver`
  - `NybblerTest.kt` — imports `...fllter.Nybbler`
- Other importers of the misspelled package:
  - `shared/gradle/src/main/kotlin/.../shared/gradle/dsl/FilterAwareExtension.kt` — imports `...fllter.BinaryInterleaver` and `...fllter.Nybbler`
  - `flows/adapters/out/charpad/src/main/kotlin/.../flows/adapters/out/charpad/CharpadOutputProducerFactory.kt` — imports `...fllter.BinaryInterleaver as BinaryInterleaverImpl` and `...fllter.Nybbler as NybblerImpl`
  - `flows/adapters/out/charpad/src/test/kotlin/.../flows/adapters/out/charpad/CharpadOutputProducerFactoryFilterTest.kt` — imports `...fllter.Nybbler`

### Desired State
All source under a consistently-spelled `filter` package; test package and main package match; no importer references the typo.

### Gap Analysis
- Move 2 main-source files to new directory + fix their `package` line.
- Fix `import` lines in 4 files (2 test files already under correct package but wrong import; 1 other main-source file; 1 other test file).
- Note: issue text says "This touches importers in `shared:gradle` and `infra`" — codebase search found no `infra` module importer of `fllter`; the actual second consumer is `flows/adapters/out/charpad`, not `infra`. This plan follows the verified code, not the issue's guess.

## 3. Relevant Code Parts

### Existing Components
- **`BinaryInterleaver.kt`** — `shared/gradle/src/main/kotlin/com/github/c64lib/rbt/shared/gradle/fllter/BinaryInterleaver.kt`
  - Purpose: binary interleaving filter utility, part of shared kernel.
  - Change: move to `.../filter/BinaryInterleaver.kt`, update `package` declaration (line 25).
- **`Nybbler.kt`** — `shared/gradle/src/main/kotlin/com/github/c64lib/rbt/shared/gradle/fllter/Nybbler.kt`
  - Purpose: nybble-splitting filter utility, part of shared kernel.
  - Change: move to `.../filter/Nybbler.kt`, update `package` declaration (line 25).
- **`BinaryInterleaverTest.kt`** — `shared/gradle/src/test/kotlin/com/github/c64lib/rbt/shared/gradle/filter/BinaryInterleaverTest.kt`
  - Change: fix `import` (line 28) from `...fllter.BinaryInterleaver` to `...filter.BinaryInterleaver`. File itself does not move (already in correct package directory).
- **`NybblerTest.kt`** — `shared/gradle/src/test/kotlin/com/github/c64lib/rbt/shared/gradle/filter/NybblerTest.kt`
  - Change: fix `import` (line 27) from `...fllter.Nybbler` to `...filter.Nybbler`.
- **`FilterAwareExtension.kt`** — `shared/gradle/src/main/kotlin/com/github/c64lib/rbt/shared/gradle/dsl/FilterAwareExtension.kt`
  - Change: fix imports (lines 28–29).
- **`CharpadOutputProducerFactory.kt`** — `flows/adapters/out/charpad/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/out/charpad/CharpadOutputProducerFactory.kt`
  - Change: fix imports (lines 31–32), including the `as BinaryInterleaverImpl` / `as NybblerImpl` aliases (aliases themselves are unaffected, only the source package path).
- **`CharpadOutputProducerFactoryFilterTest.kt`** — `flows/adapters/out/charpad/src/test/kotlin/com/github/c64lib/rbt/flows/adapters/out/charpad/CharpadOutputProducerFactoryFilterTest.kt`
  - Change: fix import (line 28).

### Architecture Alignment
- **Domain**: `shared` (shared kernel) — no domain logic change, pure package rename.
- **Use Cases**: none affected.
- **Ports**: none affected.
- **Adapters**: `flows/adapters/out/charpad` consumes the shared classes via import only; adjust import path.

### Dependencies
None — self-contained rename within existing module boundaries. No new `compileOnly` dependency needed since `shared:gradle` module itself doesn't change.

## 4. Questions and Clarifications

### Self-Reflection Questions
- **Q**: Does the issue's claim that `infra` imports from `fllter` hold up?
  - **A**: No. A codebase-wide grep for `fllter` found no matches in `infra`. The real second consumer is `flows/adapters/out/charpad`. The plan targets the verified file set, not the issue's description.
- **Q**: Do the test files need to move directories too?
  - **A**: No — `BinaryInterleaverTest.kt` and `NybblerTest.kt` already live under the correctly-spelled `.../shared/gradle/filter/` test directory. Only their `import` statements need fixing.
- **Q**: Any risk of package/class name collision at the new `filter` location?
  - **A**: No — `shared/gradle/src/main/kotlin/.../shared/gradle/filter/` does not currently exist (only the test-side `filter` package exists), so the move is a clean directory rename with no merge needed.

### Unresolved Questions
None — this is a small, fully-scoped mechanical rename with no open design questions.

### Design Decisions
- **Decision**: Whether to do this as a single atomic commit or phase it.
  - **Options**: (A) Single-phase rename since it's small and mechanical, (B) split into "move + update package" then "fix importers".
  - **Recommendation**: Option A — single phase. The whole change is under 10 files, has no intermediate safe state worth preserving separately, and splitting adds ceremony without benefit for a rename this size.

## 5. Implementation Plan

### Phase 1: Rename package and fix all references (single deliverable)
**Goal**: Eliminate every occurrence of the `fllter` typo from source while preserving behavior.

1. **Step 1.1**: Move main-source files to the correctly-named package directory
   - Files:
     - `shared/gradle/src/main/kotlin/com/github/c64lib/rbt/shared/gradle/fllter/BinaryInterleaver.kt` → `shared/gradle/src/main/kotlin/com/github/c64lib/rbt/shared/gradle/filter/BinaryInterleaver.kt`
     - `shared/gradle/src/main/kotlin/com/github/c64lib/rbt/shared/gradle/fllter/Nybbler.kt` → `shared/gradle/src/main/kotlin/com/github/c64lib/rbt/shared/gradle/filter/Nybbler.kt`
   - Description: `git mv` both files into a new `filter/` directory; update each file's `package com.github.c64lib.rbt.shared.gradle.fllter` line to `package com.github.c64lib.rbt.shared.gradle.filter`. Remove the now-empty `fllter/` directory.
   - Testing: file compiles standalone (checked in step 1.3).

2. **Step 1.2**: Fix all importer statements
   - Files:
     - `shared/gradle/src/test/kotlin/com/github/c64lib/rbt/shared/gradle/filter/BinaryInterleaverTest.kt`
     - `shared/gradle/src/test/kotlin/com/github/c64lib/rbt/shared/gradle/filter/NybblerTest.kt`
     - `shared/gradle/src/main/kotlin/com/github/c64lib/rbt/shared/gradle/dsl/FilterAwareExtension.kt`
     - `flows/adapters/out/charpad/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/out/charpad/CharpadOutputProducerFactory.kt`
     - `flows/adapters/out/charpad/src/test/kotlin/com/github/c64lib/rbt/flows/adapters/out/charpad/CharpadOutputProducerFactoryFilterTest.kt`
   - Description: replace `com.github.c64lib.rbt.shared.gradle.fllter` with `com.github.c64lib.rbt.shared.gradle.filter` in every `import` line. Preserve existing `as BinaryInterleaverImpl` / `as NybblerImpl` aliases in `CharpadOutputProducerFactory.kt` unchanged.
   - Testing: grep for `fllter` repo-wide returns zero matches after this step.

3. **Step 1.3**: Verify build and tests
   - Files: none (verification only)
   - Description: run `./gradlew :shared:gradle:build :flows:adapters:out:charpad:build` (or full `./gradlew build`) to confirm compilation and tests pass across `shared:gradle` and the `flows` charpad adapter.
   - Testing: build is green; `./gradlew detekt` shows no new violations.

**Phase 1 Deliverable**: `fllter` typo fully eliminated from the codebase; `shared:gradle` and all downstream consumers compile and pass tests; single mergeable change.

## 6. Testing Strategy

### Unit Tests
- No new tests needed — existing `BinaryInterleaverTest.kt` and `NybblerTest.kt` already cover `BinaryInterleaver` and `Nybbler`; they must continue to pass unchanged (only their import path changes).
- `CharpadOutputProducerFactoryFilterTest.kt` must continue to pass unchanged.

### Integration Tests
- None specific to this change; rely on existing `flows` and `shared:gradle` test suites plus the full `./gradlew build`.

### Manual Testing
- Repo-wide grep for `fllter` (`git grep -n fllter`) returns no results after the change.
- Optionally run the `e2e-test` skill against the tony project if there's any concern about runtime classpath issues, though as a compile-time-only rename this is not expected to be necessary.

## 7. Risks and Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Missed importer reference to `fllter` elsewhere (e.g. generated docs, KDoc links) | Low | Low | Final repo-wide grep for `fllter` before considering the change complete |
| Binary/API compatibility concern for external consumers of `shared:gradle` as a library | Low | Low | This plugin is not published as a library API for the `shared` module's internal packages; only the Gradle plugin's public DSL surface matters, and package names for internal shared-kernel classes are not part of that public contract |

## 8. Documentation Updates

- [ ] Update `doc/arc42/11_risks_and_technical_debt.md` to remove or mark resolved item D3 (the `fllter` typo entry) once this plan is executed.
- [x] No CLAUDE.md changes needed — no new patterns introduced.
- [x] No inline documentation changes needed beyond the package declarations themselves.

## 9. Rollout Plan

1. Execute as a single commit/PR touching the 7 files listed in Phase 1 (2 moved + 5 import fixes).
2. Monitor: standard CI build (`.github/workflows/build.yml`) must pass — compile + test across all modules.
3. Rollback: trivial — revert the single commit; no data migration or external state involved.

## 10. Revision History

| Date | Updated By | Changes |
|------|------------|---------|
| 2026-07-16 | AI Agent | Plan created and accepted; no unresolved questions, mechanical rename scope confirmed. |
| 2026-07-16 | AI Agent | Execution completed: all 3 steps finished successfully. Package renamed, imports fixed, builds green. Status → implemented. |

---

**Note**: This plan should be reviewed and approved before implementation begins.
