# Feature: Add dasm Compiler Support

**Issue**: #129
**Status**: Planning
**Created**: 2025-11-15

## 1. Feature Description

### Overview
Add support for the dasm assembler as a second compiler option alongside Kick Assembler. Unlike Kick Assembler which is distributed as a JAR file, dasm is a command-line tool that users install via their system package manager. This feature extends the Flow DSL to support dasm compilation with similar parameter support to Kick Assembler, plus dasm-specific parameters.

### Requirements
- Assume dasm is installed and available via PATH environment variable
- Support dasm as a second assembly compiler option in Flow DSL only (no classic DSL extension)
- Support standard assembly parameters: include paths, defines, variables
- Support dasm-specific parameters (to be discovered via `dasm` CLI inspection)
- Maintain same incremental build semantics as Kick Assembler
- Do not require JAR file or downloading (unlike Kick Assembler)
- Support multiple output formats if dasm provides them
- Reuse existing Flow DSL builder pattern for consistency

### Success Criteria
- Users can define assembly steps using `dasmStep {}` in flows DSL
- dasm compilation executes via command-line invocation
- All standard parameters work (includes, defines, output format)
- dasm-specific parameters are supported
- Incremental builds work correctly (input/output tracking)
- Unit tests for all new components
- Integration with existing Flow task infrastructure
- Build passes with no new failures

## 2. Root Cause Analysis

### Current State
The plugin currently supports only Kick Assembler for assembly compilation. Kick Assembler is tightly integrated through:
1. Domain classes and use cases in `compilers/kickass`
2. Port interfaces (`KickAssemblePort`)
3. Gradle adapters that manage JAR download and execution
4. Flow DSL extension with type-safe builders
5. Port adapters bridging Flow domain to Kick Assembler

The current architecture assumes:
- Assembly compiler is JAR-based (needs download/management)
- Command-line execution happens through `javaexec`
- Settings include version management

### Desired State
Support multiple assemblers with dasm being the second one:
1. A flexible architecture that doesn't assume JAR-based execution
2. Direct command-line tool execution for dasm (via system PATH)
3. Parallel Flow DSL support for both Kick Assembler and dasm
4. Shared domain and port abstractions
5. Compiler-specific adapters implementing the shared ports

### Gap Analysis
**What needs to change:**
1. Create new `compilers/dasm` domain module with dasm-specific logic
2. Implement `DasmAssembleUseCase` and `DasmAssemblePort`
3. Create Gradle adapter to execute dasm via system command
4. Add Flow DSL builder: `DasmStepBuilder` with dasm-specific parameters
5. Add `DasmStep` domain class in flows subdomain
6. Create `DasmPortAdapter` to bridge Flow domain to dasm compiler
7. Register dasm step in `FlowTasksGenerator`
8. Create `DasmAssembleTask` for Gradle task execution
9. Add dasm use case injection in plugin initialization
10. Update `infra/gradle` dependencies to include new dasm module

## 3. Relevant Code Parts

### Existing Components

#### Compilers Domain (Kick Assembler)
- **Location**: `compilers/kickass/src/main/kotlin/com/github/c64lib/rbt/compilers/kickass/`
- **Files**:
  - `domain/KickAssemblerSettings.kt` - Settings data class
  - `domain/usecase/KickAssembleUseCase.kt` - Use case with `apply()` method
  - `domain/usecase/port/KickAssemblePort.kt` - Port interface
  - `adapters/out/gradle/KickAssembleAdapter.kt` - Execution via javaexec
  - `adapters/out/gradle/CommandLineBuilder.kt` - CLI argument building
- **Purpose**: Encapsulates Kick Assembler-specific logic
- **Integration Point**: Will be mirrored for dasm with command-line execution instead of javaexec

#### Flows Domain - Assembly Steps
- **Location**: `flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/`
- **Files**:
  - `steps/AssembleStep.kt` - Step for Kick Assembler
  - `config/ProcessorConfig.kt` - `AssemblyConfig` data class
  - `port/AssemblyPort.kt` - Port interface for both compilers
  - `config/AssemblyConfigMapper.kt` - Config conversion logic
