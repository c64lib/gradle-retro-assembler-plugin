# Feature: Complete DASM Support

**Plan ID**: PLAN-0017
**Issue**: #TBD (issue creation currently blocked — active `gh` account `MMALECKI_CGEMU` cannot create issues on c64lib; create and link once re-authenticated)
**Status**: implemented
**Challenge**: waived 2026-09-07
**Created**: 2026-09-07
**Last Updated**: 2026-09-07

## 1. Feature Description

### Overview
DASM support landed in PR #131 (feature #129) as a flows-only `dasmStep {}` that requires a
`dasm` binary pre-installed on `PATH`, and it shipped with zero unit tests. This plan completes
DASM as a supported assembler in three streams:

1. **Harden** — unit tests for all existing DASM classes, per the project's 70% coverage goal.
2. **Binary auto-download** — per-OS download of the dasm executable from GitHub releases,
   mirroring the KickAssembler jar download design, with `PATH` as fallback.
3. **First-class dialect** — make the legacy `asm` task able to drive DASM via
   `retroProject { dialect = ... }`.

### Requirements
- Existing `dasmStep {}` behaviour is unchanged for current users (PATH-based invocation keeps working).
- Unit tests cover: `DasmStep`, `DasmConfigMapper`, `DasmStepBuilder`, `DasmCommandLineBuilder`,
  `DasmAssembleUseCase`, `DasmAssembleAdapter` wiring, and plugin wiring of DASM flow tasks.
- `resolveDevDeps` can download a versioned dasm binary for the host OS (Windows/Linux/macOS),
  cache it under `{workDir}/asms/dasm/{version}/`, stamp the version, and skip re-download when current.
- Executable resolution order: explicitly configured path → downloaded binary → `dasm` on PATH.
- `retroProject { dialect = Dasm }` wires the `asm` task to DASM assembly of `srcDirs`/`includes` sources.
- 64spec (`asmSpec`/`test`) is KickAssembler-only; under `dialect = Dasm` this must degrade explicitly, not crash obscurely.

### Success Criteria
- `./gradlew build` green; DASM classes covered by unit tests (domain modules trending to 70%+).
- On a machine without dasm on PATH, a project with `dasmStep` + configured version assembles successfully after `resolveDevDeps`.
- A `build.gradle` with `dialect = Dasm` assembles `.asm` sources into output binaries via the `asm` task.
- User manual (`doc/index.adoc`) and arc42 docs describe the new capabilities.

## 2. Root Cause Analysis

Feature #129 delivered DASM only through the flows pipeline and deliberately skipped binary
management ("dasm is a system command-line tool... available via PATH", see
`compilers/dasm/src/main/kotlin/com/github/c64lib/rbt/compilers/dasm/domain/DasmAssemblerSettings.kt`).
No tests were written anywhere for it. The legacy `dialect` path was never generalized: the
`RetroAssemblerPlugin` unconditionally builds `KickAssemblerSettings` and wires KickAssembler
(`infra/gradle/src/main/kotlin/com/github/c64lib/gradle/RetroAssemblerPlugin.kt:122-131`), and
`AssemblerType` (deprecated) has no `Dasm` value and is never branched on.

### Current State
- `dasmStep {}` (flows DSL) → `DasmStep` → `DasmAssemblyPort` → `DasmPortAdapter` →
  `DasmAssembleUseCase` → `DasmAssembleAdapter` (`project.exec { executable = "dasm" }`).
- KickAssembler jar is auto-downloaded by `resolveDevDeps` from `c64lib/asm-ka` releases and
  cached in `.ra/asms/ka/{version}/` (`DownloadKickAssemblerUseCase`).
- dasm binary must be pre-installed; no version management; no tests for any DASM class.

### Desired State
- Same flows path, plus: optional auto-downloaded, versioned dasm binary per OS; `asm` task usable
  with DASM as dialect; full unit-test coverage of DASM code.

### Gap Analysis
- No download machinery for native (non-jar) per-OS binaries: needs OS/arch detection, archive
  extraction (zip/tar.gz), executable bit on Unix.
