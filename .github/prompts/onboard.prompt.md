---
mode: 'agent'
description: 'Generate project onboarding document from provided codebase, GIT history, and other relevant files.'
tools: ['read_file', 'edit_file', 'codebase', 'search', 'searchResults', 'changes', 'editFiles', 'run_in_terminal', 'runCommands', 'runTests', 'findTestFiles', 'testFailure', 'git']
---

You are an AI assistant tasked with onboarding a new developer to a big project. Your goal is to analyze the provided git history and top modules/components to create a comprehensive onboarding summary. This summary should help the new developer quickly understand the project structure, recent developments, and key areas of focus, regardless of the underlying technology stack.

First, review the following information:

<top_modules>
{{top-modules}} - use GIT to get the most frequently edited modules/packages/directories in the project; example: 
`git log --format=format: --name-only | Where-Object { $_ -ne "" } | ForEach-Object { Split-Path -Path $_ -Parent | Where-Object { $_ -ne "" } } | Group-Object | Sort-Object -Property Count -Descending | Select-Object -First 10 | Format-Table -Property Name, Count -AutoSize`
</top_modules>

<top_files>
{{top-files}} - use GIT to get the most frequently edited files in the project; example:
`git log --since="1 year ago" --format=format: --name-only | Where-Object { $_ -ne "" } | Group-Object | Sort-Object -Property Count -Descending | Select-Object -First 20 | Format-Table -Property Name, Count -AutoSize`
</top_files>

<top_contributors>
{{top-contributors}} - use GIT to get the most active contributors in the project; example:
`git log --format='%an' | Group-Object | Sort-Object -Property Count -Descending | Select-Object -First 10 | Format-Table -Property Name, Count -AutoSize`
</top_contributors>

Analyze the git history and top modules/files to identify:
1. The main areas of development focus in the past year.
2. Frequently updated modules, directories, or core files.
3. Any significant refactoring or architectural changes indicated by commit patterns.

Based on your analysis, create an onboarding summary.

**You MUST use available tools (like file reading and search) to actively find the specific information needed for the sections below within the repository. Avoid placeholders unless the information cannot be located in standard project documentation or configuration files.**

**Specifically for 'Core Modules':**
*   When describing 'Key Files/Areas' and 'Top Contributed Files' for each module/package/directory, attempt to verify file paths and roles by listing directory contents or reading key files, correlating with the provided `<top_files>` data.

**Specifically for 'Development Environment Setup':**
*   Search for and read primary project documentation files like `README` (e.g., `README.md`, `README.rst`), `CONTRIBUTING`, or installation/setup guides (check common locations like the root directory, `.github/`, or `docs/`).
*   Identify the project's primary build/dependency management configuration file (e.g., `package.json`, `pom.xml`, `build.gradle`, `requirements.txt`, `Pipfile`, `go.mod`, `Cargo.toml`, `composer.json`, etc.). Read this file if necessary.
*   Extract actual commands for installing dependencies, building the project, running the application/server, and running tests. Look for these in build scripts (like a `Makefile`, `scripts` section in `package.json`, specific build tool files) or instructions within the documentation files.
*   Identify prerequisites (like required language runtime versions, compilers, specific tools, environment variables) mentioned in these documents.
*   If specific commands or prerequisites are not found after checking these common locations, explicitly state that (e.g., "Dependency installation command not found in checked files.").

**Specifically for 'Helpful Resources':**
*   Search for and read `README`, `CONTRIBUTING`, and other documentation files (potentially in a `/docs` or similar directory if `list_dir` shows one exists).
*   Extract actual URLs for external documentation websites, the project's issue tracker (e.g., GitHub Issues, Jira), contribution guidelines, communication channels (like Discord, Slack, mailing list links), etc., mentioned in these files.
*   If specific links are not found, explicitly state that (e.g., "Link to communication channel not found in checked files.").

To accomplish this task, you have access to the following tools:
1. file_read: Reads the content of a specified file.
2. file_search: Searches for files matching a given pattern.
3. list_dir: Lists the contents of a specified directory.

Generate a list of 5-7 questions that the new developer should dig into via analysing the codebase, git history and project repository (e.g., on GitHub, GitLab) to gain a deeper understanding of the project. These questions should be based on your analysis and address any ambiguities or areas that require further clarification.

Suggest 3-5 next steps for the new developer to get a deeper understanding of the project via codebase, git history and project repository. These steps should be practical and actionable, based on the information you've analyzed.

Your final output should **only** include the content in markdown with the specified format that you will save in `.ai/onboarding.md`, without any additional commentary or explanations outside of these sections. Structure the output as follows, **filling it with information discovered from the repository**:

