# Feature: Extract per-domain wiring helpers from `RetroAssemblerPlugin.afterEvaluate`

**Plan ID**: PLAN-0006
**Issue**: #160
**Status**: implemented
**Created**: 2026-07-16
**Last Updated**: 2026-07-16

## 1. Feature Description

### Overview
`RetroAssemblerPlugin.kt` is ~223 lines, with all use-case construction and Gradle task
wiring inline in a single `afterEvaluate` block. This plan extracts the per-domain wiring
into helper functions/methods so the composition root stays readable as more domains are
added, **without introducing a DI framework** — dependency injection remains manual,
explicit, and grep-able (per arc42 [AD-4](../doc/arc42/09_architecture_decisions.md) and
tech-debt item D5 in [§11](../doc/arc42/11_risks_and_technical_debt.md)).

### Requirements
- Break the monolithic `afterEvaluate` block into cohesive per-domain wiring helpers
  (deps, preprocess, compile/sources, spec/test, build, flows).
- Preserve **all** existing behavior: task names, task types, use-case/adapter graphs,
  and — critically — every `dependsOn` relationship and the `defaultTasks` default.
- Keep DI manual and grep-able: plain Kotlin constructor calls, no reflection/annotations,
  no Spring/Dagger/Koin.
- The helpers must live in the `infra/gradle` module (the composition root); no new
  cross-domain module dependencies are introduced.

### Success Criteria
- `RetroAssemblerPlugin.apply()` reads as a short orchestration of named helpers; the body
  of each helper owns one domain's wiring.
- `./gradlew build` passes; `./gradlew :infra:gradle:jar` builds the plugin JAR.
- E2E behavior against the `tony` project is unchanged (same tasks, same execution order).
- No public API / DSL change; no `.gradle.kts` consumer needs to change.

## 2. Root Cause Analysis

This is a maintainability refactor, not a bug fix.

### Current State
`RetroAssemblerPlugin.apply()` (`infra/gradle/src/main/kotlin/com/github/c64lib/gradle/RetroAssemblerPlugin.kt`)
creates three extensions, then in one `project.afterEvaluate { ... }` block (lines 111–221)
constructs every task and its use-case/adapter graph inline. The block mixes six concerns:

1. **deps** — `resolveDevDeps` (TASK_RESOLVE_DEV_DEPENDENCIES), `downloadDependencies` (TASK_DEPENDENCIES)
2. **preprocess** — `charpad`, `spritepad`, `goattracker`, `image`, aggregate `preprocess`
3. **sources/compile** — `settings` (KickAssemblerSettings), `assemble` (TASK_ASM), `clean`
4. **spec/test** — `assembleSpec` (TASK_ASM_SPEC), `runSpec` (TASK_TEST)
5. **build** — `build` (TASK_BUILD)
6. **flows** — `kickAssembleUseCase` + `dasmAssembleUseCase` for steps, `FlowTasksGenerator`,
   and the `assemble.dependsOn(flowsTask)` wiring; plus the `defaultTasks` default.

The concerns are **coupled by shared local variables and `dependsOn` edges** that cross
these groups:
- `preprocess.dependsOn(charpad, spritepad, goattracker, image)`
- `assemble.dependsOn(resolveDevDeps, downloadDependencies, preprocess)`
- `assembleSpec.dependsOn(resolveDevDeps, downloadDependencies)`
- `runSpec.dependsOn(assembleSpec)`
- `build.dependsOn(assemble, runSpec)`
- `assemble.dependsOn(flowsTask)` (added after flow-task generation)
- `settings` is shared by `assemble`, `assembleSpec`, and the flows' `kickAssembleUseCase`.

Any extraction must keep these edges intact — this is the main risk.

### Desired State
`apply()` creates the extensions, then calls a sequence of small, named wiring helpers,
passing forward only the task handles each downstream helper needs (e.g. the dependency
tasks and `preprocess` task feed the sources helper; `assemble`/`runSpec` feed the build
helper). Each helper is a `private` member function on the plugin class (has access to the
`Project`, extensions, and shared `settings`), returning the task handle(s) later steps
depend on. The dependency graph remains literally visible in `apply()` via the arguments
passed between helpers.

