---
description: Run all unit tests across all submodules, analyze results with detailed failure diagnostics, and generate coverage reports
user-invocable: true
allowed-tools: Agent Skill Bash Read Grep Glob
---

# Run Unit Tests

You are tasked with running all unit tests across all submodules, analyzing results, and reporting outcomes including detailed failure diagnostics.

## Context

Current branch: {{git_branch}}

This project is a multi-module Gradle plugin with 58+ submodules. Tests use JUnit 5, KoTest, Mockito, and MockK. Each module generates individual test results and JaCoCo coverage reports.

## Division of labour: Gradle execution vs. analysis

This skill has two distinct kinds of work, and they run in different places:

- **Running Gradle** (the slow, mechanical part) is delegated — never run `./gradlew` directly from the main agent.
- **Analysing results** (parsing reports, reading test/production sources, diagnosing failures, writing the report) stays on the main agent, because it needs this conversation's context and judgement.

### Delegating the Gradle run

Route every `./gradlew` invocation through the **`build`** skill, which owns the convention of spawning a **Haiku** subagent to run the command off the main agent and relay the raw result. Invoke it with the exact task list this skill needs:

```
Skill(skill: "build", args: "test collectTestResults jacocoReport")
```

The `build` skill will spawn the Haiku subagent, run `./gradlew test collectTestResults jacocoReport --console=plain`, and report the outcome (BUILD SUCCESSFUL/FAILED, failing tasks, error lines, report paths) back to you.

Running the three tasks in one invocation is deliberate — a single Gradle run wires up the dependencies (`collectTestResults` and `jacocoReport` both depend on test execution data) and avoids spawning multiple subagents.

**Fallback:** if the `build` skill is unavailable for any reason, spawn the Haiku subagent yourself, exactly as the `build` skill documents:

```
Agent(
  subagent_type: "general-purpose",
  model: "haiku",
  run_in_background: false,
  description: "Run gradle tests",
  prompt: "From the repo root run: ./gradlew test collectTestResults jacocoReport --console=plain
           (Bash tool, timeout 600000 ms). Report the final outcome line, any failing tasks with
           their error lines verbatim including file:line, and confirm the report paths exist."
)
```

Do **not** run `./gradlew` inline on the main agent.

## Your Task

Follow these steps systematically:

### Step 1: Run tests, collect results, generate coverage (delegated)

Delegate a single Gradle run to the `build` skill as described above:

```
Skill(skill: "build", args: "test collectTestResults jacocoReport")
```

Wait for it to return. Capture the reported outcome, including any failing tasks and error lines.

### Step 2: Analyze Results (on the main agent)

Once the delegated run returns, do the analysis yourself using Read/Grep/Glob on the collected reports and sources.

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

1. **Identify failing tests**: Use the failing-task output the delegated run reported, plus the collected result XMLs under `build/test-results/gradle/`, to find all failing test classes and methods.

2. **Read test failure details**: For each failing test, extract:
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

### Step 3: Module-Specific Re-run (Optional)

If failures are isolated to specific modules, offer to re-run just those modules for faster iteration. Route the re-run through the `build` skill as well:

```
Skill(skill: "build", args: ":{module}:test --tests \"TestClassName\"")
```

## Important Guidelines

- **Never run `./gradlew` directly on the main agent** — delegate it through the `build` skill (Haiku subagent). Reserve the main agent for analysis.
- Always run the full test suite first before investigating failures
- When reading failing tests, focus on understanding intent, not just syntax
- Check git diff on the current branch to correlate failures with recent changes
- For each failure, always provide a concrete suggested fix with file path and description
- Do not attempt to fix tests automatically unless the user asks
- If a test appears flaky (passes on re-run), note this explicitly
- Report coverage only if `jacocoReport` succeeded (it depends on test execution data)
- Use the `:module:test --tests "TestClassName"` form (via the `build` skill) when suggesting targeted re-runs
