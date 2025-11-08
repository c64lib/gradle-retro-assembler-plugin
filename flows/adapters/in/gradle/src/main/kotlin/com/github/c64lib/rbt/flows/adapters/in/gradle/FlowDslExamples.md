# Flow DSL Syntax Examples

This document demonstrates the various ways to define parallel execution flows using the new DSL syntax.

## Basic Flow Definition

```kotlin
flows {
    flow("simple-build") {
        description = "A simple sequential build flow"
        
        step("assemble") {
            from("src/main/asm")
            to("build/compiled")
        }
    }
}
```

## Parallel Processing Flows

```kotlin
flows {
    // Preprocessing flows that can run in parallel
    flow("asset-preprocessing") {
        description = "Process all assets in parallel"

        parallel {
            step("charpad") {
                from("src/assets/charset.ctm")
                charset { output = "build/charset.chr" }
                map { output = "build/map.bin" }
                meta {
                    output = "build/charset.h"
                    namespace = "GAME"
                    prefix = "CHR_"
                    includeVersion = true
                }
            }

            step("spritepad") {
                from("src/assets/sprites")
                to("build/processed/sprites")
            }

            step("goattracker") {
                from("src/assets/music")
                to("build/processed/music")
            }

            step("image") {
                from("src/assets/images/*.png")
                to("build/processed/images")
                configure("resolution", "320x200")
            }
        }
    }

    // Dependencies can be resolved in parallel
    flow("dependency-resolution") {
        description = "Download and resolve dependencies"

        step("resolve") {
            to("build/dependencies")
            configure("cacheDir", ".gradle/retro-assembler-cache")
        }
    }
}
```

## Charpad Step Details

The Charpad step supports dedicated output methods for fine-grained control over CTM file processing:

```kotlin
flows {
    flow("process-assets") {
        description = "Process CharPad CTM files"

        step("charpad-main") {
            from("src/assets/main.ctm")

            // Charset with optional range (default: 0-65536)
            charset {
                output = "build/charset.chr"
                start = 0
                end = 256
            }

            // Map with optional rectangular region (default: full map)
            map {
                output = "build/map.bin"
                left = 0
                top = 0
                right = 40
                bottom = 25
            }

            // Tiles with range
            tiles {
                output = "build/tiles.bin"
                start = 0
                end = 100
            }

            // Metadata output with configuration
            meta {
                output = "build/constants.h"
                namespace = "LEVEL1"
                prefix = "MAP_"
                includeVersion = true
                includeBgColours = true
                includeCharColours = true
                includeMode = false
            }

            // Additional outputs
            charsetColours { output = "build/char_colours.bin" }
            charsetAttributes { output = "build/char_attrs.bin" }
            tileTags { output = "build/tile_tags.bin" }
            tileColours { output = "build/tile_colours.bin" }
        }
    }
}
```

### Output Types Reference

The CharPad step supports the following dedicated output methods:

| Method | Output Type | Parameters | Description |
|--------|-------------|-----------|-------------|
| `charset` | Charset data | start, end | Character set with optional range |
| `charsetAttributes` | Char attributes | start, end | Character attributes (collision data) |
| `charsetColours` | Char colours | start, end | Character color data |
| `charsetMaterials` | Char materials | start, end | Character material properties |
| `charsetScreenColours` | Screen colours | start, end | Screen color per character |
| `tiles` | Tile data | start, end | Tile definitions with optional range |
| `tileTags` | Tile tags | start, end | Tile metadata tags |
| `tileColours` | Tile colours | start, end | Tile color data |
| `tileScreenColours` | Tile screen colours | start, end | Tile screen color data |
| `map` | Map data | left, top, right, bottom | Map with optional rectangular region |
| `meta` | Metadata/Header | namespace, prefix, flags | Header file with configuration |

### Range Parameters

For range-based outputs (charset, tiles, etc.):
- `start`: Starting index (default: 0)
- `end`: Ending index (default: 65536)

Example with multiple outputs of same type:
```kotlin
step("charpad-multi") {
    from("chars.ctm")

    // Split charset into two ranges
    charset {
        output = "build/charset1.chr"
        start = 0
        end = 128
    }
    charset {
        output = "build/charset2.chr"
        start = 128
        end = 256
    }
}
```

### Rectangular Region Parameters

For map outputs:
- `left`: Left column (default: 0)
- `top`: Top row (default: 0)
- `right`: Right column (default: 65536)
- `bottom`: Bottom row (default: 65536)

Example:
```kotlin
step("charpad-region") {
    from("tilemap.ctm")

    map {
        output = "build/level1_map.bin"
        left = 0
        top = 0
        right = 40
        bottom = 25
    }
}
```

