# Feature: Fix useFrom()/useTo() in Groovy nested commandStep closures

**Plan ID**: PLAN-0015
**Issue**: #182
**Status**: implemented
**Challenge**: revised 2026-07-19
**Created**: 2026-07-19
**Last Updated**: 2026-07-19

## 1. Feature Description

### Overview
In the Groovy flows DSL, `CommandStepBuilder.useFrom()` / `useTo()` fail when
called as a **bare argument expression** inside a `commandStep { ... }` closure
that is itself nested in a `flow { ... }` closure:

```groovy
flows {
    flow("myFlow") {
        commandStep("my-cmd", "exomizer") {
            from("input.bin")
            to("output.bin")
            param(useFrom())   // MissingMethodException on 'flows' extension
        }
    }
}
```

Groovy resolves `useFrom()` against the **outer** `FlowsExtension`, not the
`CommandStepBuilder` delegate the enclosing `commandStep` closure is bound to —
even though `from(...)`, `to(...)`, and `param("literal")` in the same closure
resolve correctly. The goal is to make `useFrom()`/`useTo()` resolve to the
`CommandStepBuilder` delegate in this nested-closure position, achieving
Kotlin/Groovy DSL parity.

### Requirements
- `param(useFrom())`, `option("-o", useTo())`, and equivalent bare-argument uses of
  `useFrom(idx)` / `useTo(idx)` resolve against the enclosing `CommandStepBuilder`
  inside a Groovy `flow { commandStep { ... } }` nesting.
- The Kotlin DSL behaviour is unchanged (the receiver-lambda overload already
  works; existing tests stay green).
- The fix is validated by a test that reproduces the **doubly-nested Groovy
  closure** argument-expression case — the exact shape the current
  `FlowDslGroovyOverloadTest` does not cover.

### Success Criteria
- A new test reproducing the issue's failing snippet passes.
- `./gradlew :flows:adapters:in:gradle:test` is green.
- E2E: the `../common` Groovy consumer (where the bug was found during PLAN-0011)
  can use `param(useFrom())` without the two-statement workaround.
- CLAUDE.md's `useFrom()`/`useTo()` guidance reflects the confirmed Groovy support
  (or documents any residual limitation).

## 2. Root Cause Analysis

> **Post-implementation note (2026-07-19):** the Phase-0 spike **disproved** the
> hypothesis in this section. The defect is **not** a `bindClosure` delegate/owner
> resolution quirk and is not argument-expression-specific (a bare `useFrom()`
> *statement* fails too). Actual root cause: `useFrom(index: Int = 0)` /
> `useTo(index: Int = 0)` are **Kotlin default-parameter methods** that emit no
> callable no-arg JVM signature, so Groovy's zero-arg `useFrom()` / `useTo()`
> matches nothing on the `CommandStepBuilder` delegate and falls through the owner
> chain to the `flows` extension. Fix: explicit zero-arg overloads on
> `CommandStepBuilder` (not `FlowDsl.bindClosure`). See [EXEC-0015](EXEC-0015_flows-groovy-usefrom-useto-nested-closure.md)
> Deviations #1–#2. The original analysis below is retained as historical record.

### Current State
- `bindClosure` (`flows/adapters/in/gradle/src/main/kotlin/.../FlowDsl.kt:83`) sets
  `closure.delegate = builder` and `closure.resolveStrategy = DELEGATE_FIRST`,
  then `closure.call(builder)`. This is applied uniformly to every Groovy-friendly
  overload, including `commandStep` (`FlowDsl.kt:264`).
- `useFrom()` / `useTo()` are instance methods on `CommandStepBuilder`
  (`CommandStepBuilder.kt:191`, `:235`).
- Direct body statements (`from(...)`, `param("x")`) resolve against the delegate
  and work. The failure is specific to a method call used as a **bare argument
  expression** in a **doubly-nested** closure.

