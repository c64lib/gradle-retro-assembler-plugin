/*
MIT License

Copyright (c) 2018-2025 c64lib: The Ultimate Commodore 64 Library
Copyright (c) 2018-2025 Maciej Małecki

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package com.github.c64lib.rbt.flows.domain.steps

import com.github.c64lib.rbt.flows.domain.FlowStep
import com.github.c64lib.rbt.flows.domain.config.AssemblyConfig
import com.github.c64lib.rbt.flows.domain.config.AssemblyConfigMapper
import com.github.c64lib.rbt.flows.domain.port.AssemblyPort
import java.io.File

/** Domain model for Assembly processing steps with type-safe configuration. */
class AssembleStep(
    name: String,
    inputs: List<String> = emptyList(),
    outputs: List<String> = emptyList(),
    val config: AssemblyConfig = AssemblyConfig(),
    private var assemblyPort: AssemblyPort? = null,
    private val configMapper: AssemblyConfigMapper = AssemblyConfigMapper()
) : FlowStep(name, "assemble", inputs, outputs) {

  /**
   * Injects the assembly port dependency. This is called by the adapter layer when the step is
   * prepared for execution.
   */
  fun setAssemblyPort(port: AssemblyPort) {
    this.assemblyPort = port
  }

  override fun execute(context: Map<String, Any>) {
    val port =
        assemblyPort
            ?: throw IllegalStateException(
                "AssemblyPort not injected for step '$name'. Call setAssemblyPort() before execution.")

    // Extract project root directory from context
    val projectRootDir =
        context["projectRootDir"] as? File
            ?: throw IllegalStateException("Project root directory not found in execution context")

    // Convert input paths to source files
    val sourceFiles =
        inputs.map { inputPath ->
          val file =
              if (File(inputPath).isAbsolute) {
                File(inputPath)
              } else {
                File(projectRootDir, inputPath)
              }

          if (!file.exists()) {
            throw IllegalArgumentException("Source file does not exist: ${file.absolutePath}")
          }

          file
        }

    // Map configuration to assembly commands with output handling
    val assemblyCommands =
        if (sourceFiles.size == 1 && outputs.isNotEmpty()) {
          // Single source file with explicit output - use enhanced mapping
          val outputPath = outputs.first()
          listOf(
              configMapper.toAssemblyCommand(
                  config, sourceFiles.first(), projectRootDir, outputPath))
        } else {
          // Multiple source files or no explicit output - use existing logic
          configMapper.toAssemblyCommands(config, sourceFiles, projectRootDir)
        }

    // Execute assembly compilation through the port
    try {
      port.assemble(assemblyCommands)
    } catch (e: Exception) {
      throw RuntimeException("Assembly compilation failed for step '$name': ${e.message}", e)
    }

    outputs.forEach { outputPath -> println("  Generated output: $outputPath") }
  }

  override fun validate(): List<String> {
    val errors = mutableListOf<String>()

    if (inputs.isEmpty()) {
      errors.add("Assembly step '$name' requires at least one input .asm file")
    }

    if (outputs.isEmpty()) {
      errors.add("Assembly step '$name' requires at least one output file")
    }

    // Validate input file extensions
    inputs.forEach { inputPath ->
      if (!inputPath.endsWith(".asm", ignoreCase = true) &&
          !inputPath.endsWith(".s", ignoreCase = true)) {
        errors.add("Assembly step '$name' expects .asm or .s files, but got: $inputPath")
      }
    }

    // Validate include paths exist if specified
    config.includePaths.forEach { includePath ->
      // Note: In a real implementation, we would check if the path exists
      // For now, we just validate it's not empty
      if (includePath.isBlank()) {
        errors.add("Assembly step '$name' include path cannot be blank")
      }
    }

    return errors
  }

  override fun getConfiguration(): Map<String, Any> {
    return mapOf(
        "cpu" to config.cpu.name,
        "generateSymbols" to config.generateSymbols,
        "optimization" to config.optimization.name,
        "includePaths" to config.includePaths,
        "defines" to config.defines,
        "verbose" to config.verbose)
  }

  override fun toString(): String {
    return "AssembleStep(name='$name', inputs=$inputs, outputs=$outputs, config=$config)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is AssembleStep) return false

    return name == other.name &&
        inputs == other.inputs &&
        outputs == other.outputs &&
        config == other.config
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + inputs.hashCode()
    result = 31 * result + outputs.hashCode()
    result = 31 * result + config.hashCode()
    return result
  }
}
