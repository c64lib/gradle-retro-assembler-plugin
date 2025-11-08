# Action Plan for Issue #120: Implement Flow Step for PNG Image Processing

## Issue Description

Implement a comprehensive flow step for preprocessing PNG image files. This involves reusing existing Image domain logic and use cases, while enhancing the flow step DSL with appropriate capabilities that mirror all features available in the legacy image DSL. The goal is to provide a consistent, modern interface for image processing within the flows pipeline system.

---

## Relevant Codebase Parts

### 1. **Image Domain & Use Cases** (`processors/image/src/main/kotlin/`)
   - **Image.kt**: Core domain model representing 2D pixel grids with RGBA color support
   - **ReadSourceImageUseCase.kt**: Reads PNG files via ReadImagePort interface
   - **WriteImageUseCase.kt**: Writes images in SPRITE or BITMAP format using WriteSpritePort and WriteCharsetPort
   - **CutImageUseCase.kt**: Cuts a region from image (left, top, width, height)
   - **ExtendImageUseCase.kt**: Extends image canvas with fill color
   - **SplitImageUseCase.kt**: Splits image into sub-tiles
   - **FlipImageUseCase.kt**: Flips image on X or Y axis
   - **ReduceResolutionUseCase.kt**: Reduces resolution with scale factors

   **Reasoning**: These use cases contain all the image transformation logic needed for the flow step. The flow step should orchestrate these use cases rather than reimplementing functionality.

### 2. **Flow Step Domain Structure** (`flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/`)
   - **Flow.kt**: Contains abstract `FlowStep` base class with `execute()`, `validate()`, and `getConfiguration()` methods
   - **steps/ImageStep.kt**: Placeholder step that needs implementation
   - **config/ProcessorConfig.kt**: Contains ImageConfig, ImageFormat, and other image-related enums

   **Reasoning**: Understanding the base FlowStep contract is essential for properly implementing ImageStep. ProcessorConfig defines the configuration model for image processing.

### 3. **Working Flow Step Example: CharpadStep** (`flows/src/main/kotlin/`)
   - **domain/steps/CharpadStep.kt**: Domain implementation with port injection and command execution
   - **domain/port/CharpadPort.kt**: Port interface for charpad processing
   - **domain/config/CharpadCommand.kt** & **CharpadOutputs.kt**: Command and output models
   - **adapters/in/gradle/dsl/CharpadStepBuilder.kt**: DSL builder with nested configurations
   - **adapters/in/gradle/tasks/CharpadTask.kt**: Gradle task that orchestrates execution

   **Reasoning**: CharpadStep is the closest working example of a flow step implementation. It demonstrates the proper architecture pattern: domain step → port interface → adapter implementation → Gradle task.

### 4. **Legacy Image DSL** (`shared/gradle/src/main/kotlin/com/github/c64lib/rbt/shared/gradle/dsl/`)
   - **ImagePipelineExtension.kt**: Root pipeline with input file
   - **ImageTransformationExtension.kt**: Base for all transformations (supports chaining)
   - **ImageFlipExtension.kt**, **ImageCutExtension.kt**, **ImageExtendExtension.kt**, **ImageSplitExtension.kt**, **ImageReduceResolutionExtension.kt**: Specific transformation extensions
   - **ImageWriterExtension.kt**, **SpriteWriter.kt**, **BitmapWriter.kt**: Output writers

   **Reasoning**: These legacy extensions define all capabilities that must be replicated in the new flow step DSL. They show what features users expect to be available.

### 5. **Image Adapters** (`processors/image/adapters/`)
   - **in/gradle/ProcessImage.kt**: Legacy Gradle task that chains transformations
   - **out/png/ReadPngImageAdapter.kt**: PNG reader implementation
   - **out/file/C64SpriteWriter.kt** & **C64CharsetWriter.kt**: Format writers

   **Reasoning**: These adapters implement the ports defined in use cases. They provide the actual technology-specific implementations needed.

### 6. **Infrastructure Integration** (`infra/gradle/`)
   - Must add image processing modules as `compileOnly` dependencies
   - Flow step registration mechanism

   **Reasoning**: The gradle plugin entry point needs to be aware of the new flow step for it to be available to users.

---

## Root Cause Hypothesis

**Primary Cause**: The ImageStep implementation is currently a skeleton placeholder without:
1. Proper integration with existing Image domain use cases
2. Command/output model classes to represent image operations
3. Port interface for image processing
4. DSL builder that exposes all legacy capabilities
5. Gradle task implementation that orchestrates execution

**Secondary Issues**:
- The DSL builder is too basic and doesn't support the full transformation pipeline (cut → split → flip → extend → reduce)
- Configuration model exists but isn't fully utilized in the step implementation
- No integration points defined for adapting legacy ProcessImage logic to the new flow step architecture

**Most Likely Cause**: The ImageStep was created as a framework placeholder but never fully implemented because it requires understanding how to properly adapt the existing image processing pipeline (which was designed for standalone Gradle tasks) into the flows architecture (which uses ports and dependency injection).

---

## Investigation Questions

### Self-Reflection Questions

