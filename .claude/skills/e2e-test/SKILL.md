---
name: e2e-test
description: >
  Run end-to-end tests of the locally built plugin against the tony game project
  (C:/Users/maciek/prj/cbm/tony), which serves as the real-world test harness.
  Publishes the current plugin state to mavenLocal as 1.8.1-SNAPSHOT, then runs
  tony's Gradle build (flows by default, or a caller-specified task) and verifies
  the expected artifacts are produced. Routes every Gradle invocation through a
  spawned Haiku subagent. Use when asked to e2e-test the plugin, verify plugin
  changes against tony, or run flows on tony.
user-invocable: true
allowed-tools: Agent Skill Bash Read Grep Glob
---

# e2e-test

Runs an end-to-end test of the plugin using the **tony** project as the harness. The plugin under test is this repo; the consumer project is tony. NOTE: all paths are hardcoded for now (generalization planned later).

## Hardcoded facts

| What | Value |
|------|-------|
| Plugin repo | `C:/Users/maciek/prj/github.com/c64lib/gradle-retro-assembler-plugin` |
| Harness repo | `C:/Users/maciek/prj/cbm/tony` (Git Bash: `/c/Users/maciek/prj/cbm/tony`) |
| Plugin version under test | `1.8.1-SNAPSHOT` |
| Local deployment target | `C:/Users/maciek/.m2/repository/com/github/c64lib/retro-assembler/` |
| Harness branch for experiments | `flows-experiments` |
| Default harness task | `flows` |

tony resolves the plugin from `mavenLocal()` (declared first in its `settings.gradle` `pluginManagement.repositories`) and pins version `1.8.1-SNAPSHOT` in its `build.gradle.kts`. If this repo's version in `build.gradle.kts` ever moves past `1.8.1-SNAPSHOT`, tony's pinned version must be updated to match — flag this mismatch instead of silently testing a stale artifact.

## Division of labour

Same convention as the `build` and `test` skills:

- **Running Gradle is delegated** — never run `./gradlew` inline on the main agent.
  - Gradle runs **in the plugin repo** go through the `build` skill.
  - Gradle runs **in the tony repo** are outside the `build` skill's scope, so spawn the Haiku subagent directly (template below).
- **Analysis** (verifying artifacts, diagnosing failures, writing the report) stays on the main agent.

### Haiku subagent template for tony-side Gradle runs

```
Agent(
  subagent_type: "general-purpose",
  model: "haiku",
  run_in_background: false,
  description: "Run tony gradle <tasks>",
  prompt: "Run this exact command with the Bash tool (timeout 600000 ms):
           cd /c/Users/maciek/prj/cbm/tony && ./gradlew <tasks> --console=plain
           Report: the final BUILD SUCCESSFUL/FAILED line, every failed task name,
           and for each failure the error lines verbatim (including file:line and
           any exception messages). Do not attempt fixes."
)
```

## Procedure

### Step 1: Pre-flight checks (main agent, cheap)

1. Confirm tony's working tree state and branch:
   `cd /c/Users/maciek/prj/cbm/tony && git branch --show-current && git status --porcelain`
   Expected branch: `flows-experiments` (or whatever the user is deliberately testing on). Report the branch; do not switch branches unless asked.
2. Confirm the version match: `version = "1.8.1-SNAPSHOT"` in the plugin repo's root `build.gradle.kts` vs the version in tony's `build.gradle.kts` plugin block. On mismatch, stop and report.

### Step 2: Publish the plugin to mavenLocal

Deploy the current plugin state locally via the `build` skill:

```
Skill(skill: "build", args: "publishToMavenLocal")
```

Then confirm the artifact was refreshed: the modification time of
`C:/Users/maciek/.m2/repository/com/github/c64lib/retro-assembler/gradle/1.8.1-SNAPSHOT/gradle-1.8.1-SNAPSHOT.jar`
must be newer than the publish start time. If publishing failed or the jar is stale, stop and report — running tony against a stale jar produces misleading results.

Skip this step only if the user explicitly says the current mavenLocal snapshot is the one to test.

### Step 3: Run the harness build (delegated to Haiku subagent)

Default run:

```
cd /c/Users/maciek/prj/cbm/tony && ./gradlew flows --console=plain
```

Variants, when the user asks for them:

| Request | tony task(s) |
|---------|--------------|
| default / "run e2e" / "test flows" | `flows` |
| a single flow | `flowIntro`, `flowTitle`, `flowLoader`, `flowGameEnd`, `flowGameOver`, `flowGame` |
| "full build" | `build` (heavy: needs KickAssembler, exomizer, dasm, cartconv on PATH) |
| "clean run" / stale-state suspicion | `clean flows` |
| plugin resolution only | `buildEnvironment` |

Always pass `--console=plain`. Add `--stacktrace` only on a re-run to diagnose an unclear failure, and say so.

### Step 4: Verify artifacts (main agent)

After a successful `flows` run, spot-check that representative outputs exist and are non-empty under `C:/Users/maciek/prj/cbm/tony/`:

- `build/charpad/intro/intro-large-font.charset.bin` (charpadStep + tiles interleaver)
- `build/charpad/game/dashboard-colours-lo.bin` (charsetScreenColours nybbler)
- `build/goattracker/title/tony-adam-vcs.sid` (goattrackerStep)
- `build/spritepad/game/eyes.bin` (spritepadStep)
- `build/spritepad/game/skull.bin` (imageStep split/extend/sprite)
- `build/charpad/intro/comic-1-e.charset.z.bin` (exomizerStep mem mode)
- `build/kickass/intro/intro-linked.z.bin` (assembleStep + exomizerStep raw mode)
- `build/dasm/loader/tony_loader.bin` (dasmStep)

A zero-byte or missing file after a "successful" build is a finding — report it.

If only a subset of flows was run, verify only that subset's outputs.

### Step 5: Report

```markdown
## E2E Test Report (tony harness)

**Plugin version**: 1.8.1-SNAPSHOT (published <timestamp>)
**Tony branch**: <branch>
**Task(s) run**: <tasks>
**Status**: PASSED | FAILED

### Publish
- <ok / failure details>

### Harness build
- <BUILD SUCCESSFUL/FAILED, failing tasks with error lines>

### Artifact verification
- <checked files: present/missing/empty>

### Findings
- <anything anomalous, or "none">
```

## Failure handling

- **Publish fails** → this is a plugin-repo problem; report the failing Gradle task and stop. Suggest `/check` or `/test` for diagnosis.
- **Plugin resolution fails in tony** (`Could not resolve com.github.c64lib.retro-assembler...`) → check mavenLocal contents at the hardcoded path and the version pin; report the mismatch.
- **Flow task fails** → likely the actual e2e signal. Correlate the failing `flow...Step...` task with the plugin's flows subdomain (step builders in `flows/adapters/in/gradle/.../dsl/`, domain steps in `flows/src/main/kotlin/.../steps/`) and with recent plugin changes (`git log` on the plugin repo). Report the diagnosis; do not fix unless asked.
- **External tool missing** (exomizer, dasm, cartconv, java) → environment problem, not a plugin regression. Name the missing tool and stop.

## Guardrails

- Never run `./gradlew` inline on the main agent — always delegate.
- Do not switch tony's git branch, modify tony's files, or clean tony's build directory unless the user asks.
- `publishToMavenLocal` mutates `~/.m2` — that is this skill's purpose, so it needs no extra confirmation, but never publish to any remote repository from this skill.
- One subagent per logical Gradle run; chain tasks (e.g. `clean flows`) in a single invocation.
- Report failures verbatim; do not retry blindly.
