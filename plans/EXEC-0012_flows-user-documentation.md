# Execution Log: Extend User Documentation with Flows Capabilities

**Exec ID**: EXEC-0012
**Plan**: [PLAN-0012](PLAN-0012_flows-user-documentation.md)
**Issue**: #178
**Started**: 2026-07-18
**Last Updated**: 2026-07-18
**State**: in progress

## 1. Execution Sessions

### Session 1 — 2026-07-18

- **Scope**: all (Phases 1–3, steps 1.1–3.3)
- **Mode**: autonomous
- **Outcome**: completed

**Accuracy discipline adopted (from challenge review, verdict "sound with caveats").**
Before writing, every documented method, property, default, and enum value was verified
per-property against the source-of-truth classes, with `file:line`:
- `AssembleStepBuilder.kt` — cpu=`MOS6510`, generateSymbols=true, optimization=`SPEED`,
  outputFormat=`PRG`, workDir=".ra"; `includeFiles`/`watchFiles` aliases (`:125-142`).
- `DasmStepBuilder.kt` — outputFormat=1, workDir=".ra"; defaults srcDirs `["."]`,
  includes `["**/*.asm"]`, excludes `[".ra/**/*.asm"]` (`:186-188`).
- `CharpadStepBuilder.kt` — compression=`NONE`, exportFormat=`STANDARD`, tileSize=8;
  RangeOutputBuilder `output/start=0/end=65536`; `nybbler{loOutput/hiOutput/normalizeHi=true}`,
  `interleaver{outputs}`; MapOutputBuilder `left/top/right=65536/bottom=65536`.
- `SpritepadStepBuilder.kt` — format=`MULTICOLOR`, optimization=`SIZE`, exportRaw=true;
  `sprites{output/start=0/end=65536}`.
- `ImageStepBuilder.kt` — targetFormat=`KOALA`, paletteOptimization=`REDUCE_COLORS`,
  dithering=`FLOYD_STEINBERG`; flip `axis=Axis.Y` default; extend `fillColor=Color(0,0,0,255)`;
  reduceResolution `reduceX=1/reduceY=1`; outputs `sprite{output}`/`bitmap{output}`.
- `ExomizerStepBuilder.kt` — `raw{}`/`mem{}` **property-assignment** blocks; maxOffset=65535,
  maxLength=65535, passes=100, loadAddress="auto".
- `GoattrackerStepBuilder.kt` — frequency=`PAL`, channels=3, optimization=true,
  executable="gt2reloc".
- `CommandStepBuilder.kt` — param/params/flag/flags/option/options/inputOption/outputOption/
  with/withOption + `useFrom(index=0)` (`:191`)/`useTo(index=0)` (`:235`).
- Enums (`flows/.../domain/config/ProcessorConfig.kt`, `shared/domain/{Axis,OutputFormat}.kt`):
  CpuType{MOS6502,MOS6510,MOS65C02}, AssemblyOptimization{NONE,SIZE,SPEED},
  CharpadCompression{NONE,RLE,EXOMIZER}, CharpadFormat{STANDARD,OPTIMIZED,C64LIB},
  SpriteFormat{HIRES,MULTICOLOR}, SpriteOptimization{NONE,SIZE,SPEED}, Frequency{PAL,NTSC},
  ImageFormat{KOALA,ART_STUDIO,HIRES_BITMAP,MULTICOLOR_BITMAP},
  PaletteOptimization{NONE,REDUCE_COLORS,QUANTIZE}, DitheringAlgorithm{NONE,FLOYD_STEINBERG,ORDERED},
  Axis{X,Y,BOTH}, OutputFormat{PRG,BIN}.
- DSL entry (`FlowDsl.kt`): `flow`, `dependsOn`, `var description`, all step methods with both a
  Kotlin `FlowBuilder.() -> Unit` and a Groovy `@DelegatesTo Closure` overload (DELEGATE_FIRST).
- Task naming (`FlowTasksGenerator.kt`): `flow{Flow}Step{Step}` (`:83`), `flow{Flow}` (`:92`),
  default output dir `build/flows/{flow}/{step}` (`:280`); parallel note (`:53,79-80`).

| Step | Result | Verification | Notes |
|------|--------|--------------|-------|
| 1.1 | completed | `:doc:asciidoctor` BUILD SUCCESSFUL | Section reorganised with roadmap + xref list; Basics shown in both DSLs. |
| 1.2 | completed | rendered; text from `FlowTasksGenerator.kt:53,79-80,114` | Parallel Execution subsection: file-derived ordering, `--parallel`, config-time graph validation. |
| 1.3 | completed | rendered; from `BaseFlowStepTask` + `FlowTasksGenerator.kt:280,289-307` | Change Detection subsection: up-to-date checks, `watchFiles`, default `build/flows/{flow}/{step}`, clean note. |
| 2.1 | completed | rendered; verified vs `DasmStepBuilder.kt`, `ExomizerStepBuilder.kt` | dasm + Exomizer added with full option tables; exomizer documented as property-assignment `raw{}`/`mem{}`. |
| 2.2 | completed | rendered; verified vs `CharpadStepBuilder.kt`, `ImageStepBuilder.kt` | CharPad output blocks + nybbler/interleaver filters; Image transforms (cut/split/extend/flip/reduceResolution) + sprite/bitmap. |
| 2.3 | completed | rendered; verified vs `AssembleStepBuilder.kt`, `SpritepadStepBuilder.kt`, `GoattrackerStepBuilder.kt`, `CommandStepBuilder.kt` | Assemble gains outputFormat/srcDirs/watchFiles table; Spritepad SPD `.spr` + props; Goattracker table; Command table; Test step left intact. |
| 3.1 | completed | rendered; verified vs `CommandStepBuilder.kt:191,235` | Path Helpers: from/to varargs + command-step useFrom/useTo (both DSLs). |
| 3.2 | completed | rendered; verified vs `FlowTasksGenerator.kt:83,92`, `RetroAssemblerPlugin` wiring | Task Naming table (`flows`/`flow{X}`/`flow{X}Step{Y}`) + `asm` depends on `flows`. |
| 3.3 | completed | `:doc:asciidoctor` BUILD SUCCESSFUL; HTML 164.6 KB | Complete Example refreshed: two flows + `dependsOn`, both DSLs. |

## 2. Deviations from Plan

| # | Step | Deviation | Reason | Impact |
|---|------|-----------|--------|--------|
| 1 | 2.x / Phase 2 | Phase 2 step content (dasm/exomizer/charpad/image/assemble/spritepad/goattracker/command) was written as one contiguous rewrite of the Step Types region rather than as three temporally separate steps. | All nine steps edit the same `doc/index.adoc` section; writing them together kept the section coherent and avoided churn. | None — all planned content delivered; verification unchanged. |
| 2 | — | Added a `CHANGES.adoc` entry (#178) under `1.8.0::`. | Plan Section 8 left it optional; it is the repo norm for user-facing manual changes, and the challenge review flagged omitting it. | One extra file changed beyond `doc/index.adoc`. |
| 3 | — | Test Step subsection left unchanged (plan Step 2.3 said "leave intact"); it was already accurate and dual-example-appropriate. | Already well-documented at the prior location. | Test Step example remains Groovy-only, consistent with its callout style. |

## 3. Follow-ups

- None.
