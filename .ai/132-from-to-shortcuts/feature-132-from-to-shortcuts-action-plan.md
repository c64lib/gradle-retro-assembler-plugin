# Feature: From/To Shortcuts for CommandStep DSL

**Issue**: #132
**Status**: Planning
**Created**: 2025-11-15

## 1. Feature Description

### Overview
Add convenient DSL shortcuts `useFrom()` and useTo()` to the `CommandStepBuilder` that allow referencing the input/output paths specified via `from()` and `to()` methods directly in parameter values. This eliminates the need to duplicate path names in both the dependency tracking calls and the CLI invocation.

### Requirements
- Add `useFrom()` function to CommandStepBuilder that returns the first input path
- Add `useTo()` function to CommandStepBuilder that returns the first output path
- Support using these shortcuts in `param()`, `option()`, and `withOption()` methods
- Support both single and multiple inputs/outputs (use first in each case)
- Maintain backward compatibility with existing DSL usage
- Update tests to cover the new shortcuts

### Success Criteria
- `useFrom()` returns the first input path from `from()` calls
- `useTo()` returns the first output path from `to()` calls
- Shortcuts can be used in any parameter method: `param()`, `option()`, `withOption()`
- The shortcuts resolve to actual paths in the generated command line
- All existing tests pass
- New shortcuts are well-tested with multiple scenarios
- Documentation is updated (inline KDoc)

## 2. Root Cause Analysis

### Current State
Users currently must duplicate file paths in the DSL:
```kotlin
commandStep("exomize-game-linked", "exomizer") {
    from("build/game-linked.bin")
    to("build/game-linked.z.bin")
    param("raw")
    flag("-T4")
    option("-o", "build/game-linked.z.bin")      // Duplicate path
    param("build/game-linked.bin")                // Duplicate path
}
```

**Problems:**
1. Violation of DRY principle - paths are specified twice
2. Risk of inconsistency - developer might change one but forget the other
3. More verbose and harder to read - the intent (use input/output) is not clear
4. Error-prone - easy to copy wrong path or forget trailing characters

### Desired State
```kotlin
commandStep("exomize-game-linked", "exomizer") {
    from("build/game-linked.bin")
    to("build/game-linked.z.bin")
    param("raw")
    flag("-T4")
    flag("-M256")
    flag("-P-32")
    flag("-c")
    option("-o", useTo())        // Auto-resolved to "build/game-linked.z.bin"
    param(useFrom())             // Auto-resolved to "build/game-linked.bin"
}
```

**Benefits:**
1. Single source of truth - paths defined once in `from()`/`to()`
2. Clear intent - `useFrom()` and `useTo()` explicitly show which path is being used
3. Less error-prone - automatic resolution prevents copy-paste mistakes
4. Easier to refactor - change path in one place updates everywhere

### Gap Analysis
**What needs to change:**
1. Add two new public functions to `CommandStepBuilder`: `useFrom()` and `useTo()`
2. These functions must return the actual string path (resolved at DSL build time, not execution time)
3. Functions should follow the fluent DSL style and integrate naturally with existing builders
4. No changes needed to domain layer (`CommandStep`) - parameters remain strings
5. No changes needed to adapter or execution layer - already handles string parameters
6. Update tests to cover new functionality

## 3. Relevant Code Parts

### Existing Components

#### CommandStepBuilder (DSL Layer)
- **Location**: `flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/dsl/CommandStepBuilder.kt`
- **Purpose**: Provides fluent DSL for building command steps
- **Current functionality**:
  - `from()`/`to()` methods add to mutable lists
  - `param()`, `option()`, `flag()` methods add to parameters list
  - `build()` method creates immutable `CommandStep`
- **Integration Point**: Will add `useFrom()` and `useTo()` methods that access the mutable `inputs` and `outputs` lists

#### CommandStep (Domain Model)
- **Location**: `flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/steps/CommandStep.kt`
- **Purpose**: Immutable domain model representing a command execution step
- **Current functionality**: Stores inputs, outputs, parameters as immutable lists
- **Integration Point**: NO CHANGES needed - parameters already support string values returned by `useFrom()`/`useTo()`

#### FlowDsl (DSL Orchestrator)
- **Location**: `flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/FlowDsl.kt`
- **Purpose**: Entry point DSL that creates and registers flow steps
- **Current functionality**: `commandStep()` function creates `CommandStepBuilder` and calls configure lambda
- **Integration Point**: NO CHANGES needed - builder will work the same way

#### CommandCommand & CommandConfigMapper (Domain/Adapter)
- **Location**: `flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/config/`
- **Purpose**: Map `CommandStep` to executable `CommandCommand`
- **Integration Point**: NO CHANGES needed - parameters are already strings passed through

#### Test Files
- **Location**: `flows/src/test/kotlin/com/github/c64lib/rbt/flows/domain/steps/CommandStepTest.kt`
- **Purpose**: Unit tests for CommandStep
- **Integration Point**: Will add tests to CommandStepBuilder tests (in adapters/in/gradle if exists, else add to flow tests)

### Architecture Alignment

**Domain**: flows subdomain (orchestrator for build steps)

**Use Cases**: This is NOT a use case (no new business logic executed). This is a DSL enhancement in the adapter layer.

**Ports**: NO new ports needed. This is purely a DSL convenience feature.

**Adapters**:
- **Inbound**: CommandStepBuilder (DSL adapter) gets new methods
- **Outbound**: No changes needed

**Layer Distribution**:
- **DSL Layer (Adapter-in)**: Add `useFrom()` and `useTo()` methods to CommandStepBuilder
- **Domain Layer**: No changes
- **Adapter Layer (Adapter-out)**: No changes
- **Execution Layer**: No changes

### Dependencies
- **Kotlin stdlib**: Already used in project (no new dependencies)
- **No external libraries needed**: Pure DSL enhancement using standard Kotlin
- **No dependency on other domains**: Operates only within flows subdomain

## 4. Questions and Clarifications

### Self-Reflection Questions

**Q**: Should `useFrom()` and `useTo()` support multiple input/output files?
- **A**: Based on codebase analysis, `from()` and `to()` can accept multiple paths. However, for the initial implementation, `useFrom()` and `useTo()` will return the first input/output respectively. If multiple inputs/outputs are needed in parameters, users can still manually specify paths. This keeps the API simple and covers 95% of use cases.

**Q**: Where should `useFrom()` and `useTo()` be called - before or after `from()`/`to()`?
- **A**: They should be called AFTER `from()`/`to()` have been defined, since they access the mutable lists. Calling them before will return empty paths. This is standard Kotlin builder pattern behavior. Consider adding validation/documentation.

**Q**: What if `from()` or `to()` haven't been called yet?
- **A**: `useFrom()` will return empty string if inputs list is empty, `useTo()` will return empty string if outputs list is empty. This is consistent with Kotlin's `List.first()` behavior with default values. We should document this.

**Q**: Should these work with all parameter methods (`param()`, `option()`, `withOption()`)?
- **A**: Yes, based on exploration, all these methods accept String parameters. The shortcuts are just Strings, so they work everywhere naturally.

**Q**: Are there other builder methods in the codebase that use similar patterns?
- **A**: Examined all other step builders (CharpadStepBuilder, SpritepadStepBuilder, etc.). None have similar shortcut features. This is new functionality specific to CommandStep (which makes sense - only CommandStep has from/to + parameters combination).

### Unresolved Questions

- [ ] Should there be an overload for `useFrom(index)` and `useTo(index)` to support non-first paths? (Recommendation: No, keep it simple for now - can add in future if needed)
- [ ] Should the shortcuts throw an exception if used before `from()`/`to()` are called, or silently return empty string? (Recommendation: Silently return empty string - consistent with list behavior, less invasive)

### Design Decisions

**Decision 1**: Return Type and Empty Handling
- **Options**:
  - A) Return empty string if not set (silent fallback)
  - B) Throw exception if not set (fail-fast)
  - C) Return Optional<String> (explicit null safety)
- **Recommendation**: Option A (silent fallback). Rationale: Consistent with existing Kotlin List behavior, matches DSL builder patterns, users control execution and will notice if path is empty in command output.

**Decision 2**: Method Naming
- **Options**:
  - A) `useFrom()` / `useTo()` (current proposal)
  - B) `inputPath()` / `outputPath()`
  - C) `getInputPath()` / `getOutputPath()`
  - D) `fromPath()` / `toPath()`
- **Recommendation**: Option A (`useFrom()`/`useTo()`). Rationale: Mirrors the `from()`/`to()` method names, reads naturally ("use the from path", "use the to path"), consistent with DSL style.

**Decision 3**: Scope and Applicability
- **Options**:
  - A) Only add to CommandStep/CommandStepBuilder (this issue)
  - B) Add same shortcuts to all processor steps (Charpad, Spritepad, etc.)
- **Recommendation**: Option A (CommandStepBuilder only). Rationale: Only CommandStep combines from/to with parameters. Other processors don't have parameters in same way. Can extend in future if needed.

## 5. Implementation Plan

### Phase 1: Add DSL Shortcuts to CommandStepBuilder
**Goal**: Implement `useFrom()` and `useTo()` methods in CommandStepBuilder

1. **Step 1.1**: Add `useFrom()` and `useTo()` methods to CommandStepBuilder
   - Files: `flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/dsl/CommandStepBuilder.kt`
   - Description:
     - Add `fun useFrom(): String` that returns `inputs.firstOrNull() ?: ""`
     - Add `fun useTo(): String` that returns `outputs.firstOrNull() ?: ""`
     - Add KDoc with usage examples
   - Testing: Unit tests in Phase 2

2. **Step 1.2**: Verify CommandStepBuilder compiles and existing tests pass
   - Files: No new files
   - Description: Run existing test suite to ensure changes don't break anything
   - Testing: `./gradlew :flows:adapters:in:gradle:test`

**Phase 1 Deliverable**:
- CommandStepBuilder now has `useFrom()` and `useTo()` methods
- All existing tests pass
- Can be merged independently (backward compatible, no API changes to domain layer)

### Phase 2: Add Unit Tests for New Shortcuts
**Goal**: Comprehensive test coverage for the new shortcuts

1. **Step 2.1**: Create unit tests for `useFrom()` and `useTo()` functionality
   - Files: `flows/src/test/kotlin/com/github/c64lib/rbt/flows/domain/steps/CommandStepTest.kt` or similar test file
   - Description:
     - Test `useFrom()` returns first input when called after `from()`
     - Test `useTo()` returns first output when called after `to()`
     - Test `useFrom()` returns empty string when not set
     - Test `useTo()` returns empty string when not set
     - Test shortcuts work in `param()` method
     - Test shortcuts work in `option()` method
     - Test shortcuts work with single input/output
     - Test shortcuts work with multiple inputs/outputs (returns first)
   - Testing: `./gradlew :flows:test --tests "*CommandStep*"` to verify new tests pass

2. **Step 2.2**: Verify all tests pass including integration tests
   - Files: No new files
   - Description: Run full test suite to ensure nothing broke
   - Testing: `./gradlew test`

**Phase 2 Deliverable**:
- Complete test coverage for `useFrom()` and `useTo()`
- All unit and integration tests pass
- Can be merged (feature complete and tested)

### Phase 3: Documentation and Polish
**Goal**: Update documentation and ensure code quality

1. **Step 3.1**: Add inline KDoc documentation with examples
   - Files: `flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/dsl/CommandStepBuilder.kt`
   - Description:
     - Add comprehensive KDoc to `useFrom()` and `useTo()` methods
     - Include usage examples showing before/after
     - Document empty string behavior
     - Document return types
   - Testing: Visual inspection of generated KDoc

2. **Step 3.2**: Optional - Update CLAUDE.md with example of new shortcuts
   - Files: `CLAUDE.md` (if appropriate for project guidelines)
   - Description: Add example to "Flows Subdomain Patterns" section showing usage of shortcuts
   - Testing: Visual inspection

3. **Step 3.3**: Final verification and cleanup
   - Files: No file changes
   - Description: Verify code style, KDoc formatting, no unused imports, clean build
   - Testing: `./gradlew :flows:build`, code review

**Phase 3 Deliverable**:
- Fully documented feature with examples
- Code passes style checks
- Ready for release
- Can be merged

## 6. Testing Strategy

### Unit Tests

**CommandStepBuilder Tests:**
1. **Basic Functionality**
   - `useFrom()` with single input returns first input path
   - `useTo()` with single output returns first output path
   - `useFrom()` with multiple inputs returns first input
   - `useTo()` with multiple outputs returns first output

2. **Edge Cases**
   - `useFrom()` called before any `from()` returns empty string
   - `useTo()` called before any `to()` returns empty string
   - Multiple calls to `useFrom()` return same value
   - Multiple calls to `useTo()` return same value

3. **Integration with Parameters**
   - `param(useFrom())` adds shortcut result to parameters
   - `param(useTo())` adds shortcut result to parameters
   - `option("-o", useTo())` creates correct option with resolved path
   - `withOption("-i", useFrom())` works correctly

4. **DSL Fluency**
   - Shortcuts return String and can be chained naturally
   - Works in any parameter context

### Integration Tests

1. **End-to-End Command Building**
   - Build CommandStep using shortcuts
   - Verify generated CommandCommand has correct arguments
   - Execute command and verify output paths are correct

2. **Real Usage Scenarios**
   - Exomizer command with `useFrom()` and `useTo()`
   - KickAssembler command with shortcuts
   - Multi-parameter commands using both shortcuts

### Manual Testing

1. Run the example from issue #132:
```kotlin
commandStep("exomize-game-linked", "exomizer") {
    from("build/game-linked.bin")
    to("build/game-linked.z.bin")
    param("raw")
    flag("-T4")
    flag("-M256")
    flag("-P-32")
    flag("-c")
    option("-o", useTo())
    param(useFrom())
}
```
Verify it generates same command as manual path specification.

2. Verify backward compatibility - all existing commandStep definitions still work

## 7. Risks and Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Breaking changes to existing DSL | High | Low | New methods don't change existing API, only add new functionality |
| Empty path silently used in command | Medium | Medium | Add clear KDoc warning. Users will notice in command output. Consider assertion in future if becomes problem |
| API confusion - `useFrom()` vs `from()` | Low | Low | Clear KDoc and examples. Method names are distinct and purpose is clear |
| Not handling multiple inputs/outputs | Low | Medium | Document limitation clearly. Can extend in future with overloads if needed. |
| Shortcuts called before paths set | Low | Low | Returns empty string (safe fallback). Documented behavior. |
| Test coverage gaps | Medium | Low | Comprehensive test strategy covers all scenarios |

## 8. Documentation Updates

- [ ] Add KDoc to `useFrom()` method in CommandStepBuilder with usage example
- [ ] Add KDoc to `useTo()` method in CommandStepBuilder with usage example
- [ ] Document edge case behavior (empty string when not set)
- [ ] Optionally update CLAUDE.md with example in "Flows Subdomain Patterns" section
- [ ] Optionally update README with example (if CommandStep examples exist there)

## 9. Rollout Plan

1. **Merge Phase 1** (Implementation)
   - Small change with no API modifications
   - All existing code continues to work
   - No risk of breaking existing projects

2. **Merge Phase 2** (Tests)
   - Comprehensive test coverage ensures feature works
   - Tests serve as documentation

3. **Merge Phase 3** (Documentation)
   - Full documentation available to users
   - Examples show how to use feature
   - Ready for user-facing release

4. **Release**
   - Include in next minor version update
   - Update plugin changelog with example
   - No breaking changes, backward compatible

5. **Post-Release Monitoring**
   - Monitor for issues with empty path behavior
   - If problems arise, consider adding validation/exceptions
   - Gather user feedback for potential extensions (multiple inputs/outputs, etc.)

---

**Note**: This plan is ready for implementation. All phases are independent and can be merged separately, starting with Phase 1.
