# Action Plan for Issue #66: Goattracker Flow Step Implementation

## Issue Description

Implement complete flow step integration for GoatTracker processor. The current flow DSL has placeholder implementations that need to be replaced with actual processor integration. The goal is to support all capabilities of the legacy Gradle DSL within the new flow step system.

**Issue Number:** 66
**Title:** goattracker-step
**Description:** Implement flow step for goattracker, support all capabilities of goattracker as per old DSL, implement new flow step DSL for goattracker

---

## Relevant Codebase Parts

### 1. Flow Domain Layer
**Location:** `flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/`

- `steps/GoattrackerStep.kt` - Domain model for goattracker processing steps
  - **Status:** Partially implemented (basic structure only)
  - **Issue:** `execute()` method contains placeholder println code, `port` injection missing
  - **Lines:** 38-50 contain TODOs for actual processor integration

- `config/ProcessorConfig.kt` - Configuration data classes
  - **Status:** Partially implemented (`GoattrackerConfig` exists with only 5 parameters)
  - **Issue:** Missing 9 parameters from old DSL (bufferedSidWrites, disableOptimization, playerMemoryLocation, sfxSupport, sidMemoryLocation, storeAuthorInfo, volumeChangeSupport, zeroPageLocation, zeropageGhostRegisters)
  - **Current params:** exportFormat, optimization, frequency, channels, filterSupport
  - **Missing:** Executable path configuration

- **Missing:** `port/GoattrackerPort.kt` interface
  - Should follow pattern from CharpadPort/SpritepadPort
  - Need methods: `process(command: GoattrackerCommand)` and overload for lists

- **Missing:** `config/GoattrackerCommand.kt` domain class
  - Should contain: inputFile, output, config, projectRootDir, and all 9 parameters
  - Follow pattern from CharpadCommand/SpritepadCommand

### 2. Flow Inbound Adapter (Gradle DSL)
**Location:** `flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/`

- `dsl/GoattrackerStepBuilder.kt` - Type-safe DSL builder
  - **Status:** Partially implemented (basic structure only)
  - **Issue:** Only supports 5 parameters, missing 9 from old DSL
  - **Issue:** No executable path configuration

- `tasks/GoattrackerTask.kt` - Gradle task implementation
  - **Status:** Placeholder implementation with fake file writing
  - **Issue:** Lines 50-72 contain placeholder code that writes dummy files
  - **Issue:** No port injection or actual processor invocation
  - **Missing:** Port creation and injection into GoattrackerStep

### 3. Flow Outbound Adapter (Processor Integration)
**Location:** `flows/adapters/out/` - **MISSING ENTIRELY**

- **Missing:** `goattracker/` module directory
- **Missing:** `GoattrackerAdapter.kt` implementing GoattrackerPort
  - Should bridge to existing `processors/goattracker` module
  - Should create PackSongUseCase and invoke it
  - Should handle exceptions and convert to FlowValidationException

### 4. Existing Goattracker Processor (Reference)
**Location:** `processors/goattracker/`

- `src/main/kotlin/...usecase/PackSongUseCase.kt`
  - Already supports all 9 parameters via PackSongCommand
  - Delegates to ExecuteGt2RelocPort for actual execution

- `src/main/kotlin/...usecase/PackSongCommand.kt`
  - Contains: source, output, executable, useBuildDir, + 9 optional parameters
  - Can be used directly by flow adapter

- `adapters/out/gradle/ExecuteGt2RelocAdapter.kt`
  - Executes gt2reloc tool with command-line flags
  - Supports all 9 parameters with proper flag mapping

### 5. Reference Implementations
**Pattern Sources:** Charpad and Spritepad implementations

- `flows/adapters/out/charpad/` - Complete reference implementation
  - Shows pattern for adapter, factory, port integration
  - `CharpadAdapter.kt` - Creates OutputProducers and calls domain use case
  - `CharpadOutputProducerFactory.kt` - Maps configuration to producers

- `flows/adapters/out/spritepad/` - Complete reference implementation
  - Similar pattern for different processor

---

## Root Cause Hypothesis

**Primary Cause:** The flow step integration for GoatTracker is incomplete placeholder code created before the patterns (Charpad/Spritepad) were fully established.

**Supporting Evidence:**
1. GoattrackerStep.execute() contains only `println()` statements (lines 39-49)
2. GoattrackerTask.executeStepLogic() simulates output by writing dummy files (lines 52-72)
3. GoattrackerStepBuilder only supports 5 of 9+ available parameters from old DSL
4. No GoattrackerPort interface exists (unlike CharpadPort, SpritepadPort)
5. No GoattrackerAdapter adapter exists (unlike CharpadAdapter, SpritepadAdapter)
6. No outbound adapter module exists for goattracker in flows/adapters/out/

