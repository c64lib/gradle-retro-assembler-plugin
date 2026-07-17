# Execution Log: Pipeline DSL - support for spec64 test framework

**Exec ID**: EXEC-0010
**Plan**: [PLAN-0010](PLAN-0010_pipeline-dsl-spec64-support.md)
**Issue**: #130
**Started**: 2026-07-17
**Last Updated**: 2026-07-17
**State**: completed

## 1. Execution Sessions

<!-- One subsection per execution run (a /execute invocation). Append; never rewrite past sessions. -->

### Session 1 — 2026-07-17

- **Scope**: all (Phases 1–3, steps 1.1–3.3)
- **Mode**: per-phase
- **Outcome**: completed (all 8 steps)

| Step | Result | Verification | Notes |
|------|--------|--------------|-------|
| 1.1 | completed | `:flows:test` BUILD SUCCESSFUL | Added `TestPort` (`assembleSpec`/`runSpec`); flows domain module now `implementation(:testing:64spec)` for `TestResult`. |
| 1.2 | completed | `:flows:test` BUILD SUCCESSFUL (all green) | Added `TestStep` (specs∪watched inputs, run-all-then-fail) + `TestStepTest` covering success, partial failure, missing port, validation, specs-only execution. |
| 2.1 | completed | `:flows:adapters:in:gradle:test` BUILD SUCCESSFUL | `TestStepBuilder` (`spec`/`specs`/`from`, derives `.prg`/`.vs`/`.specOut` outputs) + `FlowBuilder.testStep`; `TestStepBuilderTest`. |
| 2.2 | completed | `:flows:adapters:in:gradle:test` BUILD SUCCESSFUL | `Spec64TestPortAdapter` (assemble via libDirs/defines, `.specOut` naming; run delegates) + `TestTask` (BaseFlowStepTask, injects both use cases + extension) + `Spec64TestPortAdapterTest`. Module deps: `:testing:64spec` (impl), `:emulators:vice` (testImpl). |
| 2.3 | completed | `:flows:adapters:in:gradle:test` BUILD SUCCESSFUL | `FlowTasksGenerator` gains 3 nullable params, `is TestStep ->` branch, injection block, `configureOutputFiles` case; `FlowTasksGeneratorTest` extended (task type/name/injection + missing-usecase failure). |
| 3.1 | completed | `:infra:gradle:compileKotlin` + `:infra:gradle:test` BUILD SUCCESSFUL | `wireFlows` now takes `extension` + `depTasks`; constructs `KickAssembleSpecUseCase`/`Run64SpecTestUseCase` as in `wireSpecAndTest`, passes all to generator; `project.tasks.withType(TestTask).configureEach { dependsOn(resolveDevDeps, downloadDependencies) }`. No new `infra/gradle` deps (all already present). |
| 3.2 | completed | e2e against `../common` (2 specs, VICE x64sc, 64spec 0.7.0pr) — scratch, reverted | PASS: Groovy DSL callable (after Closure-overload fix, deviation #2); specs assemble+run on VICE; `(19/19)`+`(4/4)` printed; UP-TO-DATE with no change; re-runs on watched `lib/` content change; failing assertion → BUILD FAILED with `Step 'specs': 64spec tests failed: overall (22/23)`. One finding logged (deviation #4, stale outputs in source tree). `../common` restored clean. |
| 3.3 | completed | `./gradlew build -x detekt` BUILD SUCCESSFUL (274 tasks, tests + spotless green) | Updated `doc/arc42/building-blocks/flows.md` (purpose, `TestStep`, `TestPort` row + cross-domain note, `TestStepBuilder`/`TestTask`/Groovy overloads/dependsOn); added a "Test Step" section to `doc/index.adoc` user manual (`specs`/`from`, produced files, config reuse). §8 crosscutting doesn't enumerate steps by port → no change; CLAUDE.md doesn't enumerate steps → no change. KDoc already concise on new classes. Spotless reformatted new sources (`spotlessApply`). |

## 2. Deviations from Plan

<!-- Every departure from the plan as written -->

| # | Step | Deviation | Reason | Impact |
|---|------|-----------|--------|--------|
| 1 | 1.2 / 2.2 | Test aggregation (per-spec print + overall pass/fail) is done inside `TestStep.execute()` in the domain, not by reusing `TestReport` from `:testing:64spec:adapters:in:gradle`. | The plan (§3 Dependencies) flagged pulling an in-adapter module into another in-adapter module as awkward and offered "replicate the small aggregation logic" as the alternative. Keeping aggregation in the domain step avoids an adapter→adapter dependency and keeps the step self-sufficient. | `flows/adapters/in/gradle` does NOT depend on `:testing:64spec:adapters:in:gradle`. Output wording ("Tests execution …", "overall (n/m)") is equivalent to `TestReport` but not identical char-for-char; e2e (3.2) should confirm parity with the legacy `test` task is acceptable. |
| 2 | 2.1 / 3.2 | Added Groovy `Closure` overloads for `FlowDslBuilder.flow` and `FlowBuilder.testStep` (with `@DelegatesTo`), beyond the Kotlin receiver-lambda originally written. | E2e (3.2) surfaced the risk the plan flagged: from a Groovy `build.gradle` (`../common`), the Kotlin `FlowBuilder.() -> Unit` receiver lambda does not bind the closure delegate, so `testStep()` was "not found" (`Could not find method testStep()`). This is the first time the flows DSL is exercised from Groovy at all. | Groovy consumers can now use `flows { flow("x") { testStep("y") { ... } } }`. Kotlin DSL (tony) is unaffected — the receiver-lambda overloads remain and win for Kotlin callers. Scoped to `flow` + `testStep` (what this feature needs); other step methods remain Kotlin-only for now (follow-up candidate). |
| 3 | 3.2 | Build-system gotcha: `:infra:gradle`'s fat jar (`copySubProjectClasses`) bundled STALE `flows` classes; the Closure overloads were missing from the published `1.8.1-SNAPSHOT` jar until `:infra:gradle:clean` was run before `publishToMavenLocal`. | The `copySubProjectClasses` task copies resolved subproject artifacts into the fat jar; without cleaning `infra/gradle/build`, a stale copy persisted. | For local e2e / publishing, run `:infra:gradle:clean publishToMavenLocal` (or clean the infra jar) after changing a subproject, else the consumer tests a stale plugin. Candidate CLAUDE.md / e2e-test-skill note. |
| 4 | 3.2 | Incremental correctness finding (NOT fixed): outputs (`.prg`/`.vs`/`.specOut`) land in the source tree (`spec/`), and the task's `outputDirectory` is their parent. When those artifacts already exist, an edited spec re-runs the task but VICE could execute a not-freshly-regenerated `.prg`, so a newly-introduced failure was masked (still `(19/19)`) until the stale artifacts were deleted; a clean state propagates failure correctly (`(18/19)` → BUILD FAILED). | Matches the plan's Medium risk "`.specOut` produced next to spec sources pollutes the flows outputs model". Root-causing the exact assemble/run ordering vs. Gradle output snapshotting is out of this session's scope. | Feature is correct from a clean state; incremental re-runs on spec *edits* may need the derived artifacts treated as declared `@OutputFiles` that Gradle restores/cleans, or the assemble phase made unconditional. Logged as a follow-up (§3). |

## 3. Follow-ups

- **Incremental re-run vs. stale outputs in source tree** (from deviation #4): when a spec's `.prg`/`.vs`/`.specOut` already exist next to the source, editing the spec re-runs the flow task but the run may execute a not-freshly-reassembled `.prg`, masking a newly-introduced failure until the artifacts are deleted. From a clean state the feature is correct (pass/fail propagate properly). Candidate fix: declare the derived files as proper `@OutputFiles` so Gradle snapshots/restores them, or make the assemble phase unconditional per run. Worth a dedicated issue.
- **Groovy DSL coverage for other step types** (from deviation #2): only `flow` + `testStep` got Groovy `Closure` overloads. If Groovy consumers need `assembleStep`/`commandStep`/etc., the same overload treatment (or a shared approach) should be applied across all `FlowBuilder` step methods.
- **Build/publish note** (from deviation #3): document that `:infra:gradle:clean` must precede `publishToMavenLocal` after subproject changes so the fat jar isn't stale — candidate for CLAUDE.md and/or the `e2e-test` skill.
