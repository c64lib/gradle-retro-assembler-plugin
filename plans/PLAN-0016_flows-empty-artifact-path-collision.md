# Feature: Fix empty-path flow artifact collision blocking all Gradle tasks

**Plan ID**: PLAN-0016
**Issue**: #181
**Status**: implemented
**Challenge**: revised 2026-07-19
**Created**: 2026-07-19
**Last Updated**: 2026-07-19

## 1. Feature Description

### Overview
Every Gradle task in a project that declares two or more flows containing a
**filter-only Charpad output block** (an `interleaver`/`nybbler` filter with no primary
`output`) fails at Gradle *configuration time* with:

```
Flow validation failed: Artifact path '' is produced by multiple flows: 'intro' and 'game'
```

Because flow validation runs across **all** declared flows during configuration, this aborts
*every* task in the project (`flows`, `flowTitle`, `build`, `asm`, `tasks`, …), not just the
colliding flows. This is a pre-existing bug (confirmed at `develop` HEAD `f30f9b4`, before
#176) that blocks the tony harness project from building at all.

### Requirements
- A filter-only output block (no primary `output`, only `interleaver`/`nybbler` sub-outputs) must
  **not** register an empty-string (`''`) artifact for dependency tracking.
- Two flows each containing such a block must configure and build without a spurious
  "produced by multiple flows" collision on the empty path.
- The filter's real sub-output paths must still be tracked as produced artifacts (no regression in
  dependency detection or incremental-build wiring).
- The fix must be robust at the artifact-registration choke point so that an empty path from **any**
  present or future step type cannot poison flow validation.

### Success Criteria
- The tony project (`C:/Users/maciek/prj/cbm/tony`) runs `./gradlew flows` (and `tasks`) to
  configuration completion without the empty-path collision.
- A new regression test reproduces the two-flow empty-path scenario and passes with the fix.
- `./gradlew :flows:test` and `./gradlew :flows:adapters:in:gradle:test` pass.
- No existing flows test regresses.

## 2. Root Cause Analysis

The empty-path artifact originates from the Charpad output model and is registered without
filtering, then rejected by cross-flow validation.

### Current State
The failure is a three-link chain:

1. **Empty primary output is a legitimate domain state.** `CharpadStepBuilder` intentionally
   supports *filter-only* output blocks — a `charset`/`tiles`/`map` block that specifies only an
   `interleaver`/`nybbler` filter and no primary `output`. The guards admit these:
   `flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/dsl/CharpadStepBuilder.kt:83`
   (and the parallel guards at lines 92, 102, 112, 122, 132, 141, 151, 161, 173) add the output when
   `builder.output.isNotEmpty() || builder.filter != FilterConfig.None`. So `CharsetOutput("")`,
   `TileOutput("")`, `MapOutput("")` with a real filter are valid. Additionally, `meta {}`
   (`CharpadStepBuilder.kt:186`) adds its `MetadataOutput` **unconditionally** (no output guard at
   all), so an empty `meta {}` output is a second way an empty primary output enters the Charpad
   model.

2. **`getAllOutputPaths()` leaks the empty primary output.**
   `flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/config/CharpadOutputs.kt:182`
   builds `primaryOutputs` by mapping every block to its `.output` **unconditionally**
   (lines 183–194), then appends the filter sub-outputs (`rangeFilterOutputs`, `mapFilterOutputs`,
   lines 196–221). A filter-only block therefore contributes `""` to `primaryOutputs` while its real
   files come from the filter outputs. `CharpadStep.outputs` is exactly this list
   (`flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/steps/CharpadStep.kt:48`), so
   `CharpadStep.outputs` contains `""`.

3. **`registerStep` turns every output into an artifact with no empty-filtering.**
   `flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/FlowDsl.kt:279`
   iterates `step.outputs` and creates `FlowArtifact("${stepName}_output_${i}", output)` for each —
   including the `""`. The builder's `build()` then sets `produces = outputs`
   (`FlowDsl.kt:289`).

4. **Validation rejects the shared empty path.**
   `flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/FlowDependencyGraph.kt:49`–`56`
   records the producer of each `artifact.path`; the second flow to declare a `''`-path artifact
   trips `throw FlowValidationException("Artifact path '' is produced by multiple flows: …")`.

In the tony project, both the `intro` flow and the `game` flow contain filter-only Charpad blocks
(e.g. `tiles { interleaver { outputs = listOf(...) } }` in `build.gradle.kts`), so each registers a
`''` produced artifact and they collide.

**Precedent for the fix already exists:** the Gradle output-wiring path already discards empty
paths — `FlowTasksGenerator.kt:325` uses `step.outputs.filter { it.isNotEmpty() }.map { project.file(it) }`.
The bug is that `registerStep`'s artifact registration (and, more fundamentally,
`getAllOutputPaths()`) does not.