**Architecture Mismatch:** The flow system follows hexagonal architecture (domain → port → adapter), but GoatTracker flow step skips the port/adapter layers and attempts direct integration without proper abstraction.

**Resolution Strategy:**
1. Create complete domain model following Charpad/Spritepad pattern
2. Expand GoattrackerConfig to support all 9 parameters from old DSL
3. Implement GoattrackerPort interface and adapter
4. Bridge flow adapter to existing PackSongUseCase from processors/goattracker
5. Update GoattrackerTask to use port injection pattern
6. Expand GoattrackerStepBuilder to expose all parameters

---

## Investigation Questions

### Self-Reflection Questions

1. **Parameter Coverage:** Are all 9 parameters from GoattrackerMusicExtension (bufferedSidWrites, disableOptimization, playerMemoryLocation, sfxSupport, sidMemoryLocation, storeAuthorInfo, volumeChangeSupport, zeroPageLocation, zeropageGhostRegisters) required in the flow DSL, or are some legacy-only?

2. **Output Strategy:** GoatTracker generates a single output file per input. Should the flow step:
   - Accept single output path in DSL: `to("path/output.sid")`
   - Or allow multiple output formats like Charpad?
   - How does this map to the old DSL behavior?

3. **Executable Configuration:** Should users be able to configure gt2reloc executable path in flow DSL (like in old DSL), or assume default "gt2reloc" on PATH?

4. **Port Injection Pattern:** For GoattrackerStep, should GoattrackerPort be:
   - Injected via constructor (like Charpad)
   - Set via method (like `setGoattrackerPort()`)
   - Both patterns?

5. **Testing Strategy:** What integration tests exist for the old Gradle task? Should we mirror them for flow integration?

6. **Backward Compatibility:** Does the flow DSL need to preserve exact configuration naming from old DSL, or can it use more idiomatic names?

### Questions for Others

1. **Parameter Validation:** Which of the 9 parameters have constraints (ranges, hex values, boolean flags)? How should validation errors be reported?

2. **Export Format:** The GoattrackerConfig has `exportFormat: GoattrackerFormat` enum (SID_ONLY, ASM_ONLY, SID_AND_ASM). Does this replace the single output path, or work alongside it?

3. **Frequency Handling:** The Frequency enum (PAL, NTSC) affects timing. Are there any frequency-dependent parameters that need special handling?

4. **Filter Support:** What does `filterSupport: Boolean` do? Is it related to sound filters or data filtering? Should flow DSL expose this?

5. **Legacy Processor:** Are there any existing integration tests for processors/goattracker that demonstrate expected behavior?

---

## Next Steps

### Phase 1: Expand Domain Configuration (1-2 hours)

1. **Update GoattrackerConfig** in `flows/src/main/kotlin/.../config/ProcessorConfig.kt`
   - Add 9 missing parameters with appropriate types
   - Add executable: String = "gt2reloc"
   - Map parameter names to match old DSL naming conventions
   - Provide sensible defaults matching old processor behavior

2. **Create GoattrackerCommand** in `flows/src/main/kotlin/.../config/GoattrackerCommand.kt`
   - Data class with: inputFile, output, config, projectRootDir, workingDirectory
   - Follow pattern from CharpadCommand (which mirrors PackSongCommand structure)
   - Include all 9 parameters for direct mapping to PackSongCommand

3. **Create GoattrackerPort** in `flows/src/main/kotlin/.../domain/port/GoattrackerPort.kt`
   - Interface with overloaded process() methods
   - Pattern: `fun process(command: GoattrackerCommand)` and `fun process(commands: List<GoattrackerCommand>)`
   - Matches CharpadPort/SpritepadPort pattern

### Phase 2: Update Flow Step Domain (1-2 hours)

4. **Update GoattrackerStep.execute()**
   - Replace placeholder println code
   - Extract projectRootDir from execution context
   - Resolve relative input file paths against project root
   - Validate input files exist and are readable
   - Create GoattrackerCommand for each input file
   - Inject GoattrackerPort and call port.process(commands)
   - Handle exceptions with proper error messages

5. **Add Port Injection to GoattrackerStep**
   - Add private var: `private var goattrackerPort: GoattrackerPort? = null`
   - Add method: `fun setGoattrackerPort(port: GoattrackerPort)`
   - Validate port is injected in execute()

### Phase 3: Implement Outbound Adapter (2-3 hours)

6. **Create flows/adapters/out/goattracker module structure**
   - Create directory: `flows/adapters/out/goattracker/`
   - Create `src/main/kotlin/com/github/c64lib/rbt/flows/adapters/out/goattracker/`

