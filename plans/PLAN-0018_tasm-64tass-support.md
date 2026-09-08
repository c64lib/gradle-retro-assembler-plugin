# Feature: TASM (64tass) Assembler Support

**Plan ID**: PLAN-0018
**Issue**: —
**Status**: in progress
**Challenge**: revised 2026-09-08
**Created**: 2026-09-08
**Last Updated**: 2026-09-08

## 1. Feature Description

### Overview
Add support for the **64tass** cross-assembler (the Turbo Assembler / TASM dialect of 6502
assembly) as a supported assembler, integrated as a step in the flows DSL plus a new
`compilers` subdomain — mirroring exactly how DASM is integrated on `develop`. The 64tass
binary is expected on `PATH` (no auto-download), just like `dasm`.

### Requirements
- A new `compilers/64tass` subdomain (Kotlin package root `...compilers.tass`) with domain,
  use case, port, and outbound Gradle adapter that invokes the `64tass` executable via
  `project.exec`.
- A new flows step (`tassStep`) with config, mapper,
  port, domain step, DSL builder, Kotlin lambda + Groovy `Closure` overloads, Gradle task,
  and port/command adapters — the full `dasmStep` footprint.
- `FlowTasksGenerator` dispatches the new step type to its task and injects the use case.
- `RetroAssemblerPlugin.wireFlows` constructs and passes the use case.
- Module registration in `settings.gradle.kts`, `compileOnly` + test-runtime deps in
  `infra/gradle/build.gradle.kts` (fat jar bundles automatically), `implementation` dep in
  `flows/adapters/in/gradle/build.gradle.kts`.
- Unit tests at every layer (better coverage than the thin DASM test set), user manual and
  arc42 documentation updates.

### Success Criteria
- A `build.gradle` / `build.gradle.kts` using the new step assembles a `.asm` source with
  64tass into a `.prg` (or raw binary) via `./gradlew flows` / `./gradlew asm`, in both
  Groovy and Kotlin DSLs.
- Incremental build works: unchanged inputs → step is `UP-TO-DATE`.
- `./gradlew build` passes (all modules, all tests); Detekt clean for new code.
- Coverage of the new modules meets the 70% domain-module target.

## 2. Root Cause Analysis

Motivation: the plugin currently supports Kick Assembler (full dialect + flows step) and
DASM (flows step). Turbo Assembler syntax is one of the most widespread 6502 dialects, and
64tass is its standard cross-assembler in the C64 scene. Projects written for 64tass cannot
currently be built by this plugin.

### Current State
- Kick Assembler: full integration (`asm` task chain + `assembleStep` in flows,
  auto-downloaded JAR).
- DASM: flows-only integration (`dasmStep`), native CLI on PATH, no inbound Gradle adapter,
  no downloads (`RetroAssemblerPlugin.kt:257`, `FlowTasksGenerator.kt:182-187`).
- No 64tass/TASM support anywhere.

### Desired State
64tass reaches parity with DASM: a flows step backed by a `compilers` subdomain use case,
native binary on PATH, full Groovy + Kotlin DSL support, tests and docs.

### Gap Analysis
Everything must be created new, but every piece has a 1:1 DASM template (see §3). No
existing behaviour changes; the only touched shared files are `FlowDsl.kt`,
`FlowTasksGenerator.kt`, `ProcessorConfig.kt`, `AssemblyConfigMapper.kt` (new command data
class), `RetroAssemblerPlugin.kt`, and three build/settings files — all additive.

## 3. Relevant Code Parts

### Existing Components (the DASM template to mirror)
- **DasmAssembleUseCase / DasmAssembleCommand**: single-`apply` use case delegating to port.
  - Location: `compilers/dasm/src/main/kotlin/com/github/c64lib/rbt/compilers/dasm/usecase/DasmAssembleUseCase.kt:34-65`
  - Integration Point: template for `TassAssembleUseCase` / `TassAssembleCommand`.
- **DasmAssemblePort**: port hiding the CLI technology.
  - Location: `compilers/dasm/src/main/kotlin/.../usecase/port/DasmAssemblePort.kt:33-48`
