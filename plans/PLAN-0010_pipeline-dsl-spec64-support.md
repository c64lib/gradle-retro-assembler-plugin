# Feature: Pipeline DSL - support for spec64 test framework

**Plan ID**: PLAN-0010
**Issue**: #130
**Status**: draft
**Created**: 2026-07-17

## 1. Feature Description

### Overview
Add a new step type to the flows Pipeline DSL that runs 64spec (spec64) tests, so that automated tests can participate in flow pipelines with dependency tracking and incremental-build support, just like assembling, asset processing, and command steps do today. The legacy plugin already supports 64spec via the standalone `asmSpec` and `test` Gradle tasks; this feature brings the same capability into the flows DSL as a first-class `testStep`.

### Requirements
- A new **self-contained** `TestStep` domain step in the flows subdomain: for each spec source it first assembles the spec (via `KickAssembleSpecUseCase`, producing `.prg` + `.vs`) and then runs it on VICE (via `Run64SpecTestUseCase`, producing `.specOut` and a parsed `TestResult`).
- A `testStep(...)` DSL builder available inside `flow { ... }` blocks, following the existing `xxxStep` builder conventions; inputs are the `*.spec.asm` source files.
- The step is wired into `FlowTasksGenerator` so it produces a Gradle task per step, with both use cases injected the same way `AssembleTask` receives `KickAssembleUseCase`.
- The step participates in the flows file-based dependency graph: its inputs (spec sources, plus any files produced by upstream steps that specs depend on) drive task ordering; `.specOut` outputs drive up-to-date checks.
- All specs in a step run; the aggregate `TestReport` is printed; the task fails afterwards if any spec failed.
- VICE configuration (`viceExecutable`, `viceHeadless`, `viceAutostartPrgMode`) and KickAssembler settings are reused from the existing plugin configuration — no duplicate configuration in the DSL.

### Success Criteria
- A flow containing `testStep` runs 64spec tests through VICE and reports per-spec and overall results.
- A failing spec fails the corresponding flow task (and thus the build).
- Task dependencies are correctly derived: the test step runs after the step that produces its input `.prg`/`.vs` files.
- Unit tests cover the new domain step, DSL builder, and generator wiring; overall coverage targets (70% domain) are maintained.
- Existing legacy `asmSpec`/`test` tasks continue to work unchanged.

## 2. Root Cause Analysis

This is a feature gap, not a bug. The flows subdomain (Pipeline DSL) was introduced as the modern orchestration mechanism, but it only covers assembling, asset processing, crunching, and generic commands. Testing via 64spec is only reachable through the legacy task wiring (`wireSpecAndTest`), so projects adopting flows cannot express "run my tests as part of this pipeline".

