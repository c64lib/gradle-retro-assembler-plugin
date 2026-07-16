# Feature: Document the PNG / Image preprocessor in the user manual

**Plan ID**: PLAN-0007
**Issue**: #148
**Status**: accepted
**Created**: 2026-07-16
**Last Updated**: 2026-07-16

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
- `useBuildDir` default is effectively `true` (`getUseBuildDir().get() ?: true`); when true
  outputs go under `build/image` (the processor's normalize root), else project-relative.

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
- **Output adapters**: `C64SpriteWriter` (requires 24×21 px; writes 64 bytes: 63 data + 1 pad;
  monochrome), `C64CharsetWriter` (width/height multiple of 8; 8 bytes per 8×8 block,
  block-row-major) — `processors/image/adapters/out/file/`.
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

1. **Step 1.1**: Harvest a real example and confirm the output root.
   - Files: (read-only) sample projects (`C:/Users/maciek/prj/cbm/tony`, and the referenced
     GitHub samples `ctm-viewer`/`trex64`) for real `image { }` usage; `ProcessImage.kt` /
     `normalize` for the `build/image` output root.
   - Description: The launch task is already confirmed as `gradle image`
     (`TASK_IMAGE = "image"`). Harvest a real `image { }` example if one exists in the samples;
     confirm the `useBuildDir=true` output root path.
   - Testing: notes captured; no doc written yet.

2. **Step 1.2**: Write the `=== Image exports` subsection in `doc/index.adoc`.
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
     - Input requirements: PNG palette or RGB/RGBA, 8-bit channels; monochrome pixel rule
       (non-opaque-black → set bit); sprite must be 24×21, charset dims multiple of 8.
     - Output layout: sprite 64 bytes (63 + attr/pad), charset 8 bytes/8×8 block.
     - Cross-refs: `<<Charpad exports>>`, `<<Spritepad exports>>`, and the flows `imageStep`
       under `<<Pipeline DSL (Experimental)>>` (note flows step is `from`/`to` only for now).
   - Testing: prose review against §2/§3 facts.

3. **Step 1.3**: Build and eyeball the rendered manual.
   - Files: — (build only)
   - Description: Run `./gradlew asciidoctor`; open `doc/build/docs/asciidoc/index.html`;
     confirm the new section renders, cross-references resolve, and code blocks highlight.
   - Testing: `asciidoctor` task succeeds; section present; no broken `<<>>` refs.

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

---

**Note**: This plan should be reviewed and approved before implementation begins.