### Desired State
Empty output paths never become tracked `FlowArtifact`s. Filter-only Charpad blocks are tracked by
their real filter sub-output paths only; two flows using them configure and build cleanly. The
empty-path guard sits at the registration choke point so no step type can reintroduce the bug.

### Gap Analysis
- Stop empty strings from becoming `FlowArtifact`s in `FlowDsl.registerStep` (choke-point fix —
  covers inputs and outputs for **all** step types).
- Additionally stop `CharpadOutputs.getAllOutputPaths()` from emitting the empty primary output of a
  filter-only block (source fix — keeps `CharpadStep.outputs`, `hasOutputs()`, Gradle output wiring,
  and any other consumer clean, not just artifact registration).
- Add regression coverage at both the unit level (the two config helpers) and the integration level
  (two flows with filter-only blocks pass validation).

## 3. Relevant Code Parts

### Existing Components
- **CharpadOutputs.getAllOutputPaths()**: emits the empty primary `.output` of filter-only blocks.
  - Location: `flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/config/CharpadOutputs.kt:182`
  - Integration Point: feeds `CharpadStep.outputs` (CharpadStep.kt:48) and `hasOutputs()` (line 227).
- **FlowDsl.registerStep()**: registers a `FlowArtifact` per input/output with no empty filtering.
  - Location: `flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/FlowDsl.kt:274`
  - Integration Point: `build()` sets `produces`/`consumes` from these artifacts (FlowDsl.kt:284).
- **FlowDependencyGraph.addFlow()**: throws on cross-flow duplicate produced paths (incl. `''`).
  - Location: `flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/FlowDependencyGraph.kt:49`
- **FlowTasksGenerator**: already filters empty output paths for Gradle wiring — the precedent.
  - Location: `flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/FlowTasksGenerator.kt:325`
- **CharpadStepBuilder**: intentionally allows filter-only output blocks (the legitimate source of
  the empty primary output).
  - Location: `flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/dsl/CharpadStepBuilder.kt:83`
- **ImageStepBuilder / SpritepadOutputs** (scope boundary — not buggy):
  `ImageStepBuilder` guards empty outputs at the builder (`ImageStepBuilder.kt:169,182`), and
  `SpritepadOutputs` has no filter blocks (`SpritepadOutputs.kt:59`). The choke-point fix protects
  them regardless, but neither is a current trigger.

### Architecture Alignment
- **Domain**: `flows` (config + domain step model) and its inbound Gradle adapter (`flows/adapters/in/gradle`).
- **Use Cases**: none changed — this is a data-modelling/registration defect, not a use-case change.
- **Ports**: none.
- **Adapters**: inbound Gradle DSL (`FlowDsl`). No outbound adapter changes.
- Consistent with the existing `FlowTasksGenerator.kt:325` empty-path filtering convention.

### Dependencies
- None new. Test frameworks already present (Kotest/JUnit in the flows modules).

## 4. Questions and Clarifications

### Self-Reflection Questions
- **Q**: Which step type produces the empty path in tony?
  - **A**: Charpad. Both `intro` and `game` use filter-only Charpad blocks
    (`tiles { interleaver { … } }`), each emitting a `''` primary output.
- **Q**: Are Image and Spritepad also affected?
  - **A**: Not currently. `ImageStepBuilder` guards empty outputs at the builder (lines 169, 182);
    `SpritepadStepBuilder` guards `sprites {}` (line 64) and has no filter concept. The choke-point
    fix still protects them defensively.
- **Q**: Which step types can synthesize an empty path today?
  - **A**: Only **Charpad** synthesizes one on its own — via filter-only `charset`/`tiles`/`map`
    (etc.) blocks and via unguarded `meta {}`. Goattracker/command/assemble/dasm/exomizer pass output
    strings through verbatim, so they are only vulnerable if the build script literally writes
    `to("")`; they never synthesize an empty path. Image, Spritepad, and Test cannot produce one.
    The `registerStep` choke-point fix covers all of these uniformly (inputs and outputs).
- **Q**: Should the fix be at validation, registration, or the source model?
  - **A**: Both registration (choke point — defends all step types) and the Charpad source model
    (keeps `CharpadStep.outputs`/`hasOutputs()`/Gradle wiring correct). Not at
    `FlowDependencyGraph` validation — an empty produced path is meaningless and should never reach
    validation, so filtering there would only mask the defect.
- **Q**: Could filtering empty outputs hide a genuinely under-configured step (a real block that
  forgot its output)?
  - **A**: No. A block with neither a primary `output` nor a filter is already dropped by the
    builder guards, so an empty primary output only ever coexists with a real filter that supplies
    the actual paths. `hasOutputs()` remains true via the filter sub-outputs.

