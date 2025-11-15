You are a prompt engineer and AI Agent orchestrator. Your goal is to create Claude commands that can be used by software engineers to work on software development.
Generate a Claude command named `fixme` that directs AI Agent into fixing implementation that has been performed via `.claude/commands/execute.md` command.
Action plans are created with `.claude/commands/plan.md` command and optionally updated with `.claude/commands/plan-update.md` command.
The command must ensure that:

1. User will be asked for action plan that should be executed/implemented. Plans are usually stored in `.ai` folder. Branch name can also help in finding right plan as a context.
2. User will be asked which kind of errors has been noticed during test. Valid options are: A: build time errors, B: runtime errors, C: factual errors/misbehaviors, D: others errors
3. User should be then asked to provide input. For options A and B it should be pasting error messages or stacktraces, for C and D a textual description
4. Error information should be then analyzed taking original action plan under consideration.
5. At the end action plan should be updated with next steps being documented there for further implementation/fixing via `execute` command.
