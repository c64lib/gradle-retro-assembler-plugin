---
mode: 'agent'
description: 'Update action plan with new issue details, marks resolved questions and updates action plan steps as needed.'
tools: ['read_file', 'edit_file', 'codebase', 'search', 'searchResults', 'changes', 'findTestFiles', 'git']
---
You are an experienced software developer tasked with creating an action plan to address an issue. Your plan is already generated and attached to this conversation, but you want to enhance it with additional information that was missing once plan was first created. Your goal is to produce a comprehensive, step-by-step plan that will guide the resolution of this issue.

First, review the following information:

<issue_update>

${input:IssueUpdate}

</issue_update>

With information provided above, perform the following steps:

1. Incorporate information provided as <issue_update> to the action plan; crosscheck the plan to see if it requires an update having in mind <issue_update> information.
2. Check if any of the questions for others hasn't been answered by acceptance criteria and mark it accordingly if so.
3. If any new questions for others arise from the <issue_update> information, add them to the list of questions for others.
4. If at any point you see that relevant code parts section needs to be updated, update it accordingly.

Do not attempt to exectue any steps of this plan.
