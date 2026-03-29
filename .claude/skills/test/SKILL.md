---
description: Run all unit tests across all submodules, analyze results with detailed failure diagnostics, and generate coverage reports
user-invocable: true
---

# Run Unit Tests

You are tasked with running all unit tests across all submodules, analyzing results, and reporting outcomes including detailed failure diagnostics.

## Context

Current branch: {{git_branch}}

This project is a multi-module Gradle plugin with 58+ submodules. Tests use JUnit 5, KoTest, Mockito, and MockK. Each module generates individual test results and JaCoCo coverage reports.

## Your Task

Follow these steps systematically:

### Step 1: Run All Tests

Execute the full test suite:

```bash
./gradlew test
```

Wait for the command to complete. Capture the full output including any failures.

### Step 2: Collect Test Results

Aggregate test results for analysis:

```bash
./gradlew collectTestResults
```

### Step 3: Generate Coverage Report

Generate the aggregated JaCoCo coverage report from the test run:

```bash
./gradlew jacocoReport
```

### Step 4: Analyze Results

#### If All Tests Pass:

Present a success report:

```markdown
## Test Results Report

**Branch**: {current branch}
**Date**: {current date}
**Status**: ALL TESTS PASSED

### Summary
- **Total modules tested**: {count}
- **Test results**: All passing

### Coverage
- **Aggregated report**: `build/reports/jacoco/aggregated/index.html`
- **XML report**: `build/reports/jacoco/aggregated/jacoco.xml`
```

#### If Tests Fail:

Perform detailed failure analysis:

1. **Identify failing tests**: Parse the Gradle output to find all failing test classes and methods.

2. **Read test failure details**: For each failing test, use the Gradle output to extract:
   - Test class and method name
   - Module where the test lives
   - Exception type and message
   - Relevant stack trace lines (focus on project code, not framework internals)

3. **Read the failing test source**: For each failing test, locate and read the test file to understand:
   - What the test is asserting
   - What setup/mocking is involved
   - The expected vs actual behavior

4. **Read the tested code**: Locate the production code being tested and read it to understand:
   - What the code is supposed to do
   - Recent changes that might have caused the failure
   - Whether the test or the code is likely wrong

5. **Diagnose root cause**: For each failure, determine:
   - Is this a test bug or a code bug?
   - Is this a regression from recent changes?
   - Is this a flaky test?
   - What is the minimal fix?

Present a detailed failure report:

```markdown
## Test Results Report

**Branch**: {current branch}
**Date**: {current date}
**Status**: FAILURES DETECTED

### Failing Tests

#### Failure 1: {TestClassName.testMethodName}
- **Module**: {module path}
- **File**: {test file path}:{line number}
- **Exception**: {exception type}: {message}
- **Root Cause**: {diagnosis}
- **Suggested Fix**: {what needs to change and where}

#### Failure 2: ...
{repeat for each failure}

### Passing Modules
- {list modules where all tests passed}

### Coverage
- **Aggregated report**: `build/reports/jacoco/aggregated/index.html`

### Recommended Actions
1. {prioritized list of fixes}
```

### Step 5: Module-Specific Re-run (Optional)

If failures are isolated to specific modules, offer to re-run just those modules for faster iteration:

```
To re-run failed module tests:
./gradlew :{module}:test
```

## Important Guidelines

- Always run the full test suite first before investigating failures
- When reading failing tests, focus on understanding intent, not just syntax
- Check git diff on the current branch to correlate failures with recent changes
- For each failure, always provide a concrete suggested fix with file path and description
- Do not attempt to fix tests automatically unless the user asks
- If a test appears flaky (passes on re-run), note this explicitly
- Report coverage only if jacocoReport succeeds (it depends on test execution data)
- Use `./gradlew :module:test --tests "TestClassName"` syntax when suggesting targeted re-runs
