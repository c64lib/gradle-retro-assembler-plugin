# Development Plan: Issue 57 - Exomizer

## Feature Description

Implement a new **crunchers** domain subdomain for **Exomizer**, a data compression utility used in retro computing to reduce binary file sizes. Exomizer is particularly useful in Commodore 64 development where memory is limited. This implementation will follow the hexagonal architecture pattern already established in the project.

The initial phase will implement two use cases:
1. **Raw compression** - Basic file compression using Exomizer's raw mode
2. **Memory compression** - Compression with memory options for optimized decompression

This new domain will integrate with the flows DSL to allow users to define Exomizer steps in their build pipelines, similar to how CharPad, SpritePad, and GoatTracker processors are currently integrated.

## Root Cause Analysis

The project currently lacks support for data crunching/compression. As the Gradle Retro Assembler Plugin expands to support more aspects of retro game development, it's essential to add compression capabilities. Exomizer is a well-established tool in the Commodore 64 community and will provide users with native integration for compressing binary assets and code within their Gradle build pipelines.

## Relevant Code Parts

This implementation will create new files and follow patterns from existing processor domains:

**New domain module structure:**
- `crunchers/exomizer/src/main/kotlin/com/github/c64lib/rbt/crunchers/exomizer/`
  - `domain/` - Domain logic and data structures
  - `usecase/` - Use cases (CrunchRawUseCase, CrunchMemUseCase)
  - `usecase/port/` - Port interfaces (ExecuteExomizerPort)
- `crunchers/exomizer/adapters/in/gradle/` - Gradle task adapter
- `crunchers/exomizer/build.gradle.kts` - Domain module build config
- `crunchers/exomizer/adapters/in/gradle/build.gradle.kts` - Adapter build config

**Flows integration (updated files):**
- `flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/steps/ExomizerStep.kt` - New step class
- `flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/port/ExomizerPort.kt` - Step port interface
- `flows/adapters/in/gradle/src/main/kotlin/.../dsl/ExomizerStepBuilder.kt` - DSL builder
- `flows/adapters/in/gradle/src/main/kotlin/.../FlowDsl.kt` - Add exomizerStep method

**Plugin integration (updated file):**
- `infra/gradle/build.gradle.kts` - Add crunchers/exomizer dependencies

**Settings file (updated):**
- `settings.gradle.kts` - Register new submodules

**Test files:**
- `crunchers/exomizer/src/test/kotlin/...` - Domain/use case tests
- `crunchers/exomizer/adapters/in/gradle/src/test/kotlin/...` - Adapter tests
- `flows/adapters/in/gradle/src/test/kotlin/.../ExomizerStepBuilderTest.kt` - Step builder tests

## Questions

### Self-Reflection Questions

1. **Edge cases for file handling**: What should happen if input file doesn't exist? If output directory doesn't exist? Should we validate file permissions?

2. **Exomizer invocation options**: Beyond raw and mem modes, are there other Exomizer options we should support in the initial implementation? (e.g., compression levels, target memory addresses)

3. **Output format**: Should Exomizer produce just the compressed binary, or should it also generate a decompression stub? Will we need multiple output files?

4. **Error handling**: How should we handle Exomizer execution failures? Should we capture stderr for detailed error messages?

5. **File resolution**: Should the use case handle file path resolution, or should the adapter handle it before passing to the use case?

6. **Testing**: How will we test Exomizer integration without relying on the actual exomizer binary in unit tests? Should we mock the ExecuteExomizerPort?

### Questions for Clarification

1. **Raw mode specifics**: For the raw compression use case, what are the exact command-line parameters? (e.g., `exomizer raw -o <output> <input>`)

2. **Memory mode specifics**: For the memory compression use case, what memory-related options need to be configurable? What are typical defaults?

3. **Output files**: Will each use case produce one output file, or should we support multiple outputs (e.g., compressed data + decompression loader)?

4. **Step naming in DSL**: What should the Gradle DSL method be named? `exomizerStep()` or something more specific like `crunchWithExomizer()`?

5. **Validation rules**: What are critical validation rules for the use cases? (e.g., required parameters, valid ranges for memory options)

## Execution Plan

### Phase 1: Create Core Crunchers Domain and Exomizer Module

This phase sets up the foundational infrastructure for the new crunchers domain and the exomizer submodule.

1. **Step 1.1: Create module directory structure**
   - Create `crunchers/exomizer/src/main/kotlin/com/github/c64lib/rbt/crunchers/exomizer/` directory structure
   - Create `crunchers/exomizer/adapters/in/gradle/src/main/kotlin/...` directory structure
   - Create `crunchers/exomizer/src/test/kotlin/...` and adapter test directories
   - Deliverable: Directory structure ready for code
   - Testing: Verify directories exist with `ls` command
   - Safe to merge: Yes (structure only, no code)