### Gap Analysis
- Introduce private helper functions and thread the shared handles (`resolveDevDeps`,
  `downloadDependencies`, `preprocess`, `assemble`, `runSpec`, `settings`) through their
  signatures instead of relying on one flat scope.
- Move each domain's construction into its helper verbatim (no logic change).
- Keep `settings` construction in `apply()` (or its own tiny helper) since three helpers
  consume it.

## 3. Relevant Code Parts

### Existing Components
- **RetroAssemblerPlugin**: the composition root being refactored.
  - Location: `infra/gradle/src/main/kotlin/com/github/c64lib/gradle/RetroAssemblerPlugin.kt`
  - Purpose: single `Plugin<Project>` entry point wiring all domains to Gradle tasks.
  - Integration Point: the entire `afterEvaluate` body (lines 111–221) is the refactor target.
- **Task type classes** (`Assemble`, `Clean`, `ResolveDevDeps`, `AssembleSpec`, `Charpad`,
  `Spritepad`, `Goattracker`, `ProcessImage`, `Test`, `DownloadDependencies`, `Build`,
  `Preprocess`): unchanged; still instantiated via `project.tasks.create(...)`.
- **`FlowTasksGenerator`** (`flows/adapters/in/gradle/FlowTasksGenerator.kt`): unchanged;
  still invoked with the two flow use cases and `flowsExtension.getFlows()`.
- **Task-name constants** (`shared/gradle/Tasks*` — `TASK_*`): unchanged.

### Architecture Alignment
- **Domain**: `infra` (composition root only). No domain/use-case/port/adapter code changes.
- **Use Cases**: none created or modified — only their construction site moves.
- **Ports**: none changed.
- **Adapters**: none changed; adapter constructor calls move into helpers verbatim.
- Honors **AD-4** (manual DI stays manual and grep-able) and does not touch **AD-7**
  (`compileOnly` registrations in `infra/gradle` are already present and remain).

### Dependencies
- No new module dependencies. All types referenced are already imported / already on
  `infra/gradle`'s `compileOnly` classpath.

## 4. Questions and Clarifications

### Self-Reflection Questions
- **Q**: Can the six concerns be split into fully independent helpers?
  - **A**: No — they share `dependsOn` edges and the `settings`/task handles. The clean
    split is a **sequence of helpers that pass handles forward**, not isolated islands.
    `apply()` stays the place where the cross-domain graph is visible.
- **Q**: Should helpers be top-level functions, extension functions on `Project`, or private
  members of the plugin class?
  - **A**: Private member functions of `RetroAssemblerPlugin`. They need `Project`, the two
    extensions, and shared `settings`; members keep the call sites terse and keep the wiring
    co-located with the plugin (no new files/classes needed for a pure readability win).
- **Q**: Does the order of task creation matter?
  - **A**: Behaviorally, `dependsOn` is declared explicitly, so creation order within
    `afterEvaluate` is not load-bearing — but the plan preserves the current order anyway to
    keep the diff reviewable and avoid surprises.
- **Q**: Is there test coverage protecting this file?
  - **A**: To confirm during execution — search `infra/gradle/src/test` for ProjectBuilder-based
    plugin-application tests. If present they are the safety net (note: ProjectBuilder tests
    in this repo need `--add-opens java.base/java.lang=ALL-UNNAMED`). Either way, a ProjectBuilder
    application test asserting task presence + key `dependsOn` edges is in scope (see §6, Step 1.4).
- **Q**: One helper per domain, or coarser grouping?
  - **A**: One helper per domain (6): deps, preprocess, sources, spec/test, build, flows —
    matches the six comment-delimited groups already in the file and makes the forward-passed
    handles self-documenting. (User-confirmed 2026-07-16.)
