# Architecture Quality Check & Corrections - Action Plan
**Issue:** 193
**Task:** arch-check
**Date:** 2025-11-16

## Executive Summary

Performed comprehensive architecture quality check on 8 commits on develop branch since d9ed2abc79d55fe694e51f92d5bed4b05b684e53. Found 2 MEDIUM severity violations related to data class + mutable port field pattern in ExomizerStep and DasmStep. This action plan addresses all violations with specific implementation steps.

## Violations Found

### Critical Violation 1: Data Class + Mutable Port Field (ExomizerStep)
- **File:** `flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/steps/ExomizerStep.kt`
- **Commit:** c8c2de1
- **Severity:** MEDIUM
- **Issue:** ExomizerStep declared as `data class` but contains mutable `exomizerPort` field
  - Violates Kotlin data class immutability contract
  - Auto-generated equals/hashCode includes all constructor params
  - Mutable port field breaks equality semantics
  - Two instances with different ports would be unequal even if other properties match

### Critical Violation 2: Data Class + Mutable Port Field (DasmStep)
- **File:** `flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/steps/DasmStep.kt`
- **Commit:** 6215b0e
- **Severity:** MEDIUM
- **Issue:** DasmStep declared as `data class` but contains mutable `dasmPort` field
  - Same issues as ExomizerStep
  - Inconsistent with refactoring done in commit 3f692ea

### Secondary Violation: Setter Injection Pattern
- **Files:** ExomizerStep, DasmStep
- **Severity:** MEDIUM
- **Issue:** Port injection via setter methods instead of constructor injection
  - Current pattern requires post-construction initialization
  - CLAUDE.md recommends constructor injection as long-term approach
  - Creates potential synchronization issues

### Minor Issues (Quality)
- **Missing integration tests** for Exomizer and Dasm flows
- **File validation in domain use case** (low severity, mitigated by flows adapter)

## Architecture Analysis Summary

### ✅ What Was Done Right
- New modules (exomizer crunchers, dasm compiler) follow hexagonal architecture correctly
- Proper separation: domain layer → adapters → infrastructure
- Port interfaces properly abstract technology concerns (ExecuteExomizerPort, DasmAssemblePort)
- New modules correctly added to infra/gradle as compileOnly dependencies
- Flows domain integration properly structured with adapters
- Settings.gradle.kts updated correctly

### ❌ What Needs Fixing
- ExomizerStep and DasmStep use incorrect data class + mutable field pattern
- Setter injection pattern creates design inconsistency
- Missing integration tests for end-to-end flow validation

## Correction Implementation Plan

### Phase 1: Fix Data Class Violations

#### Task 1.1: Convert ExomizerStep to Regular Class
**Objective:** Remove data class keyword while maintaining all functionality

**Steps:**
1. Open `flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/steps/ExomizerStep.kt`
2. Change `data class ExomizerStep(` to `class ExomizerStep(`
3. Verify all properties remain as constructor parameters
4. Verify mutable port field and setter remain unchanged
5. Ensure toString() implementation is preserved (may need manual override if needed)
6. Run tests to verify no behavioral changes

**Expected Changes:**
```kotlin
// Before
data class ExomizerStep(
    override val name: String,
    override val inputs: List<String> = emptyList(),
    override val outputs: List<String> = emptyList(),
    val mode: String = "raw",
    val loadAddress: String = "auto",
    val forward: Boolean = false,
    private var exomizerPort: ExomizerPort? = null
) : FlowStep(name, "exomizer", inputs, outputs) { ... }

// After
class ExomizerStep(
    override val name: String,
    override val inputs: List<String> = emptyList(),
    override val outputs: List<String> = emptyList(),
    val mode: String = "raw",
    val loadAddress: String = "auto",
    val forward: Boolean = false,
    private var exomizerPort: ExomizerPort? = null
) : FlowStep(name, "exomizer", inputs, outputs) { ... }
```

**Validation:**
- No compilation errors
- Port setter still works
- FlowTasksGenerator can still inject ports
- All ExomizerStep tests pass

#### Task 1.2: Convert DasmStep to Regular Class
**Objective:** Remove data class keyword while maintaining all functionality

**Steps:**
1. Open `flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/steps/DasmStep.kt`
2. Change `data class DasmStep(` to `class DasmStep(`
3. Verify all properties remain as constructor parameters
4. Verify mutable port field and setter remain unchanged
5. Ensure toString() implementation is preserved
6. Run tests to verify no behavioral changes

