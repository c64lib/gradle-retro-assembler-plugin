# Action Plan for Charpad Step Integration

## Issue Description
Integrate CharpadStep with the existing charpad preprocessor, retaining all capabilities of the charpad processor from the original, non-flows-based implementation. The current CharpadStep in the flows module has a placeholder implementation that needs to be connected to the fully-featured charpad processor module.

## Issue Update
**RESOLVED**: The CharpadStep integration test was failing with "Insufficient data in CTM file" error because the test was creating a minimal synthetic CTM file. This has been resolved by adding a real CTM test resource file (`flows/src/test/resources/test-integration.ctm`) and updating the integration test to load it using `javaClass.getResourceAsStream()`. All tests are now passing.

**NEW REQUIREMENT**: The CharpadStep DSL needs to be redesigned to replace the generic "to" outputs with dedicated DSL entries for each output type (charset, map, charsetColours, charsetAttributes, etc.), matching the functionality of the original processor DSL. This will provide:
- Dedicated methods for each output type (charset, map, tiles, charsetColours, charsetAttributes, charsetMaterials, charsetScreenColours, tileTags, tileColours, tileScreenColours, meta)
- Support for start/end range parameters for charset and tile-based outputs
- Support for left/top/right/bottom rectangular region parameters for map outputs
- Support for all metadata configuration parameters (namespace, prefix, includeVersion, etc.)
- Ability to define multiple outputs of the same type (e.g., multiple charset outputs with different ranges)

## Relevant Codebase Parts