### Metadata Configuration

The `meta` output supports optional configuration parameters:
- `namespace`: Prefix for generated constants (default: "")
- `prefix`: Additional prefix for constants (default: "")
- `includeVersion`: Include CTM version in header (default: false)
- `includeBgColours`: Include background colors (default: true)
- `includeCharColours`: Include character colors (default: true)
- `includeMode`: Include screen mode info (default: false)

## Complex Dependency Chain

```kotlin
flows {
    flow("preprocessing") {
        description = "Process all assets"

        parallel {
            step("charpad") {
                from("src/assets/charset.ctm")
                charset { output = "build/charset.chr" }
                map { output = "build/map.bin" }
            }
            step("spritepad") {
                from("src/assets/sprites")
                to("build/processed/sprites")
            }
        }
    }
    
    flow("dependencies") {
        description = "Resolve project dependencies"
        
        step("download") {
            to("build/dependencies")
        }
    }
    
    flow("compilation") {
        dependsOn("preprocessing", "dependencies")
        description = "Compile assembly source code"
        
        step("assemble") {
            from("src/main/asm")
            to("build/compiled/main.prg")
            configure("libDirs", listOf("build/dependencies", "build/processed"))
        }
    }
    
    flow("testing") {
        dependsOn("compilation")
        description = "Run unit tests"
        
        step("test") {
            from("build/compiled/main.prg")
            to("build/test-results")
            configure("emulator", "x64sc")
            configure("timeout", 30000)
        }
    }
    
    flow("packaging") {
        dependsOn("compilation")
        description = "Create distribution package"
        
        step("package") {
            from("build/compiled")
            to("build/dist/game.d64")
            configure("diskType", "d64")
        }
    }
}
```

## Advanced Configuration

```kotlin
flows {
    flow("multi-target-build") {
        description = "Build for multiple target platforms"
        
        parallel {
            step("c64-build") {
                from("src/main/asm")
                to("build/c64/main.prg")
                configure(mapOf(
                    "target" to "c64",
                    "symbols" to true,
                    "optimize" to true
                ))
            }
            
            step("c128-build") {
                from("src/main/asm")
                to("build/c128/main.prg") 
                configure(mapOf(
                    "target" to "c128",
                    "mode" to "c64",
                    "symbols" to false
                ))
            }
            
            step("plus4-build") {
                from("src/main/asm")
                to("build/plus4/main.prg")
                configure("target", "plus4")
            }
        }
    }
}
```

## Migration from Sequential Tasks

### Before (Sequential)
```kotlin
// Old way - everything runs sequentially
tasks.named("assemble") {
    dependsOn("resolveDevDeps", "downloadDependencies", "preprocess")
}
tasks.named("preprocess") {
    dependsOn("charpad", "spritepad", "goattracker", "image")
}
```

### After (Parallel Flows)
```kotlin
flows {
    // These flows can run in parallel
    flow("asset-processing") {
        parallel {
            step("charpad") { /* config */ }
            step("spritepad") { /* config */ }
            step("goattracker") { /* config */ }
            step("image") { /* config */ }
        }
    }

    flow("dependency-resolution") {
        step("resolve-dev-deps") { /* config */ }
        step("download-dependencies") { /* config */ }
    }

    // This flow waits for both parallel flows to complete
    flow("compilation") {
        dependsOn("asset-processing", "dependency-resolution")
        step("assemble") { /* config */ }
    }
}
```

## Migration from Generic to Dedicated CharPad DSL

### Before (Generic Output)
```kotlin
// Old way - generic "to()" method for all outputs
step("charpad") {
    from("charset.ctm")
    to("build/charset.chr")
    to("build/map.bin")
    to("build/header.h")
    metadata {
        namespace = "GAME"
        prefix = "CHR_"
    }
}
```

### After (Dedicated Output Methods)
```kotlin
// New way - type-safe, dedicated output methods
step("charpad") {
    from("charset.ctm")

    // Explicit charset output
    charset {
        output = "build/charset.chr"
        start = 0
        end = 256
    }

    // Explicit map output with region
    map {
        output = "build/map.bin"
        left = 0
        top = 0
        right = 40
        bottom = 25
    }

    // Explicit metadata output
    meta {
        output = "build/header.h"
        namespace = "GAME"
        prefix = "CHR_"
        includeVersion = true
        includeBgColours = true
        includeCharColours = true
        includeMode = false
    }
}
```

### Key Improvements

