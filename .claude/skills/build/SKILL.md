---
name: build
description: >
  Wrap this project's Gradle build. Exposes every task the build script declares —
  lifecycle (build, assemble, check, clean), verification (test, collectTestResults,
  jacocoReport, verifyCodeCoverage, detekt), formatting (spotlessCheck, spotlessApply),
  and publishing (infra/gradle jar, publishPluginJar) — plus per-module `:module:task`
  addressing. Routes every actual Gradle invocation through a spawned Haiku subagent and
  reports the result. Use when asked to build, assemble, clean, run a Gradle task, verify
  coverage, format code, or build/publish the plugin JAR.
allowed-tools: Agent Bash Read Grep Glob
---

# build

Wraps the Gradle build of this multi-module plugin so every build operation goes through one convention. The **main agent decides which task(s) to run and with which flags**; a **spawned Haiku subagent runs the mechanical `./gradlew` command** and reports back. This keeps slow, deterministic build work off the main agent while the skill supplies the task catalogue and the guardrails.

**Boundary with `check` and `test`:** those two skills already own *diagnostic* runs — `check` runs the full static-analysis suite and reports each check, `test` runs the suite and does deep failure analysis. Prefer them when the user wants that structured reporting. Use `build` for everything else: plain lifecycle tasks, clean, assemble, formatting, publishing, arbitrary or per-module tasks, or when the user just wants a task run and its raw result relayed.

## How execution works: spawn a Haiku subagent

For every operation that runs Gradle, spawn a subagent on the **Haiku** model via the `Agent` tool:

```
Agent(
  subagent_type: "general-purpose",
  model: "haiku",
  run_in_background: false,
  description: "<short label>",
  prompt: "<the exact ./gradlew command to run, plus what to report back>"
)
```

The main agent's job before spawning:
1. Work out which task(s) and flags the request maps to (see the catalogue below).
2. Give the subagent a precise, self-contained task: the literal `./gradlew` command and the exact output to return (task outcome, failing tasks, report paths, error text). The subagent executes; it does not decide which tasks to run.

Run synchronously (`run_in_background: false`). Builds can be long — set a generous timeout on the subagent's Bash call (e.g. 600000 ms) and let it wait. Relay the subagent's result to the user; on failure, surface the failing task and the relevant error lines with `file:line` references where the output gives them.

### Invocation form

- On this repo the wrapper is `./gradlew` (Bash tool) or `.\gradlew.bat` (PowerShell). Prefer `./gradlew` via the subagent's Bash tool.
- Always add `--console=plain` so output parses cleanly.
- Pass `--stacktrace` when a previous run failed and the cause is unclear.

## Task catalogue

All tasks the build script exposes. Run any of them, or any valid combination, through the subagent.

### Lifecycle (base / Kotlin JVM plugin)

| Task | What it does |
|------|--------------|
| `build` | Assemble **and** check the whole project (compiles, runs tests, static analysis). The full lifecycle. |
| `assemble` | Compile and package without running tests or checks. |
| `check` | Run all verification tasks (tests + Spotless + Detekt) without assembling artifacts. |
| `clean` | Delete `build/` directories across all modules. |

Common combinations:
- `build -x test` — full build, skipping test execution (fast compile + static analysis).
- `clean build` — clean rebuild.

### Verification

| Task | What it does |
|------|--------------|
| `test` | Run all unit tests across every submodule. |
| `collectTestResults` | Copy every module's `test-results/test/*.xml` into `build/test-results/gradle` (depends on `test`). |
| `jacocoReport` | Generate the aggregated JaCoCo coverage report → `build/reports/jacoco/aggregated/index.html` (+ `jacoco.xml`). Needs test execution data. |
| `verifyCodeCoverage` | Verify coverage meets thresholds; prints the aggregated report path. |
| `detekt` | Static analysis → `build/reports/detekt/detekt.html` (+ xml). `ignoreFailures` is on, so it reports but does not fail the build. |

### Formatting (Spotless — ktfmt + license headers)

| Task | What it does |
|------|--------------|
| `spotlessCheck` | Verify formatting compliance; fails on violations. |
| `spotlessApply` | Auto-fix formatting. Only run when the user asks to fix formatting. |

### Publishing (the Gradle plugin itself)

| Task | What it does |
|------|--------------|
| `:infra:gradle:jar` | Build the plugin JAR. |
| `:infra:gradle:publishPluginJar` | Build the plugin JAR for publishing. |

### Per-module addressing

Any task can be scoped to one module with the `:module:task` form, e.g.:

```
./gradlew :flows:test
./gradlew :flows:adapters:in:gradle:test
./gradlew :infra:gradle:jar
./gradlew :module:test --tests "TestClassName"
```

Use this for fast iteration on a single module. This project has 58+ submodules organised in hexagonal architecture (`compilers`, `dependencies`, `emulators`, `testing`, `processors`, `flows`, `crunchers`, `shared`, `infra`).

### Discovering tasks live

If the user asks for a task not in this catalogue, or to see everything available, run it through the subagent:

```
./gradlew tasks --all --console=plain
```

and relay the list. The catalogue above is authoritative for the tasks the root build script declares; the live listing also surfaces tasks contributed by applied plugins per module.

## Mapping requests to tasks

| User says… | Command |
|------------|---------|
| "build the project" / "run the build" | `./gradlew build --console=plain` |
| "build without tests" / "just compile" | `./gradlew build -x test --console=plain` |
| "clean build" / "rebuild from scratch" | `./gradlew clean build --console=plain` |
| "clean" | `./gradlew clean --console=plain` |
| "assemble" / "package" | `./gradlew assemble --console=plain` |
| "format the code" / "fix formatting" | `./gradlew spotlessApply --console=plain` |
| "check formatting" | `./gradlew spotlessCheck --console=plain` |
| "coverage report" | `./gradlew jacocoReport --console=plain` |
| "verify coverage" | `./gradlew verifyCodeCoverage --console=plain` |
| "build the plugin jar" | `./gradlew :infra:gradle:jar --console=plain` |
| "publish the plugin jar" | `./gradlew :infra:gradle:publishPluginJar --console=plain` |
| "run <task>" / "run <task> in <module>" | `./gradlew [:module:]<task> --console=plain` |

For "run the tests and tell me what failed" or "run the checks and report", hand off to the **`test`** or **`check`** skill instead — they do the structured diagnostic reporting.

## Guardrails

- **Never run `spotlessApply` or any mutating task** (e.g. `clean`, `publishPluginJar`) **without an explicit request** — these change files, delete build output, or publish. Read-only tasks (`build`, `test`, `check`, `assemble`, report tasks) run freely.
- **Do not add `--no-verify`-style bypasses** or disable configured checks unless the user asks.
- **Do not attempt to fix compilation, test, Detekt, or Spotless failures automatically** — report them and wait, unless the user asked for a fix.
- **One subagent per logical build request.** Chain tasks in a single `./gradlew` call (e.g. `clean build`) rather than spawning several subagents.
- If Gradle fails, report the failing task and the relevant error lines (with `file:line` where available) and stop — do not retry blindly. Re-run with `--stacktrace` only to diagnose, and say so.
