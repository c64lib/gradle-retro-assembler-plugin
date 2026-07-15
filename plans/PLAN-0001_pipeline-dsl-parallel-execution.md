# Feature: Pipeline DSL - Enable Parallel Execution

**Plan ID**: PLAN-0001
**Issue**: #135
**Status**: in progress
**Created**: 2026-03-29
**Last Updated**: 2026-07-15

> Migrated from `.ai/135-pipeline-dsl-parallel-execution/feature-135-pipeline-dsl-parallel-execution-action-plan.md` into the `plans/` system. All open questions have been answered (see Section 4) — the plan is eligible for `accepted`.

## 1. Feature Description

### Overview
Enable the Pipeline DSL flows to execute independent steps and flows in parallel by wiring the existing `FlowDependencyGraph` domain logic into the Gradle task dependency graph in `FlowTasksGenerator`. Currently the domain layer already computes which flows and steps can run in parallel, but the Gradle adapter ignores this information and wires all steps sequentially.

### Requirements
- Independent flows (no dependency relationship between them) must be able to execute in parallel when Gradle `--parallel` is enabled
- Independent steps within a flow must be able to execute in parallel when they have no input/output relationship
- Flow-level task dependencies must be derived from `FlowDependencyGraph` rather than only from explicit `dependsOn` declarations
- Step-level task dependencies within a flow must be derived from file input/output relationships, not from positional ordering
- The feature must use Gradle's native task scheduling mechanism — no custom threading
- No DSL changes visible to plugin users (parallel execution is automatic and transparent)
- Parallel execution is opt-in via Gradle's standard `--parallel` flag or `org.gradle.parallel=true` in `gradle.properties`

### Success Criteria
- Two independent flows both execute in the same Gradle parallel execution wave
- Two independent steps within a single flow (i.e. no shared inputs/outputs) execute in parallel when `--parallel` is enabled
- A step that produces a file consumed by another step still executes before that consumer step
- A flow that depends on another flow (via `dependsOn` or artifact consumption) still executes after the dependency flow completes
- All existing tests continue to pass
- New unit tests cover the updated `FlowTasksGenerator` dependency wiring logic

## 2. Root Cause Analysis

### Current State
`FlowTasksGenerator.registerTasks()` currently wires step-level task dependencies using a simple index-based sequential chain:

```kotlin
// In FlowTasksGenerator.registerTasks()
flow.steps.forEachIndexed { index, step ->
    // ...
    if (index > 0) {
        stepTask.dependsOn(flowStepTasks[index - 1])  // Always sequential
    }
}
```

This means every step in a flow waits for the previous step to finish, even if the two steps are completely independent (different input and output files).

Flow-level dependencies use explicit `dependsOn` names only:
```kotlin
flow.dependsOn.forEach { depName ->
    tasksByFlowName[depName]?.let { depTask -> flowTask.dependsOn(depTask) }
}
```

There is a separate `setupFileDependencies()` method that creates cross-step dependencies based on file relationships, but it only links steps where an output of one step is an input of another. This is good, but it still cannot break the sequential within-flow chain established by the index loop.

The domain layer has rich parallel execution analysis:
- `FlowDependencyGraph.getParallelExecutionOrder()` — topological sort into parallel waves
- `FlowDependencyGraph.getParallelCandidates()` — which flows can run alongside a given flow
- `FlowService` — facade over the graph, never used by `FlowTasksGenerator`
- `FlowExecutionTask` — a legacy placeholder with a `TODO` that is no longer used by `FlowTasksGenerator`

### Desired State
`FlowTasksGenerator` should:
1. Remove the index-based sequential step chaining within each flow
2. Build step-level task dependencies solely from file input/output relationships (already partially done in `setupFileDependencies()`, needs to cover within-flow steps too)
3. Build flow-level task dependencies from `FlowDependencyGraph`'s full dependency analysis (explicit `dependsOn` + implicit artifact-based dependencies) — the domain already tracks both

