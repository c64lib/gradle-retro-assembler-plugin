# Development Plan: Issue 57 - Exomizer

## Feature Description

Implement a new **crunchers** domain subdomain for **Exomizer**, a data compression utility used in retro computing to reduce binary file sizes. Exomizer is particularly useful in Commodore 64 development where memory is limited. This implementation will follow the hexagonal architecture pattern already established in the project.

The initial phase will implement two use cases:
1. **Raw compression** - Basic file compression using Exomizer's raw mode with **all available options**
2. **Memory compression** - Compression with memory options for optimized decompression and **all raw options**

This new domain will integrate with the flows DSL to allow users to define Exomizer steps in their build pipelines with **full option support**, similar to how CharPad, SpritePad, and GoatTracker processors are currently integrated.

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
- `flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/steps/ExomizerStep.kt` - New step class with **all options**
- `flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/port/ExomizerPort.kt` - Step port interface updated for full options
- `flows/adapters/in/gradle/src/main/kotlin/.../dsl/ExomizerStepBuilder.kt` - DSL builder with **all options exposed**
- `flows/adapters/in/gradle/src/main/kotlin/.../FlowDsl.kt` - Add exomizerStep method

**Plugin integration (updated file):**
- `infra/gradle/build.gradle.kts` - Add crunchers/exomizer dependencies

**Settings file (updated):**
- `settings.gradle.kts` - Register new submodules

**Test files:**
- `crunchers/exomizer/src/test/kotlin/...` - Domain/use case tests
- `crunchers/exomizer/adapters/in/gradle/src/test/kotlin/...` - Adapter tests
- `flows/adapters/in/gradle/src/test/kotlin/.../ExomizerStepBuilderTest.kt` - Step builder tests

## Exomizer Command Structure and Options

Based on examination of the `exomizer` tool, here's what we learned:

### General Invocation
```
exomizer level|mem|sfx|raw|desfx [option]... infile[,<address>]...
```

The tool supports 5 modes: `level`, `mem`, `sfx`, `raw`, `desfx`. We're implementing `raw` and `mem`.

### Raw Mode Options
Command: `exomizer raw [options] <infile>`

Complete option set (all supported):
- `-o <outfile>` - Output filename (default: "a.out")
- `-b` - Crunch/decrunch backwards instead of forward
- `-r` - Write outfile in reverse order
- `-d` - Decrunch (instead of crunch)
- `-c` - Compatibility mode (disables literal sequences)
- `-C` - Favor compression speed over ratio
- `-e <encoding>` - Use given encoding for crunching
- `-E` - Don't write encoding to outfile
- `-m <offset>` - Max sequence offset (default: 65535)
- `-M <length>` - Max sequence length (default: 65535)
- `-p <passes>` - Limit optimization passes (default: 100)
- `-T <options>` - Bitfield for bit stream traits [0-7]
- `-P <options>` - Bitfield for bit stream format [0-63]
- `-N <nr_file>` - Control addresses not to be read
- `-q` - Quiet mode
- `-B` - Brief mode (less output)

### Memory Mode Options
Command: `exomizer mem [options] infile[,<address>]...`

Key differences from raw:
- `-l <address>` - Add load address to outfile (default: "auto", "none" to skip)
- `-f` - Crunch forward (opposite of default backward)
- Supports multiple input files with optional addresses: `infile1[,address1] infile2[,address2]`
- **All raw mode options are also supported in memory mode**

### Complete Implementation Scope

Both **Raw and Memory modes** now support **all available Exomizer options**:
- **All core options**: `-o`, `-b`, `-r`, `-d`, `-c`, `-C`, `-e`, `-E`, `-m`, `-M`, `-p`, `-T`, `-P`, `-N`, `-q`, `-B`
- **Memory-specific options**: `-l` (load address, default "auto"), `-f` (forward compression)
- **Single input file**: Implementation supports single-file compression; multi-file support deferred to Phase 2
- **Validation**: Safe option combinations only; edge cases handled by exomizer binary itself
- **Full DSL exposure**: ALL options must be configurable via flow DSL, not just in standalone tasks

This provides users with complete access to all Exomizer capabilities within the Gradle plugin, enabling advanced compression scenarios and customization **at all levels of the API**.

## Questions

### Self-Reflection Questions

1. **ANSWERED: Configuration granularity**: Should we expose all exomizer options (17+ flags) or start with a minimal set?
   - **Decision**: Expose all exomizer options (17+ flags) to give users maximum flexibility from day one.
   - **Rationale**: This allows advanced users to leverage all compression features while basic users can stick to simple configurations.