2. **Step 1.2: Create Gradle build configuration files**
   - Create `crunchers/exomizer/build.gradle.kts` using `rbt.domain` plugin, dependencies on shared modules
   - Create `crunchers/exomizer/adapters/in/gradle/build.gradle.kts` using `rbt.adapter.inbound.gradle` plugin
   - Deliverable: Two build.gradle.kts files with correct plugin and dependency configuration
   - Testing: Run `./gradlew :crunchers:exomizer:build --dry-run` to verify configuration
   - Safe to merge: Yes (no code yet)

3. **Step 1.3: Update settings.gradle.kts and infra/gradle dependencies**
   - Add new module inclusions to `settings.gradle.kts`: `include(":crunchers:exomizer")`, `include(":crunchers:exomizer:adapters:in:gradle")`
   - Add compileOnly dependencies in `infra/gradle/build.gradle.kts` for both exomizer modules
   - Deliverable: Plugin can reference exomizer modules
   - Testing: Run `./gradlew projects` and verify exomizer modules appear
   - Safe to merge: Yes (structure integration only)

### Phase 2: Implement Domain Layer - Use Cases and Data Structures

This phase creates the core domain logic for compression operations.

1. **Step 2.1: Create Exomizer port interface**
   - Create `ExecuteExomizerPort.kt` in `crunchers/exomizer/src/main/kotlin/.../usecase/port/`
   - Define method signatures: `fun executeRaw(source: File, output: File): Unit` and `fun executeMem(source: File, output: File, memOptions: String): Unit`
   - Add Kdoc explaining port purpose
   - Deliverable: Port interface that abstracts Exomizer execution
   - Testing: Verify interface compiles
   - Safe to merge: Yes (interface definition)

2. **Step 2.2: Create domain data structures**
   - Create command/parameter data classes: `CrunchRawCommand`, `CrunchMemCommand`
   - Include fields for source file, output file, and mode-specific options
   - Use immutable Kotlin data classes
   - Deliverable: Command data classes ready for use cases
   - Testing: Verify data classes compile and support equality/hashing
   - Safe to merge: Yes (data structures)

3. **Step 2.3: Implement CrunchRawUseCase**
   - Create `CrunchRawUseCase.kt` in `usecase/` directory
   - Implement single public `apply` method taking `CrunchRawCommand`
   - Call `ExecuteExomizerPort` with source and output files
   - Add error handling with custom exception types (`StepExecutionException`)
   - Deliverable: Functional use case for raw compression
   - Testing: Unit test with mocked port, verify correct parameters passed
   - Safe to merge: Yes (use case with port injection)

4. **Step 2.4: Implement CrunchMemUseCase**
   - Create `CrunchMemUseCase.kt` in `usecase/` directory
   - Implement single public `apply` method taking `CrunchMemCommand` with memory options
   - Call `ExecuteExomizerPort` with source, output, and memory options
   - Add validation for memory option format/ranges
   - Add error handling matching CrunchRawUseCase pattern
   - Deliverable: Functional use case for memory-optimized compression
   - Testing: Unit test with mocked port, test validation rules
   - Safe to merge: Yes (use case with validation)

### Phase 3: Implement Adapter Layer - Gradle Integration

This phase creates the Gradle task adapter to expose Exomizer to end users.

1. **Step 3.1: Create Gradle task for raw crunching**
   - Create `CrunchRaw.kt` in `adapters/in/gradle/src/main/kotlin/.../adapters/in/gradle/`
   - Extend Gradle `DefaultTask`
   - Define input/output properties with Gradle file handling
   - Inject `CrunchRawUseCase` via constructor
   - Implement task action that resolves files and calls use case
   - Deliverable: Functional Gradle task for raw compression
   - Testing: Functional test using Gradle test fixtures, verify task executes
   - Safe to merge: Yes (task implementation)

2. **Step 3.2: Create Gradle task for memory crunching**
   - Create `CrunchMem.kt` in `adapters/in/gradle/src/main/kotlin/.../adapters/in/gradle/`
   - Extend Gradle `DefaultTask` with memory options property
   - Implement task action that validates memory options and calls use case
   - Follow same pattern as CrunchRaw task
   - Deliverable: Functional Gradle task for memory compression
   - Testing: Functional test with memory options configuration
   - Safe to merge: Yes (task implementation)

3. **Step 3.3: Implement ExecuteExomizerPort adapter**
   - Create `GradleExomizerAdapter.kt` in `adapters/out/gradle/` (or in `adapters/in/gradle/` if simpler)
   - Implement `ExecuteExomizerPort` interface
   - Use Gradle Workers API for parallel execution (or direct execution if not parallel)
   - Capture exomizer command output and map exit codes to meaningful errors
   - Deliverable: Working port implementation that executes exomizer binary
   - Testing: Integration test that executes actual exomizer binary
   - Safe to merge: Yes (port implementation)

### Phase 4: Create Flows Integration - Step and DSL Support

This phase integrates Exomizer into the flows pipeline orchestration system.