When Gradle runs with `--parallel`, independent tasks will execute concurrently without any additional changes to the plugin.

### Gap Analysis
| Gap | Location | What to Change |
|-----|----------|----------------|
| Sequential within-flow step chaining | `FlowTasksGenerator.registerTasks()` lines 66-68 | Remove the `index > 0` sequential `dependsOn` block |
| Flow-level dependencies incomplete | `FlowTasksGenerator.registerTasks()` lines 85-90 | Use `FlowDependencyGraph` to compute all flow-level dependencies (artifact-based implicit deps are currently missing) |
| `setupFileDependencies()` only runs after all tasks created | `FlowTasksGenerator.setupFileDependencies()` | Ensure this method correctly handles within-flow dependencies after the sequential chain is removed |
| `FlowExecutionTask` is dead code | `FlowExecutionTask.kt` | Evaluate for removal or repurposing (it has a `TODO` and is not registered anywhere) |
| **Artifact identity is broken for DSL-built flows** (red-team finding) | `Flow.kt:174-179`, `FlowDsl.kt:173-178` | `FlowArtifact` is a data class whose equality includes the auto-generated `name` (`{step}_input_{n}` / `{step}_output_{n}`), so a producer's and consumer's artifact for the same path are never equal — `getAllDependencies()` finds zero implicit deps for DSL-built flows. Artifact matching must become path-based (or DSL naming must align) before flow-level implicit dependencies can work |
| **Validation produces false ERRORs for DSL-built flows** (red-team finding) | `FlowDependencyGraph.kt:178-189`, `FlowDsl.kt:174` | DSL inputs default `isSourceFile = false` and never match a producer, so `detectMissingArtifactProducers` flags every consumed source asset as an ERROR — fail-on-error validation would break existing consumer builds (incl. tony) until this is fixed |

## 3. Relevant Code Parts

### Existing Components

- **FlowTasksGenerator**: Main adapter that creates Gradle tasks from flow definitions and wires dependencies
  - Location: `flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/FlowTasksGenerator.kt`
  - Purpose: Creates one Gradle task per `FlowStep`, one aggregation task per `Flow`, one top-level `flows` aggregation task
  - Integration Point: Must be updated to remove sequential step chaining and use the dependency graph for flow-level deps

- **FlowDependencyGraph**: Domain class that builds a DAG of flows and computes topological ordering
  - Location: `flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/FlowDependencyGraph.kt`
  - Purpose: Validates the flow graph and computes which flows can run in parallel
  - Integration Point: `FlowTasksGenerator` should call `FlowDependencyGraph.addFlow()` and use the resulting dependency information

- **FlowService**: Domain facade over `FlowDependencyGraph`
  - Location: `flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/FlowService.kt`
  - Purpose: `validateFlows()`, `getExecutionPlan()`, `findParallelCandidates()` — all currently unused by `FlowTasksGenerator`
  - Integration Point: `FlowTasksGenerator` can call `FlowService.validateFlows()` at configuration time to catch dependency errors early, and use it to compute the flow dependency structure

- **BaseFlowStepTask**: Base Gradle task class for all flow step tasks
  - Location: `flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/tasks/BaseFlowStepTask.kt`
  - Purpose: Holds `inputFiles`, `outputDirectory`, and `flowStep` properties with Gradle `@Input`/`@Output` annotations
  - Integration Point: No changes needed; Gradle's incremental build uses these annotations to determine up-to-date status

- **FlowExecutionTask**: Legacy placeholder task (dead code)
  - Location: `flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/FlowExecutionTask.kt`
  - Purpose: Was the original task concept before dedicated per-step tasks were introduced; contains a `TODO` and `println` statement
  - Integration Point: Should be removed as part of cleanup

