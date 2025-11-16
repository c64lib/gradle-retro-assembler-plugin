# Feature: Improve Code Coverage by Unit Tests

**Issue**: #142
**Status**: Planning
**Created**: 2025-11-16

## 1. Feature Description

### Overview
Improve code coverage for the gradle-retro-assembler-plugin project by systematically analyzing JaCoCo coverage reports and creating unit tests for the 25 least-covered classes/files. This is a critical quality initiative to increase code reliability and maintainability across the project.

### Requirements
- Run JaCoCo coverage report using `./gradlew test jacocoReport`
- Identify top 25 least-covered classes/files across all modules
- Create comprehensive unit tests for these classes
- Increase overall project coverage from current baseline toward 70% target
- Follow existing BDD testing patterns (Kotest BehaviorSpec)
- Ensure tests are isolated, focused, and maintainable

### Success Criteria
- JaCoCo aggregated report successfully generated
- All 25 identified classes have unit tests created/extended
- Line coverage for targeted classes increased to minimum 50%
- Branch coverage for targeted classes increased to minimum 30%
- All new tests follow project conventions (BehaviorSpec, Given/When/Then)
- Build and tests pass successfully
- Overall project coverage improvement measurable

## 2. Root Cause Analysis

### Current State
**Coverage Analysis Results** (as of latest test run):

Current coverage by module tier:
- **Critical Gaps (0% coverage)**:
  - flows/adapters/in/gradle (FlowTasksGenerator, all Task classes, DSL builders)
  - shared/gradle (all 24 extension classes)
  - flows/domain (DasmStep, GoattrackerStep, ImageStep, config mappers)

- **Low Coverage (1-30%)**:
  - shared/binary-utils (28%) - core byte manipulation utilities
  - processors/spritepad (27%) - SpritePad format processing
  - emulators/vice/adapters/out/gradle (34%)

- **Good Coverage (70%+)**:
  - processors/charpad (75%)
  - processors/image/adapters/out/png (86%)
  - crunchers/exomizer (70%)

### Desired State
- All top 25 least-covered classes have meaningful test coverage (50%+ line, 30%+ branch)
- Flows infrastructure (task execution, DSL building) fully tested
- Gradle plugin extensions (DSL API surface) integration-tested
- Overall project reaches minimum 70% coverage across all modules
- Configuration and step domain logic comprehensively tested
- Binary utilities and shared domain classes well-tested

### Gap Analysis
**What needs to change:**

1. **Flows Subdomain Infrastructure** (8-10 classes)
   - Add integration tests for FlowTasksGenerator (task graph creation)
   - Test all task classes (CommandTask, AssembleTask, ImageTask, etc.)
   - Test DSL builders and path resolution
   - Test flow orchestration and step execution

2. **Gradle Plugin Extensions** (6-8 classes)
   - Test all DSL extension classes (RetroAssemblerPluginExtension, OutputsExtension, etc.)
   - Test property binding and configuration
   - Test DSL builder convenience methods

3. **Domain Configuration** (4-6 classes)
   - Test DasmConfigMapper and step configuration mapping
   - Test config data classes validation
   - Test configuration inheritance and defaults

4. **Format Processors** (2-3 classes)
   - Add tests for CTM9Processor (CharPad format 9)
   - Add tests for SPD4Header (SpritePad format 4)
   - Test edge cases in format parsing

5. **Shared Utilities** (3-5 classes)
   - Extend byte-utils coverage (ByteUtilsKt, ByteArrayExtensionsKt)
   - Test source domain model classes
   - Test edge cases in binary operations

## 3. Relevant Code Parts

### Existing Components

#### Flows Domain (flows/src/main/kotlin/)
- **FlowStep.kt**: Base class for all flow steps (already well-tested in CharpadStepTest)
- **Location**: `flows/domain/steps/`
- **Purpose**: Core domain abstraction for pipeline steps
- **Integration Point**: DasmStep, GoattrackerStep, ImageStep need tests following this pattern

