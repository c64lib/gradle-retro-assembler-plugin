# Feature: Complete Groovy DSL Coverage for Flow Steps + Fix Stale Fat Jar Root Cause

**Plan ID**: PLAN-0011
**Issue**: #176
**Status**: accepted
**Created**: 2026-07-17
**Last Updated**: 2026-07-17

## 1. Feature Description

### Original Issue Description

> ## Summary
>
> Two small, low-priority follow-ups surfaced while implementing `testStep` (PLAN-0010 / #130), bundled here since neither is urgent enough alone for a dedicated issue.
>
> ### 1. Groovy DSL coverage is incomplete for other step types
>
> PLAN-0010 added Groovy `Closure` overloads (with `@DelegatesTo`) for `FlowDslBuilder.flow(...)` and `FlowBuilder.testStep(...)` — this was the minimum needed to make `testStep` callable from a Groovy `build.gradle`. The other step builders (`assembleStep`, `charpadStep`, `spritepadStep`, `goattrackerStep`, `dasmStep`, `imageStep`, `exomizerStep`, `commandStep`) still only have the Kotlin receiver-lambda form.
>
> ### 2. `infra/gradle`'s fat jar can be built from stale subproject classes
>
> `infra/gradle`'s published plugin jar is assembled by the `copySubProjectClasses` task, which copies **resolved** subproject artifacts into `infra/gradle/build`. During PLAN-0010's e2e verification, `publishToMavenLocal` alone published a jar that was **missing** freshly-added methods in `flows/adapters/in/gradle` — until `:infra:gradle:clean` was run first. Suggested fix (documentation, not code): note the clean-first workflow.

### Overview

Two work streams:

1. **Groovy DSL completeness**: add Groovy `Closure` + `@DelegatesTo` overloads for the eight remaining `FlowBuilder` step methods so any step type is callable from a Groovy `build.gradle`, following the pattern already established for `flow(...)` and `testStep(...)`.
2. **Fat jar root-cause fix**: this plan **upgrades the issue's suggested documentation-only fix to a build-script fix**. A root-cause investigation (2026-07-17 session) found the `copySubProjectClasses` mechanism structurally broken; replace it with a properly modeled fat-jar assembly inside the `jar` task, eliminating the class of staleness bugs instead of documenting a workaround.

### Requirements

- Every `FlowBuilder` step method (`assembleStep`, `charpadStep`, `spritepadStep`, `goattrackerStep`, `dasmStep`, `imageStep`, `exomizerStep`, `commandStep`) has a Groovy `Closure` overload with `@DelegatesTo`, delegate binding, and `DELEGATE_FIRST` resolve strategy.
- Delegate-binding boilerplate is factored into a shared private helper (it repeats across 10 methods otherwise).
- `infra/gradle`'s published fat jar is assembled with correct Gradle task dependencies, up-to-date checks, and removal propagation — a change (add, modify, **delete**, rename) in any bundled subproject lands in the next `jar`/`publishToMavenLocal` output **without** requiring `:infra:gradle:clean`.
- The `copySubProjectClasses` task and the unused `resolvableImplementation` configuration are removed.

### Success Criteria

- From a Groovy `build.gradle`, `flows { flow("x") { assembleStep("y") { ... } } }` (and each other step type) configures without `Could not find method` errors.
- Kotlin DSL consumers (e.g. tony) are unaffected — receiver-lambda overloads still win for Kotlin callers.
- Reproduction scenario from the investigation passes: add a class to `flows/adapters/in/gradle`, run `:infra:gradle:jar` (no clean) → class present in fat jar; delete the class, rebuild (no clean) → class **absent** from fat jar.
- `./gradlew publishToMavenLocal` alone (no clean) always publishes a jar reflecting current subproject sources.
- Published jar contents are equivalent to today's jar for the unchanged case (same set of bundled packages; no duplicate classes; plugin descriptor and `META-INF/gradle-plugins` intact).

## 2. Root Cause Analysis

### Work stream 1 — Groovy DSL gap

Kotlin receiver-lambda parameters (`FlowBuilder.() -> Unit`) do not bind a Groovy closure's `delegate`, so Groovy consumers calling e.g. `assembleStep("main") { ... }` fail with `Could not find method assembleStep()`. This was proven and fixed for `flow`/`testStep` in PR #174; the other eight step methods still lack the overload.

### Work stream 2 — Stale fat jar (investigated 2026-07-17, reproduced)

`copySubProjectClasses` (`infra/gradle/build.gradle.kts:30-57`) has three structural defects:

1. **No declared inputs/outputs.** Gradle has no incrementality contract: the copy is *additive* into `infra/gradle/build/classes/...`, and nothing ever removes files. **Reproduced**: a class deleted from `flows/adapters/in/gradle` disappears from that module's own jar but remains in the fat jar on every subsequent build until `:infra:gradle:clean`. Renames likewise leave the old class behind (stale duplicate on the consumer's classpath).
2. **No task dependencies.** The task resolves `compileClasspath` at execution time inside `doLast`, so Gradle wires no dependency edges to the subprojects' `jar`/`classes` tasks. Correct ordering today is *incidental* — it holds only because `:infra:gradle:compileKotlin` happens to depend on the same classpath and `copySubProjectClasses` is `mustRunAfter(classes)`. Any change to that wiring (or a future configuration-cache migration) breaks it silently.
3. **Overlapping outputs.** It copies foreign classes into `build/classes/kotlin/main` — `compileKotlin`'s *own* output directory. Gradle overlapping-output handling and Kotlin incremental-compilation cleanup both manage that directory, so whether copied classes survive a given build depends on IC internals and execution order. This nondeterminism is the most plausible mechanism for the PLAN-0010 incident (fresh methods missing from the published jar), which was not deterministically reproducible in the investigation session — sequential `:infra:gradle:jar` and `publishToMavenLocal` runs *did* pick up additions.

