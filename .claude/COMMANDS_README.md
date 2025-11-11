# Claude Commands Reference

This directory contains custom Claude Code slash commands for the gradle-retro-assembler-plugin project.

## Available Commands

### `/plan` - Development Plan Creator

**Purpose**: Create comprehensive, structured development plans for features, bug fixes, and improvements.

**How to Use**:
```bash
/plan
```

**What It Does**:
1. Asks you for issue number, feature short name, and task specification
2. Analyzes the codebase using the Explore agent
3. Reviews CLAUDE.md and existing plans for patterns and conventions
4. Asks clarifying questions if needed to resolve ambiguities
5. Generates a comprehensive, phased development plan
6. Saves the plan to `.ai/{ISSUE_NUMBER}-{FEATURE_SHORT_NAME}.md`

**Output Structure**:
- **Issue Description**: What needs to be done and why
- **Root Cause Analysis**: Understanding the problem or requirement
- **Relevant Codebase Parts**: Files and modules affected
- **Investigation Questions**: Self-reflection and team decisions
- **Key Decisions Made**: Architectural choices with rationale
- **Technical Approach**: Implementation strategy
- **Execution Plan**: Phased work with deliverables per phase
- **Success Criteria**: How to verify completion
- **Risks & Mitigation**: Potential issues and solutions

**Key Features**:
✅ Interactive information gathering
✅ Automatic codebase analysis
✅ Architecture-aligned planning (Hexagonal Architecture)
✅ Incremental delivery phases (safe to merge/release)
✅ Clear acceptance criteria per step
✅ Team decision documentation
✅ Stored plans available for reference

**When to Use**:
- Planning new features
- Analyzing complex bugs
- Organizing large refactoring tasks
- Breaking down initiatives into phases
- Documenting architectural decisions
- Planning incremental delivery strategies

**Example Workflow**:
```
1. Run: /plan
2. Provide: Issue #123
3. Provide: Feature name "cached-compilation"
4. Provide: Task description
5. Review: Clarifying questions from agent
6. Answer: Additional context questions
7. Get: Generated plan at .ai/123-cached-compilation.md
8. Execute: Follow the phased execution plan
```

## Directory Structure

```
.claude/commands/
├── plan.md                 # Development Plan Creator command
└── README.md              # This file

.ai/
├── PLANNING_GUIDE.md      # User guide for planning workflow
├── {ISSUE}-{NAME}.md      # Generated development plans
└── [other existing plans]
```

## Integration with Project

These commands follow the project conventions from `CLAUDE.md`:
- Respect Hexagonal Architecture patterns
- Follow module organization (domain/adapters/in-out)
- Use proper naming conventions (UseCase.kt, *Port, etc.)
- Consider Gradle build system integration
- Align with project coding standards and testing practices

## For More Information

- See `.ai/PLANNING_GUIDE.md` for detailed planning workflow
- Check `CLAUDE.md` for architectural and coding guidelines
- Review existing plans in `.ai/` folder for examples
- Examine recent commits for established patterns
