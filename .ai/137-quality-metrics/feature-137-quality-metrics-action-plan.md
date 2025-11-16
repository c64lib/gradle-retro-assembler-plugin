# Feature: Quality Metrics Integration

**Issue**: #137
**Status**: Ready for Implementation (all decisions finalized)
**Created**: 2025-11-16
**Last Updated**: 2025-11-16

## 1. Feature Description

### Overview

Add comprehensive code quality metrics to the Gradle Retro Assembler Plugin build pipeline using industry-standard tools (Detekt for code analysis, Kover for code coverage, and JaCoCo test coverage). Integrate these tools into CircleCI to automatically generate and publish metric reports that can be displayed in the CircleCI UI and tracked over time.

### Requirements

- **Code Analysis**: Integrate Detekt for static code analysis (style violations, potential bugs, complexity)
- **Code Coverage**: Integrate Kover for Kotlin code coverage reporting
- **Test Coverage**: Generate test coverage reports using JaCoCo (industry standard for CircleCI integration)
- **Artifact Publishing**: Publish metric reports as build artifacts so CircleCI can display them
- **CI Integration**: Configure CircleCI to collect and display coverage reports
- **Build Configuration**: Add Gradle tasks for running quality checks and generating reports
- **Documentation**: Document the quality metrics setup in CLAUDE.md and/or README

### Success Criteria

- [ ] Detekt integrated and generates reports without breaking the build (warnings phase)
- [ ] Kover integrated and generates Kotlin coverage reports
- [ ] JaCoCo integrated and generates test coverage reports
- [ ] Quality reports published as build artifacts accessible in CircleCI
- [ ] CircleCI configured to collect and display coverage metrics
- [ ] All quality checks pass on `develop` and `master` branches
- [ ] Documentation updated with quality metrics guidelines
- [ ] Build time increase is minimal (< 10 seconds on typical CI runner)
- [ ] Quality metrics are tracked in CircleCI over time (visibility dashboard)

## 2. Root Cause Analysis

### Current State

The project currently:
- Uses Spotless for code formatting enforcement (style consistency)
- Runs full test suite with JUnit and Kotest
- Has 53 Gradle submodules with no unified quality metrics
- Aggregates test results to `build/test-results/gradle` directory
- Publishes to Gradle Plugin Portal without quality metrics visibility
- CircleCI job runs `./gradlew build test collectTestResults` but has no coverage/analysis metrics

### Desired State

The project should:
- Generate detailed code analysis reports (Detekt) showing potential issues
- Generate code coverage reports (Kover/JaCoCo) showing test coverage percentages and untested code
- Publish metric reports to CircleCI as artifacts
- Display coverage metrics in CircleCI UI for trend tracking
- Provide developers with actionable quality feedback
- Maintain quality standards across all 53 modules

### Gap Analysis

**What's missing:**
1. **Static Code Analysis**: No Detekt configuration or integration
2. **Code Coverage Measurement**: No JaCoCo or Kover configuration
3. **CircleCI Integration**: No artifact collection for metrics in CircleCI config
4. **Quality Gates**: No enforcement of quality standards or coverage thresholds
5. **Reporting**: No centralized quality metrics dashboard or reports
6. **Documentation**: No guidelines on quality expectations

## 3. Relevant Code Parts

### Existing Components

- **`.circleci/config.yml`**: CircleCI configuration with build, publish, and documentation jobs
  - Location: `.circleci/config.yml`
  - Purpose: Defines CI/CD pipeline
  - Integration Point: Must add artifact collection and coverage reporting steps

- **`build.gradle.kts`**: Root build file with `collectTestResults` task
  - Location: Root `build.gradle.kts`
  - Purpose: Project-wide Gradle configuration
  - Integration Point: Add quality plugin configuration and artifact publishing

- **`buildSrc/src/main/kotlin/rbt.kotlin.gradle.kts`**: Convention plugin applied to all modules
  - Location: `buildSrc/src/main/kotlin/rbt.kotlin.gradle.kts`
  - Purpose: Base Kotlin configuration for all modules
  - Integration Point: Apply Detekt and JaCoCo plugins to all modules via convention

