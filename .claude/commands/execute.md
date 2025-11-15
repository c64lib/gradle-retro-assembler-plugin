# Execute Action Plan

You are an AI Agent tasked with implementing an action plan for this software project.

## Context

This project uses action plans stored in the `.ai` folder to guide feature implementation and changes. Action plans are created with the `/plan` command and can be updated with `/plan-update`.

Current branch: {{git_branch}}

## Your Task

Follow these steps systematically:

### Step 1: Identify the Action Plan

Ask the user which action plan should be executed. To help them:
- List available action plans in the `.ai` folder
- Consider the current branch name as context for suggesting relevant plans
- Ask the user to confirm or specify the action plan file path

### Step 2: Read and Analyze the Plan

Once the action plan is identified:
- Read the action plan file completely
- Understand the overall structure (phases, steps, tasks)
- Identify which items are already completed, pending, or blocked
- Present a summary showing:
  - Total phases and their names
  - Total steps within each phase
  - Current completion status

### Step 3: Determine Scope of Execution

Ask the user which steps or phases to implement:
- Allow single step/phase: "Phase 1", "Step 2.3"
- Allow ranges: "Phase 1-3", "Steps 1.1-1.5"
- Allow "all" to execute everything that's pending
- Allow comma-separated combinations: "Phase 1, Phase 3, Step 4.2"

Parse the user's input and confirm which specific items will be executed.

### Step 4: Determine Interaction Mode

Ask the user: "Should I ask for confirmation after each step/phase before continuing?"
- If YES: Pause after each completed step/phase and wait for user approval to continue
- If NO: Execute all items in the specified range autonomously

### Step 5: Execute the Plan

For each step or phase in scope:
1. Create a todo list using TodoWrite tool with all tasks for this execution
2. Mark the current step/phase as "in progress" in your tracking
3. Read and understand the requirements
4. Implement the required changes following the project's architecture guidelines
5. Test the changes as specified in the action plan
6. Mark the step/phase as completed in your tracking
7. If interaction mode is ON, ask user: "Step X.Y completed. Continue to next step? (yes/no/skip)"
   - yes: Continue to next step
   - no: Stop execution and proceed to final update
   - skip: Mark current as skipped and move to next

### Step 6: Handle Blockers and Issues

If you encounter issues during execution:
- Document the blocker clearly
- Mark the step as "blocked" with reason
- Ask user for guidance or decision
- If user chooses to skip, mark as "skipped" with reason
- Update the action plan accordingly

### Step 7: Update the Action Plan

After execution is complete (or stopped):
1. Update the action plan file to reflect:
   - Steps/phases marked as COMPLETED (✓)
   - Steps/phases marked as SKIPPED with reason in parentheses
   - Steps/phases marked as BLOCKED with reason in parentheses
   - Timestamp of execution
2. Preserve the original plan structure and formatting
3. Add an execution log entry at the end with:
   - Date and time
   - Items executed
   - Items skipped/blocked with reasons
   - Overall outcome

### Step 8: Provide Summary

Present a final summary to the user:
- What was completed successfully
- What was skipped and why
- What is blocked and needs attention
- Suggested next steps
- Updated action plan file location

## Important Guidelines

- **Follow Architecture**: Adhere to the Hexagonal Architecture described in CLAUDE.md
- **Use TodoWrite**: Always use TodoWrite tool to track your implementation tasks
- **Test Your Changes**: Run tests after significant changes using `./gradlew test`
- **Commit Appropriately**: Follow commit message guidelines from CLAUDE.md
- **Stay Focused**: Only implement what's specified in the action plan steps
- **Ask When Uncertain**: Use AskUserQuestion tool when you need clarification
- **Update Incrementally**: Keep the action plan updated as you progress, not just at the end

## Error Handling

If builds fail or tests break:
1. Show the error to the user
2. Attempt to fix if the issue is clear
3. If uncertain, ask the user how to proceed
4. Document the issue in the action plan update

## Example Interaction Flow

```
Assistant: I'll help you execute an action plan. Let me first find available plans...

[Lists plans from .ai folder]

Based on your current branch "feature-X", I suggest: .ai/feature-X-action-plan.md

Which action plan would you like to execute?

User: Yes, that one