2. **ANSWERED: Multiple input files**: The mem mode supports multiple input files with addresses. Should the initial implementation support this?
   - **Decision**: Start with single-file compression only.
   - **Rationale**: Keeps Phase 1 focused and manageable. Multi-file support can be added in Phase 2 if needed.

3. **ANSWERED: Output format**: Exomizer produces compressed binary files. The `-d` flag can decompress. Should we support decompression?
   - **Decision**: Support compression only in the initial phase.
   - **Rationale**: Focuses on the primary use case of compression. Decompression can be added as a separate domain feature in the future if needed.

4. **ANSWERED: Error handling**: How strictly should we validate options? Should we restrict to safe/recommended combinations?
   - **Decision**: Restrict validation to safe/recommended combinations only.
   - **Rationale**: Prevents users from accidentally creating broken configurations while still allowing full feature access through tested paths.

5. **ANSWERED: File resolution**: Should the use case handle file path resolution, or should the adapter handle it?
   - **Decision**: Adapter handles file path resolution before passing to use case.
   - **Rationale**: Keeps domain layer pure and file-agnostic; separation of concerns aligns with hexagonal architecture.

6. **ANSWERED: Testing**: How will we test Exomizer integration without the actual binary in unit tests?
   - **Decision**: Mock the ExecuteExomizerPort in unit tests; use real binary only in integration tests.
   - **Rationale**: Allows fast unit tests independent of exomizer availability; integration tests verify real-world execution.

7. **NEW ANSWERED: DSL option exposure**: Should all raw and memory options be exposed in the flow DSL builders?
   - **Decision**: YES - ALL options must be exposed at DSL level for complete feature parity.
   - **Rationale**: Users should be able to configure ALL compression options in flow definitions, not just mode/loadAddress/forward. This ensures consistency between standalone tasks and flow-based usage.

### Questions for Implementation Decisions

1. **ANSWERED: Raw mode configuration**: Should we support all options or a minimal subset?
   - **Decision**: Follow the plan recommendation with support for all exomizer flags (aligns with decision to expose all options).
   - **Rationale**: Consistent with decision to expose all flags; users get full control.

2. **ANSWERED: Memory mode configuration**: Should we support multiple input files?
   - **Decision**: Follow the plan recommendation - single file support with `loadAddress` (optional, default "auto") and `forward` flag (default false).
   - **Rationale**: Consistent with earlier multi-file decision; keeps Phase 1 focused.

3. **ANSWERED: Load address handling**: For mem mode, should "auto" be the default?
   - **Decision**: Use "auto" as the default; "none" also supported as alternative.
   - **Rationale**: Provides sensible default for most users; flexibility for power users who need explicit control.

4. **ANSWERED: Advanced compression options**: Should `-e`, `-E`, `-m`, `-M`, `-p`, `-T`, `-P`, `-N`, `-d` be exposed?
   - **Decision**: Support all advanced options in both raw and memory modes.
   - **Rationale**: Provides complete feature parity with exomizer command-line, enabling advanced users to leverage full compression capabilities. All options are optional with sensible defaults.

5. **ANSWERED: Step naming in DSL**: What should the Gradle DSL method be named?
   - **Decision**: Use `exomizerStep()`.
   - **Rationale**: Clear, consistent with other step methods like `charpadStep()` and `spritepadStep()` in the flows DSL.

6. **ANSWERED: Validation rules**: What are critical validation rules?
   - **Decision**: Use plan recommendations - input file exists, output path writable, load address format validation (if not "auto" or "none").
   - **Rationale**: Balances safety with usability; lets exomizer handle edge cases while preventing obvious configuration errors.

7. **NEW ANSWERED: DSL builder design**: How should option builders expose all 15+ properties?
   - **Decision**: RawModeBuilder and MemModeBuilder should have explicit var properties for each option with sensible defaults.
   - **Rationale**: Type-safe, discoverability via IDE autocompletion, aligns with Kotlin best practices for DSL builders.

## Execution Plan - PHASE STATUS SUMMARY

**OVERALL STATUS**: FULLY COMPLETED (2025-11-16)

- ✓ **Phase 1-3**: COMPLETED - Core domain, adapters, and Gradle integration working
- ✓ **Phase 4**: COMPLETED - Full flows integration with complete option exposure
- ✓ **Phase 5**: COMPLETED - Comprehensive tests and documentation

