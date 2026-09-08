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
import com.github.c64lib.rbt.flows.domain.StepExecutionException
import com.github.c64lib.rbt.flows.domain.config.TassConfig
import com.github.c64lib.rbt.flows.domain.config.TassConfigMapper
import com.github.c64lib.rbt.flows.usecase.port.TassAssemblyPort

/**
 * 64tass assembly step for compiling Turbo Assembler dialect 6502 sources using the 64tass
 * cross-assembler.
 *
 * Validates input file extensions (.asm/.s), output format, and CPU selection. Requires
 * TassAssemblyPort injection via Gradle task.
 */
data class TassStep(
    override val name: String,
    override val inputs: List<String> = emptyList(),
    override val outputs: List<String> = emptyList(),
    val config: TassConfig = TassConfig(),
    private var tassPort: TassAssemblyPort? = null,
    private val configMapper: TassConfigMapper = TassConfigMapper()
) : FlowStep(name, "tass", inputs, outputs) {

  companion object {
    val SUPPORTED_OUTPUT_FORMATS = setOf("prg", "bin")

    /** CPU names accepted by 64tass via its --m<cpu> selection options. */
    val SUPPORTED_CPUS =
        setOf(
            "65xx",
            "6502",
            "65c02",
            "r65c02",
            "w65c02",
            "65ce02",
            "4510",
            "45gs02",
            "65816",
            "65dtv02",
            "65el02")
  }

  /**
   * Injects the 64tass assembly port dependency. This is called by the adapter layer when the step
   * is prepared for execution.
   */
  fun setTassPort(port: TassAssemblyPort) {
    this.tassPort = port
  }

  override fun execute(context: Map<String, Any>) {
    val port = tassPort ?: throw StepExecutionException("TassAssemblyPort not injected", name)

    // Extract project root directory from context
    val projectRootDir = getProjectRootDir(context)

    // Convert input paths to source files using base class helper
    val sourceFiles = resolveInputFiles(inputs, projectRootDir)

    // Map configuration to 64tass commands with output handling
    val tassCommands =
        if (sourceFiles.size == 1 && outputs.isNotEmpty()) {
          // Single source file with explicit output - use enhanced mapping
          val outputPath = outputs.first()
          listOf(
              configMapper.toTassCommand(config, sourceFiles.first(), projectRootDir, outputPath))
        } else {
          // Multiple source files or no explicit output - use existing logic
          configMapper.toTassCommands(config, sourceFiles, projectRootDir)
        }

    // Execute 64tass compilation through the port
    try {
      port.assemble(tassCommands)
    } catch (e: Exception) {
      throw StepExecutionException("64tass compilation failed: ${e.message}", name, e)
    }

    outputs.forEach { outputPath -> println("  Generated output: $outputPath") }
  }

  override fun validate(): List<String> {
    val errors = mutableListOf<String>()

    if (inputs.isEmpty()) {
      errors.add("Tass step '$name' requires at least one input .asm file")
    }

    if (outputs.isEmpty()) {
      errors.add("Tass step '$name' requires at least one output file")
    }

    // Validate input file extensions
    inputs.forEach { inputPath ->
      if (!inputPath.endsWith(".asm", ignoreCase = true) &&
          !inputPath.endsWith(".s", ignoreCase = true)) {
        errors.add("Tass step '$name' expects .asm or .s files, but got: $inputPath")
      }
    }

    // Validate output format (prg = CBM with load address, bin = raw binary)
    if (config.outputFormat !in SUPPORTED_OUTPUT_FORMATS) {
      errors.add(
          "Tass step '$name' output format must be one of $SUPPORTED_OUTPUT_FORMATS, " +
              "but got: ${config.outputFormat}")
    }

    // Validate CPU selection so a typo fails here rather than at 64tass execution
    if (config.cpu != null && config.cpu !in SUPPORTED_CPUS) {
      errors.add("Tass step '$name' cpu must be one of $SUPPORTED_CPUS, but got: ${config.cpu}")
    }

    return errors
  }

  override fun getConfiguration(): Map<String, Any> {
    return mapOf(
        "outputFormat" to config.outputFormat,
        "includePaths" to config.includePaths,
        "defines" to config.defines,
        "cpu" to (config.cpu ?: "default"),
        "caseSensitive" to (config.caseSensitive ?: false),
        "quiet" to (config.quiet ?: false))
  }

  override fun toString(): String {
    return "TassStep(name='$name', inputs=$inputs, outputs=$outputs, config=$config)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is TassStep) return false

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
