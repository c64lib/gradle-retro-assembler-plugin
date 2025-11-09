# Release 1.8.0

## Git Commit Summary
28 commits since version 1.7.6, focusing on processor asset handling (Goattracker, SpritePad, CharPad), pipeline DSL enhancements, CLI tool support, and code quality improvements.

Range: `1.7.6..develop`

## Commits by Category

### Features & Major Enhancements
- 66 goattracker step (#122) - Added GoatTracker music processor support
- 120 png step (#121) - Enhanced PNG processor step support
- 65 spritepad step (#119) - Improved SpritePad processor functionality
- 117 nybbler interleaver (#118) - Added nybbler interleaver feature
- 62 pipelines (#111) - Core pipeline DSL implementation
- 68 pipeline dsl support for compiler (#112) - Compiler integration with pipeline DSL
- 113 pipeline dsl support for cli tools (#114) - CLI tools support in pipeline DSL
- 64 pipeline support for charpad new (#115) - CharPad processor pipeline support

### Testing & Documentation
- 64 charpad documentation and tests (#116) - CharPad processor documentation and test coverage
- Reorganize project documentation: move coding guidelines under a new "About this project" section
- Add documentation for adapters and architecture; update settings for Copilot
- Update VSCode settings to include Copilot instructions and add new documentation for Copilot usage
- Add documentation for project architecture, domain structure, and commit message guidelines
- additional unit test
- improve code quality
- javadoc

### Refactoring & Infrastructure
- Refactor ExecuteFlowService and related components to use ExecuteTaskPort; update method signatures and outcomes to TaskOutcome
- Refactor BuildFlowsGraphService to use 'dependants' instead of 'followUps'; update related tests and interfaces to reflect changes in flow dependencies
- Make service classes internal; mark ExecuteStepPort as deprecated
- Rename use case classes to services; update method signatures and tests accordingly
- Refactor use case package structure; add ExecuteStepPort interface and update related classes
- Implement ExecuteFlowUseCase with step execution and modifier application; add tests for various scenarios
- Add BuildFlowsGraphUseCase and related tests; introduce FlowStepModifier
- Update copyright years to 2025 and add build configuration for domain plugin
- attempt to generate a test with genAI

### Release & Setup
- Release command (CLAUDE)
- Clean up for next release
- set up CLAUDE (2 commits)

## Full Commit List

| Hash | Author | Message |
|------|--------|---------|
| 075ae1a | Maciej Malecki | Release command (CLAUDE) |
| 6a7b78c | Maciej Malecki | Clean up for next release |
| 06a2f06 | Maciej Małecki | 66 goattracker step (#122) |
| f467a1f | Maciej Małecki | 120 png step (#121) |
| 2a2f113 | Maciej Małecki | 65 spritepad step (#119) |
| 7e2fe33 | Maciej Małecki | 117 nybbler interleaver (#118) |
| ca2fad5 | Maciej Małecki | 64 charpad documentation and tests (#116) |
| 265fbe3 | Maciej Małecki | 64 pipeline support for charpad new (#115) |
| 04c539f | Maciej Malecki | set up CLAUDE |
| a03056c | Maciej Malecki | set up CLAUDE |
| e5e7da7 | Maciej Małecki | Reorganize project documentation: move coding guidelines under a new "About this project" section |
| 2d902e5 | Maciej Małecki | 113 pipeline dsl support for cli tools (#114) |
| a7975aa | Maciej Małecki | 68 pipeline dsl support for compiler (#112) |
| 4850c26 | Maciej Małecki | 62 pipelines (#111) |
| 5fdf98b | Maciej Małecki | Refactor ExecuteFlowService and related components to use ExecuteTaskPort; update method signatures and outcomes to TaskOutcome |
| 2f2558c | Maciej Małecki | Refactor BuildFlowsGraphService to use 'dependants' instead of 'followUps'; update related tests and interfaces to reflect changes in flow dependencies |
| 742eb28 | Maciej Małecki | Make service classes internal; mark ExecuteStepPort as deprecated |
| 045e079 | Maciej Małecki | Rename use case classes to services; update method signatures and tests accordingly |
| a940f4b | Maciej Małecki | Implement ExecuteFlowUseCase with step execution and modifier application; add tests for various scenarios |
| 9f04513 | Maciej Małecki | Refactor use case package structure; add ExecuteStepPort interface and update related classes |
| 3d3c2b2 | Maciej Małecki | Add BuildFlowsGraphUseCase and related tests; introduce FlowStepModifier |
| 28cff6d | Maciej Małecki | Update copyright years to 2025 and add build configuration for domain plugin |
| 30f12b1 | Maciej Małecki | Add documentation for adapters and architecture; update settings for Copilot |
| d06820b | Maciej Małecki | Update VSCode settings to include Copilot instructions and add new documentation for Copilot usage |
| c43546f | Maciej Małecki | Add documentation for project architecture, domain structure, and commit message guidelines |
| 75fb146 | Maciej Małecki | javadoc |
| 89aa210 | Maciej Małecki | additional unit test |
| 3966f60 | Maciej Małecki | improve code quality |
| 5c59adb | Maciej Małecki | attempt to generate a test with genAI |

---

## Overview
[DRAFT - User should complete] This release focuses on expanding processor support for asset files and introducing the new Pipeline DSL for orchestrating build steps. Major additions include GoatTracker music processor, enhanced SpritePad and CharPad support with better pipeline integration, and PNG step improvements. Significant internal refactoring improves code organization and maintainability.

## Breaking Changes
[DRAFT - User should complete] Note any incompatible changes that users need to migrate:
- ExecuteStepPort is now deprecated (evaluate impact)
- Use case classes renamed to services (check if this affects public API)

## New Features
[DRAFT - Extracted from commits]
- GoatTracker music processor support for handling SID music files
- PNG image processor step implementation
- SpritePad asset processor enhancements
- Nybbler interleaver utility for data processing
- **Pipeline DSL**: New domain for orchestrating build steps
  - Compiler integration via pipeline steps
  - CLI tool support in pipelines
  - CharPad processor pipeline support
  - Flow execution with step modifiers

## Improvements
[DRAFT - Extracted from commits]
- CharPad processor documentation and test coverage expansion
- Better service class organization (marked internal where appropriate)
- Refactored flow execution model using ExecuteTaskPort
- Flow dependency management improvements (dependants model)
- Code quality enhancements and additional unit tests
- Updated copyright years to 2025

## Bug Fixes
[DRAFT - User should complete] Any specific bugs addressed

## Refactoring & Internal Changes
- ExecuteFlowService refactored to use ExecuteTaskPort with TaskOutcome
- BuildFlowsGraphService refactored for clearer flow dependency management
- Use case classes renamed to services with updated method signatures
- Use case package structure reorganization with new ExecuteStepPort interface
- Deprecated ExecuteStepPort in favor of ExecuteTaskPort
- Service classes marked as internal for better API boundaries

## Documentation Updates
[DRAFT - User should complete]
- Project architecture documentation
- Domain structure guide
- Adapter and architecture patterns documentation
- Commit message guidelines
- Coding guidelines reorganization
- Copilot integration instructions

## Dependency Updates
[DRAFT - User should complete] Any dependency version changes

## Migration Guide (if applicable)
[DRAFT - User should complete] For breaking changes only:
- If ExecuteStepPort or use case class changes affect users, provide migration path

## Contributors
- Maciej Malecki
- Maciej Małecki

## Release Notes for CHANGES.adoc
[DRAFT - Ready to copy]

```
1.8.0::
* Processors: Add GoatTracker music processor support (#122)
* Processors: Enhance PNG image processor step support (#121)
* Processors: Improve SpritePad asset processor functionality (#119)
* Features: Add nybbler interleaver utility (#118)
* Features: Add CharPad processor documentation and tests (#116)
* Flows: Add pipeline DSL support for CharPad processor (#115)
* Flows: Add pipeline DSL support for CLI tools (#114)
* Flows: Add pipeline DSL support for compiler (#112)
* Flows: Implement core pipelines feature (#111)
* Infrastructure: Refactor ExecuteFlowService to use ExecuteTaskPort
* Infrastructure: Refactor BuildFlowsGraphService with improved dependency management
* Infrastructure: Rename use case classes to services
* Infrastructure: Reorganize use case package structure
* Infrastructure: Mark service classes as internal for cleaner API boundaries
* Infrastructure: Mark ExecuteStepPort as deprecated
* Documentation: Add comprehensive architecture and domain documentation
* Documentation: Update copyright years to 2025
* Quality: Improve code quality and add additional unit tests
```

## Documentation References
[DRAFT - User should complete] List of .adoc files in `doc/` directory that need updates based on new features

---

## Next Steps

1. **Review the categorization** above - feel free to reorganize commits if needed
2. **Complete DRAFT sections**:
   - Overview
   - Breaking Changes
   - Bug Fixes
   - Dependency Updates
   - Migration Guide (if needed)
   - Documentation Updates
   - Documentation References
3. Once approved, this document will be used to:
   - Extract entries into `CHANGES.adoc`
   - Update relevant documentation files in `doc/` directory
