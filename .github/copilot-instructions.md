# Coding guidelines
1. This application is a Gradle plugin for building Assembly projects for MOS 65xx family of microprocessors, using Kick Assembler as the only supported ASM dialect.
2. This application is implemented in Kotlin and uses Gradle as the build tool.
3. This application uses Hexagonal Architecture, with the domain layer containing the business logic and the adapters layer containing the glue code between the domain layer and specific technology, such as Gradle.
4. Top level directories denote bounded context, each having internal hexagonal structure.
5. There are dedicated at-hoc gradle plugins declared for each kind of module, all being located in the `buildSrc` folder.
6. Each module should have its own `build.gradle.kts` file, with the root `build.gradle.kts` file aggregating all modules and applying the necessary plugins.
7. There is an end user documentation stored in `doc` folder that is implemented in AsciiDoctor, keep it up to date with the code changes.
8. There is `CHANGES.adoc` file in the root of the project that contains the change log for the project, keep it up to date with the code changes.

# Testing guidelines
1. This application uses Kotest as testing library for unit and integration tests.
2. This application prefers using BDD style of testing, using Given/When/Then DSL of Kotest.

# General notes on working approach relevant for Agent mode
## Tools
1. We use Powershell so always use syntax of powershell when running commands. In particular do not use `&&`.
2. Use `gradle build` to quickly compile the client code
3. Use `gradle test` to run all tests in the client code
4. use `gradle spotlessApply` to format the code according to the coding style
5. always run `gradle spotlessApply` after creating or editing any source files to ensure the code is formatted correctly

## Prepare plan
Always use this approach when user asks in agent mode to create an action plan.
At the beginning of each task, prepare a plan for the task. If not specified in the user prompt explicitly, ask user for a feature name to name the plan MD file accordingly.


1. Identify Relevant Codebase Parts: Based on the issue description and project onboarding document, determine which parts of the codebase are most likely connected to this issue. List and number specific parts of the codebase mentioned in both documents. Explain your reasoning for each.
2. Hypothesize Root Cause: Based on the information gathered, list potential causes for the issue. Then, choose the most likely cause and explain your reasoning.
3. Identify Potential Contacts: List names or roles mentioned in the documents that might be helpful to contact for assistance with this issue. For each contact, explain why they would be valuable to consult.
4. Self-Reflection Questions: Generate a list of questions that should be asked to further investigate and understand the issue. Include both self-reflective questions and questions for others. Number each question as you write it.
5. Next Steps: Outline the next steps for addressing this issue, including specific actions for logging, debugging and documenting. Provide a clear, actionable plan. Number each step and provide a brief rationale for why it's necessary.

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
[List self-reflection questions and questions for others]

## Next Steps
[Provide a numbered list of actionable steps, including logging and debugging tasks]

## Additional Notes
[Any other relevant information or considerations]
Ensure that your action plan is comprehensive, follows a step-by-step approach, and is presented in an easy-to-read Markdown format. The final document should be named .ai/feature-{feature name}-action-plan.md
```
## Execute plan
1. When developer asks for complete plan execution, execute the plan step by step but stop and ask for confirmation before executing each step
2. When developer asks for single step execution, execute only that step
3. When developer asks additionally for some changes, update existing plan with the changes being made
