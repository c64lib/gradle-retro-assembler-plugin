You are an experienced software developer tasked with creating an action plan to address an issue. Your goal is to produce a comprehensive, step-by-step plan that will guide the resolution of this issue.

First, ask the user for:
- Issue number
- Issue name/title
- Issue description

Then create an action plan document following these steps:

1. **Identify Relevant Codebase Parts**: Based on the issue description and CLAUDE.md, determine which parts of the codebase are most likely connected to this issue. List and number specific parts of the codebase. Explain your reasoning for each.

2. **Hypothesize Root Cause**: Based on the information gathered, list potential causes for the issue. Then, choose the most likely cause and explain your reasoning.

3. **Identify Potential Contacts**: List names or roles that might be helpful to contact for assistance with this issue. For each contact, explain why they would be valuable to consult.

4. **Self-Reflection Questions**: Generate a list of questions that should be asked to further investigate and understand the issue. Include both self-reflective questions and questions for others. Number each question.

5. **Next Steps**: Outline the next steps for addressing this issue, including specific actions for logging and debugging. Provide a clear, actionable plan. Number each step and provide a brief rationale for why it's necessary.

After completing your analysis, create a Markdown document with the following structure:

```markdown
# Action Plan for [Issue Name]

## Issue Description
[Briefly summarize the issue]

## Relevant Codebase Parts
[List and briefly describe the relevant parts of the codebase]

## Root Cause Hypothesis
[State and explain your hypothesis]

## Investigation Questions

### Self-Reflection Questions
[List self-reflection questions]

### Questions for Others
[List questions for others]

## Next Steps
[Provide a numbered list of actionable steps, including logging and debugging tasks]

## Additional Notes
[Any other relevant information or considerations]
```

Save the final document as `.ai/feature-[issue-number]-[issue-name]-action-plan.md`.

Your final output should consist only of the Markdown document content and the creation of the file.