1. How should the transformation pipeline be modeled in the flow step? Should transformations be represented as nested objects or as a fluent builder chain?
   - **ANSWERED**: Use fluent builder chain pattern (following CharpadStep/SpritepadStep precedent)

2. What output model should ImageStep use? Should it support multiple output files (like legacy split outputs) or a single primary output?
   - **ANSWERED**: Support multiple output files for split operations, similar to legacy behavior

3. Should the flow step expose all transformation options as top-level DSL methods, or should they be nested under a transformations() block?
   - **ANSWERED**: Expose as top-level DSL methods following CharpadStep/SpritepadStep precedent

4. How should the port interface bridge between the flow step domain and the existing image processing use cases? Should it wrap multiple use cases or create a unified ImageProcessingPort?
   - **ANSWERED**: Create a unified ImagePort that internally orchestrates multiple use cases

5. What error handling and validation should occur at each level (domain, builder, task)?
   - **ANSWERED**: Both build-time (DSL builder validation) and execution-time (task validation) depending on data type:
     - Build-time: Configuration properties (file paths, dimensions, formats)
     - Execution-time: Runtime state (file existence, image dimensions compatibility)

### Questions for Others (Answered)

1. Are there any existing flow step implementations for other processors (beyond Charpad/Spritepad) that I should reference?
   - **ANSWERED**: No, Charpad/Spritepad are the only references available. Use these as the template.

2. What's the expected behavior if a user chains multiple image transformations? Should they be applied sequentially, or are there any optimization opportunities?
   - **ANSWERED**: Transformations should be applied sequentially. No optimization opportunities needed at this time.

3. Should the ImageStep support the same output formats as the legacy DSL (sprite and bitmap), or are there additional formats needed?
   - **ANSWERED**: Support only legacy behaviors (sprite and bitmap formats). No additional formats required.

4. Are there any performance considerations when processing large PNG files through the flow step pipeline?
   - **ANSWERED**: No specific performance considerations identified. Standard processing is acceptable.

5. Should the flow step validation happen at DSL build time or at task execution time?
   - **ANSWERED**: Both, depending on validation type:
     - DSL build-time: Type safety and basic configuration validation
     - Task execution-time: File existence, image compatibility checks

---

## Next Steps

### Phase 1: Analysis & Design

