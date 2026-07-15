# 12. Glossary

Domain and technical terms used throughout this documentation. External systems referenced here appear in the context diagram ([§3](03_context_and_scope.md)) and the deployment view ([§7](07_deployment_view.md)); architectural terms are elaborated in [§8 Crosscutting Concepts](08_crosscutting_concepts.md).

## Toolchain & external systems

| Term | Definition |
|------|------------|
| **Kick Assembler** | A feature-rich cross-assembler for the MOS 65xx family, the plugin's primary ASM dialect. Distributed as a Java jar that the `dependencies` context downloads and version-pins (`DownloadKickAssemblerUseCase`). |
| **DASM** | A veteran macro cross-assembler for 6502-family CPUs; the emerging second compiler backend (`compilers:dasm`, `DasmAssembleUseCase`). Native binary expected on `PATH`. |
| **VICE** | The Versatile Commodore Emulator. Its `x64` emulator runs compiled 6502 code so that 64spec tests can execute against real machine behavior (`emulators:vice`). |
| **64spec** | A unit-testing framework for 6502 assembly: specs are themselves assembled and run inside VICE, reporting pass/fail (`testing:64spec`, `Run64SpecTestUseCase`). |
| **Exomizer** | A cross-platform compressor/cruncher for 6502 binaries. Produces self-decompressing or raw-crunched output (`crunchers:exomizer`, `CrunchMemUseCase`/`CrunchRawUseCase`). |
| **gt2reloc / GoatTracker** | GoatTracker is a C64 SID music tracker; gt2reloc relocates/packs its songs. Used by `processors:goattracker` (`PackSongUseCase`). |
| **Gradle Plugin Portal** | The public registry (`plugins.gradle.org`) where the plugin is published under id `com.github.c64lib.retro-assembler` (via `publish.yml`). |

## File formats

| Term | Definition |
|------|------------|
| **CTM** | CharPad's project/tile-map file format; input to `processors:charpad` (`ProcessCharpadUseCase`). |
| **SPD** | SpritePad's sprite-project file format; input to `processors:spritepad` (`ProcessSpritepadUseCase`). |
| **SNG** | GoatTracker's song file format; input to `processors:goattracker`. |
| **SID** | The Commodore 64 sound-chip and its associated music-data format; the musical output domain of GoatTracker assets. |
| **PRG** | The standard Commodore executable binary format (a 2-byte load address followed by data); typical assembled/crunched output. |

## Architecture terms

| Term | Definition |
|------|------------|
| **Bounded context / domain** | A self-contained business capability implemented as one or more Gradle modules under a top-level domain directory (`compilers`, `processors`, `flows`, …). See [§5](05_building_block_view.md). |
| **Hexagonal architecture (ports & adapters)** | The mandated structure separating a technology-free core (`src/domain`, `src/usecase`) from technology-specific edges (`adapters/in`, `adapters/out`). See [§8.1](08_crosscutting_concepts.md#81-hexagonal-architecture-ports--adapters). |
| **Port** | A plain Kotlin interface declared next to a use case that hides a technology concern; implemented by an adapter (e.g. `KickAssemblePort`, `CharpadPort`). |
| **Adapter** | A technology-specific implementation on the edge of a hexagon — inbound (Gradle tasks, DSL extensions, step builders) or outbound (file system, Gradle API, PNG, native process). |
| **Use case** | A `*UseCase.kt` class with a single public `apply(payload)` method representing one business operation. See [§8.2](08_crosscutting_concepts.md#82-use-case-per-operation-and-the-apply-contract). |
| **Flow** | A named pipeline in the `flows` DSL composed of ordered/parallel steps with declared inputs/outputs. |
| **Step** | A single unit of work within a flow (e.g. a CharPad processing step, a command step), extending `FlowStep`, with `inputs`/`outputs` used for dependency and up-to-date derivation. |
| **Composition root** | The single place where the object graph is wired — here, `RetroAssemblerPlugin.apply()`'s `afterEvaluate` block. See [§8.3](08_crosscutting_concepts.md#83-manual-dependency-injection). |
| **`compileOnly` rule** | The convention that every domain module must be a `compileOnly` dependency of `infra:gradle` so it can be wired; omission causes a runtime `ClassNotFoundException`. See [AD-7](09_architecture_decisions.md). |
| **Cruncher** | A compressor for 6502 binaries (here, Exomizer); the `crunchers` bounded context. |
| **Shared kernel** | Cross-domain code under `shared/` reused by domains instead of being duplicated. See [§8.4](08_crosscutting_concepts.md#84-shared-kernel). |
| **MOS 65xx / 6502** | The 8-bit microprocessor family (6502/6510/…) targeted by the assembled code; the C64's CPU is a 6510 in this family. |