- **`infra/gradle/build.gradle.kts`**: Main plugin definition with artifact publishing
  - Location: `infra/gradle/build.gradle.kts`
  - Purpose: Defines Gradle plugin and publishing configuration
  - Integration Point: Configure quality report artifact publishing

- **`gradle.properties`**: Version management for all dependencies
  - Location: `gradle.properties`
  - Purpose: Centralized version management
  - Integration Point: Add Detekt, Kover, JaCoCo version properties

### Architecture Alignment

This feature is **infrastructure/DevOps-focused** and does not fit into the hexagonal architecture domains (compilers, processors, flows, etc.). Instead, it affects the build infrastructure:

- **Build Infrastructure**: Modifies `buildSrc/` convention plugins and root Gradle configuration
- **CI/CD Pipeline**: Updates `.circleci/config.yml` to collect and publish metrics
- **No Domain Changes**: No changes to business logic or domain modules
- **No Use Cases**: No new use cases needed (purely build/infrastructure concern)
- **No Ports/Adapters**: No ports or adapters needed (infrastructure level)

### Dependencies

- **Detekt** (static code analysis):
  - Latest stable version (recommended: 1.23.x)
  - Plugin: `io.gitlab.arturbosch.detekt`
  - Purpose: Analyze Kotlin code for style violations, potential bugs, complexity issues
  - Dependency: Adds ~10-15 seconds to build time

- **Kover** (Kotlin code coverage):
  - Latest stable version (recommended: 0.7.x)
  - Plugin: `org.jetbrains.kotlinx.kover`
  - Purpose: Generate Kotlin-specific code coverage reports
  - Dependency: Integrates with JaCoCo under the hood
  - Alternative: JaCoCo alone, but Kover provides Kotlin-specific enhancements

- **JaCoCo** (Java code coverage):
  - Latest stable version (recommended: 0.8.x)
  - Plugin: `jacoco`
  - Purpose: Generate test coverage reports (industry standard for CircleCI)
  - Dependency: Required for CircleCI integration

- **CircleCI Configuration**:
  - `store_test_results` step (already exists)
  - `store_artifacts` step (needs configuration for coverage reports)
  - `codecov` orb (optional, for automatic coverage reporting to codecov.io)

## 4. Questions and Clarifications

### Self-Reflection Questions

Based on codebase analysis:

- **Q**: Should Detekt be enforced as a build failure or warning?
  - **A**: Recommendation: Run in warning mode initially (do not fail build), gradually increase strictness. This allows gradual adoption without blocking CI.

- **Q**: Should all 53 modules report coverage metrics?
  - **A**: Yes, but focus on domain modules and main plugin. Test utilities and infrastructure modules can have lower coverage thresholds.

- **Q**: How should coverage reports be aggregated across 53 modules?
  - **A**: Use Kover's built-in aggregation via `koverReport` task that combines coverage from all modules into a single report.

- **Q**: Should coverage metrics be published to external services (Codecov, SonarQube)?
  - **A**: CircleCI has built-in support for storing coverage reports. Optional: Codecov integration for trend tracking across PRs.

- **Q**: What coverage threshold should be enforced?
  - **A**: Recommend starting at 70% for initial implementation, gradually increasing to 80%+ for critical modules.

- **Q**: Should quality metrics be visible in GitHub PRs?
  - **A**: Possible via CircleCI's native PR reporting or external services like Codecov. Start with CircleCI dashboard.

### Unresolved Questions

None - all questions have been answered.

### Answered Questions

- [x] **Coverage Threshold**: What minimum code coverage percentage is acceptable?
  - **A**: 70% minimum for overall coverage, with differentiated thresholds by module type (see Coverage Targets below)

- [x] **Detekt Strictness**: Should Detekt violations fail the build immediately, or just be warnings initially?
  - **A**: Warnings only (recommended approach). Run in warning mode in Phase 1, gradually increase strictness over time in Phase 3.

