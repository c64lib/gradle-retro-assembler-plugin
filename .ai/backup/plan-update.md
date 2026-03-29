# Plan Update Command

You are tasked with updating an existing development action plan. Follow this workflow exactly:

## Step 1: Locate the Action Plan

First, identify which action plan needs to be updated:

1. **Check current branch name** for context (format: `{issue-number}-{feature-short-name}`)
2. **Search for action plans** in `.ai/` directory
3. **Ask user to specify** which plan to update if multiple plans exist or if unclear

Use the AskUserQuestion tool to confirm which plan file should be updated if there's any ambiguity.

Expected plan location pattern: `.ai/{issue-number}-{feature-short-name}/feature-{issue-number}-{feature-short-name}-action-plan.md`

## Step 2: Read the Current Plan

Read the entire action plan file to understand:
- Current feature requirements and scope
- Existing implementation steps
- Open questions that need answers
- Current design decisions
- Implementation status

## Step 3: Determine Update Scope

Ask the user about the scope of updates using AskUserQuestion tool. Common update types include:

1. **Specification Changes**
   - Modified requirements
   - Changed success criteria
   - Updated feature scope
   - New constraints or considerations

2. **Answered Questions**
   - Responses to unresolved questions
   - Clarifications on design decisions
   - Stakeholder feedback

3. **Additional Acceptance Criteria**
   - New success criteria
   - Additional testing requirements
   - Performance or quality metrics

4. **Implementation Updates**
   - Status changes (Planning → In Progress → Completed)
   - Phase completion updates
   - New risks or mitigation strategies

5. **Architecture Refinements**
   - Updated integration points
   - Modified port/adapter design
   - Changed dependencies

6. **Other Updates**
   - Documentation needs
   - Testing strategy changes
   - Rollout plan modifications

**IMPORTANT**: If the user's input is incomplete or unclear, use AskUserQuestion tool to gather clarifications before proceeding.

## Step 4: Apply Updates Consistently

When updating the plan, ensure consistency across ALL relevant sections:

### For Specification Changes:
- Update **Section 1: Feature Description**
  - Modify Overview, Requirements, or Success Criteria as needed
- Update **Section 2: Root Cause Analysis**
  - Adjust Desired State and Gap Analysis if scope changed
- Update **Section 5: Implementation Plan**
  - Revise phases and steps to reflect new requirements
  - Update deliverables for each phase
- Update **Section 6: Testing Strategy**
  - Adjust test scenarios to match new requirements
- Update **Section 7: Risks and Mitigation**
  - Add new risks or update existing ones
- Update **Section 8: Documentation Updates**
  - Add new documentation needs if applicable

### For Answered Questions:
- Move answered questions from **"Unresolved Questions"** subsection to **"Self-Reflection Questions"** subsection
- Format answered questions as:
  ```markdown
  - **Q**: {Question}
    - **A**: {Answer provided by user}
  ```
- Mark questions as answered using checkbox: `- [x] {Question}` before moving
- If the answer impacts other sections, propagate changes:
  - Update implementation steps if the answer changes approach
  - Update architecture alignment if ports/adapters are affected
  - Update risks if new concerns emerge
  - Update testing strategy if verification approach changes

### For Design Decisions:
- Update the **"Design Decisions"** subsection with chosen option
- Format as:
  ```markdown
  - **Decision**: {What was decided}
    - **Options**: {Option A, Option B, etc.}
    - **Chosen**: {Selected option}
    - **Rationale**: {Why this was chosen}
  ```
- Propagate decision impacts to:
  - Implementation Plan (update steps to reflect chosen approach)
  - Relevant Code Parts (update if different components involved)
  - Dependencies (add/remove based on decision)
  - Testing Strategy (adjust based on approach)

### For Additional Acceptance Criteria:
- Add new criteria to **Section 1: Success Criteria**
- Update **Section 6: Testing Strategy** to verify new criteria
- Update relevant phase deliverables in **Section 5**

### For Implementation Status:
- Update the **Status** field at the top (Planning → In Progress → Completed)
- Mark completed steps with checkboxes: `- [x]`
- Add **"Last Updated"** field with current date
- If phases are completed, add completion notes