### Suspected root cause (to be confirmed by a spike — see Unresolved Questions)
`bindClosure` sets `delegate`/`resolveStrategy` on the closure object but does
**not** reset its `owner`. When Groovy compiles `param(useFrom())`, the inner
`useFrom()` has no explicit receiver, so it is dispatched via the closure's
owner/delegate chain. For a closure literal lexically nested inside the outer
`flow`/`flows` closures, the `owner` is the enclosing closure/extension. Under
`DELEGATE_FIRST` the *delegate* (`CommandStepBuilder`) should be consulted first —
but the observed `MissingMethodException` on the `flows` extension suggests that
for this call position the delegate we set is not the one Groovy consults, most
likely because:
- the `commandStep` closure's `resolveStrategy`/`delegate` we set via reflection
  is correct for body statements but the *nested* argument-expression dispatch
  walks the lexical `owner` chain up to `FlowsExtension` (the Gradle-created
  extension object) which is `OWNER_FIRST` by default, or
- the closure Gradle actually passes is a curried/rehydrated closure whose
  delegate we set is not the receiver used for unqualified argument calls.

### Desired State
`useFrom()`/`useTo()` used as bare arguments resolve to the current
`CommandStepBuilder` regardless of closure nesting depth.

### Gap Analysis
1. **Reproduce** the exact failure in a JVM test (spike) to confirm which
   owner/delegate is consulted.
2. **Fix** so the delegate is consulted for the nested argument-expression call.
   Candidate approaches (decide after the spike — see Design Decisions):
   - **A. `DELEGATE_ONLY`** on the `commandStep` closure so owner resolution never
     reaches `FlowsExtension`. Lowest-code, but may break legitimate references to
     outer-scope Groovy variables/methods inside `commandStep`.
   - **B. Explicitly reset `closure.owner`** (or rebind) to the `CommandStepBuilder`
     in `bindClosure`.
   - **C. Provide `useFrom`/`useTo` on the resolution path another way** — e.g. a
     `methodMissing`/`propertyMissing` on the builder, or documenting the
     workaround if the quirk proves unfixable without regressions.
3. **Test** the nested case and guard against regression.
4. **Document** the outcome in CLAUDE.md.

## 3. Relevant Code Parts

### Existing Components
- **`FlowDsl.kt` — `bindClosure`** (`flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/FlowDsl.kt:83`)
  - The single Groovy delegate-binding idiom shared by every `*Step` overload.
  - Integration Point: the fix most likely lives here (or in a `commandStep`-
    specific variant), since it owns `delegate`/`resolveStrategy`.
- **`FlowDsl.kt` — `commandStep` Closure overload** (`FlowDsl.kt:264`)
  - Binds a `CommandStepBuilder`. The failing closure.
- **`CommandStepBuilder`** (`flows/adapters/in/gradle/src/main/kotlin/.../dsl/CommandStepBuilder.kt:191,235`)
  - Hosts `useFrom`/`useTo`. May need a resolution-assist hook (approach C).
- **`FlowsExtension`** (`flows/adapters/in/gradle/src/main/kotlin/.../FlowsExtension.kt`)
  - The outer Gradle extension `useFrom()` erroneously resolves against; the top of
    the owner chain.
- **`FlowDslGroovyOverloadTest`** (`flows/adapters/in/gradle/src/test/kotlin/.../FlowDslGroovyOverloadTest.kt`)
  - Covers single-level Closure binding per step, but **not** the doubly-nested
    `flow { commandStep { param(useFrom()) } }` argument-expression case. The test
    gap that let this bug through.
- **`CommandStepBuilderTest`** (`flows/adapters/in/gradle/src/test/kotlin/.../dsl/CommandStepBuilderTest.kt`)
  - Unit-tests `useFrom`/`useTo` directly (not through Groovy closures).

### Architecture Alignment
- **Domain**: `flows` (inbound Gradle adapter — `adapters/in/gradle`). This is
  adapter-level Groovy-interop code; it does not touch domain use cases or ports.
