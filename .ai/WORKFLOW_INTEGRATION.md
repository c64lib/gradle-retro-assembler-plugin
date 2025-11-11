# Development Workflow Integration

## Complete Development Lifecycle with `/plan` Command

This document shows how the `/plan` command integrates into your complete development workflow.

## Phase 1: Planning (Using `/plan`)

### Step 1: Initiate Planning
```bash
/plan
```

### Step 2: Provide Input
The command asks for:
- **Issue Number**: GitHub issue ID (e.g., "156")
- **Feature Short Name**: URL-friendly name (e.g., "parallel-builds")
- **Task Specification**: Detailed description of requirements

### Step 3: Automatic Analysis
Agent automatically:
- Scans the codebase for relevant files
- Reviews architectural patterns in CLAUDE.md
- Examines similar implementations
- Checks existing plans in `.ai/` folder

### Step 4: Resolve Ambiguities
If needed, agent asks clarifying questions about:
- Scope and boundaries
- Technical approach
- Integration points
- Priority and constraints

### Step 5: Generated Plan
Agent creates: `.ai/{ISSUE_NUMBER}-{FEATURE_SHORT_NAME}.md`

Contains:
- Issue description and success criteria
- Root cause analysis
- Relevant codebase parts
- Investigation and decisions
- Technical approach
- **Phased execution plan** with deliverables

## Phase 2: Implementation (Following the Plan)

### For Each Phase:

1. **Review Phase Objectives**
   - Understand what this phase accomplishes
   - Check deliverables and acceptance criteria
   - Verify dependencies are met

2. **Execute Steps**
   - Follow the numbered steps in sequence
   - Create git branch for the phase
   - Implement according to architectural guidelines

3. **Test and Validate**
   - Run tests specified in the plan
   - Verify acceptance criteria are met
   - Check for regressions

4. **Code Review**
   - Follow project code review guidelines from CLAUDE.md
   - Ensure architectural patterns are followed
   - Verify testing is complete

5. **Merge to Branch**
   - Merge phase work into develop branch
   - Each phase is independently mergeable
   - Safe to release at phase boundaries

## Phase 3: Execution Throughout Project

### During Implementation:
- Reference the plan file as primary guide
- Update plan if new information emerges
- Track progress against phases
- Mark completed steps/phases

### For Team Communication:
- Share the plan with stakeholders
- Use it in code review discussions
- Reference in pull request descriptions
- Use as basis for status updates

### Quality Gates:
- Each phase should have passing tests
- No regressions from previous phases
- Code follows project conventions
- Documentation is updated

## Integration Points

### With CLAUDE.md:
- Plans respect Hexagonal Architecture patterns
- Follow domain/adapters/in-out structure
- Use proper naming conventions (UseCase.kt, *Port, etc.)
- Consider Gradle Workers API for parallel execution

### With Git Workflow:
- Each phase can be a separate commit or PR
- Incremental merging reduces risk
- Clear commit messages from plan phases
- Rollback points at phase boundaries

### With Testing:
- Each phase includes testing strategy
- JUnit/Kotlin test conventions
- Test files mirror main source structure
- Run: `./gradlew :module:test`

### With CI/CD:
- Each phase is independently buildable
- Full build: `./gradlew build`
- Tests run automatically
- Incremental releases possible

## Example Workflow

### Scenario: Add New Processor

```
1. Run: /plan
   ├─ Issue: "156"
   ├─ Name: "audio-processor"
   └─ Spec: "Add support for WAV file processing..."

2. Agent generates: .ai/156-audio-processor.md
   ├─ Phase 1: Create domain model
   ├─ Phase 2: Implement use case
   ├─ Phase 3: Create ports/interfaces
   ├─ Phase 4: Build adapters
   └─ Phase 5: Tests and integration

3. For Phase 1 (Domain Model):
   ├─ Create src/domain/AudioFile.kt
   ├─ Create src/domain/AudioConfig.kt
   ├─ Add unit tests
   ├─ Commit: "Add AudioFile domain models for processor"
   └─ Merge to develop

4. For Phase 2 (Use Case):
   ├─ Create src/usecase/ProcessAudioUseCase.kt
   ├─ Implement business logic
   ├─ Add integration tests
   ├─ Commit: "Implement ProcessAudioUseCase"
   └─ Merge to develop

5. Continue through all phases...

6. Final: All phases merged, feature complete and releasable
```

## Plan File Structure Reference

Your generated plans contain:

| Section | Usage |
|---------|-------|
| **Issue Description** | Reference during implementation to stay focused |
| **Root Cause Analysis** | Understand the "why" before starting |
| **Codebase Parts** | Know what files you'll touch |
| **Investigation Q&A** | Design decisions already made (no need to reconsider) |
| **Technical Approach** | Overview of how to implement |
| **Execution Plan** | Step-by-step guide for each phase |
| **Success Criteria** | How to know when you're done |
| **Risks** | Anticipate and avoid potential issues |

## Collaboration Patterns

### When Working with Others:

1. **Share the plan** early in development
2. **Use it as review guide** for code reviewers
3. **Reference phases** in PR descriptions
4. **Update plan** if requirements change
5. **Use Q&A sections** for team decisions

### In Code Reviews:
- Reference phase deliverables
- Check against acceptance criteria
- Verify architectural compliance
- Confirm tests match plan
- Look for scope creep beyond plan

### In Status Updates:
- Report which phase is complete
- Identify blockers early
- Reference risks from plan
- Show incremental progress

## Files and Commands Reference

```
Commands:
  /plan                      # Initiate planning process

Generated Files:
  .ai/{ISSUE}-{NAME}.md     # Comprehensive development plan

Reference Guides:
  CLAUDE.md                 # Architecture and conventions
  .ai/PLANNING_GUIDE.md     # Planning workflow guide
  .claude/COMMANDS_README.md # Commands reference
  .ai/WORKFLOW_INTEGRATION.md # This file

Example Plans:
  .ai/feature-1.8.0-release-action-plan.md
  .ai/release-1.8.0.md
  And others in .ai/ directory
```

## Best Practices

✅ **Do**:
- Use `/plan` for significant work
- Follow phases in order
- Keep plan updated as situation evolves
- Reference plan in code reviews and PRs
- Use plan's success criteria as acceptance gate
- Archive completed plans as reference

❌ **Don't**:
- Skip the planning phase for complex work
- Deviate from plan without updating it
- Merge incomplete phases
- Ignore plan's risk sections
- Skip testing phases
- Forget to update documentation per plan

## Summary

The `/plan` command creates a foundation for successful development:
1. **Clarity**: Everyone knows what needs to be done
2. **Incremental Delivery**: Phases can be merged independently
3. **Quality**: Each phase has clear acceptance criteria
4. **Safety**: Risks are identified and mitigated
5. **Collaboration**: Clear communication framework
6. **Traceability**: Plan documents all decisions

Use `/plan` as your primary tool for organizing complex development work!