### Unresolved Questions
{none}

### Design Decisions
- **Decision**: Where to filter empty paths.
  - **Options**: (A) `FlowDsl.registerStep` only; (B) `CharpadOutputs.getAllOutputPaths` only;
    (C) both.
  - **Recommendation**: **C**. `registerStep` is the choke point that fixes the reported crash for
    every step type; fixing `getAllOutputPaths` additionally keeps the domain `CharpadStep.outputs`
    list clean for all its other consumers (Gradle output wiring, logging, incremental build).
    Belt-and-suspenders, each justified independently.

### Adversarial Challenge
- **Status**: revised 2026-07-19 (mode A, run at acceptance)
- **Findings**: Verdict was *sound with caveats* — diagnosis and fix correct; caveats about test
  fidelity and one unverified regression surface. Addressed:
  - **Test could pass vacuously (medium).** Original Step 1.3 could hand-build `Flow.produces` and
    bypass `registerStep`, the exact site the empty path is born. → Step 1.3 rewritten to drive the
    real DSL builder → `build()` → `FlowDependencyGraph` path, and to fail on `develop` HEAD first.
  - **Over-filtering could silently drop real outputs (gap).** → Steps 1.1 and 1.3 now assert the
    filter sub-outputs *survive*, not just that `''` is gone.
  - **Positional-coupling regression risk (medium).** → Verified: `CharpadCommand.getAllOutputFiles()`
    (CharpadCommand.kt:44) maps over `getAllOutputPaths()` with no index zip; filtering is safe and
    removes a latent bogus "project-root" output. Risk downgraded to resolved.
  - **`isNotBlank()` vs `isNotEmpty()` inconsistency (low).** → Standardised on `isNotEmpty()` to
    match the `FlowTasksGenerator.kt:325` precedent.
  - **Which list to filter (hidden assumption).** → Step 1.1 pins the filter to the `primaryOutputs`
    sub-list only, preserving `hasOutputs()`.

## 5. Implementation Plan

### Phase 1: Source fix + regression tests (mergeable fix)
**Goal**: Empty output paths never enter `CharpadStep.outputs` or the flow artifact registry;
regression tests lock the behaviour in.

1. **Step 1.1** — [x] done: Filter empty primary outputs in `CharpadOutputs.getAllOutputPaths()`.
   - Files: `flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/config/CharpadOutputs.kt`
   - Description: Apply `.filter { it.isNotEmpty() }` to the **`primaryOutputs` sub-list only**
     (CharpadOutputs.kt:183–194), leaving `rangeFilterOutputs`/`mapFilterOutputs` untouched. Use
     `isNotEmpty()` (not `isNotBlank()`) to match the existing repo precedent at
     `FlowTasksGenerator.kt:325`. Because the filter sub-outputs are unaffected, `hasOutputs()`
     stays true for filter-only blocks. (Confirmed no positional coupling: the only consumer,
     `CharpadCommand.getAllOutputFiles()` at CharpadCommand.kt:44, maps over the list independently
     with no index zip — filtering the empty entry also removes a latent bogus "project-root" output
     file, a strict improvement.)
   - Testing: Unit test — a `CharpadOutputs` with a filter-only `tiles`/`map` block returns only the
     filter sub-output paths, no `""`; **assert the real filter sub-outputs are still present**; and
     `hasOutputs()` stays true. Add a case for an empty `meta {}` output (unguarded builder path).

2. **Step 1.2** — [x] done: Filter empty paths at the artifact-registration choke point in `FlowDsl.registerStep()`.
   - Files: `flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/FlowDsl.kt`
   - Description: In `registerStep`, skip blank entries when building input/output `FlowArtifact`s
     (guard the `step.inputs`/`step.outputs` iterations at lines 276–281 with `isNotEmpty()`).
     Defensive against any step type, mirroring `FlowTasksGenerator.kt:325`.
   - Testing: Unit test — a step whose `outputs` (and `inputs`) contain `""` registers no empty-path
     artifact.

