# Execution Log: Fix stale pass/fail results in testStep when derived artifacts already exist

**Exec ID**: EXEC-0013
**Plan**: [PLAN-0013](PLAN-0013_teststep-stale-spec-results.md)
**Issue**: #175
**Started**: 2026-07-18
**Last Updated**: 2026-07-18
**State**: completed

## 1. Execution Sessions

### Session 1 — 2026-07-18

- **Scope**: all (Phases 1-3)
- **Mode**: per-phase
- **Outcome**: completed

| Step | Result | Verification | Notes |
|------|--------|--------------|-------|
| 1.1 | completed | `:flows:adapters:in:gradle:test` green (confirms fixed `Spec64TestPortAdapterTest`) | Delete of stale `.specOut` added at top of `Run64SpecTestUseCase.apply`, before the VICE call |
| 1.2 | completed | same run as 1.1 | Existence guard added after VICE run, before `readBytes()`; throws `IllegalStateException` naming spec + expected path |
| 2.1 | completed | `:testing:64spec:test` — 5/5 tests green (Run64SpecTestUseCaseTest.xml: tests=5 failures=0 errors=0) | New `src/test` bootstrapped in `testing/64spec`; covers fresh result, stale-not-rewritten (the #175 failure mode), stale-overwritten-with-different-content, `.prg`/`.vs` left intact, and missing-result exception message |
| 3.1 | completed | e2e against `c64lib/common` (legacy `Run64Spec`/`test` task, not flows — see Deviation #2): baseline `test` run green (32/32) with plugin 1.8.1-SNAPSHOT published to mavenLocal; broke `math-add16.spec.asm`'s first assertion **without** deleting the resulting `.prg`/`.vs`/`.specOut`; re-run correctly reported `(31/32) FAILED` / spec `(18/19)`, matching clean-state behaviour instead of the pre-fix stale pass | Confirms the exact #175 repro is fixed on the legacy path, which shares `Run64SpecTestUseCase` with flows `testStep` |
| 3.2 | completed | `spotlessCheck` green after `spotlessApply`; `:testing:64spec:test` and `:flows:adapters:in:gradle:test` both green | Added the 64spec test-results invariant to `CLAUDE.md` (Flows Subdomain Patterns → Common Patterns) and Kdoc to `Run64SpecTestUseCase` |

## 2. Deviations from Plan

| # | Step | Deviation | Reason | Impact |
|---|------|-----------|--------|--------|
| 1 | 1.1 | `/challenge` (mode A) run against the plan before execution surfaced that `flows/adapters/in/gradle/src/test/kotlin/.../port/Spec64TestPortAdapterTest.kt:99-115` pre-writes `math.spec.specOut` and calls `adapter.runSpec(spec)` with a fake `RunTestOnVicePort` that never writes the file — it currently relies on the pre-written `.specOut` surviving untouched. Once Step 1.1 deletes stale `.specOut` before invoking the VICE port, this existing test would start failing. | Not discovered during planning: the plan's "existing tests" search only checked `testing/64spec`, missing this adapter-level test in `flows/adapters/in/gradle`. | Fixing this test is treated as part of Step 1.1 (per user decision), not a plan rewrite — the fake `RunTestOnVicePort` will be updated to write `.specOut` when invoked, matching real VICE behavior. |
| 2 | 3.1 | Plan's Step 3.1 assumed reproducing #175 via `flowVerificationStepSpecs` (a flows-based task) against `c64lib/common`. The local `common` checkout (branch `master`) uses the plain legacy DSL pinned to plugin `1.6.0`, with no `flows`/`testStep` block, and its `pluginManagement`/`mavenLocal()` override commented out in `settings.gradle`. Verified the **legacy `Run64Spec`/`test` task** path instead (per user decision) — it shares `Run64SpecTestUseCase` with flows `testStep`, so the fix is equally proven. Temporarily edited `common/settings.gradle` (uncommented `pluginManagement { repositories { mavenLocal() ... } }`) and `common/build.gradle` (plugin version `1.6.0` → `1.8.1-SNAPSHOT`) to point at the locally-published SNAPSHOT; reverted both via `git checkout --` immediately after verification, confirmed `common`'s working tree is clean. | `common`'s checked-out state doesn't match the flows-based setup implied by the plan/issue; avoided modifying a separate git repo's committed history. | No lasting change to `common`. Verification evidence lives only in this exec log's Session 1 table (Step 3.1) and this deviation row. |
| 3 | 3.2 | New test file `Run64SpecTestUseCaseTest.kt` initially failed `spotlessKotlinCheck` (multi-line KDoc formatting, lambda block formatting). Fixed via `./gradlew :testing:64spec:spotlessApply`. | Standard ktfmt formatting rules; new file wasn't pre-formatted to match. | None — auto-fixed, re-verified green. |

## 3. Follow-ups

- None yet.