1. **flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/steps/CharpadStep.kt** - Current placeholder implementation with basic validation and configuration structure
2. **processors/charpad/src/main/kotlin/com/github/c64lib/rbt/processors/charpad/usecase/ProcessCharpadUseCase.kt** - Main charpad processing logic supporting CTM versions 5-9
3. **processors/charpad/domain/** - Domain models including various producers (CharsetProducer, MapProducer, TileProducer, etc.)
4. **processors/charpad/adapters/in/gradle/** - Gradle adapter with CharpadMetaOutput and task implementation
5. **flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/config/ProcessorConfig.kt** - CharpadConfig data class with compression, export format, and optimization settings
6. **flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/dsl/CharpadStepBuilder.kt** - DSL builder for charpad configuration
7. **flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/tasks/CharpadTask.kt** - Existing placeholder Gradle task for charpad processing
8. **flows/adapters/in/gradle/src/main/kotlin/com/github/c64lib/rbt/flows/adapters/in/gradle/assembly/KickAssemblerPortAdapter.kt** - Example adapter pattern implementation for connecting flows to compilers module
9. **flows/adapters/out/** - Location where intermediate inbound adapter for charpad processing will be created
10. **shared/gradle/src/main/kotlin/com/github/c64lib/rbt/shared/gradle/dsl/OutputsExtension.kt** - Original processor DSL with dedicated methods for each output type (charset, map, charsetColours, etc.)
11. **shared/gradle/src/main/kotlin/com/github/c64lib/rbt/shared/gradle/dsl/StartEndExtension.kt** - Extension for outputs with start/end range parameters
12. **shared/gradle/src/main/kotlin/com/github/c64lib/rbt/shared/gradle/dsl/MapExtension.kt** - Extension for map outputs with left/top/right/bottom rectangular region parameters
13. **shared/gradle/src/main/kotlin/com/github/c64lib/rbt/shared/gradle/dsl/MetadataExtension.kt** - Extension for metadata outputs with namespace, prefix, and inclusion flags
14. **processors/charpad/adapters/in/gradle/src/main/kotlin/.../Charpad.kt** - Original charpad task showing how outputs are configured and processed

## Root Cause Hypothesis
The CharpadStep currently has only a placeholder execute() method that prints debug information instead of performing actual charpad processing. The integration requires:
- Creating an intermediate inbound adapter module that bridges flows to charpad processor (flows module cannot directly depend on processors/charpad)
- Creating a proper port adapter following the established pattern (similar to KickAssemblerPortAdapter)
- Connecting to the ProcessCharpadUseCase from the charpad processor module through the intermediate adapter
- Mapping the flows CharpadConfig to the appropriate output producers
- Handling file I/O between flows and the processor module
- Preserving all existing charpad capabilities including multiple CTM format support and various output types

## Investigation Questions

### Self-Reflection Questions
1. How should the CharpadConfig properties map to the existing output producers in the charpad processor?
2. What is the proper dependency injection pattern for connecting flows module to processors module through intermediate adapter?
3. How should file paths be resolved between the flows context and processor input/output streams?
4. Are there any missing configuration options in CharpadConfig that exist in the original processor?
5. How should error handling be unified between the flows validation and processor exceptions?
6. What is the correct way to handle the ctm8PrototypeCompatibility flag in the flows context?
7. **NEW**: How should the dedicated output DSL methods be structured in the domain model vs the DSL builder? Should the domain model have separate lists for each output type, or a unified structure with type discriminators?
8. **NEW**: How should default values for start/end and left/top/right/bottom parameters be handled? Should they match the original processor defaults (0, 65536)?
9. **NEW**: Should the dedicated DSL methods be added to CharpadStepBuilder only, or also reflected in the CharpadStep domain model? How to maintain clean separation between domain and DSL concerns?

### Question for others
1. ~~Should the flows module directly depend on the processors/charpad module, or should there be an intermediate adapter?~~ **RESOLVED**: Flows module cannot depend on processors/charpad. There must be an intermediate inbound adapter declared to indirectly set up this dependency.
2. ~~Are there any specific output file naming conventions that should be enforced by the flows integration?~~ **RESOLVED**: No, there are no specific conventions; users should be able to provide any name for an output file
3. ~~Should the integration support all the existing output producers or only a subset for the flows use case?~~ **RESOLVED**: Yes, all existing output producers should be supported to retain full capabilities
4. ~~How should metadata outputs be handled in the flows context (namespace, prefixes, etc.)?~~ **RESOLVED**: Metadata output should support all parameters explicitly, per step definition

## Next Steps

1. ❌ **~~Add dependency from flows to charpad processor~~** - ~~Modify flows/build.gradle.kts to include dependency on processors:charpad module to access ProcessCharpadUseCase and domain models~~ **CANCELLED**: Flows module cannot directly depend on processors/charpad

2. ✅ **Create intermediate inbound adapter module** - Create flows/adapters/out/charpad module with its own build.gradle.kts that depends on both flows domain and processors:charpad, following the established pattern of other adapter modules

3. ✅ **Create CharpadPort interface** - Define a domain port interface in flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/port/ following the AssemblyPort pattern for charpad processing operations

4. ✅ **Create CharpadAdapter** - Implement adapter in flows/adapters/out/charpad/.../CharpadAdapter.kt that bridges flows domain to charpad processor module, similar to KickAssemblerPortAdapter pattern

5. ✅ **Create comprehensive output producer factory** - Implement a factory class within the adapter that converts CharpadConfig to the complete collection of OutputProducer instances, supporting all existing producers: CharsetProducer, MapProducer, TileProducer, CharAttributesProducer, CharColoursProducer, CharMaterialsProducer, CharScreenColoursProducer, TileTagsProducer, TileColoursProducer, TileScreenColoursProducer, and HeaderProducer

6. ✅ **Implement file I/O adapters** - Create adapters within the port adapter to convert flows file paths to InputByteStream and handle various output types (binary, text) to files. Ensure users can specify any output file names without enforcing specific naming conventions

7. ✅ **Extend CharpadConfig for metadata support** - Add explicit metadata configuration parameters to CharpadConfig to support all metadata output options per step definition (namespace, prefix, version inclusion flags, background colours inclusion, char colours inclusion, mode inclusion, etc.)

8. ✅ **Update CharpadStep.execute() method** - Replace placeholder implementation with calls to the CharpadPort interface for actual charpad processing

9. ✅ **Update CharpadTask.executeStepLogic() method** - Replace placeholder implementation in the Gradle task adapter to use the new CharpadAdapter from the intermediate adapter module

10. ✅ **Add comprehensive error handling** - Map charpad processor exceptions (InvalidCTMFormatException, InsufficientDataException) to flows validation errors within the adapter

11. ✅ **Extend CharpadConfig if needed** - Compare flows CharpadConfig with all capabilities in the original processor and add missing options like ctm8PrototypeCompatibility to support all output producers

12. ✅ **Create integration tests** - Add tests that verify the CharpadStep and CharpadTask produce identical outputs to the original charpad processor for various CTM file formats, testing all supported output producers including metadata outputs

13. ✅ **Fix integration test CTM file issue** - Replaced synthetic CTM file creation with a real CTM file resource (`flows/src/test/resources/test-integration.ctm`). Updated the integration test in CharpadStepTest.kt:409 to load the CTM file using `javaClass.getResourceAsStream("/test-integration.ctm")`. All tests are now passing successfully.

14. **Redesign CharpadStep DSL with dedicated output methods** - Replace the generic "to" output configuration with dedicated DSL methods matching the original processor:
    * Create domain models for each output type configuration (CharsetOutput, MapOutput, MetadataOutput, etc.) in flows/src/main/kotlin/com/github/c64lib/rbt/flows/domain/config/
    * Update CharpadStepBuilder to provide dedicated methods: `charset {}`, `map {}`, `charsetColours {}`, `charsetAttributes {}`, `charsetMaterials {}`, `charsetScreenColours {}`, `tiles {}`, `tileTags {}`, `tileColours {}`, `tileScreenColours {}`, `meta {}`
    * Each method should accept a lambda for configuring output-specific parameters (start/end for ranges, left/top/right/bottom for maps, namespace/prefix/flags for metadata)
    * Support multiple outputs of the same type (e.g., multiple charset outputs with different ranges)
    * Remove the generic `to()` method and related generic output configuration
    * Update CharpadStep domain model to store lists of dedicated output configurations instead of generic outputs
    * Ensure backward compatibility by providing migration path documentation

15. **Update CharpadAdapter to handle dedicated output configurations** - Modify the CharpadAdapter and OutputProducerFactory to work with the new dedicated output models:
    * Update CharpadOutputProducerFactory to accept lists of dedicated output configurations
    * Create producer instances based on each dedicated output configuration
    * Map domain output models (CharsetOutput, MapOutput, etc.) to their corresponding producers (CharsetProducer, MapProducer, etc.)
    * Ensure all parameters (start/end, left/top/right/bottom, metadata flags) are correctly passed to producers

16. **Update CharpadCommand and mapping logic** - Modify CharpadCommand and related mappers to work with dedicated outputs:
    * Update CharpadCommand domain model to include lists for each output type
    * Update CharpadConfigMapper to map from CharpadStepBuilder's dedicated outputs to CharpadCommand
    * Ensure proper validation of output configurations

17. **Update integration and unit tests** - Modify existing tests to use the new dedicated DSL:
    * Update CharpadStepTest to use dedicated output methods (charset, map, meta, etc.)
    * Add tests for multiple outputs of the same type
    * Add tests for range parameters (start/end) and rectangular regions (left/top/right/bottom)
    * Update CharpadAdapter tests to verify correct producer creation from dedicated configurations
    * Ensure all existing test scenarios still pass with the new DSL

18. **Update documentation** - Modify flows documentation to include charpad step usage examples and configuration options, emphasizing support for all output types including explicit metadata configuration parameters. Include examples of the new dedicated DSL methods and migration guide from generic "to" outputs.

19. **Validate against existing charpad tests** - Run existing charpad processor tests to ensure no regression in core functionality

20. **Add flows-specific charpad tests** - Create tests for the CharpadStep integration including validation, configuration mapping, and file I/O scenarios for all output producer types, with specific tests for metadata output configuration

## Additional Notes
- The charpad processor supports CTM versions 5, 6, 7, 8, 82, and 9 with different processing logic for each
- All existing output producers must be supported: charset, char attributes/colours/materials/screen colours, tiles, tile tags/colours/screen colours, map, and header producers
- The original implementation includes sophisticated metadata handling with namespace support and optional inclusion flags
- Metadata outputs must support all parameters explicitly per step definition, requiring expansion of CharpadConfig to include all metadata configuration options (namespace, prefix, includeVersion, includeBgColours, includeCharColours, includeMode, etc.)
- Special attention needed for CTM8 prototype compatibility flag that may need to be exposed in CharpadConfig
- The established adapter pattern uses port interfaces in the domain layer with concrete adapters in the adapter layer, maintaining hexagonal architecture boundaries
- Both CharpadStep (domain) and CharpadTask (gradle adapter) need to be updated to use the new port adapter
- Users should have complete flexibility in naming output files - no specific naming conventions should be enforced by the flows integration
- The integration must retain full compatibility with all existing charpad processor capabilities to ensure no functionality is lost in the flows-based implementation
- **IMPORTANT**: Flows module cannot directly depend on processors/charpad - an intermediate inbound adapter module (flows/adapters/out/charpad) must be created to bridge this dependency indirectly
- **DSL Redesign**: The new dedicated DSL methods (charset, map, charsetColours, etc.) should mirror the original processor DSL structure found in OutputsExtension, StartEndExtension, MapExtension, and MetadataExtension
- **Multiple Outputs**: The DSL must support multiple outputs of the same type (e.g., `charset { output = "chars1.bin"; start = 0; end = 128 }` and `charset { output = "chars2.bin"; start = 128; end = 256 }`)
- **Parameter Defaults**: Default values for range parameters should match the original processor: start=0, end=65536, left=0, top=0, right=65536, bottom=65536
- **Migration Path**: Existing code using generic "to" outputs will need to be migrated to use dedicated output methods; documentation should provide clear migration examples
