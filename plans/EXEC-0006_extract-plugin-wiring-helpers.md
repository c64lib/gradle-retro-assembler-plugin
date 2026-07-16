# Execution Log: Extract per-domain wiring helpers from `RetroAssemblerPlugin.afterEvaluate`

**Exec ID**: EXEC-0006
**Plan**: [PLAN-0006](PLAN-0006_extract-plugin-wiring-helpers.md)
**Issue**: #160
**Started**: 2026-07-16
**Last Updated**: 2026-07-16
**State**: completed

## 1. Execution Sessions

### Session 1 — 2026-07-16

- **Scope**: all (Phase 1, steps 1.1–1.4)
- **Mode**: per-phase
- **Outcome**: completed

| Step | Result | Verification | Notes |
|------|--------|--------------|-------|
| 1.1 | completed | Confirmed no existing test source dir under `infra/gradle/src/test` | Gap noted; addressed by Step 1.4 |
| 1.2 | completed | `./gradlew :infra:gradle:compileKotlin` BUILD SUCCESSFUL | 6 private helpers added: wireDependencies, wirePreprocess, wireSources, wireSpecAndTest, wireBuild, wireFlows |
| 1.3 | completed | `./gradlew :infra:gradle:compileKotlin` BUILD SUCCESSFUL | apply() rewritten to orchestrate helpers; all 7 dependsOn edges preserved |
| 1.4 | completed | `./gradlew :infra:gradle:test --tests "com.github.c64lib.gradle.RetroAssemblerPluginTest"` green; full `./gradlew build` green (274 tasks) | New `RetroAssemblerPluginTest` (ProjectBuilder-based) asserting all 13 tasks exist and all 7 dependsOn edges hold |

## 2. Deviations from Plan

| # | Step | Deviation | Reason | Impact |
|---|------|-----------|--------|--------|
| 1 | 1.2/1.3 | First compile attempt failed with unresolved-reference errors across the whole file | Transient stale incremental Kotlin compiler state, surfaced after a git stash/pop cycle during investigation; a clean rerun (`--rerun`) succeeded | None — false alarm, no code change needed |
| 2 | 1.4 | `infra/gradle` had no `src/test` directory at all, and its `build.gradle.kts` only declared `compileOnly` deps on the domain modules — insufficient for a test that actually applies the plugin | Confirmed during Step 1.1 gap-check; a ProjectBuilder application test needs the real classes at test runtime, not just compile time, and needs `--add-opens java.base/java.lang=ALL-UNNAMED` (known repo quirk, see memory) | Added `tasks.withType<Test> { jvmArgs(...) }` and a full `testImplementation` block mirroring every `compileOnly` entry in `infra/gradle/build.gradle.kts`; created the `src/test/kotlin` tree |
| 3 | 1.4 | Test initially failed with `IllegalArgumentException: Cannot determine version from "latest"` | `RetroAssemblerPluginExtension.dialectVersion` defaults to `DIALECT_VERSION_LATEST = "latest"`, which `SemVer.fromString` cannot parse — pre-existing behavior, not caused by this refactor; real consuming projects always set a concrete `dialectVersion` in their DSL block | Test explicitly sets `dialectVersion = "5.25"` before evaluating, working around the pre-existing gap rather than fixing it (out of scope for this plan) |
| 4 | 1.4 | Full `./gradlew build` initially failed on `:infra:gradle:spotlessKotlinCheck` (ktfmt formatting violations in the two new/changed Kotlin files) | Spotless enforces ktfmt repo-wide; my hand-formatted helper functions and test didn't match | Ran `./gradlew :infra:gradle:spotlessApply` to auto-fix; rerunning `./gradlew build` was green afterward |

## 3. Follow-ups

- The pre-existing gap where `RetroAssemblerPluginExtension.dialectVersion` defaults to
  `"latest"` (unparseable by `SemVer.fromString`) is out of scope for this plan — noted here
  in case it's worth its own issue/plan later.
