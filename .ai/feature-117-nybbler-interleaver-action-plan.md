# Action Plan for Issue #117: Nybbler-Interleaver

## Issue Description

The old charpad preprocessor (Gradle DSL-based) supports nybbler and interleaver filters to be applied onto each output. These capabilities need to be extended to the new charpad flow step to maintain feature parity with the existing system.

**Important**: Both the old DSL structure and new flow-based structure will be maintained in parallel. This is NOT a migration effort; the goal is to extend the new flow step with equivalent capabilities while keeping the old system functional.

**Nybbler**: Splits bytes into low and high nibbles (4-bit halves), writing them to separate output files. Allows direct access to nybbles (either upper or lower 4 bits of a byte) for independent conversion and transformation. Supports optional normalization of high nibbles.

**Interleaver**: Distributes binary data across multiple output streams in round-robin fashion, splitting the byte stream evenly. Allows access to bytes within larger chunks (e.g., upper or lower byte of a word value) independently for conversion and transformation.

## Relevant Codebase Parts

### 1. Existing Filter Implementations (Shared)
**Location**: `shared/gradle/src/main/kotlin/com/github/c64lib/rbt/shared/gradle/filter/`
- **Nybbler.kt** (lines 29-49): Core nybbler implementation with low/high nibble splitting
- **BinaryInterleaver.kt** (lines 31-49): Core interleaver implementation with round-robin distribution
- **NybblerTest.kt** (lines 31-97): Comprehensive tests showing nybbler usage patterns
- **BinaryInterleaverTest.kt** (lines 34-75): Tests demonstrating interleaver behavior

**Reasoning**: These files contain the working implementations that need to be integrated into the new flow step. The logic itself doesn't need to change; it needs to be wired into the new architecture.

### 2. Old Charpad DSL Configuration (Reference Implementation)
**Location**: `shared/gradle/src/main/kotlin/com/github/c64lib/rbt/shared/gradle/dsl/`
- **FilterAwareExtension.kt** (lines 51-110): Shows how nybbler/interleaver are configured and applied in the old system
- **NybblerExtension.kt** (lines 29-33): DSL model for nybbler configuration
- **InterleaverExtension.kt** (lines 29-32): DSL model for interleaver configuration
- **StartEndExtension.kt** (lines 30-34): Base class for charset/map outputs that inherits filter support

**Reasoning**: This shows the existing API contract that users depend on. The new flow step should offer equivalent functionality, though the API may differ to fit the flow-based architecture.

### 3. New Charpad Flow Step (Target for Extension)
**Location**: `flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/`
- **steps/CharpadStep.kt** (lines 35-97): Flow step execution logic that needs to apply filters
- **config/CharpadOutputs.kt** (lines 34-160): Output configuration classes (RangeOutput, MapOutput, etc.) that need filter fields
- **config/ProcessorConfig.kt** (lines 44-59): CharpadConfig that may need global filter settings

**Reasoning**: These are the domain models that define the charpad flow step's capabilities. They need to be extended with nybbler/interleaver configuration fields.

### 4. New Charpad Output Producer Factory (Implementation Point)
**Location**: `flows/adapters/out/charpad/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/out/charpad/`
- **CharpadOutputProducerFactory.kt** (lines 44-171): Factory that creates output producers; needs to wrap outputs with filters
- **createBinaryOutput()** method (lines 164-171): Currently creates simple file outputs; needs to support filter wrapping

**Reasoning**: This is where the filter application logic needs to be implemented. Similar to how FilterAwareExtension.resolveOutput() works in the old system, this factory needs to wrap binary outputs with nybbler/interleaver filters based on configuration.

### 5. Integration Tests
**Location**: `processors/charpad/src/test/kotlin/`
- Tests for old charpad preprocessor that demonstrate nybbler/interleaver usage
- New tests will be needed in `flows/src/test/kotlin/` to verify the new implementation

**Reasoning**: Tests define expected behavior and ensure the migration maintains compatibility.

## Root Cause Hypothesis

**Hypothesis**: The new charpad flow step was implemented with a focus on core functionality (processing CharPad files and extracting various output types) but did not yet implement the binary filtering capabilities (nybbler/interleaver) that existed in the legacy system.

