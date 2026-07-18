# Feature: Extend User Documentation with Flows Capabilities

**Plan ID**: PLAN-0012
**Issue**: #178
**Status**: implemented
**Created**: 2026-07-18
**Last Updated**: 2026-07-18

## 1. Feature Description

### Overview

Original issue: *"extend user documentation with flow capabilities — Be rich in examples, note on parallel execution, change detection etc."*

The flows (Pipeline DSL) subdomain has grown substantially, but the user manual's
"Pipeline DSL (Experimental)" section (`doc/index.adoc:1248-1526`) is incomplete and in
places inaccurate relative to the actual DSL surface. This plan rewrites and expands that
section so users get an accurate, example-rich reference covering **every step type**, the
**path helpers**, and — as the issue explicitly requests — the cross-cutting behaviours
**parallel execution** and **change detection / incremental builds**, plus task naming and
build integration.

This is a **documentation-only** change: no production Kotlin code is modified. The single
deliverable file is `doc/index.adoc` (published to GitHub Pages via
`.github/workflows/documentation.yml` → `./gradlew asciidoctor`).

### Requirements

- Document all step types with realistic examples: `assembleStep`, `dasmStep` (currently
  undocumented), `charpadStep` (expanded), `spritepadStep`, `imageStep` (expanded),
  `goattrackerStep`, `exomizerStep` (currently undocumented), `commandStep`, `testStep`.
- Document the `from()`/`to()` path helpers and the Command-step-specific `useFrom()`/`useTo()`.
- Add a dedicated subsection on **parallel execution** (Gradle `--parallel`, file-derived
  ordering, independent steps/flows run concurrently).
- Add a dedicated subsection on **change detection / incremental builds** (Gradle up-to-date
  checks, watched/additional inputs for assembly imports, default output dirs).
- Document **flow dependencies** (`dependsOn` + implicit artifact-derived deps) and **task
  naming** (`flows`, `flow{Flow}`, `flow{Flow}Step{Step}`) and build integration (`asm`
  depends on `flows`).
- Correct existing inaccuracies (e.g. `spritepadStep` input is `.spr`/SPD, CharPad output
  richness, Image transformation pipeline).
- Keep the existing "Experimental" framing and the note that `preprocess` remains the
  recommended default.
- Show both **Kotlin** (`build.gradle.kts`) and **Groovy** (`build.gradle`) DSL examples
  **throughout** — parallel `[source,kotlin]` and `[source,groovy]` blocks for every step
  (both DSLs are supported; decided Q3).
- Provide **full parameter reference tables** for every step, including the CharPad
  output/filter richness and the Image transformation pipeline (decided Q1/Q4).

### Success Criteria

- Every step builder listed in Section 3 has an accurate example and a properties list in the
  manual.
- `parallel execution` and `change detection` each have their own clearly titled subsection.
- `./gradlew asciidoctor` renders `doc/index.adoc` without AsciiDoc errors, and the generated
  HTML at `doc/build/docs/asciidoc/index.html` shows the new content.
- No example in the manual references a DSL method or property that does not exist in the
  current code (verified against the builder classes).

## 2. Root Cause Analysis

This is a feature (documentation gap), not a bug. Motivation: the flows subdomain shipped
incrementally (PLAN-0010 spec64 support, PLAN-0011 Groovy DSL coverage) and the user manual
was not kept in lockstep. Users cannot discover `dasmStep`, `exomizerStep`, the CharPad
nybbler/interleaver filters, or the Image transformation pipeline, and there is no guidance on
the two behaviours that most affect build correctness and speed — parallelism and incremental
rebuilds.

### Current State

- `doc/index.adoc:1248-1526` ("Pipeline DSL (Experimental)") documents only:
  `flows{}` basics, and steps `assembleStep`, `charpadStep` (minimal), `spritepadStep`,
  `imageStep` (trivial `from/to` only), `goattrackerStep`, `commandStep`, `testStep`, plus a
  Flow Dependencies subsection and a Complete Example.
- Missing entirely: `dasmStep`, `exomizerStep`, CharPad output/filter richness, the Image
  transformation blocks, `useFrom()/useTo()`, parallel execution, change detection, task
  naming.
- Two flow-related images already exist but are unreferenced: `doc/img/flow-arch.excalidraw.png`,
  `doc/img/pipelines.png`.
