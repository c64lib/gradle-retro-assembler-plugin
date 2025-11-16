# Feature: Flows Task Integration

**Issue**: #134
**Status**: Planning
**Created**: 2025-11-16

## 1. Feature Description

### Overview
Create a top-level `flows` Gradle task that automatically executes all defined flows in sequence. The `flows` task should be automatically run as a dependency of the `asm` task, ensuring all flow-based preprocessing happens before assembly compilation.

### Requirements
- Create a new `flows` aggregation task that depends on all flow-level tasks
- Make the `asm` task depend on the `flows` task to ensure flows run before assembly
- The task should run all flows defined in the `flows {}` block in the build.gradle.kts
- Flows should execute in correct dependency order (respecting flow-level dependencies)
- The solution must integrate seamlessly with the existing hexagonal architecture
- Task naming should follow existing conventions

### Success Criteria
- ✓ A `flows` task exists and can be run independently via `./gradlew flows`
- ✓ Running `./gradlew asm` automatically runs the `flows` task first
- ✓ All flow-generated tasks are properly ordered and execute correctly
- ✓ Incremental build support is maintained (tasks skip if inputs haven't changed)
- ✓ File-based dependencies between flows continue to work
- ✓ Explicit flow dependencies (dependsOn) continue to work
- ✓ Unit tests verify the behavior
- ✓ No breaking changes to existing flow functionality

## 2. Root Cause Analysis

### Current State
- Individual flow-generated tasks are created (e.g., `flowPreprocessingStepCharpadStep`, `flowCompilationStepAssembleStep`)
- Each flow creates its own step tasks that automatically depend on each other
- The `asm` task is independent and does NOT depend on flow-generated tasks
- Users must manually run flow tasks before running `asm` task
- Users must know which flow task names correspond to their defined flows

### Desired State
- A single `flows` aggregation task that represents all flows
- This `flows` task automatically depends on all top-level flow tasks
- The `asm` task automatically depends on `flows` to ensure proper ordering
- Users can run `./gradlew flows` to execute all flows once
- The dependency chain is: `build` → `asm` → `flows` → (all flow tasks)
- Builds are more intuitive - users don't need to know internal flow task naming

### Gap Analysis
- **Missing Aggregation Task**: No top-level `flows` task that depends on all flow-level tasks
- **Missing Task Dependency**: The `asm` task doesn't depend on the `flows` task
- **Current Workaround**: Users must either:
  1. Run flows manually before `asm`: `./gradlew flows* asm`
  2. Add explicit flow task names to their build configuration
  3. Use a custom build script with hardcoded task names

## 3. Relevant Code Parts

### Existing Components

#### Task Registration and Generation
- **FlowTasksGenerator.kt**: `flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/FlowTasksGenerator.kt`
  - Lines 48-93: Main `registerTasks()` method
  - Lines 84-89: Flow-level dependency setup
  - Lines 261-287: File-based dependency setup
  - Purpose: Creates individual Gradle tasks for each flow and step
  - Integration Point: This is where we'll add logic to create the aggregation `flows` task

#### Plugin Initialization
- **RetroAssemblerPlugin.kt**: `infra/gradle/src/main/kotlin/com/github/c64lib/gradle/RetroAssemblerPlugin.kt`
  - Lines 167-173: Creation of `asm` task
  - Lines 110-214: `afterEvaluate` block where flow tasks are registered
  - Purpose: Main plugin entry point
  - Integration Point: Here we'll modify the `asm` task to depend on the new `flows` task

#### Task Constants
- **Tasks.kt**: `shared/gradle/src/main/kotlin/com/github/c64lib/rbt/shared/gradle/Tasks.kt`
  - Purpose: Centralized task name constants
  - Integration Point: Add `TASK_FLOWS` constant here

#### Gradle Extension
- **FlowsExtension.kt**: `flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/FlowsExtension.kt`
  - Purpose: Holds collection of flows
  - Integration Point: Can access `flows` to determine which flow tasks to aggregate

### Architecture Alignment

**Domain**: Flows subdomain (orchestration domain)
- No domain changes needed - the aggregation logic is purely an adapter concern
- Existing `Flow` and `FlowStep` classes remain unchanged
- Existing `FlowService` for validation and ordering remains unchanged

**Use Cases**: No new use cases required
- Flow execution remains unchanged
- Step execution remains unchanged

**Ports**: No new ports required
- All existing ports (AssemblyPort, CommandPort, etc.) remain unchanged
- The aggregation is a Gradle adapter concern

**Adapters**:
- **Inbound (Gradle)**:
  - Modify: `FlowTasksGenerator.registerTasks()` to create the aggregation task
  - Modify: `RetroAssemblerPlugin` to make `asm` depend on `flows` task
  - Add: Task constant `TASK_FLOWS = "flows"` in `Tasks.kt`
- **Outbound**: No changes needed

### Dependencies
- **Gradle API**: Already a dependency (no new dependencies)
- **Existing task classes**: Will reuse BaseFlowStepTask, AssembleTask, etc.

## 4. Questions and Clarifications

### Self-Reflection Questions (Answered through Research)

- **Q**: How are flow-level aggregation tasks currently created?
  - **A**: The `FlowTasksGenerator.registerTasks()` method creates individual flow tasks (e.g., `flowPreprocessing`, `flowCompilation`) at lines 71-80. Each flow task depends on its last step task.

- **Q**: How does the `asm` task currently integrate?
  - **A**: The `asm` task is created in `RetroAssemblerPlugin.kt` lines 167-173 and depends on `resolveDevDeps`, `downloadDependencies`, and `preprocess` tasks - but NOT on flow tasks.

- **Q**: How are flows stored and accessed?
  - **A**: Flows are stored in `FlowsExtension` which is accessible via `flowsExtension.getFlows()` and passed to `FlowTasksGenerator`.

- **Q**: What is the task naming convention?
  - **A**: Flow tasks are named `flow{FlowNameCapitalized}` (e.g., `flowPreprocessing`). This comes from the flow's `name` property.

- **Q**: Will this break existing builds?
  - **A**: No, because we're only adding a new dependency relationship. Existing users will see flows run automatically, which is the desired behavior.

### Unresolved Questions
- [ ] Should the `flows` task be created even if no flows are defined? (Recommendation: Yes, for consistency - it will just be an empty task)
- [ ] Should we add progress logging when the `flows` task runs? (Recommendation: Use Gradle's default task execution output)
- [ ] Are there any existing CI/CD pipelines that might be affected? (Recommendation: Check recent commits)

### Design Decisions

#### Decision 1: Task Dependency Chain
- **Options**:
  - A) `asm` → `flows` → (all flow tasks)
  - B) Keep `asm` independent, create `flows` task separately
  - C) Merge `flows` into `asm` task logic