**Expected Changes:**
```kotlin
// Before
data class DasmStep(
    override val name: String,
    override val inputs: List<String> = emptyList(),
    override val outputs: List<String> = emptyList(),
    val dasmAssemblyConfig: DasmAssemblyConfig = DasmAssemblyConfig(),
    private var dasmPort: DasmAssemblyPort? = null
) : FlowStep(name, "dasm", inputs, outputs) { ... }

// After
class DasmStep(
    override val name: String,
    override val inputs: List<String> = emptyList(),
    override val outputs: List<String> = emptyList(),
    val dasmAssemblyConfig: DasmAssemblyConfig = DasmAssemblyConfig(),
    private var dasmPort: DasmAssemblyPort? = null
) : FlowStep(name, "dasm", inputs, outputs) { ... }
```

**Validation:**
- No compilation errors
- Port setter still works
- FlowTasksGenerator can still inject ports
- All DasmStep tests pass

#### Task 1.3: Verify Port Injection Compatibility
**Objective:** Ensure task adapters continue to work correctly after changes

**Steps:**
1. Review `flows/adapters/in/gradle/tasks/ExomizerTask.kt`
   - Verify it still calls `setExomizerPort()` correctly
   - Verify step initialization still works
2. Review `flows/adapters/in/gradle/tasks/DasmTask.kt`
   - Verify it still calls `setDasmPort()` correctly
   - Verify step initialization still works
3. Check FlowTasksGenerator for any port injection logic
4. Run full gradle build to ensure no adapter issues

**Expected Result:**
- Task adapters continue to function unchanged
- Port injection works via setter methods
- No breaking changes to existing APIs

### Phase 2: Update Documentation

#### Task 2.1: Update CLAUDE.md
**Objective:** Clarify the step class pattern for future implementations

**File:** `CLAUDE.md`

**Changes Required:**

In the "Flows Subdomain Patterns" section under "Step Classes (Domain Layer)", add clarification:

```markdown
### Port Injection in Step Classes

Step classes with mutable port fields **must NOT be declared as data classes**. While Kotlin's `data class` keyword is useful for immutable value objects, it auto-generates `equals()` and `hashCode()` methods based on ALL constructor parameters. Since ports are mutable and injected post-construction, including them in the data class would create equality comparison bugs.

**Correct Pattern - Regular Class with Mutable Port:**
```kotlin
class ExomizerStep(
    override val name: String,
    override val inputs: List<String> = emptyList(),
    override val outputs: List<String> = emptyList(),
    val mode: String = "raw",
    val loadAddress: String = "auto",
    val forward: Boolean = false,
    private var exomizerPort: ExomizerPort? = null
) : FlowStep(name, "exomizer", inputs, outputs) {
    fun setExomizerPort(port: ExomizerPort) {
        this.exomizerPort = port
    }
    // ... rest of implementation
}
```

**Future Improvement:** Constructor-based port injection is the preferred long-term approach, but requires changes to task adapter infrastructure to support constructor parameters. This refactoring is tracked separately.
```

**Location:** Insert after "Step Classes (Domain Layer)" heading and before "Common Patterns" section

**Validation:**
- Documentation clearly explains the pattern
- Existing examples in CLAUDE.md are reviewed for consistency
- No contradictions with other documented patterns

### Phase 3: Add Integration Tests (Quality Improvement)

#### Task 3.1: Add ExomizerStep Integration Test
**Objective:** Verify Exomizer flows work end-to-end from domain through adapters

**File:** `flows/adapters/in/gradle/src/test/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/steps/ExomizerStepIntegrationTest.kt`

**Test Scope:**
- Create test ExomizerStep with valid configuration
- Set port via setter injection
- Call execute() and verify port method is called
- Verify error handling when port is not set
- Verify file path resolution works correctly

**Expected Test Cases:**
1. `testExomizerStepExecutionWithValidPort` - Happy path execution
2. `testExomizerStepExecutionWithoutPort` - Validates port is required
3. `testExomizerStepInputOutputResolution` - Verifies file paths are resolved correctly
4. `testExomizerStepValidation` - Validates configuration rules

#### Task 3.2: Add DasmStep Integration Test
**Objective:** Verify Dasm flows work end-to-end from domain through adapters