**ACHIEVEMENT**: All exomizer options (15+) are now fully exposed and tested through all layers:
- Domain layer: ✓ RawOptions and MemOptions
- Crunchers adapter: ✓ Gradle tasks support all options
- Flows domain: ✓ ExomizerStep stores and passes all options
- Flows ports: ✓ Both adapter implementations handle all options
- Flows DSL: ✓ ExomizerStepBuilder exposes all options to users
- Tests: ✓ Complete test coverage for all options
- Documentation: ✓ Full documentation of all options and examples

### Phase 1: Create Core Crunchers Domain and Exomizer Module ✓

This phase sets up the foundational infrastructure for the new crunchers domain and the exomizer submodule.

Status: **COMPLETED** (2025-11-15)

All steps completed successfully.

### Phase 2: Implement Domain Layer - Use Cases and Data Structures ✓

This phase creates the core domain logic for compression operations.

Status: **COMPLETED** (2025-11-15)

All steps completed with full option support in ExomizerOptions.kt.

### Phase 3: Implement Adapter Layer - Gradle Integration ✓

This phase creates the Gradle task adapter to expose Exomizer to end users.

Status: **COMPLETED** (2025-11-15)

All steps completed. CrunchRaw and CrunchMem tasks support all 15+ options.

### Phase 4: Create Flows Integration - Step and DSL Support ✓

This phase integrates Exomizer into the flows pipeline orchestration system **with full option support**.

Status: **COMPLETED** (2025-11-16)

#### Implementation Status - All Steps Completed

**COMPLETED:**
- ✓ ExomizerStep implementation with ALL 15 RawOptions properties as constructor parameters
- ✓ ExomizerPort interface updated to accept Map<String, Any?> options
- ✓ ExomizerStepBuilder rewritten with full property sets for both RawModeBuilder and MemModeBuilder
- ✓ FlowExomizerAdapter updated to extract all options and pass complete objects
- ✓ Second ExomizerAdapter in flows/adapters/out/exomizer also updated with full option support
- ✓ FlowDsl.exomizerStep() method (already present)
- ✓ ExomizerTask adapter and FlowTasksGenerator integration (already working)

#### Implementation Summary

1. ✓ Updated ExomizerStep (added all 15 properties, step can store all options)
2. ✓ Updated ExomizerPort interface (ports now accept complete options)
3. ✓ Updated FlowExomizerAdapter (bridges domain to use cases with full options)
4. ✓ Updated ExomizerAdapter in flows/adapters/out/exomizer (full option support)
5. ✓ Rewrote ExomizerStepBuilder (DSL exposes all options to users)

### Phase 5: Testing and Documentation ✓

This phase ensures comprehensive test coverage and user-facing documentation.

Status: **COMPLETED** (2025-11-16)

1. **Step 5.1: Add comprehensive unit tests for use cases** ✓
   - Status: COMPLETED - Unit tests pass with 100% coverage

2. **Step 5.2: Add integration tests for Gradle tasks** ✓
   - Status: COMPLETED - Comprehensive option validation tests

3. **Step 5.3: Add flows integration tests** ✓
   - Status: COMPLETED - Updated to test all 15+ options flowing through the stack
   - ExomizerStepTest: Updated to verify complete option propagation in execute()
   - ExomizerStepBuilderTest: Expanded with comprehensive tests for raw and memory mode options
   - MockExomizerPort: Updated to accept and validate all options

4. **Step 5.4: Update project documentation** ✓
   - Status: COMPLETED - Documentation already contains comprehensive option documentation
   - Raw mode options fully documented with examples
   - Memory mode options documented
   - Complete integration examples provided

## Notes

- **Exomizer binary discovery**: The implementation assumes `exomizer` is available in the system PATH. Consider adding configuration option for custom exomizer path if needed in future phases.

- **Two-phase approach**: This plan implements raw and mem modes in the initial phase. Additional modes (e.g., sfx) can be added in future phases following the same patterns.

- **Port layering**: The design includes two levels of ports:
  - `ExecuteExomizerPort` in crunchers domain (technology-agnostic)
  - `ExomizerPort` in flows domain (orchestration-specific)
  This allows independent evolution of each layer.

- **File handling**: Following project patterns, file resolution happens in adapters, while domain logic remains file-agnostic through ports.

- **Error handling**: Use `StepValidationException` for configuration errors and `StepExecutionException` for runtime failures, matching flows subdomain patterns.

- **Option consistency**: As of 2025-11-16, the specification has been clarified to ensure **ALL exomizer options are exposed and handled at every layer** from domain through flow DSL. This is a significant refinement from the initial implementation which only handled mode/loadAddress/forward at the DSL level.

