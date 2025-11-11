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
import com.github.c64lib.rbt.flows.domain.config.CommandConfigMapper
import com.github.c64lib.rbt.flows.domain.port.CommandPort
import java.io.File

/**
 * Generic CLI command execution step.
 *
 * Validates: command name format, parameter safety
 * Requires: CommandPort injection via Gradle task
 */
data class CommandStep(
    override val name: String,
    val command: String,
    override val inputs: List<String> = emptyList(),
    override val outputs: List<String> = emptyList(),
    val parameters: List<String> = emptyList(),
    private var commandPort: CommandPort? = null,
    private val configMapper: CommandConfigMapper = CommandConfigMapper()
) : FlowStep(name, "command", inputs, outputs) {

  /**
   * Injects the command port dependency. This is called by the adapter layer when the step is
   * prepared for execution.
   */
  fun setCommandPort(port: CommandPort) {
    this.commandPort = port
  }

  /** Add a parameter to the command */
  operator fun plus(parameter: String): CommandStep {
    return CommandStep(
        name, command, inputs, outputs, parameters + parameter, commandPort, configMapper)
  }

  /** Set input paths for this command step */
  fun from(vararg paths: String): CommandStep {
    return CommandStep(
        name, command, paths.toList(), outputs, parameters, commandPort, configMapper)
  }

  /** Set output paths for this command step */
  fun to(vararg paths: String): CommandStep {
    return CommandStep(name, command, inputs, paths.toList(), parameters, commandPort, configMapper)
  }

  /** Get the full command line that would be executed */
  fun getCommandLine(): List<String> {
    return listOf(command) + parameters
  }

  override fun execute(context: Map<String, Any>) {
    val port =
        commandPort
            ?: throw IllegalStateException(
                "CommandPort not injected for step '$name'. Call setCommandPort() before execution.")

    // Extract project root directory from context
    val projectRootDir =
        context["projectRootDir"] as? File
            ?: throw IllegalStateException("Project root directory not found in execution context")

    // Extract environment variables from context if available
    @Suppress("UNCHECKED_CAST")
    val environment = context["environment"] as? Map<String, String> ?: emptyMap()

    // Extract timeout from context if available
    val timeoutSeconds = context["timeoutSeconds"] as? Long

    // Map step configuration to command
    val commandCommand =
        configMapper.toCommandCommand(this, projectRootDir, environment, timeoutSeconds)

    // Execute CLI command through the port
    try {
      port.execute(commandCommand)
    } catch (e: Exception) {
      throw RuntimeException("Command execution failed for step '$name': ${e.message}", e)
    }

    outputs.forEach { outputPath -> println("  Expected output: $outputPath") }
  }

  override fun validate(): List<String> {
    val errors = mutableListOf<String>()

    // Basic command validation
    if (command.isBlank()) {
      errors.add("Command cannot be blank")
    }

    // Validate command executable name
    if (command.contains("/") || command.contains("\\")) {
      // If command contains path separators, validate it as a file path
      val commandFile = File(command)
      if (!commandFile.exists()) {
        errors.add("Command executable not found: $command")
      } else if (!commandFile.canExecute()) {
        errors.add("Command file is not executable: $command")
      }
    } else {
      // For simple command names, we can't easily validate existence without PATH lookup
      // But we can validate that it's a reasonable command name
      if (command.isNotBlank() && !command.matches(Regex("[a-zA-Z0-9._-]+"))) {
        errors.add("Command name contains invalid characters: $command")
      }
    }

    // Validate parameters don't contain suspicious characters that could cause issues
    parameters.forEach { param ->
      if (param.contains("\n") || param.contains("\r")) {
        errors.add("Command parameter contains line breaks: '$param'")
      }
      // Note: Parameters containing ';', '|', or '&' are allowed as ProcessBuilder handles them
      // safely
    }

    // Validate input file paths are not empty and have reasonable format
    inputs.forEach { inputPath ->
      if (inputPath.isBlank()) {
        errors.add("Input file path cannot be blank")
      } else {
        val inputFile = File(inputPath)
        // Check for obviously invalid paths
        if (inputPath.contains("..")) {
          errors.add("Input file path contains '..' which could be unsafe: $inputPath")
        }
        // If it's an absolute path, we can do basic validation
        if (inputFile.isAbsolute) {
          val parentDir = inputFile.parentFile
          if (parentDir != null && !parentDir.exists()) {
            errors.add("Input file parent directory does not exist: ${parentDir.absolutePath}")
          }
        }
      }
    }

    // Validate output file paths are not empty and have reasonable format
    outputs.forEach { outputPath ->
      if (outputPath.isBlank()) {
        errors.add("Output file path cannot be blank")
      } else {
        val outputFile = File(outputPath)
        // Check for obviously invalid paths
        if (outputPath.contains("..")) {
          errors.add("Output file path contains '..' which could be unsafe: $outputPath")
        }
        // Validate that parent directory is valid (if absolute path)
        if (outputFile.isAbsolute) {
          val parentDir = outputFile.parentFile
          if (parentDir != null) {
            // Check if parent directory path is reasonable
            val parentPath = parentDir.absolutePath
            if (parentPath.length > 260) { // Windows path length limit
              errors.add("Output file parent directory path is too long: $parentPath")
            }
          }
        }
      }
    }

    // Validate that we have either inputs or outputs (or both) - commands should do something
    if (inputs.isEmpty() && outputs.isEmpty()) {
      errors.add("Command step should declare either input files, output files, or both")
    }

    // Validate command line length doesn't exceed reasonable limits
    val commandLine = getCommandLine()
    val commandLineString = commandLine.joinToString(" ")
    if (commandLineString.length > 8192) { // Reasonable command line length limit
      errors.add(
          "Command line is too long (${commandLineString.length} characters): consider using parameter files or environment variables")
    }

    // Validate parameter consistency - check for common mistakes
    validateParameterConsistency(errors)

    return errors
  }

  /** Validates parameter consistency and checks for common configuration mistakes. */
  private fun validateParameterConsistency(errors: MutableList<String>) {
    // Check for duplicate parameters that might indicate configuration errors
    val parameterCounts = parameters.groupingBy { it }.eachCount()
    parameterCounts.forEach { (param, count) ->
      if (count > 1 && !param.startsWith("-")) {
        // Non-flag parameters shouldn't be duplicated usually
        errors.add("Parameter '$param' appears $count times - this might be unintentional")
      }
    }

    // Check for conflicting output specifications
    val outputFlags = parameters.filter { it == "-o" || it == "--output" }
    if (outputFlags.isNotEmpty() && outputs.isNotEmpty()) {
      errors.add(
          "Both command parameters and step outputs specify output files - this might cause conflicts")
    }

    // Check for input/output parameter consistency
    val inputFlags = parameters.filter { it == "-i" || it == "--input" }
    if (inputFlags.size > inputs.size) {
      errors.add(
          "More input flags in parameters (${inputFlags.size}) than declared input files (${inputs.size})")
    }

    // Validate parameter pairing (flags that require values)
    validateParameterPairing(errors)
  }

  /** Validates that flag parameters are properly paired with their values. */
  private fun validateParameterPairing(errors: MutableList<String>) {
    val flagsRequiringValues =
        setOf("-o", "--output", "-i", "--input", "-f", "--file", "-d", "--directory")

    for (i in parameters.indices) {
      val param = parameters[i]
      if (flagsRequiringValues.contains(param)) {
        if (i + 1 >= parameters.size) {
          errors.add("Flag '$param' requires a value but none provided")
        } else {
          val nextParam = parameters[i + 1]
          if (nextParam.startsWith("-")) {
            errors.add(
                "Flag '$param' appears to be followed by another flag '$nextParam' instead of a value")
          }
        }
      }
    }
  }

  override fun getConfiguration(): Map<String, Any> {
    return mapOf(
        "command" to command,
        "parameters" to parameters,
        "commandLine" to getCommandLine().joinToString(" "))
  }

  override fun toString(): String {
    return "CommandStep(name='$name', command='$command', parameters=$parameters, inputs=$inputs, outputs=$outputs)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is CommandStep) return false

    return name == other.name &&
        command == other.command &&
        inputs == other.inputs &&
        outputs == other.outputs &&
        parameters == other.parameters
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + command.hashCode()
    result = 31 * result + inputs.hashCode()
    result = 31 * result + outputs.hashCode()
    result = 31 * result + parameters.hashCode()
    return result
  }

}