1. **Type Safety**: Each output method is explicitly typed (charset, map, metadata, etc.)
2. **Clear Intent**: You can immediately see what type of output is being generated
3. **Parameter Support**: Each output type supports its relevant parameters:
   - Range-based (charset, tiles): `start` and `end`
   - Region-based (map): `left`, `top`, `right`, `bottom`
   - Metadata: namespace, prefix, and inclusion flags
4. **Multiple Outputs**: Support for multiple outputs of the same type with different configurations
5. **Better IDE Support**: Full autocompletion and parameter validation

### Backward Compatibility

The generic `to()` method is deprecated but still supported for migration:
```kotlin
// Still works, but shows deprecation warning
step("charpad") {
    from("charset.ctm")
    to("build/charset.chr")  // @Deprecated - use charset { } instead
    to("build/map.bin")      // @Deprecated - use map { } instead
}
```

Use the IDE's "Replace with" suggestion to quickly migrate to dedicated output methods.

## Performance Benefits

With the new flow syntax:
- **Asset processing flows** (charpad, spritepad, goattracker, image) run in parallel
- **Dependency resolution** runs in parallel with asset processing  
- **Compilation** only starts when both preprocessing flows complete
- **Testing** and **Packaging** can run in parallel after compilation

This can reduce build times from sequential execution (A + B + C + D) to parallel execution (max(A, B) + C + max(D, E)), potentially cutting build times in half or more for complex projects.

## Binary Filters: Nybbler and Interleaver

The CharPad step supports optional binary filters on range-based outputs to transform charset and tile data at different granularity levels. These filters are useful for accessing and transforming data at nibble (4-bit) or byte level independently.

### Filter Applicability

Filters are applicable to **range-based outputs only** (those with `start` and `end` parameters):

| Output Type | Supports Filters | Reason |
|------------|-----------------|--------|
| `charset` | ✅ Yes | Range-based output |
| `charsetAttributes` | ✅ Yes | Range-based output |
| `charsetColours` | ✅ Yes | Range-based output |
| `charsetMaterials` | ✅ Yes | Range-based output |
| `charsetScreenColours` | ✅ Yes | Range-based output |
| `tiles` | ✅ Yes | Range-based output |
| `tileTags` | ✅ Yes | Range-based output |
| `tileColours` | ✅ Yes | Range-based output |
| `tileScreenColours` | ✅ Yes | Range-based output |
| `map` | ✅ Yes | Region-based output (also produces binary data) |
| `meta` | ❌ No | Metadata/header output, not binary data |

**Note:** Only metadata output does not support filters because it generates text headers, not binary data. All other outputs (including maps) produce binary data and support filters.

### Nybbler Filter

The nybbler filter splits each byte into low and high nibbles (4-bit halves), writing them to separate output files. This is useful in Commodore 64 development where character codes or color values often pack two nybbles per byte.

**Use Cases:**
- Extracting upper/lower 4-bit values independently
- Converting character codes that store two nybbles per byte
- Processing graphics data that requires separate handling of nibble pairs
- Generating lookup tables for nibble-based color mappings

**Syntax:**
```kotlin
step("charpad-nybbler") {
    from("charset.ctm")

    // Split charset into low and high nibbles
    charset {
        output = "build/charset.chr"
        nybbler {
            loOutput = "build/charset_lo.chr"      // Low nibbles (lower 4 bits)
            hiOutput = "build/charset_hi.chr"      // High nibbles (upper 4 bits)
            normalizeHi = true                      // Shift high nibbles right (default: true)
        }
    }
}
```

**How it works:**

Input byte `0xA5` (binary: 1010 0101):
- Low nibbles output: `0x5` (binary: 0000 0101)
- High nibbles output (normalized): `0x0A` (binary: 0000 1010)
- High nibbles output (not normalized): `0xA0` (binary: 1010 0000)

**Advanced Examples:**

Only extract high nibbles (discard low):
```kotlin
charset {
    output = "build/charset.chr"
    nybbler {
        hiOutput = "build/charset_hi.chr"
        normalizeHi = true
    }
}
```

Extract low nibbles without normalizing high:
```kotlin
charset {
    output = "build/charset.chr"
    nybbler {
        loOutput = "build/charset_lo.chr"
        hiOutput = "build/charset_hi.chr"
        normalizeHi = false  // Keep high nibbles in upper 4 bits
    }
}
```

Use nybbler with other outputs:
```kotlin
step("charpad-complex") {
    from("charset.ctm")

    charset {
        output = "build/charset.chr"
        start = 0
        end = 256
        nybbler {
            loOutput = "build/lo.chr"
            hiOutput = "build/hi.chr"
        }
    }

    map {
        output = "build/map.bin"
        left = 0
        top = 0
        right = 40
        bottom = 25
    }
}
```