1. **Step 4.1: Create ExomizerStep data class**
   - Create `ExomizerStep.kt` in `flows/src/main/kotlin/.../flows/domain/steps/`
   - Extend `FlowStep` abstract base class
   - Support both raw and memory compression modes (via configuration)
   - Include immutable fields: `name`, `inputs`, `outputs`, `mode`, `memOptions` (optional)
   - Implement `execute()` method that validates port and calls appropriate use case
   - Implement `validate()` method with critical domain rules
   - Deliverable: Step class ready for flow pipelines
   - Testing: Unit test with mocked port, test validation logic
   - Safe to merge: Yes (step implementation)

2. **Step 4.2: Create ExomizerPort for flows**
   - Create `ExomizerPort.kt` in `flows/src/main/kotlin/.../flows/domain/port/`
   - Define methods: `fun crunchRaw(source: File, output: File): Unit` and `fun crunchMem(...): Unit`
   - This port abstracts the crunchers domain for the flows layer
   - Deliverable: Port interface for step integration
   - Testing: Verify interface compiles
   - Safe to merge: Yes (interface definition)

3. **Step 4.3: Create ExomizerStepBuilder DSL class**
   - Create `ExomizerStepBuilder.kt` in `flows/adapters/in/gradle/src/main/kotlin/.../dsl/`
   - Implement type-safe DSL builder pattern matching CharpadStepBuilder
   - Support configuration: `from()`, `to()`, `raw()`, `mem()`
   - Implement `build()` method returning configured `ExomizerStep`
   - Deliverable: DSL builder for Exomizer steps
   - Testing: Unit test with BehaviorSpec pattern, test all configuration paths
   - Safe to merge: Yes (builder implementation)

4. **Step 4.4: Integrate exomizerStep into FlowDsl**
   - Update `FlowDsl.kt` to add `exomizerStep()` method
   - Method signature: `fun exomizerStep(name: String, configure: ExomizerStepBuilder.() -> Unit)`
   - Follow existing pattern from `charpadStep()`, `spritepadStep()`, etc.
   - Deliverable: DSL method available to users
   - Testing: Test that method creates and returns correct step
   - Safe to merge: Yes (DSL integration)

5. **Step 4.5: Implement flows adapter for ExomizerPort**
   - Create adapter in `flows/adapters/in/gradle/` that implements `ExomizerPort`
   - Bridge between flows domain and crunchers domain
   - Instantiate `CrunchRawUseCase` and `CrunchMemUseCase` with port
   - Deliverable: Working port implementation for step execution
   - Testing: Integration test with ExomizerStep
   - Safe to merge: Yes (adapter implementation)

### Phase 5: Testing and Documentation

This phase ensures comprehensive test coverage and user-facing documentation.

1. **Step 5.1: Add comprehensive unit tests for use cases**
   - Test `CrunchRawUseCase` with mocked port
   - Test `CrunchMemUseCase` with valid and invalid memory options
   - Test error handling and exception mapping
   - Deliverable: Unit tests with high coverage
   - Testing: Run `./gradlew :crunchers:exomizer:test` and verify pass
   - Safe to merge: Yes (tests)

2. **Step 5.2: Add integration tests for Gradle tasks**
   - Test `CrunchRaw` and `CrunchMem` tasks with actual exomizer binary
   - Test file resolution, output generation, error handling
   - Deliverable: Integration tests for adapter layer
   - Testing: Run `./gradlew :crunchers:exomizer:adapters:in:gradle:test`
   - Safe to merge: Yes (tests)

3. **Step 5.3: Add flows integration tests**
   - Test `ExomizerStep` with mocked port
   - Test `ExomizerStepBuilder` DSL with all configuration options
   - Test step validation logic
   - Deliverable: Tests for step and builder
   - Testing: Run `./gradlew :flows:adapters:in:gradle:test`
   - Safe to merge: Yes (tests)

4. **Step 5.4: Update project documentation**
   - Add section to README or docs explaining Exomizer cruncher
   - Document use case examples: raw compression, memory compression
   - Document DSL usage: `exomizerStep { ... }`
   - Deliverable: User-facing documentation
   - Testing: Manual review for clarity and correctness
   - Safe to merge: Yes (documentation)

## Notes

- **Exomizer binary discovery**: The implementation assumes `exomizer` is available in the system PATH. Consider adding configuration option for custom exomizer path if needed in future phases.

- **Two-phase approach**: This plan implements raw and mem modes in the initial phase. Additional modes (e.g., sfx) can be added in future phases following the same patterns.

- **Port layering**: The design includes two levels of ports:
  - `ExecuteExomizerPort` in crunchers domain (technology-agnostic)
  - `ExomizerPort` in flows domain (orchestration-specific)
  This allows independent evolution of each layer.

- **File handling**: Following project patterns, file resolution happens in adapters, while domain logic remains file-agnostic through ports.

- **Error handling**: Use `StepValidationException` for configuration errors and `StepExecutionException` for runtime failures, matching flows subdomain patterns.

- **Future extensions**: Phase 5 can be extended to support additional Exomizer options, compression profiles, or integration with other crunchers (if similar tools are added later).

---

**Next Steps**: Once you confirm this plan is acceptable, we can proceed with Phase 1 (module setup) and continue through all phases to full completion.
