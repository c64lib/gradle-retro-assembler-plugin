---
name: release
description: Lead the maintainer through a full release of the Gradle Retro Assembler Plugin — choosing the next semantic version, generating release notes from develop's commit history, updating CHANGES.adoc, merging develop into master via a merge commit, and pushing the semver tag that triggers publication to the Gradle Plugin Portal. Invoke for "/release", "cut a release", "release the plugin", "prepare a new version".
allowed-tools: Agent Bash Read Grep Edit Write AskUserQuestion
---

# release

Guide the maintainer through cutting a release, end to end. This is an interactive, human-in-the-loop workflow: at every gate you present state and wait for explicit confirmation before taking an irreversible or outward-facing action (merge to master, push, tag).

## Release mechanics (how this project actually publishes)

Understand these before running the steps — they shape the whole flow:

- **The version is the git tag, not a file.** `build.gradle.kts` keeps a fixed `X.Y.Z-SNAPSHOT` dev version. At publish time the CI passes `-Ptag=<tag>` and the build uses the tag as the project version. **Do not bump the version in `build.gradle.kts`** as part of a release.
- **Tags trigger publication.** `.github/workflows/publish.yml` fires on pushed tags matching `[0-9]+.[0-9]+.[0-9]+` (and `-*` pre-release suffixes). Pushing the tag runs `./gradlew build publishPlugins` against the Gradle Plugin Portal. Pushing the tag is therefore the point of no return.
- **Tags live on `master`, not `develop`.** Releases are cut by merging `develop` into `master` via a merge commit (historically a PR from `c64lib/develop`), then tagging the merge commit on `master`.
- **Version tags are not visible on develop.** To find the previous release boundary, use the tag on `master` (or ask the maintainer for the last-release commit hash), not `git describe` on develop.

## Workflow

Work through these gates in order. Do not skip ahead; each gate depends on the previous one being confirmed.

### 1. Choose the release version

1. Read the current highest version: `git tag --sort=-creatordate | head -5` and the top entry of `CHANGES.adoc`.
2. Summarize what has landed on `develop` since that version (a quick `git log <last-tag-or-hash>..develop --oneline` count) so the maintainer can judge the semver bump.
3. Propose the next version using **semantic versioning** — recommend patch / minor / major based on whether the pending commits contain breaking changes, new features, or only fixes — and confirm with the maintainer via `AskUserQuestion`. Let them override.
4. Confirm the exact tag string (e.g. `1.8.1`). Note whether it is a pre-release (`-rc1`, etc.).

### 2. Determine the commit range

- The range is `<last-release-boundary>..develop`.
- Prefer the previous version tag as the boundary: `git log <prev-tag>..develop`.
- If tags aren't reachable from the current checkout, ask the maintainer for the last-release commit hash (they can find it with `git log`). The boundary is **exclusive** — commits after it are included.

### 3. Generate the release document

Produce a draft at `.ai/release-<version>.md` following the structure below. Pre-populate it from the commit range; leave `[DRAFT - User should complete]` markers where human judgment is needed.

- Extract every commit in `<boundary>..develop` (inclusive of develop tip).
- Parse each message for: affected component/domain (prefix or content), issue/PR references (`#NNN`), and change type (feature / fix / refactor / docs / infra).
- Organize into the category sections. Categorization is a *draft* — ask the maintainer to review and correct it.
- Auto-generate the contributor list from `git log <range> --format='%an'` (unique).

Document structure:

```markdown
# Release <version>

## Git Commit Summary
[Count of commits and brief summary of activity range]

Range: `<boundary>..develop`

## Commits by Category

### Features
### Bug Fixes & Improvements
### Refactoring & Infrastructure
### Documentation & Setup

## Full Commit List
[Table: hash | author | message]

---

## Overview
[DRAFT] What this release focuses on.

## Breaking Changes
[DRAFT] Incompatible changes users must know about.

## New Features
[DRAFT - extracted] Organized by component, with PR references.

## Improvements
[DRAFT - extracted]

## Bug Fixes
[DRAFT - extracted]

## Refactoring & Internal Changes
[DRAFT - extracted]

## Documentation Updates
[DRAFT]

## Dependency Updates
[DRAFT]

## Migration Guide (if applicable)
[DRAFT] For breaking changes only.

## Contributors
[Auto-generated] Unique authors.

## Release Notes for CHANGES.adoc
[DRAFT - ready to copy] Pre-formatted AsciiDoc entries.

### Format example
<version>::
* Component: Brief description of change (#PR)
* Another component: Another change (#PR)
```

Pause here and let the maintainer review categorization and complete the DRAFT sections before continuing.

### 4. Update CHANGES.adoc

Once the maintainer is happy with the release document:

- Insert a new `<version>::` block at the **top** of `CHANGES.adoc`, directly under `= Change log`, above the current highest version.
- Match the existing house style: `* Component: description (#PR)`, most significant changes first (features → improvements → fixes → infra → docs → quality). See the existing `1.8.0::` block as the template.
- Source the entries from the "Release Notes for CHANGES.adoc" section of the release document.
- Also update any relevant `.adoc` files under `doc/` that the release document's "Documentation Updates" / "Documentation References" sections flag.
- Commit these changes to `develop` (via git-utils / a normal commit) so they are part of what gets merged to master. Confirm the commit before pushing.

### 5. Merge develop into master

This is the first outward-affecting step — confirm explicitly before doing it.

Preferred (PR) path, matching project history:
1. Ensure `develop` is pushed and clean: `git status`, `git push origin develop`.
2. Open a PR from `develop` into `master` (use `gh-utils` / `gh pr create --base master --head develop`), titled for the release.
3. After CI is green and the maintainer approves, merge it with a **merge commit** (not squash/rebase) so history matches prior releases: `gh pr merge --merge`.

Direct path (only if the maintainer explicitly prefers no PR):
- `git checkout master && git pull && git merge --no-ff develop -m "Release <version>"` then confirm before pushing `master`.

Verify `master` now contains the release commits before tagging.

### 6. Tag to trigger publication

**Point of no return — pushing the tag publishes to the Gradle Plugin Portal.** Confirm one final time.

1. Check out the merge commit on `master`: `git checkout master && git pull`.
2. Create the tag on that commit: `git tag <version>` (annotated is fine: `git tag -a <version> -m "Release <version>"`).
3. Push it: `git push origin <version>`.
4. The `Publish` workflow (`.github/workflows/publish.yml`) will run `build publishPlugins -Ptag=<version>`.

### 7. Verify and wrap up

- Watch the `Publish` workflow run (`gh run watch` / point the maintainer at Actions) and confirm it succeeds.
- Confirm the new version appears at https://plugins.gradle.org/plugin/com.github.c64lib.retro-assembler .
- Optionally create a GitHub Release from the tag, using the release document as the body.
- Remind the maintainer that `master` and `develop` should now agree; no `build.gradle.kts` version bump is needed (the `-SNAPSHOT` dev version stays as-is until they choose to bump it for the next cycle).

## Guardrails

- Never push a tag or merge to master without explicit maintainer confirmation at that gate.
- Never edit the `-SNAPSHOT` version in `build.gradle.kts` as part of releasing.
- Never squash or rebase-merge `develop` into `master` — releases use merge commits.
- If tests/CI are red on `develop` or on the master PR, stop and surface it; do not tag a broken build.
- Delegate the actual `git`/`gh` invocations through `git-utils` / `gh-utils` where practical, consistent with the rest of the repo.

**Release document location**: `.ai/release-<version>.md`