- **Use Cases / Ports**: none changed.
- **Adapters**: the fix is confined to the inbound Gradle DSL adapter.
- No new module, so no `infra/gradle` `compileOnly` change.

### Dependencies
- Groovy (`groovy.lang.Closure`) — already a dependency of this adapter.
- Gradle TestKit (`gradleTestKit()` / `GradleRunner`) for the reproduction test —
  the hand-rolled `TestClosure` shim cannot reproduce the real `FlowsExtension`
  owner chain, so a functional test with a real `build.gradle` is required (see
  Design Decision: Reproduction-test vehicle).

## 4. Questions and Clarifications

### Self-Reflection Questions
- **Q**: Does the existing `FlowDslGroovyOverloadTest` already cover this?
  - **A**: No. It exercises each Closure overload at a single level and uses a
    hand-rolled `TestClosure` whose `doCall` runs `body` against the passed
    delegate. It never nests `commandStep` inside a `flow` closure, and never calls
    `useFrom()`/`useTo()` as a bare argument — exactly the failing shape.
- **Q**: Is Kotlin affected?
  - **A**: No. Kotlin uses the receiver-lambda overloads (`FlowDsl.kt:253`), where
    `useFrom()` is an unambiguous member call on the `CommandStepBuilder` receiver.
- **Q**: Is a workaround available today?
  - **A**: Yes — assign to a local first (`def i = useFrom(); param(i)`), used in
    the PLAN-0011 verification. The fix should remove the need for it.
- **Q**: How should the doubly-nested owner-chain behaviour be reproduced in a
    test, at sufficient fidelity to confirm the real cause?
  - **A**: With a **Gradle TestKit functional test** — a real `build.gradle` in a
    temp project run via `GradleRunner`. Highest fidelity: it reproduces the actual
    `FlowsExtension` owner chain and real Groovy compilation of the nested closures,
    which a hand-rolled shim closure (or a Groovy test source set that doesn't wire
    the real extension as owner) may not. Slower/heavier, but the bug is
    owner-chain-specific, so fidelity wins.
- **Q**: Which fix approach — `DELEGATE_ONLY`, reset `owner`, resolution-assist —
    should be committed to?
  - **A**: **Decide after the spike.** Phase 1 first confirms which object Groovy
    actually consults; only then is the minimal safe fix chosen. The chosen fix
    must not break references to outer Groovy variables inside `commandStep`, which
    rules out `DELEGATE_ONLY` unless the spike proves it safe for these leaf
    closures.
- **Q**: Should the fix be scoped to `commandStep` only, or the shared idiom?
  - **A**: **Harden the shared `bindClosure`** so nested-closure resolution works
    uniformly for every `*Step` overload. The root cause lives in the shared
    binding idiom; fixing it once there gives every current and future builder
    correct behaviour and avoids per-overload divergence.

### Unresolved Questions
(none)

### Design Decisions
- **Decision**: How to make `useFrom()`/`useTo()` resolve to the builder in a
  nested Groovy closure.
  - **Chosen**: Confirm the consulted object via a Phase-1 spike, then apply the
    minimal safe fix in the shared `bindClosure`. Candidate mechanisms remain
    (A) `DELEGATE_ONLY`, (B) reset the closure `owner` to the builder,
    (C) a resolution-assist (`methodMissing`) on the builder, (D) document the
    limitation + workaround if no safe fix exists — the spike picks between them.
  - **Rationale**: The root cause is explicitly unconfirmed; a spike-first approach
    targets the real cause rather than a guess. The fix must preserve
    `DELEGATE_FIRST`-style access to outer Groovy variables, which favours a
    targeted `owner` fix (B) over `DELEGATE_ONLY` (A) unless the spike proves A safe.
- **Decision**: Reproduction-test vehicle.
  - **Chosen**: Gradle TestKit functional test with a real `build.gradle`.
  - **Rationale**: Highest fidelity for an owner-chain-specific defect; a shim
    closure risks a false green.
