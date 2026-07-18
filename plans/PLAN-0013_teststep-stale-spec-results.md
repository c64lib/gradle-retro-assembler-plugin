# Feature: Fix stale pass/fail results in testStep when derived artifacts already exist

**Plan ID**: PLAN-0013
**Issue**: #175
**Status**: implemented
**Created**: 2026-07-18
**Last Updated**: 2026-07-18

## 1. Feature Description

### Overview
The flows Pipeline DSL `testStep` can report a **stale pass** for a spec whose assertions now
fail, whenever that spec's derived `.prg`/`.vs`/`.specOut` files already exist from a previous run.
This plan makes the `.specOut` result always reflect the current run — either a fresh result or an
explicit failure — by deleting the stale `.specOut` at the start of the run use case (before VICE)
and refusing to parse a missing result file afterwards.

### Requirements
- A `testStep` re-run against pre-existing `.prg`/`.vs`/`.specOut` must reflect the newly assembled
  spec, never a previous run's result.
- If VICE does not (re)produce the `.specOut` for the current run, the step must fail loudly rather
  than parse a leftover file.
- The fix must also protect the legacy `Run64Spec` task path, not only flows, because both share the
  same `Run64SpecTestUseCase`.
- No change to the public DSL surface or spec-authoring conventions.

### Success Criteria
- With stale `.prg`/`.vs`/`.specOut` present, editing a spec to fail an assertion and re-running
  `testStep` reports the failure and fails the build — matching clean-state behaviour.
- A missing `.specOut` after a VICE run raises a clear error instead of a false green.
- Regression coverage exists for both "stale artifacts present" and "result file missing".

## 2. Root Cause Analysis

The `.specOut` result file is **written by the 64spec test program running inside VICE**
(`write_final_results_to_file` / `result_file_name` variables set in `KickAssembleSpecAdapter`),
not by the plugin. The plugin therefore has no direct signal that VICE actually (re)wrote the file.

`Run64SpecTestUseCase.apply` (`testing/64spec/.../Run64SpecTestUseCase.kt:32-39`) unconditionally
runs VICE and then does `File(resultFile(testSource)).readBytes()`. If the current run does not
overwrite the `.specOut` — e.g. the freshly assembled `.prg` fails to autostart, VICE exits early,
or the write races the read — `readBytes()` returns the **previous run's** bytes, which
`parseTestOutput` happily reports as a pass.

The Gradle output-tracking layer is not the culprit: `TestTask.outputFiles` is declared
`@get:OutputFiles` and wired in `FlowTasksGenerator.configureOutputFiles`, and VICE demonstrably
runs on both invocations (the task is not `UP-TO-DATE`-skipped). The defect is purely that the
use case reads back a file it never verified belongs to the current run.

### Current State
- `execute()` in `TestStep` loops specs: `assembleSpec(specFile)` then `runSpec(specFile)`
  (`flows/.../steps/TestStep.kt:60-63`).
- `assembleSpec` → `KickAssembleSpecUseCase` → `KickAssembleSpecAdapter.assemble` runs KickAssembler
  via `project.javaexec`, emitting `.prg` and `.vs`.
- `runSpec` → `Run64SpecTestUseCase.apply` runs VICE, then reads `resultFile(testSource)` (`.specOut`).
- Nothing deletes prior `.prg`/`.vs`/`.specOut`, and nothing checks the `.specOut` is current.

### Desired State
- At the start of the run use case (after the caller has assembled the spec, before VICE), the stale
  `.specOut` is deleted so a leftover result can never be read back.
- After the VICE run, `Run64SpecTestUseCase` verifies the `.specOut` exists before parsing; if it is
  absent, it throws a descriptive error.
- Result: every reported pass/fail corresponds to the current run, or the build fails explicitly.

### Gap Analysis
- At the start of `Run64SpecTestUseCase.apply`, delete the spec's `.specOut` (tolerant of an
  already-absent file) so no stale result survives into the run. `.prg`/`.vs` are **not** deleted
  here — the caller just assembled them and VICE autostarts the `.prg`.
- After the VICE run, add an existence guard on `.specOut` before `readBytes()`.
- Add regression tests (the `testing/64spec` module currently has no `src/test`).

## 3. Relevant Code Parts

### Existing Components
- **Run64SpecTestUseCase**: runs VICE and parses `.specOut`; the single fix site. Gains: (1) delete
  the spec's stale `.specOut` at the start of `apply`, before the VICE run; (2) a `.specOut`
  existence guard after the run, before `readBytes()`.
  - Location: `testing/64spec/src/main/kotlin/.../usecase/Run64SpecTestUseCase.kt:31-39`
  - Integration Point: shared by flows `testStep` and the legacy `Run64Spec` task, so the invariant
    holds for both.