#### Flows Gradle Adapter (flows/adapters/in/gradle/)
- **FlowTasksGenerator.kt**: Creates Gradle task graph from flows
- **Location**: `flows/adapters/in/gradle/src/main/kotlin/.../FlowTasksGenerator.kt`
- **Purpose**: Orchestrates task creation and dependency setup
- **Integration Point**: Must be tested with actual Gradle task infrastructure

#### Gradle Extensions (shared/gradle/src/main/kotlin/)
- **RetroAssemblerPluginExtension**: Main plugin DSL entry point
- **FilterAwareExtension, OutputsExtension**: DSL configuration containers
- **Location**: `shared/gradle/src/main/kotlin/.../`
- **Purpose**: Gradle plugin API surface for users
- **Integration Point**: Tested via extension property binding and DSL evaluation

#### Test Utilities (shared/testutils/)
- **BinaryInputMock, BinaryOutputMock**: Existing mocks for I/O testing
- **Location**: `shared/testutils/src/main/kotlin/.../`
- **Purpose**: Reusable test fixtures for processor testing
- **Integration Point**: Can be extended with Gradle mocks for task testing

### Architecture Alignment

**Domain**:
- Flows (orchestration/pipeline)
- Shared (utilities, configuration)
- Processors (data transformation)

**Use Cases**:
- FlowStepExecutionUseCase (similar to existing CharpadProcessorUseCase pattern)
- Task configuration and graph building use cases
- Configuration mapping use cases (DasmConfigMapperUseCase, etc.)

**Ports**:
- FlowPort (interface for step execution)
- ConfigMapperPort (interface for configuration transformation)
- TaskGeneratorPort (interface for Gradle task creation)

**Adapters**:
- In: Gradle DSL extensions, task infrastructure
- Out: Step executors, configuration processors

### Dependencies
- **Kotest 4.5.0** - Already configured for BDD testing
- **JUnit Jupiter 5.7.0** - Test runner
- **Mockito 3.11.2 & MockK 1.13.2** - Mocking (prefer inline objects per existing patterns)
- **Gradle TestKit** - For testing Gradle plugin tasks (may need to add)
- **JaCoCo 0.8.11** - Coverage measurement (already configured)

## 4. Questions and Clarifications

### Self-Reflection Questions
- **Q**: Should we add Gradle TestKit for functional testing of tasks?
  - **A**: Yes - Gradle TestKit is standard for plugin testing and will help verify task execution

- **Q**: Should Gradle extensions be tested with full DSL evaluation or unit-tested in isolation?
  - **A**: Both - Unit tests for property validation, integration tests for DSL evaluation

- **Q**: How deep should we test FlowTasksGenerator task graph creation?
  - **A**: Test with actual Gradle Project mock to verify task dependencies and configurations

- **Q**: Should we keep inline mock objects or introduce a Mock builder pattern?
  - **A**: Keep inline objects per existing codebase conventions (see CharpadStepTest patterns)

### Unresolved Questions
- [ ] Should test coverage for shared/gradle extensions require full Gradle project setup or mocked?
- [ ] Do we need to add Gradle TestKit as a testImplementation dependency?
- [ ] Should configuration mappers (Dasm, Goattracker) be tested separately or as step integration tests?

### Design Decisions
- **Decision**: Test Framework Choice
  - **Options**: Kotest BehaviorSpec (current), JUnit 5 native, or hybrid
  - **Recommendation**: Continue with Kotest BehaviorSpec for consistency with existing 90% of codebase

- **Decision**: Mock Strategy for Gradle Tasks
  - **Options**: Full Gradle TestKit, Gradle Project mock, inline mock adapters
  - **Recommendation**: Start with Gradle Project mocks (lighter), escalate to TestKit for integration tests

- **Decision**: Test Organization
  - **Options**: Add tests to existing modules, create separate test-modules, add integration-test source set
  - **Recommendation**: Add to existing src/test/kotlin mirroring production structure (per existing pattern)

- **Decision**: Priority of Coverage Targets
  - **Options**: Flows-first (most critical), Utilities-first (foundational), Balanced across all tiers
  - **Recommendation**: Flows-first approach - flows is newest, has largest coverage gap, highest risk