### Current State

- Fat jar assembled by copying subproject `build/classes` trees into `infra/gradle/build` before `jar` packages the source-set output.
- Workaround documented in team memory: run `:infra:gradle:clean` before `publishToMavenLocal` after subproject changes.
- `resolvableImplementation` configuration (`infra/gradle/build.gradle.kts:21-27`) is created but referenced nowhere.
- Odd self-reference `project(":infra:gradle").tasks.jar { ... }` (line 55) instead of plain `tasks.jar`.

### Desired State

The `jar` task itself bundles the classes of all project dependencies by unpacking their jars (`zipTree`), declared as task inputs. Gradle then provides: implicit dependencies on every subproject `jar` task, content-based up-to-date checks, and full rebuild-from-inputs semantics (deletions/renames propagate). No copy task, no writes into `compileKotlin`'s output dir, no clean-first workflow.

### Gap Analysis

- Add 8 `Closure` overloads + shared helper + tests in `FlowDsl.kt`.
- Replace `copySubProjectClasses` with `tasks.jar { from(<project-dep jars as zipTrees>) }` using an `artifactView` filtered to `ProjectComponentIdentifier`; delete dead configuration; simplify the self-reference; update docs/memory that prescribe the clean-first workaround.

## 3. Relevant Code Parts

### Existing Components

- **FlowDsl.kt**: DSL entry point; contains the pattern to replicate.
  - Location: `flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/FlowDsl.kt`
  - `flow(String, Closure<*>)` at line 55 and `testStep(String, Closure<*>)` at line 223 are the templates; `registerStep` (line 232) is the existing shared registration helper.
  - Note: the older Kotlin step methods (lines 101–210, 243–256) each inline the artifact-registration block instead of calling `registerStep` — the overload work is a natural moment to converge them all on `registerStep`.
  - `commandStep` differs in signature: `(stepName, command, configure)`.
- **Step builders**: `flows/adapters/in/gradle/.../dsl/*.kt` (`AssembleStepBuilder`, `CharpadStepBuilder`, `SpritepadStepBuilder`, `GoattrackerStepBuilder`, `DasmStepBuilder`, `ImageStepBuilder`, `ExomizerStepBuilder`, `CommandStepBuilder`) — the `@DelegatesTo` targets.
- **infra/gradle build script**: `infra/gradle/build.gradle.kts`
  - `copySubProjectClasses` task: lines 30–57 (to be removed).
  - `resolvableImplementation` configuration: lines 21–27 (unused, to be removed).
  - `compileOnly(project(...))` dependency list: lines 83–132 — the set of bundled modules; the new mechanism derives the same set from `compileClasspath` project dependencies, so the list stays the single source of truth.

### Architecture Alignment