- **Q**: Where do the helpers live?
  - **A**: `private fun` members of `RetroAssemblerPlugin` in the same file — smallest change,
    no new type, DI stays visibly manual in `apply()`. (User-confirmed 2026-07-16.)
- **Q**: Is a ProjectBuilder application test in scope?
  - **A**: Yes — add/strengthen it as the regression guard (Step 1.4 is now unconditional).
    (User-confirmed 2026-07-16.)

### Unresolved Questions
*(none — all resolved 2026-07-16)*

### Design Decisions
- **Decision**: Helper granularity.
  - **Chosen**: One helper per domain concern (deps, preprocess, sources, spec/test, build, flows).
  - **Rationale**: Matches the six comment-delimited groups already in the code, keeps each
    helper small, and makes the forward-passed handles self-documenting.
- **Decision**: Helper location/shape.
  - **Chosen**: `private fun` members in `RetroAssemblerPlugin` (same file).
  - **Rationale**: Smallest change, no new type, DI stays visibly manual and grep-able (AD-4).
- **Decision**: Test scope.
  - **Chosen**: Add/strengthen a ProjectBuilder application test asserting tasks + `dependsOn` edges.
  - **Rationale**: The only mechanical guard that a moved `dependsOn` edge wasn't dropped.

## 5. Implementation Plan

### Phase 1: Extract helpers (single mergeable refactor)
**Goal**: Replace the monolithic `afterEvaluate` body with a short orchestration calling
private per-domain wiring helpers, preserving all behavior.

1. **Step 1.1** ✅: Confirm the safety net.
   - Files: `infra/gradle/src/test/kotlin/**`
   - Description: Grep for existing plugin-application tests. If found, run them to establish
     a green baseline. If none assert task graph, note the gap for Step 1.4.
   - Testing: `./gradlew :infra:gradle:test` green before any edit.
   - Result: no `src/test` existed under `infra/gradle` — gap confirmed, addressed in Step 1.4.

2. **Step 1.2** ✅: Introduce private wiring helpers.
   - Files: `infra/gradle/src/main/kotlin/com/github/c64lib/gradle/RetroAssemblerPlugin.kt`
   - Description: Add private member functions, moving each domain's construction verbatim:
     - `wireDependencies(project, extension): DependencyTasks` → returns `resolveDevDeps`,
       `downloadDependencies`.
     - `wirePreprocess(project, preprocessExtension): TaskProvider/Task` → creates the four
       processor tasks + `preprocess`, applies `preprocess.dependsOn(...)`, returns `preprocess`.
     - `kickAssemblerSettings(extension): KickAssemblerSettings` (shared).
     - `wireSources(project, extension, settings, depTasks, preprocess): Assemble` → creates
       `assemble` (+ its `dependsOn`) and `clean`, returns `assemble`.
     - `wireSpecAndTest(project, extension, settings, depTasks): Test` → creates `assembleSpec`,
       `runSpec` (+ their `dependsOn`), returns `runSpec`.
     - `wireBuild(project, assemble, runSpec)` → creates `build` (+ `dependsOn`).
     - `wireFlows(project, extension, flowsExtension, settings, assemble)` → creates the two
       flow use cases, runs `FlowTasksGenerator`, applies `assemble.dependsOn(flowsTask)`.
   - Description (cont.): Use a small private data holder (e.g. `data class DependencyTasks`)
     or a `Pair` for the two dependency task handles — keep it local to the file.
   - Testing: compiles; no `dependsOn` edge dropped (checklist against §2 Current State list).
   - Result: `DependencyTasks` data class + 6 helpers added; compiles clean.

3. **Step 1.3** ✅: Rewrite `apply()` to orchestrate.
   - Files: `RetroAssemblerPlugin.kt`
   - Description: `apply()` = create the 3 extensions, then inside `afterEvaluate`:
     compute `settings`, call helpers in order, thread the returned handles, and finish with
     the `defaultTasks` default. The cross-domain graph stays visible via the arguments.
   - Testing: `./gradlew :infra:gradle:jar` builds; `./gradlew build` passes.
   - Result: `apply()` reduced to a short orchestration; full `./gradlew build` green.