- [x] **External Services**: Do you want to integrate with Codecov, SonarCloud, or keep metrics internal to CircleCI?
  - **A**: Keep metrics internal to CircleCI only for Phase 1. No external service integration at this time.

- [x] **Coverage Targets**: Should all modules have the same coverage threshold, or can infrastructure modules have lower thresholds?
  - **A**: Different by module type. Domain modules should aim for 70%+, infrastructure/test utility modules can have 50%+.

- [x] **PR Integration**: Should coverage reports be automatically posted to GitHub PRs for visibility?
  - **A**: No PR integration. Users will view coverage metrics through CircleCI artifacts only.

- [x] **Historical Tracking**: Do you want to track metrics over time in a dashboard (requires external service)?
  - **A**: Not required at this time. CircleCI artifacts provide sufficient tracking for Phase 1-2.

### Design Decisions

**All design decisions have been finalized based on user answers:**

- **Decision**: How to report coverage across 53 modules?
  - **Chosen**: Option A (Aggregate all modules into single report via Kover aggregation)
  - **Rationale**: Simpler to implement and understand, standard approach for multi-module projects. Domain and infrastructure modules will be combined with different threshold enforcement.

- **Decision**: Which tool for code coverage (JaCoCo vs Kover)?
  - **Chosen**: Both tools in phases - Phase 1: JaCoCo for CircleCI compatibility, Phase 2: Add Kover for Kotlin-specific insights
  - **Rationale**: JaCoCo is industry standard with built-in CircleCI support. Kover adds Kotlin-specific value without conflicts.

- **Decision**: Code analysis tool and enforcement level?
  - **Chosen**: Detekt only, in warning mode (as per user answer: "Warnings only")
  - **Rationale**: Aligns with project scope, avoids additional infrastructure. Phase 1 runs in warning mode, Phase 3 can gradually increase strictness.

- **Decision**: CircleCI integration approach?
  - **Chosen**: Option A - Store reports as artifacts only (CircleCI only, no external services per user answer)
  - **Rationale**: Provides immediate value with minimal complexity. User chose no Codecov/external service integration at this time.

- **Decision**: Coverage threshold enforcement?
  - **Chosen**: Different thresholds by module type (per user answer)
  - **Rationale**: Domain modules 70%+, infrastructure/test utilities 50%+. Balances quality with practical implementation needs.

- **Decision**: PR integration for coverage metrics?
  - **Chosen**: No PR integration (per user answer)
  - **Rationale**: Users will view metrics in CircleCI artifacts. Keeps implementation simple and doesn't require external services.

## 5. Implementation Plan

### Phase 1: Foundation - JaCoCo Test Coverage & Detekt Setup

**Goal**: Establish test coverage baseline and static code analysis without breaking the build.

**Deliverable**: Developers can run `./gradlew jacocoReport` to see test coverage, `./gradlew detekt` for code analysis, CircleCI collects coverage artifacts.

#### Step 1.1: Add JaCoCo and Detekt Dependencies

- **Files**:
  - `gradle.properties` - Add version properties
  - `buildSrc/src/main/kotlin/rbt.kotlin.gradle.kts` - Add plugin configuration

- **Description**:
  - Add `jacocoVersion` and `detektVersion` to `gradle.properties`
  - Apply `jacoco` plugin to convention plugin (all modules)
  - Apply `io.gitlab.arturbosch.detekt` plugin to convention plugin
  - Configure Detekt with baseline rules (warning mode, not errors)
  - Create Detekt configuration file (detekt.yml) with sensible defaults

- **Testing**:
  - Run `./gradlew clean build` - should succeed (no build failures from Detekt)
  - Run `./gradlew detekt` - should generate reports
  - Run `./gradlew jacocoReport` - should generate coverage reports

#### Step 1.2: Configure JaCoCo for Multi-Module Aggregation

- **Files**:
  - `build.gradle.kts` - Add JaCoCo aggregation task
  - `buildSrc/src/main/kotlin/rbt.kotlin.gradle.kts` - JaCoCo configuration per module

