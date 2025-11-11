# Development Planning Guide

## Using the `/plan` Command

The `/plan` command is a Claude Code slash command that guides you through creating comprehensive development plans for features, bug fixes, and improvements.

### When to Use

Use `/plan` when you need to:
- Plan a new feature implementation
- Analyze and plan a bug fix
- Organize a complex refactoring task
- Break down a large initiative into phases
- Document architectural decisions
- Plan an incremental delivery strategy

### Quick Start

Simply invoke the command:
```bash
/plan
```

The AI agent will interactively guide you through the planning process.

### What Happens Next

1. **Information Collection**: You'll be asked for:
   - Issue number (e.g., "123", "GH-456")
   - Feature short name (URL-friendly, e.g., "image-compression")
   - Detailed task specification

2. **Codebase Analysis**: The agent will automatically:
   - Scan relevant code parts
   - Review architectural patterns
   - Examine similar implementations
   - Check project documentation

3. **Clarification**: If needed, the agent will ask follow-up questions to:
   - Resolve ambiguities
   - Validate assumptions
   - Confirm technical approach
   - Get team input on decisions

4. **Plan Generation**: The agent creates a comprehensive plan including:
   - Feature description and goals
   - Root cause analysis (for bugs)
   - Relevant codebase parts
   - Investigation questions with answers
   - Technical approach
   - Phased execution plan with deliverables
   - Success criteria and risks

### Output

The generated plan is saved to:
```
.ai/{ISSUE_NUMBER}-{FEATURE_SHORT_NAME}.md
```

### Plan Structure

Each plan includes:

| Section | Purpose |
|---------|---------|
| **Issue Description** | What is being done and why |
| **Root Cause Analysis** | Understanding of the problem/requirement |
| **Relevant Codebase Parts** | Files and modules affected |
| **Investigation Questions** | Self-reflection and team decisions |
| **Key Decisions Made** | Architectural choices and rationale |
| **Technical Approach** | Implementation strategy |
| **Execution Plan** | Phased approach with deliverables |
| **Success Criteria** | How to verify completion |
| **Risks & Mitigation** | Potential issues and solutions |

### Plan Characteristics

✅ **Incremental Delivery**: Each phase produces mergeabledeliverables
✅ **Clear Acceptance Criteria**: Every step has defined completion criteria
✅ **Architecture-Aligned**: Follows Hexagonal Architecture patterns from CLAUDE.md
✅ **Team-Focused**: Incorporates input from stakeholders
✅ **Well-Documented**: Detailed enough to execute without clarification
✅ **Risk-Aware**: Identifies and mitigates potential issues

### Example Plans

See existing plans in `.ai/` folder for reference:
- `feature-1.8.0-release-action-plan.md` - Documentation update plan
- `release-1.8.0.md` - Release plan
- Feature-specific plans in numbered directories

### Tips for Best Results

1. **Provide Complete Specifications**: Give the agent all context about the requirement
2. **Answer Clarifying Questions**: Help resolve any ambiguities quickly
3. **Review and Iterate**: If the plan needs adjustments, ask the agent to refine it
4. **Follow the Plan**: Use the generated plan as a step-by-step guide during implementation
5. **Update if Needed**: As circumstances change, update the plan accordingly

### Architecture Reference

Plans respect the project's architecture patterns:
- **Hexagonal Architecture**: Domain/Adapters/Ports separation
- **Module Organization**: Domain-driven organization with clear boundaries
- **Use Cases**: Kotlin classes with single `apply` method
- **Ports**: Technology-specific code hidden behind interfaces
- **Testing**: Standard JUnit/Kotlin test conventions
- **Gradle Integration**: Workers API for parallel execution

See `CLAUDE.md` for complete architectural guidance.