7. **Create GoattrackerAdapter** (main implementation)
   - Implement GoattrackerPort interface
   - For each GoattrackerCommand:
     - Validate input file (exists, readable, non-empty)
     - Create PackSongCommand from GoattrackerCommand
     - Instantiate PackSongUseCase with ExecuteGt2RelocPort
     - Call useCase.apply(packSongCommand)
     - Handle exceptions: InvalidFileException → FlowValidationException
   - Comprehensive error handling with context in messages

8. **Create build.gradle.kts** for adapter module
   - Depends on: `:flows` (for port), `:processors:goattracker` (for PackSongUseCase, ExecuteGt2RelocPort)
   - Uses appropriate plugin configuration

### Phase 4: Update DSL and Task Layer (1-2 hours)

9. **Update GoattrackerStepBuilder** in `flows/adapters/in/gradle/.../dsl/`
   - Add properties for all 9 new parameters
   - Add property for executable path
   - Update build() method to include all parameters in GoattrackerConfig
   - Keep existing from()/to() methods for input/output paths

10. **Update GoattrackerTask.executeStepLogic()** in `flows/adapters/in/gradle/.../tasks/`
    - Remove placeholder file writing code (lines 52-72)
    - Create GoattrackerAdapter instance
    - Call `step.setGoattrackerPort(adapter)`
    - Create execution context with projectRootDir
    - Ensure GoattrackerStep is type-safe cast
    - Call step.execute(context)

### Phase 5: Build Configuration (30 minutes)

11. **Register new module in settings.gradle.kts**
    - Add include statement for flows/adapters/out/goattracker

12. **Add compileOnly dependency in infra/gradle/build.gradle.kts**
    - Add `:flows:adapters:out:goattracker` to compileOnly dependencies
    - This ensures plugin can load GoattrackerAdapter at runtime

### Phase 6: Testing (2-3 hours)

13. **Unit Tests for GoattrackerAdapter**
    - Test successful parameter mapping to PackSongCommand
    - Test exception handling (FileNotFoundException → FlowValidationException)
    - Test validation of input files
    - Test all 9 parameters are correctly passed through

14. **Integration Tests for GoattrackerTask**
    - Test end-to-end flow with sample .sng files (if available)
    - Validate task inputs/outputs are properly tracked
    - Test incremental build behavior

15. **DSL Tests for GoattrackerStepBuilder**
    - Test all 9 parameters are configurable via builder
    - Test executable path configuration
    - Test validation in GoattrackerStep

### Phase 7: Validation (1 hour)

16. **Manual Testing**
    - Create sample build script using goattrackerStep DSL
    - Verify all 9 parameters are accessible
    - Test with actual .sng file if possible
    - Verify output files are generated correctly

17. **Documentation Review**
    - Ensure CLAUDE.md reflects goattracker flow step implementation
    - Add examples to any DSL documentation

---

## Debugging and Logging Strategy

### Logging Points

1. **GoattrackerStep.execute()**
   - Log port injection status
   - Log input file resolution (before/after)
   - Log GoattrackerCommand creation details

2. **GoattrackerAdapter.process()**
   - Log input file validation (exists, readable, size)
   - Log PackSongCommand creation
   - Log actual processor execution

3. **GoattrackerTask.executeStepLogic()**
   - Log step type validation
   - Log adapter creation
   - Log execution context preparation

### Error Handling

- All domain exceptions should be caught and converted to descriptive messages
- File not found errors should suggest checking paths
- Invalid configuration errors should list actual vs expected values
- Port injection failures should be clear and actionable

---

## Additional Notes

### Architecture Patterns Used

- **Hexagonal Architecture:** Domain → Port → Adapter (matching Charpad/Spritepad)
- **Dependency Injection:** Via port setter method on domain step
- **Factory Pattern:** GoattrackerAdapter creates PackSongCommand from flow domain config
- **Gradle Integration:** Via BaseFlowStepTask for incremental builds

### Key Dependencies

- `flows` module (domain + adapters/in)
- `processors/goattracker` module (PackSongUseCase, PackSongCommand, ExecuteGt2RelocPort)
- `shared/gradle` module (existing GoattrackerMusicExtension for reference)

### Potential Challenges

1. **Parameter Mapping:** Need to map all 9 parameters correctly from GoattrackerConfig → PackSongCommand
2. **Output Directory Handling:** Goattracker outputs single file per input; ensure working directory setup is correct
3. **Executable Path:** Need to support both default "gt2reloc" and custom paths
4. **Gradle Integration:** Must properly integrate with Workers API if parallel execution needed

### Testing Considerations

- Existing goattracker processor may have integration tests that can inform flow tests
- Should validate gt2reloc executable is available before executing
- Output file validation should check both .sid and .asm files (if SID_AND_ASM format)