- The authoritative internal cross-check is `doc/arc42/building-blocks/flows.md`.

### Desired State

The "Pipeline DSL" section is a complete, accurate, example-rich user reference: every step
type documented, both DSLs acknowledged, and dedicated subsections for parallel execution,
change detection/incremental builds, flow dependencies, and task naming/build integration.

### Gap Analysis

Rewrite/expand a single AsciiDoc section. No code, no ports, no adapters. The "change" is
purely in `doc/`. Verification is: builder classes are the source of truth for every method
and default; `./gradlew asciidoctor` must render cleanly.

## 3. Relevant Code Parts

### Existing Components

- **User manual (single source)**: `doc/index.adoc`
  - Pipeline DSL section: `doc/index.adoc:1248-1526` — the region to rewrite/expand.
  - AsciiDoc conventions in-file: `[source,groovy]` blocks, `Properties::` labeled lists,
    `[WARNING]`/`[NOTE]` admonitions, callouts `<1>`.
- **Asciidoctor build**: `doc/build.gradle.kts` — `org.asciidoctor.jvm.convert`, `sourceDir(".")`,
  copies `img/`; **`doc/img/`** holds `flow-arch.excalidraw.png`, `pipelines.png`.
- **Publish workflow**: `.github/workflows/documentation.yml` — on `master`, `./gradlew asciidoctor`
  → deploy `doc/build/docs/asciidoc` to `gh-pages`.
- **Internal accuracy cross-check**: `doc/arc42/building-blocks/flows.md`.

### Source-of-truth builder classes (verify every documented method/default against these)

All under `flows/adapters/in/gradle/src/main/kotlin/.../dsl/` unless noted:

- **DSL entry/wiring**: `FlowsExtension.kt`, `FlowDsl.kt` (`flow()`, `FlowBuilder` step methods
  + Groovy `Closure` overloads, `dependsOn`), registered in
  `infra/gradle/.../RetroAssemblerPlugin.kt:116` (`flows` extension), `:245` (`wireFlows`),
  `:284-287` (`asm`/`assemble` depend on `flows`).
- **`AssembleStepBuilder.kt`** — cpu/generateSymbols/optimization/outputFormat/includePaths/
  define/srcDirs/includes/excludes/watchFiles, default `workDir=".ra"`.
- **`DasmStepBuilder.kt`** — dasm assembler (undocumented): outputFormat/verboseness/errorFormat/
  strictSyntax/removeOnError/symbolTableSort/listFile/symbolFile, defaults srcDirs `["."]`,
  includes `["**/*.asm"]`, excludes `[".ra/**/*.asm"]`.
- **`CharpadStepBuilder.kt`** — compression/exportFormat/tileSize/metadata; output blocks
  `charset/charsetAttributes/charsetColours/charsetMaterials/charsetScreenColours/tiles/
  tileTags/tileColours/tileScreenColours`, `map{}`, `meta{}`; filters `nybbler{}`,
  `interleaver{}`.
- **`SpritepadStepBuilder.kt`** — format/optimization/exportRaw/exportOptimized/animationSupport;
  `sprites{output;start;end}`. Input is SPD (`.spr`).
- **`GoattrackerStepBuilder.kt`** — frequency/channels/optimization/executable + nullable tuning
  options (already largely documented).
- **`ImageStepBuilder.kt`** — targetFormat/paletteOptimization/dithering/backgroundColor/
  transparencySupport; transforms `cut/split/extend/flip/reduceResolution`; outputs
  `sprite{}`, `bitmap{}`.
- **`ExomizerStepBuilder.kt`** — compression (undocumented): `raw{}`/`mem{}` modes, options
  backwards/reverse/encoding/maxOffset/passes/loadAddress/etc.
- **`CommandStepBuilder.kt`** — param/params/flag/flags/option/options/inputOption/outputOption/
  with/withOption + `useFrom(index)`/`useTo(index)` (`:191`,`:235`).
- **`TestStepBuilder.kt`** — spec/specs/from (already well documented at `doc/index.adoc:1424`).
- **Parallelism / change detection / task naming**:
  - `flows/adapters/in/gradle/.../FlowTasksGenerator.kt` — `setupFileDependencies` (`:329`),
    parallel note (`:52-54`,`:78-80`), additional input registration (`:289-307`), default
    output dir `build/flows/{flow}/{step}` (`:280`), task names `flow{Flow}Step{Step}` (`:82`),
    `flow{Flow}` (`:92`), `flows` = `TASK_FLOWS` (`:367`), `validateFlowGraph()` (`:125`).
  - `flows/adapters/in/gradle/.../tasks/BaseFlowStepTask.kt` — `@InputFiles @PathSensitive(RELATIVE)`,
    `@OutputDirectory` (Gradle up-to-date checking).