- **Purpose**: Domain logic for assembly compilation (compiler-agnostic)
- **Integration Point**: Will add `DasmStep` similar to `AssembleStep`

#### Flows Adapters - Gradle Integration
- **Location**: `flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/`
- **Files**:
  - `dsl/AssembleStepBuilder.kt` - Type-safe builder for Kick Assembler
  - `assembly/KickAssemblerPortAdapter.kt` - Port adapter to Kick Assembler
  - `assembly/KickAssemblerCommandAdapter.kt` - Command translation
  - `tasks/AssembleTask.kt` - Gradle task for Kick Assembler
  - `FlowTasksGenerator.kt` - Task factory pattern
  - `FlowDsl.kt` - DSL registration
- **Purpose**: Gradle integration and DSL builders
- **Integration Point**: Will add parallel dasm builders and tasks

#### Gradle Plugin
- **Location**: `infra/gradle/src/main/kotlin/com/github/c64lib/gradle/`
- **File**: `RetroAssemblerPlugin.kt`
- **Purpose**: Plugin initialization and use case injection
- **Integration Point**: Will inject `DasmAssembleUseCase`

### Architecture Alignment

#### Domain
- **Which domain**: New `compilers/dasm` domain for dasm-specific logic, extends flows domain for step definitions
- **Use Cases**:
  - `DasmAssembleUseCase` - Execute dasm compilation
- **Ports**:
  - `DasmAssemblePort` - Interface for dasm execution
  - Reuse `AssemblyPort` from flows for step execution (compiler-agnostic)
- **Adapters**:
  - **In (Gradle)**: `DasmStepBuilder`, `DasmPortAdapter`, `DasmAssembleTask`
  - **Out (System)**: `DasmAssembleAdapter` - Execute `dasm` command-line tool

#### Key Design Decision: Shared vs Separate Ports
**Question**: Should `DasmStep` and `AssembleStep` both implement `AssemblyPort`, or should dasm have its own `DasmAssemblyPort`?

**Recommendation**: Both should implement the same `AssemblyPort` interface from flows domain. This allows:
- Reuse existing port interface
- Same Flow execution context
- Potential future support for swapping assemblers per-step
- Cleaner architecture (assemblers are interchangeable implementations)

### Dependencies

1. **No new external dependencies**: dasm is provided via system PATH
2. **Internal dependencies**:
   - `compilers/dasm` depends on `flows` domain for port interface
   - `flows/adapters/in/gradle` depends on `compilers/dasm` for use case
   - `infra/gradle` adds `compilers/dasm` as `compileOnly` dependency

## 4. Questions and Clarifications

### Self-Reflection Questions (Answered via exploration)

- **Q**: How does Kick Assembler output format work?
  - **A**: Via `OutputFormat` enum (PRG, BIN). dasm supports output to file (-o flag), need to check what formats dasm supports.

- **Q**: How are additional input files (indirect dependencies) tracked?
  - **A**: Via `additionalInputs` in `AssemblyConfig` and `includeFiles`/`watchFiles` in builder. Same mechanism applies to dasm.

- **Q**: How does Gradle task dependency work for incremental builds?
  - **A**: Via `@InputFiles` and `@OutputFiles` task properties. AssembleTask registers these via `BaseFlowStepTask`.

- **Q**: Are there existing tests for assembly steps?
  - **A**: Yes, in `flows/src/test/kotlin/`. Can use same patterns for dasm.

- **Q**: How is the plugin version managed?
  - **A**: Via `dialectVersion` extension property in plugin. dasm doesn't need version management (system tool).

- **Q**: What are all the command-line parameters supported by dasm?
  - **A**: dasm supports: `-f#` (output format 1-3), `-o` (output file), `-l` (list file), `-L` (list all passes), `-s` (symbol dump file), `-v#` (verboseness 0-4), `-d` (debug mode), `-D`/`-M` (define symbols), `-I` (include directory), `-p#` (max passes), `-P#` (max passes with fewer checks), `-T#` (symbol table sorting), `-E#` (error format 0=MS, 1=Dillon, 2=GNU), `-S` (strict syntax checking), `-R` (remove output on errors), `-m#` (recursion safety barrier).

