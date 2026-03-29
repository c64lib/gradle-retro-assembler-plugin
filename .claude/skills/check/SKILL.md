---
description: Run full build and all static analysis checks (compilation, Spotless, Detekt, JaCoCo) across all submodules and report results
user-invocable: true
---

# Check Build Status

You are tasked with running a full build and all static analysis checks across all submodules, then reporting the results.

## Context

Current branch: {{git_branch}}

This project is a multi-module Gradle plugin with 58+ submodules organized in hexagonal architecture. Static checks include:
- **Compilation**: Kotlin compilation across all modules
- **Spotless**: Code formatting enforcement (ktfmt + license headers)
- **Detekt**: Static code analysis (complexity, naming, style, potential bugs)
- **JaCoCo**: Code coverage reporting (aggregated across modules)

## Your Task

Follow these steps systematically:

### Step 1: Run the Build (Excluding Tests)

Execute the Gradle build excluding test execution to check compilation and static analysis:

```bash
./gradlew build -x test
```

Wait for the command to complete. Capture the full output.

### Step 2: Run Detekt Analysis

Execute Detekt static analysis separately to get detailed results:

```bash
./gradlew detekt
```

Wait for the command to complete. Capture the full output.

### Step 3: Run Spotless Check

Verify code formatting compliance:

```bash
./gradlew spotlessCheck
```

Wait for the command to complete. Capture the full output.

### Step 4: Generate Coverage Report

Generate the aggregated JaCoCo coverage report (from last test run):

```bash
./gradlew jacocoReport
```

Wait for the command to complete. Capture the full output.

### Step 5: Analyze and Report Results

Present a structured report to the user:

```markdown
## Build Status Report

**Branch**: {current branch}
**Date**: {current date}

### Compilation
- **Status**: PASS / FAIL
- **Details**: {any compilation errors with file:line references}

### Spotless (Code Formatting)
- **Status**: PASS / FAIL
- **Violations**: {count and list of files with formatting issues}
- **Auto-fix**: Run `./gradlew spotlessApply` to fix formatting issues

### Detekt (Static Analysis)
- **Status**: PASS / WARNINGS
- **Violations**: {count by category}
- **Top Issues**: {list the most significant findings}
- **Report**: `build/reports/detekt/detekt.html`

### JaCoCo (Coverage)
- **Status**: GENERATED / SKIPPED (no test data)
- **Report**: `build/reports/jacoco/aggregated/index.html`

### Summary
- {overall assessment}
- {recommended actions if any checks failed}
```

### Step 6: Handle Failures

If any check fails:

1. **Compilation failure**: Report exact errors with file paths and line numbers. Suggest specific fixes.
2. **Spotless failure**: List non-compliant files. Offer to run `./gradlew spotlessApply` to auto-fix.
3. **Detekt violations**: Categorize violations (complexity, naming, style, bugs). Highlight critical ones.
4. **JaCoCo issues**: Note if coverage data is missing (tests haven't been run).

## Important Guidelines

- Run each check separately to isolate failures clearly
- Always report file paths relative to project root
- For Spotless failures, always mention the auto-fix command
- For Detekt, focus on actionable violations (not style-only warnings)
- Do not attempt to fix issues automatically unless the user asks
- If build fails early, still attempt remaining checks where possible