### Architecture Alignment

- **Domain**: `flows` (documentation of), plus the `doc/` documentation asset.
- **Use Cases / Ports / Adapters**: none created or modified — documentation only.
- **CLAUDE.md rules**: no new module, so no `infra/gradle` `compileOnly` change; no code, so no
  coverage impact. The CLAUDE.md arc42 rule is about architecture changes; this touches only the
  user manual, but we will cross-check accuracy against `doc/arc42/building-blocks/flows.md`.

### Dependencies

- Asciidoctor toolchain (already configured in `doc/build.gradle.kts`); Ruby deps in `doc/Gemfile`.
  No new dependency.

## 4. Questions and Clarifications

### Self-Reflection Questions

- **Q**: Where do the new docs go — a new file or the existing manual?
  - **A**: The existing manual `doc/index.adoc` is the only user-facing published document
    (`asciidoctor` renders `doc/*.adoc` at the doc root; `documentation.yml` deploys it). New
    flows docs extend the existing "Pipeline DSL (Experimental)" section in place.
- **Q**: Is this code or docs?
  - **A**: Docs only. No builder, port, or adapter changes. Verification is AsciiDoc rendering
    plus manual accuracy cross-check against the builder classes.
- **Q**: How is accuracy guaranteed?
  - **A**: Every documented method/property/default is checked against the `*StepBuilder.kt`
    classes and `FlowTasksGenerator.kt`; `doc/arc42/building-blocks/flows.md` is the internal
    cross-reference.
- **Q**: How is rendering verified locally?
  - **A**: `./gradlew asciidoctor` (or `./gradlew :doc:asciidoctor`) → inspect
    `doc/build/docs/asciidoc/index.html`.
- **Q**: Depth for the undocumented and expanded steps? (Q1/Q4)
  - **A**: **Full parameter tables** for every step, including `dasmStep`/`exomizerStep` and the
    full CharPad output types + `nybbler{}`/`interleaver{}` filters and the Image transformation
    pipeline (`cut/split/extend/flip/reduceResolution`, `sprite/bitmap`). Exhaustive reference is
    preferred over a common-subset + source-pointer treatment.
- **Q**: DSL language coverage? (Q3)
  - **A**: **Both Kotlin and Groovy examples throughout** — every step gets parallel
    `[source,kotlin]` (`build.gradle.kts`) and `[source,groovy]` (`build.gradle`) examples.
- **Q**: Embed the existing flow diagrams? (Q2)
  - **A**: **No** — keep the section text + code only for now; diagrams can be revisited later.

### Unresolved Questions

_None — all resolved._

### Design Decisions

- **Decision**: Single-file vs. split documentation.
  - **Options**: (A) Expand `doc/index.adoc` in place; (B) create a new `doc/flows.adoc` included
    into the manual.
  - **Recommendation**: (A) Expand in place. The manual is a single rendered document and the
    flows section already exists there; a split adds include wiring for no user benefit.

## 5. Implementation Plan

### Phase 1: Structure & Core Behaviours (mergeable: overview + parallel/change-detection docs)
**Goal**: Establish the expanded section skeleton and document the two behaviours the issue
explicitly names, so the highest-value gap is closed first and independently mergeable.

1. **Step 1.1** — [x] done: Reorganise the "Pipeline DSL (Experimental)" section outline.
   - Files: `doc/index.adoc`
   - Description: Keep the Experimental warning and the `preprocess`-is-default note. Establish
     subsection order: Basics → Step Types → Path Helpers → Parallel Execution → Change
     Detection & Incremental Builds → Flow Dependencies → Task Naming & Build Integration →
     Complete Example. Add a short intro paragraph. (Per Q2, no diagrams — text + code only.)
   - Testing: `./gradlew asciidoctor` renders without errors.