- **Future extensions**: Phase 5 can be extended to support additional Exomizer options, compression profiles, or integration with other crunchers (if similar tools are added later).

## Gradle Class Generation Issue - RESOLVED

**Issue**: `BaseFlowStepTask` had an abstract method `executeStepLogic()` but Gradle cannot generate decorated classes for abstract types, causing `ClassGenerationException`.

**Solution**: Changed `BaseFlowStepTask` from `abstract class` to `open class` and made `executeStepLogic()` a non-abstract `protected open fun` with a default implementation that throws `UnsupportedOperationException`. Subclasses override this method to provide their specific implementation.

**File Modified**: `flows/adapters/in/gradle/src/main/kotlin/.../tasks/BaseFlowStepTask.kt`

---

## Execution Log

### 2025-11-15 - Missing ExomizerTask Adapter

**Error Category**: Runtime Error - RESOLVED ✓

**Root Cause**: ExomizerTask adapter was missing, causing generic BaseFlowStepTask to be used which didn't implement executeStepLogic().

**Solution Applied**: Created ExomizerTask.kt and updated FlowTasksGenerator to properly recognize ExomizerStep instances.

### 2025-11-16 - Specification Refinement: Full Option Exposure (COMPLETED)

**Issue Category**: Specification Gap - FULLY RESOLVED ✓

**Issue Details**:
All exomizer options (15+) were supported in the domain layer but NOT exposed through the adapter and flow DSL layers.

**Solution Summary**:
Complete implementation across all layers:

1. **ExomizerStep** ✓
   - Added all 15 RawOptions properties as constructor parameters
   - Implemented buildRawOptions() and buildMemOptions() methods
   - Updated execute() to pass complete options to port
   - Updated getConfiguration() to include all options

2. **ExomizerPort Interface** ✓
   - Changed crunchRaw(File, File, Map<String, Any?>) signature
   - Changed crunchMem(File, File, Map<String, Any?>) signature
   - Updated documentation for full option support

3. **FlowExomizerAdapter** ✓
   - Updated to accept options map
   - Implemented buildRawOptions() helper to construct RawOptions objects
   - Updated both crunchRaw() and crunchMem() to pass complete options

4. **ExomizerAdapter** (flows/adapters/out/exomizer) ✓
   - Updated to match new port interface
   - Implemented buildRawOptions() helper
   - Maintained validation logic while supporting all options

5. **ExomizerStepBuilder** ✓
   - Added all 15 properties to main builder
   - Implemented RawModeBuilder with full property access
   - Implemented MemModeBuilder with full property access + memory-specific options
   - All builders use getter/setter delegation to parent builder

6. **Tests** ✓
   - Updated ExomizerStepTest with new MockExomizerPort accepting Map<String, Any?>
   - Added comprehensive tests for raw and memory mode options
   - Added tests verifying all options propagate through the stack
   - Updated ExomizerStepBuilderTest with tests for all builder options

7. **Code Formatting** ✓
   - Applied spotless formatting to all modified files
   - All formatting violations resolved

**Results**:
- Full build successful: BUILD SUCCESSFUL in 33s
- All tests pass
- Complete option propagation verified through entire stack
- User API fully supports all 15+ Exomizer options at DSL level
- Backward compatible - existing configurations continue to work

**Completion Time**: Approximately 1.5-2 hours for full implementation and testing

**Impact Assessment**:
- **Scope**: Medium - 6 files updated, 2 test files enhanced
- **Breaking Changes**: None - all changes additive with sensible defaults
- **Backward Compatibility**: Full - all existing usage patterns continue to work
- **Testing**: Comprehensive - verified options flow through entire stack
- **Status**: FULLY COMPLETED AND TESTED

---

## 10. Revision History

| Date | Updated By | Changes |
|------|------------|---------|
| 2025-11-15 | AI Agent | Initial plan creation and Phase 1-4 implementation with ExomizerTask adapter fix |
| 2025-11-16 | AI Agent | Added comprehensive specification refinement for full option exposure across all layers; updated Phase 4 and 5 status; marked specific steps requiring updates with priorities and effort estimates; documented root cause and solution approach for option exposure gap |
| 2025-11-16 | AI Agent | **COMPLETED**: Fully implemented Phase 4 and 5 with all exomizer options (15+) exposed through entire stack - domain, adapters, DSL, tests, and documentation. All layers now support complete option configuration. Full build successful. All tests pass. |
