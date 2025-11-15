# Fix Command

You are tasked with diagnosing errors encountered during implementation and updating the action plan with fix steps.

## Context

This command is used when the `/execute` command has been run and errors or issues were encountered during implementation. The goal is to analyze errors, diagnose root causes, and update the action plan with next steps for fixing the issues.

Current branch: {{git_branch}}

## Your Task

Follow these steps systematically:

### Step 1: Locate the Action Plan

First, identify which action plan was being executed:

1. **Check current branch name** for context (format: `{issue-number}-{feature-short-name}`)
2. **Search for action plans** in `.ai/` directory
3. **Ask user to specify** which plan was being executed if multiple plans exist or if unclear

Use the AskUserQuestion tool to confirm which plan file should be updated if there's any ambiguity.

Expected plan location pattern: `.ai/{issue-number}-{feature-short-name}/feature-{issue-number}-{feature-short-name}-action-plan.md`

### Step 2: Read and Understand the Action Plan

Read the entire action plan file to understand:
- What was being implemented
- Which phase/step was being executed
- The intended implementation approach
- Expected outcomes and deliverables
- Architecture and design decisions

### Step 3: Identify Error Category

Ask the user to categorize the type of error encountered using the AskUserQuestion tool:

**Question**: "What type of error did you encounter during implementation?"

**Options**:
1. **Build-time errors**
   - Description: Compilation failures, type errors, syntax errors, dependency resolution issues

2. **Runtime errors**
   - Description: Exceptions, crashes, null pointer errors, class not found errors during execution or testing

3. **Factual errors/misbehaviors**
   - Description: Code compiles and runs but produces wrong results, incorrect behavior, or doesn't meet requirements

4. **Other errors**
   - Description: Configuration issues, environment problems, tooling errors, or other unexpected problems

### Step 4: Gather Error Details

Based on the error category, ask the user for detailed information:

#### For Build-time Errors (Category A):
Use AskUserQuestion tool to ask:
- **Question**: "Please provide the complete error message and stacktrace from the build failure. Include the full output showing what file, line, and what the compiler/build tool reported."

#### For Runtime Errors (Category B):
Use AskUserQuestion tool to ask:
- **Question**: "Please provide the complete exception stacktrace or runtime error output. Include the exception type, message, and the full stack trace showing where the error occurred."

#### For Factual Errors/Misbehaviors (Category C):
Use AskUserQuestion tool to ask:
- **Question**: "Please describe what behavior you expected versus what actually happened. Include any relevant output, logs, or test results that demonstrate the incorrect behavior."

#### For Other Errors (Category D):
Use AskUserQuestion tool to ask:
- **Question**: "Please describe the error or issue in detail. Include any error messages, unexpected behavior, or problems you encountered."

### Step 5: Analyze Error in Context

With the error details and action plan context:

1. **Identify Root Cause**
   - Analyze the error message/behavior
   - Review the relevant code sections that were implemented
   - Consider the architecture and design decisions from the plan
   - Identify what went wrong and why

2. **Use Exploration Tools**
   - Use Task tool with subagent_type=Explore to investigate the codebase
   - Search for relevant code sections using Grep or Glob
   - Read the affected files using Read tool
   - Understand the context around the error

