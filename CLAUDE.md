# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Gradle Retro Assembler Plugin is a Gradle plugin that adds capability for building Assembly projects for MOS 65xx family of microprocessors (primarily Commodore 64). Currently supports Kick Assembler as the ASM dialect.

Published at: https://plugins.gradle.org/plugin/com.github.c64lib.retro-assembler

## Build Commands

### Build and Test
```bash
./gradlew build          # Build all modules and run tests
./gradlew test           # Run tests only
./gradlew clean          # Clean build directories
```

### Running Tests
```bash
./gradlew test                                    # Run all tests
./gradlew :flows:test                             # Run tests for specific module
./gradlew :flows:adapters:in:gradle:test          # Run tests for specific submodule
./gradlew collectTestResults                      # Collect all test results to build/test-results/gradle
```

### Module-Specific Tasks
```bash
./gradlew :infra:gradle:jar                       # Build plugin JAR
./gradlew :infra:gradle:publishPluginJar          # Build plugin JAR for publishing
```

## Architecture

This project uses **Hexagonal Architecture** (Ports and Adapters pattern).

### Domain Structure

The project is organized by business domains at the top level:

- **compilers**: Compiles source files into binary files (currently Kick Assembler)
- **dependencies**: Manages external dependencies (libraries downloaded from internet)
- **emulators**: Emulates hardware (VICE emulator) for automated testing
- **testing**: Supports automated testing (64spec framework)
- **processors**: Processes asset files (graphics, music) for use in source files
  - **charpad**: Processes CharPad files for graphical assets
  - **spritepad**: Processes SpritePad files for graphical assets
  - **goattracker**: Processes GoatTracker files for musical assets
  - **image**: Processes image files for graphical assets
- **flows**: Pipeline DSL for orchestrating build steps (recent addition)
- **shared**: Shared kernel elements used across domains
- **infra**: Infrastructure concerns (the actual Gradle plugin implementation)

### Module Organization

Each domain follows this structure:
```
domain-name/
├── src/                          # Business logic
│   ├── domain/                   # Domain data structures
│   └── usecase/                  # Domain functions (use cases)
└── adapters/
    ├── in/                       # Inbound adapters (e.g., Gradle DSL)
    └── out/                      # Outbound adapters (e.g., file system, Gradle API)
```

### Use Cases

- Use cases are **Kotlin classes** with a **single public method** named `apply`
- Use case class names always end with `UseCase.kt` suffix
- The `apply` method consumes a payload parameter and may return a use case result object
- Examples:
  - `KickAssembleUseCase.kt` in `compilers/kickass/src/main/kotlin/.../usecase/`
  - `ProcessCharpadUseCase.kt` in `processors/charpad/src/main/kotlin/.../usecase/`

### Ports (Interfaces)

- **All technology-specific code must be hidden behind a port (interface)**
- Ports prevent adapters from leaking into domain code
- Use cases receive port implementations via dependency injection
- Example: `KickAssemblePort` interface implemented by Gradle-specific adapters

### Gradle as a Concern

Gradle itself is treated as a technology concern and must be isolated in adapters:
- Inbound adapters: Gradle tasks and DSL extensions
- Outbound adapters: Gradle APIs for file operations, Workers API, etc.

### Parallel Execution

For parallel task execution, **always use Gradle's Workers API** (not custom threading).

### Adding New Modules

When adding a new module to the project, **you must also add it as `compileOnly` dependency in the `infra/gradle` module**. Failure to do this will result in `ClassNotFoundError` at runtime. The `infra/gradle` module is the entry point for the Gradle plugin and needs to have all domain modules available during compilation to properly integrate them.

## Testing

- Tests use standard Kotlin test conventions with JUnit
- Test files end with `Test.kt` suffix
- Tests are organized in `src/test/kotlin/` mirroring the main source structure
- Run specific test: `./gradlew :module:test --tests "TestClassName"`

## Commit Message Guidelines

- For single-file commits: mention the file name and summarize changes
- For multi-file commits: mention the files and summarize changes
- For changes in domain directories: mention which domain was affected
- Format example: "Update KickAssembleUseCase in compilers domain to support additional output formats"

## Code Review Guidelines

- Do not suggest including implementation details in API comments

## Flows Subdomain Patterns

The flows subdomain is an orchestrator domain that coordinates multiple processor and compiler subdomains into processing pipelines with dependency tracking and incremental build support.

### Step Classes (Domain Layer)

Step classes are immutable data classes representing individual processing steps within a flow:

- **Base class**: All steps extend `FlowStep` abstract base class
- **Pattern**: Use Kotlin `data class` for immutable value objects with auto-generated equals/hashCode
- **Structure**: Each step has:
  - `name: String` - Unique step identifier
  - `inputs: List<String>` - Input file paths for change detection
  - `outputs: List<String>` - Output file paths for incremental builds
  - Step-specific configuration properties (e.g., `compression`, `tileSize`, `channels`)
  - Port field (injected by Gradle task infrastructure)

### Common Patterns