- `DasmAssemblerSettings` is a dummy marker; needs a real `pathToExecutable` setting threaded into
  `DasmAssembleAdapter`.
- No inbound Gradle adapter for DASM (`compilers/dasm/adapters/in/gradle` does not exist).
- No `Dasm` enum value; no dialect branching in plugin wiring.
- No tests.

## 3. Relevant Code Parts

### Existing Components
- **DasmAssembleUseCase / DasmAssembleCommand**: DASM use case (libDirs, defines map, outputFormat 1–3, list/symbol files, verboseness, errorFormat, strictSyntax, removeOnError, symbolTableSort)
  - Location: `compilers/dasm/src/main/kotlin/com/github/c64lib/rbt/compilers/dasm/usecase/DasmAssembleUseCase.kt`
  - Integration Point: settings-aware execution; download use case will live beside it.
- **DasmAssembleAdapter + DasmCommandLineBuilder**: native execution via `project.exec`, hardcoded `executable = "dasm"`
  - Location: `compilers/dasm/adapters/out/gradle/src/main/kotlin/com/github/c64lib/rbt/compilers/dasm/adapters/out/gradle/`
  - Integration Point: must accept a resolved executable path from settings (fallback `"dasm"`).
- **DownloadKickAssemblerUseCase**: the download/caching pattern to mirror (version stamp file, `force` flag)
  - Location: `compilers/kickass/src/main/kotlin/com/github/c64lib/rbt/compilers/kickass/usecase/DownloadKickAssemblerUseCase.kt:39-48`
- **ResolveDevDeps task**: inbound adapter running the KickAssembler download; DASM download hooks in here
  - Location: `compilers/kickass/adapters/in/gradle/src/main/kotlin/.../ResolveDevDeps.kt`
- **RetroAssemblerPlugin**: hardcoded KickAssembler settings construction and wiring; dialect never consulted
  - Location: `infra/gradle/src/main/kotlin/com/github/c64lib/gradle/RetroAssemblerPlugin.kt:122-131` (settings), `:196-215` (wireSources), `:245-289` (wireFlows, DASM use case at `:257`)
- **AssemblerType**: deprecated enum `{ KickAssembler, None }`
  - Location: `shared/domain/src/main/kotlin/com/github/c64lib/retroassembler/domain/AssemblerType.kt:27-31`
- **RetroAssemblerPluginExtension**: `dialect`, `dialectVersion`, `srcDirs`, `includes`, `excludes`, `libDirs`, `defines`, `outputFormat`
  - Location: `shared/gradle/src/main/kotlin/com/github/c64lib/rbt/shared/gradle/dsl/RetroAssemblerPluginExtension.kt`
- **Flows DASM classes**: `DasmStep`, `DasmConfigMapper`/`DasmCommand`, `DasmAssemblyPort`, `DasmStepBuilder`, `DasmPortAdapter`, `DasmCommandAdapter`, `DasmAssembleTask`, `FlowTasksGenerator` branch
  - Location: `flows/src/main/kotlin/com/github/c64lib/rbt/flows/...` and `flows/adapters/in/gradle/...`
  - Integration Point: primary test targets (mirror `AssembleStepTest`, Kotest BehaviorSpec).
- **shared/filedownload FileDownloader**: existing download helper used by both kickass and dependencies domains
  - Location: `shared/filedownload/`

### Architecture Alignment
- **Domain**: `compilers/dasm` (download use case, settings), `flows` (tests only), `shared/domain` (enum value), `infra/gradle` (wiring)
- **Use Cases**: new `DownloadDasmUseCase` (single `apply`); modify `DasmAssembleUseCase` only if settings threading requires it
- **Ports**: new `DownloadDasmPort`, `ReadVersionPort`/`SaveVersionPort` (dasm-owned copies, keeping domains decoupled), optional `UnpackArchivePort`
- **Adapters**: new `compilers/dasm/adapters/out/filedownload` (download+unpack), new `compilers/dasm/adapters/in/gradle` (legacy `asm`-path task and/or resolve hook); both registered in `settings.gradle.kts` and as `compileOnly` + `testImplementation` in `infra/gradle/build.gradle.kts` (CLAUDE.md rule)

