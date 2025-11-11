# Claude Commands

This directory contains custom Claude Code slash commands for streamlining development tasks.

## Available Commands

### `/plan` - Comprehensive Development Plan Creator

Create detailed, actionable development plans for features, bug fixes, and improvements.

**Quick Start:**
```bash
/plan
```

**What happens:**
1. You provide: issue number, feature short name, and task specification
2. Agent analyzes codebase and reviews existing patterns
3. Agent asks clarifying questions (if needed)
4. Generates comprehensive plan at: `.ai/{ISSUE}-{FEATURE}.md`

**Output includes:**
- Issue description and goals
- Root cause analysis
- Relevant codebase parts (with file paths)
- Investigation questions with answers
- Technical approach and decisions
- Phased execution plan with incremental deliverables
- Success criteria and risk mitigation

**Key Features:**
- ✅ Normalized structure (consistent format)
- ✅ Incremental phases (safe to merge independently)
- ✅ Clear acceptance criteria per step
- ✅ Architecture-aligned (Hexagonal Architecture)
- ✅ Team decision documentation
- ✅ Risk awareness and mitigation

**Documentation:**
- See `.ai/PLANNING_GUIDE.md` for detailed user guide
- See `.ai/WORKFLOW_INTEGRATION.md` for lifecycle integration
- See `.ai/PLAN_REQUIREMENTS_MET.md` for requirements verification

## How to Use Commands

1. In Claude Code (Claude Code CLI), simply type the command:
   ```bash
   /plan
   ```

2. Follow the interactive prompts

3. Review the generated output in the specified location

## Adding New Commands

To add a new command:

1. Create a new file in `.claude/commands/` directory
2. Name it: `command-name.md`
3. Write detailed instructions that Claude will follow
4. Update this README with the new command

## Command Guidelines

When creating commands:
- **Be Specific**: Each step should be clear and actionable
- **Be Complete**: Include all context needed to execute
- **Be Consistent**: Follow the style of existing commands
- **Be Interactive**: Use tools like `AskUserQuestion` for user input
- **Be Automated**: Use appropriate agents (Explore, Task, etc.) for analysis

## Related Documentation

- `.ai/PLANNING_GUIDE.md` - User guide for planning
- `.ai/WORKFLOW_INTEGRATION.md` - Development lifecycle
- `.claude/COMMANDS_README.md` - Detailed commands reference
- `CLAUDE.md` - Project architecture guidelines
