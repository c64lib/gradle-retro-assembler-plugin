# Execution Log: Fix useFrom()/useTo() in Groovy nested commandStep closures

**Exec ID**: EXEC-0015
**Plan**: [PLAN-0015](PLAN-0015_flows-groovy-usefrom-useto-nested-closure.md)
**Issue**: #182
**Started**: 2026-07-19
**Last Updated**: 2026-07-19
**State**: completed

## 1. Execution Sessions

### Session 1 — 2026-07-19

- **Scope**: all (Phase 0 through Phase 3; steps 0.1–3.4)
- **Mode**: per-phase
- **Outcome**: completed — 8 steps done, 1 skipped (2.2, moot). Fix verified by unit + in-JVM Groovy repro + real-Gradle e2e; coverage + spotless green.

| Step | Result | Verification | Notes |
|------|--------|--------------|-------|
| 0.1 | completed | Spike test (`FlowsGroovyUseFromReproTest`) in `infra/gradle` using `GroovyShell` + `ProjectBuilder` reproduces the exact issue-182 error. **No Gradle TestKit needed** — a lightweight in-JVM Groovy-eval vehicle works. | See Deviation #1 (vehicle) |
| 0.2 | completed | Boundary/mechanism pinned via probes. **Root cause is NOT closure delegate/owner binding.** `delegate.useFrom()` throws `MissingMethodException: CommandStepBuilder.useFrom() applicable for argument types: ()` — Groovy can't match the zero-arg call because `useFrom(index: Int = 0)` is a **Kotlin default-parameter** method (compiles to `useFrom(int)` + synthetic mask, no true no-arg overload). Falls through to `flows` owner → reported error. Same for `useTo`. | See Deviation #2 (root cause) |
| 1.1 | completed | Keeper reproduction test `FlowsGroovyUseFromUseToTest` (Kotest, `infra/gradle`) with `useFrom()`/`useTo()` cases. Confirmed **RED** before fix: both fail with `CustomMessageMissingMethodException` (the issue-182 error). Scratch spike `FlowsGroovyUseFromReproTest` removed. | red before fix, as required |
| 2.1 | completed | Added zero-arg `useFrom()` / `useTo()` overloads to `CommandStepBuilder` (delegating to `useFrom(0)`/`useTo(0)`). Reproduction test now **GREEN**; existing `CommandStepBuilderTest` + `FlowDslGroovyOverloadTest` still green (no regression). | Fix in `CommandStepBuilder.kt`, not `FlowDsl.kt` (see Deviation #2) |
| 2.2 | skipped | Outer-scope-access guard is **moot**: the fix does not touch closure delegate/owner binding (`bindClosure` unchanged), so there is no `DELEGATE_ONLY`-style risk to outer Groovy variables. | Superseded by the corrected root cause (Deviation #2) |
| 3.1 | completed | Broadened `FlowsGroovyUseFromUseToTest`: added indexed `useFrom(1)`/`useTo(1)` (multi in/out) and the two-statement-workaround guard. All 4 cases green. | — |
| 3.2 | completed | `./gradlew test verifyCodeCoverage` → BUILD SUCCESSFUL, coverage threshold **passed**. The in-JVM GroovyShell reproduction test covers the new `useFrom()`/`useTo()` overloads, so the anticipated TestKit/coverage gap did not materialise. | coverage risk resolved |
| 3.3 | completed | E2E via real Gradle process: published fixed plugin to mavenLocal (1.8.1-SNAPSHOT), ran a throwaway Groovy consumer using `param(useFrom())` + `option("-o", useTo())`. Result: `E2E-PARAMS: [in.bin, -o, out.bin]`, `E2E-VERIFY: OK`, BUILD SUCCESSFUL. | See Deviation #4 (consumer target) |
| 3.4 | completed | `doc/index.adoc`: added a NOTE confirming `useFrom()`/`useTo()` work as bare arguments in both Groovy and Kotlin DSLs (no workaround needed). `CLAUDE.md`: added a maintainer note explaining the zero-arg overloads exist for Groovy (Kotlin default params don't emit a no-arg JVM signature) — "do not remove". No stale Kotlin-only caveat existed to remove. | — |

## 2. Deviations from Plan

| # | Step | Deviation | Reason | Impact |
|---|------|-----------|--------|--------|
| 1 | 0.1 | Reproduction vehicle is **`GroovyShell` + `ProjectBuilder` in `infra/gradle`**, not Gradle TestKit in `:flows:adapters:in:gradle` as the plan specified. | `:flows:adapters:in:gradle` does not apply `java-gradle-plugin` (no `withPluginClasspath`); `infra/gradle` is the actual plugin and already has a ProjectBuilder test + `--add-opens`. A `GroovyShell` eval compiles the nested closures for real (faithful) yet runs in-JVM (counts for JaCoCo — resolves the coverage risk) and needs zero TestKit setup. | Simpler, faster, higher-coverage than planned. TestKit-standup risk (rated High) is avoided entirely. Reproduction test lands in `infra/gradle`. |
| 2 | 0.2 | **Root cause differs from the plan's hypothesis.** Not a `bindClosure` delegate/owner-resolution quirk and not argument-expression-specific (a bare `useFrom()` *statement* fails too). Actual cause: `useFrom(index: Int = 0)` / `useTo(index: Int = 0)` are **Kotlin default-parameter** methods with no true zero-arg JVM overload, so Groovy's `useFrom()` (no args) matches nothing on the `CommandStepBuilder` delegate and falls through to the `flows` owner. | Confirmed empirically: `delegate.useFrom()` → `MissingMethodException: CommandStepBuilder.useFrom() applicable for argument types: ()`. Kotlin default params don't produce a callable no-arg signature for Groovy. | **Fix site moves from `FlowDsl.bindClosure` to `CommandStepBuilder`**: add explicit zero-arg `useFrom()` / `useTo()` overloads (the "fix scope = harden bindClosure" decision no longer applies). Fix is smaller and lower-risk than planned; the `DELEGATE_ONLY`/owner-reset options are moot. |
| 3 | 2.2 | Step 2.2 (outer-scope-access TestKit guard) **skipped** — no longer applicable. | The fix doesn't touch closure binding, so there's no `DELEGATE_ONLY` regression to guard against. | One planned step drops out; net scope smaller. |
| 4 | 3.3 | E2E used a **throwaway Groovy consumer in the scratchpad** (published plugin → real `gradlew` process), not `../common`. | `../common` (and `tony`) do not currently use `useFrom()`/`useTo()` in a `commandStep`, so neither exercises the fix; editing a consumer project is out of scope. The throwaway consumer uses the exact issue-182 shape and is the faithful real-process signal. Needed a `retroProject { dialectVersion = "5.25" }` block (the default `"latest"` fails SemVer parse at configuration — unrelated to #182). | Real-Gradle e2e still achieved; observed a pre-existing unrelated quirk (plugin parses `dialectVersion` even for flows-only builds) — see Follow-ups. |

## 3. Follow-ups

- **Pre-existing quirk (out of scope for #182):** the plugin parses `retroProject.dialectVersion`
  via SemVer during configuration even for **flows-only** builds that never assemble, so a consumer
  using only the flows DSL must still set a valid `dialectVersion` (the default `"latest"` fails with
  "Cannot determine version from \"latest\""). Candidate for a separate issue: defer/skip
  `dialectVersion` parsing when no assembly is configured. Discovered while building the #182 e2e
  consumer.
