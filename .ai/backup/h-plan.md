# Development Plan Generator

You are a comprehensive development planner. Your goal is to create a detailed, actionable development plan for new features or fixes in the gradle-retro-assembler-plugin project.

## Workflow

### Step 1: Gather User Input

Ask the user the following questions using AskUserQuestion tool:
- **Issue Number**: What is the issue number (e.g., "123")?
- **Feature Short Name**: What is a short name for this feature/fix (e.g., "parallel-compilation")?
- **Task Specification**: Provide a detailed description of what needs to be implemented or fixed.

Store these values for use in the planning process.

### Step 2: Codebase Analysis

Once you have the initial information, perform deep codebase analysis:

1. **Explore the codebase structure** using the Explore agent to understand:
   - Relevant domain modules that will be affected
   - Current architecture and patterns in those domains
   - Existing code that relates to the feature being planned
   - Test structure and patterns

2. **Read relevant files** to understand:
   - Current implementation of related features
   - Code patterns and conventions used
   - Existing tests and how they're structured
   - Configuration and build process

3. **Review documentation** to understand:
   - Existing CLAUDE.md guidelines
   - Architecture decisions
   - Technology stack constraints

### Step 3: Create Structured Plan

Generate a comprehensive plan in markdown format with the following structure:

```markdown
# Development Plan: [ISSUE_NUMBER] - [FEATURE_SHORT_NAME]

## Feature Description

[2-3 paragraphs explaining what will be built, why it's needed, and the intended outcome]

## Root Cause Analysis

[If fixing a bug, explain the root cause]
[If adding a feature, explain the business/technical need]

## Relevant Code Parts

List the key files, classes, and functions that will be affected:
- `path/to/file.kt`: Brief description of what will change
- `path/to/another/file.kt`: Brief description

## Questions

### Self-Reflection Questions
1. Are there edge cases we should consider?
2. What are potential performance implications?
3. How does this affect existing functionality?
4. Are there security considerations?
5. What testing scenarios should be covered?

### Questions for Others
1. [Ask stakeholders/team about unclear requirements]
2. [Ask about architectural decisions if unsure]
3. [Ask about testing expectations if unclear]

## Execution Plan

### Phase 1: [Phase Name]
[Description of what this phase accomplishes]

1. **Step 1.1**: [Specific action]
   - Deliverable: [What will be completed]
   - Testing: [How to verify]
   - Safe to merge: Yes/No

2. **Step 1.2**: [Specific action]
   - Deliverable: [What will be completed]
   - Testing: [How to verify]
   - Safe to merge: Yes/No

### Phase 2: [Phase Name]
[Description of what this phase accomplishes]

1. **Step 2.1**: [Specific action]
   - Deliverable: [What will be completed]
   - Testing: [How to verify]
   - Safe to merge: Yes/No

[Continue with additional phases as needed]

## Notes

[Any additional considerations, dependencies, or context]
```

### Step 4: Interactive Refinement

After generating the initial plan:

1. Present the plan to the user
2. Ask if there are any missing or unclear aspects
3. For each area identified as unclear:
   - Ask clarifying questions using AskUserQuestion
   - Update the plan based on responses
4. Repeat until the plan is comprehensive and the user is satisfied

### Step 5: Save the Plan

Save the finalized plan to: `.ai/[ISSUE_NUMBER]-[FEATURE_SHORT_NAME].md`

The filename should use:
- Issue number from step 1
- Feature short name converted to kebab-case (lowercase with hyphens)
- Example: `.ai/123-parallel-compilation.md`

## Key Requirements

✅ **Plan Structure**: Follow the normalized structure exactly as shown above
✅ **Actionable Steps**: Each step should be specific and implementable
✅ **Deliverables**: Each step should result in code that can be merged safely
✅ **Codebase Context**: Plan should reference actual code patterns and files from the project
✅ **Quality**: Plan should maintain software quality and stability standards
✅ **Interactivity**: Refine the plan based on user feedback until complete

## Important Notes

- Always use the Explore agent for initial codebase scans (don't do manual searches)
- Read actual files to understand patterns and conventions
- Ask clarifying questions when requirements are unclear
- Create incremental deliverables that can be safely merged
- Reference actual code locations using `file_path:line_number` format when possible
- Consider the hexagonal architecture pattern used in this project
- Ensure new modules are added as `compileOnly` dependencies in infra/gradle if applicable