- **Domain**: flows (adapters/in/gradle) for stream 1; infra (build infrastructure only, no production code) for stream 2.
- **Use Cases**: none created or modified.
- **Ports**: none — stream 1 is pure inbound-adapter DSL surface; stream 2 is build wiring.
- **Adapters**: `flows/adapters/in/gradle` inbound adapter extended with Groovy-callable overloads.

### Dependencies

- `groovy.lang.Closure` / `groovy.lang.DelegatesTo` — already imported and used in `FlowDsl.kt`; no new module dependencies.
- No new modules → the `infra/gradle` `compileOnly` rule from CLAUDE.md is not triggered.

## 4. Questions and Clarifications

### Self-Reflection Questions

- **Q**: Should the fat-jar fix be documentation (as the issue suggests) or a code fix?
  - **A**: Code fix. The investigation demonstrated a reproducible correctness bug (deletions never propagate) plus structurally unsound task modeling; documenting a `clean`-first workaround leaves the trap armed for every future contributor and for CI edge cases.
- **Q**: Can the bundled-module set be derived without hardcoding a list in the jar task?
  - **A**: Yes — `configurations.compileClasspath.get().incoming.artifactView { componentFilter { it is ProjectComponentIdentifier } }` selects exactly the project dependencies (all `com.github.c64lib.retro-assembler*` modules, verified during investigation), keeping the `dependencies {}` block the single source of truth.
- **Q**: Does using jars-as-zipTrees instead of classes-dirs change jar contents?
  - **A**: Subproject jars are built from the same `classes` dirs, so contents are equivalent; `exclude("META-INF/**")` on the merged trees mirrors today's `exclude("**/META-INF/*")` and protects `infra/gradle`'s own plugin descriptor from being clobbered by subproject manifests.
- **Q**: Why not fix ordering/inputs on `copySubProjectClasses` instead of removing it?
  - **A**: Even with wired dependencies it would still write into `compileKotlin`'s output directory (overlapping outputs — defect 3) and still need delete-propagation logic (`Sync` semantics would delete `infra`'s own classes). Merging at the `jar` level removes the whole defect class.
- **Q**: Should the Groovy overloads be covered by unit tests, e2e against `../common`, or both?
  - **A**: Both (user decision, 2026-07-17): `FlowDslGroovyOverloadTest` in-module for all 10 overloads plus the Groovy consumer smoke test in Phase 3.
- **Q**: Are the `sources`/`javadoc` jars in scope? They currently do not bundle subproject sources.
  - **A**: Out of scope (user decision, 2026-07-17): keep as-is; only the classes fat jar is fixed.

### Unresolved Questions

*(none — all resolved)*

### Design Decisions

- **Decision**: How to assemble the fat jar.
  - **Options**: (A) merge project-dependency jars into `tasks.jar` via `artifactView` + `zipTree`; (B) keep a copy task but model it properly (`Sync` into a dedicated staging dir declared as an extra jar `from`); (C) adopt the Shadow plugin.
  - **Recommendation**: **A** — smallest diff, uses only Gradle-core APIs, gives implicit task dependencies and correct incrementality; B adds a moving part (staging dir) for no benefit; C brings a third-party plugin and relocation machinery this project doesn't need (external deps like vavr/pngj must stay *out* of the jar, as today).
- **Decision**: How to de-duplicate the Closure-binding boilerplate.
  - **Options**: (A) private generic helper `private fun <T> Closure<*>.configureFor(builder: T)`; (B) copy-paste the 3 lines into each of 8 overloads.
  - **Recommendation**: **A**, applied also to the two existing overloads so there is exactly one binding idiom.

## 5. Implementation Plan

### Phase 1: Groovy DSL overloads (flows domain)
**Goal**: Every step type callable from Groovy `build.gradle`.

1. **Step 1.1**: Extract a shared closure-binding helper and converge older step methods on `registerStep`.
   - Files: `flows/adapters/in/gradle/.../FlowDsl.kt`
   - Description: add `private fun <T> bindClosure(builder: T, closure: Closure<*>): T` (sets delegate, `DELEGATE_FIRST`, calls); refactor `flow(String, Closure)`/`testStep(String, Closure)` to use it; refactor the 8 older Kotlin step methods to call `registerStep` instead of inlined registration blocks (behavior-preserving).
   - Testing: `./gradlew :flows:adapters:in:gradle:test` — existing DSL tests stay green.