### Dependencies
- dasm GitHub releases (`dasm-assembler/dasm`), latest packaged **2.20.14.1** — per-OS archives (Windows/macOS/Linux). Exact asset naming to be verified at implementation time.
- No new library dependencies expected (zip/tar handling exists in `dependencies` domain via `UntarDependencyAdapter`; reuse pattern, not the class, to keep domains decoupled).

## 4. Questions and Clarifications

### Self-Reflection Questions
- **Q**: Is there an existing generic compiler abstraction to plug DASM into?
  - **A**: No. Each assembler is its own hexagonal sub-domain; flows is the polymorphism point. The legacy path needs explicit branching in `RetroAssemblerPlugin`.
- **Q**: Where do assembler binaries get cached?
  - **A**: `{workDir}/asms/{tool}/{version}/` with a sibling `version` stamp file (`.ra` by default); mirror this for dasm.
- **Q**: Are there tests to mirror?
  - **A**: Yes — flows Kotest BehaviorSpec tests (`flows/src/test/kotlin/.../AssembleStepTest.kt`, builder tests under `flows/adapters/in/gradle/src/test/kotlin`), and `RetroAssemblerPluginTest` (ProjectBuilder) in `infra/gradle`.
- **Q**: Download source: upstream `dasm-assembler/dasm` releases directly, or a `c64lib/asm-dasm` mirror (consistent with `c64lib/asm-ka`)?
  - **A**: Upstream releases (user decision, 2026-09-07). No mirror to maintain; official provenance. Asset-naming drift is mitigated by pinning the URL template per verified version (Step 2.1).
- **Q**: Version property: reuse `dialectVersion` when `dialect = Dasm`, or a dedicated `dasmVersion` property?
  - **A**: Dedicated `dasmVersion` on `RetroAssemblerPluginExtension` (user decision, 2026-09-07). Works for flows-only projects; `dialectVersion` keeps meaning the KickAssembler version. Unset ⇒ no download, PATH fallback.
- **Q**: Behaviour of `asmSpec`/`test` when `dialect = Dasm`?
  - **A**: Skip with a logged warning (user decision, 2026-09-07) — `./gradlew build` stays green end-to-end under `dialect = Dasm`.
- **Q**: Executable resolution precedence, and an override property?
  - **A**: `dasmExecutable` (explicit) → downloaded versioned binary → `"dasm"` on PATH (user decision, 2026-09-07). Deterministic once a version is pinned.

### Unresolved Questions
(none — all resolved 2026-09-07)

### Design Decisions
- **Decision**: How the legacy `asm` task gains DASM support
  - **Options**: (A) add `Dasm` to the deprecated `AssemblerType` and branch in `RetroAssemblerPlugin`; (B) introduce a new non-deprecated dialect type and migrate
  - **Recommendation**: (A) — minimal surface, honours the existing deprecation plan (flows remain the strategic path; the enum dies in 2.0.0 regardless).
- **Decision**: Where the DASM binary resolution logic lives
  - **Options**: (A) in `DasmAssemblerSettings` (a real settings object with `pathToExecutable: File?`), resolved by the plugin at wiring time; (B) inside the adapter at execution time
  - **Recommendation**: (A) — keeps the adapter dumb, matches `KickAssemblerSettings` shape, testable without Gradle.
- **Decision**: Archive extraction implementation
  - **Options**: (A) dasm-owned `UnpackArchivePort` + adapter using Java zip/commons-compress; (B) reuse `dependencies` domain's `UntarDependencyAdapter`
  - **Recommendation**: (A) — cross-domain reuse of adapters violates the hexagonal boundaries; copy the pattern, not the class.
- **Decision**: Download source
  - **Chosen**: Upstream `dasm-assembler/dasm` GitHub releases
  - **Rationale**: No mirror maintenance; official provenance; URL template pinned per verified version.