### For Architecture Refinements:
- Update **Section 3: Architecture Alignment**
- Update **Section 3: Existing Components** if integration points changed
- Update **Section 3: Dependencies** if new dependencies added
- Ensure **Section 5: Implementation Plan** reflects architecture changes

## Step 5: Preserve Plan Structure

**CRITICAL**: Maintain the exact structure from the original plan command:
- Keep all 9 main sections in order
- Preserve markdown formatting
- Keep section numbering consistent
- Maintain table formats for risks
- Preserve checkbox formats for action items

## Step 6: Track Changes

Add a **"Revision History"** section at the end of the document (before the final note) if it doesn't exist:

```markdown
## 10. Revision History

| Date | Updated By | Changes |
|------|------------|---------|
| {YYYY-MM-DD} | {User/AI} | {Brief description of changes} |
```

Add a new row for each update with:
- Current date
- Who made the update (use "AI Agent" for updates made by Claude)
- Brief summary of what changed

## Step 7: Interactive Review

After applying updates:

1. Present the updated plan to the user
2. Highlight what was changed
3. Specifically call out any cascading changes made to maintain consistency
4. Ask if additional updates are needed
5. If yes, use AskUserQuestion tool to gather more information
6. Repeat until the user is satisfied

## Step 8: Save and Confirm

Once updates are complete:

1. Save the updated plan to the same file location
2. Confirm with the user that updates are complete
3. Summarize what was changed
4. Suggest next steps if applicable (e.g., "Plan is ready for Phase 2 implementation")

## Important Guidelines

### Consistency Rules
- **Cross-Reference Impact**: When updating one section, always check if other sections need updates
- **Traceability**: Ensure requirements trace through to implementation steps and tests
- **Completeness**: Don't leave orphaned questions or decisions without resolution paths

### Answer Documentation
- **Format Precisely**: Use the exact format for answered questions
- **Preserve Context**: Keep the original question text intact
- **Clear Answers**: Ensure answers are complete and actionable
- **Mark Completion**: Always mark answered questions with `[x]` before moving them

### Clarification Protocol
- **Ask When Unclear**: If update scope is ambiguous, ask for clarification
- **Verify Impact**: If an update affects multiple sections, confirm the extent with the user
- **Suggest Options**: If there are multiple ways to interpret an update, present options
- **Confirm Understanding**: Repeat back your understanding before making large changes

### Architecture Compliance
- Ensure updates still follow hexagonal architecture principles
- Verify use case pattern compliance (single-method classes with `apply`)
- Check that ports properly isolate technology concerns
- Remind about `infra/gradle` dependency updates if modules are added

### Safety Checks
- Don't remove important information unless explicitly requested
- Preserve historical context (don't delete answered questions)
- Maintain backward compatibility considerations in updates
- Keep risk assessments current

## Edge Cases

### If Plan File Not Found
1. List available plans in `.ai/` directory
2. Check if user meant a different file
3. Offer to create a new plan using the `/plan` command instead

### If Update Conflicts with Existing Content
1. Highlight the conflict to the user
2. Present both versions (current vs. proposed)
3. Ask for guidance on resolution
4. Document the decision in Revision History

### If Questions Reference Non-Existent Sections
1. Alert the user that the plan structure might be outdated
2. Offer to restructure to match current template
3. Get approval before major restructuring

## Output Format

When presenting changes to the user, use this format:

```markdown
## Changes Applied to Action Plan

**Plan**: `.ai/{path-to-plan}`
**Date**: {YYYY-MM-DD}

### Summary
{Brief overview of what was updated}

### Detailed Changes

#### Section 1: Feature Description
- {Change 1}
- {Change 2}

#### Section 4: Questions and Clarifications
- Moved question "{question}" from Unresolved to Self-Reflection
- Added answer: {answer}

#### Section 5: Implementation Plan
- Updated Phase 2, Step 2.1 to reflect new approach
- Added new step 3.3 for additional requirement

{...etc for all changed sections...}

### Cascading Updates
{List any changes made to maintain consistency across sections}

### Next Steps
{Suggest what the user might want to do next}
```

---

**Note**: This command updates existing plans only. To create a new plan, use the `/plan` command instead.
