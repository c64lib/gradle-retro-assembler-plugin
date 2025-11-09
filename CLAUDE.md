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

## Technology Stack

- **Language**: Kotlin
- **Build Tool**: Gradle (this is a Gradle plugin project)
- **Key Dependencies**:
  - Vavr (functional data structures)
  - PNGJ (PNG image processing)
  - Gradle API