- **Decision**: Version configuration
  - **Chosen**: Dedicated `dasmVersion` property on `RetroAssemblerPluginExtension` (unset ⇒ no download)
  - **Rationale**: Unambiguous alongside `dialectVersion` (KickAssembler); usable by flows-only projects.
- **Decision**: 64spec under `dialect = Dasm`
  - **Chosen**: `asmSpec`/`test` skip with a logged warning
  - **Rationale**: keeps `./gradlew build` green; 64spec remains a documented KickAssembler-only capability.
- **Decision**: Executable resolution
  - **Chosen**: `dasmExecutable` property → downloaded `{workDir}/asms/dasm/{version}/` binary → `"dasm"` on PATH
  - **Rationale**: explicit override for power users; deterministic builds once a version is pinned; unchanged behaviour for current PATH users.

### Adversarial Challenge
- **Status**: waived 2026-09-07 — declined at acceptance
- **Findings**: —

## 5. Implementation Plan

### Phase 1: Harden existing DASM code (tests only)
**Goal**: Full unit-test coverage of the already-shipped DASM classes; safe to merge alone.

1. **Step 1.1**: Flows domain tests
   - Files: `flows/src/test/kotlin/com/github/c64lib/rbt/flows/domain/steps/DasmStepTest.kt`, `.../config/DasmConfigMapperTest.kt`
   - Description: Mirror `AssembleStepTest` (Kotest BehaviorSpec, `IsolationMode.InstancePerTest`): validation ranges (outputFormat 1–3, verboseness 0–4, errorFormat 0–2, symbolTableSort 0–1), port validation, input/output resolution, mapper field mapping.
   - Testing: `./gradlew :flows:test`
2. **Step 1.2**: Flows inbound adapter tests
   - Files: `flows/adapters/in/gradle/src/test/kotlin/.../dsl/DasmStepBuilderTest.kt`, `.../assembly/DasmCommandAdapterTest.kt`
   - Description: builder produces correct `DasmStep` (defaults + all knobs, Groovy-relevant overloads), command adapter maps flows `DasmCommand` → compiler `DasmAssembleCommand`.
   - Testing: `./gradlew :flows:adapters:in:gradle:test`
3. **Step 1.3**: Compiler module tests
   - Files: `compilers/dasm/src/test/kotlin/.../usecase/DasmAssembleUseCaseTest.kt`, `compilers/dasm/adapters/out/gradle/src/test/kotlin/.../DasmCommandLineBuilderTest.kt`
   - Description: use case delegates to port with the command untouched; command-line builder emits `-I/-D/-f/-o/-l/-s/-v/-E/-S/-R/-T` correctly, source first, out-of-range values silently omitted (current behaviour — assert it deliberately).
   - Testing: `./gradlew :compilers:dasm:test :compilers:dasm:adapters:out:gradle:test`
4. **Step 1.4**: Plugin wiring test
   - Files: `infra/gradle/src/test/kotlin/com/github/c64lib/gradle/RetroAssemblerPluginTest.kt` (extend)
   - Description: a `flows { flow(...) { dasmStep(...) } }` project registers the `flow...Step...` task and injects the DASM use case.
   - Testing: `./gradlew :infra:gradle:test`

**Phase 1 Deliverable**: Tests only; no production code changes. Mergeable immediately.

### Phase 2: DASM binary auto-download
**Goal**: Versioned, cached, per-OS dasm binary via `resolveDevDeps`; PATH remains the fallback.

1. **Step 2.1**: Verify release asset naming — **done 2026-09-07**
   - Files: (research) — findings recorded here
   - Findings: since **v2.20.16** releases use tag `v{version}` and uniform assets
     `dasm-v{version}-{windows|macos|linux}.zip`, each zip containing a single root binary
     (`dasm.exe`/`dasm`); v2.20.17 is current. Older releases (≤ 2.20.14.1) use mixed
     tar.gz/arch-suffixed naming and are **not supported** for auto-download (PATH-only).
     Pinned URL template:
     `https://github.com/dasm-assembler/dasm/releases/download/v{version}/dasm-v{version}-{os}.zip`.
   - Testing: all three v2.20.17 assets downloaded and inspected.
