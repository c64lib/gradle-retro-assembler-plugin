# Execution Log: Pipeline DSL - Enable Parallel Execution

**Exec ID**: EXEC-0001
**Plan**: [PLAN-0001](PLAN-0001_pipeline-dsl-parallel-execution.md)
**Issue**: #135
**Started**: 2026-07-15
**Last Updated**: 2026-07-15
**State**: completed

> Backfilled 2026-07-15 from the original /execute session, which predated the execution-log addition (MET-0014).

## 1. Execution Sessions

### Session 1 — 2026-07-15

- **Scope**: all steps (Phases 0–3)
- **Mode**: per-phase
- **Outcome**: completed — every plan step done; plan moved to `implemented`

| Step | Result | Verification | Notes |
|------|--------|--------------|-------|
| 0.1 | completed | Spike test `DslShapedArtifactMatchingTest` — 3 assertions failed pre-fix, confirming broken implicit deps for DSL-shaped artifacts | Observation kept as regression test |
| 0.2 | completed | Spike test `FlowDslDependencyTest` — both validation assertions failed pre-fix on tony-shaped flows | Observation kept as regression test |
| 0.3 | completed | `:flows:test` + `:flows:adapters:in:gradle:test` green (216 tests) | Path-based matching in `FlowDependencyGraph`; source-file marking in `FlowDslBuilder.build()`. Commit `7412b27` |
| 1.1 | completed | `:infra:gradle:compileKotlin` green; no references remain | Deleted dead `FlowExecutionTask.kt`. Commit `afd8a4b` |
| 1.2 | completed | `FlowTasksGeneratorTest` — circular `dependsOn` fails at config time; tony-shaped flows validate clean; 79 adapter tests green | `validateFlowGraph()` added to `FlowTasksGenerator`. Commit `afd8a4b` |
| 2.1 | completed | `FlowTasksGeneratorTest` — independent steps have empty dependency sets | Removed index-based sequential step chain. Commit `6a0da23` |
| 2.2 | completed | `FlowTasksGeneratorTest` — artifact-only linkage wires `flowCompilation` → `flowAssets`; explicit `dependsOn` wires `flowSecond` → `flowFirst` | Flow-level deps from `FlowService.getDependenciesOf()`. Commit `6a0da23` |
| 2.3 | completed | `FlowTasksGeneratorTest` — aggregation task depends on all step tasks | `dependsOn(flowStepTasks)`. Commit `6a0da23` |
| 3.1 | completed | `:flows:adapters:in:gradle:test` — 10 new wiring assertions + 2 validation scenarios green | `FlowTasksGeneratorTest`, `FlowDslDependencyTest`. Commit `6a0da23` |
| 3.2 | completed (with 2.2) | `FlowServiceTest` green | `FlowService.getDependenciesOf()` added; `getAllDependencies` `private` → `internal`. Commits `6a0da23`, `eb5149b` |
| 3.3 | completed | Documentation review | CLAUDE.md Parallel Execution note. Commit `eb5149b` |
| 3.4 | completed | e2e gate PASSED against tony: plain `flows` clean of validation errors; full `flows --parallel` rebuild BUILD SUCCESSFUL in 12s with parallel interleaving; all 8 representative artifacts fresh and non-empty | Commit `7b85f3c` (plan closeout) |

## 2. Deviations from Plan

| # | Step | Deviation | Reason | Impact |
|---|------|-----------|--------|--------|
| 1 | 1.2 | Added `--add-opens java.base/java.lang=ALL-UNNAMED` to the flows-adapter test JVM in `flows/adapters/in/gradle/build.gradle.kts` — unplanned | Gradle's `ProjectBuilder` (used by the new `FlowTasksGeneratorTest`) fails on modern JDKs with a misleading `Could not inject synthetic classes.` error | Required for any ProjectBuilder-based test in this module; recorded as a project memory |
| 2 | 0.3 / 2.x | Cleaned two pre-existing unused-variable Detekt/compiler warnings in `FlowDependencyGraph.kt` (`getParallelExecutionOrder`, `getParallelCandidates`) | Touching the file anyway; keep it warning-free | Minor; no behaviour change |
| 3 | 3.2 | `FlowService` accessor implemented together with Step 2.2 rather than in its Phase 3 slot | Step 2.2 depends on it (noted in the plan itself) | None — plan anticipated this ordering |
| 4 | 3.4 | e2e run needed a manual `rm -rf` of tony's generated output dirs to force a genuine full rebuild; the Haiku subagent's first "clean flows" report was misleading (66 tasks up-to-date after clean) | tony's `clean` does not delete the literal `build/charpad|kickass|dasm|…` dirs the flows write to | Verification method adjusted; not a plugin regression. Recorded as a project memory |

## 3. Follow-ups

- Consider opening a PR into `develop` via gh-utils (branch `feature/135-pipeline-dsl-parallel-execution` pushed; not yet a PR).
- Unrelated: repo default branch has 6 open Dependabot alerts (1 high, 5 moderate) surfaced during push — review in the Security tab.
- tony harness quirks (clean not deleting flow outputs; `flowIntroStepLoadingPicture` UP-TO-DATE due to duplicate output path) captured as project memory for future e2e runs.