**File:** `flows/adapters/in/gradle/src/test/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/steps/DasmStepIntegrationTest.kt`

**Test Scope:**
- Create test DasmStep with valid configuration
- Set port via setter injection
- Call execute() and verify port method is called
- Verify error handling when port is not set
- Verify file path resolution works correctly

**Expected Test Cases:**
1. `testDasmStepExecutionWithValidPort` - Happy path execution
2. `testDasmStepExecutionWithoutPort` - Validates port is required
3. `testDasmStepInputOutputResolution` - Verifies file paths are resolved correctly
4. `testDasmStepValidation` - Validates configuration rules

### Phase 4: Validation & Testing

#### Task 4.1: Run Full Build
**Command:** `./gradlew clean build`

**Expected Results:**
- ✅ No compilation errors
- ✅ All existing tests pass
- ✅ New integration tests pass
- ✅ Code quality checks pass (detekt)

#### Task 4.2: Run Specific Test Modules
**Commands:**
```bash
./gradlew :flows:test                          # All flows tests
./gradlew :flows:adapters:in:gradle:test       # Flows gradle adapters tests
./gradlew :crunchers:exomizer:test             # Exomizer tests
./gradlew :compilers:dasm:test                 # Dasm tests
```

**Expected Results:**
- ✅ All tests pass
- ✅ Code coverage maintained or improved

#### Task 4.3: Verify Architecture Compliance
**Methods:**
- Visual inspection of converted classes
- Review commit diff to ensure only `data class` → `class` change
- Verify no behavioral changes introduced
- Confirm pattern consistency with other step classes

## Implementation Order

1. **Task 1.1** - Convert ExomizerStep (5 min)
2. **Task 1.2** - Convert DasmStep (5 min)
3. **Task 1.3** - Verify port injection compatibility (10 min)
4. **Task 2.1** - Update CLAUDE.md (15 min)
5. **Task 3.1** - Add ExomizerStep integration test (30 min)
6. **Task 3.2** - Add DasmStep integration test (30 min)
7. **Task 4.1** - Run full build (5-10 min)
8. **Task 4.2** - Run specific test modules (5-10 min)
9. **Task 4.3** - Verify architecture compliance (10 min)

**Total Estimated Time:** 2-2.5 hours

## Success Criteria

- ✅ ExomizerStep converted from data class to regular class
- ✅ DasmStep converted from data class to regular class
- ✅ All existing tests pass without modification
- ✅ Port injection continues to work correctly
- ✅ CLAUDE.md clarifies step class pattern
- ✅ Integration tests added for both steps
- ✅ Full build passes with no errors
- ✅ Code coverage maintained or improved
- ✅ No architecture violations detected
- ✅ Detekt checks pass

## Risk Assessment

**Low Risk Implementation:**
- Changes are surgical (removing `data class` keyword only)
- No behavioral changes expected
- Existing tests validate backward compatibility
- Port injection mechanism unchanged
- Task adapters unchanged

**Mitigations:**
- Run full test suite before commit
- Code review for manual verification
- No changes to public APIs or ports
- Rollback plan: revert to `data class` if issues arise

## Commit Strategy

**Single commit covering:**
- Convert ExomizerStep to regular class
- Convert DasmStep to regular class
- Update CLAUDE.md documentation
- Add integration tests for both steps

**Commit Message:**
```
Fix architecture violations: Convert ExomizerStep and DasmStep to regular classes

Removes `data class` keyword from ExomizerStep and DasmStep to fix mutable port field pattern violation. Data classes auto-generate equals/hashCode based on ALL constructor parameters, including mutable fields, which breaks immutability contract and creates equality bugs.

- Convert ExomizerStep from data class to regular class
- Convert DasmStep from data class to regular class
- Update CLAUDE.md to clarify step class patterns
- Add integration tests for ExomizerStep and DasmStep
- Verify port injection compatibility with task adapters

Fixes violations found in architecture quality check (Issue #193).
```

## Future Improvements (Not in Scope)

1. **Constructor-based port injection:** Long-term refactoring to inject ports via constructor instead of setter methods. Requires changes to task adapter infrastructure.
2. **Automated architecture validation:** Add CI checks to prevent similar violations in future PRs.
3. **Architecture documentation:** Create detailed architecture guide with visual diagrams.
