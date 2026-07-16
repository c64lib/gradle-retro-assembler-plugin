# Execution Log: Document the PNG / Image preprocessor in the user manual

**Exec ID**: EXEC-0007
**Plan**: [PLAN-0007](PLAN-0007_document-image-preprocessor.md)
**Issue**: #148
**Started**: 2026-07-16
**Last Updated**: 2026-07-16
**State**: completed

## 1. Execution Sessions

### Session 1 — 2026-07-16

- **Scope**: all (Phase 1, steps 1.1–1.3)
- **Mode**: per-phase
- **Outcome**: completed

| Step | Result | Verification | Notes |
|------|--------|--------------|-------|
| 1.1 | completed | tony `build.gradle.kts:1103+` (real usage); `normalize` (`WriterUtils.kt:28`); `C64SpriteWriterTest.kt:67-71` (byte layout) | Findings below; task-based observation blocked by tony's broken flows-experiments config, so byte layout confirmed via the writer's own unit test and path via `normalize` source (both authoritative) |
| 1.2 | completed | New `=== Image exports` section added to `doc/index.adoc` (between GoatTracker and Output transformations) | Kotlin `.set()` DSL; documents input/useBuildDir/writers/5 transformations w/ defaults; pipeline semantics, once-only guard, split fan-out + indexed naming walkthrough; 4 worked examples (sprite, nested split/extend, flip, bitmap); input reqs + sprite/charset layouts; cross-refs to Charpad/Spritepad/flows |
| 1.3 | completed | `./gradlew asciidoctor` BUILD SUCCESSFUL, no invalid-reference warnings; HTML produced | Verified in generated HTML: `_image_exports` + sub-anchors present, 4 `language-kotlin` blocks rendered, `<<Charpad exports>>`/`<<Spritepad exports>>`/`<<Pipeline DSL...>>` cross-refs resolved to links (no unresolved literal text) |

## 2. Deviations from Plan

| # | Step | Deviation | Reason | Impact |
|---|------|-----------|--------|--------|
| 1 | 1.1 | Plan/issue examples used `input = file(...)` / `useBuildDir = ...` / `sprite { output = ... }`, but real tony usage (Kotlin DSL, `build.gradle.kts`) uses the Gradle `Property` accessors: `getInput().set(file(...))`, `getUseBuildDir().set(true)`, `sprite { getOutput().set(file(...)) }`. `input`/`useBuildDir`/`output` are abstract `Property<T>` getters (`ImagePipelineExtension`, `ImageWriterExtension`) — no plain field setter. | Verified against tony `build.gradle.kts:1103+` and the extension source. | **User decision: document the Kotlin DSL (`.set()` accessors)**, matching tony's real, tested usage; use `[source,kotlin]` blocks (the section will differ from the manual's Groovy examples elsewhere — acceptable, it reflects reality). |
| 2 | 1.1 | Could not run tony's `image` task to observe outputs live | tony's `flows-experiments` branch fails configuration-time flow validation (`Artifact path '' is produced by multiple flows: 'intro' and 'game'`) — a tony-side issue unrelated to this plugin/doc change | Byte layout confirmed instead via `C64SpriteWriterTest.kt:67-71` (`size==64`, `[62]==0`, `[63]==0`); output path confirmed via `normalize` source. Both authoritative; no live run needed. |

## 3. Follow-ups

- tony's `flows-experiments` branch currently fails configuration-time flow validation
  (`Artifact path '' is produced by multiple flows: 'intro' and 'game'`). Out of scope for this
  doc plan, but worth a look — it blocks running individual preprocessor tasks on that branch.
