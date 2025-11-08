# Action Plan for Issue #117: Nybbler-Interleaver

## Issue Description

The old charpad preprocessor (Gradle DSL-based) supports nybbler and interleaver filters to be applied onto each output. These capabilities need to be extended to the new charpad flow step to maintain feature parity with the existing system.

**Important**: Both the old DSL structure and new flow-based structure will be maintained in parallel. This is NOT a migration effort; the goal is to extend the new flow step with equivalent capabilities while keeping the old system functional.

**Nybbler**: Splits bytes into low and high nibbles (4-bit halves), writing them to separate output files. Supports optional normalization of high nibbles.

**Interleaver**: Distributes binary data across multiple output streams in round-robin fashion, splitting the byte stream evenly.

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
3. Ensuring the filters are applied in the correct order

**Scope Clarification**: The old DSL will continue to exist and function unchanged. This is a parallel implementation to extend the new flow-based system, not a replacement or migration.

## Investigation Questions

### Self-Reflection Questions

1. **Configuration Design**: Should nybbler/interleaver be configured per-output (e.g., each CharsetOutput can have its own nybbler), or globally for all charpad outputs?
   - **Analysis**: The old system uses per-output configuration (StartEndExtension for charset/map). This provides maximum flexibility. Recommendation: Per-output configuration.

2. **API Compatibility**: Should the new flow step's filter API exactly mirror the old DSL, or should it be redesigned to fit the flow-based paradigm?
   - **Analysis**: The flow system uses Kotlin data classes rather than Gradle DSL actions. The concepts (loOutput, hiOutput, normalizeHi) should remain the same, but the syntax will differ.

3. **Filter Ordering**: In what order should filters be applied? Should nybbler come before or after interleaver?
   - **Analysis**: Looking at FilterAwareExtension.kt lines 74-110, the logic suggests only one filter type is applied per output. Need to verify if both can be combined.

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

2. **Use Cases**: What are the primary use cases for nybbler and interleaver in Commodore 64 development?
   - **Why important**: Helps validate the design and may reveal edge cases or additional requirements.

3. **Testing Requirements**: Are there existing CharPad projects with nybbler/interleaver configurations that can be used for integration testing?
   - **Why important**: Real-world test cases are more valuable than synthetic ones for validating the implementation.

4. **Filter Combinations**: Can nybbler and interleaver be applied simultaneously to the same output, or are they mutually exclusive?
   - **Why important**: Affects the configuration model and validation logic.

5. **Performance**: Are there performance concerns with filter usage? Should the new implementation optimize for specific scenarios?
   - **Why important**: May influence implementation details (e.g., buffering strategies).

## Next Steps

### 1. Verify Filter Composition Rules
**Action**: Analyze FilterAwareExtension.kt lines 74-110 to determine if nybbler and interleaver can be combined, and in what order.

**Rationale**: The configuration model depends on whether filters are mutually exclusive or composable. The code shows `hasNybbler`, `hasInterleavers`, and `hasMainOutput` as separate branches in a when expression, suggesting they're mutually exclusive per output.

**Expected Outcome**: Documentation of filter composition rules to guide the new API design.

### 2. Design Configuration Model
**Action**: Create data classes for filter configuration in the flows domain:
```kotlin
// In flows/src/main/kotlin/.../domain/config/CharpadOutputs.kt

data class NybblerConfig(
    val loOutput: String? = null,
    val hiOutput: String? = null,
    val normalizeHi: Boolean = true
)

data class InterleaverConfig(
    val output: String
)

// Extend RangeOutput implementations
data class CharsetOutput(
    override val output: String,
    override val start: Int = 0,
    override val end: Int = 65536,
    val nybbler: NybblerConfig? = null,
    val interleavers: List<InterleaverConfig> = emptyList()
) : RangeOutput
```

**Rationale**: This maintains the domain-driven design of the flow architecture while providing equivalent functionality to the old DSL. Using data classes is idiomatic for the flows module.

**Expected Outcome**: Type-safe configuration model that can be validated at compile time.