2. **Step 1.2** — [x] done: Write the **Parallel Execution** subsection.
   - Files: `doc/index.adoc`
   - Description: Explain that step ordering within a flow is derived solely from file
     input/output relationships (`FlowTasksGenerator.setupFileDependencies`), that independent
     steps and flows run concurrently under Gradle `--parallel` / `org.gradle.parallel=true`,
     and that Workers API underpins processor parallelism. Note config-time validation of the
     flow graph (circular deps fail the build). Reference the CLAUDE.md "Parallel Execution"
     wording for consistency.
   - Testing: Renders; wording cross-checked against `FlowTasksGenerator.kt:52-54,78-80,125,329`.

3. **Step 1.3** — [x] done: Write the **Change Detection & Incremental Builds** subsection.
   - Files: `doc/index.adoc`
   - Description: Explain Gradle up-to-date checking via `@InputFiles`/`@OutputDirectory`
     (`BaseFlowStepTask`), that `inputs`/`outputs` (`from`/`to`) drive re-runs, that assembly
     steps additionally watch `#import`ed sources (`registerAdditionalInputFiles`,
     `watchFiles`), and the default output dir `build/flows/{flow}/{step}` when `to()` is
     omitted. Add a short note referencing the known clean-behaviour quirk if relevant.
   - Testing: Renders; cross-checked against `BaseFlowStepTask.kt` and
     `FlowTasksGenerator.kt:280,289-307`.

**Phase 1 Deliverable**: The section now explains parallel execution and incremental builds
accurately — the issue's named requirements — even before every step is expanded. Mergeable.

### Phase 2: Step Type Reference (mergeable: complete, accurate step catalogue)
**Goal**: Every step type documented accurately with a realistic example and properties list.

1. **Step 2.1** — [x] done: Add **`dasmStep`** and **`exomizerStep`** (currently undocumented).
   - Files: `doc/index.adoc`
   - Description: New subsections with Kotlin + Groovy examples and **full parameter tables**
     (Q1: exhaustive, including exomizer `raw{}`/`mem{}` modes and dasm output/verboseness/error
     options).
   - Testing: Methods/defaults verified against `DasmStepBuilder.kt`, `ExomizerStepBuilder.kt`.

2. **Step 2.2** — [x] done: Expand **`charpadStep`** and **`imageStep`**.
   - Files: `doc/index.adoc`
   - Description: Per Q4 — fully document CharPad output types and `nybbler{}`/`interleaver{}`
     filters, and the Image transformation pipeline (`cut/split/extend/flip/reduceResolution`)
     plus `sprite{}`/`bitmap{}` outputs, with parameter tables. Replace the trivial Image
     `from/to`-only example. Kotlin + Groovy examples.
   - Testing: Verified against `CharpadStepBuilder.kt`, `ImageStepBuilder.kt`.

3. **Step 2.3** — [x] done: Correct and round out the remaining steps.
   - Files: `doc/index.adoc`
   - Description: Verify/fix `assembleStep` (add `outputFormat`, `srcDirs`, `watchFiles`),
     `spritepadStep` (confirm SPD `.spr` input, format/optimization/exportRaw props),
     `goattrackerStep` (already good), `commandStep`, `testStep` (already good — leave intact).
   - Testing: Each verified against its `*StepBuilder.kt`.

**Phase 2 Deliverable**: A complete, accurate step catalogue. Mergeable independently of Phase 3.

### Phase 3: Path Helpers, Task Naming, Examples & Polish (mergeable: finished section)
**Goal**: Cover the remaining cross-cutting DSL surface and finalise examples.

1. **Step 3.1** — [x] done: Write the **Path Helpers** subsection.
   - Files: `doc/index.adoc`
   - Description: Document `from()`/`to()` (single + vararg) generally, and the
     Command-step-specific `useFrom(index)`/`useTo(index)` with the DRY example from CLAUDE.md.
   - Testing: Verified against `CommandStepBuilder.kt:191,235`.

2. **Step 3.2** — [x] done: Write the **Task Naming & Build Integration** subsection.
   - Files: `doc/index.adoc`
   - Description: Document task names `flows`, `flow{Flow}`, `flow{Flow}Step{Step}`; that
     `./gradlew flows` runs all flows and `asm`/`assemble` depend on `flows`; and the
     `dependsOn` + implicit artifact-derived dependency model. Align with CLAUDE.md "Task
     Execution Order".
   - Testing: Verified against `FlowTasksGenerator.kt:82,92,367` and `RetroAssemblerPlugin.kt:284-287`.