1. **Study CharpadStep Architecture** (Rationale: It's the best working example)
   - Examine CharpadStep domain class, CharpadPort interface, and CharpadCommand structure
   - Understand how CharpadTask injects the port and calls execute()
   - Document the pattern for ImageStep

2. **Map Legacy Capabilities to New Architecture** (Rationale: Ensure no features are lost)
   - List each legacy ImageTransformationExtension class (Cut, Flip, Extend, Split, ReduceResolution)
   - For each transformation, determine how it should be represented in the new DSL builder
   - Decide whether to use nested builders or method chaining

3. **Design ImageCommand and ImageOutputs Classes** (Rationale: Required for domain step execution)
   - Create data classes representing a complete image processing pipeline
   - Define what outputs the step can produce (sprite, bitmap, or both)
   - Document the command structure for port implementation

4. **Design ImagePort Interface** (Rationale: Bridges domain to adapters)
   - Determine if one port handles all image processing or if multiple ports are needed
   - Define port methods that accept ImageCommand and return ImageOutputs
   - Ensure port can be implemented by adapting existing ProcessImage task logic

### Phase 2: Domain Implementation

5. **Create ImageCommand Class** (Rationale: Required for domain step)
   - Define data class representing the complete image processing configuration
   - Include fields for: inputFile, transformations, outputFormat, and other configuration
   - Add validation logic if needed

6. **Create ImageOutputs Class** (Rationale: Represents step results)
   - Define data class with List<Pair<String, File>> for multiple outputs
   - Support sprite and bitmap output formats
   - Handle indexed naming for split outputs (e.g., tile_0, tile_1)

7. **Create ImagePort Interface** (Rationale: Define contract for adapters)
   - In `flows/src/main/kotlin/.../flows/domain/port/ImagePort.kt`
   - Define `fun process(command: ImageCommand): ImageOutputs` method
   - Add any necessary validation or capability methods

8. **Implement ImageStep Domain Class** (Rationale: Core domain logic)
   - Extend FlowStep base class
   - Inject ImagePort dependency
   - Implement execute() to create ImageCommand from inputs and call port
   - Implement validate() to check inputs and configuration
   - Implement getConfiguration() to return ProcessorConfig

### Phase 3: DSL & Gradle Task Implementation

9. **Enhance ImageStepBuilder** (Rationale: Provide user-facing DSL)
   - Add methods for each transformation: cut(), flip(), extend(), split(), reduceResolution()
   - Support method chaining and nested builders for complex configurations
   - Add output configuration methods: sprite(), bitmap()
   - Add validation in build() method
   - Follow CharpadStepBuilder pattern with nested builder classes if needed

10. **Implement ImageTask Gradle Class** (Rationale: Execute step in Gradle)
    - Extend BaseFlowStepTask
    - Implement executeStepLogic() to create ImageAdapter and inject as port
    - Implement validateStep() to delegate to domain validation
    - Follow CharpadTask pattern exactly

### Phase 4: Adapter Implementation

11. **Create ImageAdapter** (Rationale: Implement ImagePort with existing logic)
    - Implement ImagePort interface in `flows/adapters/in/gradle/`
    - Adapt existing ProcessImage task logic to work as a port
    - Use existing ReadPngImageAdapter, CutImageUseCase, etc.
    - Handle chaining of transformations
    - Write outputs based on format configuration

12. **Update infra/gradle Dependencies** (Rationale: Make modules available to plugin)
    - Add `compileOnly` dependencies for image processor modules (if not already present)
    - Follow pattern documented in CLAUDE.md

### Phase 5: Testing

13. **Create ImageStep Domain Tests** (Rationale: Verify core logic)
    - Test execute() with various configurations
    - Test validate() with valid and invalid inputs
    - Use BehaviorSpec pattern with mock ImagePort
    - Test transformation chains

14. **Create ImageStepBuilder Tests** (Rationale: Verify DSL)
    - Test DSL builder methods for each transformation
    - Test invalid configurations
    - Test build() creates correct ImageStep instance

15. **Create ImageTask Tests** (Rationale: Verify Gradle integration)
    - Test task execution with sample PNG files
    - Test output file creation
    - Test error handling

16. **Integration Test** (Rationale: Verify end-to-end)
    - Create test PNG files and expected outputs
    - Test full flow step execution through Gradle task
    - Verify sprite and bitmap output formats
    - Test transformation chains (e.g., cut → flip → split)

### Phase 6: Documentation & Cleanup

17. **Update Build Files** (Rationale: Ensure proper module dependencies)
    - Add image processor modules to infra/gradle if needed
    - Verify all test dependencies are included

18. **Add Code Comments** (Rationale: Explain architecture decisions)
    - Document why certain design choices were made
    - Add clarifying comments to complex transformation logic

19. **Run Full Build & Tests** (Rationale: Verify no regressions)
    - Execute `./gradlew build` to ensure all tests pass
    - Run specific image flow tests with `./gradlew :flows:test`

---

## Additional Notes

### Architecture Pattern to Follow

The implementation should follow the hexagonal architecture pattern already established in the codebase:

```
Domain Layer (ImageStep)
↓ (depends on port)
Port Interface (ImagePort)
↑ (implemented by)
Adapter Layer (ImageAdapter)
↓ (uses)
Existing Image Use Cases & Domain Logic
```

### Key Implementation Considerations

1. **Fluent Builder DSL**: Use fluent builder chain pattern for transformation methods (cut(), flip(), extend(), split(), reduceResolution()). Expose these as top-level DSL methods, not nested under a transformations() block. This follows the CharpadStep/SpritepadStep precedent.

2. **Sequential Transformation Execution**: Transformations must be applied sequentially in the order specified by the user. No optimization or reordering is required. The ImagePort should apply transformations in the order they appear in the command.

3. **Multiple Outputs Support**: Legacy image processing could produce multiple output files (from split operations). The ImageOutputs model must support this with a List<Pair<String, File>> structure. Handle indexed naming for split outputs (e.g., tile_0, tile_1, tile_2).

4. **Output Formats**: Support only the legacy formats: SPRITE and BITMAP. No additional formats are required at this time.

5. **Unified ImagePort**: Create a single ImagePort interface that internally orchestrates multiple image use cases (Read, Cut, Flip, Extend, Split, ReduceResolution, Write). This hides the complexity of use case coordination from the domain layer.

6. **Two-Level Validation**:
   - **Build-time (DSL)**: Validate configuration properties (file paths, dimensions, formats) when ImageStep is built
   - **Execution-time (Task)**: Validate runtime state (file existence, image dimensions compatibility) when ImageTask executes

   This ensures both type safety and runtime correctness.

7. **Reuse, Don't Reimplement**: All image transformation logic already exists in use cases. The ImagePort should orchestrate these use cases rather than reimplementing functionality.

8. **Configuration Alignment**: The ImageConfig data class in ProcessorConfig.kt already contains targetFormat, paletteOptimization, dithering, etc. The DSL and domain should use this existing structure.

### Potential Blockers

1. **ProcessImage Task Complexity**: The legacy ProcessImage task handles transformation chaining recursively. Adapting this to the port pattern may require careful refactoring.

2. **File Output Handling**: The flows architecture may have specific expectations about how output files are named and tracked. This should be clarified with existing flow step implementations.

3. **Dependency Injection**: Ensuring the ImageAdapter is properly instantiated and injected as the ImagePort may require understanding the existing DI mechanism in flows.

### Success Criteria

- ✅ ImageStep domain class fully implements FlowStep contract
- ✅ All legacy image DSL capabilities are available in new flow step DSL
- ✅ DSL builder allows intuitive, fluent configuration of transformations
- ✅ Gradle task properly executes image processing
- ✅ Sprite and bitmap output formats both work
- ✅ Transformation chains work correctly
- ✅ All tests pass
- ✅ No regressions in existing image processing functionality
- ✅ Code follows hexagonal architecture pattern
- ✅ Port interface cleanly separates domain from adapters