### Current State
- `testing/64spec` domain provides `Run64SpecTestUseCase` (`testing/64spec/src/main/kotlin/.../usecase/Run64SpecTestUseCase.kt:31`) which runs a compiled spec on VICE (via `emulators/vice`'s `RunTestOnViceUseCase`) and parses the `(n/m)` result from the generated `.specOut` file.
- The legacy plugin wires this in `RetroAssemblerPlugin.wireSpecAndTest` (`infra/gradle/src/main/kotlin/com/github/c64lib/gradle/RetroAssemblerPlugin.kt:216-237`): `asmSpec` assembles `spec/**/*.spec.asm` into `.prg` + `.vs`, then `test` runs each on VICE.
- The flows subdomain has 8 step types (`AssembleStep`, `CharpadStep`, `CommandStep`, `DasmStep`, `ExomizerStep`, `GoattrackerStep`, `ImageStep`, `SpritepadStep`) and no test step.

### Desired State
A `testStep` inside a flow runs 64spec tests, ordered after whichever step produced the test binaries, with test results reported and failures failing the build.

### Gap Analysis
- **Spec assembly is not plain assembly**: `KickAssembleSpecAdapter` (`compilers/kickass/adapters/out/gradle/.../KickAssembleSpecAdapter.kt:46-55`) passes `-vicesymbols` plus 64spec-specific variables (`on_exit=jam`, `write_final_results_to_file=true`, `change_character_set=true`, `result_file_name=...`). The flows `assembleStep` (`KickAssemblerPortAdapter` → `KickAssembleUseCase`) never emits `-vicesymbols`, so it cannot produce spec-ready `.prg`/`.vs`. Hence the step is self-contained: it performs spec assembly and test execution itself.
- New domain step class + port interface in `flows`.
- New DSL builder + `FlowBuilder` registration.
- New Gradle task class + port adapter delegating to `KickAssembleSpecUseCase` and `Run64SpecTestUseCase`.
- `FlowTasksGenerator` and `RetroAssemblerPlugin.wireFlows` need to construct and inject the use case (and `wireFlows` needs access to the `RetroAssemblerPluginExtension`, which it currently does not receive).
- `flows/adapters/in/gradle` needs new module dependencies on `:testing:64spec`, `:emulators:vice`, and `:emulators:vice:adapters:out:gradle`.

## 3. Relevant Code Parts

### Existing Components
- **Run64SpecTestUseCase**: The use case the new step delegates to.
  - Location: `testing/64spec/src/main/kotlin/.../testing/a64spec/usecase/Run64SpecTestUseCase.kt:31`
  - Purpose: Runs one spec `.prg` on VICE and parses `TestResult` from `.specOut`.
  - Integration Point: Called by the new flows port adapter; constructed in `wireFlows` exactly as in `wireSpecAndTest` (`RetroAssemblerPlugin.kt:229-234`).
- **KickAssembleSpecUseCase + KickAssembleSpecAdapter**: Spec assembly (KickAssembler with `-vicesymbols` and 64spec variables).
  - Location: `compilers/kickass/src/main/kotlin/.../usecase/KickAssembleSpecUseCase.kt:30`, adapter `compilers/kickass/adapters/out/gradle/.../KickAssembleSpecAdapter.kt:33` (needs `project` + `KickAssemblerSettings`)
  - Purpose: Produces the `.prg`/`.vs` pair a spec run requires; distinct from plain `KickAssembleUseCase`.
  - Integration Point: Called by the new flows port adapter as the first phase of `TestStep` execution; constructed in `wireFlows` exactly as in `wireSpecAndTest` (`RetroAssemblerPlugin.kt:222-227`) — `wireFlows` already receives `settings`.
- **functions.kt (file-name conventions)**: Derives `.prg`, `.vs`, `.specOut` paths from a spec source.
  - Location: `testing/64spec/src/main/kotlin/.../usecase/functions.kt:29-33`
  - Purpose: Defines the intermediate-file contract the step's inputs/outputs must honour.
  - Integration Point: The `TestStep`'s inputs/outputs declaration must match these names for correct incremental builds.
- **TestReport**: Aggregates `TestResult`s and prints the report.
  - Location: `testing/64spec/adapters/in/gradle/src/main/kotlin/.../TestReport.kt:29`
  - Purpose: Reused by the new `TestTask` for identical reporting to the legacy `test` task.
  - Integration Point: `TestTask` (or the step) folds results and throws on failure.
- **AssembleStep (best analog)**: End-to-end pattern for a step wrapping another domain via a port.
  - Location: `flows/src/main/kotlin/.../domain/steps/AssembleStep.kt:39`, port `flows/src/main/kotlin/.../usecase/port/AssemblyPort.kt:35`, task `flows/adapters/in/gradle/.../tasks/AssembleTask.kt:38`, adapter `flows/adapters/in/gradle/.../assembly/KickAssemblerPortAdapter.kt:37`
  - Purpose: Template for `TestStep`/`TestPort`/`TestTask`/`Run64SpecTestPortAdapter`.
  - Integration Point: Same injected-use-case pattern (Exomizer's no-injection pattern is not applicable because VICE needs `project` + `extension`).
- **FlowDsl / FlowBuilder**: DSL entry point where step builder methods are registered.
  - Location: `flows/adapters/in/gradle/.../dsl/FlowDsl.kt:67` (e.g. `assembleStep` at 133-146)
  - Purpose: Add `testStep(name) { ... }` here; register input/output `FlowArtifact`s for dependency tracking.
- **FlowTasksGenerator**: Creates Gradle tasks from steps and injects use cases.
  - Location: `flows/adapters/in/gradle/.../FlowTasksGenerator.kt:52` (constructor), `:138` (`createStepTask` dispatch), `:200` (`configureBaseTask` injection)
  - Purpose: Add constructor param, `is TestStep ->` branch, and injection block.
- **RetroAssemblerPlugin.wireFlows**: DI composition root for flows.
  - Location: `infra/gradle/src/main/kotlin/com/github/c64lib/gradle/RetroAssemblerPlugin.kt:244`
  - Purpose: Construct `Run64SpecTestUseCase(RunTestOnViceUseCase(RunTestOnViceAdapter(project, extension)))` and pass to the generator; requires threading `extension` into `wireFlows`.
- **RetroAssemblerPluginExtension**: VICE + spec configuration source.
  - Location: `shared/gradle/.../dsl/RetroAssemblerPluginExtension.kt:50-54` (`viceExecutable`, `viceHeadless`, `viceAutostartPrgMode`, `specDirs`, `specIncludes`)
  - Purpose: VICE settings reused as-is; `specDirs`/`specIncludes` remain legacy-only (flows steps are explicit-file driven).

### Architecture Alignment
- **Domain**: `flows` (orchestrator), delegating to `compilers/kickass` (spec assembly), `testing/64spec`, and transitively `emulators/vice`.
- **Use Cases**: No new use cases; reuse `KickAssembleSpecUseCase`, `Run64SpecTestUseCase` (and `RunTestOnViceUseCase`).
- **Ports**: New `TestPort` in `flows/src/main/kotlin/.../usecase/port/` covering both phases of a spec run — e.g. `fun assembleSpec(source: File)` and `fun runSpec(source: File): TestResult` (exact signature decided in Phase 1).
- **Adapters**: New `TestStepBuilder` (in-adapter DSL), `TestTask` (in-adapter Gradle task), `Spec64TestPortAdapter` (port implementation in `flows/adapters/in/gradle/.../port/`, following `FlowExomizerAdapter` placement) delegating to both use cases.

### Verification Project: `../common` (c64lib/common)
The sibling checkout `../common` (https://github.com/c64lib/common) is a real consumer of the 64spec testing feature and serves as the e2e harness for this plan:
- Uses the plugin (currently `1.6.0`) with `dialect = "KickAssembler"`, `dialectVersion = "5.25"`, and pulls the test framework via `libFromGitHub "c64lib/64spec", "0.7.0pr"` (`../common/build.gradle`).
- Contains 8 real spec files in `spec/` (`math-add16.spec.asm`, `mem-cmp16.spec.asm`, `copy-large-mem-forward.spec.asm`, `decompress-rle.spec.asm`, …) exercising the library sources in `lib/`, following the `sfspec: init_spec() / describe / it / assert_*` 64spec conventions.
- Today it runs tests through the legacy `asmSpec` + `test` tasks; for e2e verification it will be temporarily switched to the locally published `1.8.1-SNAPSHOT` and given a `flows { flow { testStep(...) } }` block listing the spec sources — changes to `../common` are scratch-only and not committed.

### Dependencies
- `flows/build.gradle.kts` (domain module): add `:testing:64spec` for the `TestResult` type in the `TestPort` signature (per Design Decision).
- `flows/adapters/in/gradle/build.gradle.kts`: add `implementation(project(":testing:64spec"))` (use case + file-name functions), `implementation(project(":testing:64spec:adapters:in:gradle"))` (reuse `TestReport`) — or replicate the small aggregation logic if pulling an in-adapter module in feels wrong; `:compilers:kickass` is already a dependency (used by `AssembleTask`). Verify at implementation time whether `:emulators:vice` types leak into signatures and add it only if needed.
- No new module → no `infra/gradle` `compileOnly` change needed (all touched modules are already wired); verify at implementation time.

## 4. Questions and Clarifications

### Self-Reflection Questions
- **Q**: How does 64spec test execution work today?
  - **A**: `asmSpec` (KickAssembler) compiles `spec/**/*.spec.asm` into `<name>.prg` + `<name>.vs`; `test` runs each `.prg` on VICE with the `.vs` mon-commands, VICE writes `<name>.specOut`, and the use case parses the `(n/m)` result.
- **Q**: Which existing step is the right implementation template?
  - **A**: `AssembleStep` — it wraps another domain's use case behind a flows port with the use case injected through `FlowTasksGenerator`. Exomizer's direct-instantiation pattern doesn't apply because VICE needs `project` and `extension`.
- **Q**: Does `wireFlows` have access to everything needed?
  - **A**: Not yet — it receives `settings` and `flowsExtension` but not the `RetroAssemblerPluginExtension` required by `RunTestOnViceAdapter`; it must be threaded in.
- **Q**: Should the test step assemble the spec itself or require an upstream `assembleStep` to produce the `.prg`/`.vs`?
  - **A**: Initially decided as run-only (2026-07-17), then **revised to self-contained** the same day after gap analysis: the flows `assembleStep` cannot produce spec binaries because only `KickAssembleSpecAdapter` emits `-vicesymbols` and the 64spec KickAssembler variables (`on_exit=jam`, `write_final_results_to_file=true`, `change_character_set=true`, `result_file_name`). Rather than duplicating spec-assembly concerns into `assembleStep` or adding a second step type, `testStep` assembles and runs each spec itself (user decision, 2026-07-17).
- **Q**: Can the flows `assembleStep` produce spec-ready `.prg`/`.vs` binaries?
  - **A**: No. `AssemblyCommand`/`KickAssembleCommand` support `defines` and `values`, but the `-vicesymbols` flag and the 64spec variable set are only applied by `KickAssembleSpecAdapter` (`compilers/kickass/adapters/out/gradle/.../KickAssembleSpecAdapter.kt:46-55`). This is why the step is self-contained.
- **Q**: Is there a real project to verify against end-to-end?
  - **A**: Yes — the sibling checkout `../common` (c64lib/common) uses 64spec via the legacy tasks with 8 spec files in `spec/`; see *Verification Project* in Section 3.
- **Q**: DSL naming — `testStep`, `specStep`, or `spec64Step`?
  - **A**: `testStep` (user decision, 2026-07-17). Consistent with the legacy `test` task and framework-agnostic; the framework choice can become a step property later.
- **Q**: Multiple spec files per step, or one spec per step?
  - **A**: Multiple specs per step (user decision, 2026-07-17). One step runs a list of specs and prints an aggregate `TestReport`, like the legacy `test` task; the step re-runs when any input changes.
- **Q**: Should flow test tasks be wired into the legacy `test`/`build` lifecycle?
  - **A**: Flows-only (user decision, 2026-07-17). Test steps run via their flow tasks / the `flows` aggregation task only; legacy and flows pipelines stay independent and the user controls placement.

### Unresolved Questions
None — all questions resolved (see Self-Reflection Questions).

### Design Decisions
- **Decision**: Where the port's result type lives.
  - **Chosen**: Reuse `TestResult` from `:testing:64spec` in the `TestPort` signature (flows module gains a dependency on the `:testing:64spec` domain module).
  - **Rationale**: `TestResult` is a pure value object; flows already orchestrates other domains, and a flows-local type would add a mapping layer with no practical decoupling benefit. (User decision, 2026-07-17.)
- **Decision**: Spec assembly inside the step vs. upstream step.
  - **Chosen**: Self-contained step — `testStep` assembles each spec (via `KickAssembleSpecUseCase`) and then runs it (via `Run64SpecTestUseCase`). *(Revised 2026-07-17; supersedes the earlier run-only choice.)*
  - **Rationale**: Spec assembly is a specialised KickAssembler invocation (`-vicesymbols` + 64spec variables) that the flows `assembleStep` doesn't and shouldn't produce; a self-contained step keeps the DSL simple (specs in, results out) and avoids either a second step type or overloading `assembleStep`. Mirrors what `asmSpec` + `test` do together.
- **Decision**: DSL method name.
  - **Chosen**: `testStep`.
  - **Rationale**: Consistent with the legacy task name `test` and generic enough if other test frameworks arrive.
- **Decision**: Step granularity.
  - **Chosen**: One `testStep` accepts multiple spec inputs and aggregates results into a single `TestReport`.
  - **Rationale**: Fewer tasks, identical reporting to the legacy `test` task; incremental-build granularity per step is sufficient.
- **Decision**: Build lifecycle integration.
  - **Chosen**: Flows-only — no dependency from legacy `test`/`build` onto flow test tasks.
  - **Rationale**: Keeps the legacy and flows pipelines independent; users opt in by running `flows`/`asm` or wiring dependencies themselves.
- **Decision**: DSL input style.
  - **Chosen**: Explicit files only — `from("spec/foo.spec.asm", ...)` like other flow steps; no glob/directory discovery.
  - **Rationale**: Keeps the file-based dependency graph exact and each spec source individually tracked for up-to-date checks. (User decision, 2026-07-17.)
- **Decision**: Failure mode with multiple specs.
  - **Chosen**: Run all specs, print the aggregate `TestReport`, then fail the task if any spec failed.
  - **Rationale**: Identical semantics to the legacy `test` task; gives a complete picture per run. (User decision, 2026-07-17.)

## 5. Implementation Plan

### Phase 1: Domain step and port (flows domain layer)
**Goal**: `TestStep` and `TestPort` exist in the flows domain with validation and unit tests.

1. **Step 1.1**: Create `TestPort` interface
   - Files: `flows/src/main/kotlin/.../usecase/port/TestPort.kt`
   - Description: Port covering both phases of a spec run — `assembleSpec(source: File)` and `runSpec(source: File): TestResult` (`TestResult` reused from `:testing:64spec` per Design Decision; alternatively a single `executeSpec(source): TestResult` if the two phases never need separating — decide during implementation). Add the `:testing:64spec` dependency to `flows/build.gradle.kts`.
   - Testing: Compilation; exercised via step tests with a fake port.
2. **Step 1.2**: Create `TestStep` domain step
   - Files: `flows/src/main/kotlin/.../domain/steps/TestStep.kt`
   - Description: `data class TestStep(name, inputs, outputs, var port: TestPort? = null) : FlowStep(name, "test", inputs, outputs)`. Inputs are `*.spec.asm` sources; outputs are the derived `.specOut` files. `execute()` validates the port, resolves input spec sources via base-class helpers, then for each spec assembles and runs it via the port; all specs run, results are aggregated, and `StepExecutionException` is thrown afterwards if any failed. `validate()` enforces at least one input and the `.spec.asm` extension convention.
   - Testing: `flows/src/test/kotlin/.../domain/steps/TestStepTest.kt` (Kotest BehaviorSpec, fake `TestPort`): success, failing tests (all specs still executed), missing port, missing inputs.

**Phase 1 Deliverable**: Mergeable domain-layer step (unused by DSL yet), fully unit-tested.

### Phase 2: DSL builder and Gradle task wiring
**Goal**: `testStep` usable in `flow { ... }`, generating a working Gradle task.

1. **Step 2.1**: Create `TestStepBuilder` and register `testStep` in `FlowBuilder`
   - Files: `flows/adapters/in/gradle/.../dsl/TestStepBuilder.kt`, `flows/adapters/in/gradle/.../dsl/FlowDsl.kt`
   - Description: Builder with `from(...)`/`to(...)` following `AssembleStepBuilder`; `FlowBuilder.testStep(name, configure)` registers the step and its `FlowArtifact`s for dependency tracking.
   - Testing: `TestStepBuilderTest.kt` mirroring `CommandStepBuilderTest.kt`; `FlowDslDependencyTest.kt` addition asserting ordering after a producing step.
2. **Step 2.2**: Create `Spec64TestPortAdapter` and `TestTask`
   - Files: `flows/adapters/in/gradle/.../port/Spec64TestPortAdapter.kt`, `flows/adapters/in/gradle/.../tasks/TestTask.kt`, `flows/adapters/in/gradle/build.gradle.kts`
   - Description: Adapter implements `TestPort` by delegating to injected `KickAssembleSpecUseCase` (assemble phase — deriving `resultFileName` via `functions.kt` conventions) and `Run64SpecTestUseCase` (run phase). `TestTask : BaseFlowStepTask` follows `AssembleTask.kt:38`: injects both use cases, sets the port on the step, builds the context, executes, and reports via `TestReport`, throwing `GradleException` on failure. Add the module dependencies per Section 3.
   - Testing: Unit test for the adapter with stubbed use cases (assemble called before run, result mapping); task covered in Step 2.3's generator test.
3. **Step 2.3**: Wire `FlowTasksGenerator`
   - Files: `flows/adapters/in/gradle/.../FlowTasksGenerator.kt`
   - Description: Add `kickAssembleSpecUseCase` and `run64SpecTestUseCase` constructor params (nullable, mirroring `kickAssembleUseCase`), `is TestStep ->` branch in `createStepTask` (`:138`), and injection block in `configureBaseTask` (`:200`).
   - Testing: Extend `FlowTasksGeneratorTest.kt` — task created, named `flow{Flow}Step{Name}`, use cases injected (needs the `--add-opens java.base/java.lang=ALL-UNNAMED` ProjectBuilder convention already used by these tests).

**Phase 2 Deliverable**: Mergeable DSL + task layer; `testStep` works when the generator is constructed with the use case (not yet by the plugin).

### Phase 3: Plugin integration, e2e verification, documentation
**Goal**: Feature reachable from a real build; docs updated.

1. **Step 3.1**: Wire the plugin composition root
   - Files: `infra/gradle/src/main/kotlin/com/github/c64lib/gradle/RetroAssemblerPlugin.kt`
   - Description: Thread `extension` into `wireFlows` (`:244`); construct `KickAssembleSpecUseCase(KickAssembleSpecAdapter(project, settings))` and `Run64SpecTestUseCase(RunTestOnViceUseCase(RunTestOnViceAdapter(project, extension)))` exactly as in `wireSpecAndTest` (`:222-234`) and pass both to `FlowTasksGenerator` (`settings` is already available in `wireFlows`). Per the lifecycle decision, do **not** add any dependency from the legacy `test`/`build` tasks onto flow test tasks.
   - Testing: `./gradlew build`; plugin functional test if present.
2. **Step 3.2**: End-to-end verification against `../common`
   - Files: none in this repo (scratch changes in `../common`, not committed)
   - Description: Publish `1.8.1-SNAPSHOT` to mavenLocal (as the `e2e-test` skill does for tony); temporarily switch `../common/build.gradle` to that version (with mavenLocal in plugin resolution) and add a `flows { flow { testStep(...) } }` block listing a few of its 8 real spec files (e.g. `spec/math-add16.spec.asm`, `spec/mem-cmp16.spec.asm`). Verify: specs assemble, VICE runs them, per-spec and overall `TestReport` output matches the legacy `test` task's, and the flow task is UP-TO-DATE on re-run without input changes.
   - Testing: Manual/e2e — all 8 common specs pass via `testStep`; a deliberately broken assertion fails the build; results match running the legacy `./gradlew test` in the same project.
3. **Step 3.3**: Documentation
   - Files: `doc/arc42/05_building_block_view.md`, `doc/arc42/08_crosscutting_concepts.md`, `FlowsExtension.kt`/`FlowDsl.kt` KDoc, `CLAUDE.md` (step list if enumerated), AsciiDoc user docs under `doc/` if flows are documented there
   - Description: Document the new step type, its required upstream artifacts (`.prg`/`.vs`), VICE configuration reuse, and update the arc42 building-block page per CLAUDE.md's architecture-docs rule.
   - Testing: Docs build (documentation workflow) renders.

**Phase 3 Deliverable**: Fully integrated, verified, documented feature.

## 6. Testing Strategy

### Unit Tests
- `TestStepTest` (flows domain): execute/validate paths with a fake port — success, test failure → `StepExecutionException` (with all specs still executed), null port, empty inputs, non-`.spec.asm` input rejected.
- `TestStepBuilderTest` (dsl): builder produces correctly configured step; validation errors.
- `Spec64TestPortAdapterTest`: delegates to stubbed `KickAssembleSpecUseCase` + `Run64SpecTestUseCase` (assemble before run), maps results.
- `FlowTasksGeneratorTest` extension: task creation + injection for `TestStep`.

### Integration Tests
- `FlowDslDependencyTest`: a flow where an upstream step produces a file the `testStep` declares as input (e.g. a generated include) derives the correct task dependency; independent `testStep` runs in parallel with unrelated steps.

### Manual Testing
- e2e against `../common` (c64lib/common, 8 real spec files — see Section 3 *Verification Project*): green specs pass with output matching the legacy `test` task, a deliberately broken assertion fails the build with the `Overall test report (n/m)` output; incremental build marks the test task UP-TO-DATE when inputs unchanged.

## 7. Risks and Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| VICE not installed/headless issues in e2e or CI environments | Medium | Medium | Keep VICE interaction behind the existing `RunTestOnVicePort`; unit tests use fakes; e2e is manual via `../common` where VICE is available |
| Self-contained step re-assembles and re-runs all its specs when any single input changes | Low | Medium | Accepted trade-off of the multi-spec granularity decision; users can split specs across multiple `testStep`s for finer incremental builds |
| Flows domain gaining a compile dependency on `:testing:64spec` blurs domain boundaries | Low | Medium | Accepted by design decision — only the `TestResult` value object crosses the boundary; a flows-local result type remains a cheap fallback isolated to the port signature |
| `.specOut` produced next to spec sources pollutes the flows outputs model (outputs land in source tree, not `build/`) | Medium | Medium | Declare `.specOut` as the step's output for up-to-date checks; document; a later refactor could redirect outputs but is out of scope |
| Threading `extension` into `wireFlows` disturbs existing wiring | Low | Low | Mechanical parameter addition; covered by existing build + generator tests |
| Duplicate task-name clash if a flow step task collides with legacy `test` task | Low | Low | Flow tasks are namespaced `flow{Flow}Step{Name}`; no collision with `test` |

## 8. Documentation Updates

- [ ] Update `doc/arc42/05_building_block_view.md` (flows building block: new step type, new dependency on testing/emulators domains)
- [ ] Update `doc/arc42/08_crosscutting_concepts.md` if the port-injection concept enumerates steps
- [ ] KDoc for `TestStep`, `TestPort`, `TestStepBuilder`, `FlowBuilder.testStep` (concise, 3–5 lines per class)
- [ ] Update user-facing flows documentation (AsciiDoc under `doc/`) with a `testStep` example
- [ ] CLAUDE.md: no structural change expected; update step examples only if needed

## 9. Rollout Plan

1. Merge per phase behind no flag — the step is inert unless a user writes `testStep` in their DSL; legacy `asmSpec`/`test` untouched.
2. e2e-verify against tony before release; release as part of the next minor version (new DSL capability).
3. Rollback: revert the PR(s); no data or config migration involved.

## 10. Revision History

| Date | Updated By | Changes |
|------|------------|---------|
| 2026-07-17 | AI Agent | Initial plan created from issue #130 and codebase analysis. |
| 2026-07-17 | AI Agent | Resolved all four open questions (run-only step, `testStep` name, multi-spec granularity, flows-only lifecycle); updated Design Decisions and Step 3.1 accordingly. |
| 2026-07-17 | AI Agent | Recorded three further decisions: reuse `TestResult` in `TestPort`, explicit-files-only DSL input, run-all-then-fail failure mode; propagated to Steps 1.1 and Risks. |
| 2026-07-17 | AI Agent | Gap analysis: flows `assembleStep` cannot produce spec binaries (`-vicesymbols` + 64spec variables only in `KickAssembleSpecAdapter`); decision revised to **self-contained `testStep`** (assemble + run). Added `../common` (c64lib/common) as the e2e verification project. Propagated to Requirements, Gap Analysis, Relevant Code, Design Decisions, Implementation Plan, Testing Strategy, and Risks. |

---

**Note**: This plan should be reviewed and approved before implementation begins.