- **Description**:
  - Configure JaCoCo test report aggregation at root level (aggregates all module coverage)
  - Create `jacocoReport` task that combines coverage from all 53 modules
  - Generate HTML reports to `build/reports/jacoco/aggregated/`
  - Exclude test utilities and infra modules from coverage (they have infrastructure role)

- **Testing**:
  - Run `./gradlew jacocoReport` - generates aggregated HTML report
  - Open `build/reports/jacoco/aggregated/index.html` - should show combined coverage
  - Verify all domain modules are included, infrastructure modules are excluded

#### Step 1.3: Create Detekt Configuration

- **Files**:
  - `detekt.yml` - Detekt configuration file at root
  - `.detekt-baseline.xml` - Baseline file for existing violations (optional)

- **Description**:
  - Create sensible detekt.yml with baseline rules (not overly strict initially)
  - Set complexity rules (cognitive complexity < 15)
  - Set naming rules (standard Kotlin conventions)
  - Set potential bugs rules
  - Disable overly strict rules that would require extensive refactoring
  - Create baseline file to allow gradual improvement

- **Testing**:
  - Run `./gradlew detekt` - should complete without errors
  - Review generated reports in `build/reports/detekt/`
  - Verify baseline is created if violations exist

### Phase 2: CircleCI Integration & Kover Coverage

**Goal**: Make metrics visible in CircleCI and add Kotlin-specific coverage insights.

**Deliverable**: CircleCI displays coverage reports, developers see metrics on each build, JaCoCo reports stored as artifacts.

#### Step 2.1: Configure CircleCI to Collect Coverage Artifacts

- **Files**:
  - `.circleci/config.yml` - Add artifact collection steps

- **Description**:
  - Update CircleCI build job to store JaCoCo HTML reports as artifacts
  - Store Detekt HTML reports as artifacts
  - Use `store_artifacts` step to make reports downloadable in CircleCI UI
  - Configure artifact retention (30 days default)
  - Add links to reports in job artifacts section

- **Testing**:
  - Run build in CircleCI (or local with CircleCI config verification)
  - Verify artifacts appear in CircleCI UI under "Artifacts" tab
  - Click artifacts to verify reports are accessible

#### Step 2.2: Integrate Kover for Kotlin-Specific Coverage

- **Files**:
  - `gradle.properties` - Add Kover version
  - `buildSrc/src/main/kotlin/rbt.kotlin.gradle.kts` - Add Kover plugin
  - `build.gradle.kts` - Configure Kover aggregation

- **Description**:
  - Apply `org.jetbrains.kotlinx.kover` plugin to convention plugin
  - Configure Kover to generate HTML, XML, and JSON reports
  - Set up aggregation task for multi-module projects
  - Generate reports to `build/reports/kover/`
  - Verify Kover and JaCoCo coexist peacefully

- **Testing**:
  - Run `./gradlew koverHtmlReport` - generates Kotlin-specific coverage report
  - Open `build/reports/kover/` - should show detailed Kotlin coverage
  - Verify coverage percentages are reasonable (not 0% or 100%)
  - Run `./gradlew build` - should succeed with both tools

#### Step 2.3: Update CircleCI to Collect Kover Artifacts

- **Files**:
  - `.circleci/config.yml` - Add Kover artifact collection

- **Description**:
  - Add `store_artifacts` step for Kover reports
  - Store both HTML and XML Kover reports (XML for external tool integration)
  - Organize artifacts by report type in CircleCI UI

- **Testing**:
  - Run build in CircleCI
  - Verify Kover reports appear in artifacts alongside JaCoCo and Detekt

### Phase 3: Coverage Enforcement & External Integration

**Goal**: Enforce quality standards and optionally integrate with external metrics services.

**Deliverable**: Build fails if coverage drops below threshold, metrics visible in Codecov (optional), quality trends tracked.

#### Step 3.1: Add Coverage Threshold Enforcement

- **Files**:
  - `build.gradle.kts` - Add Kover verification rules
  - `buildSrc/src/main/kotlin/rbt.kotlin.gradle.kts` - Configure per-module thresholds