- **DasmAssembleAdapter + DasmCommandLineBuilder**: outbound adapter running
  `project.exec { executable = "dasm" }` with a fluent internal CLI builder.
  - Location: `compilers/dasm/adapters/out/gradle/src/main/kotlin/.../DasmAssembleAdapter.kt:35-74`,
    `DasmCommandLineBuilder.kt:34-111`
  - Integration Point: template for `TassAssembleAdapter` + `TassCommandLineBuilder`
    (executable `64tass`; **key CLI difference**: dasm takes source first then options —
    64tass takes options first, source file last, and defaults output to `a.out`, so `-o`
    must always be emitted).
- **DasmStep**: flows domain step (taskType `"dasm"`, port injection, validate + execute).
  - Location: `flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/steps/DasmStep.kt:39-160`
- **DasmConfig / DasmCommand / DasmConfigMapper**: flows-side config, command mirror, and
  mapper with source discovery (srcDirs/includes/excludes globs) and output resolution.
  - Location: `flows/src/main/kotlin/.../domain/config/ProcessorConfig.kt:130-146`,
    `AssemblyConfigMapper.kt:52-65`, `DasmConfigMapper.kt:35-250`
- **DasmAssemblyPort**: flows port with single + list `assemble` (default method).
  - Location: `flows/src/main/kotlin/.../usecase/port/DasmAssemblyPort.kt:35-52`
- **DasmStepBuilder + FlowDsl `dasmStep`**: DSL builder (function-style setters) and the
  Kotlin lambda + `@DelegatesTo` Groovy Closure overloads on `FlowBuilder`.
  - Location: `flows/adapters/in/gradle/src/main/kotlin/.../dsl/DasmStepBuilder.kt:31-193`,
    `FlowDsl.kt:185-198` (closure binding helper `bindClosure` at `FlowDsl.kt:83-88`)
- **DasmAssembleTask + DasmPortAdapter + DasmCommandAdapter**: `BaseFlowStepTask` subclass
  injecting the port adapter into the step before `execute`.
  - Location: `flows/adapters/in/gradle/src/main/kotlin/.../tasks/DasmAssembleTask.kt:39-110`,
    `assembly/DasmPortAdapter.kt:37-50`, `assembly/DasmCommandAdapter.kt:36-69`
- **FlowTasksGenerator**: nullable use-case constructor params, `is DasmStep ->` dispatch,
  injection, `registerAdditionalInputFiles` overload, `configureOutputFiles` branch.
  - Location: `flows/adapters/in/gradle/src/main/kotlin/.../FlowTasksGenerator.kt:55-63,182-187,230-240,299-307,315`
- **RetroAssemblerPlugin.wireFlows**: use-case construction and generator invocation.
  - Location: `infra/gradle/src/main/kotlin/com/github/c64lib/gradle/RetroAssemblerPlugin.kt:245-289`
- **Module registration**: `settings.gradle.kts:29-30` (dasm modules),
  `infra/gradle/build.gradle.kts:76-77,129-130` (compileOnly + test-runtime),
  `flows/adapters/in/gradle/build.gradle.kts:15` (implementation dep). Fat jar bundles all
  `compileClasspath` project deps automatically (`infra/gradle/build.gradle.kts:27-39`).

### Architecture Alignment
- **Domain**: new `compilers/64tass` subdomain (package `com.github.c64lib.rbt.compilers.tass`
  — a package segment cannot start with a digit); flows domain extended with the new step.
- **Use Cases**: `TassAssembleUseCase` (single public `apply(TassAssembleCommand)`).
- **Ports**: `TassAssemblePort` (compilers side), `TassAssemblyPort` (flows side).
- **Adapters**: outbound `compilers/64tass/adapters/out/gradle` (`TassAssembleAdapter`,
  `TassCommandLineBuilder`); inbound flows Gradle adapter pieces (`TassAssembleTask`,
  `TassPortAdapter`, `TassCommandAdapter`, `TassStepBuilder`, `FlowDsl` overloads). No
  inbound adapter module under `compilers/64tass` (same as DASM).

### Dependencies
- No new external libraries. The 64tass executable (v1.5x+) must be on PATH at build time
  of the *consuming* project (documented requirement, like dasm).

## 4. Questions and Clarifications

### Self-Reflection Questions
- **Q**: Should 64tass be a full dialect (`AssemblerType`) or flows-only?
  - **A**: Flows-only, mirroring DASM — confirmed by the user. `AssemblerType` is deprecated
    ("Will be removed in 2.0.0", `shared/domain/.../AssemblerType.kt:27`), so extending it
    would grow a deprecated surface.