- **Recommendation**: Option A
  - **Rationale**: This matches the feature request ("'flows' task should be automatically run before 'asm' task is run"). It separates concerns cleanly and allows users to run flows independently if needed.

#### Decision 2: Where to Create the Aggregation Task
- **Options**:
  - A) In `FlowTasksGenerator.registerTasks()` at the end
  - B) In `RetroAssemblerPlugin` after calling `FlowTasksGenerator`
  - C) In a new separate builder class
- **Recommendation**: Option A
  - **Rationale**: `FlowTasksGenerator` is responsible for creating all flow-related tasks. Creating the aggregation task here keeps all flow task generation in one place and maintains a single source of truth.

#### Decision 3: Behavior When No Flows Defined
- **Options**:
  - A) Don't create `flows` task if no flows exist
  - B) Always create `flows` task (empty if no flows)
  - C) Create `flows` task only if at least one flow exists
- **Recommendation**: Option B
  - **Rationale**: Provides consistent behavior regardless of whether flows are defined. The `asm` task can safely depend on an empty `flows` task.

## 5. Implementation Plan

### Phase 1: Foundation - Add Task Constant and Create Aggregation Task Logic
**Goal**: Create the infrastructure for the `flows` task and prepare task generation

#### Step 1.1: Add TASK_FLOWS constant
- **Files**: `shared/gradle/src/main/kotlin/com/github/c64lib/rbt/shared/gradle/Tasks.kt`
- **Description**: Add a new constant `const val TASK_FLOWS = "flows"` alongside other task constants
- **Testing**: Verify constant is accessible and has correct value

#### Step 1.2: Modify FlowTasksGenerator to create aggregation task
- **Files**: `flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/FlowTasksGenerator.kt`
- **Description**:
  - At the end of `registerTasks()` method (after all flow tasks are created)
  - Create a new aggregation task named using `TASK_FLOWS` constant
  - Make this task depend on all top-level flow task names (flow-level tasks, not step tasks)
  - Handle the case where no flows are defined (empty flows list - task is created but has no dependencies)
- **Algorithm**:
  ```kotlin
  // At end of registerTasks() method
  val flowTaskNames = flows.map { flow -> "flow${flow.name.replaceFirstChar { it.uppercase() }}" }

  if (flowTaskNames.isNotEmpty()) {
      val flowsAggregation = taskContainer.create(TASK_FLOWS) {
          flowTaskNames.forEach { flowTaskName ->
              this.dependsOn(flowTaskName)
          }
      }
  } else {
      taskContainer.create(TASK_FLOWS)
  }
  ```
- **Testing**:
  - Create unit test that verifies `flows` task is created
  - Verify it depends on correct flow tasks
  - Test with no flows defined

**Phase 1 Deliverable**: The `flows` task is created and can be run via `./gradlew flows`. It executes all defined flows in correct dependency order.

### Phase 2: Core Implementation - Integrate with asm Task
**Goal**: Make the `asm` task depend on `flows` task to ensure flows run before assembly

#### Step 2.1: Modify RetroAssemblerPlugin to add dependency
- **Files**: `infra/gradle/src/main/kotlin/com/github/c64lib/gradle/RetroAssemblerPlugin.kt`
- **Description**:
  - Find where the `asm` task is created (currently line 167)
  - Add `assemble.dependsOn(flows)` after the task is created
  - Need to access the `flows` task from the task container
  - Code location: After `FlowTasksGenerator` is called, before build task creation
  - Logic:
    ```kotlin
    // After FlowTasksGenerator.registerTasks() is called
    val flowsTask = project.tasks.findByName(TASK_FLOWS)
    if (flowsTask != null) {
        assemble.dependsOn(flowsTask)
    }
    ```