**Evidence**:
1. The filter implementations (Nybbler.kt, BinaryInterleaver.kt) exist in shared/gradle and are working
2. The old DSL system (FilterAwareExtension) has complete nybbler/interleaver support
3. The new flow step's CharpadOutputs data classes have no fields for filter configuration
4. CharpadOutputProducerFactory creates simple file outputs without any filter wrapping

**Why this happened**: The flow-based architecture was likely built incrementally, starting with core charpad processing. Binary filtering is an advanced feature that was deferred for later implementation (this issue).

**What needs to happen**: The existing filter implementations need to be integrated into the flow step by:
1. Adding configuration fields to domain models (CharpadOutputs)
2. Updating the output producer factory to wrap outputs with filters
3. Enforcing mutual exclusivity - only one filter type (nybbler OR interleaver) per output

**Scope Clarification**: The old DSL will continue to exist and function unchanged. This is a parallel implementation to extend the new flow-based system, not a replacement or migration.

**Filter Purpose**: Both nybbler and interleaver are complementary filters that operate at different granularity levels:
- **Nybbler** works at 4-bit granularity (nibble level) - separating upper/lower nibbles for independent processing
- **Interleaver** works at byte granularity within larger chunks (word/multi-byte level) - separating individual bytes for independent processing
These filters enable flexible data transformation pipelines for asset processing in Commodore 64 development.

## Investigation Questions

### Self-Reflection Questions

1. **Configuration Design**: Should nybbler/interleaver be configured per-output (e.g., each CharsetOutput can have its own nybbler), or globally for all charpad outputs?
   - **Analysis**: The old system uses per-output configuration (StartEndExtension for charset/map). This provides maximum flexibility. Recommendation: Per-output configuration.

2. **API Compatibility**: Should the new flow step's filter API exactly mirror the old DSL, or should it be redesigned to fit the flow-based paradigm?
   - **Analysis**: The flow system uses Kotlin data classes rather than Gradle DSL actions. The concepts (loOutput, hiOutput, normalizeHi) should remain the same, but the syntax will differ.

3. ✅ **Filter Ordering**: In what order should filters be applied? Should nybbler come before or after interleaver?
   - **Answer**: Not applicable - filters cannot be combined (mutually exclusive)
   - **Analysis**: FilterAwareExtension.kt lines 74-110 shows only one filter type per output. Configuration must enforce that only nybbler OR interleaver can be configured, never both.