### Interleaver Filter

The interleaver filter distributes binary data across multiple output streams in round-robin fashion. This is useful for accessing bytes within larger chunks (like word-aligned data) independently.

**Use Cases:**
- Separating even/odd bytes for word-aligned data structures
- Splitting bitmap data into separate bit-planes
- Extracting upper/lower bytes of word-aligned values independently
- Creating parallel data streams for specialized processing

**Syntax:**
```kotlin
step("charpad-interleaver") {
    from("charset.ctm")

    // Split charset across 2 outputs (even/odd bytes)
    charset {
        output = "build/charset.chr"
        interleaver {
            outputs = listOf(
                "build/charset_even.chr",
                "build/charset_odd.chr"
            )
        }
    }
}
```

**How it works:**

Input bytes: `[0x01, 0x02, 0x03, 0x04, 0x05, 0x06]`

With 2 outputs (even/odd distribution):
- Output 0: `[0x01, 0x03, 0x05]` (indices 0, 2, 4, ...)
- Output 1: `[0x02, 0x04, 0x06]` (indices 1, 3, 5, ...)

With 3 outputs:
- Output 0: `[0x01, 0x04]` (indices 0, 3, ...)
- Output 1: `[0x02, 0x05]` (indices 1, 4, ...)
- Output 2: `[0x03, 0x06]` (indices 2, 5, ...)

**Advanced Examples:**

Split charset into 4 streams (for multi-way processing):
```kotlin
charset {
    output = "build/charset.chr"
    interleaver {
        outputs = listOf(
            "build/char_0.chr",
            "build/char_1.chr",
            "build/char_2.chr",
            "build/char_3.chr"
        )
    }
}
```

Use interleaver with range-based output:
```kotlin
tiles {
    output = "build/tiles.bin"
    start = 0
    end = 100
    interleaver {
        outputs = listOf(
            "build/tiles_lower.bin",
            "build/tiles_upper.bin"
        )
    }
}
```

### Filter Constraints and Behavior

**Important Constraints:**

1. **Filter Applicability:** Only range-based outputs (charset, tiles, colors, attributes, etc.) support filters. Maps and metadata outputs do not.

2. **Mutual Exclusivity:** Only one filter type (nybbler OR interleaver) can be applied per output. The following will compile but will only use the interleaver:

```kotlin
// ❌ Invalid: Both filters specified (only interleaver will be used)
charset {
    output = "build/charset.chr"
    nybbler {
        loOutput = "build/lo.chr"
    }
    interleaver {
        outputs = listOf("build/i0.chr", "build/i1.chr")
    }
}
```

**Correct approach - choose one:**
```kotlin
// ✅ Valid: Using nybbler
charset {
    output = "build/charset.chr"
    nybbler {
        loOutput = "build/lo.chr"
        hiOutput = "build/hi.chr"
    }
}
```

### Practical Examples

**Example 1: Separating Charset into Bit Planes**
```kotlin
step("charset-bitplanes") {
    description = "Extract charset into separate bit planes for color processing"
    from("graphics.ctm")

    charset {
        output = "build/charset.chr"
        interleaver {
            outputs = listOf(
                "build/bitplane_0.chr",
                "build/bitplane_1.chr",
                "build/bitplane_2.chr",
                "build/bitplane_3.chr"
            )
        }
    }
}
```

**Example 2: Processing Character Attributes with Nybbler**
```kotlin
step("char-attributes-nibbles") {
    description = "Split character attributes into separate nibble streams"
    from("charset.ctm")

    // Character attributes (collision data, etc.)
    charsetAttributes {
        output = "build/char_attrs.bin"
        nybbler {
            loOutput = "build/attrs_low.bin"       // Lower nibble attributes
            hiOutput = "build/attrs_high.bin"      // Upper nibble attributes
            normalizeHi = true
        }
    }
}
```

**Example 3: Complex Multi-Output Processing**
```kotlin
step("charpad-comprehensive") {
    description = "Process charset with both nybbler and map"
    from("level_charset.ctm")

    charset {
        output = "build/charset.chr"
        nybbler {
            loOutput = "build/charset_lo.chr"
            hiOutput = "build/charset_hi.chr"
        }
    }

    charsetColours {
        output = "build/colours.bin"
        interleaver {
            outputs = listOf(
                "build/colours_fg.bin",    // Foreground colors
                "build/colours_bg.bin"     // Background colors
            )
        }
    }

    map {
        output = "build/map.bin"
        left = 0
        top = 0
        right = 40
        bottom = 25
    }
}
```

