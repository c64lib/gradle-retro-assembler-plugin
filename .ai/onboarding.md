# Project Onboarding: Gradle Retro Assembler Plugin

## Welcome

Welcome to the Gradle Retro Assembler Plugin project! This is a Gradle plugin that adds capability of building Assembly projects for MOS 65xx family of microprocessors, currently supporting Kick Assembler as the only ASM dialect.

## Project Overview & Structure

The core functionality revolves around providing automated build capabilities for Commodore 64 software development using Assembly language. The project is organized as a multi-module Gradle project with hexagonal architecture, where each bounded context contains domain logic and adapters for specific technologies like Gradle.

## Core Modules

### `shared/gradle`

- **Role:** Core Gradle integration layer providing shared DSL components and plugin infrastructure
- **Key Files/Areas:** 
  - DSL Components: `shared/gradle/src/main/kotlin/com/github/c64lib/rbt/shared/gradle/dsl/*`
  - Plugin Infrastructure: Various Gradle integration utilities
- **Top Contributed Files:** Multiple DSL-related Kotlin files in the shared gradle module
- **Recent Focus:** Heavy development activity with 53 commits, indicating ongoing work on core Gradle integration and DSL improvements

### `processors/charpad`

- **Role:** Charpad file format processor for handling CTM (Charpad Tilemap) files
- **Key Files/Areas:** 
  - Domain Logic: `processors/charpad/src/main/kotlin/com/github/c64lib/rbt/processors/charpad/domain/*`
  - Gradle Adapters: `processors/charpad/adapters/in/gradle/*`
- **Top Contributed Files:** Domain model files for Charpad processing
- **Recent Focus:** Significant recent activity with CTM format support improvements and file processing enhancements

### `flows`

- **Role:** Workflow orchestration and execution engine for build processes
- **Key Files/Areas:** 
  - Use Cases: `flows/src/main/kotlin/com/github/c64lib/rbt/flows/usecase/*`
  - Domain Model: `flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/*`
  - Gradle Integration: `flows/adapters/in/gradle/*`
- **Top Contributed Files:** `ExecuteFlowsUseCase.kt`, `ExecuteFlowService.kt`, `ProcessorConfig.kt`, `BuildFlowsGraphService.kt`
- **Recent Focus:** Major development effort on flow execution system, graph building, and command task integration

### `infra/gradle`

- **Role:** Core infrastructure and main plugin entry point
- **Key Files/Areas:** 
  - Main Plugin: `infra/gradle/src/main/kotlin/com/github/c64lib/gradle/RetroAssemblerPlugin.kt`
  - Preprocessing: `infra/gradle/src/main/kotlin/com/github/c64lib/gradle/preprocess/*`
- **Top Contributed Files:** RetroAssemblerPlugin.kt and various preprocessing components
- **Recent Focus:** Infrastructure improvements and preprocessing pipeline enhancements

### `processors/image`

- **Role:** Image processing capabilities for PNG transformation into sprites or charsets
- **Key Files/Areas:** 
  - Adapters: `processors/image/adapters/in/gradle/*`, `processors/image/adapters/out/png/*`
  - File Output: `processors/image/adapters/out/file/*`
- **Top Contributed Files:** Various image processing adapter files
- **Recent Focus:** Image processing functionality with cutting, splitting, and extending capabilities

### `compilers/kickass`

- **Role:** Kick Assembler compiler integration
- **Key Files/Areas:** 
  - Gradle Integration: `compilers/kickass/adapters/out/gradle/*`
  - Command Building: Command line builder components
- **Top Contributed Files:** `CommandLineBuilder.kt` and related compiler integration files
- **Recent Focus:** Compiler integration improvements and command line parameter handling

## Key Contributors

- **Maciej Małecki:** Primary maintainer and architect, responsible for overall project design, core functionality, and major feature development (90 commits)
- **Maciej Malecki:** Secondary contributor working on various project aspects (28 commits)
- **dependabot[bot]:** Automated dependency updates (1 commit)

## Overall Takeaways & Recent Focus

1. **Flow System Architecture:** Major development effort on workflow orchestration system with sophisticated graph-based execution model for build processes
2. **Processor Enhancement:** Significant improvements to file format processors, particularly Charpad CTM format support with new versions and features
3. **Gradle Integration Improvements:** Ongoing refinement of core Gradle plugin infrastructure and DSL components for better user experience
4. **Build Pipeline Optimization:** Focus on preprocessing capabilities and command execution infrastructure
5. **Image Processing Expansion:** Addition of PNG image processing capabilities for sprite and charset generation

## Potential Complexity/Areas to Note

- **Hexagonal Architecture:** The project uses sophisticated hexagonal architecture with clear separation between domain logic and adapters, requiring understanding of ports and adapters pattern
- **Multi-Module Gradle Setup:** Complex multi-module structure with 40+ modules requiring careful dependency management and understanding of Gradle's multi-project builds
- **File Format Processing:** Deep knowledge of retro computing file formats (CTM, SPD, etc.) and their binary structures is essential for processor development

## Questions for the Team

1. What are the current priorities for the flow execution system and how does the graph-based execution model handle complex dependency scenarios?
2. How is the testing strategy structured across the multi-module architecture, and what are the conventions for BDD-style testing with Kotest?
3. What are the specific design principles governing the hexagonal architecture implementation and adapter layer responsibilities?
4. How does the dependency management work for external tools like Kick Assembler, and what are the download and caching strategies?
5. What is the release process and versioning strategy for the Gradle plugin, and how are breaking changes handled?
6. How should new processor types be implemented following the existing patterns, and what interfaces need to be implemented?
7. What are the performance considerations for large Assembly projects and how does the build pipeline handle optimization?

## Next Steps

1. **Set up Development Environment:** Follow the Gradle build setup and ensure Kotlin development environment is configured properly
2. **Explore the `flows` Module:** Investigate the highly active workflow execution system to understand the core build orchestration logic
3. **Run the Test Suite:** Execute `gradle test` to understand the testing patterns and see the BDD-style Kotest specifications in action
4. **Study Processor Implementation:** Examine the `processors/charpad` module to understand how file format processors are implemented within the hexagonal architecture
5. **Review Recent Changes:** Analyze recent commits and Pull Requests related to flow system improvements and processor enhancements

## Development Environment Setup

1. **Prerequisites:** JDK (compatible with Kotlin 1.7.0), Gradle (via wrapper included)
2. **Dependency Installation:** `gradlew build` (Gradle wrapper handles dependency resolution)
3. **Building the Project:** `gradle build`
4. **Running Tests:** `gradle test`
5. **Code Formatting:** `gradle spotlessApply` (always run after editing source files)
6. **Common Issues:** Common issues section not found in checked files

## Helpful Resources

- **Documentation:** https://c64lib.github.io/gradle-retro-assembler-plugin/
- **Issue Tracker:** https://github.com/c64lib/gradle-retro-assembler-plugin/issues
- **Contribution Guide:** Contribution guide not found in checked files
- **Communication Channels:** Communication channel links not found in checked files
- **Learning Resources:** Project roadmap available at https://github.com/orgs/c64lib/projects/3/views/6
