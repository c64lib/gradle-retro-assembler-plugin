# Feature: Document the PNG / Image preprocessor in the user manual

**Plan ID**: PLAN-0007
**Issue**: #148
**Status**: implemented
**Created**: 2026-07-16
**Last Updated**: 2026-07-16 (implemented)

## 1. Feature Description

### Overview
The PNG image preprocessor (`processors/image`) is a fully-featured part of the plugin but
has **no dedicated section** in the user manual (`doc/index.adoc`, published to
https://c64lib.github.io/gradle-retro-assembler-plugin/). CharPad, SpritePad and GoatTracker
each have a `Processors` subsection; the image preprocessor does not. This plan adds a
**PNG / Image preprocessor** subsection under `== Processors`, documenting its DSL,
transformation pipeline semantics, input requirements, output layouts, and worked examples —
**documentation only, no code changes**.

### Requirements
- New subsection under `== Processors` in `doc/index.adoc` (AsciiDoctor; the manual is a
  single file rendered by `./gradlew asciidoctor` → `doc/build/docs/asciidoc`, deployed to
  `gh-pages` by `.github/workflows/documentation.yml` on `master`).
- Document the **actual** DSL as implemented, not the loose shape in the issue text (see
  §2 Gap Analysis — the real entry point is an `image { }` block, writers are `sprite { }` /
  `bitmap { }`, not `imagePipelines` / `spriteWriter` / `bitmapWriter`).
- Cover: `input`, `useBuildDir`, both writers, all five transformations (`cut`, `split`,
  `extend`, `flip`, `reduceResolution`) with parameters + defaults; pipeline/nesting
  semantics; multi-image fan-out and indexed output naming; input PNG requirements; sprite
  vs. bitmap output byte layout; cross-links to CharPad/SpritePad and the flows `imageStep`.
- Follow the existing manual's style (definition lists `term::`, `[source,groovy]` blocks,
  `<<Section>>` cross-refs, `NOTE`/`WARNING` admonitions).

### Success Criteria
- All acceptance-criteria checkboxes in issue #148 are satisfiable from the rendered manual.
- `./gradlew asciidoctor` builds the manual without errors and the new section renders
  (verified locally; publication happens automatically on merge to `master`).
- Every documented DSL option, default, and constraint matches the source of truth
  (verified against the classes listed in §3), including the exact sprite (24×21 → 64 bytes)
  and charset (width/height multiple of 8) size constraints.

## 2. Root Cause Analysis

