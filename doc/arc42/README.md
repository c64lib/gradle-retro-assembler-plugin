# Gradle Retro Assembler Plugin — Architecture Documentation (arc42)

This is the technical architecture documentation for the Gradle Retro Assembler Plugin, following the [arc42](https://arc42.org/) template. It complements the [User's Manual](../index.adoc) (usage-focused, published to gh-pages) and the [knowledge base](../kb/) (agent-facing notes) by giving a complete, structured view of the system's architecture for contributors and AI agents.

All diagrams are authored as [Mermaid](https://mermaid.js.org/) fenced code blocks, so they render natively in GitHub's Markdown preview — no external toolchain or build step is required to view or edit them. To change a diagram, edit the ```mermaid block directly in the relevant `.md` file and preview the rendered result on GitHub.

## Sections

1. [Introduction and Goals](01_introduction_and_goals.md) — requirements overview, quality goals, stakeholders
2. [Architecture Constraints](02_architecture_constraints.md) — technical and organizational constraints
3. [Context and Scope](03_context_and_scope.md) — business and technical context, external systems
4. [Solution Strategy](04_solution_strategy.md) — key architectural decisions and approaches
5. [Building Block View](05_building_block_view.md) — bounded contexts, per-domain hexagon internals
6. [Runtime View](06_runtime_view.md) — key runtime scenarios as sequence diagrams
7. [Deployment View](07_deployment_view.md) — distribution, CI pipelines, runtime placement
8. [Crosscutting Concepts](08_crosscutting_concepts.md) — hexagonal architecture, DI, error handling, parallelism
9. [Architecture Decisions](09_architecture_decisions.md) — lightweight decision log
10. [Quality Requirements](10_quality_requirements.md) — quality tree and scenarios
11. [Risks and Technical Debt](11_risks_and_technical_debt.md) — honest debt register
12. [Glossary](12_glossary.md) — domain and technical terms

## Related documentation

- [`doc/index.adoc`](../index.adoc) — User's Manual (usage-focused, published to gh-pages)
- [`doc/concept/`](../concept/) — earlier, partial concept paper (predates `flows`, `crunchers`, `dasm`)
- [`doc/kb/`](../kb/) — agent-facing knowledge base notes (architecture, domain, adapters)

## Status

All twelve arc42 sections are complete (built per [PLAN-0002](../../plans/PLAN-0002_arc42-technical-documentation.md)). Keep the building-block view (§5) and crosscutting concepts (§8) current when the architecture changes; record newly discovered inconsistencies in [§11](11_risks_and_technical_debt.md).