**Example 4: Complete Game Asset Processing Pipeline**
```kotlin
parallel {
    step("main-charset") {
        from("assets/main_charset.ctm")

        charset {
            output = "build/assets/charset.chr"
            nybbler {
                loOutput = "build/assets/charset_decoded.bin"
                hiOutput = "build/assets/charset_encoded.bin"
            }
        }

        map {
            output = "build/assets/main_map.bin"
        }
    }

    step("level-charset") {
        from("assets/level_charset.ctm")

        charset {
            output = "build/assets/level_chars.chr"
            interleaver {
                outputs = listOf(
                    "build/assets/level_stream_0.chr",
                    "build/assets/level_stream_1.chr"
                )
            }
        }

        tiles {
            output = "build/assets/level_tiles.bin"
        }
    }
}
```

### Output Path Tracking

When using filters, all output paths (including filter outputs) are automatically tracked for build dependency purposes:

```kotlin
step("charpad") {
    from("charset.ctm")

    charset {
        output = "build/charset.chr"
        nybbler {
            loOutput = "build/charset_lo.chr"
            hiOutput = "build/charset_hi.chr"
        }
    }
}

// The following files are all tracked as outputs:
// - build/charset.chr
// - build/charset_lo.chr
// - build/charset_hi.chr
```

This ensures proper build ordering and dependency resolution in the flow system.

### Comprehensive Example: All Filterable Output Types

This example demonstrates filters applied to all range-based output types available in CharPad:

```kotlin
step("all-outputs-with-filters") {
    description = "Process all available output types with filters"
    from("complete_charset.ctm")

    // Charset - split into nibbles
    charset {
        output = "build/charset.chr"
        nybbler {
            loOutput = "build/charset_lo.chr"
            hiOutput = "build/charset_hi.chr"
        }
    }

    // Charset attributes - use interleaver
    charsetAttributes {
        output = "build/attributes.bin"
        interleaver {
            outputs = listOf(
                "build/attr_stream0.bin",
                "build/attr_stream1.bin"
            )
        }
    }

    // Charset colours - split into nibbles
    charsetColours {
        output = "build/colours.bin"
        nybbler {
            loOutput = "build/colours_lo.bin"
            hiOutput = "build/colours_hi.bin"
        }
    }

    // Charset materials - use interleaver
    charsetMaterials {
        output = "build/materials.bin"
        interleaver {
            outputs = listOf(
                "build/mat_0.bin",
                "build/mat_1.bin",
                "build/mat_2.bin",
                "build/mat_3.bin"
            )
        }
    }

    // Charset screen colours - split into nibbles
    charsetScreenColours {
        output = "build/screen_colours.bin"
        nybbler {
            loOutput = "build/screen_lo.bin"
            hiOutput = "build/screen_hi.bin"
        }
    }

    // Tiles - use interleaver with range
    tiles {
        output = "build/tiles.bin"
        start = 0
        end = 256
        interleaver {
            outputs = listOf(
                "build/tiles_even.bin",
                "build/tiles_odd.bin"
            )
        }
    }

    // Tile tags - split into nibbles
    tileTags {
        output = "build/tile_tags.bin"
        nybbler {
            loOutput = "build/tags_lo.bin"
            hiOutput = "build/tags_hi.bin"
        }
    }

    // Tile colours - use interleaver
    tileColours {
        output = "build/tile_colours.bin"
        interleaver {
            outputs = listOf(
                "build/tilecolour_0.bin",
                "build/tilecolour_1.bin"
            )
        }
    }

    // Tile screen colours - split into nibbles
    tileScreenColours {
        output = "build/tile_screen_colours.bin"
        nybbler {
            loOutput = "build/tsc_lo.bin"
            hiOutput = "build/tsc_hi.bin"
        }
    }

    // Map - also supports filters (produces binary data)
    map {
        output = "build/map.bin"
        left = 0
        top = 0
        right = 40
        bottom = 25
        nybbler {
            loOutput = "build/map_lo.bin"
            hiOutput = "build/map_hi.bin"
        }
    }

    // NOTE: Metadata output does NOT support filters
    // (generates text headers, not binary data)
    meta {
        output = "build/header.h"
        namespace = "GAME"
        prefix = "ASSET_"
    }
}
```

This comprehensive example shows:
- All 10 binary output types that support filters (9 range-based + map)
- Both nybbler and interleaver filters being used
- Mix of filtered and non-filtered outputs in the same step
- Range parameters combined with filters (tiles example)
- Region parameters combined with filters (map example)
- Metadata output without filters (as it generates text headers)
