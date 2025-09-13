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
import com.github.c64lib.rbt.flows.domain.config.CharpadCommand
import com.github.c64lib.rbt.flows.domain.config.CharpadConfig
import com.github.c64lib.rbt.flows.domain.port.CharpadPort
import java.io.File

/** Domain model for Charpad processing steps with type-safe configuration. */
class CharpadStep(
    name: String,
    inputs: List<String> = emptyList(),
    outputs: List<String> = emptyList(),
    val config: CharpadConfig = CharpadConfig(),
    private var charpadPort: CharpadPort? = null
) : FlowStep(name, "charpad", inputs, outputs) {

  /**
   * Injects the charpad port dependency. This is called by the adapter layer when the step is
   * prepared for execution.
   */
  fun setCharpadPort(port: CharpadPort) {
    this.charpadPort = port
  }

  override fun execute(context: Map<String, Any>) {
    val port =
        charpadPort
            ?: throw IllegalStateException(
                "CharpadPort not injected for step '$name'. Call setCharpadPort() before execution.")

    // Extract project root directory from context
    val projectRootDir =
        context["projectRootDir"] as? File
            ?: throw IllegalStateException("Project root directory not found in execution context")

    // Convert input paths to CTM files
    val inputFiles =
        inputs.map { inputPath ->
          val file =
              if (File(inputPath).isAbsolute) {
                File(inputPath)
              } else {
                File(projectRootDir, inputPath)
              }

          if (!file.exists()) {
            throw IllegalArgumentException("CTM file does not exist: ${file.absolutePath}")
          }

          file
        }

    // Create output files map from outputs list
    // For charpad, we support multiple output types but need to determine the mapping
    val outputFilesMap = createOutputFilesMap(outputs, projectRootDir)

    // Create CharpadCommand instances for each input file
    val charpadCommands =
        inputFiles.map { inputFile ->
          CharpadCommand(
              inputFile = inputFile,
              outputFiles = outputFilesMap,
              config = config,
              projectRootDir = projectRootDir)
        }

    // Execute charpad processing through the port
    try {
      port.process(charpadCommands)
    } catch (e: Exception) {
      throw RuntimeException("Charpad processing failed for step '$name': ${e.message}", e)
    }

    outputs.forEach { outputPath -> println("  Generated output: $outputPath") }
  }

  /**
   * Creates a map of output file types to File objects from the outputs list. For charpad
   * processing, we need to determine which output corresponds to which type (charset, map, tiles,
   * etc.)
   */
  private fun createOutputFilesMap(outputs: List<String>, projectRootDir: File): Map<String, File> {
    val outputMap = mutableMapOf<String, File>()

    outputs.forEachIndexed { index, outputPath ->
      val file =
          if (File(outputPath).isAbsolute) {
            File(outputPath)
          } else {
            File(projectRootDir, outputPath)
          }

      // Determine output type based on file extension or position
      val outputKey =
          when {
            outputPath.contains("charset", ignoreCase = true) ||
                outputPath.endsWith(".chr", ignoreCase = true) -> "charset"
            outputPath.contains("map", ignoreCase = true) ||
                outputPath.endsWith(".map", ignoreCase = true) -> "map"
            outputPath.contains("tiles", ignoreCase = true) ||
                outputPath.endsWith(".tiles", ignoreCase = true) -> "tiles"
            outputPath.contains("header", ignoreCase = true) ||
                outputPath.contains("metadata", ignoreCase = true) ||
                outputPath.endsWith(".h", ignoreCase = true) ||
                outputPath.endsWith(".inc", ignoreCase = true) -> "header"
            outputPath.contains("attributes", ignoreCase = true) -> "charattributes"
            outputPath.contains("colours", ignoreCase = true) -> "charcolours"
            outputPath.contains("materials", ignoreCase = true) -> "charmaterials"
            outputPath.contains("screen", ignoreCase = true) -> "charscreencolours"
            else -> "output$index" // Fallback to indexed naming
          }

      outputMap[outputKey] = file
    }

    return outputMap
  }

  override fun validate(): List<String> {
    val errors = mutableListOf<String>()

    if (inputs.isEmpty()) {
      errors.add("Charpad step '$name' requires at least one input .ctm file")
    }

    if (outputs.isEmpty()) {
      errors.add("Charpad step '$name' requires at least one output file")
    }

    // Validate input file extensions
    inputs.forEach { inputPath ->
      if (!inputPath.endsWith(".ctm", ignoreCase = true)) {
        errors.add("Charpad step '$name' expects .ctm files, but got: $inputPath")
      }
    }

    // Validate tile size
    if (config.tileSize !in listOf(8, 16, 32)) {
      errors.add("Charpad step '$name' tile size must be 8, 16, or 32, but got: ${config.tileSize}")
    }

    return errors
  }

  override fun getConfiguration(): Map<String, Any> {
    return mapOf(
        "compression" to config.compression.name,
        "exportFormat" to config.exportFormat.name,
        "tileSize" to config.tileSize,
        "charsetOptimization" to config.charsetOptimization,
        "generateMap" to config.generateMap,
        "generateCharset" to config.generateCharset,
        "ctm8PrototypeCompatibility" to config.ctm8PrototypeCompatibility,
        "namespace" to config.namespace,
        "prefix" to config.prefix,
        "includeVersion" to config.includeVersion,
        "includeBgColours" to config.includeBgColours,
        "includeCharColours" to config.includeCharColours,
        "includeMode" to config.includeMode)
  }

  override fun toString(): String {
    return "CharpadStep(name='$name', inputs=$inputs, outputs=$outputs, config=$config)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is CharpadStep) return false

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
