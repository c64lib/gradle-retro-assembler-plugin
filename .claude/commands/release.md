# Release Command

This command prepares release documentation for a new version of the Gradle Retro Assembler Plugin.

## Instructions

1. Ask the user for the release version using semantic versioning (e.g., 1.7.7, 1.8.0, 2.0.0)
2. Find the last version tag to determine the commit range
3. Extract commits from `develop` branch since the last tag using `git log <last-tag>..develop`
4. Generate a draft release document at `.ai/release-<version>.md` with:
   - Structured markdown template
   - Pre-populated commit history organized by category
5. Ask the user to review and refine the categorization
6. Note that this document will serve as:
   - Source for generating release notes in `CHANGES.adoc`
   - Source for updating ADOC documentation in `doc/` directory

## Commit Processing

- Extract all commits between last version tag and develop branch
- Parse commit messages to identify:
  - Component/domain affected (from commit message prefix or content)
  - Issue/PR references (e.g., #122, #121)
  - Type of change (feature, fix, refactor, docs, etc.)
- Organize commits into draft sections (may need user refinement)

## Document Structure

The release document will use the following markdown structure:

```markdown
# Release <version>

## Git Commit Summary
[Count of commits and brief summary of activity range]

Range: `<last-tag>..develop`

## Commits by Category

### Features
- [Feature commits with PR numbers if available]

### Bug Fixes & Improvements
- [Bug fix and improvement commits with PR numbers if available]

### Refactoring & Infrastructure
- [Refactoring commits]

### Documentation & Setup
- [Documentation and setup commits]

## Full Commit List
[Table with all commits: hash, author, message for reference]

---

## Overview
[DRAFT - User should complete] Brief description of what this release focuses on - new features, improvements, bug fixes

## Breaking Changes
[DRAFT - User should complete] Any incompatible changes users need to know about

## New Features
[DRAFT - Extracted from commits] Organized by component/area with PR references

## Improvements
[DRAFT - Extracted from commits] Performance and UX enhancements

## Bug Fixes
[DRAFT - Extracted from commits] Issues resolved with references

## Refactoring & Internal Changes
[DRAFT - Extracted from commits] Non-user-facing improvements

## Documentation Updates
[DRAFT - User should complete] List of documentation files that were updated

## Dependency Updates
[DRAFT - User should complete] Any dependency version changes

## Migration Guide (if applicable)
[DRAFT - User should complete] For breaking changes only

## Contributors
[Auto-generated from commits] List of unique authors

## Release Notes for CHANGES.adoc
[DRAFT - Ready to copy] Pre-formatted AsciiDoc entries

### Format example:
```
<version>::
* Component: Brief description of change
* Another component: Another change description
```

## Documentation References
[DRAFT - User should complete] List of .adoc files in doc/ that need updates
```

## Next Steps

After creating the document:
1. User reviews git commit categorization
2. User completes DRAFT sections (Overview, Breaking Changes, Documentation Updates, etc.)
3. Extract entries from "Release Notes for CHANGES.adoc" section into CHANGES.adoc
4. Update relevant documentation files in doc/ directory based on "Documentation References" section
5. Use the document as a basis for creating official release notes

---
**Location**: `.ai/release-<version>.md`