- **functions.kt (64spec)**: `prgFile`, `resultFile`, `viceSymbolFile` derive artifact paths from the
  spec source — the canonical set of files to delete/guard.
  - Location: `testing/64spec/src/main/kotlin/.../usecase/functions.kt:29-33`
- **KickAssembleSpecUseCase**: assembles a spec into `.prg`/`.vs`. **Unchanged** — re-assembly still
  overwrites those files; deletion is handled run-side.
  - Location: `compilers/kickass/src/main/kotlin/.../usecase/KickAssembleSpecUseCase.kt:30-41`
- **Spec64TestPortAdapter**: bridges flows `TestPort` to the two use cases; unchanged in behaviour,
  it simply benefits from the hardened run use case.
  - Location: `flows/adapters/in/gradle/.../port/Spec64TestPortAdapter.kt:48-59`
- **TestStep**: flows domain step running the assemble→run loop; unchanged.
  - Location: `flows/src/main/kotlin/.../steps/TestStep.kt:52-75`
- **TestStepTest**: existing flows-domain test using a `RecordingTestPort`; template for test style.
  - Location: `flows/src/test/kotlin/.../steps/TestStepTest.kt`

### Architecture Alignment
- **Domain**: `testing/64spec` only — the run use case owns the whole `.specOut` lifecycle.
- **Use Cases**: `Run64SpecTestUseCase.apply` gains, at its start, deletion of the spec's stale
  `.specOut`, then (after the VICE run) a `.specOut` existence guard before parsing.
  Keeps the single-`apply` shape. `KickAssembleSpecUseCase` is **not** modified.
- **Ports**: no new port. Deletion uses `java.io.File` directly inside the use case, consistent with
  the module already deriving paths via pure `functions.kt` helpers; no Gradle types leak in.
- **Adapters**: none changed. `KickAssembleSpecAdapter` still owns the KickAssembler invocation and
  overwrites `.prg`/`.vs` on re-assembly exactly as before.

### Dependencies
- None new. Deletion and existence checks use `java.io.File`, already used throughout these use cases.

## 4. Questions and Clarifications

### Self-Reflection Questions
- **Q**: Why is the task not simply `UP-TO-DATE`-skipped, given outputs are declared?
  - **A**: The edited `.spec.asm` is a declared input, so Gradle re-executes; VICE runs both times.
    The bug is the use case reading a leftover `.specOut`, independent of Gradle up-to-date checks.
- **Q**: Where should the fix live so it is reusable and unit-testable?
  - **A**: In `Run64SpecTestUseCase` (the 64spec run use case). It is pure (only `java.io.File`),
    unit-testable without Gradle, and shared by both flows and the legacy `Run64Spec` task — so the
    invariant is enforced everywhere. Deletion and the existence guard land in the same use case.
- **Q**: Should `.specOut` deletion live in the assemble use case or the run use case?
  - **A**: The **run** use case, just before invoking VICE. This keeps the result file's entire
    lifecycle — delete stale, run VICE (which writes it), guard existence, read — inside the single
    use case that owns it, rather than splitting deletion (assemble) from the read (run).
- **Q**: Which files are deleted before the run — all three derived artifacts?
  - **A**: Only `.specOut`. In `TestStep.execute` the caller runs `assembleSpec(spec)` immediately
    before `runSpec(spec)` (`TestStep.kt:60-63`), so `.prg`/`.vs` are freshly produced and VICE is
    about to autostart the `.prg`; deleting them run-side would destroy the binary under test.
    `.specOut` is the one file VICE writes *during* the run, so clearing only it is both sufficient
    (it is the file read back) and safe.
- **Q**: Does deleting artifacts before every run hurt incremental builds?
  - **A**: Minimal impact. The task only executes when Gradle decides inputs changed; when it does
    execute it must regenerate anyway. Deletion just guarantees the regeneration is observed.
- **Q**: Does `KickAssembleSpecUseCase` need to change?
  - **A**: No. With deletion moved run-side, the assemble use case is untouched; the whole fix is
    contained in `testing/64spec`, and re-assembly still overwrites `.prg`/`.vs` as before.
- **Q**: Does the `testing/64spec` module have existing tests?
  - **A**: No — only `src/main`. The `rbt.domain` plugin provides Kotest; this plan adds `src/test`.

### Unresolved Questions
_None — all resolved._

