# 9. Architecture Decisions

The project predates any formal ADR process, so this is a **lightweight decision log reconstructed** from [`CLAUDE.md`](../../CLAUDE.md), the knowledge-base notes ([`doc/kb/`](../../doc/kb/)), and the legacy `.ai/` plans. Each entry records the *context*, the *decision*, and its *consequences* — enough to explain why the architecture looks the way it does, without claiming to be a contemporaneous record. Cross-cutting mechanics are in [§8](08_crosscutting_concepts.md); strategy framing is in [§4](04_solution_strategy.md).

## AD-1 — Hexagonal architecture per domain

- **Context:** The plugin must support several assembler dialects and many asset processors, added over time by different contributors, without each addition destabilizing the others.
- **Decision:** Adopt hexagonal (ports & adapters) architecture, one bounded context per business capability, each split into `src/{domain,usecase}` and `adapters/{in,out}`. Technology types never enter the core.
- **Consequences:** Use cases are unit-testable without Gradle; new dialects/processors are added as new modules rather than edits to existing ones; contributors have a single predictable module shape to follow. Cost: more modules and some boilerplate (a port + adapter per technology touchpoint).

## AD-2 — Gradle isolated as an adapter concern

- **Context:** The system *is* a Gradle plugin, but Gradle's API is a heavy, hard-to-test dependency.
- **Decision:** Treat Gradle as pure technology. `Project`, `Task`, and extension APIs appear only under `adapters/in/gradle` and `adapters/out/gradle`; domain and use-case code is Gradle-free.
- **Consequences:** Business logic is testable in isolation; a hypothetical non-Gradle front end would only need new adapters. Cost: an inbound adapter layer (tasks, DSL, step builders) must be maintained alongside each capability.

## AD-3 — Use-case-per-operation with a single `apply` method

- **Context:** Business operations needed a uniform, discoverable structure for humans and AI agents.
- **Decision:** Every operation is a `*UseCase` class exposing exactly one public method, `apply(payload)`, optionally returning a result.
- **Consequences:** One entry point per capability, trivially testable and mockable; no implicit method-ordering contracts. Cost: many small classes rather than fewer aggregated services.

## AD-4 — No DI framework; manual wiring in `afterEvaluate`

- **Context:** Use cases must be connected to their port implementations and to Gradle tasks somewhere.
- **Decision:** Wire everything by hand in `RetroAssemblerPlugin.apply()` inside `project.afterEvaluate` — explicit constructor calls, no Spring/Dagger/Koin.
- **Consequences:** The full dependency graph is visible and grep-able; no reflection/annotation magic. Cost: a single large wiring block (noted as debt in [§11](11_risks_and_technical_debt.md)) and the enforced `compileOnly` registration step for new modules (AD-7).

## AD-5 — Gradle Workers API over custom threading

- **Context:** Some tasks (and independent flow steps) can run in parallel, but ad-hoc threads would fight Gradle's own execution model and be hard to reason about.
- **Decision:** Never hand-roll threading; use the Gradle Workers API for in-task parallelism, and let Gradle's scheduler parallelize independent tasks under `--parallel`.
- **Consequences:** Parallelism composes with Gradle's daemon, build cache, and up-to-date checks. Cost: parallelism is expressed indirectly (through task/worker structure) rather than imperatively.

## AD-6 — Flows: file-based task-dependency derivation

- **Context:** The `flows` Pipeline DSL orchestrates many steps whose ordering and parallelism should follow their real data dependencies, not manual `dependsOn` bookkeeping.
- **Decision:** `FlowTasksGenerator` derives Gradle `dependsOn` and input/output wiring from the `FlowDependencyGraph` and each step's declared `inputs`/`outputs`; Gradle then schedules and incrementally skips steps.
- **Consequences:** Correct ordering, automatic parallelism, and free incremental builds without manual dependency declarations. Design history: [PLAN-0001](../../plans/PLAN-0001_pipeline-dsl-parallel-execution.md). Cost: steps must declare accurate inputs/outputs for the derivation to be correct.

## AD-7 — New domains registered as `compileOnly` in `infra:gradle`

- **Context:** With manual DI and explicit module inclusion, `infra:gradle` must be able to see a new domain's classes to wire them.
- **Decision:** Every domain module is a `compileOnly` dependency of `infra:gradle`; a new module is inert until added here and wired in `RetroAssemblerPlugin`.
- **Consequences:** The task graph stays explicit and auditable; auto-discovery is deliberately avoided. Cost: forgetting the registration yields a runtime `ClassNotFoundException` — a known footgun documented in `CLAUDE.md`.

## AD-8 — Markdown + Mermaid for this architecture documentation

- **Context:** The repo already has an AsciiDoc User's Manual (gh-pages) and scattered kb/concept notes, but no complete, maintainable architecture reference.
- **Decision:** Author this arc42 set as Markdown with Mermaid diagrams under `doc/arc42/`, rendered natively on GitHub with no build toolchain; do not publish it to gh-pages in this iteration.
- **Consequences:** Diagrams are diffable and editable in place by humans and agents; zero toolchain to view. The existing AsciiDoctor → gh-pages pipeline ([`documentation.yml`](../../.github/workflows/documentation.yml)) is untouched; gh-pages publication of arc42 is deferred to a possible follow-up. Full rationale: [PLAN-0002](../../plans/PLAN-0002_arc42-technical-documentation.md) (this document's own plan — a self-referential first ADR).