- **Q**: What output formats does dasm support?
  - **A**: dasm supports 3 output formats via `-f#` flag: format 1 (default binary), format 2, and format 3. All are binary output formats suitable for ROM development.

- **Q**: Should dasm-specific parameters be in `AssemblyConfig` or separate?
  - **A**: Create separate `DasmConfig` class. dasm has many unique parameters (symbol table sorting, error format, recursion safety) that don't apply to Kick Assembler. Separate config ensures type safety and clear separation of concerns.

- **Q**: Should there be a way to select which assembler to use per-step, or just `dasmStep` vs `assembleStep`?
  - **A**: Keep separate step classes. `AssembleStep` for Kick Assembler, `DasmStep` for dasm. This follows the existing pattern and provides better type safety in the DSL.

- **Q**: Does dasm support VICE symbol file generation?
  - **A**: Yes, via `-s` flag. This generates a symbol dump file compatible with VICE emulator, similar to Kick Assembler's symbol file support.

- **Q**: Should we validate dasm is in PATH during configuration, or fail at execution time?
  - **A**: Validate at execution time. This is consistent with other system tools and allows flexibility for users who install dasm after Gradle configuration. Fail fast with clear error message if dasm not found when task executes.


### Design Decisions

#### Decision 1: Step Type Separation
- **What needs to be decided**: Should we have one `AssemblyStep` with a compiler selector field, or separate `AssembleStep` (Kick) and `DasmStep` classes?
- **Options**:
  1. **Single `AssemblyStep` with compiler selector** - `compiler: Compiler.KICK_ASSEMBLER | Compiler.DASM`
  2. **Separate step classes** - `AssembleStep` for Kick, `DasmStep` for dasm