3. **Step 3.3** — [x] done: Refresh the **Complete Example** and add Kotlin/Groovy coverage per Q3.
   - Files: `doc/index.adoc`
   - Description: Update the end-to-end example to exercise multiple steps + a `dependsOn`,
     ensuring every method used exists. Provide it in both Kotlin (`.kts`) and Groovy (Q3).
   - Testing: Full `./gradlew asciidoctor` render; open `doc/build/docs/asciidoc/index.html`
     and eyeball the flows section end-to-end.

**Phase 3 Deliverable**: The complete, accurate, example-rich flows section. Mergeable.

## 6. Testing Strategy

### Unit Tests
- None — documentation-only change; no production code touched.

### Integration Tests
- **Doc build**: `./gradlew asciidoctor` (or `:doc:asciidoctor`) must complete without AsciiDoc
  syntax/rendering errors. This is the CI gate that `documentation.yml` runs on `master`.

### Manual Testing
- Open `doc/build/docs/asciidoc/index.html` and review the flows section for correct rendering
  of admonitions, source blocks, callouts, and (if added) images.
- **Accuracy pass**: for each documented method/property/default, confirm it exists in the
  corresponding `*StepBuilder.kt` / `FlowTasksGenerator.kt`. This is the primary correctness
  check for docs — no example may reference a non-existent DSL element.
- Optionally cross-read `doc/arc42/building-blocks/flows.md` to ensure user docs and internal
  docs agree.

## 7. Risks and Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Documented DSL drifts from actual builder API (wrong method/default) | Medium | Medium | Verify every example against `*StepBuilder.kt`; accuracy pass in Section 6 |
| AsciiDoc syntax error breaks the published manual | Medium | Low | Run `./gradlew asciidoctor` before merge; CI renders on `master` |
| Full parameter tables drift as the experimental DSL changes | Medium | Medium | Full tables were chosen deliberately (Q1); keep the "Experimental" framing, verify every property against the `*StepBuilder.kt` at write time, and treat the manual as regenerated when the DSL changes rather than hand-patched |
| Kotlin + Groovy examples for every step doubles volume / risks the two drifting | Low | Medium | Keep examples minimal and mechanically parallel (same step, two source blocks); verify both compile-shape against the builders |
| Section becomes too long/hard to navigate | Low | Medium | Clear subsection hierarchy; consider an overview diagram (Q2) |

## 8. Documentation Updates

- [x] This IS the documentation update (`doc/index.adoc`).
- [ ] `README.md` — no change needed (it only links to the manual).
- [ ] `CLAUDE.md` — no new pattern introduced; no change expected.
- [x] `CHANGES.adoc` — added a changelog entry (#178) under `1.8.0::`.
- [ ] Cross-check against `doc/arc42/building-blocks/flows.md` (internal) — no edit expected,
      read-only accuracy reference.

## 9. Rollout Plan

1. Merge the doc changes to `develop`, then to `master` via the normal flow.
2. On push to `master`, `.github/workflows/documentation.yml` runs `./gradlew asciidoctor` and
   deploys to `gh-pages` → live at https://c64lib.github.io/gradle-retro-assembler-plugin/.
3. Monitor: confirm the GitHub Pages build succeeded and the flows section renders correctly
   live. No rollback risk beyond reverting the doc commit — documentation-only, no runtime impact.

## 10. Revision History

| Date | Updated By | Changes |
|------|------------|---------|
| 2026-07-18 | AI Agent | Initial draft created for issue #178 |
| 2026-07-18 | AI Agent | Resolved Q1–Q4: full parameter tables for all steps, Kotlin+Groovy examples throughout, no diagrams. Propagated into requirements, Phase 2/3 steps, and risks. |
| 2026-07-18 | AI Agent | Status → accepted (no unresolved questions). |
| 2026-07-18 | AI Agent | Status → in progress; execution started (EXEC-0012). |
| 2026-07-18 | AI Agent | All steps 1.1–3.3 completed. Rewrote the Pipeline DSL section of `doc/index.adoc` (all 9 steps documented with full tables + Kotlin/Groovy examples, parallel-execution/change-detection/task-naming subsections) and added a `CHANGES.adoc` entry (#178). `:doc:asciidoctor` renders clean (BUILD SUCCESSFUL). Status → implemented. See EXEC-0012 for the per-step log and deviations. |

---

**Note**: This plan should be reviewed and approved before implementation begins.
