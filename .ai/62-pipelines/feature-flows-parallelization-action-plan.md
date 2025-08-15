# Action Plan for Flows Parallelization

## Issue Description
Implement parallel execution of build flows in the gradle-retro-assembler-plugin to improve build performance by allowing independent tasks to run concurrently while respecting dependencies.

## Relevant Codebase Parts
1. **flows/** - Main bounded context for flow management following hexagonal architecture
2. **flows/adapters/in/gradle/** - Contains FlowDsl.kt and FlowsExtension.kt for Gradle DSL integration
3. **flows/src/main/kotlin/domain/** - Core domain logic for flow execution and dependency management
4. **buildSrc/** - Contains custom Gradle plugins for different module types
5. **RetroAssemblerPlugin** - Main plugin class that needs to be updated for flow registration

## Root Cause Hypothesis
The current implementation lacks a proper mechanism to leverage Gradle's built-in parallelization capabilities. The solution requires generating actual Gradle tasks that can be parallelized by Gradle's execution engine, rather than implementing custom parallelization logic in the domain layer.

## Investigation Questions
1. How can we leverage Gradle's task parallelization instead of implementing custom threading?
2. What is the best way to generate Gradle tasks dynamically from flow definitions?
3. How should the outbound adapter communicate with Gradle's task system?
4. What information needs to be passed between the domain layer and generated tasks?
5. How do we ensure proper dependency setup between generated tasks?

## Next Steps

### Phase 1: Foundation (Steps 1-3) ✅ COMPLETED
1. ✅ **Analyze Current Architecture** - Understanding current flows structure and hexagonal architecture
   - Rationale: Understanding current architecture is essential for designing parallel flows

2. ✅ **Define Flow DSL Syntax** - Created Kotlin DSL syntax for defining flows in build.gradle.kts files in `flows/adapters/in/gradle/`
   - Rationale: Users need an intuitive way to define parallel execution flows
   - **Implementation**: FlowDsl.kt, FlowsExtension.kt, and examples located in adapters/in/gradle following hexagonal architecture

3. ✅ **Create Flow Dependency Graph** - Implement logic to build and validate dependency graphs between flows
   - Rationale: Essential for determining which flows can execute in parallel

### Phase 2: Core Implementation (Steps 4-7) ✅ COMPLETED
4. ✅ **Implement Flow Domain Layer** - Build the core business logic for flow execution and dependency management
   - **CORRECTED APPROACH**: Focus on logical flow representation and dependency resolution, NOT custom parallelization
   - Rationale: Domain should contain pure business logic for flow definitions and dependencies, leaving parallelization to Gradle
   - Implementation: Create domain models for Flow, FlowDependency, and FlowGraph without threading concerns

5. ✅ **Create Outbound Gradle Adapter** - Build adapter that generates actual Gradle tasks from flow definitions
   - **NEW APPROACH**: Create outbound adapter in `flows/adapters/out/gradle/` that:
     - Takes flow definitions from domain layer
     - Generates corresponding Gradle tasks dynamically
     - Sets up proper task dependencies based on flow dependencies
     - Passes references to logical flow execution to generated tasks
   - Rationale: Leverages Gradle's native parallelization instead of custom threading

6. ✅ **Enhance Plugin Registration (o4-mini)** - Update RetroAssemblerPlugin to use the outbound adapter for task generation
   - Implementation: Plugin calls outbound adapter to generate tasks instead of registering flows directly
   - Rationale: Integrates flow-based task generation into main plugin lifecycle

7. ✅ **Implement Task Execution Bridge (o4-mini)** - Create mechanism to execute logical flows within generated Gradle tasks
   - Implementation: Generated tasks receive references to domain flow objects and execute them
   - Rationale: Bridges between Gradle's task execution and domain logic

### Phase 3: Integration & Testing (Steps 8-10)
8. ✅ **Integration Testing (manual)** - Test flow execution with various dependency scenarios
9. **Performance Validation** - Measure and validate performance improvements
10. **Documentation Update** - Update user documentation and examples

### Phase 4: Extensibility Enhancement (Steps 11-15) ✅ COMPLETED
11. ✅ **Fix Flow DSL nesting issue (o4-mini)** - Revise FlowDsl.kt and FlowsExtension.kt to prevent unnecessary nested DSL elements in the flows DSL (simplify DSL structure and avoid redundant nesting)
12. ✅ **Fix error message interpolation (o4-mini)** - Update error message generation in Gradle tasks to correctly evaluate string templates (use proper Kotlin string interpolation or Gradle logging APIs to display validation issues instead of literal syntax)
13. ✅ **Remove explicit parallel DSL support (o4-mini)** - Removed `parallel` function and `ParallelStepsBuilder`, updated Flow DSL and examples to derive parallel execution from dependencies
14. ✅ **Fix Source File Input Validation (GitHub Copilot)** - Modify validation logic to distinguish between source file inputs and produced artifacts
   - **Issue**: Current validation flags all consumed artifacts without producers as errors, but source files (e.g., .asm, .ctm, .spd files) don't need producers
   - **Solution**: Enhance FlowArtifact with source file indicators and update validation to exclude source files from missing producer checks
   - **Files to modify**: 
     - `flows/src/main/kotlin/domain/Flow.kt` - Add source file identification to FlowArtifact
     - `flows/src/main/kotlin/domain/FlowDependencyGraph.kt` - Update validation logic to skip source file artifacts
   - Rationale: Source files are the starting point of the build pipeline and should not be flagged as missing when not produced by flows

15. ✅ **Implement Extensible Step Architecture (GitHub Copilot)** - Refactor FlowStep from data class to abstract class hierarchy for better extensibility and command execution
   - **Current Issue**: FlowStep is a data class with generic configuration Map, limiting type safety and extensibility
   - **Solution**: 
     - Convert FlowStep to abstract class with common properties (name, inputs, outputs)
     - Remove configuration Map to eliminate generic key-value storage
     - Create CommandStep concrete implementation for CLI command execution
     - Implement convenient command building with + operator for parameters
     - Support any input/output paths for maximum flexibility
   - **Files to modify**:
     - `flows/src/main/kotlin/domain/Flow.kt` - Convert FlowStep to abstract class
     - `flows/src/main/kotlin/domain/CommandStep.kt` - New concrete command step implementation
     - `flows/adapters/in/gradle/src/main/kotlin/.../FlowDsl.kt` - Update DSL to work with abstract steps
   - **Benefits**: Type-safe step definitions, easier testing, better IDE support, extensible for future step types
   - Rationale: Abstract class hierarchy provides better type safety and extensibility than generic data class with Map configuration

### Phase 5: Advanced Task Implementation (Step 16) ✅ COMPLETED
16. ✅ **Implement Dedicated Step Tasks with Gradle Annotations (GitHub Copilot)** - Create dedicated Gradle task classes for each flow step type with proper @Input/@Output annotations and file-based dependencies
   - **Issue**: Current FlowExecutionTask is generic and doesn't leverage Gradle's incremental build capabilities or proper input/output tracking
   - **Solution**:
     - Create dedicated task classes for each step type (CharpadTask, SpritepadTask, AssembleTask, etc.) in `flows/adapters/in/gradle/`
     - Add @InputFiles/@InputDirectory annotations for source files and dependencies
     - Add @OutputFiles/@OutputDirectory annotations for generated artifacts
     - Implement automatic task dependency detection based on file inputs/outputs
     - Update FlowTasksGenerator to create specific task types instead of generic FlowExecutionTask
     - Ensure tasks are only re-executed when inputs change (incremental builds)
     - **Enhanced**: Add dedicated DSL constructs for each concrete flow step type with type-specific configuration
   - **Files implemented**:
     - `flows/adapters/in/gradle/src/main/kotlin/.../tasks/BaseFlowStepTask.kt` - Base class with common input/output tracking
     - `flows/adapters/in/gradle/src/main/kotlin/.../tasks/CharpadTask.kt` - Dedicated Charpad task implementation
     - `flows/adapters/in/gradle/src/main/kotlin/.../tasks/SpritepadTask.kt` - Dedicated Spritepad task implementation
     - `flows/adapters/in/gradle/src/main/kotlin/.../tasks/AssembleTask.kt` - Dedicated Assembly task implementation
     - `flows/adapters/in/gradle/src/main/kotlin/.../tasks/GoattrackerTask.kt` - Dedicated GoatTracker task implementation
     - `flows/adapters/in/gradle/src/main/kotlin/.../tasks/ImageTask.kt` - Dedicated Image processing task implementation
     - `flows/adapters/in/gradle/src/main/kotlin/.../tasks/CommandTask.kt` - Dedicated Command execution task implementation
     - `flows/adapters/in/gradle/src/main/kotlin/.../FlowTasksGenerator.kt` - Updated to generate specific task types
   - **Enhancement Required - Dedicated DSL Constructs**:
     - `flows/adapters/in/gradle/src/main/kotlin/.../FlowDsl.kt` - Add dedicated DSL functions for each step type:
       - `charpadStep(name: String, configure: CharpadStepBuilder.() -> Unit)` - Type-safe Charpad configuration
       - `spritepadStep(name: String, configure: SpritepadStepBuilder.() -> Unit)` - Type-safe Spritepad configuration  
       - `goattrackerStep(name: String, configure: GoattrackerStepBuilder.() -> Unit)` - Type-safe GoatTracker configuration
       - `assembleStep(name: String, configure: AssembleStepBuilder.() -> Unit)` - Type-safe Assembly configuration
       - `imageStep(name: String, configure: ImageStepBuilder.() -> Unit)` - Type-safe Image processing configuration
     - Create dedicated step builder classes with processor-specific options:
       - `CharpadStepBuilder` - Configure .ctm processing options (compression, format, etc.)
       - `SpritepadStepBuilder` - Configure .spd processing options (sprite format, optimization, etc.)
       - `GoattrackerStepBuilder` - Configure .sng processing options (export format, optimization, etc.)
       - `AssembleStepBuilder` - Configure assembly options (CPU type, symbols, optimization, etc.)
       - `ImageStepBuilder` - Configure image conversion options (format, palette, dithering, etc.)
   - **Enhanced DSL Usage Examples**:
     ```kotlin
     flows {
         flow("assets") {
             charpadStep("charset") {
                 from("src/assets/charset.ctm")
                 to("build/assets/charset.chr", "build/assets/charset.map")
                 compression = CharpadCompression.NONE
                 exportFormat = CharpadFormat.STANDARD
             }
             
             spritepadStep("sprites") {
                 from("src/assets/sprites.spd") 
                 to("build/assets/sprites.spr")
                 optimization = SpriteOptimization.SIZE
                 format = SpriteFormat.MULTICOLOR
             }
             
             goattrackerStep("music") {
                 from("src/music/theme.sng")
                 to("build/music/theme.sid", "build/music/theme.asm")
                 exportFormat = GoattrackerFormat.SID_AND_ASM
                 optimization = true
             }
         }
         
         flow("compilation") {
             dependsOn("assets")
             
             assembleStep("main") {
                 from("src/main/main.asm")
                 to("build/output/main.prg")
                 cpu = CpuType.MOS6510
                 generateSymbols = true
                 optimization = AssemblyOptimization.SPEED
                 includePaths("build/assets", "lib/c64lib")
             }
         }
     }
     ```
   - **Benefits**: 
     - Proper incremental build support (tasks only run when inputs change)
     - Automatic dependency resolution based on file relationships
     - Better Gradle integration and performance
     - Enhanced parallel execution through file-based dependency tracking
     - **Enhanced**: Type-safe DSL with processor-specific configuration options
     - **Enhanced**: IDE auto-completion and validation for step configurations
     - **Enhanced**: Compile-time validation of step parameters and file paths
   - Rationale: Dedicated task classes with proper annotations enable Gradle's incremental build system and automatic parallelization based on file dependencies. Enhanced DSL provides type-safe, processor-specific configuration with better developer experience.

### Phase 6: Enhanced DSL Implementation (Step 17) ✅ COMPLETED
17. ✅ **Implement Type-Safe DSL Constructs for Concrete Flow Steps (GitHub Copilot)** - Create dedicated DSL functions and builders for each processor type with type-specific configuration options
   - **Issue**: Current generic step() function lacks type safety and processor-specific configuration options
   - **Solution**:
     - Create dedicated DSL functions for each processor type in FlowDsl.kt
     - Implement type-safe builder classes with processor-specific configuration options
     - Add enum classes for configuration values (compression types, formats, CPU types, etc.)
     - Update FlowTasksGenerator to handle type-specific step configurations
     - Create domain model classes for each step type extending FlowStep
   - **Files implemented**:
     - `flows/src/main/kotlin/domain/steps/CharpadStep.kt` - Domain model for Charpad processing steps
     - `flows/src/main/kotlin/domain/steps/SpritepadStep.kt` - Domain model for Spritepad processing steps  
     - `flows/src/main/kotlin/domain/steps/GoattrackerStep.kt` - Domain model for GoatTracker processing steps
     - `flows/src/main/kotlin/domain/steps/AssembleStep.kt` - Domain model for Assembly processing steps
     - `flows/src/main/kotlin/domain/steps/ImageStep.kt` - Domain model for Image processing steps
     - `flows/src/main/kotlin/domain/config/ProcessorConfig.kt` - Configuration enums and data classes
     - `flows/adapters/in/gradle/src/main/kotlin/.../dsl/CharpadStepBuilder.kt` - Type-safe Charpad DSL builder
     - `flows/adapters/in/gradle/src/main/kotlin/.../dsl/SpritepadStepBuilder.kt` - Type-safe Spritepad DSL builder
     - `flows/adapters/in/gradle/src/main/kotlin/.../dsl/GoattrackerStepBuilder.kt` - Type-safe GoatTracker DSL builder
     - `flows/adapters/in/gradle/src/main/kotlin/.../dsl/AssembleStepBuilder.kt` - Type-safe Assembly DSL builder
     - `flows/adapters/in/gradle/src/main/kotlin/.../dsl/ImageStepBuilder.kt` - Type-safe Image DSL builder
     - `flows/adapters/in/gradle/src/main/kotlin/.../FlowDsl.kt` - Enhanced with dedicated DSL functions
     - `flows/adapters/in/gradle/src/main/kotlin/.../FlowTasksGenerator.kt` - Updated to handle processor-specific step types
   - **Configuration Options Implemented**:
     - **Charpad**: Compression (none, RLE, exomizer), export format (standard, optimized, c64lib), tile size, charset optimization
     - **Spritepad**: Sprite format (hires, multicolor), optimization (size, speed), export format (raw, optimized), animation support
     - **GoatTracker**: Export format (SID only, ASM only, both), optimization level, frequency (PAL, NTSC), channels, filter support
     - **Assembly**: CPU type (6502, 6510, 65C02), optimization level, symbol generation, include paths, defines, verbose mode
     - **Image**: Target format (Koala, Art Studio, etc.), palette optimization, dithering algorithm, background color, transparency support
   - **Benefits**:
     - Type-safe configuration with compile-time validation
     - IDE auto-completion for all configuration options
     - Processor-specific validation and error messages
     - Better integration with existing processor modules
     - Cleaner, more maintainable DSL syntax
   - Rationale: Type-safe DSL constructs provide better developer experience, catch configuration errors at compile time, and integrate seamlessly with the existing processor architecture.