- **FlowDependencyGraphTest**: Comprehensive test suite for the dependency graph
  - Location: `flows/src/test/kotlin/com/github/c64lib/rbt/flows/domain/FlowDependencyGraphTest.kt`
  - Purpose: Already proves the domain logic correctly identifies parallel candidates — confirms Phase 1 of domain work is complete
  - Integration Point: Tests for `FlowTasksGenerator` should verify the same scenarios at the Gradle task level

### Architecture Alignment
- **Domain**: `flows` — the graph algorithms are sound, but artifact matching semantics must change (Phase 0): producer/consumer matching keyed by path, DSL inputs marked as source files
- **Use Cases**: No new use cases. `FlowService` gains a public per-flow dependency accessor (Step 3.2) — it is the adapter's only access path, since `FlowDependencyGraph` is `internal` to the `flows` module and invisible to `flows:adapters:in:gradle`
- **Ports**: No new ports needed
- **Adapters**: `FlowTasksGenerator` in `flows/adapters/in/gradle` carries the wiring change; it consumes the domain exclusively through `FlowService`

### Dependencies
- No new module dependencies required
- The `flows` module (domain) is already a dependency of `flows:adapters:in:gradle`
- Gradle's `--parallel` feature is a Gradle runtime concern; no API additions needed

## 4. Questions and Clarifications

### Self-Reflection Questions
- **Q**: Does Gradle need `--parallel` to be explicitly set for the new dependency wiring to take effect?
  - **A**: Yes. Gradle only runs tasks in parallel when `--parallel` flag is passed or `org.gradle.parallel=true` is set in `gradle.properties`. The correct dependency wiring is a prerequisite for parallel execution to be correct; without `--parallel`, tasks run sequentially regardless of their dependency graph.

- **Q**: Is the `setupFileDependencies()` method sufficient to replace the index-based sequential step chain?
  - **A**: For cross-flow step dependencies it works. For within-flow steps, if two steps within the same flow have no file relationship, they currently have no task dependency connecting them (other than the index chain). After removing the index chain, they will be free to run in parallel, which is the desired behaviour.

- **Q**: Does `FlowDependencyGraph` already handle artifact-based implicit dependencies between flows?
  - **A**: Yes. `getAllDependencies()` in `FlowDependencyGraph` combines both explicit `dependsOn` and artifact-based implicit dependencies. The current `FlowTasksGenerator` only uses explicit `dependsOn`, missing the implicit artifact-based relationships.

- **Q**: Is `FlowExecutionTask` used anywhere?
  - **A**: No. `FlowTasksGenerator.registerTasks()` never registers a `FlowExecutionTask`. It has a `TODO` comment and a `println`. It is dead code left over from an earlier iteration.

- **Q**: How should artifact identity be fixed so implicit dependencies actually fire for DSL-built flows?
  - **A**: Path-based matching (user decision, 2026-07-15). Key `artifactProducers`/`artifactConsumers` by `path` in `FlowDependencyGraph` instead of full `FlowArtifact` equality. Smallest, most robust change — contained in the domain, no DSL changes, and works for hand-built flows too. Implemented in Step 0.3.

- **Q**: How should false `MissingArtifactProducer` errors be avoided for genuine source inputs?
  - **A**: Mark DSL-created inputs `isSourceFile = true` when no step in any flow produces that path (user decision, 2026-07-15). Source assets stop erroring while real wiring mistakes (a consumed intermediate nothing produces) are still caught as ERRORs. Determined at DSL `build()` time or in the graph; implemented in Step 0.3.

- **Q**: Should flow validation failures at configuration time throw a `GradleException` (fail the build) or just log a warning?
  - **A**: Fail the build (user decision, 2026-07-15). With the Phase 0 fixes eliminating false positives, remaining error-severity issues are genuine (circular dependencies, consumed intermediates nothing produces) and would break the build anyway — failing early at configuration time with a clear message beats a confusing mid-build failure. Warning-severity issues are logged only. Precondition unchanged: fail-on-error ships only after Phase 0.

