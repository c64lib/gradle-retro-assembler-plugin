# Building Block: infra

[← Back to §5 Building Block View](../05_building_block_view.md)

## Purpose

`infra:gradle` is the **composition root** and the actual Gradle plugin. It is the single module the user applies (`com.github.c64lib.retro-assembler`). It owns no business logic; its job is to:

1. Register the plugin DSL extensions (`retroProject`, `preprocess`, `flows`).
2. In `afterEvaluate`, instantiate every use case with its concrete adapters (manual constructor injection) and create the Gradle tasks.
3. Wire the task graph (`preprocess` → `asm`; deps + flows → `asm`; `asm` + spec/test → `build`).

Entry point: `RetroAssemblerPlugin` — `infra/gradle/src/main/kotlin/com/github/c64lib/gradle/RetroAssemblerPlugin.kt`.

## The `compileOnly` wiring rule

Because `infra:gradle` must *see* every domain's use cases and adapters to wire them, it declares **all domain modules as `compileOnly` dependencies**. Adding a new module without adding it here causes a `ClassNotFoundException` at runtime. This rule is stated in [`CLAUDE.md`](../../../CLAUDE.md) and repeated in [§8 Crosscutting Concepts](../08_crosscutting_concepts.md).

## Manual dependency injection

There is **no DI framework**. In `afterEvaluate`, each task is created and its use case is constructed inline with its adapter(s), for example:

```
task.kickAssembleUseCase = KickAssembleUseCase(KickAssembleAdapter(project, settings))
```

The full wiring and task-dependency graph is walked step by step in [§6 Runtime View](../06_runtime_view.md) (plugin application & wiring scenario).

## Task graph created here

| Task | Constant | Depends on |
|------|----------|------------|
| `resolveDevDeps` | `TASK_RESOLVE_DEV_DEPENDENCIES` | — |
| `downloadDeps` | `TASK_DEPENDENCIES` | — |
| `charpad` / `spritepad` / `goattracker` / `image` | `TASK_CHARPAD` … | — |
| `preprocess` | `TASK_PREPROCESS` | charpad, spritepad, goattracker, image |
| `flows` | `TASK_FLOWS` | all top-level flow tasks (via `FlowTasksGenerator`) |
| `asm` | `TASK_ASM` | resolveDevDeps, downloadDeps, preprocess, flows |
| `asmSpec` | `TASK_ASM_SPEC` | resolveDevDeps, downloadDeps |
| `test` | `TASK_TEST` | asmSpec |
| `clean` | `TASK_CLEAN` | — |
| `build` | `TASK_BUILD` | asm, test |

Task-name constants come from `shared:gradle` `Tasks.kt` (see [shared.md](shared.md)).

## Ports & adapters

`infra:gradle` defines no ports of its own — it is the place where all contexts' ports are *bound* to their adapters. It depends (`compileOnly`) on every domain and on `shared`.