- **Q**: Which "TASM" is meant?
  - **A**: 64tass, the Turbo Assembler successor cross-assembler — confirmed by the user.
- **Q**: Download the binary automatically like the unmerged DASM-completion branch?
  - **A**: No — PATH-based like DASM on `develop`; work is based on `develop`, independent of
    `feature/complete-dasm-support` — confirmed by the user.
- **Q**: Why PLAN-0018 when `develop` only has up to PLAN-0016?
  - **A**: PLAN-0017 is taken by "Complete DASM Support" on the unmerged
    `feature/complete-dasm-support` branch; using 0018 avoids an ID collision at merge time.
- **Q**: How does the 64tass CLI differ from dasm's for the command-line builder?
  - **A**: `64tass [options] source` (source **last**; dasm puts source first); output must
    always be given via `-o` (default is `a.out`); format is flag-based, not numeric:
    default output is CBM `.prg` with load address, `-b`/`--nostart` gives a raw binary;
    includes `-I <path>`, defines `-D <name>=<value>`, listing `-L <file>`, labels
    `-l <file>` (+ `--vice-labels`), case-sensitivity `-C`, ASCII→PETSCII `-a`, `-q` quiet,
    `--long-branch`, CPU via `--m6502`/`--m65c02`/`--m65816`/`--m65dtv02`/`--m4510`. Exact
    flags to be verified against the 64tass manual during Phase 1 implementation.

- **Q**: DSL step name: `tassStep` (named after the tool, like `dasmStep`) or `tasmStep`
  (named after the dialect the user requested)?
  - **A**: `tassStep` — named after the tool, consistent with `dasmStep`; classes use the
    `Tass` prefix (user decision, 2026-09-08).
- **Q**: Config surface for v1: minimal or extended?
  - **A**: Extended — includePaths, defines, outputFormat, listFile, labelsFile, cpu,
    caseSensitive, asciiConversion, quiet, longBranch, viceLabels, plus the shared
    srcDirs/includes/excludes/workDir/additionalInputs — matching dasm's near-full CLI
    coverage (user decision, 2026-09-08).
- **Q**: `outputFormat` representation: string values or the shared `OutputFormat` enum?
  - **A**: String values (`outputFormat: String = "prg"`), validated in `TassStep.validate()`
    against `{prg, bin}`, extensible to `xex`/`hex`/`srec` later; mirrors dasm's
    tool-specific raw representation and avoids coupling to the shared enum
    (user decision, 2026-09-08).

### Unresolved Questions
(none)

### Design Decisions
- **Decision**: Class-name prefix and module/directory name.
  - **Options**: A) directory `compilers/64tass`, Gradle paths `:compilers:64tass[:...]`,
    packages/classes `tass`/`Tass*`; B) directory `compilers/tass` everywhere.
  - **Recommendation**: A — the directory and Gradle module carry the real tool name
    (as `dasm` does), while packages/classes use `tass` because identifiers cannot start
    with a digit.
- **Decision**: Where the new use-case parameter lands in `FlowTasksGenerator`'s
  constructor.
  - **Options**: A) append `tassAssembleUseCase: TassAssembleUseCase? = null` after
    `dasmAssembleUseCase`; B) append at the very end after `extension`.
  - **Recommendation**: A — keeps the assembler use cases grouped; the only positional
    caller is `RetroAssemblerPlugin.wireFlows` (`RetroAssemblerPlugin.kt:267-275`), updated
    in the same change; tests construct it with named/default args.

### Adversarial Challenge
- **Status**: revised 2026-09-08 (mode A red-team at acceptance)
- **Findings**: (1) **high** — risk table falsely claimed no file overlap with the unmerged
  `feature/complete-dasm-support` branch; verified that branch also edits
  `RetroAssemblerPlugin.kt`, `infra/gradle/build.gradle.kts`, `settings.gradle.kts` → risk
  row rewritten with merge-order mitigation. (2) **medium** — derived output extension was
  fixed at `.prg` regardless of `outputFormat` → Step 2.1 now requires format-aware
  extension derivation. (3) **medium** — extended config surface lacked validation rules
  for `cpu` → Step 2.2 now validates `cpu` against the allowed 64tass set. (4)
  **low-medium** — builder unit tests can't catch a wrongly-remembered CLI flag → manual
  smoke test against the real binary made mandatory before the Phase 3 merge. One
  hypothetical noted without change: `compilers/64tass` directory vs `tass` package naming
  asymmetry (accepted consciously in §4 Design Decisions).