## 5. Implementation Plan

### Phase 1: Foundation - Flows Infrastructure (Deliverable: Testable Task Infrastructure)
**Goal**: Establish test infrastructure for Gradle task testing and create tests for core FlowTasksGenerator

1. **Step 1.1**: Add Gradle TestKit dependency and create test utilities for task testing
   - Files: `shared/testutils/build.gradle.kts`, `shared/testutils/src/test/kotlin/.../GradleProjectMock.kt`
   - Description: Add `gradle-test-kit` as testImplementation dependency in buildSrc/build.gradle.kts. Create GradleProjectMock helper class extending Gradle Project behavior for testing
   - Testing: Verify mock can create tasks and set properties

2. **Step 1.2**: Create comprehensive tests for FlowTasksGenerator
   - Files: `flows/adapters/in/gradle/src/test/kotlin/.../FlowTasksGeneratorTest.kt`
   - Description: Test task creation from flow definitions, verify task dependencies, test step task naming, verify port injection
   - Testing: 40+ test cases covering: single task creation, complex flow dependencies, invalid configurations, edge cases

3. **Step 1.3**: Create tests for all Task classes in flows/adapters/in/gradle/tasks/
   - Files: `flows/adapters/in/gradle/src/test/kotlin/.../tasks/*Task*Test.kt`
   - Description: CommandTask, DasmAssembleTask, AssembleTask, ImageTask, GoattrackerTask, SpritepadTask, CharpadTask, ExomizerTask
   - Testing: Each task class gets 5-8 test cases testing configuration, execution, port validation

**Phase 1 Deliverable**: FlowTasksGenerator and all task classes have >50% line coverage, task infrastructure is testable

### Phase 2: Core Implementation - Gradle Extensions and Domain Configuration (Deliverable: Testable DSL API)
**Goal**: Test Gradle plugin DSL surface and configuration domain layer

1. **Step 2.1**: Create tests for Gradle DSL extension classes in shared/gradle
   - Files: `shared/gradle/src/test/kotlin/.../RetroAssemblerPluginExtensionTest.kt`, `*ExtensionTest.kt` for other extensions
   - Description: Test property binding, DSL evaluation, configuration defaults, nested extension configuration
   - Testing: 50+ test cases covering: property setting, type validation, nested DSL blocks, configuration merging

2. **Step 2.2**: Create tests for all DSL step builders in flows/adapters/in/gradle/dsl/
   - Files: `flows/adapters/in/gradle/src/test/kotlin/.../dsl/*BuilderTest.kt`
   - Description: Test path resolution (useFrom/useTo), parameter building, port injection, configuration validation
   - Testing: 30+ test cases for path resolution, parameter building, validation rules per builder

3. **Step 2.3**: Create tests for configuration mapping classes (DasmConfigMapper, etc.)
   - Files: `flows/domain/src/test/kotlin/.../config/*MapperTest.kt`
   - Description: Test configuration transformation, inheritance, defaults, validation
   - Testing: 25+ test cases covering all configuration paths and combinations

**Phase 2 Deliverable**: All extension classes and configuration mappers have >50% line coverage, DSL API is fully tested

### Phase 3: Integration and Polish - Format Processors and Utilities (Deliverable: Comprehensive Coverage)
**Goal**: Complete coverage for specialized processors and shared utilities, achieve 70%+ overall coverage

1. **Step 3.1**: Create tests for format processor classes (CTM9Processor, SPD4Header, etc.)
   - Files: `processors/charpad/src/test/kotlin/.../post6/CTM9ProcessorTest.kt`, `processors/spritepad/src/test/kotlin/.../SPD4HeaderTest.kt`
   - Description: Test format-specific parsing, version detection, header validation, edge cases
   - Testing: 20+ test cases covering format versions, compression modes, validation rules

2. **Step 3.2**: Extend shared/binary-utils test coverage
   - Files: `shared/binary-utils/src/test/kotlin/.../ByteUtilsTest.kt`, `ByteArrayExtensionsTest.kt`
   - Description: Add comprehensive tests for byte manipulation utilities, extension functions, edge cases
   - Testing: 30+ test cases for bit operations, byte conversions, array manipulations, boundary conditions