**Port Injection**: Each step has a port field (e.g., `var port: AssemblyPort? = null`) that is injected by Gradle task infrastructure before execution. The `validatePort()` method in FlowStep base class validates the port is not null before use.

**File Resolution**: Use protected methods from FlowStep base class:
- `resolveInputFiles(inputPaths, projectRootDir)` - Resolves and validates multiple input files
- `resolveOutputFile(outputPath, projectRootDir)` - Resolves single output file
- These methods handle relative path resolution from project root directory

**Validation**: Keep validation minimal and focused on critical domain rules:
- Range validation (e.g., tile size must be 8, 16, or 32)
- File extension validation
- Required vs. optional parameter consistency
- Defer edge cases and execution-level checks to adapters

**Error Handling**: Use custom exception classes for consistent error reporting:
- `StepValidationException` for configuration/validation errors
- `StepExecutionException` for runtime/execution errors
- Exception messages automatically prepend step name: "Step '<name>': {message}"

**Documentation**: Use concise Kdoc following Kotlin style guide:
- 3-5 lines per class documenting purpose and validation rules
- Remove verbose multi-paragraph documentation and code examples
- Add inline comments only for non-obvious logic

### Example Step Implementation

```kotlin
data class CharpadStep(
    override val name: String,
    override val inputs: List<String>,
    override val outputs: List<String>,
    val compression: CharpadCompression,
    var port: CharpadPort? = null
) : FlowStep(name, inputs, outputs) {

  /**
   * CharPad file processor step.
   *
   * Validates compression type and file paths.
   */
  override fun execute(context: Map<String, Any>) {
    val validPort = validatePort(port, "CharpadPort")
    val projectRootDir = getProjectRootDir(context)
    val inputFile = resolveInputFile(inputs[0], projectRootDir)

    try {
      validPort.processCharpad(inputFile, compression)
    } catch (e: Exception) {
      throw StepExecutionException("Failed to process CharPad", name, e)
    }
  }

  override fun validate() {
    if (compression == null) {
      throw StepValidationException("Compression type is required", name)
    }
  }
}
```

### DSL Builder Patterns - CommandStepBuilder

The `CommandStepBuilder` provides convenient DSL shortcuts `useFrom()` and `useTo()` for referencing input/output paths in command parameters:

```kotlin
// Define paths once in from()/to(), then reference them in parameters
commandStep("exomize-game", "exomizer") {
    from("build/game-linked.bin")
    to("build/game-linked.z.bin")
    param("raw")
    flag("-T4")
    option("-o", useTo())        // Resolves to "build/game-linked.z.bin"
    param(useFrom())             // Resolves to "build/game-linked.bin"
}

// With multiple inputs/outputs, use index parameter (default 0)
commandStep("process", "tool") {
    from("file1.txt", "file2.txt")
    to("out1.txt", "out2.txt")
    option("-i1", useFrom(0))    // Uses "file1.txt"
    option("-i2", useFrom(1))    // Uses "file2.txt"
    option("-o1", useTo(0))      // Uses "out1.txt"
    option("-o2", useTo(1))      // Uses "out2.txt"
}
```

**Benefits:**
- Single source of truth for paths (DRY principle)
- Clear intent with readable method names
- Prevents copy-paste errors
- Works seamlessly in `param()`, `option()`, and `withOption()` methods

### Task Execution Order

The flows subdomain integrates seamlessly with the build pipeline through automatic task dependencies:

**Task Execution Chain:**
```
build → asm → flows → (all flow tasks in dependency order)
         ↓
         (all other dependencies: resolveDevDeps, downloadDeps, preprocess)
```

**Key Points:**
- The `flows` aggregation task is automatically created by `FlowTasksGenerator` in `flows/adapters/in/gradle/FlowTasksGenerator.kt`
- The `flows` task depends on all top-level flow tasks (e.g., `flowPreprocessing`, `flowCompilation`)
- The `asm` task depends on the `flows` task, ensuring all flow-based preprocessing runs before assembly compilation
- Users can run `./gradlew flows` to execute all flows independently, or `./gradlew asm` to run flows automatically before assembly
- The dependency chain maintains correct execution order even with complex flow dependencies (flow-level `dependsOn` relationships)

**Example Build Execution:**
```bash
# Run assembly task - automatically runs flows first
./gradlew asm

# Run all flows independently
./gradlew flows

# Clean build - automatically runs flows before assembly
./gradlew clean build
```

**Flow Task Naming Convention:**
- Flow-level aggregation tasks are named `flow{FlowNameCapitalized}` (e.g., `flowPreprocessing`, `flowCompilation`)
- Top-level aggregation task is named `flows` (constant: `TASK_FLOWS` in `Tasks.kt`)
- Step-level tasks are named `flow{FlowName}Step{StepName}` (e.g., `flowPreprocessingStepCharpadStep`)

## Technology Stack

- **Language**: Kotlin
- **Build Tool**: Gradle (this is a Gradle plugin project)
- **Key Dependencies**:
  - Vavr (functional data structures)
  - PNGJ (PNG image processing)
  - Gradle API
