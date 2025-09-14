# Action Plan for Charpad Step Integration

## Issue Description
Integrate CharpadStep with the existing charpad preprocessor, retaining all capabilities of the charpad processor from the original, non-flows-based implementation. The current CharpadStep in the flows module has a placeholder implementation that needs to be connected to the fully-featured charpad processor module.

## Issue Update
**Current Test Failure**: The CharpadStep integration test is failing with "Insufficient data in CTM file" error because the test creates a minimal synthetic CTM file that doesn't contain sufficient data for the charpad processor to work with. The error occurs at:

```
java.lang.RuntimeException: Charpad processing failed for step 'integrationTest': Insufficient data in CTM file 'test.ctm': Unexpected end of file reached while reading CTM data. The CTM file appears to be corrupted or truncated.
```

This indicates that the test needs to use real CTM files as resources, similar to how the existing charpad processor tests work (using files like `/text-hires/text-hi-per-char-notiles-ctm5.ctm`).

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

12.1. **🔧 Fix integration test CTM file issue** - The current integration test creates a synthetic CTM file with insufficient data, causing "Insufficient data in CTM file" error. Replace the synthetic CTM file creation with a real CTM file resource:
   - Copy one of the existing CTM files from `processors/charpad/src/test/resources/` (e.g., `text-hires/text-hi-per-char-notiles-ctm5.ctm`) to `flows/src/test/resources/`
   - Update the integration test to load the CTM file using `javaClass.getResourceAsStream()` similar to how charpad processor tests work
   - Remove the synthetic CTM file creation code and replace with resource loading
   - This will provide a properly formatted CTM file with sufficient data for the charpad processor to work with

13. **Update documentation** - Modify flows documentation to include charpad step usage examples and configuration options, emphasizing support for all output types including explicit metadata configuration parameters

14. **Validate against existing charpad tests** - Run existing charpad processor tests to ensure no regression in core functionality

15. **Add flows-specific charpad tests** - Create tests for the CharpadStep integration including validation, configuration mapping, and file I/O scenarios for all output producer types, with specific tests for metadata output configuration

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
