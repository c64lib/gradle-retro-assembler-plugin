# 8. Crosscutting Concepts

These are the conventions and mechanisms that apply across every bounded context. They are what make the many domain modules feel like one system, and they are the rules a contributor or AI agent must respect when extending it. Each concept below cites at least one real class or path so it can be verified against the code.

## 8.1 Hexagonal architecture (ports & adapters)

Every domain module is split into a technology-free core and technology-specific edges:

```
<domain>/
├── src/
│   ├── domain/     # value types, entities, domain exceptions
│   └── usecase/    # *UseCase classes + port interfaces (usecase/port)
└── adapters/
    ├── in/<tech>/  # inbound: Gradle tasks, DSL extensions, step builders
    └── out/<tech>/ # outbound: file system, Gradle API, PNG, native processes
```

**The rule:** no technology type (Gradle `Project`/`Task`, file-system wiring, PNG library, process spawning) may appear in `src/domain` or `src/usecase`. Such code lives behind a **port** — a plain Kotlin interface declared next to the use case — and is implemented by an adapter. This is what lets use cases be unit-tested without a Gradle project, and is enforced by module boundaries plus review. See the per-domain hexagon diagrams in [§5](05_building_block_view.md) and the building-block pages under [`building-blocks/`](building-blocks/).

## 8.2 Use-case-per-operation and the `apply` contract

Each unit of business behavior is a class named `*UseCase.kt` with a **single public method `apply(payload)`**, optionally returning a result object. Examples: `KickAssembleUseCase`, `ProcessCharpadUseCase` ([`processors/charpad/.../usecase/ProcessCharpadUseCase.kt`](../../processors/charpad/src/main/kotlin/com/github/c64lib/rbt/processors/charpad/usecase/ProcessCharpadUseCase.kt)), `ResolveGitHubDependencyUseCase`, `CrunchMemUseCase`. This gives one predictable entry point per capability instead of multi-method services with implicit call ordering.

## 8.3 Manual dependency injection

There is **no DI framework** (no Spring, Dagger, Koin). All wiring happens in one place — `RetroAssemblerPlugin.apply()` ([`infra/gradle/.../RetroAssemblerPlugin.kt`](../../infra/gradle/src/main/kotlin/com/github/c64lib/gradle/RetroAssemblerPlugin.kt)) — inside `project.afterEvaluate`, where each use case is constructed with its concrete adapter(s) and bound to a Gradle task via explicit constructor calls. The dependency graph is therefore grep-able rather than hidden behind annotations/reflection. The step-by-step wiring is diagrammed in [§6 Runtime View](06_runtime_view.md).

A direct consequence is the **`compileOnly` rule**: `infra:gradle` declares every domain module as a `compileOnly` dependency so it can see and wire their use cases. Adding a module without registering it here causes a `ClassNotFoundException` at runtime (stated in [`CLAUDE.md`](../../CLAUDE.md)).

## 8.4 Shared kernel

Cross-domain code lives under `shared/` and is depended on by the domains rather than duplicated:

| Package | Responsibility |
|---------|----------------|
| `shared:domain` | Common value types and the domain exceptions (§8.5) |
| `shared:gradle` | DSL extension base types, task-name constants ([`Tasks.kt`](../../shared/gradle/src/main/kotlin/com/github/c64lib/rbt/shared/gradle/Tasks.kt)), and the `fllter` byte-stream filters (interleaver/nybbler) |
| `shared:processor` | Streaming `Processor` / `OutputProducer` abstractions (§8.7) |
| `shared:binary-utils` | Binary/byte helpers |
| `shared:filedownload` | File download primitives used by `dependencies` and Kick Assembler retrieval |
| `shared:testutils` | Test-only helpers |

See [`building-blocks/shared.md`](building-blocks/shared.md).

## 8.5 Error handling

Two layers of exceptions, both with consistent conventions:

- **Shared domain exceptions** ([`shared/domain/.../`](../../shared/domain/src/main/kotlin/com/github/c64lib/rbt/shared/domain/)): `IllegalConfigurationException`, `IllegalInputException`, `OutOfDataException` — thrown by use cases/processors for bad configuration, bad input data, and stream underrun respectively.
- **Flows exceptions** (in the `flows` domain): `StepValidationException` and `StepExecutionException` ([`Flow.kt`](../../flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/Flow.kt)) and `FlowValidationException` ([`FlowDependencyGraph.kt`](../../flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/FlowDependencyGraph.kt)). The step exceptions carry a `stepName` and **prepend the step name** to the message (`Step '<name>': <message>`), so failures in a pipeline are attributable to a specific step. Validation exceptions are raised before execution; execution exceptions wrap runtime failures.

## 8.6 Parallel execution and incremental builds

Two related performance concepts, both delegating to Gradle rather than hand-rolling:

- **No custom threading** anywhere (mandated by `CLAUDE.md`). Where parallelism within a single task is needed, the **Gradle Workers API** is used.
- **Flows auto-parallelism**: `FlowTasksGenerator` ([`flows/adapters/in/gradle/.../FlowTasksGenerator.kt`](../../flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/FlowTasksGenerator.kt)) derives Gradle task `dependsOn` and input/output relationships from the domain's `FlowDependencyGraph` and each step's declared `inputs`/`outputs`. Under `--parallel` (or `org.gradle.parallel=true`), Gradle's own scheduler runs independent steps/flows concurrently, and its up-to-date checking gives **incremental builds** for free — a step is skipped when its declared inputs and outputs are unchanged. The design history is [PLAN-0001](../../plans/PLAN-0001_pipeline-dsl-parallel-execution.md).

## 8.7 Streaming processor abstraction

Asset processors (charpad, spritepad, image, goattracker) share the `shared:processor` streaming model: an `InputByteStream` is consumed by a `Processor` that emits to one or more `OutputProducer`/`Output` sinks ([`shared/processor/.../`](../../shared/processor/src/main/kotlin/com/github/c64lib/rbt/shared/processor/)). This keeps large binary asset transformation memory-bounded and composable, and is why asset extraction can fan a single source file out to several typed outputs.

## 8.8 DSL builder patterns (`useFrom()` / `useTo()`)

The flows Pipeline DSL provides `useFrom(index)` and `useTo(index)` shortcuts in `CommandStepBuilder` so input/output paths are declared once in `from(...)`/`to(...)` and then referenced by name in `param()`/`option()`/`withOption()`. This keeps a single source of truth for paths (DRY) and prevents copy-paste errors — see the examples in [`CLAUDE.md`](../../CLAUDE.md) ("DSL Builder Patterns") and [`building-blocks/flows.md`](building-blocks/flows.md).

## 8.9 Task naming conventions

Task names and their string constants live in `shared:gradle` [`Tasks.kt`](../../shared/gradle/src/main/kotlin/com/github/c64lib/rbt/shared/gradle/Tasks.kt). Flow tasks follow a fixed naming scheme so they are discoverable:

- Top-level aggregate: `flows` (`TASK_FLOWS`)
- Per-flow aggregate: `flow<FlowNameCapitalized>` (e.g. `flowPreprocessing`)
- Per-step: `flow<FlowName>Step<StepName>`

The full task graph and its dependencies are tabulated in [`building-blocks/infra.md`](building-blocks/infra.md).

---

> **For maintainers and agents:** when the architecture changes (a new domain, a new port, a changed wiring or task-dependency rule), update the affected building-block page in [§5](05_building_block_view.md) and the relevant concept above so this document stays authoritative.
