# Action Plan for Release 1.8.0 - Update Project Documentation

## Issue Description
Update the project documentation in the `doc` folder with descriptions of new features introduced in version 1.8.0, using the existing `.ai/release-1.8.0.md` as the basis for content.

## Key Decisions Made

- **Pipeline DSL Documentation**: Create as a separate, dedicated section marked as experimental (not replacing existing Processors section)
- **Functional Overview**: Add remark that Pipeline DSL is prototyped in experimental stage
- **Pipeline Steps**: Document GoatTracker and other pipeline steps (charpad, spritepad, png) independently in a dedicated section
- **Example Projects**: Will NOT be updated for this release (trex64, ctm-viewer)
- **Migration Guide**: NOT needed (no user-facing API changes)
- **Experimental Warnings**: All pipeline-related features must be marked as experimental/prototype

## Relevant Codebase Parts

1. **`doc/index.adoc`** - Main entry point for user documentation
   - Currently lists plugin version as 1.7.6 (line 12: `:actualPluginVersion: 1.7.6`)
   - Contains functional overview that covers assembling, processors, and CI capabilities
   - Needs version update and potential additions for Pipeline DSL feature

2. **`doc/concept/_02_overview.adoc`** - High-level project model overview
   - Describes retro assembler project model and data files
   - Mentions processors and conversion pipeline
   - Should include reference to new Pipeline DSL functionality

3. **`doc/concept/_03_processes.adoc`** - Process diagrams and descriptions
   - Covers simple assembling, dependency resolution, and resource conversion
   - Should be extended or referenced with the new Pipeline DSL execution model

4. **`doc/concept/_04_requirements.adoc`** - External dependencies documentation
   - Lists required external tools (Vice, Exomizer, GoatTracker 2)
   - Relevant for documenting GoatTracker processor feature

5. **`doc/concept/_10_architecture.adoc`** - Architecture documentation
   - Will need updates if internal refactoring details should be exposed to users

6. **`CHANGES.adoc`** - User-facing changelog file
   - Should be updated with release notes extracted from `.ai/release-1.8.0.md`
   - Contains the "Release notes for CHANGES.adoc" section with properly formatted entries

7. **`doc/kb/` folder** - Internal knowledge base documents
   - Contains architecture, adapters, domain, testing, commit guidelines, and Copilot instructions
   - Some of these may have been updated in this release but are internal-facing

## Root Cause Hypothesis

**Primary Hypothesis**: The release notes in `.ai/release-1.8.0.md` document significant new features (Pipeline DSL, processor enhancements, refactoring) that users need to be aware of, but these haven't yet been integrated into the user-facing documentation in the `doc/` folder. The version number is still outdated (1.7.6), and processor features (especially Pipeline DSL) need to be documented with examples for users to understand how to use them.

**Supporting Evidence**:
- Version number in `doc/index.adoc:12` is `1.7.6`, but release is `1.8.0`
- Release notes mention major features: Pipeline DSL, GoatTracker processor, PNG processor enhancements, SpritePad improvements
- Current documentation doesn't mention Pipeline DSL or the new pipeline-based processor configurations
- Several commits in the release relate to documentation improvements (architecture, adapters, domain structure, commit guidelines, Copilot)

## Investigation Questions

### Self-Reflection Questions

1. Which new features from 1.8.0 are user-facing vs. internal infrastructure changes?
2. Should Pipeline DSL be documented as a primary feature with examples, or is it primarily an internal concern?
3. Are there any breaking changes that require migration guides beyond what's in the release notes?
4. What level of detail should processor documentation have - quick start or comprehensive guide?
5. Should deprecated ExecuteStepPort be mentioned in user documentation, or only in internal/architecture docs?

### Questions for Others

1. **Should the Pipeline DSL section replace, enhance, or supplement the existing "Processors" section in `doc/index.adoc`?**
   - ✅ **ANSWERED**: Pipeline DSL should be a separate section marked as experimental, most likely a subject for change

2. **Do existing example projects (trex64, ctm-viewer) use the new Pipeline DSL features, and should they be updated?**
   - ✅ **ANSWERED**: They don't use Pipeline DSL features and won't be updated at this moment

3. **Should GoatTracker processor documentation be moved to a separate section, or remain part of the general Processors section?**
   - ✅ **ANSWERED**: GoatTracker processor should be documented independently, together with other pipeline steps

4. **Are there any user-facing API changes that require migration guide documentation?**
   - ✅ **ANSWERED**: No user-facing API changes require migration guide documentation

5. **Should the "Functional overview" section in `doc/index.adoc` be restructured to emphasize Pipeline DSL?**
   - ✅ **ANSWERED**: Only via remark that such feature is prototyped in experimental stage

## Next Steps

1. **Update version number in `doc/index.adoc`**
   - Change `:actualPluginVersion: 1.7.6` to `:actualPluginVersion: 1.8.0`
   - Verify all other version attributes are current

2. **Add Pipeline DSL as separate experimental section in `doc/index.adoc`**
   - Create new dedicated section for Pipeline DSL (NOT replacing Processors section)
   - Mark feature as experimental/prototype stage
   - Add remark in "Functional overview" section that Pipeline DSL is prototyped in experimental stage
   - Include basic examples of how to use pipelines for processor orchestration
   - Include warning about experimental feature status and potential for change

3. **Document Pipeline Steps independently** (separate from traditional Processors section)
   - Document GoatTracker step independently, together with other pipeline steps (charpad, spritepad, png)
   - Each step should have its own documentation entry with examples and parameters
   - Mark GoatTracker processor with experimental status warning

4. **Update "External dependencies" documentation**
   - Review `doc/concept/_04_requirements.adoc` to ensure GoatTracker and other tools are properly documented
   - Add any new version requirements

5. **Update `CHANGES.adoc` with release notes**
   - Copy release notes section from `.ai/release-1.8.0.md` (lines 145-165) into `CHANGES.adoc`
   - Ensure proper AsciiDoc formatting and structure
   - Add changelog entry dated for version 1.8.0
   - Note: No migration guide needed (no user-facing API changes)

6. **Review and integrate architecture documentation updates**
   - Verify that documentation added during this release (architecture, adapters, domain) is properly reflected in user-facing docs
   - Ensure internal knowledge base docs are not accidentally exposed to users

7. **Verify internal documentation consistency**
   - Ensure `doc/kb/architecture.md`, `doc/kb/adapters.md`, and `doc/kb/domain.md` are consistent with code changes
   - Update Copilot instructions if needed to reflect new patterns

8. **Test documentation builds**
   - Ensure all `.adoc` files render correctly with AsciiDoctor
   - Verify all cross-references and includes are valid
   - Check that generated HTML output looks correct

9. **Create summary validation document**
   - Verify all features from release notes are documented somewhere in `doc/`
   - Ensure no contradictions between release notes and documentation
   - Check for completeness of examples
   - Confirm example projects do NOT need updating for this release

## Additional Notes

- The release notes indicate this is a substantial release with 28 commits and significant feature additions
- The Pipeline DSL is flagged as important infrastructure that may need prominent documentation
- Several processor improvements suggest that processor documentation may be spread across multiple files
- Internal refactoring (use case to services renaming, ExecuteStepPort deprecation) should be reflected in `doc/kb/` but may not need user-facing documentation
- Consider whether example projects need updates for 1.8.0
- The release included documentation organization improvements that may affect how information is structured
- Some features are marked as "experimental" (GoatTracker), so appropriate warnings should be included