- **Q**: Should `FlowExecutionTask.kt` be removed in this PR or tracked as a separate cleanup issue?
  - **A**: Remove in this PR (user decision, 2026-07-15). Zero user-facing impact, trivial diff, and it reduces confusion in the exact file area this feature touches. Already specified as Step 1.1.

- **Q**: Should `FlowTasksGenerator` use `FlowService` as its facade or call `FlowDependencyGraph` directly?
  - **A**: Settled by module visibility (red-team finding): `FlowDependencyGraph` is an `internal class` in the `flows` Gradle module (`FlowDependencyGraph.kt:28`), and `FlowTasksGenerator` lives in a different Gradle module (`flows:adapters:in:gradle`). Kotlin `internal` does not cross Gradle module boundaries, so the adapter cannot reference `FlowDependencyGraph` at all. The only viable path is the public `FlowService` (`FlowService.kt:28`), which needs a new public method exposing per-flow dependencies (e.g. `getDependenciesOf(flows, flowName): Set<String>`).

### Unresolved Questions
None — all questions answered (see Self-Reflection Questions above).

### Design Decisions
- **Decision**: How to wire flow-level task dependencies when the dependency is implicit (via artifacts) rather than explicit (via `dependsOn`)
  - **Options**:
    - Option A: Build a `FlowDependencyGraph`, call `getParallelExecutionOrder()`, and use the resulting level information to set up `mustRunAfter` constraints between flow aggregation tasks at the same level
    - Option B: Build a `FlowDependencyGraph`, retrieve `getAllDependencies()` per flow, and call `flowTask.dependsOn(depTask)` for each dependency (explicit or implicit)
  - **Recommendation**: Option B. It is more straightforward — `dependsOn` correctly models the execution constraint for both explicit and artifact-based flow dependencies. Option A using `mustRunAfter` is weaker (it only orders, not requires, prior execution) and does not ensure the dependency is actually executed before the consumer.

- **Decision**: Whether to remove the index-based sequential step chain
  - **Options**:
    - Option A: Remove it entirely — rely solely on file-based dependencies for within-flow step ordering
    - Option B: Keep it as a fallback for steps that declare no inputs/outputs
  - **Recommendation**: Option A. Steps that have no declared inputs or outputs have no file-level dependencies and are genuinely independent — they can safely run in parallel. If they do have a dependency, it must be expressed via `inputs`/`outputs`. This is consistent with how Gradle works in general.

## 5. Implementation Plan

### Phase 0: Artifact Identity & Feasibility (make implicit dependencies real)
**Goal**: Prove the two red-team findings with cheap spikes, then fix artifact identity so the domain graph actually computes implicit dependencies for DSL-built flows. Every later phase depends on this.

1. **Step 0.1**: Spike — verify implicit dependencies are broken for DSL-built flows
   - Files: none (throwaway test or scratch verification)
   - Description: Construct two DSL-built flows where flow B consumes a file that flow A produces (no explicit `dependsOn`), call `FlowService.getExecutionPlan()`, and observe the levels. Expected per the red-team finding: A and B land in the same level (no implicit dependency detected) because `FlowArtifact` data-class equality includes the auto-generated `name` (`FlowDsl.kt:173-178`).
   - Testing: The spike's observation is the deliverable; record the result in this plan
   - [x] **Completed 2026-07-15** — Confirmed. Pre-fix, DSL-shaped producer/consumer artifacts (same path, auto-generated names `{step}_output_{n}` vs `{step}_input_{n}`) produced no implicit dependency; validation instead reported false `MissingArtifactProducer` errors, including for a flow's own within-flow intermediates. Encoded as permanent regression tests in `flows/src/test/kotlin/com/github/c64lib/rbt/flows/domain/DslShapedArtifactMatchingTest.kt` (3 of its assertions failed pre-fix).

