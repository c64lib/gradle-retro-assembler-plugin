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
                from("src/assets/charset")
                to("build/processed/charset")
                configure("format", "bin")
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

## Complex Dependency Chain

```kotlin
flows {
    flow("preprocessing") {
        description = "Process all assets"
        
        parallel {
            step("charpad") {
                from("src/assets/charset")
                to("build/processed/charset")
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

## Performance Benefits

With the new flow syntax:
- **Asset processing flows** (charpad, spritepad, goattracker, image) run in parallel
- **Dependency resolution** runs in parallel with asset processing  
- **Compilation** only starts when both preprocessing flows complete
- **Testing** and **Packaging** can run in parallel after compilation

This can reduce build times from sequential execution (A + B + C + D) to parallel execution (max(A, B) + C + max(D, E)), potentially cutting build times in half or more for complex projects.