### Design Decisions
- **Decision**: Fix approach for stale results.
  - **Options**: (A) Delete derived artifacts before each run + guard result exists; (B) freshness
    check via mtime only; (C) both delete and mtime-verify.
  - **Recommendation**: **(A)** — chosen. Deletion is deterministic and immune to filesystem mtime
    resolution issues; the post-run existence guard converts any silent VICE failure into a loud
    build failure. Simpler and more robust than mtime comparison.
- **Decision**: Fix location.
  - **Options**: (A) in the 64spec/kickass use cases; (B) in the flows `Spec64TestPortAdapter`;
    (C) in `TestStep.execute`.
  - **Recommendation**: **(A)** — chosen, and further narrowed to **`Run64SpecTestUseCase` alone**:
    both the delete-before-run and the existence guard live there. The run use case is pure and
    unit-testable and the fix protects the legacy `Run64Spec` task path too. `KickAssembleSpecUseCase`
    is not modified.
- **Decision**: Which use case deletes the stale `.specOut`, and which files.
  - **Options**: (A) assemble use case (all cleanup in one place); (B) run use case, just before VICE.
  - **Recommendation**: **(B)** — chosen. Keeps the result file's full lifecycle (delete → run →
    guard → read) inside the one use case that consumes it; the assemble use case stays untouched.
    Only `.specOut` is deleted (not `.prg`/`.vs`): the caller assembles right before running, so the
    binary must survive into the VICE autostart.

## 5. Implementation Plan

### Phase 1: Harden Run64SpecTestUseCase (core fix) — ✅ completed

**Goal**: Guarantee every `.specOut` read reflects the current run, or fails loudly.

1. - [x] **Step 1.1**: Delete the stale `.specOut` at the start of the run use case, before VICE.
   - Files: `testing/64spec/src/main/kotlin/.../usecase/Run64SpecTestUseCase.kt`
   - Description: At the top of `apply`, before the `runTestOnViceUseCase.apply(...)` call, delete the
     spec's `.specOut` if present, using the `resultFile` helper. Deletion must be tolerant of an
     already-absent file. **Only `.specOut` is deleted run-side** — `.prg`/`.vs` were just written by
     the caller's preceding `assembleSpec(spec)` (see `TestStep.execute` at `TestStep.kt:60-63`) and
     VICE is about to autostart the `.prg`, so deleting them here would destroy the binary under test.
     `.specOut` is the file VICE itself writes during the run, so clearing it first guarantees any
     result read afterwards belongs to this run.
   - Testing: unit test asserting `.specOut` is removed before the VICE port is invoked, and that
     `.prg`/`.vs` are left intact.
2. - [x] **Step 1.2**: Guard the result-file read after the run.
   - Files: `testing/64spec/src/main/kotlin/.../usecase/Run64SpecTestUseCase.kt`
   - Description: After the VICE run and before `readBytes()`, check `resultFile.exists()`; if it does
     not, throw a descriptive exception naming the spec and the expected `.specOut` path, so a run
     that failed to produce a result becomes a build failure instead of a stale/blank pass.
   - Testing: unit test — missing `.specOut` → exception; present `.specOut` → parsed result.

**Phase 1 Deliverable**: The core correctness fix, contained in one module and mergeable on its own —
stale reads are eliminated and missing results fail the build, for both flows and the legacy task.

### Phase 2: Regression tests — ✅ completed

**Goal**: Lock in the fixed behaviour with automated coverage.

1. - [x] **Step 2.1**: Add `src/test` to the `testing/64spec` module for `Run64SpecTestUseCase`.
   - Files: `testing/64spec/src/test/kotlin/.../usecase/Run64SpecTestUseCaseTest.kt`
     (and `parseTestOutput`/`fromPetscii` coverage if convenient)
   - Description: Fake `RunTestOnVicePort`. Cover the full lifecycle in one place:
     (a) fake writes a fresh `.specOut` → correct `TestResult`;
     (b) a **stale** `.specOut` pre-exists and the fake does NOT rewrite it → the pre-run deletion +
     existence guard turn this into a thrown exception, never the stale pass (the exact #175 failure
     mode);
     (c) a fake that writes a *different* `.specOut` → the new content, not the old, is parsed;
     (d) assert `.specOut` is deleted before the fake port runs, and that pre-existing `.prg`/`.vs`
     are left intact (never deleted run-side).
   - Testing: `./gradlew :testing:64spec:test`.

**Phase 2 Deliverable**: Regression suite covering stale-artifact and missing-result scenarios,
entirely within `testing/64spec`.

