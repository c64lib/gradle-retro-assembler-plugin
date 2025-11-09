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
import com.github.c64lib.rbt.flows.domain.config.GoattrackerCommand
import com.github.c64lib.rbt.flows.domain.config.GoattrackerConfig
import com.github.c64lib.rbt.flows.domain.port.GoattrackerPort
import java.io.File

/** Domain model for GoatTracker processing steps with type-safe configuration. */
class GoattrackerStep(
    name: String,
    inputs: List<String> = emptyList(),
    outputs: List<String> = emptyList(),
    val config: GoattrackerConfig = GoattrackerConfig()
) : FlowStep(name, "goattracker", inputs, outputs) {

  private var goattrackerPort: GoattrackerPort? = null

  fun setGoattrackerPort(port: GoattrackerPort) {
    goattrackerPort = port
  }

  override fun execute(context: Map<String, Any>) {
    val port =
        goattrackerPort
            ?: throw IllegalStateException(
                "GoatTracker port is not injected. Cannot execute GoattrackerStep '$name'")

    // Extract project root from context
    @Suppress("UNCHECKED_CAST")
    val projectRootDir =
        (context["projectRootDir"] as? File)
            ?: throw IllegalStateException(
                "projectRootDir not found in execution context for GoattrackerStep '$name'")

    // Create GoattrackerCommand for each input/output pair
    val commands = mutableListOf<GoattrackerCommand>()

    inputs.forEachIndexed { index, inputPath ->
      val inputFile =
          File(inputPath).let { file ->
            if (file.isAbsolute) file else File(projectRootDir, inputPath)
          }

      // Validate input file
      if (!inputFile.exists()) {
        throw IllegalArgumentException(
            "Input file not found for GoattrackerStep '$name': $inputPath (resolved to ${inputFile.absolutePath})")
      }

      if (!inputFile.isFile) {
        throw IllegalArgumentException(
            "Input path is not a file for GoattrackerStep '$name': $inputPath (resolved to ${inputFile.absolutePath})")
      }

      if (!inputFile.canRead()) {
        throw IllegalArgumentException(
            "Input file is not readable for GoattrackerStep '$name': $inputPath (resolved to ${inputFile.absolutePath})")
      }

      // Get output file for this input
      val outputPath =
          if (index < outputs.size) outputs[index]
          else {
            throw IllegalStateException(
                "GoattrackerStep '$name' has ${inputs.size} inputs but only ${outputs.size} outputs")
          }

      val outputFile =
          File(outputPath).let { file ->
            if (file.isAbsolute) file else File(projectRootDir, outputPath)
          }

      // Create command
      commands.add(
          GoattrackerCommand(
              inputFile = inputFile,
              output = outputFile,
              config = config,
              projectRootDir = projectRootDir))
    }

    // Process all commands
    port.process(commands)
  }

  override fun validate(): List<String> {
    val errors = mutableListOf<String>()

    if (inputs.isEmpty()) {
      errors.add("GoatTracker step '$name' requires at least one input .sng file")
    }

    if (outputs.isEmpty()) {
      errors.add("GoatTracker step '$name' requires at least one output file")
    }

    // Validate input file extensions
    inputs.forEach { inputPath ->
      if (!inputPath.endsWith(".sng", ignoreCase = true)) {
        errors.add("GoatTracker step '$name' expects .sng files, but got: $inputPath")
      }
    }

    // Validate channels
    if (config.channels !in 1..3) {
      errors.add(
          "GoatTracker step '$name' channels must be between 1 and 3, but got: ${config.channels}")
    }

    return errors
  }

  override fun getConfiguration(): Map<String, Any> {
    val configMap =
        mutableMapOf<String, Any>(
            "frequency" to config.frequency.name,
            "channels" to config.channels,
            "optimization" to config.optimization,
            "executable" to config.executable)

    // Add optional parameters if set
    config.bufferedSidWrites?.let { configMap["bufferedSidWrites"] = it }
    config.disableOptimization?.let { configMap["disableOptimization"] = it }
    config.playerMemoryLocation?.let { configMap["playerMemoryLocation"] = it }
    config.sfxSupport?.let { configMap["sfxSupport"] = it }
    config.sidMemoryLocation?.let { configMap["sidMemoryLocation"] = it }
    config.storeAuthorInfo?.let { configMap["storeAuthorInfo"] = it }
    config.volumeChangeSupport?.let { configMap["volumeChangeSupport"] = it }
    config.zeroPageLocation?.let { configMap["zeroPageLocation"] = it }
    config.zeropageGhostRegisters?.let { configMap["zeropageGhostRegisters"] = it }

    return configMap
  }

  override fun toString(): String {
    return "GoattrackerStep(name='$name', inputs=$inputs, outputs=$outputs, config=$config)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is GoattrackerStep) return false

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