## 5. Implementation Plan

> **Execution status (2026-09-08)**: all steps 1.1–3.4 are **completed** on branch
> `feature/tasm-64tass-support` (full `./gradlew build` green, 296 tasks). See
> [EXEC-0018](EXEC-0018_tasm-64tass-support.md) for per-step evidence and deviations.
> Remaining before `implemented`: the mandatory manual smoke test with a real 64tass
> binary (Section 6) and the merge to `develop`.

### Phase 1: compilers/64tass subdomain (mergeable compiler backend)
**Goal**: A self-contained, fully tested compiler subdomain, not yet reachable from any DSL.

1. **Step 1.1**: Create domain + use case + port
   - Files: `compilers/64tass/src/main/kotlin/com/github/c64lib/rbt/compilers/tass/domain/TassAssemblerSettings.kt`,
     `.../usecase/TassAssembleUseCase.kt` (incl. `TassAssembleCommand`),
     `.../usecase/port/TassAssemblePort.kt`, `compilers/64tass/build.gradle.kts`
     (`id("rbt.domain")`, dep `:shared:domain`)
   - Description: Mirror `DasmAssemblerSettings` (marker), `DasmAssembleUseCase.kt:34-65`,
     `DasmAssemblePort.kt:33-48` with the 64tass option set decided in §4.
   - Testing: unit test `TassAssembleUseCaseTest` (port receives unpacked command).
2. **Step 1.2**: Create outbound Gradle adapter with CLI builder
   - Files: `compilers/64tass/adapters/out/gradle/src/main/kotlin/.../TassAssembleAdapter.kt`,
     `.../TassCommandLineBuilder.kt`, `compilers/64tass/adapters/out/gradle/build.gradle.kts`
     (`id("rbt.adapter.outbound.gradle")`)
   - Description: `TassAssembleAdapter(project) : TassAssemblePort` runs
     `project.exec { executable = "64tass" }`; `TassCommandLineBuilder` emits options first
     and the source path **last**, always emits `-o`. Verify every flag against the 64tass
     manual before coding.
   - Testing: unit test `TassCommandLineBuilderTest` (each option, ordering, defaults).
3. **Step 1.3**: Register modules
   - Files: `settings.gradle.kts` (add `:compilers:64tass`,
     `:compilers:64tass:adapters:out:gradle` next to lines 29-30),
     `infra/gradle/build.gradle.kts` (compileOnly pair next to lines 76-77, test-runtime
     pair next to lines 129-130 — the CLAUDE.md `compileOnly` rule)
   - Description: fat jar picks the modules up automatically via `bundledProjectJars`.
   - Testing: `./gradlew build` green.

**Phase 1 Deliverable**: New compiler subdomain compiles, is tested, ships inside the
plugin jar, and changes no behaviour.

### Phase 2: flows domain integration (step + config + mapper)
**Goal**: The flows domain knows the step; still not exposed in the DSL.

1. **Step 2.1**: Config + command + mapper
   - Files: `flows/src/main/kotlin/.../domain/config/ProcessorConfig.kt` (add `TassConfig`
     after `DasmConfig`, `ProcessorConfig.kt:130-146` as template),
     `.../config/AssemblyConfigMapper.kt` (add `TassCommand` data class after
     `DasmCommand`, lines 52-65), new `.../config/TassConfigMapper.kt`
     (mirror `DasmConfigMapper.kt:35-250`: `toTassCommand(s)`, source discovery via
     srcDirs/includes/excludes globs, output resolution — derived output extension is
     **format-aware**: `basename + ".prg"` when `outputFormat = "prg"`,
     `basename + ".bin"` when `"bin"`; never a fixed extension, so a raw binary is not
     misnamed `.prg`)
   - Testing: `TassConfigMapperTest` (explicit output, derived output, discovery, defines).
