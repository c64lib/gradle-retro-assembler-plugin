# 1. Introduction and Goals

## 1.1 Requirements Overview

The **Gradle Retro Assembler Plugin** (`com.github.c64lib.retro-assembler`) adds Gradle build capability for Assembly projects targeting the MOS 65xx family of microprocessors, primarily the Commodore 64. It automates a workflow that retro-computing developers would otherwise perform manually: downloading and version-pinning an assembler toolchain, resolving external asset/library dependencies, preprocessing graphics and music assets, compiling sources, and running automated tests against an emulator.

Core capabilities, derived from the plugin's task graph and DSL surface (see [§5 Building Block View](05_building_block_view.md) for the full domain inventory):

- **Compilation** of 6502/65xx assembly sources — currently [Kick Assembler](http://www.theweb.dk/KickAssembler/) (`compilers:kickass`), with an emerging DASM backend (`compilers:dasm`)
- **Dependency management** — downloading and unpacking external libraries/assets hosted on GitHub (`dependencies`)
- **Asset preprocessing** — converting CharPad, SpritePad, GoatTracker, and raster image files into assembler-consumable data (`processors:*`)
- **Compression** of binary output via Exomizer (`crunchers:exomizer`)
- **Automated testing** — running [64spec](https://github.com/segfaulti/64spec)-based unit specs against real 6502 code inside the [VICE](https://vice-emu.sourceforge.io/) emulator (`emulators:vice`, `testing:64spec`)
- **Pipeline orchestration** — a DSL (`flows`) for composing multi-step, dependency-tracked, incrementally-built asset/compile pipelines with automatic parallel execution

The plugin is published to the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/com.github.c64lib.retro-assembler) and is consumed by C64 hobbyist/demoscene projects as a standard Gradle build plugin dependency — see the [User's Manual](../index.adoc) for the consumer-facing DSL reference.

## 1.2 Quality Goals

| Priority | Quality Goal | Motivation |
|----------|-------------|------------|
| 1 | **Extensibility** | New assembler dialects, asset processors, and crunchers must be addable as self-contained domains without touching unrelated code — the hexagonal, domain-per-module structure exists specifically to keep this cheap. |
| 2 | **Build correctness & reproducibility** | Assembled output must be deterministic and version-pinned (assembler jar, dependency versions) so retro projects build identically across machines and CI. |
| 3 | **Incremental build performance** | Large asset/source trees must not be fully reprocessed on every build; the `flows` subdomain derives Gradle task inputs/outputs from declared step inputs/outputs to enable up-to-date checks and parallel execution. |
| 4 | **Testability** | Automated regression testing of 6502 assembly itself (not just the Kotlin plugin code) via 64spec + VICE emulation, so consumer projects can catch regressions before hardware/real-emulator testing. |
| 5 | **Maintainer/agent legibility** | The codebase must be navigable by both human contributors and AI coding agents — enforced module conventions (`*UseCase.kt`, ports, `adapters/in`/`adapters/out`) are a deliberate legibility mechanism, not just style. |

## 1.3 Stakeholders

| Role | Expectations |
|------|--------------|
| **Retro/demoscene developers** (plugin consumers) | A working, documented Gradle DSL to build C64 assembly projects without hand-rolling Makefiles or shell scripts; stable releases on the Plugin Portal. |
| **Plugin contributors** | A consistent architecture (hexagonal, domain-per-module) that makes it obvious where new capability belongs and how to test it in isolation. |
| **Maintainer** (project owner) | Sustainable maintenance: coverage targets (≥70% domain modules), static analysis (Detekt), and a plugin that keeps working across new Gradle/Kotlin versions. |
| **AI coding agents** (Claude Code and similar) | Machine-readable architectural conventions (`CLAUDE.md`, `doc/kb/`, this arc42 set) so automated changes follow the same hexagonal rules as human-authored ones. |
| **External toolchains** (indirect stakeholders) | Kick Assembler, DASM, VICE, Exomizer, gt2reloc — the plugin must track their invocation contracts (CLI flags, file formats, version compatibility) accurately. |