3. **Trace Back to Action Plan**
   - Identify which step in the action plan caused or relates to the error
   - Determine if the issue is:
     - Implementation mistake (code written incorrectly)
     - Design flaw (action plan approach was wrong)
     - Missing consideration (something wasn't accounted for in planning)
     - Environment/tooling issue (unrelated to the implementation)

### Step 6: Formulate Fix Strategy

Based on the root cause analysis, determine the fix approach:

1. **Quick Fix**: Simple correction that doesn't require plan changes
   - Single file edit
   - Typo or syntax correction
   - Import statement fix

2. **Implementation Adjustment**: Code needs rework but plan stays the same
   - Different implementation of same approach
   - Refactoring to fix the issue
   - Additional error handling

3. **Design Revision**: The planned approach needs to change
   - Architecture adjustment
   - Different integration point
   - Alternative solution needed

4. **New Steps Required**: Additional work needed that wasn't in original plan
   - Missing dependencies
   - Additional configuration
   - Prerequisite steps

### Step 7: Update the Action Plan

Update the action plan file with fix steps. The update structure depends on the fix strategy:

#### For Quick Fixes:
Add a subsection under the current phase/step being executed:

```markdown
**Step X.Y Fix** (Added: {YYYY-MM-DD})
- **Issue**: {Brief description of error}
- **Root Cause**: {What caused the error}
- **Fix**: {What needs to be done}
- Files: `{files to modify}`
- Testing: {How to verify fix}
```

#### For Implementation Adjustments:
Update the existing step with corrected approach:

```markdown
**Step X.Y**: {Original action item} *(Revised: {YYYY-MM-DD})*
- Files: `{files to create/modify}`
- Description: {Updated implementation approach}
- **Previous Issue**: {What went wrong}
- **Correction**: {How the approach is being adjusted}
- Testing: {How to verify}
```

#### For Design Revisions:
Add a new subsection in Section 4 (Questions and Clarifications):

```markdown
### Design Issues Encountered

**Issue {N}**: {Description of design problem}
- **Discovered**: {YYYY-MM-DD}
- **Original Approach**: {What was planned}
- **Problem**: {Why it didn't work}
- **Revised Approach**: {New solution}
- **Impact**: {What sections of the plan are affected}
```

Then update the affected sections (Architecture Alignment, Implementation Plan, etc.)

#### For New Steps Required:
Add new steps to the appropriate phase:

```markdown
**Step X.{N+1}**: {New required step} *(Added: {YYYY-MM-DD})*
- Files: `{files to create/modify}`
- Description: {What needs to be done}
- **Reason**: {Why this step was added (refer to error)}
- Testing: {How to verify}
```

### Step 8: Add Execution Log Entry

Add or update the execution log section at the end of the action plan:

```markdown
## Execution Log

### {YYYY-MM-DD} - Error Diagnosis and Fix Planning

**Error Category**: {A/B/C/D - Full category name}

**Error Details**:
```
{Paste of error message/stacktrace/description}
```

**Root Cause Analysis**:
{Detailed explanation of what caused the error}

**Affected Step**: {Phase X, Step X.Y}

**Fix Strategy**: {Quick Fix/Implementation Adjustment/Design Revision/New Steps}

**Fix Steps Added**:
- {List of new or modified steps in the plan}

**Next Actions**:
- {What should be done next to resolve the issue}
```

### Step 9: Add Revision History Entry

Update the Revision History section (or create it if it doesn't exist):

```markdown
## 10. Revision History

| Date | Updated By | Changes |
|------|------------|---------|
| {YYYY-MM-DD} | AI Agent | Added fix steps for {error category}: {brief description} |
```

### Step 10: Present Summary and Next Steps

Provide the user with a clear summary:

```markdown
## Fix Analysis Summary

**Action Plan**: `.ai/{path-to-plan}`
**Error Category**: {Category name}
**Affected Step**: {Phase X, Step X.Y}

### Root Cause
{Clear explanation of what went wrong}

### Fix Strategy
{What approach will be taken}

### Changes to Action Plan

{List sections that were updated}

### Next Steps

You can now:
1. Run `/execute` again to implement the fix steps
2. Review the updated action plan at: `.ai/{path}`
3. Manually implement the fix if you prefer

The action plan has been updated with detailed fix steps based on the error analysis.
```

## Important Guidelines

### Error Analysis
- **Be Thorough**: Use Task tool to explore code and understand context
- **Be Specific**: Identify exact files, lines, and root causes
- **Consider Architecture**: Ensure fixes align with hexagonal architecture
- **Check Similar Code**: Look for patterns in existing code that might help

### Fix Quality
- **Actionable Steps**: Fix steps should be clear and implementable
- **Testable**: Each fix step should include verification approach
- **Minimal Impact**: Prefer fixes that don't require extensive plan changes
- **Safe**: Don't suggest risky changes without proper testing

### Plan Updates
- **Preserve Structure**: Maintain the original plan format and sections
- **Track Changes**: Always add revision history entries
- **Clear Marking**: Use date stamps and "Added/Revised" markers
- **Cross-Reference**: Link fix steps back to original steps

### Communication
- **User-Friendly**: Explain errors in plain language
- **Educational**: Help user understand why the error occurred
- **Forward-Looking**: Focus on solution, not just problem
- **Transparent**: Show your analysis and reasoning

## Edge Cases

### If Multiple Errors
1. Ask user to prioritize or provide all errors
2. Analyze each error separately
3. Look for common root causes
4. Create fix steps that address multiple errors if possible
5. Document all errors in execution log

### If Error is Not Clear
1. Ask follow-up questions using AskUserQuestion
2. Request additional context (logs, test output, etc.)
3. Explore the codebase to understand the situation
4. Make best effort analysis based on available information
5. Document assumptions in the fix steps

### If Error Suggests Plan Was Wrong
1. Clearly highlight the design issue
2. Propose alternative approach
3. Explain impact on other plan sections
4. Get user approval before major plan revisions
5. Update all affected sections for consistency

### If Error is Environmental
1. Distinguish between code issues and environment issues
2. Document environment requirements in plan
3. Add setup/configuration steps if needed
4. Consider adding environment validation step
5. Don't change implementation unnecessarily

## Output Format

Always conclude with this format:

```markdown
---

## ✅ Fix Analysis Complete

**Plan Updated**: `.ai/{path}`
**Fix Strategy**: {Strategy type}
**Estimated Effort**: {Small/Medium/Large}

The action plan has been updated with {N} fix step(s). You can now run `/execute` to implement the fixes.
```

---

**Note**: This command analyzes errors and updates plans. To implement the fixes, use the `/execute` command after the plan is updated.
