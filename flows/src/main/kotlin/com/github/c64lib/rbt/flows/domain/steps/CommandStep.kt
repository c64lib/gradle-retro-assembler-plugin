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
import com.github.c64lib.rbt.flows.domain.config.CommandConfigMapper
import com.github.c64lib.rbt.flows.domain.port.CommandPort

/**
 * Generic CLI command execution step.
 *
 * Validates command name format. Requires CommandPort injection via Gradle task.
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
    val port = commandPort ?: throw StepExecutionException("CommandPort not injected", name)

    // Extract project root directory from context
    val projectRootDir = getProjectRootDir(context)

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
      throw StepExecutionException("Command execution failed: ${e.message}", name, e)
    }

    outputs.forEach { outputPath -> println("  Expected output: $outputPath") }
  }

  override fun validate(): List<String> {
    val errors = mutableListOf<String>()

    // Basic command validation
    if (command.isBlank()) {
      errors.add("Command cannot be blank")
    }

    // Validate input file paths are not empty
    inputs.forEach { inputPath ->
      if (inputPath.isBlank()) {
        errors.add("Input file path cannot be blank")
      }
    }

    // Validate output file paths are not empty
    outputs.forEach { outputPath ->
      if (outputPath.isBlank()) {
        errors.add("Output file path cannot be blank")
      }
    }

    return errors
  }

  override fun getConfiguration(): Map<String, Any> {
    return mapOf("command" to command, "parameters" to parameters)
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
