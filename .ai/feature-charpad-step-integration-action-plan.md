# Action Plan for Charpad Step Integration

## Issue Description
Integrate CharpadStep with the existing charpad preprocessor, retaining all capabilities of the charpad processor from the original, non-flows-based implementation. The current CharpadStep in the flows module has a placeholder implementation that needs to be connected to the fully-featured charpad processor module.

## Relevant Codebase Parts

1. **flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/steps/CharpadStep.kt** - Current placeholder implementation with basic validation and configuration structure
2. **processors/charpad/src/main/kotlin/com/github/c64lib/rbt/processors/charpad/usecase/ProcessCharpadUseCase.kt** - Main charpad processing logic supporting CTM versions 5-9
3. **processors/charpad/domain/** - Domain models including various producers (CharsetProducer, MapProducer, TileProducer, etc.)
4. **processors/charpad/adapters/in/gradle/** - Gradle adapter with CharpadMetaOutput and task implementation
5. **flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/config/ProcessorConfig.kt** - CharpadConfig data class with compression, export format, and optimization settings
6. **flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/dsl/CharpadStepBuilder.kt** - DSL builder for charpad configuration
7. **flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/tasks/CharpadTask.kt** - Existing placeholder Gradle task for charpad processing
8. **flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/assembly/KickAssemblerPortAdapter.kt** - Example adapter pattern implementation for connecting flows to compilers module

## Root Cause Hypothesis
The CharpadStep currently has only a placeholder execute() method that prints debug information instead of performing actual charpad processing. The integration requires:
- Creating a proper port adapter following the established pattern (similar to KickAssemblerPortAdapter)
- Connecting to the ProcessCharpadUseCase from the charpad processor module through the adapter
- Mapping the flows CharpadConfig to the appropriate output producers
- Handling file I/O between flows and the processor module
- Preserving all existing charpad capabilities including multiple CTM format support and various output types

## Investigation Questions

### Self-Reflection Questions
1. How should the CharpadConfig properties map to the existing output producers in the charpad processor?
2. What is the proper dependency injection pattern for connecting flows module to processors module?
3. How should file paths be resolved between the flows context and processor input/output streams?
4. Are there any missing configuration options in CharpadConfig that exist in the original processor?
5. How should error handling be unified between the flows validation and processor exceptions?
6. What is the correct way to handle the ctm8PrototypeCompatibility flag in the flows context?

### Question for others
1. ~~Should the flows module directly depend on the processors/charpad module, or should there be an intermediate adapter?~~ **RESOLVED**: Yes, there should be an adapter following the same pattern as compilers integration (KickAssemblerPortAdapter)
2. ~~Are there any specific output file naming conventions that should be enforced by the flows integration?~~ **RESOLVED**: No, there are no specific conventions; users should be able to provide any name for an output file
3. Should the integration support all the existing output producers or only a subset for the flows use case?
4. How should metadata outputs be handled in the flows context (namespace, prefixes, etc.)?

## Next Steps

1. **Add dependency from flows to charpad processor** - Modify flows/build.gradle.kts to include dependency on processors:charpad module to access ProcessCharpadUseCase and domain models

2. **Create CharpadPort interface** - Define a domain port interface in flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/port/ following the AssemblyPort pattern for charpad processing operations

3. **Create CharpadPortAdapter** - Implement adapter in flows/adapters/in/gradle/.../CharpadPortAdapter.kt that bridges flows domain to charpad processor module, similar to KickAssemblerPortAdapter pattern

4. **Create output producer factory** - Implement a factory class within the adapter that converts CharpadConfig to appropriate collection of OutputProducer instances based on configuration flags

5. **Implement file I/O adapters** - Create adapters within the port adapter to convert flows file paths to InputByteStream and handle various output types (binary, text) to files. Ensure users can specify any output file names without enforcing specific naming conventions

6. **Update CharpadStep.execute() method** - Replace placeholder implementation with calls to the CharpadPort interface for actual charpad processing

7. **Update CharpadTask.executeStepLogic() method** - Replace placeholder implementation in the Gradle task adapter to use the new CharpadPortAdapter

8. **Add comprehensive error handling** - Map charpad processor exceptions (InvalidCTMFormatException, InsufficientDataException) to flows validation errors within the adapter

9. **Extend CharpadConfig if needed** - Compare flows CharpadConfig with all capabilities in the original processor and add missing options like ctm8PrototypeCompatibility

10. **Create integration tests** - Add tests that verify the CharpadStep and CharpadTask produce identical outputs to the original charpad processor for various CTM file formats

11. **Update documentation** - Modify flows documentation to include charpad step usage examples and configuration options

12. **Validate against existing charpad tests** - Run existing charpad processor tests to ensure no regression in core functionality

13. **Add flows-specific charpad tests** - Create tests for the CharpadStep integration including validation, configuration mapping, and file I/O scenarios

## Additional Notes
- The charpad processor supports CTM versions 5, 6, 7, 8, 82, and 9 with different processing logic for each
- Multiple output producers exist: charset, char attributes/colours/materials, tiles, tile tags/colours, map, and header producers
- The original implementation includes sophisticated metadata handling with namespace support and optional inclusion flags
- Special attention needed for CTM8 prototype compatibility flag that may need to be exposed in CharpadConfig
- The established adapter pattern uses port interfaces in the domain layer with concrete adapters in the adapter layer, maintaining hexagonal architecture boundaries
- Both CharpadStep (domain) and CharpadTask (gradle adapter) need to be updated to use the new port adapter
- Users should have complete flexibility in naming output files - no specific naming conventions should be enforced by the flows integration
- Consider whether the flows integration should support the full Gradle task capabilities or focus on core processing functionality
