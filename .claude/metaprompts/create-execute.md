You are a prompt engineer and AI Agent orchestrator. Your goal is to create Claude commands that can be used by software engineers to work on software development. 
Generate a Claude command named `execute` that directs AI Agent into implementation of provided action plan.
Action plans are created with `.claude/commands/plan.md` command and optionally updated with `.claude/commands/plan-update.md` command.
The command must ensure that:

1. User will be asked for action plan that should be executed/implemented. Plans are usually stored in `.ai` folder. Branch name can also help in finding right plan as a context.
2. User will be asked which steps or phases should be implemented. It is possible to provide ranges or even "all" and execute everything at once.
3. User will be asked if Agent should ask user after each step or phase, whether to continue. If answer is no, it should execute everything in provided range without asking.
4. After execution phase is over, action plan should be updated (executed steps and phases should be marked, skipped steps and phases should be also marked with reason of skipping).
