# Action Plan for Assemble Step Integration

## Issue Description
Implement AssembleStep and AssembleTask in `flows` bounded context to integrate with the actual KickAssembler compiler. The AssembleStepBuilder needs the same capability as the existing `Assemble.kt` task in the `compilers/kickass` module, but implemented within the hexagonal flow architecture.

## Relevant Codebase Parts
1. **flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/steps/AssembleStep.kt** - Current basic implementation that needs enhancement
2. **compilers/kickass/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/compilers/kickass/adapters/in/gradle/Assemble.kt** - Reference implementation with KickAssembleUseCase integration
3. **flows/domain/Flow.kt** - Base FlowStep abstract class definition
4. **flows/adapters/in/gradle/** - Gradle adapter layer for flow execution
5. **compilers/kickass/usecase/KickAssembleUseCase** - Business logic for assembly compilation
6. **shared/gradle/dsl/RetroAssemblerPluginExtension.kt** - Configuration extension used by existing Assemble task

## Root Cause Hypothesis
The current `AssembleStep` in the flows domain is a placeholder implementation that only prints debug information. It lacks:
- Integration with `KickAssembleUseCase` for actual compilation
- Configuration mapping from flow config to KickAssemble commands
- Proper file handling and pattern matching like the reference `Assemble.kt`
- Gradle task adapter for execution within the flow framework

## Investigation Questions
1. How should the AssemblyConfig in flows domain map to KickAssembleCommand parameters?
2. What is the relationship between flow-based execution and traditional Gradle task execution?
3. Should AssembleStep directly depend on the kickass compiler module, or should there be an abstraction layer?
4. How should file pattern matching and source file discovery work within the flow context?
5. What are the input/output file conventions for assembly steps in flows?
6. How should error handling and logging be implemented in the flow-based approach?
7. Is there a need for a separate AssembleTask in addition to AssembleStep, or should the step handle Gradle integration internally?

## Next Steps

### Phase 1: Analysis and Design
1. **Analyze existing AssemblyConfig structure** - Examine the current config class and compare with KickAssembleCommand requirements
2. **Review flow execution architecture** - Understand how FlowStep execution integrates with Gradle tasks
3. **Design config mapping strategy** - Plan how AssemblyConfig maps to KickAssembleCommand parameters
4. **Identify dependency injection approach** - Determine how KickAssembleUseCase will be provided to AssembleStep

### Phase 2: Domain Layer Implementation
5. **Enhance AssembleStep domain logic** - Integrate KickAssembleUseCase into the execute method
6. **Update AssemblyConfig class** - Ensure it contains all necessary configuration parameters
7. **Implement file pattern matching** - Add source file discovery logic similar to reference implementation
8. **Add proper error handling** - Implement validation and error reporting

### Phase 3: Adapter Layer Implementation
9. **Create AssembleStepBuilder** - Implement builder pattern for creating configured AssembleStep instances
10. **Enhance Gradle task integration** - Ensure AssembleStep works properly within the flow task execution framework
11. **Add dependency injection** - Wire up KickAssembleUseCase through the adapter layer

### Phase 4: Testing and Integration
12. **Write unit tests** - Test AssembleStep logic with Kotest BDD style
13. **Write integration tests** - Test end-to-end flow execution with actual assembly files
14. **Update documentation** - Update AsciiDoctor docs and CHANGES.adoc file
15. **Format and validate code** - Run spotlessApply and ensure compilation

## Additional Notes
- The implementation should follow hexagonal architecture principles with domain logic separated from Gradle-specific concerns
- The AssembleStep should reuse existing KickAssembleUseCase rather than duplicating assembly logic
- Consider backward compatibility with existing Assemble task usage patterns
- The flow-based approach should provide additional flexibility for complex build pipelines while maintaining the same core functionality