3. **Step 1.3** — [x] done: Integration regression test for the two-flow collision — **through the real DSL path**.
   (Implemented as `FilterOnlyEmptyPathCollisionTest`; verified failing on HEAD without the fix, green with it. Drives `FlowDslBuilder` per exec-log deviation #1.)
   - Files: new test under
     `flows/adapters/in/gradle/src/test/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/`
     (near `CharpadIntegrationTest.kt`)
   - Description: Build two flows via the **DSL builder** — each containing a `charpadStep` with a
     filter-only block (e.g. `tiles { interleaver { outputs = listOf(...) } }`, no primary `output`)
     — call `build()` so `registerStep` runs, add both flows to a `FlowDependencyGraph`, and assert
     `addFlow`/`validate()` do **not** throw the empty-path `FlowValidationException`. Additionally
     assert the flows' `produces` contain the real filter sub-output paths and **no empty-path
     artifact**. Do NOT hand-build `Flow.produces` — the test must exercise the DSL→build→graph path
     where the empty path is actually born, or it can pass vacuously. This is the direct #181
     reproduction: it must fail on `develop` HEAD and pass after Steps 1.1–1.2.
   - Testing: Run on `develop` HEAD first to confirm it reproduces, then confirm green after the fix.

**Phase 1 Deliverable**: The empty-path collision is fixed and covered by unit + integration tests;
mergeable on its own.

### Phase 2: End-to-end verification against tony
**Goal**: Confirm the real-world harness that surfaced the bug now configures and builds.

1. **Step 2.1** — [x] done: e2e-test the locally built plugin against tony.
   - Files: none (verification only) — use the `e2e-test` skill.
   - Result: tony `./gradlew tasks` and `./gradlew flows` both BUILD SUCCESSFUL; no "produced by multiple flows" error; 68 flow steps ran; representative artifacts present and non-empty.
   - Description: Publish the plugin to mavenLocal as the snapshot and run tony's `flows` (and
     `tasks`) task; confirm configuration completes with no empty-path `FlowValidationException` and
     expected artifacts are produced.
   - Testing: `./gradlew tasks` / `./gradlew flows` on tony reach configuration completion.

**Phase 2 Deliverable**: Verified fix against the originating project; ready to ship.

## 6. Testing Strategy

### Unit Tests
- `CharpadOutputs.getAllOutputPaths()`: filter-only block yields only real sub-output paths, no `""`;
  `hasOutputs()` remains true.
- `FlowDsl.registerStep`: a step output list containing `""` produces no empty-path `FlowArtifact`.

### Integration Tests
- Two flows with filter-only Charpad blocks pass `FlowDependencyGraph.addFlow`/`validate()` without
  the empty-path collision (direct #181 reproduction).

### Manual Testing
- Run `e2e-test` (or manually `./gradlew flows` in tony) and confirm the build configures past the
  previously fatal validation error.

## 7. Risks and Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Filtering empty outputs hides a genuinely mis-configured block | Medium | Low | Builder guards already drop no-output/no-filter blocks; an empty primary output only appears alongside a real filter. `hasOutputs()` still validates configuration presence. |
| Downstream consumer relies on positional index of `getAllOutputPaths()` including the empty slot | Low | Low | **Resolved** — verified `CharpadCommand.getAllOutputFiles()` (CharpadCommand.kt:44) maps over the list independently with no index zip; filtering the empty entry removes a latent bogus "project-root" output file. |
| Fixing only one of the two sites leaves latent bug | Medium | Low | Apply both fixes (Steps 1.1 + 1.2) and cover with integration test at the graph level. |

## 8. Documentation Updates

- [ ] No README change expected (behavioural bugfix, no DSL surface change).
- [ ] Consider a brief note in `doc/arc42/08_crosscutting_concepts.md` if the empty-path filtering
      becomes a documented cross-cutting rule (optional).
- [ ] Add concise inline comments at both fix sites explaining why empty paths are dropped
      (filter-only outputs), referencing the `FlowTasksGenerator.kt:325` precedent.
- [ ] Update CLAUDE.md only if a new pattern warrants it (likely not).

## 9. Rollout Plan

1. Ship in Phase 1 as a self-contained bugfix on a feature branch → PR into `develop`.
2. Verify against tony (Phase 2) before merge; watch for any flows integration-test regressions.
3. Rollback strategy: revert the two small edits — they are isolated to `CharpadOutputs.kt` and
   `FlowDsl.kt` with no API or schema change.

## 10. Revision History

| Date | Updated By | Changes |
|------|------------|---------|
| 2026-07-19 | AI Agent | Created plan; root cause traced to `CharpadOutputs.getAllOutputPaths` + `FlowDsl.registerStep`. |
| 2026-07-19 | AI Agent | Accepted. Ran adversarial challenge (mode A) → revised: sharpened Step 1.3 to drive the real DSL path and assert sub-outputs survive; standardised on `isNotEmpty()`; verified `CharpadCommand` non-positional use (risk resolved); pinned Step 1.1 to the `primaryOutputs` sub-list. |
| 2026-07-19 | AI Agent | Executed all steps (autonomous). Applied both source fixes + `FilterOnlyEmptyPathCollisionTest`; verified test fails on HEAD without fix and passes with it; flows unit tests + Spotless green; tony e2e (`tasks`, `flows`) BUILD SUCCESSFUL with artifacts present. Status → implemented. See [EXEC-0016](EXEC-0016_flows-empty-artifact-path-collision.md) (deviation: test drives `FlowDslBuilder`). |

---

**Note**: This plan should be reviewed and approved before implementation begins.
