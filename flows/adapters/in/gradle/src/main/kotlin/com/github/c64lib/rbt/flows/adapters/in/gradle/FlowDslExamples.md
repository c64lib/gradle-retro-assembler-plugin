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