2. **Step 2.2**: Flows port + domain step
   - Files: new `flows/src/main/kotlin/.../usecase/port/TassAssemblyPort.kt`
     (mirror `DasmAssemblyPort.kt:35-52`), new `.../domain/steps/TassStep.kt`
     (mirror `DasmStep.kt:39-160`; taskType `"tass"`; `validate()`: ≥1 input, ≥1 output,
     inputs end `.asm`/`.s`, `outputFormat` in `{prg, bin}`, and `cpu` (when set) in the
     allowed 64tass set (`6502`, `65c02`, `65816`, `65dtv02`, `4510`, ... — finalized from
     the manual in Step 1.2) so a typo fails validation, not the exec)
   - Testing: `TassStepTest` in `flows/src/test/kotlin/.../steps/` (validation matrix,
     execute-with-mock-port, port-not-injected failure) — closing the gap DASM left
     (no `DasmStepTest` exists).

**Phase 2 Deliverable**: Flows domain + tests merge cleanly with no user-visible change.

### Phase 3: DSL, task, wiring, docs (user-visible feature)
**Goal**: The step is usable from Groovy and Kotlin build scripts and documented.

1. **Step 3.1**: DSL builder + FlowDsl overloads
   - Files: new `flows/adapters/in/gradle/src/main/kotlin/.../dsl/TassStepBuilder.kt`
     (mirror `DasmStepBuilder.kt:31-193`), `.../FlowDsl.kt` (add Kotlin lambda + Groovy
     `@DelegatesTo` Closure overloads next to `dasmStep`, `FlowDsl.kt:185-198`, using
     `bindClosure`), `flows/adapters/in/gradle/build.gradle.kts` (add
     `implementation(project(":compilers:64tass"))` next to line 15)
   - Testing: extend `FlowDslGroovyOverloadTest.kt` with the two step-overload tests
     (Closure + Kotlin lambda, artifact registration), add `TassStepBuilderTest`.
2. **Step 3.2**: Task + adapters + generator dispatch
   - Files: new `.../tasks/TassAssembleTask.kt` (mirror `DasmAssembleTask.kt:39-110`),
     new `.../assembly/TassPortAdapter.kt` + `.../assembly/TassCommandAdapter.kt`
     (mirror `DasmPortAdapter.kt:37-50`, `DasmCommandAdapter.kt:36-69`),
     `.../FlowTasksGenerator.kt` (nullable constructor param per §4 decision, `is TassStep ->`
     dispatch next to lines 182-187, injection block next to 230-240,
     `registerAdditionalInputFiles` overload next to 299-307, `configureOutputFiles`
     branch next to 315)
   - Testing: extend `FlowTasksGeneratorTest` with a TassStep dispatch/injection case.
3. **Step 3.3**: Plugin wiring
   - Files: `infra/gradle/src/main/kotlin/com/github/c64lib/gradle/RetroAssemblerPlugin.kt`
     (`wireFlows`: construct `TassAssembleUseCase(TassAssembleAdapter(project))` next to
     line 257, pass to `FlowTasksGenerator`, lines 267-275)
   - Testing: `./gradlew build`; manual smoke test (Step 6 Manual Testing).
4. **Step 3.4**: Documentation
   - Files: `doc/index.adoc` (new "64tass Step" section after the "dasm Step" section,
     lines 1371-1418, Groovy + Kotlin examples + property table),
     `doc/arc42/05_building_block_view.md:18,69` (diagram node + module rows),
     `doc/arc42/building-blocks/compilers.md` (use-case/port/adapter rows, outbound
     paragraph), `doc/arc42/building-blocks/flows.md:14,25,43` (step, port mapping, task),
     `doc/arc42/03_context_and_scope.md`, `doc/arc42/07_deployment_view.md` (native binary
     on PATH), `doc/arc42/12_glossary.md`
   - Description: per CLAUDE.md, arc42 pages must stay authoritative when a new domain/port
     is added.
   - Testing: docs build (documentation workflow renders AsciiDoc on master).

**Phase 3 Deliverable**: Complete, documented, user-visible 64tass support.

## 6. Testing Strategy

### Unit Tests
- `TassCommandLineBuilderTest` — every flag, option ordering (options before source), `-o`
  always present, boundary values.
- `TassAssembleUseCaseTest` — command unpacking into the port.
- `TassConfigMapperTest` — output resolution (explicit/derived), glob discovery, defines,
  additional inputs.
- `TassStepTest` — validation matrix (inputs/outputs/extensions/format), execute happy path
  with mock port, missing-port error.