4. **Output Path Tracking**: How should additional output paths (nybbler's loOutput/hiOutput, interleaver outputs) be registered with the flow step's getAllOutputPaths()?
   - **Analysis**: CharpadStep.kt line 49 calls charpadOutputs.getAllOutputPaths(). This needs to include filter outputs for dependency tracking.

5. **Null Output Handling**: The old system allows null outputs (e.g., getNybbler().loOutput can be null). Should the new system support this, or require explicit file paths?
   - **Analysis**: FilterAwareExtension allows null and uses DevNull() as a fallback. This should be preserved for flexibility.

6. **Multiple Interleavers**: The old system supports multiple interleavers via a list. Is this still required in the new system?
   - **Analysis**: InterleaverExtension shows `interleavers.add(ex)` - yes, multiple interleavers are supported. The new system should maintain this.

### Questions for Others

1. ✅ **Architecture Decision - Old vs New Structure**: Should the old DSL and new flow step maintain separate implementations or share code?
   - **Answer**: Keep old structure in parallel. Both systems will coexist independently.
   - **Implication**: No refactoring of shared/gradle components required. Only new flow step needs extension.

2. ✅ **Use Cases**: What are the primary use cases for nybbler and interleaver in Commodore 64 development?
   - **Answer**:
     - **Nybbler**: Used to access nybbles directly (either upper or lower 4 bits of a byte) and perform further conversions on them independently
     - **Interleaver**: Allows access to bytes within larger chunks independently (e.g., upper or lower byte of a word value) for conversion
   - **Implication**: These are complementary filters for different granularity levels of data access and transformation

3. ✅ **Testing Requirements**: Are there existing CharPad projects with nybbler/interleaver configurations that can be used for integration testing?
   - **Answer**: Existing tests in the old functionality should be copied and converted to the new flow-based system
   - **Implication**:
     - Reduces test creation effort by reusing proven test cases
     - Ensures parity between old and new implementations
     - Existing tests serve as both validation and documentation
     - Test location: Copy from old test location to `flows/src/test/kotlin/...`

4. ✅ **Filter Combinations**: Can nybbler and interleaver be applied simultaneously to the same output, or are they mutually exclusive?
   - **Answer**: They cannot be combined - they are mutually exclusive
   - **Implication**:
     - Configuration model should enforce mutual exclusivity (either nybbler OR interleaver per output, not both)
     - Validation logic must reject configurations with both filters on the same output
     - Simplifies implementation - no need to handle filter composition/ordering
     - Each output can have only one filter type applied

5. ✅ **Performance**: Are there performance concerns with filter usage? Should the new implementation optimize for specific scenarios?
   - **Answer**: Existing domain implementation is very performant - no additional optimization needed as long as existing domain implementations (Nybbler, BinaryInterleaver) are used
   - **Implication**:
     - No custom buffering or optimization strategies required
     - Reuse existing Nybbler.kt and BinaryInterleaver.kt implementations directly
     - Focus on correct integration, not performance tuning
     - No performance-related tests needed

## Next Steps

### 1. Enforce Filter Mutual Exclusivity
**Action**: Ensure the configuration model enforces that nybbler and interleaver are mutually exclusive on any output:
1. Only allow nybbler OR interleaver per output type, never both
2. Add validation logic to reject conflicting configurations
3. Consider using sealed class hierarchy or explicit validation

**Rationale**: Nybbler and interleaver cannot be combined - they are mutually exclusive. This simplifies the implementation and avoids filter composition complexity. FilterAwareExtension.kt lines 74-110 shows separate branches for each filter type.

**Expected Outcome**: Validation rules preventing misconfiguration of incompatible filters.

### 2. Design Configuration Model
**Action**: Create data classes for filter configuration in the flows domain with mutual exclusivity enforcement:
```kotlin
// In flows/src/main/kotlin/.../domain/config/CharpadOutputs.kt

sealed class FilterConfig {
    data class Nybbler(
        val loOutput: String? = null,
        val hiOutput: String? = null,
        val normalizeHi: Boolean = true
    ) : FilterConfig()

    data class Interleaver(
        val outputs: List<String>
    ) : FilterConfig()

    object None : FilterConfig()
}

// Extend RangeOutput implementations
data class CharsetOutput(
    override val output: String,
    override val start: Int = 0,
    override val end: Int = 65536,
    val filter: FilterConfig = FilterConfig.None
) : RangeOutput
```

**Rationale**:
- Sealed class hierarchy enforces mutual exclusivity at the type level
- Only one filter type can be selected per output (nybbler, interleaver, or none)
- Maintains domain-driven design while preventing invalid configurations
- Using data classes is idiomatic for the flows module

**Expected Outcome**: Type-safe configuration model with compile-time mutual exclusivity enforcement.

### 3. Update getAllOutputPaths()
**Action**: Modify CharpadOutputs.kt to include filter output paths in getAllOutputPaths():
```kotlin
fun getAllOutputPaths(): List<String> {
    val primaryOutputs = charsets.map { it.output } +
                        charAttributes.map { it.output } + ...
    val filterOutputs = (charsets + charAttributes + ...).flatMap { output ->
        when (val filter = output.filter) {
            is FilterConfig.Nybbler -> listOfNotNull(filter.loOutput, filter.hiOutput)
            is FilterConfig.Interleaver -> filter.outputs
            FilterConfig.None -> emptyList()
        }
    }
    return primaryOutputs + filterOutputs
}
```

**Rationale**: Flow dependency tracking requires knowing all output files. Filters create additional outputs that must be registered. Sealed class pattern simplifies the extraction logic.

**Expected Outcome**: Correct dependency graph for flow execution ordering, including all filter outputs.

### 4. Implement Filter Wrapping in CharpadOutputProducerFactory
**Action**: Modify CharpadOutputProducerFactory.kt to wrap binary outputs with filters based on sealed class:
```kotlin
private fun createBinaryOutput(outputFile: File, filter: FilterConfig): Output<ByteArray> {
    val baseOutput = FileBinaryOutput(outputFile)
    return when (filter) {
        is FilterConfig.Nybbler -> {
            val lo = filter.loOutput?.let { FileBinaryOutput(File(it)) }
            val hi = filter.hiOutput?.let { FileBinaryOutput(File(it)) }
            Nybbler(lo, hi, filter.normalizeHi)
        }
        is FilterConfig.Interleaver -> {
            val outputs = filter.outputs.map { FileBinaryOutput(File(it)) }
            BinaryInterleaver(List.ofAll(outputs))
        }
        FilterConfig.None -> baseOutput
    }
}
```

**Rationale**:
- Sealed class pattern provides exhaustive when expression - compiler ensures all cases handled
- Mutual exclusivity enforced at type level - cleaner than boolean flags
- Mirrors FilterAwareExtension.resolveOutput() logic but type-safe
- Existing filter implementations (Nybbler, BinaryInterleaver) reused directly

**Expected Outcome**: Type-safe functional nybbler and interleaver support in the charpad flow step.

### 5. Write Unit Tests
**Action**: Copy and convert existing tests from old charpad functionality to the new flow-based system:
1. Identify all charpad-related tests in `processors/charpad/src/test/kotlin/`
2. Copy tests that cover nybbler/interleaver functionality
3. Convert them to work with the new CharpadStep architecture (instead of old DSL)
4. Adapt assertions to use new configuration model (data classes instead of DSL actions)
5. Add any missing edge cases:
   - Nybbler with loOutput only
   - Nybbler with hiOutput only
   - Nybbler with both outputs
   - Nybbler with normalizeHi = true/false
   - Single interleaver
   - Multiple interleavers
   - Interleaver with uneven byte count (should throw exception)
   - Combined charset extraction + nybbler

**Rationale**:
- Reusing existing proven test cases reduces test creation effort
- Ensures parity between old and new implementations
- Existing tests document expected behavior and edge cases
- Conversion process validates the new implementation against known good behavior

**Expected Outcome**: Comprehensive test suite mirroring old functionality, ensuring correctness and parity.

### 6. Write Integration Tests
**Action**: Create an end-to-end test that:
1. Loads a real CharPad file
2. Configures a charpad flow step with nybbler outputs
3. Executes the step
4. Verifies that lo/hi output files are created with correct content

**Rationale**: Integration tests provide confidence that the feature works in realistic scenarios, not just unit test mocks.

**Expected Outcome**: Proof that the feature works end-to-end with real CharPad files.

### 7. Update Documentation
**Action**: Document the new nybbler/interleaver configuration in:
- Flow step configuration examples
- Migration guide from old DSL to new flow system
- CharpadOutputs API documentation

**Rationale**: Users need to know how to use the new feature, especially those migrating from the old DSL.

**Expected Outcome**: Clear documentation enabling users to adopt the feature.

### 8. Validate with Real-World Example
**Action**: Find or create a sample project that uses the old charpad DSL with nybbler/interleaver, then migrate it to the new flow step.

**Rationale**: Real-world validation ensures the implementation meets actual user needs and uncovers issues that synthetic tests might miss.

**Expected Outcome**: Confirmation that the feature provides equivalent functionality to the old system.

## Additional Notes

### Architecture Considerations

**Hexagonal Architecture Compliance**: The implementation maintains clear separation:
- **Domain** (flows/src/domain): FilterConfig sealed class enforcing mutual exclusivity
- **Use Case** (CharpadStep.execute): Logic for applying filters
- **Adapter** (flows/adapters/out/charpad): CharpadOutputProducerFactory wraps outputs with filters

**Shared Code Reuse**: The existing filter implementations (Nybbler.kt, BinaryInterleaver.kt) in shared/gradle will be reused directly. **No refactoring of shared/gradle is required** since both old and new systems will maintain separate, parallel implementations.

This approach:
- Reduces risk of breaking existing functionality
- Avoids cross-module dependencies between old DSL and new flow architectures
- Keeps concerns properly isolated

### Backward Compatibility

The old charpad DSL (Charpad.kt task) should continue to work unchanged. This implementation only extends the new flow step. Users can migrate at their own pace.

### Dependencies

**Reusing Existing Implementations**:
- Nybbler.kt (shared/gradle) - depends on BinaryOutput interface
- BinaryInterleaver.kt (shared/gradle) - depends on BinaryOutput and Vavr Seq
- CharpadOutputProducerFactory already has access to these dependencies
- **No performance concerns** - existing implementations are already optimized

**No new dependencies are required.**

The implementation strategy is to reuse these battle-tested, performant implementations directly rather than creating new ones or adding wrapper logic.

### Potential Challenges

1. ✅ **Filter Order Ambiguity**: RESOLVED - Filters are mutually exclusive, no composition needed.

2. ✅ **Performance Optimization**: RESOLVED - Existing domain implementations are already performant. No optimization needed as long as Nybbler.kt and BinaryInterleaver.kt are reused directly.

3. **Output Buffer Management**: FilterAwareExtension.kt line 79 shows buffers are tracked separately. The new system needs equivalent buffer lifecycle management.

4. **Null vs DevNull**: The old system uses DevNull() for null outputs. The new system should decide whether to:
   - Require explicit file paths (simpler, fail-fast)
   - Support null with DevNull fallback (more flexible)
   - Current approach: Optional<String> with null allowed

5. **Gradle Workers API**: The charpad flow step may use parallel execution. Filters must be thread-safe or properly isolated per-worker. (Existing implementations are thread-safe by design - they don't maintain state.)

6. **Interleaver Output Count Validation**: Must validate that number of interleaver outputs divides evenly into byte stream length (BinaryInterleaver throws IllegalInputException otherwise).

### Success Criteria

1. All existing tests (NybblerTest, BinaryInterleaverTest) continue to pass
2. New CharpadStepTest tests pass with nybbler/interleaver configurations
3. Integration test with real CharPad file produces correct filtered outputs
4. Documentation clearly explains the feature
5. Real-world migration example validates feature parity with old DSL

### Timeline Estimate

- Step 1 (Verify filter rules): 1 hour
- Step 2 (Design config model): 2 hours
- Step 3 (Update getAllOutputPaths): 1 hour
- Step 4 (Implement filter wrapping): 4 hours
- Step 5 (Write unit tests): 3 hours
- Step 6 (Write integration tests): 2 hours
- Step 7 (Documentation): 2 hours
- Step 8 (Real-world validation): 2 hours

**Total estimated effort**: ~17 hours

### Related Issues/PRs

- Check if there are open issues about flow step feature parity
- Review recent PRs that added new flow steps for architectural patterns to follow
- Look for user requests or bug reports related to nybbler/interleaver

## Plan Updates

### Update 1: Parallel Architecture Decision (2025-11-08)

**Information Added**: Both old DSL structure and new flow-based structure will be maintained in parallel.

**Changes Made**:
1. Updated Issue Description to clarify this is NOT a migration effort
2. Added scope clarification to Root Cause Hypothesis
3. Marked Question 1 as answered with key implications
4. Updated Architecture Considerations to reflect that no refactoring of shared/gradle is needed
5. Emphasized risk reduction and concern isolation benefits

**Impact on Implementation**:
- Simplifies the architecture by avoiding cross-module dependencies
- Reduces risk of breaking existing functionality
- Allows independent evolution of old DSL and new flow systems
- **No changes required to shared/gradle components**
- Implementation scope remains focused on flows module only

### Update 2: Use Cases and Filter Purpose Clarification (2025-11-08)

**Information Added**: Detailed explanation of what nybbler and interleaver do and their complementary roles in data transformation.

**Changes Made**:
1. Marked Question 2 as answered with specific use case details
2. Enhanced Issue Description with purpose of both filters
3. Added Filter Purpose explanation to Root Cause Hypothesis section
4. Clarified granularity levels: nybbler (4-bit/nibble level) vs interleaver (byte level within chunks)

**Impact on Implementation**:
- Confirms filters are **complementary, not alternatives** - both may be needed in different scenarios
- Validates that filters operate at different data granularity levels
- Helps inform validation logic and test case design
- Confirms both filters are essential for flexible asset processing pipelines

### Update 3: Test Strategy - Copy and Convert Existing Tests (2025-11-08)

**Information Added**: Existing tests in old charpad functionality should be copied and converted to the new flow-based system.

**Changes Made**:
1. Marked Question 3 as answered with specific test strategy
2. Replaced Step 5 (Write Unit Tests) with copy-and-convert approach
3. Updated rationale to emphasize reuse and parity validation
4. Defined conversion process: adapt from DSL to data classes

**Impact on Implementation**:
- **Reduces test creation effort** - leverages existing proven test cases
- **Ensures feature parity** - validates new implementation against known good behavior
- **Improves confidence** - existing tests document expected behavior and edge cases
- **Defines clear test location** - convert to `flows/src/test/kotlin/...`
- **Simplifies validation** - comparison between old and new implementations becomes straightforward
- **First step in Step 5** should now identify and locate existing charpad tests in old DSL

### Update 4: Filter Mutual Exclusivity - Sealed Class Pattern (2025-11-08)

**Information Added**: Nybbler and interleaver cannot be combined - they are mutually exclusive per output.

**Changes Made**:
1. Marked Question 4 as answered with mutual exclusivity constraint
2. Marked Self-Reflection Question 3 as answered (filter ordering not applicable)
3. Updated "What needs to happen" in Root Cause Hypothesis to enforce mutual exclusivity instead of composition
4. Redesigned Step 1 from "Verify Filter Composition Rules" to "Enforce Filter Mutual Exclusivity"
5. Completely redesigned Step 2 configuration model using sealed class hierarchy instead of separate fields
6. Updated Step 3 getAllOutputPaths() to use when expression on sealed class
7. Updated Step 4 filter wrapping implementation to use sealed class exhaustive when
8. Updated Architecture Considerations to reflect FilterConfig sealed class
9. Marked Challenge #1 as resolved
10. Added new Challenge #5 for interleaver output count validation

**Code Pattern - Sealed Class Approach**:
```kotlin
sealed class FilterConfig {
    data class Nybbler(...) : FilterConfig()
    data class Interleaver(...) : FilterConfig()
    object None : FilterConfig()
}
```

**Impact on Implementation**:
- **Type-safe mutual exclusivity** - enforced at compile time via sealed class
- **Simplified when expressions** - exhaustive checking by compiler
- **Cleaner than boolean flags** - prevents invalid states at the type level
- **Better error messages** - type mismatch instead of runtime validation
- **Reduced test complexity** - no tests needed for invalid combinations
- **Eliminates configuration challenge #1** - no need to decide filter composition order

### Key Insight:
Filters are **complementary at different granularity levels** (confirmed in Update 2) but **mutually exclusive per output** (confirmed in Update 4). The sealed class pattern elegantly captures this constraint.

### Update 5: Performance - Existing Implementations Are Optimal (2025-11-08)

**Information Added**: Existing domain implementations (Nybbler.kt, BinaryInterleaver.kt) are already performant - no additional optimization needed.

**Changes Made**:
1. Marked Question 5 as answered with performance assessment
2. Updated Dependencies section to emphasize reuse of battle-tested implementations
3. Marked Challenge #2 as resolved (Performance Optimization)
4. Clarified that existing implementations are thread-safe by design
5. Renumbered remaining challenges

**Code Pattern - Direct Reuse**:
```kotlin
// Simply reuse existing implementations:
val nybbler = Nybbler(lo, hi, normalizeHi)
val interleaver = BinaryInterleaver(List.ofAll(outputs))
```

**Impact on Implementation**:
- **No custom buffering strategies needed** - existing implementations handle it optimally
- **No performance testing required** - can rely on existing test coverage
- **Simplified adapter implementation** - just wrap and delegate
- **Focus on integration, not optimization** - reduces implementation scope
- **Guaranteed performance parity** - with old DSL system since using same implementations

**Summary of All Questions Answered**:
1. ✅ Architecture: Keep old structure in parallel
2. ✅ Use Cases: Complementary filters at different granularity levels
3. ✅ Testing: Copy and convert existing tests
4. ✅ Filter Combinations: Mutually exclusive per output
5. ✅ Performance: Existing implementations already optimal

**Ready for Implementation** - All major questions answered, design patterns established, implementation approach clear.