2. **Step 1.2**: Add `Closure` overloads for the 8 remaining step methods.
   - Files: `flows/adapters/in/gradle/.../FlowDsl.kt`
   - Description: for each of `assembleStep`, `charpadStep`, `spritepadStep`, `goattrackerStep`, `dasmStep`, `imageStep`, `exomizerStep` add `fun xxxStep(stepName: String, @DelegatesTo(XxxStepBuilder::class) configure: Closure<*>)`; for `commandStep` add `fun commandStep(stepName: String, command: String, @DelegatesTo(CommandStepBuilder::class) configure: Closure<*>)`. Kdoc one-liner mirroring the `testStep` overload.
   - Testing: new `FlowDslGroovyOverloadTest` (Kotlin, instantiating `object : Closure<Unit>(null) { fun doCall() { ... } }`) asserting each overload configures its builder and registers artifacts.
3. **Step 1.3**: Spotless + Detekt pass.
   - Files: as above
   - Description: `./gradlew :flows:adapters:in:gradle:spotlessApply detekt` — 8 near-identical overloads may trip duplication rules; keep the helper tight.
   - Testing: `./gradlew :flows:adapters:in:gradle:check`

**Phase 1 Deliverable**: Groovy consumers can use every step type; mergeable independently of Phase 2.

### Phase 2: Fat jar root-cause fix (infra)
**Goal**: Correct, incremental fat-jar assembly; no clean-first workflow.

1. **Step 2.1**: Replace `copySubProjectClasses` with jar-level merging.
   - Files: `infra/gradle/build.gradle.kts`
   - Description: delete the `tasks { copySubProjectClasses ... }` block (lines 30–53), the `dependsOn` wiring (line 55), the `mustRunAfter` (line 57), and the unused `resolvableImplementation` configuration (lines 21–27). Configure instead:
     ```kotlin
     val bundledProjectJars = configurations.compileClasspath.get().incoming
         .artifactView { componentFilter { it is ProjectComponentIdentifier } }
         .files
     tasks.jar {
       from(bundledProjectJars.elements.map { jars -> jars.map { zipTree(it) } }) {
         exclude("META-INF/**")
       }
       duplicatesStrategy = DuplicatesStrategy.FAIL
     }
     ```
     (`DuplicatesStrategy.FAIL` surfaces accidental class collisions between subprojects instead of silently picking one.)
   - Testing: `./gradlew :infra:gradle:clean :infra:gradle:jar` then `unzip -l`/`javap` — jar contains `com/github/c64lib/rbt/**` classes, plugin descriptor intact, no duplicates error.
2. **Step 2.2**: Verify incremental correctness (the reproduction scenarios).
   - Files: none (verification step)
   - Description: (a) add a probe method to `FlowBuilder`, run `:infra:gradle:jar` *without clean*, `javap` confirms presence; (b) add then delete a probe class, rebuild *without clean*, confirm absence; (c) `./gradlew publishToMavenLocal` without clean reflects a fresh change in `~/.m2/.../gradle-1.8.1-SNAPSHOT.jar`. Revert probes.
   - Testing: as described; also `./gradlew build` full pass.
3. **Step 2.3**: Retire the workaround documentation.
   - Files: `.claude/skills/e2e-test/SKILL.md` (if it prescribes clean-first), team memory note `infra-gradle-fat-jar-stale.md`, `doc/arc42/08_crosscutting_concepts.md` (if fat-jar assembly is described)
   - Description: remove/replace clean-first guidance with a note that `jar` now merges subproject jars with proper dependencies; update the arc42 concept if it documents the old mechanism.
   - Testing: doc review only.

**Phase 2 Deliverable**: Structurally sound fat-jar build; mergeable independently of Phase 1.

### Phase 3: End-to-end verification
**Goal**: Prove both streams against real consumers.

1. **Step 3.1**: e2e against tony (Kotlin DSL consumer).
   - Files: none
   - Description: via `e2e-test` skill — publish 1.8.1-SNAPSHOT (no manual clean!) and run tony's `flows`.
   - Testing: expected artifacts produced; publish-without-clean exercises the Phase 2 fix on the real path.
2. **Step 3.2**: e2e against `../common` (Groovy DSL consumer).
   - Files: scratch edits to `C:/Users/maciek/prj/cbm/common/build.gradle` (reverted afterwards, as in EXEC-0010 step 3.2)
   - Description: exercise at least `assembleStep` and one processor step (e.g. `charpadStep` or `commandStep`) from Groovy syntax; confirm no `Could not find method` errors.
   - Testing: configuration phase succeeds; steps execute or fail only for environmental reasons (missing assets), not DSL binding.