3. **Step 3.3**: Create tests for shared/domain/source package classes
   - Files: `shared/domain/src/test/kotlin/.../source/*Test.kt`
   - Description: Test SourceModel, SourceModelBuilder, Comment, Label, Namespace classes
   - Testing: 20+ test cases covering model construction, validation, serialization

4. **Step 3.4**: Verify overall coverage metrics and address remaining gaps
   - Files: All previous test files, build.gradle.kts for coverage configuration
   - Description: Run final coverage report, identify any remaining gaps <50% line coverage, add targeted tests
   - Testing: Verify coverage report generation, validate 70%+ overall coverage goal

**Phase 3 Deliverable**: All 25 target classes have >50% line coverage, overall project reaches 70%+ coverage goal

## 6. Testing Strategy

### Unit Tests
- **BDD-Style Coverage**: Use Kotest BehaviorSpec with Given/When/Then structure for all new tests
- **Target Classes**: Each of the 25 least-covered classes gets 5-30 dedicated test cases
- **Isolation**: Test one concern per test, use inline mocks for dependencies
- **Edge Cases**: Include boundary conditions, null inputs, invalid configurations

### Integration Tests
- **Task Execution**: Test FlowTasksGenerator with Gradle project mock
- **DSL Evaluation**: Test extension property binding with actual Gradle extension mechanism
- **Configuration Flow**: Test end-to-end flow configuration (DSL → domain model → execution)
- **Port Injection**: Verify ports are correctly injected into steps before execution

### Manual Testing
- Run `./gradlew clean test` to verify all tests pass
- Run `./gradlew test jacocoReport` to verify coverage report generation
- Run `./gradlew verifyCodeCoverage` to check coverage thresholds
- Open `build/reports/jacoco/aggregated/index.html` to inspect detailed coverage metrics

## 7. Risks and Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| JaCoCo report generation fails | High | Medium | Investigate and fix build.gradle.kts JaCoCo configuration; verify all modules have jacoco plugin |
| Gradle TestKit has breaking changes with version 7.6 | Medium | Low | Consult Gradle 7.6 TestKit docs; test early with sample task |
| Flows infrastructure is too complex to test effectively | Medium | Medium | Start with FlowTasksGenerator, build incrementally, use functional approach |
| DSL extension testing requires full Gradle setup | Medium | Medium | Start with unit tests of extension classes, escalate to integration tests with Gradle mocks |
| Coverage goals not achievable with reasonable effort | Low | Low | Prioritize Phase 1 (flows) and Phase 2 (extensions); Phase 3 can be incremental |
| New tests reveal untested edge cases in production code | Medium | Medium | Fixes for production bugs discovered during testing are acceptable (improves quality) |

## 8. Documentation Updates

- [x] Update CLAUDE.md with testing patterns for flows infrastructure if needed
- [x] Add inline documentation in complex test setup methods
- [ ] Document GradleProjectMock helper usage if created
- [ ] Update README.md with coverage target goals

## 9. Rollout Plan

1. **Phase 1 Release** (Week 1-2):
   - Merge FlowTasksGenerator and Task classes test coverage
   - Verify flows module reaches 50%+ coverage
   - Deploy as patch release

2. **Phase 2 Release** (Week 2-3):
   - Merge Gradle extension tests
   - Verify shared/gradle module reaches 50%+ coverage
   - Deploy as minor version release

3. **Phase 3 Release** (Week 3-4):
   - Merge processor and utilities test coverage
   - Verify overall project reaches 70% goal
   - Deploy as minor version release with updated documentation

4. **Monitoring**:
   - Monitor CircleCI artifacts for JaCoCo reports in each build
   - Track coverage metrics over time in CI/CD pipeline
   - Alert on coverage regression (>5% drop)

---

**Next Steps**:
1. Review this plan for approval
2. Clarify any unresolved questions above
3. Run `/exec` to begin Phase 1 implementation
4. Create branches following pattern `feature/142-coverage-phase-1`, `feature/142-coverage-phase-2`, etc.