### 3. Update getAllOutputPaths()
**Action**: Modify CharpadOutputs.kt to include nybbler and interleaver output paths in getAllOutputPaths():
```kotlin
fun getAllOutputPaths(): List<String> {
    val primaryOutputs = charsets.map { it.output } +
                        charAttributes.map { it.output } + ...
    val nybblerOutputs = charsets.flatMap { listOfNotNull(it.nybbler?.loOutput, it.nybbler?.hiOutput) } + ...
    val interleaverOutputs = charsets.flatMap { it.interleavers.map { i -> i.output } } + ...
    return primaryOutputs + nybblerOutputs + interleaverOutputs
}
```

**Rationale**: Flow dependency tracking requires knowing all output files. Filters create additional outputs that must be registered.

**Expected Outcome**: Correct dependency graph for flow execution ordering.

### 4. Implement Filter Wrapping in CharpadOutputProducerFactory
**Action**: Modify CharpadOutputProducerFactory.kt to wrap binary outputs with filters:
```kotlin
private fun createBinaryOutput(outputFile: File, filterConfig: OutputFilterConfig): Output<ByteArray> {
    val baseOutput = FileBinaryOutput(outputFile)
    return when {
        filterConfig.hasNybbler -> {
            val lo = filterConfig.nybbler?.loOutput?.let { FileBinaryOutput(File(it)) }
            val hi = filterConfig.nybbler?.hiOutput?.let { FileBinaryOutput(File(it)) }
            Nybbler(lo, hi, filterConfig.nybbler?.normalizeHi ?: true)
        }
        filterConfig.hasInterleavers -> {
            val outputs = filterConfig.interleavers.map { FileBinaryOutput(File(it.output)) }
            BinaryInterleaver(List.ofAll(outputs))
        }
        else -> baseOutput
    }
}
```

**Rationale**: This mirrors the logic in FilterAwareExtension.resolveOutput() but adapted for the new architecture. The existing filter implementations can be reused directly.

**Expected Outcome**: Functional nybbler and interleaver support in the charpad flow step.

### 5. Write Unit Tests
**Action**: Create CharpadStepTest.kt in flows/src/test/kotlin/ with test cases for:
- Nybbler with loOutput only
- Nybbler with hiOutput only
- Nybbler with both outputs
- Nybbler with normalizeHi = true/false
- Single interleaver
- Multiple interleavers
- Interleaver with uneven byte count (should throw exception)
- Combined charset extraction + nybbler
- Edge case: null outputs should use DevNull

**Rationale**: The existing NybblerTest.kt and BinaryInterleaverTest.kt test the filters in isolation. New tests are needed to verify integration with the charpad flow step.

**Expected Outcome**: Comprehensive test coverage ensuring the feature works correctly and handles edge cases.

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

**Hexagonal Architecture Compliance**: The implementation should maintain clear separation:
- **Domain** (flows/src/domain): NybblerConfig, InterleaverConfig data classes
- **Use Case** (CharpadStep.execute): Logic for applying filters
- **Adapter** (flows/adapters/out/charpad): CharpadOutputProducerFactory wraps outputs

**Shared Code Reuse**: The existing filter implementations (Nybbler.kt, BinaryInterleaver.kt) in shared/gradle will be reused directly. **No refactoring of shared/gradle is required** since both old and new systems will maintain separate, parallel implementations.

This approach:
- Reduces risk of breaking existing functionality
- Avoids cross-module dependencies between old DSL and new flow architectures
- Keeps concerns properly isolated

### Backward Compatibility

The old charpad DSL (Charpad.kt task) should continue to work unchanged. This implementation only extends the new flow step. Users can migrate at their own pace.

### Dependencies

- Nybbler.kt depends on BinaryOutput interface
- BinaryInterleaver.kt depends on BinaryOutput and Vavr Seq
- CharpadOutputProducerFactory already has access to these dependencies

No new dependencies are required.

### Potential Challenges

1. **Filter Order Ambiguity**: If both nybbler and interleavers are configured, which is applied first? The old system suggests they're mutually exclusive per output (based on FilterAwareExtension when branches), but this needs verification.

2. **Output Buffer Management**: FilterAwareExtension.kt line 79 shows buffers are tracked separately. The new system needs equivalent buffer lifecycle management.

3. **Null vs DevNull**: The old system uses DevNull() for null outputs. The new system should decide whether to:
   - Require explicit file paths (simpler, fail-fast)
   - Support null with DevNull fallback (more flexible)

4. **Gradle Workers API**: The charpad flow step may use parallel execution. Filters must be thread-safe or properly isolated per-worker.

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
