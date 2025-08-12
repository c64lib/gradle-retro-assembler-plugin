# Action Plan for Flows Parallelization

## Issue Description
Implement parallel execution of build flows in the gradle-retro-assembler-plugin to improve build performance by allowing independent tasks to run concurrently while respecting dependencies.

## Relevant Codebase Parts
1. **flows/** - Main bounded context for flow management following hexagonal architecture
2. **flows/adapters/in/gradle/** - Contains FlowDsl.kt and FlowsExtension.kt for Gradle DSL integration
3. **flows/src/main/kotlin/domain/** - Core domain logic for flow execution and dependency management
4. **buildSrc/** - Contains custom Gradle plugins for different module types
5. **RetroAssemblerPlugin** - Main plugin class that needs to be updated for flow registration

## Root Cause Hypothesis
The current implementation lacks a proper mechanism to leverage Gradle's built-in parallelization capabilities. The solution requires generating actual Gradle tasks that can be parallelized by Gradle's execution engine, rather than implementing custom parallelization logic in the domain layer.

## Investigation Questions
1. How can we leverage Gradle's task parallelization instead of implementing custom threading?
2. What is the best way to generate Gradle tasks dynamically from flow definitions?
3. How should the outbound adapter communicate with Gradle's task system?
4. What information needs to be passed between the domain layer and generated tasks?
5. How do we ensure proper dependency setup between generated tasks?

## Next Steps

### Phase 1: Foundation (Steps 1-3) ✅ COMPLETED
1. ✅ **Analyze Current Architecture** - Understanding current flows structure and hexagonal architecture
   - Rationale: Understanding current architecture is essential for designing parallel flows

2. ✅ **Define Flow DSL Syntax** - Created Kotlin DSL syntax for defining flows in build.gradle.kts files in `flows/adapters/in/gradle/`
   - Rationale: Users need an intuitive way to define parallel execution flows
   - **Implementation**: FlowDsl.kt, FlowsExtension.kt, and examples located in adapters/in/gradle following hexagonal architecture

3. ✅ **Create Flow Dependency Graph** - Implement logic to build and validate dependency graphs between flows
   - Rationale: Essential for determining which flows can execute in parallel

### Phase 2: Core Implementation (Steps 4-7)
4. ✅ **Implement Flow Domain Layer** - Build the core business logic for flow execution and dependency management
   - **CORRECTED APPROACH**: Focus on logical flow representation and dependency resolution, NOT custom parallelization
   - Rationale: Domain should contain pure business logic for flow definitions and dependencies, leaving parallelization to Gradle
   - Implementation: Create domain models for Flow, FlowDependency, and FlowGraph without threading concerns

5. ✅ **Create Outbound Gradle Adapter** - Build adapter that generates actual Gradle tasks from flow definitions
   - **NEW APPROACH**: Create outbound adapter in `flows/adapters/out/gradle/` that:
     - Takes flow definitions from domain layer
     - Generates corresponding Gradle tasks dynamically
     - Sets up proper task dependencies based on flow dependencies
     - Passes references to logical flow execution to generated tasks
   - Rationale: Leverages Gradle's native parallelization instead of custom threading

6. ✅ **Enhance Plugin Registration** - Update RetroAssemblerPlugin to use the outbound adapter for task generation
   - Implementation: Plugin calls outbound adapter to generate tasks instead of registering flows directly
   - Rationale: Integrates flow-based task generation into main plugin lifecycle

7. **Implement Task Execution Bridge** - Create mechanism to execute logical flows within generated Gradle tasks
   - Implementation: Generated tasks receive references to domain flow objects and execute them
   - Rationale: Bridges between Gradle's task execution and domain logic

### Phase 3: Integration & Testing (Steps 8-10)
8. **Integration Testing** - Test flow execution with various dependency scenarios
   - Rationale: Ensure parallelization works correctly with complex dependency graphs

9. **Performance Validation** - Measure and validate performance improvements
   - Rationale: Confirm that parallelization actually improves build times

10. **Documentation Update** - Update user documentation and examples
    - Rationale: Users need clear guidance on defining and using parallel flows

## Additional Notes
- **Key Architectural Change**: Instead of implementing custom parallelization in the domain layer, we leverage Gradle's built-in task parallelization by generating tasks dynamically
- **Outbound Adapter Pattern**: The new outbound Gradle adapter follows hexagonal architecture principles by adapting domain concepts to Gradle's task system
- **Task Generation Strategy**: Flow definitions are transformed into actual Gradle tasks with proper dependencies, allowing Gradle's execution engine to handle parallelization
- **Reference Passing**: Generated tasks maintain references to domain flow objects to execute the actual business logic
- **Dependency Mapping**: Flow dependencies are translated to Gradle task dependencies to maintain execution order