- **Recommendation**: **Separate step classes** because:
  - Each compiler has different parameter sets
  - Clearer type safety in DSL (can't mix incompatible parameters)
  - Follows current pattern (AssembleStep already exists)
  - Future extensibility for additional compilers
  - Easier to find/debug compiler-specific code

#### Decision 2: Shared Port vs Separate
- **What needs to be decided**: Should `DasmStep` use `AssemblyPort` or create `DasmAssemblyPort`?
- **Options**:
  1. **Reuse `AssemblyPort`** - Same port interface, different implementations
  2. **Create `DasmAssemblyPort`** - Separate port for dasm
- **Recommendation**: **Reuse `AssemblyPort`** because:
  - Core operation is the same (compile assembly)
  - Enables future flexibility (swap assemblers)
  - Cleaner architecture (single port, multiple implementations)
  - Follows DIP (Dependency Inversion Principle)
  - Test code can mock single interface

#### Decision 3: Parameter Handling for dasm-Specific Options
- **What needs to be decided**: How to handle dasm-specific parameters in DSL?
- **Options**:
  1. **Extend `AssemblyConfig`** - Add optional dasm-specific fields
  2. **Separate `DasmConfig`** - Create dasm-only config class
  3. **Raw parameters map** - `dasmParams: Map<String, String>` for flexibility
- **Recommendation**: **Separate `DasmConfig`** because:
  - Type safety (compiler-specific parameters won't be mixed)
  - Clear documentation of what each compiler supports
  - Builder pattern works the same way as `AssemblyConfig`
  - Easier to validate compiler-specific constraints
  - Future compilers can have their own configs

#### Decision 4: Output Format Support
- **What needs to be decided**: Should `OutputFormat` enum be shared or separate?
- **Options**:
  1. **Shared `OutputFormat`** - Both compilers support same formats
  2. **Separate `OutputFormat` per compiler** - Different enums for each
- **Recommendation**: **Shared `OutputFormat`** if dasm supports PRG/BIN, otherwise we'll create compiler-specific enums or map them

## 5. Implementation Plan

### Phase 1: Foundation - dasm Domain Module
**Goal**: Create the dasm compiler domain module with use case and ports

1. **Step 1.1**: Create `compilers/dasm` module structure
   - Files: `build.gradle.kts`, `src/main/kotlin/`, `src/test/kotlin/`
   - Description: Follow same structure as `compilers/kickass`
   - Testing: Verify directory structure created

2. **Step 1.2**: Create `DasmAssemblerSettings` domain class
   - Files: `compilers/dasm/src/main/kotlin/.../domain/DasmAssemblerSettings.kt`
   - Description: Store dasm-specific settings (minimal - just marks this is dasm compiler)
   - Testing: Unit test instantiation

3. **Step 1.3**: Create `DasmAssemblePort` port interface
   - Files: `compilers/dasm/src/main/kotlin/.../usecase/port/DasmAssemblePort.kt`
   - Description: Port interface for dasm execution (similar to `KickAssemblePort`)
   - Testing: Verify interface compiles

4. **Step 1.4**: Create `DasmAssembleUseCase` use case
   - Files: `compilers/dasm/src/main/kotlin/.../usecase/DasmAssembleUseCase.kt`
   - Description: Single-method `apply()` use case that delegates to `DasmAssemblePort`
   - Testing: Mock port, verify `apply()` delegates correctly

5. **Step 1.5**: Create `DasmAssembleAdapter` for CLI execution
   - Files: `compilers/dasm/adapters/out/gradle/src/main/kotlin/.../DasmAssembleAdapter.kt`
   - Description: Implements `DasmAssemblePort`, executes `dasm` command via system exec
   - Testing: Mock project/exec, verify command building

6. **Step 1.6**: Create command-line builder for dasm
   - Files: `compilers/dasm/adapters/out/gradle/src/main/kotlin/.../DasmCommandLineBuilder.kt`
   - Description: Build dasm CLI arguments from parameters
   - Testing: Unit tests for each parameter combination

7. **Step 1.7**: Add `compilers/dasm` as dependency in `infra/gradle`
   - Files: `infra/gradle/build.gradle.kts`
   - Description: Add `compileOnly project(':compilers:dasm')`
   - Testing: Verify build succeeds

**Phase 1 Deliverable**: A functional `DasmAssembleUseCase` that can compile assembly code via dasm CLI. Testable in isolation with mocked Gradle project.

---

### Phase 2: Flows Domain Integration
**Goal**: Create dasm-specific step classes and configuration in flows domain

1. **Step 2.1**: Create `DasmConfig` data class
   - Files: `flows/src/main/kotlin/.../domain/config/DasmConfig.kt`
   - Description: dasm-specific configuration (separate from `AssemblyConfig`)
   - Fields: includes (List<String>), defines (Map<String, String>), outputFormat (1-3), listFile (Optional), symbolFile (Optional), verboseness (0-4), errorFormat (0=MS, 1=Dillon, 2=GNU), strictSyntax (Boolean), removeOnError (Boolean), symbolTableSort (0=alphabetical, 1=address)
   - Testing: Unit test instantiation with various configs

2. **Step 2.2**: Create `DasmStep` flow step class
   - Files: `flows/src/main/kotlin/.../domain/steps/DasmStep.kt`
   - Description: Extends `FlowStep`, holds dasm configuration
   - Methods: `execute()`, `validate()`, `getConfiguration()`
   - Testing: Unit tests for validation, execution context setup

3. **Step 2.3**: Create `DasmConfigMapper` for config conversion
   - Files: `flows/src/main/kotlin/.../domain/config/DasmConfigMapper.kt`
   - Description: Convert `DasmConfig` to dasm command structure (similar to `AssemblyConfigMapper`)
   - Methods: File discovery, output resolution, command building
   - Testing: Unit tests with various glob patterns and file scenarios

4. **Step 2.4**: Create `DasmCommand` data class (if needed)
   - Files: `flows/src/main/kotlin/.../domain/config/DasmCommand.kt` (if separate from `AssemblyCommand`)
   - Description: Immutable command data passed to use case
   - Testing: Unit test instantiation

**Phase 2 Deliverable**: Complete dasm step domain logic with configuration and validation. Can be tested independently with mock ports.

---

### Phase 3: Gradle Adapter Integration
**Goal**: Connect dasm steps to Gradle task infrastructure

1. **Step 3.1**: Create `DasmStepBuilder` DSL builder
   - Files: `flows/adapters/in/gradle/src/main/kotlin/.../dsl/DasmStepBuilder.kt`
   - Description: Type-safe builder for configuring dasm steps (similar to `AssembleStepBuilder`)
   - Methods: `from()`, `to()`, `includePath()`, `define()`, plus dasm-specific setters
   - Testing: Unit tests for builder method chaining

2. **Step 3.2**: Create `DasmPortAdapter`
   - Files: `flows/adapters/in/gradle/src/main/kotlin/.../assembly/DasmPortAdapter.kt`
   - Description: Implements `AssemblyPort`, bridges to `DasmAssembleUseCase`
   - Method: `assemble()` - converts step config to dasm commands
   - Testing: Mock use case, verify command translation

3. **Step 3.3**: Create `DasmCommandAdapter`
   - Files: `flows/adapters/in/gradle/src/main/kotlin/.../assembly/DasmCommandAdapter.kt`
   - Description: Converts domain command to dasm-specific command structure
   - Testing: Unit tests for command translation

4. **Step 3.4**: Create `DasmAssembleTask` Gradle task
   - Files: `flows/adapters/in/gradle/src/main/kotlin/.../tasks/DasmAssembleTask.kt`
   - Description: Gradle task for dasm step execution (extends `BaseFlowStepTask`)
   - Properties: `@InputFiles`, `@OutputFiles`, injected `DasmAssembleUseCase`
   - Testing: Integration test with real Gradle project

5. **Step 3.5**: Register dasm step in `FlowTasksGenerator`
   - Files: `flows/adapters/in/gradle/src/main/kotlin/.../FlowTasksGenerator.kt`
   - Description: Add pattern matching for `DasmStep`, create `DasmAssembleTask`
   - Testing: Unit tests for task factory

6. **Step 3.6**: Register `dasmStep` in `FlowDsl`
   - Files: `flows/adapters/in/gradle/src/main/kotlin/.../FlowDsl.kt`
   - Description: Add `dasmStep {}` builder method to DSL
   - Testing: Integration test with flows extension

7. **Step 3.7**: Add dasm use case injection in plugin
   - Files: `infra/gradle/src/main/kotlin/.../RetroAssemblerPlugin.kt`
   - Description: Create `DasmAssembleUseCase`, pass to `FlowTasksGenerator`
   - Testing: Integration test with full plugin setup

**Phase 3 Deliverable**: Full Flow DSL support for dasm with working `dasmStep {}` builder. Can be used in gradle build files to compile with dasm.

---

### Phase 4: Testing & Polish
**Goal**: Comprehensive testing and documentation

1. **Step 4.1**: Add unit tests for dasm domain module
   - Files: `compilers/dasm/src/test/kotlin/`
   - Description: Tests for use case, command builder, parameter handling
   - Testing: Run `./gradlew :compilers:dasm:test`

2. **Step 4.2**: Add unit tests for flows dasm components
   - Files: `flows/src/test/kotlin/` (DasmStep, DasmConfig, DasmConfigMapper)
   - Description: Step validation, configuration mapping, file discovery
   - Testing: Run `./gradlew :flows:test`

3. **Step 4.3**: Add integration tests for dasm DSL
   - Files: `flows/adapters/in/gradle/src/test/kotlin/` (DasmStepBuilder, FlowDsl)
   - Description: End-to-end DSL testing
   - Testing: Run `./gradlew :flows:adapters:in:gradle:test`

4. **Step 4.4**: Verify full build and tests pass
   - Files: N/A
   - Description: Run full build, ensure no regressions
   - Testing: `./gradlew build`

5. **Step 4.5**: Document dasm usage in project README/CLAUDE.md
   - Files: `README.md` or `CLAUDE.md`
   - Description: Add section on dasm usage and parameters
   - Testing: Manual verification of documentation accuracy

**Phase 4 Deliverable**: Fully tested, documented dasm compiler support ready for release.

---

## 6. Testing Strategy

### Unit Tests

**Domain Module Tests** (`compilers/dasm/src/test/kotlin/`):
- `DasmAssembleUseCaseTest` - Verify use case delegates to port
- `DasmCommandLineBuilderTest` - Test CLI argument generation for all parameters
- `DasmAssembleAdapterTest` - Mock project/exec, verify command execution

**Flows Domain Tests** (`flows/src/test/kotlin/`):
- `DasmStepTest` - Validate step configuration, execute method
- `DasmConfigTest` - Validate constraints (required fields, ranges)
- `DasmConfigMapperTest` - Test file discovery, path resolution, command generation

**Gradle Adapter Tests** (`flows/adapters/in/gradle/src/test/kotlin/`):
- `DasmStepBuilderTest` - Builder method chaining, default values
- `DasmPortAdapterTest` - Port implementation with mocked use case
- `DasmCommandAdapterTest` - Command translation logic
- `DasmAssembleTaskTest` - Task input/output registration, execution

### Integration Tests

- Full Flow DSL parsing and task generation
- dasm step execution in test gradle project
- Incremental build behavior (task up-to-date detection)
- File discovery with glob patterns
- Parameter passing from DSL through to CLI

### Manual Testing

- Create sample build.gradle with dasm step
- Run `gradle build` and verify dasm compilation works
- Modify source file and verify incremental build
- Test with various parameter combinations
- Verify error handling for missing dasm executable

## 7. Risks and Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| dasm not in PATH | Task fails at runtime | High | Validate dasm availability early (configuration phase), provide helpful error message |
| dasm parameter differences | Compilation fails | Medium | Document all dasm parameters, create comprehensive unit tests for CLI builder |
| Output format mismatch | Generated files unusable | Medium | Test with real dasm to verify output formats, handle format conversion if needed |
| Incremental build breakage | Silent build failures | Medium | Add unit tests for file tracking, integration test with real file changes |
| Breaking Kick Assembler | Existing users affected | Low | Use separate step classes, no changes to existing `AssembleStep`, run full test suite |
| Missing dasm-specific features | Feature incomplete | Low | Ask user for specific dasm params needed, add to Phase 2 design |

## 8. Documentation Updates

- [ ] Add dasm section to CLAUDE.md with architecture notes
- [ ] Document `dasmStep {}` DSL in README (parameters, examples)
- [ ] Add inline Kdoc comments for dasm-specific classes
- [ ] Document dasm parameter mapping (CLI flags to config properties)
- [ ] Add troubleshooting section for PATH issues
- [ ] Add example build.gradle showing dasm usage

## 9. Rollout Plan

1. **Phase 1-2**: Internal development and testing
   - Merge foundation and flows domain work to feature branch
   - Run full test suite, verify no Kick Assembler regressions

2. **Phase 3**: Gradle integration and DSL
   - Add task infrastructure
   - Test with sample projects
   - Verify incremental builds work

3. **Phase 4**: Polish and release
   - Complete all unit/integration tests
   - Update documentation
   - Merge to develop/main branch

4. **Monitoring**:
   - Watch for GitHub issues related to dasm
   - Monitor build times (ensure no performance regression)
   - Collect user feedback on missing features

5. **Rollback Strategy**:
   - If critical issues found: revert Phase 3 (DSL integration)
   - Keep Phase 1-2 (domain logic) as internal implementation
   - Phase 1-2 can be reverted cleanly as they don't affect existing code

---

## 10. Revision History

| Date | Updated By | Changes |
|------|------------|---------|
| 2025-11-15 | Claude Code | Answered all 6 unresolved questions via dasm CLI inspection. Discovered full parameter set, output formats, and decided on separate DasmConfig with validated parameters. Updated DasmConfig field definitions in Phase 2 with discovered dasm parameters. |

---

**Note**: This plan has been updated with discovered dasm parameters and answered all unresolved questions. Ready to begin Phase 1 implementation.