- `TassStepBuilderTest` — builder defaults and every setter.
- `FlowDslGroovyOverloadTest` additions — Closure + Kotlin overloads register artifacts.
- `FlowTasksGeneratorTest` addition — TassStep → TassAssembleTask with injected use case.

### Integration Tests
- None automated in-repo (matches DASM/KickAss precedent — `project.exec` boundary is not
  integration-tested); covered by manual testing.

### Manual Testing
- Sample project with a minimal Turbo-Assembler-syntax `.asm`, `tassStep` in Groovy and in
  Kotlin DSL; run `./gradlew flows`, verify `.prg` output loads in VICE; rerun to verify
  `UP-TO-DATE`; run with 64tass absent from PATH to verify the failure message is clear.

## 7. Risks and Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| CLI flags coded from memory don't match real 64tass | High | Medium | Verify each flag against the 64tass manual (https://tass64.sourceforge.net/) during Step 1.2 before coding. Note: builder unit tests encode the author's belief, so they cannot catch a swapped flag (e.g. `-l` labels vs `-L` listing) — the manual smoke test against the real binary (Section 6) is **mandatory before the Phase 3 merge** |
| `64tass` not on PATH in consuming project | Medium | High | Same posture as dasm: documented prerequisite; exec failure surfaces the command line (printArgs pattern) |
| Merge conflicts with unmerged `feature/complete-dasm-support` (PLAN-0017) | Medium | High | That branch also modifies `RetroAssemblerPlugin.kt` (same `wireFlows` region), `infra/gradle/build.gradle.kts`, and `settings.gradle.kts` (verified via `git diff --stat develop...feature/complete-dasm-support`). Keep this plan's edits additive and adjacent-line-aware; decide merge order before Phase 1 (preferably merge the DASM branch first) and budget for manual conflict resolution in those three files. Plan-ID collision itself is avoided by using 0018 |
| `FlowTasksGenerator` constructor change breaks callers | Low | Low | Parameter is nullable with default; only positional caller (`RetroAssemblerPlugin`) updated in same commit |
| Missing `compileOnly` in `infra/gradle` → runtime `ClassNotFoundError` | High | Low | Explicit Step 1.3 per CLAUDE.md rule; manual smoke test exercises the wired path |
| Windows: executable resolution (`64tass.exe`) | Low | Low | `project.exec` resolves via PATH incl. `.exe` on Windows (same as dasm today) |

## 8. Documentation Updates

- [ ] `doc/index.adoc` — new step section (Groovy + Kotlin examples, property table)
- [ ] `doc/arc42/05_building_block_view.md` — compilers node + module list
- [ ] `doc/arc42/building-blocks/compilers.md` — use case, port, adapter rows + outbound note
- [ ] `doc/arc42/building-blocks/flows.md` — step subclass, port mapping, task list
- [ ] `doc/arc42/03_context_and_scope.md`, `07_deployment_view.md`, `12_glossary.md`
- [ ] Inline KDoc per flows conventions (3-5 lines per class)
- [ ] CLAUDE.md — no new patterns introduced; no update needed

## 9. Rollout Plan

1. Merge per phase (each phase is independently mergeable and behaviour-preserving until
   Phase 3 exposes the DSL).
2. Release as a minor version via the standard tag-driven `publish.yml` pipeline.
3. Monitor: GitHub issues from users on PATH/CLI-flag problems.
4. Rollback: the feature is additive and opt-in (only active when a build script uses the
   step); reverting the Phase 3 wiring commit disables it without touching other domains.

## 10. Revision History

| Date | Updated By | Changes |
|------|------------|---------|
| 2026-09-08 | AI Agent | Initial plan created from DASM-pattern codebase analysis |
| 2026-09-08 | AI Agent | Resolved all open questions (step name `tassStep`, extended config surface, string `outputFormat`) per user decisions |
| 2026-09-08 | AI Agent | Adversarial challenge run; revised merge-conflict risk row, format-aware output extension, cpu validation, mandatory smoke test. Status → accepted |
| 2026-09-08 | AI Agent | Executed all steps 1.1–3.4 (EXEC-0018); full build green; docs updated. Status → in progress (smoke test + merge pending) |

---

**Note**: This plan should be reviewed and approved before implementation begins.
