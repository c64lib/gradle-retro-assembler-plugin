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
import com.github.c64lib.rbt.flows.domain.config.CharpadOutputs
import com.github.c64lib.rbt.flows.domain.config.FilterConfig
import com.github.c64lib.rbt.flows.domain.port.CharpadPort
import java.io.File

/** Domain model for Charpad processing steps with type-safe configuration. */
class CharpadStep(
    name: String,
    inputs: List<String> = emptyList(),
    val charpadOutputs: CharpadOutputs,
    val config: CharpadConfig = CharpadConfig(),
    private var charpadPort: CharpadPort? = null
) : FlowStep(name, "charpad", inputs, charpadOutputs.getAllOutputPaths()) {

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

    // Create CharpadCommand instances for each input file
    val charpadCommands =
        inputFiles.map { inputFile ->
          CharpadCommand(
              inputFile = inputFile,
              charpadOutputs = charpadOutputs,
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

  override fun validate(): List<String> {
    val errors = mutableListOf<String>()

    if (inputs.isEmpty()) {
      errors.add("Charpad step '$name' requires at least one input .ctm file")
    }

    if (!charpadOutputs.hasOutputs()) {
      errors.add("Charpad step '$name' requires at least one output configuration")
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

    // Validate output configurations
    charpadOutputs.charsets.forEach { charset ->
      // Allow empty output path only if a filter is configured
      if (charset.output.isEmpty() && charset.filter == FilterConfig.None) {
        errors.add("Charpad step '$name': charset output path cannot be empty")
      }
      if (charset.start < 0 || charset.end < 0 || charset.start >= charset.end) {
        errors.add(
            "Charpad step '$name': charset start/end range invalid: start=${charset.start}, end=${charset.end}")
      }
    }

    charpadOutputs.maps.forEach { map ->
      // Allow empty output path only if a filter is configured
      if (map.output.isEmpty() && map.filter == FilterConfig.None) {
        errors.add("Charpad step '$name': map output path cannot be empty")
      }
      if (map.left < 0 || map.top < 0 || map.right < 0 || map.bottom < 0) {
        errors.add(
            "Charpad step '$name': map coordinates cannot be negative: left=${map.left}, top=${map.top}, right=${map.right}, bottom=${map.bottom}")
      }
      if (map.left >= map.right || map.top >= map.bottom) {
        errors.add(
            "Charpad step '$name': map rectangular region invalid: left=${map.left}, top=${map.top}, right=${map.right}, bottom=${map.bottom}")
      }
    }

    charpadOutputs.metadata.forEach { meta ->
      if (meta.output.isEmpty()) {
        errors.add("Charpad step '$name': metadata output path cannot be empty")
      }
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
        "includeMode" to config.includeMode,
        "charsetOutputs" to charpadOutputs.charsets.size,
        "mapOutputs" to charpadOutputs.maps.size,
        "metadataOutputs" to charpadOutputs.metadata.size)
  }

  override fun toString(): String {
    return "CharpadStep(name='$name', inputs=$inputs, outputs=${charpadOutputs.getAllOutputPaths()}, config=$config)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is CharpadStep) return false

    return name == other.name &&
        inputs == other.inputs &&
        charpadOutputs == other.charpadOutputs &&
        config == other.config
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + inputs.hashCode()
    result = 31 * result + charpadOutputs.hashCode()
    result = 31 * result + config.hashCode()
    return result
  }
}