- **Description**:
  - Add Kover coverage verification with configurable thresholds
  - Set minimum coverage (e.g., 70% overall, higher for critical modules)
  - Create task to verify coverage before `check` task
  - Make coverage failure blocking for builds
  - Document coverage expectations for contributors

- **Testing**:
  - Run `./gradlew check` - should fail if coverage drops below threshold
  - Intentionally reduce test coverage, verify failure
  - Add tests to increase coverage above threshold, verify success

#### Step 3.2: (Optional for Future) - External Service Integration

- **Status**: Deferred - User chose not to integrate with external services (Codecov, SonarCloud) at this time
- **Notes**: This step can be added in a future phase if external service integration becomes desired
- **Future Considerations**:
  - Could add Codecov orb to CircleCI for automatic PR comments
  - Could upload coverage reports to Codecov.io or SonarCloud for trend tracking
  - Would require additional documentation for contributors
  - Not needed for Phase 1-2 implementation (CircleCI artifacts sufficient)

#### Step 3.3: Documentation & Guidelines

- **Files**:
  - `CLAUDE.md` - Add quality metrics section
  - `README.md` - Add links to quality reports
  - Create `QUALITY.md` - Comprehensive quality metrics guide (optional)

- **Description**:
  - Document how to run quality checks locally
  - Explain coverage thresholds and rationale
  - Document Detekt baseline philosophy
  - Add links to CircleCI artifacts
  - Provide guidelines for maintaining/improving coverage
  - Document how to interpret quality reports

- **Testing**:
  - Follow documentation steps as newcomer, verify accuracy
  - Verify all commands work as documented

## 6. Testing Strategy

### Unit Tests

- **Existing Tests**: All 53 modules already have JUnit 5 and Kotest tests
- **No New Tests**: Quality metrics tools don't require new test logic
- **Coverage Baseline**: Measure current coverage with JaCoCo/Kover
- **Expected Coverage**: Aim for 70-80% on domain modules, 50%+ on infrastructure

### Integration Tests

- **CircleCI Integration**: Manual testing in CircleCI pipeline
  - Verify artifacts are collected correctly
  - Verify reports are accessible
  - Verify artifact retention works

- **Multi-Module Aggregation**: Test coverage aggregation across all 53 modules
  - Run `./gradlew jacocoReport` on full project
  - Verify all modules are included
  - Verify no modules are double-counted

### Manual Testing

- [ ] Run `./gradlew detekt` locally and review violations
- [ ] Run `./gradlew jacocoReport` and review coverage gaps
- [ ] Run `./gradlew koverHtmlReport` and review Kotlin-specific coverage
- [ ] Run `./gradlew check` and verify all quality tasks pass
- [ ] Push to CircleCI and verify artifacts are collected
- [ ] Download artifacts from CircleCI and verify readability
- [ ] Add a new test to increase coverage, verify metrics improve
- [ ] Intentionally remove a test, verify metrics decrease

## 7. Risks and Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Build time increases significantly | High | Medium | Phase approach: Add JaCoCo first (baseline), measure impact, add Kover only if acceptable |
| Detekt fails build on existing violations | High | High | Use baseline/warning mode in Phase 1, gradually increase strictness in Phase 3 |
| Coverage metrics are inaccurate | Medium | Low | Use both JaCoCo and Kover for cross-validation, compare against IDE coverage |
| CircleCI quota exceeded by artifacts | Medium | Low | Configure artifact retention (30 days), compress reports if needed |
| Tools conflict or produce inconsistent results | Medium | Medium | Test both tools together in Phase 2, document any workarounds |
| Performance degradation on 53 modules | High | Medium | Implement incremental build support, exclude slow modules if needed, optimize configuration |
| Coverage metrics become outdated/stale | Medium | Medium | Integrate with CircleCI dashboard, set up alerts for coverage drops |

## 8. Documentation Updates

- [ ] Update `CLAUDE.md` to add "Quality Metrics" section documenting:
  - How to run Detekt, JaCoCo, and Kover locally
  - CircleCI integration details
  - Coverage thresholds and expectations
  - Links to reports in CircleCI

