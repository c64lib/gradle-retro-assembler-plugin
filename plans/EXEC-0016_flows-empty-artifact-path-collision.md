# Execution Log: Fix empty-path flow artifact collision blocking all Gradle tasks

**Exec ID**: EXEC-0016
**Plan**: [PLAN-0016](PLAN-0016_flows-empty-artifact-path-collision.md)
**Issue**: #181
**Started**: 2026-07-19
**Last Updated**: 2026-07-19
**State**: completed

## 1. Execution Sessions

### Session 1 — 2026-07-19

- **Scope**: all (Steps 1.1, 1.2, 1.3, 2.1)
- **Mode**: autonomous
- **Outcome**: completed — all 4 steps done and verified

| Step | Result | Verification | Notes |
|------|--------|--------------|-------|
| 1.1 | completed | `:flows:test` green; unit assertions pass | Filtered empty `primaryOutputs` sub-list in `CharpadOutputs.getAllOutputPaths()` with `isNotEmpty()`. |
| 1.2 | completed | `:flows:adapters:in:gradle:test` green | Filtered empty in/out paths in `FlowDsl.registerStep()` with `isNotEmpty()`. |
| 1.3 | completed | Fails on HEAD (lines 61/83) without fix; green with fix | New `FilterOnlyEmptyPathCollisionTest` drives DSL→`FlowDslBuilder.build()`→`FlowService.validateFlows`; asserts no empty artifact + sub-outputs survive. |
| 2.1 | completed | tony `./gradlew tasks` + `flows` BUILD SUCCESSFUL; no "produced by multiple flows" error; 68 flow steps ran; artifacts present & non-empty | Fix confirmed against the originating harness (plugin published to mavenLocal 1.8.1-SNAPSHOT). |

## 2. Deviations from Plan

| # | Step | Deviation | Reason | Impact |
|---|------|-----------|--------|--------|
| 1 | 1.3 | Regression test drives `FlowDslBuilder` (`flow("intro"){...}`) instead of `FlowBuilder(...).build()` directly. | `FlowBuilder.build()` alone does not mark unproduced consumed inputs as source files — that happens in `FlowDslBuilder.build()` (FlowDsl.kt:66-76). Using `FlowBuilder` directly made the raw `.ctm` inputs trip a spurious `MissingArtifactProducer` ERROR unrelated to #181. | Test now exercises the true plugin entry point (more faithful reproduction); no production change. |

## 3. Follow-ups

- tony's `flows` run emits pre-existing, unrelated warnings — a `loader` orphaned-flow warning
  ("produces artifacts that are never consumed: build-loader_output_0") and Gradle
  implicit-task-dependency optimization warnings around charpad outputs. Neither is the #181 bug
  and neither fails the build; candidate for a separate cleanup issue if desired.