### Phase 3: Verification and docs — ✅ completed

**Goal**: Confirm the real-world scenario and record the invariant.

1. - [x] **Step 3.1**: E2E verification against a real spec project.
   - Files: n/a (uses `c64lib/common` or the `e2e-test` harness)
   - Description: Reproduce the issue's steps — run `flowVerificationStepSpecs`, break an assertion
     **without** deleting artifacts, re-run, and confirm the build now fails with the corrected count.
   - Testing: manual/e2e per the reproduction in issue #175.
2. - [x] **Step 3.2**: Document the invariant.
   - Files: `CLAUDE.md` (Flows Subdomain Patterns or a 64spec note), inline Kdoc on the run use case.
   - Description: State that the stale `.specOut` is deleted before each run and a missing `.specOut`
     afterwards is a hard failure, so spec results never go stale.
   - Testing: doc review.

**Phase 3 Deliverable**: E2E-confirmed fix plus documentation of the guarantee.

## 6. Testing Strategy

### Unit Tests
- `Run64SpecTestUseCase` (bootstraps a new `src/test` in `testing/64spec`): deletes the stale
  `.specOut` (and leaves `.prg`/`.vs` intact) before invoking the VICE port; missing `.specOut` after
  the run throws; present `.specOut` parses; a fake that rewrites `.specOut` yields the new content,
  not stale.

### Integration Tests
- Optional flows-level test via `Spec64TestPortAdapter` with fakes, asserting the hardened run use
  case composes correctly through the adapter. The existing `TestStepTest` `RecordingTestPort` pattern
  is the model.

### Manual Testing
- The issue #175 reproduction against `c64lib/common`: pre-existing artifacts + broken assertion +
  re-run must fail with the corrected `(n/m)` count, matching clean-state behaviour.

## 7. Risks and Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Run-side deletion accidentally removes `.prg`/`.vs`, destroying the binary VICE autostarts | High | Low | Design deletes **only `.specOut`** run-side; `.prg`/`.vs` are produced by the caller's preceding `assembleSpec` and left untouched. Unit-test asserts `.prg`/`.vs` survive. |
| Path derivation for the deleted `.specOut` diverges from what VICE writes (wrong file deleted/kept) | Medium | Low | Reuse the same `resultFile` helper VICE's result path is derived from; unit-test the deleted set. |
| Legacy `Run64Spec` task behaviour changes unexpectedly | Low | Low | Change is additive (delete-then-run, guard-before-read); covered by unit tests; e2e exercises the path indirectly. |
| Deletion adds slight rebuild cost | Low | High | Only occurs when the task actually executes (inputs changed), which already regenerates artifacts. |

## 8. Documentation Updates

- [x] Update CLAUDE.md (flows/64spec) with the "stale `.specOut` deleted before each run; missing
      `.specOut` is a hard failure" invariant.
- [x] Add inline Kdoc to `Run64SpecTestUseCase.apply` (deletes stale `.specOut` before the run, guards
      result-file existence after).
- [x] No README/arc42 architecture change (no new port/domain/wiring) — confirmed, none needed.

## 9. Rollout Plan

1. Ship Phase 1 (+ Phase 2 tests) on a feature branch, PR into `develop`.
2. Verify via the issue #175 e2e reproduction before merge.
3. Rollback strategy: revert is safe and isolated — the change only affects `Run64SpecTestUseCase`;
   no schema, DSL, or task-graph changes to unwind.

## 10. Revision History

| Date | Updated By | Changes |
|------|------------|---------|
| 2026-07-18 | AI Agent | Initial draft. |
| 2026-07-18 | AI Agent | Resolved fix-location question: whole fix in `Run64SpecTestUseCase` (run-side), `KickAssembleSpecUseCase` untouched. Narrowed run-side deletion to **`.specOut` only** — `.prg`/`.vs` must survive into VICE autostart since the caller assembles immediately before running. Propagated through overview, gap analysis, architecture, phases, tests, and risks. |
| 2026-07-18 | AI Agent | Status → accepted (all Unresolved Questions resolved). |
| 2026-07-18 | AI Agent | Status → implemented. All 5 steps executed and verified (see [EXEC-0013](EXEC-0013_teststep-stale-spec-results.md)): `/challenge` review before execution caught that `Spec64TestPortAdapterTest.kt` would break, fixed inline; e2e-verified the exact #175 repro against `c64lib/common`'s legacy `Run64Spec` task (flows `testStep` shares the same use case); CLAUDE.md and Kdoc updated. |

---

**Note**: This plan should be reviewed and approved before implementation begins.