2. **Step 0.2**: Spike — measure validation false positives against tony's flows
   - Files: none (throwaway)
   - Description: Run `FlowService.validateFlows()` over flows equivalent to tony's six flows and count `MissingArtifactProducer` errors on genuine source inputs. Expected: many false ERRORs, confirming fail-on-error validation is unsafe before the identity fix.
   - Testing: The spike's observation is the deliverable; record the result in this plan
   - [x] **Completed 2026-07-15** — Confirmed. Tony-shaped flows (six independent pipelines, source `.ctm`/`.asm`/`.sng` inputs, within-flow intermediates, and intro's duplicate output path from the `loading`/`loadingPicture` steps) built through the real `FlowDslBuilder` failed validation with error-severity issues pre-fix. Encoded as permanent regression tests in `flows/adapters/in/gradle/src/test/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/FlowDslDependencyTest.kt` (both validation assertions failed pre-fix).

3. **Step 0.3**: Fix artifact identity in the domain
   - Files: `flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/FlowDependencyGraph.kt`, possibly `flows/adapters/in/gradle/src/main/kotlin/.../FlowDsl.kt`
   - Description: Implement the resolution chosen for the artifact-identity open question (leading option: key `artifactProducers`/`artifactConsumers` by `path` rather than full `FlowArtifact` equality, and mark DSL-created inputs as `isSourceFile = true` so unproduced source assets stop erroring). Re-run the Step 0.1/0.2 spikes to confirm implicit deps now fire and false ERRORs are gone.
   - Testing: New/updated `FlowDependencyGraphTest` cases using DSL-shaped artifacts (auto-generated names, path overlap); both spikes green
   - [x] **Completed 2026-07-15** — `FlowDependencyGraph` now keys `artifactProducers`/`artifactConsumers` by `path` (`FlowDependencyGraph.kt`); same-flow duplicate output paths are allowed (tony's intro case) while cross-flow duplicates still throw `FlowValidationException`; `MissingArtifactProducer` is skipped when any consumer artifact for the path is a source file. `FlowDslBuilder.build()` (`FlowDsl.kt`) now marks consumed artifacts whose path no flow produces as `isSourceFile = true`. All 216 tests in `:flows` and `:flows:adapters:in:gradle` pass, including the new DSL-shaped regression tests.

**Phase 0 Deliverable**: `FlowDependencyGraph` computes correct implicit dependencies for flows as the DSL actually builds them; validation produces no false errors on tony-shaped flows

### Phase 1: Cleanup and Preparation (dead code removal + validation hook)
**Goal**: Remove legacy dead code and add early configuration-time validation to catch flow graph errors before tasks execute.

1. **Step 1.1**: Remove `FlowExecutionTask.kt`
   - Files: `flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/FlowExecutionTask.kt` (delete)
   - Description: This file is dead code — it is never instantiated by `FlowTasksGenerator` and contains a `TODO`. Removing it reduces confusion.
   - Testing: Build compiles without errors; no test references this class
   - [x] **Completed 2026-07-15** — File deleted; only historical plan documents referenced it. `:infra:gradle:compileKotlin` and all flows-adapter tests pass.

2. **Step 1.2**: Add flow graph validation in `FlowTasksGenerator.registerTasks()`
   - Files: `flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/FlowTasksGenerator.kt`
   - Description: At the start of `registerTasks()`, call `FlowService.validateFlows(flows)` (the adapter cannot use `FlowDependencyGraph` directly — it is `internal` to the `flows` module). Log warnings for warning-severity issues. Throw a `GradleException` for error-severity issues. **Precondition: Phase 0 is complete** — enabling fail-on-error before the artifact-identity fix would break existing consumer builds with false `MissingArtifactProducer` errors (red-team finding).
   - Testing: Verify that a circular dependency between two flows causes a build failure with a clear error message, and that tony-shaped flows (source-file inputs) validate cleanly
   - [x] **Completed 2026-07-15** — `registerTasks()` now calls `validateFlowGraph()` first: warnings are logged via `project.logger.warn`, error-severity issues throw `GradleException` listing every error; `FlowValidationException` from graph construction (cross-flow duplicate producers) is wrapped in `GradleException` too. Covered by new `FlowTasksGeneratorTest` (ProjectBuilder-based): circular `dependsOn` fails at configuration time with "Circular dependency" in the message, tony-shaped flows register cleanly. Note: `flows/adapters/in/gradle/build.gradle.kts` gained `--add-opens java.base/java.lang=ALL-UNNAMED` for the test JVM — Gradle's `ProjectBuilder` requires it on JDK 16+.

**Phase 1 Deliverable**: Cleaner codebase with validation feedback during configuration; existing behaviour preserved

### Phase 2: Core Change — Parallel Task Dependency Wiring (adapter refactor)
**Goal**: Replace the index-based sequential step chain with file-based dependency wiring, and replace the explicit-only flow dependency wiring with graph-computed dependency wiring.

1. **Step 2.1**: Remove sequential step chaining within flows
   - Files: `flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/FlowTasksGenerator.kt`
   - Description: Remove the `if (index > 0) { stepTask.dependsOn(flowStepTasks[index - 1]) }` block inside `registerTasks()`. Retain the collection of `flowStepTasks` for the flow aggregation task. The flow aggregation task should depend on **all** step tasks in the flow (not just the last one) to ensure all steps have completed before the flow-level task is considered done.
   - Testing: Build a flow with two independent steps; verify both Gradle tasks appear in the task dependency graph without an ordering constraint between them

2. **Step 2.2**: Wire flow-level task dependencies using the dependency graph
   - Files: `flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/FlowTasksGenerator.kt`
   - Description: Replace the current explicit-only flow dependency wiring loop with graph-computed dependencies retrieved through the new public `FlowService` API from Step 3.2 (the adapter cannot touch `FlowDependencyGraph` — `internal` to the `flows` module). For each flow, retrieve all dependencies (explicit and artifact-based, working after Phase 0), then call `flowTask.dependsOn(depTask)` for each dependency flow's aggregation task.
   - Testing: Build two DSL-built flows where one consumes a file the other produces (without explicit `dependsOn`); verify the consuming flow's task depends on the producing flow's task

3. **Step 2.3**: Update flow aggregation task to depend on all step tasks
   - Files: `flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/FlowTasksGenerator.kt`
   - Description: Change the flow aggregation task creation to `t.dependsOn(flowStepTasks)` (all steps) rather than `t.dependsOn(flowStepTasks.last())` (only the last step). This is required after removing the sequential chain — previously the last task transitively depended on all prior tasks; without the chain, the aggregation task must explicitly depend on every step task.
   - Testing: A flow with three steps (A→B→C via files, plus D independent) should have its aggregation task depend on all four step tasks

**Phase 2 Deliverable**: Flows that are independent now execute in parallel when `--parallel` is set; flows with artifact or `dependsOn` dependencies still execute in the correct order

### Phase 3: Testing, Verification, and Documentation
**Goal**: Confirm the parallel execution wiring is correct with tests, and document the behaviour for users.

1. **Step 3.1**: Add unit tests for `FlowTasksGenerator` dependency wiring
   - Files: `flows/adapters/in/gradle/src/test/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/FlowTasksGeneratorTest.kt` (new)
   - Description: Write tests verifying:
     - Two independent flows have no task dependency between their aggregation tasks
     - Two flows with explicit `dependsOn` have the correct task dependency
     - Two flows with artifact-based dependencies have the correct task dependency
     - A flow with two independent steps has no sequential task dependency between the step tasks
     - A flow with two steps where step B's input is step A's output has `stepB.dependsOn(stepA)`
     - The flow aggregation task depends on all step tasks in the flow
   - Testing: `./gradlew :flows:adapters:in:gradle:test`

2. **Step 3.2**: Expose per-flow dependencies through the public `FlowService`
   - Files: `flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/FlowService.kt` (and `FlowDependencyGraph.kt` internally)
   - Description: Add a public `FlowService.getDependenciesOf(flows: Collection<Flow>, flowName: String): Set<String>` (or equivalent) that delegates to `FlowDependencyGraph.getAllDependencies()`. The originally planned `private → internal` visibility change does not work: Kotlin `internal` does not cross Gradle module boundaries, and `FlowDependencyGraph` is itself an `internal` class invisible to `flows:adapters:in:gradle` (red-team finding). The public service API is the only viable access path and keeps the graph encapsulated.
   - Testing: New `FlowService` unit test for the accessor; domain tests still pass; no behaviour change
   - Note: This step is a prerequisite of Step 2.2 and should be implemented before or together with it (kept in Phase 3 numbering for continuity)

3. **Step 3.3**: Update CLAUDE.md or user documentation
   - Files: `CLAUDE.md` (Parallel Execution section)
   - Description: Add a note that within the `flows` section: "Parallel execution of independent flows and steps is automatic when Gradle's `--parallel` flag is enabled. Task dependencies are derived from file input/output relationships and flow `dependsOn` declarations."
   - Testing: Documentation review only

4. **Step 3.4**: End-to-end gate against the tony harness
   - Files: none (verification step)
   - Description: Run the e2e-test skill against tony — first a plain `flows` run (configuration-time regression check: validation must not fail tony's six flows), then a `clean flows --parallel` run — and verify all expected artifacts are produced. Unit tests cannot catch configuration-time breakage of real consumer builds (red-team finding), so this gate is mandatory before merge.
   - Testing: e2e-test report PASSED on both runs, artifacts verified

**Phase 3 Deliverable**: Fully tested parallel execution capability, verified end-to-end against a real consumer project, ready for merge

## 6. Testing Strategy

### Unit Tests
- `FlowTasksGeneratorTest`: Test the task dependency wiring logic in isolation using a mock Gradle `Project`. Verify that:
  - Independent step tasks within the same flow are not wired sequentially
  - Cross-step file-based dependencies are established correctly
  - Flow aggregation tasks depend on all their step tasks
  - Flow-level dependencies from both explicit `dependsOn` and artifact consumption are wired as Gradle task dependencies
  - `FlowValidationException` errors are surfaced as `GradleException` at configuration time

### Integration Tests
- Extend `CharpadIntegrationTest` or create a new integration scenario with two flows (e.g., preprocessing and compilation) where the preprocessing flow produces files consumed by compilation. Verify the task graph has the correct dependency.
- Consider a scenario with two fully independent preprocessing flows to verify they appear in the same execution wave.

### Manual Testing
- Build a project with flows DSL using `./gradlew flows --parallel --dry-run` and inspect the output to confirm independent flows show no ordering constraint
- Build a project with dependent flows and verify the dependent flow's tasks appear after the dependency's tasks in `--dry-run` output
- Run a real build with `--parallel` and verify both independent flows execute concurrently (check build scan or timing output)

## 7. Risks and Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Removing sequential step chain breaks builds where steps had hidden ordering assumptions not expressed via input/output declarations | High | Low | Document that step ordering within a flow is determined solely by input/output relationships; provide clear error messages when outputs are missing |
| Flow aggregation task now depends on all steps instead of last step, changing task graph shape | Low | Low | This is correct behaviour; existing builds unaffected unless they have non-file ordering assumptions |
| `FlowDependencyGraph.getAllDependencies()` visibility change breaks encapsulation | Low | Low | Use `internal` visibility (same module) rather than `public`; or add a named accessor method |
| Artifact-based implicit flow dependencies create unexpected `dependsOn` constraints | Medium | Low | Add integration test to verify the expected constraints; confirm with test that the `FlowDependencyGraph` logic is invoked correctly |
| **Implicit dependencies silently wire nothing** — artifact equality never matches for DSL-built flows, making Step 2.2 a no-op that looks implemented | High | High (without Phase 0) | Phase 0 spikes prove/fix the mechanism before any adapter wiring; unit tests use DSL-shaped artifacts, not hand-built ones |
| **Fail-on-error validation breaks existing consumer builds** via false `MissingArtifactProducer` errors on source inputs | High | High (without Phase 0) | Phase 0 fixes identity + marks DSL inputs as source files; Step 3.4 e2e gate runs tony before merge |
| Existing `FlowDependencyGraphTest` tests expectations change | Low | Medium | Phase 0 changes domain matching semantics — existing graph tests must be reviewed and extended with DSL-shaped artifact cases |

## 8. Documentation Updates

- [ ] Update `CLAUDE.md` to note that parallel execution within flows is automatic via Gradle's `--parallel` flag
- [ ] Add inline Kdoc to `FlowTasksGenerator` explaining the dependency wiring strategy
- [ ] Remove any documentation references to `FlowExecutionTask` if found

## 9. Rollout Plan

1. Remove `FlowExecutionTask` (no user-facing impact — it was never used)
2. Apply `FlowTasksGenerator` changes on the existing `feature/135-pipeline-dsl-parallel-execution` branch
3. Run `./gradlew build` to verify no compilation errors and all existing tests pass
4. Test manually with a sample project using `--parallel` and `--dry-run`
5. Merge to master as part of milestone 1.8.0
6. Rollback strategy: The change is purely in `FlowTasksGenerator` (adapter layer); reverting the two method changes (`registerTasks` index chain removal and flow dependency loop replacement) restores prior sequential behaviour without any domain changes

## 10. Revision History

| Date | Updated By | Changes |
|------|------------|---------|
| 2026-07-15 | AI Agent | Migrated from `.ai/135-…` into `plans/PLAN-0001`; header updated to canonical format (Plan ID, `draft` status). Content unchanged. |
| 2026-07-15 | AI Agent | Incorporated adversarial red-team findings (/challenge): added Phase 0 (artifact-identity fix + feasibility spikes) — `FlowArtifact` equality never matches for DSL-built flows and validation false-errors on source inputs; answered the FlowService-vs-graph question (settled by `internal` module visibility); revised Steps 1.2/2.2/3.2 to route through a new public `FlowService` API and gate fail-on-error validation on Phase 0; added Step 3.4 e2e gate against tony; updated Gap Analysis, Architecture Alignment, and Risks accordingly. Two new unresolved questions added; status remains `draft`. |
| 2026-07-15 | Maciej Małecki / AI Agent | All four unresolved questions answered interactively: (1) artifact identity → path-based matching in FlowDependencyGraph; (2) validation false positives → mark DSL inputs isSourceFile when unproduced; (3) validation failures → fail the build (GradleException) after Phase 0; (4) FlowExecutionTask → remove in this PR. Unresolved Questions now empty — plan eligible for acceptance. |
| 2026-07-15 | Maciej Małecki / AI Agent | Status `draft` → `accepted` (acceptance gate satisfied); plan copied onto GitHub issue #135 per user decision. |
| 2026-07-15 | AI Agent | Phase 0 executed: Steps 0.1/0.2 spikes confirmed both red-team findings (recorded inline); Step 0.3 implemented path-based artifact matching in `FlowDependencyGraph` and source-file marking in `FlowDslBuilder.build()`; new regression tests added; all 216 flows/adapter tests green. Status `accepted` → `in progress`. |
| 2026-07-15 | AI Agent | Phase 1 executed: Step 1.1 removed dead `FlowExecutionTask.kt`; Step 1.2 added configuration-time `validateFlowGraph()` to `FlowTasksGenerator` (warnings logged, errors fail the build via `GradleException`), with new ProjectBuilder-based `FlowTasksGeneratorTest`; test JVM `--add-opens` added for ProjectBuilder on modern JDKs. All 79 adapter tests green. |

---

**Note**: This plan should be reviewed and approved before implementation begins.