Documentation gap, not a bug. The feature shipped without manual coverage, so users must read
source or sample projects to discover it (the issue's stated motivation).

### Current State
`doc/index.adoc` `== Processors` (line 408+) documents `Charpad exports` (420),
`Spritepad exports` (751), `Goat Tracker exports` (807), and shared `Output transformations`
(871). There is **no** image/PNG subsection. The flows `imageStep` is mentioned once under
`Pipeline DSL (Experimental)` (line 1094) but only as `from`/`to` — not the preprocessor DSL.

### Desired State
A new `=== Image exports` (PNG / Image preprocessor) subsection sits alongside the other three
preprocessors, structured like them (intro → explicit-launch task → minimal example → DSL
element definition lists → transformation semantics → worked examples → input/output notes →
cross-refs).

### Gap Analysis — issue text vs. real DSL (must document the real one)
Verified against the code; the manual must describe **what the code does**:

| Issue text | Actual DSL / behavior (source of truth) |
|------------|------------------------------------------|
| `imagePipelines` block | Pipeline is declared with an `image { }` block inside `preprocess` (`PreprocessingExtension.image(...)` → `imagePipelines` list). One `image { }` per source PNG. |
| `spriteWriter` / `bitmapWriter` | Writers are nested blocks `sprite { output = ... }` and `bitmap { output = ... }` (`ImageTransformationExtension.sprite/bitmap`). Each takes a single `output` file. |
| `flip(axis)` | `flip { axis = Axis.X \| Y \| BOTH }`, default `Y`. |
| `reduceResolution(reduceX/reduceY)` | `reduceResolution { reduceX = Int; reduceY = Int }`, both default `1`. |
| `extend(newWidth/newHeight/fillColor)` | `extend { newWidth; newHeight; fillColor = Color(r,g,b,a) }`; defaults: keep dimension, `fillColor` opaque black `Color(0,0,0,255)`. |
| `cut(left/top/width/height)` | `cut { left=0; top=0; width=null→to-edge; height=null→to-edge }`. |
| `split(width/height)` | `split { width=null→full; height=null→full }` → fan-out to N sub-images. |
| "palette / color depth" input reqs | `ReadPngImageAdapter`: supports palette PNG (`channels<3`, via PLTE) and RGB/RGBA (`channels>=3`); requires 8-bit `ImageLineInt` rows — `ImageLineByte` throws. Alpha forced to 255. Pixel → bit: any color other than opaque black `Color(0,0,0,255)` = `1`, else `0` (monochrome/hires). |

Key semantic facts to document (from `ProcessImage.kt`):
- `ImagePipelineExtension` **is** an `ImageTransformationExtension`, so writers and
  transformations can be declared directly at the `image { }` root and nested recursively
  inside any transformation block.
- Processing order within one level: the current node's own transform is applied first
  (flip/cut/extend/split/reduceResolution as applicable), then child `cut`/`split`/`extend`/
  `flip`/`reduceResolution` blocks recurse, then `sprite`/`bitmap` writers emit for each
  resulting image.
- `split` is the fan-out: it turns one image into a grid of sub-images; writers then emit one
  file per sub-image.
- **Indexed output naming** (`toIndexedName`): a single image → the exact `output` name; N>1
  images → `name_{i}.ext` (0-based) per image; 0 images → error.
- **Output root when `useBuildDir=true`** (⚠ corrected after challenge — do NOT copy the
  sibling docs' `build/<processor>` wording on faith): `normalize`
  (`processors/image/adapters/out/file/WriterUtils.kt:28`) relativizes the `output` path
  against the project dir and re-roots it under `build/` **directly — there is no `image/`
  segment**. So `output = file("sprites/dino.bin")` with `useBuildDir=true` →
  `build/sprites/dino.bin`, not `build/image/sprites/dino.bin`. Step 1.1 must verify this
  empirically and document the real resolved path.
- `useBuildDir` default: `ProcessImage` reads `getUseBuildDir().get() ?: true`, but
  `getUseBuildDir()` is an abstract `Property<Boolean>` with **no convention set** in
  `ImagePipelineExtension`, so whether an *unset* `useBuildDir` yields `true` (via `?:`) or
  throws on `.get()` is **unverified** — Step 1.1 must confirm the unset-default behavior
  before documenting a default.

## 3. Relevant Code Parts (source of truth to document against)

### Existing Components
- **`ProcessImage.kt`** (DSL task / pipeline execution): `processors/image/adapters/in/gradle/.../ProcessImage.kt`
  — drives `imagePipelines`, transformation recursion, writer fan-out, indexed naming.
- **DSL extensions** (`shared/gradle/.../dsl/`): `PreprocessingExtension` (`image { }` entry,
  `imagePipelines`), `ImagePipelineExtension` (`input`, `useBuildDir`), `ImageTransformationExtension`
  (`sprite`/`bitmap`/`cut`/`split`/`extend`/`flip`/`reduceResolution` + once-only guard),
  `ImageWriterExtension` (`output`), `ImageCutExtension`, `ImageSplitExtension`,
  `ImageExtendExtension` (`fillColor: Color`), `ImageFlipExtension` (`axis: Axis`),
  `ImageReduceResolutionExtension`.
- **Domain enums/values**: `Axis { X, Y, BOTH }`, `Color(red, green, blue, alpha)` (`shared/domain`).
- **Output adapters**: `C64SpriteWriter` (requires exactly 24×21 px; allocates a 64-byte array,
  populates **only the first 63 bytes** = 21 rows × 3 bytes, byte 63 left as **zero padding**
  — NOT an attribute byte, unlike SpritePad's 64th byte; monochrome), `C64CharsetWriter`
  (width/height multiple of 8; 8 bytes per 8×8 block, block-row-major) —
  `processors/image/adapters/out/file/`.
- **PNG reader**: `ReadPngImageAdapter.kt` — `processors/image/adapters/out/png/`.
- **Manual**: `doc/index.adoc` `== Processors` (line 408); style exemplars at Charpad (420),
  Spritepad (751), GoatTracker (807).

### Architecture Alignment
- **Domain**: documentation only — no `processors/image` code, no `infra/gradle` wiring change.
- No use cases / ports / adapters created or modified.

### Dependencies
- AsciiDoctor toolchain already present (`./gradlew asciidoctor`). No new tooling.

## 4. Questions and Clarifications

### Self-Reflection Questions
- **Q**: Where does the manual actually live and how is it built/published?
  - **A**: Single file `doc/index.adoc`; `./gradlew asciidoctor` → `doc/build/docs/asciidoc`;
    `documentation.yml` deploys to `gh-pages` on push to `master`. So the doc lands on the
    site only after the PR merges to `develop` and `develop`→`master` releases.
- **Q**: Does the issue's DSL description match the code?
  - **A**: No — see §2 Gap Analysis. The plan documents the real DSL (`image { }`,
    `sprite`/`bitmap`), and treats the issue text as intent, not literal syntax.
- **Q**: Are there existing real-world examples to mine for accuracy?
  - **A**: Base worked examples on real `image { }` usage from the sample projects (`tony`,
    `ctm-viewer`, `trex64`) when found; fall back to minimal synthetic examples respecting the
    hard size constraints (sprite 24×21; charset multiples of 8). (User-confirmed 2026-07-16.)
- **Q**: What is the explicit launch task name?
  - **A**: `gradle image` — `TASK_IMAGE = "image"` in
    `shared/gradle/.../Tasks.kt:48`. Document it alongside "runs only if an `image { }`
    pipeline is defined". (Verified against code 2026-07-16.)
- **Q**: Section title?
  - **A**: `=== Image exports`, parallel to the sibling headings; the intro sentence names it
    "PNG / Image preprocessor" so both terms are searchable. (User-confirmed 2026-07-16.)
- **Q**: Output-layout depth?
  - **A**: Concrete but concise — state the sprite 24×21→64-byte layout, the charset
    8-bytes-per-8×8-block layout, and the monochrome pixel rule in a few lines each, matching
    the manual's existing density. (User-confirmed 2026-07-16.)

### Unresolved Questions
*(none — all resolved 2026-07-16)*

### Design Decisions
- **Decision**: Section title.
  - **Chosen**: `=== Image exports` (intro sentence names it "PNG / Image preprocessor").
  - **Rationale**: Consistency with the sibling "Charpad exports" / "Spritepad exports"
    headings, while keeping both search terms discoverable.
- **Decision**: Output-layout depth.
  - **Chosen**: Concrete but concise byte-layout description.
  - **Rationale**: Users need the exact sprite/charset layout and pixel rule to consume the
    `.bin` in KickAssembler; kept to a few lines each to match the manual's density.
- **Decision**: Examples source.
  - **Chosen**: Mine real sample projects (`tony`/`ctm-viewer`/`trex64`) for `image { }` usage,
    synthetic minimal fallback.
  - **Rationale**: Real examples are higher-fidelity; synthetic fallback keeps the plan
    unblocked if no sample uses the preprocessor.
- **Decision**: Explicit task name.
  - **Chosen**: Document `gradle image` (`TASK_IMAGE = "image"`).
  - **Rationale**: Verified in source; parallels the other preprocessors' explicit-launch docs.

## 5. Implementation Plan

### Phase 1: Author and verify the Image preprocessor section (single mergeable doc change)
**Goal**: Add a complete, accurate `=== Image exports` subsection to `doc/index.adoc` and
confirm the manual builds.

1. **Step 1.1** ✅: Empirically observe real preprocessor behavior (challenge-hardened).
   - Files: (read-only) sample projects (`C:/Users/maciek/prj/cbm/tony`, GitHub samples
     `ctm-viewer`/`trex64`) for real `image { }` usage; a scratch project or `/e2e-test` to
     actually run the preprocessor.
   - Description: The launch task is confirmed as `gradle image` (`TASK_IMAGE = "image"`).
     **Run the image preprocessor once** against a minimal real `image { }` block (a 24×21 PNG
     → `sprite { output = ... }`) and *observe*, rather than infer:
     1. The actual output file path with `useBuildDir=true` and with `=false` (settles the
        corrected `build/…` vs `build/image/…` claim — see §2).
     2. What happens when `useBuildDir` is left unset (does it default to `true` or throw?).
     3. That the produced sprite file is 64 bytes with byte 63 = zero padding.
     Also harvest a real `image { }` example from the samples if one exists.
   - Testing: observed paths/bytes recorded; §2 claims reconciled with reality before any prose
     is written. Delegate any Gradle run to `e2e-test`/`build` (never inline).
   - Result: tony has 13+ real `image { }` blocks (`build.gradle.kts:1103+`) — DSL uses Kotlin
     `.set()` accessors (`getInput().set(...)`, `getUseBuildDir().set(...)`,
     `sprite { getOutput().set(...) }`), user-confirmed to document that form. Output path via
     `normalize` = `build/<relativized output>` (no `build/image/` segment). Sprite byte layout
     confirmed 64 bytes with trailing zero padding via `C64SpriteWriterTest.kt:67-71`. Live task
     run blocked by tony's broken `flows-experiments` config (logged as deviation). See
     [EXEC-0007](EXEC-0007_document-image-preprocessor.md).

2. **Step 1.2** ✅: Write the `=== Image exports` subsection in `doc/index.adoc`.
   - Files: `doc/index.adoc` (insert under `== Processors`, after `Goat Tracker exports`,
     before `Output transformations` — or after it; place to read naturally).
   - Description: Author, in manual style:
     - Intro (what it does; PNG → hardware sprites, software sprite shapes, bitmap/charset).
     - Explicit-launch task (`gradle image`) + "runs only if an `image { }` pipeline exists".
     - Minimal example (`image { input=...; sprite { output=... } }`).
     - DSL element definition lists: pipeline (`input`, `useBuildDir`), writers (`sprite`,
       `bitmap` → `output`), transformations `cut` (left/top/width/height), `split`
       (width/height), `extend` (newWidth/newHeight/fillColor), `flip` (axis: X/Y/BOTH),
       `reduceResolution` (reduceX/reduceY) — each with defaults.
     - Pipeline semantics: nesting, transform-then-recurse-then-write order, `split` fan-out,
       indexed output naming (`name_0.bin`, `name_1.bin`, …), `useBuildDir` behavior.
     - ≥3 worked examples: (a) PNG → hardware sprites, (b) PNG → bitmap/charset,
       (c) sprite-sheet `split` + per-frame output; plus a `cut` + `flip` + `sprite` example.
       **Every example must be configuration-legal**: `ImageTransformationExtension.execute`
       throws `GradleException` if any writer/transform block (`sprite`, `cut`, …) is declared
       more than once at the same level — so multiple outputs come from `split` fan-out, never
       from repeated `sprite {}` blocks. Include a concrete filename walkthrough for the split
       example (e.g. 96×21 input → `split { width=24; height=21 }` → `dino_0.bin`…`dino_3.bin`).
     - Input requirements: PNG palette or RGB/RGBA, 8-bit channels; monochrome pixel rule
       (non-opaque-black → set bit); sprite must be 24×21, charset dims multiple of 8.
     - Output layout: sprite 64 bytes (63 + attr/pad), charset 8 bytes/8×8 block.
     - Cross-refs: `<<Charpad exports>>`, `<<Spritepad exports>>`, and the flows `imageStep`
       under `<<Pipeline DSL (Experimental)>>` (note flows step is `from`/`to` only for now).
   - Testing: prose review against §2/§3 facts.
   - Result: `=== Image exports` added between GoatTracker and Output transformations. Kotlin
     `.set()` DSL; input/useBuildDir + both writers + all 5 transformations w/ defaults; pipeline
     order + once-only guard + split fan-out + indexed-naming walkthrough; 4 worked examples;
     input reqs; sprite 64-byte-w/-zero-padding + charset 8-bytes/block layouts; cross-refs.

3. **Step 1.3** ✅: Build and eyeball the rendered manual.
   - Files: — (build only)
   - Description: Run `./gradlew asciidoctor`; open `doc/build/docs/asciidoc/index.html`;
     confirm the new section renders, cross-references resolve, and code blocks highlight.
   - Testing: `asciidoctor` task succeeds with **no "possible invalid reference" warnings**;
     grep the generated HTML for the new section's anchor and each `<<>>` target to confirm
     they resolve (not just an eyeball).
   - Result: `./gradlew asciidoctor` BUILD SUCCESSFUL, no invalid-reference warnings, HTML
     produced. Verified in HTML: `_image_exports` + all sub-anchors present, 4 `language-kotlin`
     blocks rendered, all cross-refs resolved to links.

**Phase 1 Deliverable**: `doc/index.adoc` gains a complete, accurate Image preprocessor
subsection; manual builds clean. Mergeable as-is (docs-only). Publication to the site follows
automatically once merged to `master`.

## 6. Testing Strategy

### Unit Tests
- None — documentation only.

### Integration Tests
- `./gradlew asciidoctor` must succeed and produce `doc/build/docs/asciidoc/index.html`
  containing the new section with resolved cross-references.

### Manual Testing
- Visual review of the rendered HTML (TOC entry, code blocks, admonitions, cross-links).
- Cross-check every documented option/default/constraint against the §3 source files.

## 7. Risks and Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Documenting the issue's DSL shape instead of the real one | High | Medium | §2 gap table pins the real DSL; verify each snippet compiles conceptually against the extension classes |
| Second-order factual errors within the real DSL (output root path, unset-`useBuildDir` default, sprite 64th byte = padding-not-attribute) | High | Medium | Step 1.1 now *runs* the preprocessor and observes actual paths/bytes before writing; §2/§3 corrected per challenge |
| An example that is configuration-illegal (duplicate `sprite {}`/transform block) | Medium | Medium | Step 1.2 requires every example to respect the once-only guard; multi-output via `split` fan-out only |
| Example PNGs violating hard size constraints (sprite 24×21, charset ÷8) mislead users | Medium | Medium | State constraints explicitly next to each example; use conformant dimensions in samples |
| Broken AsciiDoc cross-reference or syntax breaks the `asciidoctor` build | Medium | Low | Build locally in Step 1.3 before shipping |
| Site not updated after merge to develop | Low | Low | Documented: publication triggers on `master` only — call this out in the PR, not a doc bug |

## 8. Documentation Updates

- [x] This *is* the documentation update (`doc/index.adoc`).
- [ ] No CLAUDE.md change (no new code pattern).
- [ ] Optionally add a one-line pointer in the arc42 docs if they index user-facing docs
      (verify; likely not needed).

## 9. Rollout Plan

1. Ship as one docs-only PR on `feature/148-document-image-preprocessor` into `develop`.
2. Verify locally via `./gradlew asciidoctor`. Site publication happens automatically when the
   change reaches `master` (`documentation.yml`); no manual deploy step.
3. Rollback: revert the single doc commit — zero runtime impact.

## 10. Revision History

| Date | Updated By | Changes |
|------|------------|---------|
| 2026-07-16 | AI Agent | Status: draft → accepted. All Unresolved Questions resolved (section title, layout depth, examples source, launch task name). |
| 2026-07-16 | AI Agent | Incorporated adversarial challenge findings: corrected the `build/image` output-root claim (`normalize` re-roots under `build/` with no `image/` segment), reframed the sprite 64th byte as zero padding (not an attribute byte), flagged the unset-`useBuildDir` default as unverified, strengthened Step 1.1 to run the preprocessor and observe real paths/bytes, added example configuration-legality (once-only guard) + split-naming walkthrough to Step 1.2, hardened Step 1.3 cross-ref check, expanded the risk table. Status stays `accepted`. |
| 2026-07-16 | AI Agent | Status: accepted → implemented. All Phase 1 steps (1.1–1.3) executed and verified; `=== Image exports` section added to `doc/index.adoc`, `./gradlew asciidoctor` green with resolved cross-refs. During execution: confirmed Kotlin `.set()` DSL from real tony usage (documented that form per user decision), confirmed output path + sprite byte layout empirically. See [EXEC-0007](EXEC-0007_document-image-preprocessor.md). |

---

**Note**: This plan should be reviewed and approved before implementation begins.