2. **Step 2.2**: Domain: settings + download use case
   - Files: `compilers/dasm/src/main/kotlin/.../domain/DasmAssemblerSettings.kt` (replace dummy with `pathToExecutable: File?`, `version: SemVer?`), `.../usecase/DownloadDasmUseCase.kt`, `.../usecase/port/{DownloadDasmPort,ReadVersionPort,SaveVersionPort,UnpackArchivePort}.kt`
   - Description: `DownloadDasmUseCase.apply(command)` — OS/arch → asset URL, download to `{workDir}/asms/dasm/{version}/`, unpack, mark executable (Unix), stamp version; skip when stamp matches and `!force`.
   - Testing: unit tests with mocked ports (URL selection per OS, skip logic, force).
3. **Step 2.3**: Outbound adapters
   - Files: new module `compilers/dasm/adapters/out/filedownload` (`DownloadDasmAdapter`, `UnpackArchiveAdapter`, version adapters); `settings.gradle.kts`; `infra/gradle/build.gradle.kts` (compileOnly + testImplementation)
   - Description: implement ports with `shared/filedownload` `FileDownloader` and JDK zip/tar handling.
   - Testing: adapter unit tests where feasible; manual `resolveDevDeps` run per OS.
4. **Step 2.4**: Executable resolution + wiring
   - Files: `compilers/dasm/adapters/out/gradle/DasmAssembleAdapter.kt` (accept settings; use resolved executable, fallback `"dasm"`), `infra/gradle/.../RetroAssemblerPlugin.kt` (build `DasmAssemblerSettings`, extend `resolveDevDeps` wiring to trigger DASM download when `dasmVersion` is set), `shared/gradle/.../RetroAssemblerPluginExtension.kt` (new `dasmVersion` and `dasmExecutable` properties)
   - Description: thread settings through `wireFlows` (and later `wireSources`); resolution order `dasmExecutable` → downloaded `{workDir}/asms/dasm/{dasmVersion}/` binary → PATH; download only when `dasmVersion` is configured.
   - Testing: wiring tests in `RetroAssemblerPluginTest`; e2e on Windows (this machine) with no dasm on PATH.

**Phase 2 Deliverable**: Auto-download behind explicit version opt-in; PATH behaviour unchanged when unconfigured. Mergeable.

### Phase 3: First-class dialect for the legacy `asm` task
**Goal**: `retroProject { dialect = Dasm }` assembles sources via the `asm` task.

1. **Step 3.1**: Enum + extension
   - Files: `shared/domain/.../AssemblerType.kt` (add `Dasm`), `shared/gradle/.../RetroAssemblerPluginExtension.kt` (doc; version property per Phase 2 decision)
   - Description: additive enum change; keep `@Deprecated` annotation and 2.0.0 note.
   - Testing: compiles; extension defaults unchanged.
2. **Step 3.2**: DASM inbound adapter module
   - Files: new module `compilers/dasm/adapters/in/gradle` (`Assemble`-equivalent task `DasmAssemble.kt` using `srcDirs`/`includes`/`excludes` file-tree pattern from kickass `Assemble.kt`, deriving `-o` output per source and `-f` from `OutputFormat`); `settings.gradle.kts`; `infra/gradle/build.gradle.kts`
   - Description: per-source assembly loop calling `DasmAssembleUseCase`; map extension `defines` (List) → dasm defines (Map) explicitly.
   - Testing: task unit test (ProjectBuilder), e2e sample project.
3. **Step 3.3**: Plugin dialect branching
   - Files: `infra/gradle/.../RetroAssemblerPlugin.kt` (`wireSources` branches on `extension.dialect`; `wireSpecAndTest` makes `asmSpec`/`test` skip with a logged warning under `Dasm`; `resolveDevDeps` downloads the right tool)
   - Description: `KickAssembler`/`None` keep today's behaviour exactly; `Dasm` wires the new task; 64spec tasks log "64spec requires KickAssembler — skipping" and do nothing.
   - Testing: `RetroAssemblerPluginTest` cases per dialect value.