- **Decision**: Fix scope.
  - **Chosen**: Harden the shared `bindClosure` (all overloads), not just
    `commandStep` — **conditional on Phase 0** confirming the leak is at the
    plugin-owned `flow→commandStep` boundary. If the leak is at the Gradle-owned
    `flows→flow` boundary, the fix site is revisited.
  - **Rationale**: The defect is in the shared idiom; one fix benefits every builder
    and avoids per-overload divergence — provided the leaking boundary is one
    `bindClosure` actually controls.

### Adversarial Challenge
- **Status**: revised 2026-07-19
- **Findings**: Red-teamed at acceptance (mode A). Confirmed the owner-chain
  diagnosis against real code (`RetroAssemblerPlugin.kt:116` registers `flows` as a
  `FlowsExtension`, which `extends FlowDslBuilder`). Three findings folded in:
  (1) **[high]** TestKit is greenfield in this repo (zero existing `GradleRunner`
  usage) — added **Phase 0** to prove TestKit can apply the plugin before any fix
  work, and re-rated the standup risk to High probability. (2) **[medium]** The leak
  may be at the Gradle-owned `flows→flow` boundary, which `bindClosure` cannot fix —
  Phase 0 Step 0.2 now pins the leaking boundary and the fix-scope decision is
  conditional on it. (3) **[medium]** Fidelity/coverage guards — the outer-variable
  guard must be a TestKit test (Step 2.2), a `from()`-before-`useFrom()` ordering
  note was added (Step 1.1), the two-statement workaround is now guarded (Step 3.1),
  and a `verifyCodeCoverage` check was added (Step 3.2). Also fixed a Phase-1/Phase-2
  contradiction: mechanism selection moved wholly into Phase 2.

## 5. Implementation Plan