- [ ] Update project `README.md` to add:
  - Links to quality badges (if using Codecov)
  - Link to latest CircleCI artifacts
  - Coverage status overview

- [ ] Create `QUALITY.md` (optional but recommended):
  - Detailed quality metrics strategy
  - How to improve coverage
  - How to address Detekt violations
  - Guidelines for contributors

- [ ] Update contributing guidelines (if exists):
  - Note about quality metrics in CI
  - Links to improvement resources

## 9. Rollout Plan

### Safe Rollout Strategy

1. **Phase 1 Release** (JaCoCo + Detekt baseline):
   - Merge to `develop` branch first
   - CircleCI green on develop
   - No build failures from Detekt (baseline mode)
   - Allows developers to see metrics without blocking

2. **Stabilization Period** (1-2 weeks):
   - Monitor CircleCI artifacts
   - Address obvious Detekt violations if needed
   - Let coverage baseline settle
   - Gather team feedback

3. **Phase 2 Release** (Kover + CircleCI artifacts):
   - Merge to `develop`
   - Verify Kover reports are generated correctly
   - CircleCI shows all reports as artifacts

4. **Phase 3 Release** (Coverage enforcement + Documentation):
   - Merge to `develop` first
   - Enable coverage thresholds (70% for domain modules, 50% for infrastructure)
   - Address coverage gaps before merging to `master`
   - Document guidelines for contributors
   - No external service integration (kept internal to CircleCI as per user preference)

### Monitoring & Rollback

- **What to Monitor**:
  - CircleCI build times (alert if > 20% increase)
  - Build success rate (should remain > 95%)
  - Artifact sizes (should be < 50MB total)
  - Coverage trend (should be stable or increasing)

- **Rollback Strategy**:
  - Phase 1: Remove JaCoCo/Detekt plugins from convention plugin, remove CircleCI artifact steps
  - Phase 2: Remove Kover plugin, revert CircleCI changes
  - Phase 3: Disable coverage thresholds if blocking too many builds

---

## Next Steps

**Plan is now complete and ready for implementation!** All questions have been answered and all design decisions finalized.

### Recommended Approach:
1. **Phase 1 Implementation**: Begin with JaCoCo + Detekt setup (Steps 1.1-1.3)
   - Estimated effort: 2-3 hours
   - Minimal risk (warnings-only mode)
   - Provides immediate value (baseline metrics)

2. **Phase 2 Implementation**: Add Kover integration and CircleCI artifacts
   - Estimated effort: 2-3 hours
   - Low risk (additive, no enforcement yet)
   - Adds Kotlin-specific insights

3. **Phase 3 Implementation**: Enable coverage enforcement and documentation
   - Estimated effort: 1-2 hours
   - Moderate risk (may block builds if coverage is low)
   - Adds quality gates and documentation

### Key Decisions Made:
✅ **Coverage Threshold**: 70% overall (domain modules), 50% (infrastructure modules)
✅ **Detekt Mode**: Warnings only (Phase 1), gradual strictness increase (Phase 3)
✅ **Integration**: CircleCI artifacts only (no Codecov/external services)
✅ **PR Reporting**: No automatic PR integration
✅ **Tool Choice**: JaCoCo Phase 1 + Kover Phase 2

**Status**: Ready for Phase 1 implementation

---

## 10. Revision History

| Date | Updated By | Changes |
|------|------------|---------|
| 2025-11-16 | Claude (plan-update) | Answered all 6 unresolved questions. Finalized all design decisions. Clarified: 70% coverage threshold (with 50% for infra modules), Detekt warnings-only mode, CircleCI-only integration (no Codecov), no PR integration, module-specific thresholds. Updated Phase 3 and Rollout Plan to reflect user preferences. |

---

**Note**: This plan follows hexagonal architecture principles by treating quality metrics as infrastructure concerns (not affecting domain logic). All changes are in `buildSrc/`, `.circleci/`, and root build configuration - no domain modules are modified.
