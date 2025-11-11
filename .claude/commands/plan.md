# Plan Command

You are tasked with creating a comprehensive development plan for a new feature or issue. Follow this workflow exactly:

## Step 1: Gather Information

First, collect the following information from the user:

1. **Issue Number**: The GitHub issue number or ticket ID
2. **Feature Short Name**: A brief, kebab-case name for the feature (e.g., "bitmap-step", "flow-optimization")
3. **Task Specification**: Detailed description of what needs to be implemented

Use the AskUserQuestion tool to gather this information if not already provided.

## Step 2: Codebase Analysis

Before creating the plan, you must:

1. Review the project structure and architecture (use Task tool with subagent_type=Explore)
2. Identify relevant existing code that relates to this feature
3. Understand how similar features are implemented
4. Review relevant documentation files
5. Analyze dependencies and integration points

## Step 3: Create the Plan

Create a markdown file at `.ai/{issue-number}-{feature-short-name}/feature-{issue-number}-{feature-short-name}-action-plan.md`

The plan must follow this exact structure:

```markdown
# Feature: {Feature Name}

**Issue**: #{issue-number}
**Status**: Planning
**Created**: {YYYY-MM-DD}

## 1. Feature Description

### Overview
{Concise description of what needs to be implemented}

### Requirements
- {Requirement 1}
- {Requirement 2}
- {etc.}

### Success Criteria
- {Criterion 1}
- {Criterion 2}
- {etc.}

## 2. Root Cause Analysis

{If this is a bug fix or improvement, explain the root cause. If it's a new feature, explain why it's needed and what problem it solves.}

### Current State
{Description of how things work currently}

### Desired State
{Description of how things should work after implementation}

### Gap Analysis
{What needs to change to bridge the gap}

## 3. Relevant Code Parts

### Existing Components
- **{Component/File Name}**: {Brief description and relevance}
  - Location: `{path/to/file}`
  - Purpose: {Why this is relevant}
  - Integration Point: {How the new feature will interact with this}

### Architecture Alignment
{How this feature fits into the hexagonal architecture:}
- **Domain**: {Which domain this belongs to}
- **Use Cases**: {What use cases will be created/modified}
- **Ports**: {What interfaces will be needed}
- **Adapters**: {What adapters will be needed (in/out, gradle, etc.)}

### Dependencies
- {Dependency 1 and why it's needed}
- {Dependency 2 and why it's needed}

## 4. Questions and Clarifications

### Self-Reflection Questions
{Questions you've answered through research:}
- **Q**: {Question}
  - **A**: {Answer based on codebase analysis}

### Unresolved Questions
{Questions that need clarification from stakeholders:}
- [ ] {Question 1}
- [ ] {Question 2}

### Design Decisions
{Key decisions that need to be made:}
- **Decision**: {What needs to be decided}
  - **Options**: {Option A, Option B, etc.}
  - **Recommendation**: {Your recommendation and why}

## 5. Implementation Plan

### Phase 1: Foundation ({Deliverable: What can be merged})
**Goal**: {What this phase achieves}

1. **Step 1.1**: {Action item}
   - Files: `{files to create/modify}`
   - Description: {What to do}
   - Testing: {How to verify}

2. **Step 1.2**: {Action item}
   - Files: `{files to create/modify}`
   - Description: {What to do}
   - Testing: {How to verify}

**Phase 1 Deliverable**: {What can be safely merged and released after this phase}

### Phase 2: Core Implementation ({Deliverable: What can be merged})
**Goal**: {What this phase achieves}

1. **Step 2.1**: {Action item}
   - Files: `{files to create/modify}`
   - Description: {What to do}
   - Testing: {How to verify}

2. **Step 2.2**: {Action item}
   - Files: `{files to create/modify}`
   - Description: {What to do}
   - Testing: {How to verify}

**Phase 2 Deliverable**: {What can be safely merged and released after this phase}

### Phase 3: Integration and Polish ({Deliverable: What can be merged})
**Goal**: {What this phase achieves}

1. **Step 3.1**: {Action item}
   - Files: `{files to create/modify}`
   - Description: {What to do}
   - Testing: {How to verify}

2. **Step 3.2**: {Action item}
   - Files: `{files to create/modify}`
   - Description: {What to do}
   - Testing: {How to verify}

**Phase 3 Deliverable**: {What can be safely merged and released after this phase}

## 6. Testing Strategy

### Unit Tests
- {What needs unit tests}
- {Testing approach}

### Integration Tests
- {What needs integration tests}
- {Testing approach}

### Manual Testing
- {Manual test scenarios}

## 7. Risks and Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| {Risk 1} | {High/Medium/Low} | {High/Medium/Low} | {How to mitigate} |
| {Risk 2} | {High/Medium/Low} | {High/Medium/Low} | {How to mitigate} |

## 8. Documentation Updates

- [ ] Update README if needed
- [ ] Update CLAUDE.md if adding new patterns
- [ ] Add inline documentation
- [ ] Update any relevant architectural docs

## 9. Rollout Plan

1. {How to release this safely}
2. {What to monitor}
3. {Rollback strategy if needed}

---

**Note**: This plan should be reviewed and approved before implementation begins.
```

## Step 4: Interactive Refinement

After creating the initial plan:

1. Present the plan to the user
2. Specifically highlight the "Unresolved Questions" section
3. Specifically highlight the "Design Decisions" section
4. Ask if they want to clarify any questions or make any design decisions now
5. If yes, use AskUserQuestion tool to gather clarifications
6. Update the plan with the new information
7. Repeat until the user is satisfied

## Step 5: Finalization

Once the plan is complete:

1. Ensure the file is saved in the correct location
2. Confirm with the user that the plan is ready
3. Suggest next steps (e.g., "You can now start implementing Phase 1" or "Run /exec to begin execution")

## Important Notes

- **Architecture Compliance**: Ensure the plan follows hexagonal architecture principles
- **Incremental Delivery**: Each phase must produce a mergeable, releasable increment
- **Safety First**: Never suggest changes that could break existing functionality without proper testing
- **Use Case Pattern**: Remember that use cases are single-method classes with `apply` method
- **Port Pattern**: Technology-specific code must be hidden behind ports
- **Gradle Module**: If adding new modules, remind about updating `infra/gradle` dependencies
- **Parallel Execution**: Always use Gradle Workers API for parallel tasks

## Thoroughness

- Use the Task tool with subagent_type=Explore to thoroughly understand the codebase
- Look for similar features to understand patterns
- Check existing tests to understand testing patterns
- Review recent commits to understand coding conventions
- Don't guess - if unsure, explore more or ask the user
