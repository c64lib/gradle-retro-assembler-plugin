# Action Plan for CommandStep Flows DSL Integration

## Issue Description
The existing CommandStep and CLI support should be integrated with the flows DSL so that CLI commands can be launched in flows with from/to specified. CLI should be launched only if from resources have changed. Implementation should follow the AssemblyStep/AssemblyTask approach with proper change detection and incremental build support.

## Relevant Codebase Parts
1. **Domain Layer - CommandStep**: `flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/steps/CommandStep.kt` - Contains the domain model for command execution but currently has placeholder implementation
2. **Domain Layer - AssembleStep**: `flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/steps/AssembleStep.kt` - Reference implementation showing proper port injection, validation, and execution patterns
3. **Adapter Layer - CommandTask**: `flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/tasks/CommandTask.kt` - Gradle task implementation with CLI execution logic but needs integration improvements
4. **Adapter Layer - BaseFlowStepTask**: `flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/tasks/BaseFlowStepTask.kt` - Provides incremental build support and change detection through Gradle's input/output tracking
5. **DSL Layer - AssembleStepBuilder**: `flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/dsl/AssembleStepBuilder.kt` - Reference DSL builder implementation
6. **DSL Layer - FlowDsl**: `flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/FlowDsl.kt` - Main DSL entry point that needs CommandStep integration

## Root Cause Hypothesis
The CommandStep is currently incomplete and not properly integrated with the flows DSL. The main issues are:
1. CommandStep.execute() has placeholder implementation instead of actual CLI execution
2. No CommandStepBuilder exists for DSL integration
3. No port abstraction for CLI execution (following the port/adapter pattern)
4. CommandTask exists but isn't properly integrated with the DSL and change detection
5. Missing DSL method in FlowDsl for creating command steps

## Investigation Questions
1. Should we create a CommandPort interface similar to AssemblyPort for CLI execution abstraction?
2. How should we handle different CLI tools and their specific parameter patterns?
3. Should command steps support working directory specification beyond the project root?
4. How should we handle command output capture and logging?
5. Should we support environment variable injection for commands?
6. How should we handle command failure scenarios and error reporting?
7. Should we support timeout configuration for long-running commands?

## Next Steps

### Phase 1: Domain Layer Enhancement
1. ✅ **Create CommandPort Interface** - Define domain port for CLI execution following AssemblyPort pattern
2. ✅ **Enhance CommandStep Domain Model** - Add port injection and proper execution implementation
3. ✅ **Add Command Configuration Model** - Create configuration classes for command parameters, environment, working directory

### Phase 2: Adapter Layer Implementation  
4. ✅ **Create CommandPortAdapter** - Implement the CLI execution port adapter with proper error handling
5. ✅ **Enhance CommandTask Integration** - Improve CommandTask to work with enhanced CommandStep and proper change detection
6. ✅ **Add Command-Specific Validation** - Implement thorough validation for command existence, parameters, and file paths

### Phase 3: DSL Integration
7. ✅ **Create CommandStepBuilder** - Implement DSL builder following AssembleStepBuilder pattern with from/to support
8. ✅ **Integrate with FlowDsl** - Add commandStep method to FlowBuilder class
9. **Add DSL Documentation** - Update FlowDsl documentation with command step examples

### Phase 4: Testing and Validation
10. **Create Unit Tests** - Add comprehensive tests for CommandStep, CommandPort, and CommandStepBuilder
11. **Create Integration Tests** - Test full flow execution with command steps and change detection
12. **Test Change Detection** - Verify incremental builds work correctly when input files change
13. **Update Documentation** - Update user documentation with command step usage examples

## Additional Notes
- The implementation should maintain consistency with existing AssemblyStep patterns
- Change detection relies on Gradle's incremental build support through proper input/output file tracking
- CLI execution should be platform-agnostic but may need Windows-specific considerations for the current context
- Error handling should provide clear feedback about command failures and missing dependencies
- The port/adapter pattern ensures testability and flexibility for different CLI execution strategies

## Documentation of the Action Plan (do not modify this section)
- Premium tokens at start: 35%
- Date of start: 2025-08-18
- Execution time: <update>
- Model used to create plan: Sonnet 4
