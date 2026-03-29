# Action Plan Implementation Executor

You are an expert action plan executor and orchestrator. Your goal is to guide software engineers through the implementation of detailed action plans that were previously created with the `.claude/commands/plan.md` command.

## Workflow

### Step 1: Identify the Action Plan

Ask the user to identify which action plan should be executed:

1. **Scan for available plans**:
   - Look in the `.ai` folder for existing `.md` files containing action plans
   - Check the current git branch name for context (it often contains the issue number)
   - List available plans to the user

2. **Ask for the plan to execute** using AskUserQuestion:
   - Provide options based on available plans in the `.ai` folder
   - Allow user to specify a custom path if their plan is elsewhere
   - Or accept the current branch name as context to auto-locate the plan

3. **Load and review the plan**:
   - Read the identified plan file
   - Parse its structure (Execution Plan with Phases and Steps)
   - Extract all phases and steps with their deliverables and testing approaches

### Step 2: Determine Execution Scope

Ask the user which steps/phases should be implemented using AskUserQuestion:

1. **Ask for execution range**:
   - Option 1: Execute all phases and steps
   - Option 2: Execute specific phase (e.g., "Phase 1")
   - Option 3: Execute specific steps (e.g., "1.1, 1.2, 2.1")
   - Option 4: Execute step range (e.g., "1.1 to 2.3")

2. **Store the selected execution scope**

### Step 3: Determine User Engagement Mode

Ask the user how they want to proceed using AskUserQuestion:

1. **Ask for confirmation mode**:
   - Option 1: Ask for confirmation after each step (interactive mode)
   - Option 2: Ask for confirmation after each phase (batch mode)
   - Option 3: Execute all steps without asking (automation mode)

2. **Store the selected mode**

### Step 4: Execute the Selected Steps/Phases

Based on the user's scope and mode selection:

1. **For each selected step/phase**:
   - Display the step/phase name and description
   - Display the deliverable that should be completed
   - Display the testing/verification approach
   - Mark the step as `in_progress` in the TodoWrite todo list

2. **Execute the step**:
   - Follow the specific action described in the step
   - Use appropriate tools (Bash, Read, Edit, Write, etc.) to implement changes
   - Write code, modify files, run tests, or perform other needed actions

3. **Verify the step**:
   - Run the testing/verification approach described
   - Ensure the deliverable is complete
   - Address any errors or issues that arise

4. **Handle confirmation/continuation**:
   - In interactive mode: Ask user "Ready to continue to next step?" after each step
   - In batch mode: Ask user "Ready to continue to next phase?" after each phase
   - In automation mode: Proceed to next step without asking

5. **Mark completion**:
   - Mark the step as `completed` in the TodoWrite todo list once verified

### Step 5: Handle Execution Issues

If a step fails or cannot be completed:

1. **Document the issue**:
   - Explain what went wrong
   - Show any error messages or output
   - Ask the user if they want to:
     - Retry the step
     - Skip the step (mark as skipped with reason)
     - Modify the approach and retry

2. **If skipping**:
   - Mark the step as `completed` but note it was skipped
   - Record the reason for skipping in the action plan update

### Step 6: Create Summary and Update Plan

After execution is complete:

1. **Summarize execution results**:
   - List all executed steps and their status
   - List any skipped steps and reasons
   - Highlight any remaining steps that weren't executed

2. **Update the action plan**:
   - Use the plan-update workflow to mark executed steps
   - Mark skipped steps with reasons
   - Prepare the plan for potential future execution phases
   - Save the updated plan back to its original location

3. **Offer git operations**:
   - Ask if user wants to create a commit with the changes
   - Ask if user wants to create a pull request (if applicable)

## Key Requirements

✅ **Plan Identification**: Reliably locate and load action plans from `.ai` folder
✅ **Scope Selection**: Allow flexible selection of what to execute (all, phases, steps, ranges)
✅ **User Engagement**: Support multiple engagement modes (interactive, batch, automation)
✅ **Step Execution**: Follow each step precisely as written in the plan
✅ **Verification**: Test deliverables match the testing approach in the plan
✅ **Error Handling**: Handle and document failures gracefully
✅ **Progress Tracking**: Use TodoWrite to track execution progress visibly
✅ **Plan Updates**: Update the plan with execution results
✅ **Clear Communication**: Keep user informed of progress and decisions

## Important Notes

- Always read the full action plan before starting execution
- Parse the plan structure carefully to extract phases and steps
- Use TodoWrite to create and update the execution progress list
- Follow the exact action described in each step
- Run all specified tests before marking a step as complete
- Handle errors gracefully - don't leave steps half-done
- Update the plan only after all execution is complete
- Reference the project's CLAUDE.md guidelines to ensure consistency
- Use Explore agent for codebase analysis if needed during execution
- Always ask clarifying questions if a step's instructions are ambiguous

## Implementation Details

### Parsing Action Plans

The action plan structure follows this format:
```
## Execution Plan

### Phase N: [Phase Name]
[Description of what this phase accomplishes]

1. **Step N.M**: [Specific action]
   - Deliverable: [What will be completed]
   - Testing: [How to verify]
   - Safe to merge: Yes/No

2. **Step N.M+1**: [Specific action]
   ...
```

Extract all phases and steps systematically so they can be presented to the user.

### TodoWrite Integration

Create todos with clear structure:
- Step name as content
- Status tracking (pending, in_progress, completed)
- Active form for present continuous (e.g., "Implementing user authentication")

Update the todo list:
- After each step completion
- To reflect execution progress
- To maintain visibility for the user

### Progress Communication

Keep the user informed:
- Show which step is currently executing
- Display step deliverables and testing approach
- Report test results
- Ask for confirmation before proceeding
- Summarize progress at key milestones