4. **Step 1.4** ✅: Add/strengthen a ProjectBuilder application test.
   - Files: `infra/gradle/src/test/kotlin/com/github/c64lib/gradle/RetroAssemblerPluginTest.kt`
   - Description: Apply the plugin to a `ProjectBuilder` project, trigger evaluation, and
     assert all `TASK_*` tasks exist and that the key edges hold (`assemble` dependsOn
     resolveDevDeps/downloadDependencies/preprocess/flows; `build` dependsOn assemble/runSpec;
     `runSpec` dependsOn assembleSpec; `preprocess` dependsOn the four processors). Run with
     `--add-opens java.base/java.lang=ALL-UNNAMED`.
   - Testing: new test green.
   - Result: `RetroAssemblerPluginTest` added; required creating `infra/gradle/src/test` from
     scratch plus `testImplementation` deps mirroring every `compileOnly` entry (see
     [EXEC-0006](EXEC-0006_extract-plugin-wiring-helpers.md) for details/deviations). Green.

**Phase 1 Deliverable**: A single mergeable commit — `RetroAssemblerPlugin.apply()` refactored
into named per-domain helpers, all behavior preserved, build green. This is the whole feature;
no further phases required.

## 6. Testing Strategy

### Unit Tests
- ProjectBuilder-based plugin-application test (new or strengthened) asserting task presence
  and the `dependsOn` graph — this is the regression guard for the refactor. Requires
  `--add-opens java.base/java.lang=ALL-UNNAMED` (known repo quirk).

### Integration Tests
- `./gradlew build` (all modules) and `./gradlew :infra:gradle:jar` must pass.

### Manual Testing
- E2E against `tony` (`/e2e-test`): publish the plugin to mavenLocal and run `flows`/`build`,
  verifying the same tasks and execution order as before the refactor.

## 7. Risks and Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| A `dependsOn` edge is dropped when moving code between helpers | High | Medium | Explicit checklist of the 7 edges in §2; ProjectBuilder test asserts them; e2e run confirms order |
| `settings` accidentally reconstructed per-helper (behavior parity but subtle) | Low | Low | Construct once in `apply()` and pass by reference |
| Over-engineering into a DI-ish framework, violating AD-4 | Medium | Low | Constrain to plain private functions + constructor calls; reviewer checks grep-ability |
| Hidden ordering dependence in `afterEvaluate` | Low | Low | Preserve current creation order; edges are explicit anyway |

## 8. Documentation Updates

- [ ] Update arc42 §11 item **D5** to mark the debt addressed (link this plan / the PR).
- [ ] No CLAUDE.md change expected (no new pattern); add one line only if a wiring-helper
      convention is worth codifying.
- [ ] Inline: brief Kdoc/comment on each helper naming its domain.
- [ ] No README/DSL doc change (no user-facing change).

## 9. Rollout Plan

1. Ship as one PR into `develop` on `feature/160-extract-plugin-wiring-helpers`; pure
   internal refactor, no version bump, no consumer change.
2. Verify via CI build + the ProjectBuilder application test + one `/e2e-test` run on `tony`.
3. Rollback is trivial (revert the single commit) since no API/DSL surface changed.

## 10. Revision History

| Date | Updated By | Changes |
|------|------------|---------|
| 2026-07-16 | AI Agent | Status: draft → accepted. All Unresolved Questions resolved (helper granularity, helper shape, test scope) per user confirmation. |
| 2026-07-16 | AI Agent | Status: accepted → implemented. All Phase 1 steps (1.1–1.4) executed and verified; `./gradlew build` green. See [EXEC-0006](EXEC-0006_extract-plugin-wiring-helpers.md) for the full session log and deviations. |

---

**Note**: This plan should be reviewed and approved before implementation begins.