### Phase 0: TestKit feasibility + pin the leaking boundary (spike)
**Goal**: De-risk the two biggest unknowns before writing reproduction/fix code:
(a) can Gradle TestKit apply *this* plugin at all in `:flows:adapters:in:gradle`
(no TestKit exists anywhere in the repo today — this is greenfield), and (b) which
closure-nesting boundary actually leaks the `useFrom()` resolution.
**Status**: ✅ Completed 2026-07-19. Spike used `GroovyShell`+`ProjectBuilder` in
`infra/gradle` (not TestKit — see EXEC-0015 Deviation #1); root cause pinned to
Kotlin default-param methods (Deviation #2).

1. **[x] Step 0.1**: Prove TestKit can apply the plugin.
   - Files: a throwaway/keeper functional test + temp `build.gradle` in
     `flows/adapters/in/gradle/src/test/...`; add `gradleTestKit()` and configure
     `withPluginClasspath()` (needs `java-gradle-plugin` or a manual
     plugin-under-test-metadata classpath; the plugin's runtime deps — Vavr, PNGJ —
     must resolve at configuration time).
   - Description: Stand up one minimal `GradleRunner` build that applies the plugin
     and asserts the `flows {}` extension is recognised (e.g. an empty
     `flows { }` block configures without error). If TestKit fights the classpath,
     record it and fall back to a Groovy test source set (previously rejected) or a
     TestKit variant that injects the classpath manually — decide here, not mid-fix.
   - Testing: The feasibility build runs green via `GradleRunner`.

2. **[x] Step 0.2**: Pin the leaking boundary.
   - Files: extend the Step 0.1 fixture.
   - Description: Determine whether the `useFrom()` MissingMethodException arises at
     the **Gradle-owned** `flows {}`→`flow {}` boundary (the `flows` extension is
     created by `project.extensions.create("flows", FlowsExtension::class.java)` —
     `RetroAssemblerPlugin.kt:116` — and `FlowsExtension extends FlowDslBuilder`), or
     the **plugin-owned** `flow {}`→`commandStep {}` boundary (bound via
     `bindClosure`, `FlowDsl.kt:83`). Only the latter is fixable in `bindClosure`;
     if the leak is at the Gradle-owned boundary, the fix site changes (revisit
     Design Decision: Fix scope).
   - Testing: A note in the exec log naming the leaking boundary and confirming (or
     redirecting) the fix site.

**Phase 0 Deliverable**: Confirmed TestKit vehicle (or documented fallback) + the
named leaking boundary. This gates Phases 1–2; if the leak is Gradle-owned, the
plan's fix-site decision is revised before proceeding.

### Phase 1: Reproduce + confirm root cause (failing test)
**Goal**: A red test that reproduces the issue and pins down which object Groovy
consults, so the fix targets the real cause rather than the guessed one.
**Status**: ✅ Completed 2026-07-19. Keeper `FlowsGroovyUseFromUseToTest`
(`GroovyShell`+`ProjectBuilder`, `infra/gradle`) confirmed RED before the fix.

1. **[x] Step 1.1**: Add a Gradle TestKit reproduction test for the nested-closure
   argument case.
   - Files: a new functional test (e.g.
     `flows/adapters/in/gradle/src/test/kotlin/.../FlowDslGroovyNestedUseFromFunctionalTest.kt`)
     plus a temp-project `build.gradle` fixture; wire a `GradleRunner`
     (`gradleTestKit()`) test dependency/source set if not already present.
   - Description: Generate a real temp project whose `build.gradle` applies the
     plugin and declares `flows { flow("f") { commandStep("c","tool") { from("i.bin");
     to("o.bin"); param(useFrom()) } } }`, then run a flows task via `GradleRunner`.
     This reproduces the actual `FlowsExtension` owner chain and real Groovy
     compilation. Confirm it fails with the reported `MissingMethodException` on the
     `flows` extension. **Ordering note**: `useFrom()` throws `IllegalStateException`
     if no inputs exist (`CommandStepBuilder.kt:192`), so the fixture must call
     `from(...)` *before* `param(useFrom())` — otherwise the test fails for an
     unrelated reason and could be misread as "fix didn't work."
   - Testing: Run `:flows:adapters:in:gradle:test`; the new functional test fails
     before the fix (red), reproducing the issue.

**Phase 1 Deliverable**: A committed reproduction TestKit test (behind `@Disabled`
if kept red on `develop`) confirming the symptom. Choosing the fix *mechanism*
(DELEGATE_ONLY / owner-reset / assist) is Phase 2's job — it requires trying
candidates against this red test, not just observing the symptom.

### Phase 2: Fix the resolution
**Goal**: `useFrom()`/`useTo()` resolve to the `CommandStepBuilder` in the nested
Groovy closure; the Phase 1 test goes green.
**Status**: ✅ Completed 2026-07-19. Fix = zero-arg `useFrom()`/`useTo()` overloads
on `CommandStepBuilder` (not `bindClosure`). Step 2.2 skipped as moot.

1. **[x] Step 2.1**: Select and apply the fix mechanism.
   - Files: `flows/adapters/in/gradle/src/main/kotlin/.../FlowDsl.kt` (fix site
     confirmed in Phase 0 — the shared `bindClosure` `FlowDsl.kt:83` **if** the leak
     is at the plugin-owned `flow→commandStep` boundary; and `CommandStepBuilder.kt`
     only if approach C is chosen).
   - Description: **Choose the mechanism here** by trying candidates against the
     Phase-1 red test (this selection needs experimentation, not just the symptom):
     (A) `DELEGATE_ONLY`, (B) also set `closure.owner` to the builder, (C) a
     resolution-assist on the builder. Apply the minimal one that turns the red test
     green. Fixing the shared idiom means every builder benefits and it stays
     single-sourced. Must preserve access to outer Groovy variables inside step
     closures (this is exactly what `DELEGATE_ONLY` risks breaking).
   - Testing: Phase 1 reproduction test passes; all existing `FlowDslGroovyOverload`
     and `CommandStepBuilder` tests stay green.

2. **[~] Step 2.2 (SKIPPED — moot)**: Guard outer-scope access — as a TestKit test.
   The fix does not touch closure delegate/owner binding, so there is no
   `DELEGATE_ONLY`-style risk to outer Groovy variables. See EXEC-0015 Deviation #3.
   - Files: functional test alongside the Phase-1 reproduction.
   - Description: Add a TestKit `build.gradle` case that references an outer Groovy
     variable (a `def` or project property) *inside* a `commandStep` closure and
     asserts it still resolves after the fix. **This guard must be a TestKit test,
     not a shim/Kotlin test** — a shim cannot reproduce Groovy owner semantics, so a
     shim guard would pass vacuously and hide a `DELEGATE_ONLY` regression.
   - Testing: The outer-variable TestKit build succeeds post-fix.

**Phase 2 Deliverable**: Working fix at the confirmed site with the reproduction
test green, existing overload tests green, and the outer-scope guard passing.

### Phase 3: Regression guard, e2e, and docs
**Goal**: Lock in the behaviour and update guidance.
**Status**: ✅ Completed 2026-07-19 (Steps 3.1–3.4).

1. **[x] Step 3.1**: Broaden test coverage + guard the rollback path.
   - Files: `flows/adapters/in/gradle/src/test/kotlin/.../FlowDslGroovyOverloadTest.kt`
     and/or the TestKit fixtures.
   - Description: Add nested-closure cases for `useTo()`, indexed `useFrom(i)`/
     `useTo(i)`, and use inside `option(...)`/`withOption(...)` — not just `param`.
     Also add a case asserting the **two-statement workaround still works**
     (`def i = useFrom(); param(i)`), since the plan names it as the rollback path.
   - Testing: `:flows:adapters:in:gradle:test` green.

2. **[x] Step 3.2**: Verify coverage did not regress.
   - Files: none (verification only).
   - Description: A TestKit functional test runs in a separate build JVM and may
     **not** count toward JaCoCo coverage of the changed `bindClosure` lines, which
     could trip `verifyCodeCoverage` (CLAUDE.md: 70% domain / 50% infra floor). If
     the changed lines are not covered by an in-JVM test, add a targeted in-JVM
     `FlowDslGroovyOverloadTest` case that exercises the fixed binding path so
     coverage holds. Run `./gradlew jacocoReport verifyCodeCoverage` (via the
     `build`/`check` skill).
   - Testing: `verifyCodeCoverage` passes; `bindClosure`'s changed lines are covered.

3. **[x] Step 3.3**: E2E verification against the `../common` Groovy consumer.
   - Files: none (verification only); note results in the exec log.
   - Description: Confirm `param(useFrom())` works without the two-statement
     workaround, via the `e2e-test`/`verify` path used in PLAN-0011.
   - Testing: Groovy consumer builds with the direct `useFrom()` form.

4. **[x] Step 3.4**: Update documentation.
   - Files: `CLAUDE.md` (the `useFrom()`/`useTo()` DSL-shortcut section), and any
     flows user docs mentioning the Kotlin-only caveat.
   - Description: State that `useFrom()`/`useTo()` now work in the Groovy DSL as
     bare arguments; remove/repurpose the workaround note, or document any residual
     limitation if approach (D) was taken.
   - Testing: Doc review.

**Phase 3 Deliverable**: Regression-guarded, e2e-verified, documented fix.

## 6. Testing Strategy

### Unit Tests
- New nested-closure reproduction test (Phase 1) + broadened coverage (Phase 3.1)
  in `FlowDslGroovyOverloadTest` (or a dedicated test class).
- Existing `CommandStepBuilderTest` (direct `useFrom`/`useTo`) must stay green.

### Integration Tests
- Gradle TestKit functional test with a real `build.gradle` using `flows { flow {
  commandStep { param(useFrom()) } } }` (Phase 1 reproduction; the chosen vehicle).

### Manual Testing
- E2E against `../common` (Phase 3.2): the actual Groovy consumer that surfaced the
  bug.

## 7. Risks and Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Standing up TestKit is greenfield here (no existing TestKit/`GradleRunner` in the repo; documented fat-jar/classpath history) and eats significant time | High | **High** | Phase 0 spike proves TestKit can apply the plugin *before* any fix work; documented Groovy-source-set fallback if the classpath fights back |
| The leak is at the Gradle-owned `flows→flow` boundary, not the plugin-owned `flow→commandStep` boundary — so `bindClosure` is the wrong fix site | High | Medium | Phase 0 Step 0.2 pins the leaking boundary before committing to the fix site; Design Decision (Fix scope) is revised if the leak is Gradle-owned |
| TestKit functional test doesn't count toward JaCoCo, regressing `verifyCodeCoverage` | Medium | Medium | Phase 3.2 verifies coverage and adds an in-JVM test covering the changed `bindClosure` lines if needed |
| `DELEGATE_ONLY` (approach A) breaks legitimate references to outer Groovy variables inside step closures | Medium | Medium | Prefer a targeted owner fix (B); Phase 2.1 adds a test referencing an outer variable inside a step closure |
| Hardening shared `bindClosure` regresses other step overloads | Medium | Low | Existing single-level overload tests (all `*Step`) must stay green; the change is one shared idiom, so a regression surfaces across the whole suite immediately |
| Root cause differs from the suspicion, wasting the fix | Medium | Medium | Phase 1 spike confirms the consulted object before any fix is written |

## 8. Documentation Updates

- [ ] CLAUDE.md — update the `useFrom()`/`useTo()` DSL-shortcut section (currently
      Kotlin-example-only) to confirm Groovy support (or document residual limits)
- [ ] Flows user docs — remove the Groovy `useFrom()` workaround caveat if fixed
- [ ] Inline Kdoc on `useFrom`/`useTo` if behaviour/notes change
- [ ] No CLAUDE.md architectural-pattern change (adapter-local interop fix)

## 9. Rollout Plan

1. Merge via feature branch → develop; the fix is adapter-local and low-risk.
2. Verify against the `../common` Groovy consumer before/after.
3. Rollback: revert the `FlowDsl.kt` (and any `CommandStepBuilder.kt`) change; the
   two-statement workaround remains available, so consumers are never blocked.

## 10. Revision History

| Date | Updated By | Changes |
|------|------------|---------|
| 2026-07-19 | AI Agent | Resolved all 3 open questions: (1) reproduce via Gradle TestKit functional test; (2) fix mechanism decided after Phase-1 spike; (3) harden the shared `bindClosure` for all overloads. Propagated to design decisions, Phase 1/2 steps, dependencies, testing, and risks. |
| 2026-07-19 | AI Agent | Status draft → accepted (no unresolved questions). |
| 2026-07-19 | AI Agent | Adversarial challenge at acceptance (revised): added Phase 0 (TestKit feasibility + pin leaking boundary), made fix-scope conditional on the boundary, moved fix-mechanism selection into Phase 2, added outer-variable TestKit guard (2.2), coverage check (3.2), workaround guard (3.1), and ordering note (1.1); re-rated TestKit risk to High. |
| 2026-07-19 | AI Agent | Executed (per-phase). Phase-0 spike disproved the closure-binding hypothesis: real cause is Kotlin default-param methods lacking a no-arg JVM overload for Groovy. Fix = zero-arg `useFrom()`/`useTo()` overloads on `CommandStepBuilder`. Reproduction via `GroovyShell`+`ProjectBuilder` in `infra/gradle` (no TestKit). Steps 0.1–1.1, 2.1, 3.1–3.4 done; 2.2 skipped (moot). Verified: unit + Groovy repro + real-Gradle e2e green, coverage + spotless pass. Status → implemented. Deviations + a follow-up (dialectVersion parse for flows-only builds) in [EXEC-0015](EXEC-0015_flows-groovy-usefrom-useto-nested-closure.md). |

---

**Note**: This plan should be reviewed and approved before implementation begins.