4. **Step 3.4**: Documentation
   - Files: `doc/index.adoc` (dialect section + dasm auto-download), `doc/arc42/building-blocks/compilers.md`, `doc/arc42/05_building_block_view.md`, `doc/arc42/08_crosscutting_concepts.md`, `CLAUDE.md` (project overview sentence: no longer Kick-only)
   - Description: keep arc42 authoritative per CLAUDE.md architecture-change rule.
   - Testing: docs build (documentation workflow) renders.

**Phase 3 Deliverable**: Full first-class DASM dialect; docs updated. Mergeable.

## 6. Testing Strategy

### Unit Tests
- Kotest BehaviorSpec across flows/compilers modules as listed per phase; mocked ports throughout; no network in tests (download use case tested against fake ports).

### Integration Tests
- `RetroAssemblerPluginTest` (ProjectBuilder): task registration and use-case injection per dialect and per flows configuration.

### Manual Testing
- Windows (this machine): sample project using `dasmStep` with auto-downloaded 2.20.14.1; sample project with `dialect = Dasm` running `./gradlew asm`.
- CI (Linux) exercises build + tests on PR.

## 7. Risks and Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Release asset names/layout differ per version or lack an arch we need (e.g. Apple Silicon, ARM Linux) | Medium | Medium | Verify assets first (Step 2.1); fall back to PATH with a clear message for unsupported platforms |
| Behaviour change for existing dasmStep users (executable resolution) | High | Low | Download strictly opt-in via version property; default resolution unchanged (`"dasm"` on PATH) |
| Legacy-path branching destabilises KickAssembler wiring | High | Low | Branch only inside `wireSources`/`wireSpecAndTest`; dialect `KickAssembler`/`None` paths byte-for-byte equivalent; wiring tests for all dialects |
| Deprecated `AssemblerType` removed in 2.0.0 strands the dialect feature | Medium | Medium | Documented decision: flows is the strategic path; dialect support is a 1.x bridge |
| Native binary trust/security (downloading executables) | Medium | Low | Pin exact version + HTTPS GitHub releases; document provenance; consider checksum pinning |

## 8. Documentation Updates

- [ ] `doc/index.adoc` — dialect = Dasm section, dasm version/auto-download, updated dasmStep notes
- [ ] `doc/arc42/building-blocks/compilers.md` + `05_building_block_view.md` + `08_crosscutting_concepts.md`
- [ ] `CLAUDE.md` — project overview (multi-dialect), new module compileOnly entries implied by rule
- [ ] Inline KDoc on new use case/ports (concise, per flows doc conventions)

## 9. Rollout Plan

1. Merge phases independently to `develop` (each is green and self-contained); release together as the next minor (1.9.0) alongside the already-merged flows DASM support.
2. Monitor GitHub issues for dasm download failures on exotic platforms; PATH fallback is the escape hatch.
3. Rollback: each phase is revertable in isolation; Phase 2/3 are opt-in by configuration, so disabling is config-level (unset version / dialect).

## 10. Revision History

| Date | Updated By | Changes |
|------|------------|---------|
| 2026-09-07 | AI Agent | Initial plan created from codebase analysis |
| 2026-09-07 | AI Agent | All four unresolved questions resolved by user (upstream download, `dasmVersion`/`dasmExecutable` properties, 64spec skip-with-warning, configured→downloaded→PATH resolution); decisions and steps 2.4/3.3 updated accordingly |
| 2026-09-07 | AI Agent | Status → accepted; adversarial challenge waived at acceptance; feature branch deferred to execution |
| 2026-09-07 | AI Agent | Status → implemented: all steps 1.1–3.4 executed on `feature/complete-dasm-support`, full `./gradlew build` green. Step 2.1 findings recorded (v2.20.16+ zip scheme, default 2.20.17). Deviations and per-step evidence in [EXEC-0017](EXEC-0017_complete-dasm-support.md); notable: two real bugs caught by new tests (darwin→WINDOWS detection; configuration-time crash on `dialectVersion="latest"` under Dasm dialect), both fixed |

---

**Note**: This plan should be reviewed and approved before implementation begins.
