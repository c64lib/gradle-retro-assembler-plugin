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
import com.github.c64lib.rbt.flows.domain.config.DasmConfig
import com.github.c64lib.rbt.flows.domain.config.DasmConfigMapper
import com.github.c64lib.rbt.flows.domain.port.DasmAssemblyPort

/**
 * Dasm assembly step for compiling 6502 assembly files using the dasm assembler.
 *
 * Validates input file extensions (.asm/.s) and output file specification. Requires
 * DasmAssemblyPort injection via Gradle task.
 */
data class DasmStep(
    override val name: String,
    override val inputs: List<String> = emptyList(),
    override val outputs: List<String> = emptyList(),
    val config: DasmConfig = DasmConfig(),
    private var dasmPort: DasmAssemblyPort? = null,
    private val configMapper: DasmConfigMapper = DasmConfigMapper()
) : FlowStep(name, "dasm", inputs, outputs) {

  /**
   * Injects the dasm assembly port dependency. This is called by the adapter layer when the step is
   * prepared for execution.
   */
  fun setDasmPort(port: DasmAssemblyPort) {
    this.dasmPort = port
  }

  override fun execute(context: Map<String, Any>) {
    val port = dasmPort ?: throw StepExecutionException("DasmAssemblyPort not injected", name)

    // Extract project root directory from context
    val projectRootDir = getProjectRootDir(context)

    // Convert input paths to source files using base class helper
    val sourceFiles = resolveInputFiles(inputs, projectRootDir)

    // Map configuration to dasm commands with output handling
    val dasmCommands =
        if (sourceFiles.size == 1 && outputs.isNotEmpty()) {
          // Single source file with explicit output - use enhanced mapping
          val outputPath = outputs.first()
          listOf(
              configMapper.toDasmCommand(config, sourceFiles.first(), projectRootDir, outputPath))
        } else {
          // Multiple source files or no explicit output - use existing logic
          configMapper.toDasmCommands(config, sourceFiles, projectRootDir)
        }

    // Execute dasm compilation through the port
    try {
      port.assemble(dasmCommands)
    } catch (e: Exception) {
      throw StepExecutionException("Dasm compilation failed: ${e.message}", name, e)
    }

    outputs.forEach { outputPath -> println("  Generated output: $outputPath") }
  }

  override fun validate(): List<String> {
    val errors = mutableListOf<String>()

    if (inputs.isEmpty()) {
      errors.add("Dasm step '$name' requires at least one input .asm file")
    }

    if (outputs.isEmpty()) {
      errors.add("Dasm step '$name' requires at least one output file")
    }

    // Validate input file extensions
    inputs.forEach { inputPath ->
      if (!inputPath.endsWith(".asm", ignoreCase = true) &&
          !inputPath.endsWith(".s", ignoreCase = true)) {
        errors.add("Dasm step '$name' expects .asm or .s files, but got: $inputPath")
      }
    }

    // Validate output format (1-3 for dasm)
    if (config.outputFormat !in 1..3) {
      errors.add("Dasm step '$name' output format must be 1-3, but got: ${config.outputFormat}")
    }

    // Validate verboseness (0-4 for dasm)
    if (config.verboseness != null && config.verboseness !in 0..4) {
      errors.add("Dasm step '$name' verboseness must be 0-4, but got: ${config.verboseness}")
    }

    // Validate error format (0=MS, 1=Dillon, 2=GNU)
    if (config.errorFormat != null && config.errorFormat !in 0..2) {
      errors.add("Dasm step '$name' error format must be 0-2, but got: ${config.errorFormat}")
    }

    // Validate symbol table sort (0=alphabetical, 1=address)
    if (config.symbolTableSort != null && config.symbolTableSort !in 0..1) {
      errors.add(
          "Dasm step '$name' symbol table sort must be 0-1, but got: ${config.symbolTableSort}")
    }

    return errors
  }

  override fun getConfiguration(): Map<String, Any> {
    return mapOf(
        "outputFormat" to config.outputFormat,
        "includePaths" to config.includePaths,
        "defines" to config.defines,
        "verboseness" to (config.verboseness ?: "none"),
        "strictSyntax" to (config.strictSyntax ?: false),
        "removeOnError" to (config.removeOnError ?: false))
  }

  override fun toString(): String {
    return "DasmStep(name='$name', inputs=$inputs, outputs=$outputs, config=$config)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is DasmStep) return false

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
