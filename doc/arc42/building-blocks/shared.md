# Building Block: shared (shared kernel)

[← Back to §5 Building Block View](../05_building_block_view.md)

## Purpose

`shared` is the **shared kernel** — cross-domain building blocks used by every bounded context. It contains no orchestration or business use cases of its own; it provides value types, common exceptions, Gradle DSL scaffolding, a streaming processor abstraction, binary/file utilities, and test helpers.

## Modules

| Module | Package root | Contents |
|--------|--------------|----------|
| `shared:domain` | `...shared.domain` | Value types (`SemVer`, `Color`, `OutputFormat`, `AssemblerType`, `Dependency`/`DependencyVersion`, `Axis`) and the shared domain exceptions `IllegalConfigurationException`, `IllegalInputException`, `OutOfDataException` |
| `shared:gradle` | `...shared.gradle` | Task-name constants (`Tasks.kt`), task groups (`TaskGroups.kt`), the plugin DSL extensions (`RetroAssemblerPluginExtension`, `PreprocessingExtension`, and the per-processor `*Extension` classes), and the interleaver/nybbler binary filters |
| `shared:processor` | `...shared.processor` | Streaming processing abstraction: `Processor`, `InputByteStream`/`FisInput`, `OutputProducer`/`Output`/`OutputBuffer`, `BinaryProducer`/`ScalableBinaryProducer` |
| `shared:binary-utils` | `...shared.binary` | Binary manipulation helpers |
| `shared:filedownload` | `...shared.filedownload` | `FileDownloader` — HTTP download helper used by `dependencies` and `compilers:kickass` |
| `shared:testutils` | `...shared.testutils` | Shared test fixtures/utilities |

## Key concepts contributed to the architecture

- **Shared domain exceptions** — `IllegalConfigurationException`, `IllegalInputException`, `OutOfDataException` are the common error vocabulary across domains (see [§8 Crosscutting Concepts](../08_crosscutting_concepts.md)).
- **Streaming processor abstraction** — `Processor` reads an `InputByteStream` and fans results out to a collection of `OutputProducer`s. The `charpad` and `spritepad` processors and the image writers build on this (see [processors.md](processors.md)).
- **Task constants** — `Tasks.kt` centralizes task names (`TASK_ASM`, `TASK_PREPROCESS`, `TASK_FLOWS`, …) so the composition root and adapters agree on the task graph (see [infra.md](infra.md)).
- **DSL extensions** — the user-facing `retroProject { }` / `preprocess { }` configuration blocks are declared here as Gradle extension classes, then populated by the plugin and read by the in-adapters.

## Ports & adapters

`shared` defines **no ports and no adapters** — it is a kernel, not a hexagon. It is a plain dependency of every other context.