- **Testing**:
  - Run `./gradlew asm` and verify it triggers the `flows` task first
  - Check task execution order in build output
  - Verify flow tasks execute before assembly step

#### Step 2.2: Verify no breaking changes
- **Files**: Various test files
- **Description**:
  - Ensure existing task dependencies still work
  - Verify incremental build behavior (tasks skip if inputs unchanged)
  - Test with and without flows defined
- **Testing**:
  - Run full build with flows defined
  - Run full build without flows
  - Run `./gradlew clean build` to verify clean build works
  - Run build twice to verify incremental build behavior

**Phase 2 Deliverable**: The `asm` task automatically depends on and runs the `flows` task. Users can run `./gradlew build` or `./gradlew asm` and flows will execute automatically in correct order.

### Phase 3: Integration and Testing
**Goal**: Comprehensive testing and documentation updates

#### Step 3.1: Add unit tests for flows task generation
- **Files**: Create or modify `flows/src/test/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/FlowTasksGeneratorTest.kt`
- **Description**:
  - Test that `flows` task is created
  - Test that `flows` task depends on all flow-level tasks
  - Test with multiple flows
  - Test with no flows
  - Test that flow execution order is preserved
- **Testing**: All test cases pass

#### Step 3.2: Add integration tests
- **Files**: Create or modify test files in `infra/gradle/src/test/`
- **Description**:
  - Test that `asm` task depends on `flows` task
  - Test that `./gradlew flows` executes all flows
  - Test that `./gradlew asm` runs flows before assembly
  - Test with complex flow dependencies
  - Test parallel execution of independent flows
- **Testing**: All test cases pass

#### Step 3.3: Update documentation
- **Files**:
  - `CLAUDE.md` - Update flows section if needed
  - Any inline comments in code
- **Description**:
  - Document the new `flows` task in CLAUDE.md
  - Document task execution order
  - Add example of build output
- **Testing**: Documentation is clear and accurate

**Phase 3 Deliverable**: Complete test coverage, documented features, and verified behavior across all use cases.

## 6. Testing Strategy

### Unit Tests
- **FlowTasksGeneratorTest**:
  - Test creation of `flows` aggregation task
  - Test `flows` task depends on all flow-level tasks
  - Test with zero, one, and multiple flows
  - Test with flows that have dependencies
  - Mock task container and verify task creation

### Integration Tests
- **RetroAssemblerPluginTest**:
  - Test that `asm` task depends on `flows` task
  - Test build task execution order
  - Create a sample project with flows defined
  - Run `./gradlew flows` and verify execution
  - Run `./gradlew asm` and verify flows execute first
  - Test incremental builds
  - Test with flow dependencies

### Manual Testing
1. Create a test project with multiple flows
2. Run `./gradlew flows` - verify all flows execute
3. Run `./gradlew asm` - verify flows execute before assembly
4. Run `./gradlew clean build` - verify full build works
5. Run `./gradlew build` twice - verify incremental build skips unchanged tasks
6. Test with complex flow dependencies
7. Test build output shows correct task execution order

## 7. Risks and Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Breaking change to existing builds | High | Low | Make dependency automatic (desired feature), test thoroughly with sample projects |
| Task ordering issues with complex flows | Medium | Medium | Comprehensive integration tests with various flow dependency patterns, use existing FlowService for validation |
| Performance impact from additional dependency | Low | Low | The aggregation task itself does no work, only adds dependency - minimal overhead |
| Gradle cache invalidation | Low | Low | Use existing incremental build mechanisms, task inputs/outputs unchanged |
| Null reference when accessing flows task | Medium | Low | Check for null return from `findByName()`, verify task creation order |

## 8. Documentation Updates

- [ ] Update `CLAUDE.md` Flows section to mention the automatic `flows` task
- [ ] Add example showing task execution order
- [ ] Document that `flows` task is automatically run before `asm`
- [ ] Update inline comments in `FlowTasksGenerator` to explain aggregation task creation
- [ ] Add comment in `RetroAssemblerPlugin` showing dependency chain

## 9. Rollout Plan

1. **Implementation**: Complete Phase 1-3 per implementation plan
2. **Testing**: Run full test suite, including new unit and integration tests
3. **Sample Project**: Create/update sample build.gradle.kts with flows
4. **Build & Verify**:
   - Run `./gradlew build` on sample project
   - Verify task execution order matches expected
   - Verify both flows and assembly complete successfully
5. **Documentation**: Update CLAUDE.md with new behavior
6. **Release**: Publish as patch/minor version update
7. **Monitoring**: Check GitHub issues for any unexpected behavior reports

---

**Next Steps**:
- Review this plan and provide any feedback on design decisions (Questions section)
- Confirm Phase 1 approach for task aggregation logic
- Confirm Phase 2 approach for adding dependency to `asm` task
- Approve plan for implementation to begin
