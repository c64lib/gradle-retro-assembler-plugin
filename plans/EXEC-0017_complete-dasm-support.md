# Execution Log: Complete DASM Support

**Exec ID**: EXEC-0017
**Plan**: [PLAN-0017](PLAN-0017_complete-dasm-support.md)
**Issue**: #TBD (creation blocked — active `gh` account cannot create issues on c64lib)
**Started**: 2026-09-07
**Last Updated**: 2026-09-07
**State**: completed

## 1. Execution Sessions

<!-- One subsection per execution run (a /execute invocation). Append; never rewrite past sessions. -->

### Session 1 — 2026-09-07

- **Scope**: all (1.1–3.4)
- **Mode**: autonomous
- **Outcome**: completed — full `./gradlew build` green (298 tasks)

| Step | Result | Verification | Notes |
|------|--------|--------------|-------|
| 1.1 | completed | `:flows:test` green (BUILD SUCCESSFUL) | `DasmStepTest.kt`, `DasmConfigMapperTest.kt` |
| 1.2 | completed | `:flows:adapters:in:gradle:test` green | `DasmStepBuilderTest.kt`, `DasmCommandAdapterTest.kt` |
| 1.3 | completed | `:compilers:dasm:test`, `:compilers:dasm:adapters:out:gradle:test` green | `DasmAssembleUseCaseTest.kt`, `DasmCommandLineBuilderTest.kt` |
| 1.4 | completed | `:infra:gradle:test` green | `RetroAssemblerPluginTest` extended with dasm flow wiring scenario |
| 2.1 | completed | Live inspection of dasm-assembler/dasm releases + all three v2.20.17 zips | See deviation #2: modern scheme ≥ 2.20.16, default 2.20.17 |
| 2.2 | completed | `DownloadDasmUseCaseTest`, `DasmOperatingSystemTest` (mocked ports; caught real darwin→WINDOWS detection bug) | `DasmAssemblerSettings` (real), `DasmOperatingSystem`, `DownloadDasmUseCase` + 4 ports |
| 2.3 | completed | `UnpackZipAdapterTest` (zip layout, nesting, zip-slip rejection) | New module `compilers/dasm/adapters/out/filedownload`; version adapters in `adapters/out/gradle`; settings.gradle.kts + infra deps updated |
| 2.4 | completed | `RetroAssemblerPluginTest`: `resolveDasm` registration + dasm flow step dependency | `dasmVersion`/`dasmExecutable` extension props; `ResolveDasm` task in new `adapters/in/gradle` module; settings threaded through `wireFlows` |
| 3.1 | completed | Full build (see 3.3 row) | `AssemblerType.Dasm` added (enum stays deprecated) |
| 3.2 | completed | `DasmAssembleTest` (defines parsing, PRG→-f1/.prg, BIN→-f3/.bin, source discovery) | `DasmAssemble` task in `compilers/dasm/adapters/in/gradle` |
| 3.3 | completed | `RetroAssemblerPluginTest` Dasm-dialect scenarios (asm task type, dependency graph, 64spec no-ops) | `wireSources`/`wireSpecAndTest` branch on dialect; asm↛resolveDevDeps under Dasm |
| 3.4 | completed | Full build; docs rendered as Markdown/AsciiDoc | `doc/index.adoc`, arc42 `compilers.md`/`05`/`infra.md`, `CLAUDE.md` overview |

## 2. Deviations from Plan

<!-- Every departure from the plan as written. "None" if the plan was followed exactly. -->

| # | Step | Deviation | Reason | Impact |
|---|------|-----------|--------|--------|
| 1 | — | Feature branch created as `feature/complete-dasm-support` without issue number, and `git pull --rebase` was skipped during branch creation | GitHub issue creation blocked (EMU account lacks permission); pull blocked by uncommitted plan files, but `develop` was already up to date with origin | Branch may be renamed to `feature/{issue}-complete-dasm-support` once the issue exists |
| 3 | 2.3/2.4 | `compilers/dasm/adapters/in/gradle` module created in Phase 2 (with `ResolveDasm` task) instead of Phase 3 | The download-trigger task is an inbound Gradle adapter and needs a home before Phase 3 | Phase 3 adds `DasmAssemble` to the already-existing module |
| 4 | 1.x | `./gradlew spotlessApply` run to fix ktfmt violations in the new test files (twice during the session) | Full builds failed on `spotlessKotlinCheck` for newly written sources | Formatting-only rewrites; no logic change |
| 5 | 3.3 | Plugin now tolerates unparseable `dialectVersion` (default `"latest"`) under `dialect = Dasm` via `SemVer.fromStringOrNull ?: SemVer(0, 0)` | New dialect test exposed a configuration-time crash (`Cannot determine version from "latest"`) that would hit every real Dasm-dialect project | KickAssembler/None paths unchanged; recorded as OBSERVATION-20260907-190845-502159 |
| 6 | 2.3/3.2 | `--add-opens java.base/java.lang=ALL-UNNAMED` added to test JVMs of both new dasm adapter modules | `ProjectBuilder`-based tests fail on JDK 9+ module access without it (same flag already present in `infra/gradle`) | Build-config-only change |
| 2 | 2.1 | Auto-download targets dasm ≥ 2.20.16 (default 2.20.17), not 2.20.14.1 as the plan assumed | Verified live: since v2.20.16 releases use tag `v{version}` and uniform assets `dasm-v{version}-{windows\|macos\|linux}.zip`, each zip containing a single root binary (`dasm.exe`/`dasm`); older releases use mixed tar.gz/arch-suffixed naming | Simpler design: zip-only extraction (JDK `java.util.zip`), no tar.gz support, no arch detection; older versions remain PATH-only |

## 3. Follow-ups

<!-- Work discovered during execution that is out of scope. "None" if empty. -->

- Create the GitHub issue ("Complete DASM support: tests, binary auto-download, first-class dialect") once `gh` is re-authenticated with an account that has access; link it in PLAN-0017/EXEC-0017 headers and the index.