```markdown
# Project Onboarding: [Project Name Found in Repo, e.g., from README or build config]
 
## Welcome
 
Welcome to the [Project Name Found in Repo] project! [Brief 1-2 sentence description of what the project does and its purpose, potentially summarized from README].
 
## Project Overview & Structure
 
The core functionality revolves around [key functionality description, inferred from README/context]. The project is organized as [monorepo/multi-project/single application/etc.], with the following key components/modules:
 
## Core Modules
 
### `[Module/Package/Directory Name]`
 
- **Role:** [Concise description of this component's purpose]
- **Key Files/Areas:** 
  - [Category/Group Name]: `path/to/file1`, `path/to/file2`, etc.
  - [Category/Group Name]: `path/to/file3`, `path/to/file4`, etc.
- **Top Contributed Files:** `path/to/filename`, `path/to/filename`, etc.  
- **Recent Focus:** [Description of recent work, features, or bug fixes in this area with issue/PR references if possible to infer]
 
[Repeat for each major module/package/directory based on the top_modules data]
 
## Key Contributors
 
- **[Contributor Name]:** [Areas of focus or expertise, key contributions based on available data]
- **[Contributor Name]:** [Areas of focus or expertise, key contributions based on available data]
[List top 3-5 contributors]
 
## Overall Takeaways & Recent Focus
 
1. **[Major Theme/Initiative]:** [Description of significant recent project-wide change based on active modules/files]
2. **Feature Development:** [Description of recent major features added to the project, inferred from active areas]
3. **[Specific Area] Improvements:** [Details on focused improvements in a particular active area]
4. **UI/UX Refinement (if applicable):** [Recent UI/UX changes and improvements, inferred from relevant file activity]
5. **Performance & Stability:** [Recent performance optimizations and stability improvements, inferred from core logic/testing activity]
 
## Potential Complexity/Areas to Note
 
- **[Complex Area]:** [Description of why this area might be complex (e.g., core domain logic, concurrency, state management, external system integration) and what to watch out for]
- **[Complex Area]:** [Description of why this area might be complex and what to watch out for]
- **[Complex Area]:** [Description of why this area might be complex and what to watch out for]
 
## Questions for the Team
 
1. [Question about project structure, architecture, or key design decisions]
2. [Question about build process, deployment, or development workflows inferred from setup/docs]
3. [Question about specific complex areas identified]
4. [Question about data persistence, state management, or inter-service communication patterns]
5. [Question about contributing changes to active modules or adding new features]
6. [Question about testing strategy, code quality standards, or CI/CD pipeline]
7. [Question about collaboration, code ownership, or release process]
 
## Next Steps
 
1. **[Action Item]:** [Specific details, e.g., Set up the development environment using instructions found in README]
2. **[Action Item]:** [Specific details, e.g., Explore the `[highly_active_module/directory]` identified as highly active]
3. **[Action Item]:** [Specific details, e.g., Run the project's test suite using the command found in the build configuration/documentation]
4. **[Action Item]:** [Specific details, e.g., Trace a core business logic flow related to `[frequently_edited_core_file]`]
5. **[Action Item]:** [Specific details, e.g., Review recent Pull Requests/Merge Requests related to `[another_active_module]`]
 
## Development Environment Setup
 
1. **Prerequisites:** [Actual prerequisites found, e.g., Language Runtime version X.Y, Compiler Z, Tool A]
2. **Dependency Installation:** `[Actual command(s) found, or "Command not specified"]`
3. **Building the Project (if applicable):** `[Actual command(s) found, or "Command not specified"]`
4. **Running the Application/Service:** `[Actual command(s) found, or "Command not specified"]`
5. **Running Tests:** `[Actual command(s) found, or "Command not specified"]`
6. **Common Issues:** [Summarize common setup issues mentioned in docs, or state "Common issues section not found in checked files"]
 
## Helpful Resources
 
- **Documentation:** [Actual link(s) found, or state "Primary documentation link not found"]
- **Issue Tracker:** [Actual link(s) found, e.g., GitHub Issues URL, Jira Project URL]
- **Contribution Guide:** [Actual link(s) found]
- **Communication Channels:** [Actual link(s) found, e.g., Slack invite, mailing list archive]
- **Learning Resources:** [Actual link(s) found, or state "Specific learning resources section not found"]
```
 
Ensure that all information in the summary is based on <top_modules>, <top_files>, <top_contributors> and your exploration of the project using the provided tools. If you cannot find specific information, indicate that it was not found in the checked files.
 
Your final output should consist only of the markdown-formatted onboarding summary that you will save in .ai/onboarding.md and should not duplicate or rehash any of the work you did in the exploration section of the thinking block. Finish the work after you created document with a required structure and content.
 
Begin your response with your exploration.