**Phase 3 Deliverable**: Verified release candidate; issue #176 closeable.

## 6. Testing Strategy

### Unit Tests

- `FlowDslGroovyOverloadTest` in `flows/adapters/in/gradle/src/test/kotlin/...`: for each of the 8 overloads, build a `Closure` from Kotlin, invoke via `FlowBuilder`, assert the step lands in the built `Flow` with expected inputs/outputs. (There are currently **no** Closure-overload tests, even for `flow`/`testStep` — cover those two as well.)
- Existing `TestStepBuilderTest`, `FlowTasksGeneratorTest` etc. remain green (regression guard for the `registerStep` convergence).

### Integration Tests

- `RetroAssemblerPluginTest` (`infra/gradle`, ProjectBuilder-based) remains green — guards that the build-script change doesn't affect plugin application. (Reminder: ProjectBuilder tests need `--add-opens java.base/java.lang=ALL-UNNAMED`, already configured.)
- Jar-content assertion after Step 2.1 (manual `javap`/`unzip` in EXEC log; optionally a small `jarContentCheck` verification task if it proves cheap).

### Manual Testing

- Incremental scenarios of Step 2.2 (add / delete / publish-without-clean).
- Groovy consumer smoke test of Step 3.2.

## 7. Risks and Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| `zipTree` merging changes jar layout subtly (services files, module-info, duplicate resources) | Medium | Medium | `DuplicatesStrategy.FAIL` + before/after `unzip -l` diff of jar contents in Step 2.1; `META-INF/**` excluded as today |
| `artifactView`/`componentFilter` resolves at configuration time and behaves differently under `--parallel` or future Gradle versions (project on Gradle 7.6, already emitting 8.0 deprecations) | Medium | Low | Lazy wiring via `files.elements.map`; verify with `--parallel` run in Step 2.2 |
| Stale classes already sitting in `infra/gradle/build/classes` from the old mechanism pollute the first post-fix build | Low | High | One-time `:infra:gradle:clean` when the fix lands (called out in EXEC log + PR description); afterwards clean is never needed |
| 8 new overloads bloat `FlowBuilder` past Detekt complexity/length limits | Low | Medium | Shared `bindClosure` helper keeps each overload to ~3 lines; Detekt run in Step 1.3 |
| Groovy `Closure` overload resolution ambiguity for Kotlin callers (lambda vs SAM) | Low | Low | Same shape as the already-shipped `flow`/`testStep` pair, proven in PR #174 with tony unaffected |
| Plugin-portal publish (`publishPlugins`, plugin-publish 0.14.0) packages a different artifact than `tasks.jar` | High | Low | Verify `publishPluginJar`/`publishPlugins --validate-only` (or inspect task wiring) uses the configured `jar` output in Step 2.2 |

## 8. Documentation Updates

- [ ] CLAUDE.md: no clean-first workflow to add (the point of the fix); check no existing text prescribes it.
- [ ] Update `doc/arc42/08_crosscutting_concepts.md` if it documents fat-jar assembly (per CLAUDE.md's architecture-docs rule).
- [ ] Retire the `infra-gradle-fat-jar-stale` memory note / e2e-test skill clean-first guidance (Step 2.3).
- [ ] Kdoc on new overloads (concise, mirroring existing style).

## 9. Rollout Plan

1. Land as one PR to `develop` (feature branch `feature/176-flows-groovy-dsl-and-fat-jar-fix`), phases as separate commits so either stream could be reverted alone.
2. Monitor: first CI build after merge (clean CI env exercises the new jar assembly from scratch); next local `publishToMavenLocal` + tony e2e (incremental path).
3. Rollback: revert the Phase 2 commit — the old copy mechanism returns intact (no other code depends on the new wiring); Phase 1 is additive API surface with no rollback hazard.

## 10. Revision History

| Date | Updated By | Changes |
|------|------------|---------|
| 2026-07-17 | AI Agent | Plan created from root-cause investigation of the stale fat jar (reproduced deletion-staleness; fix upgraded from documentation to build-script change). |
| 2026-07-17 | AI Agent | Resolved both open questions (unit tests + e2e for overloads; sources/javadoc jars out of scope); accepted. |

---

**Note**: This plan should be reviewed and approved before implementation begins.
