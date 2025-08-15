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
package com.github.c64lib.rbt.flows.domain

/**
 * A command-based flow step that can execute any CLI command with parameters.
 *
 * Example usage:
 * ```kotlin
 * val step = CommandStep("compile", "kickass")
 *     .from("src/main.asm")
 *     .to("build/main.prg")
 *     + "-cpu" + "6510"
 *     + "-o" + outputPath
 * ```
 */
class CommandStep(
    name: String,
    private val command: String,
    inputs: List<String> = emptyList(),
    outputs: List<String> = emptyList(),
    private val parameters: List<String> = emptyList()
) : FlowStep(name, "command", inputs, outputs) {

  /** Add a parameter to the command */
  operator fun plus(parameter: String): CommandStep {
    return CommandStep(name, command, inputs, outputs, parameters + parameter)
  }

  /** Set input paths for this command step */
  fun from(vararg paths: String): CommandStep {
    return CommandStep(name, command, paths.toList(), outputs, parameters)
  }

  /** Set output paths for this command step */
  fun to(vararg paths: String): CommandStep {
    return CommandStep(name, command, inputs, paths.toList(), parameters)
  }

  /** Get the full command line that would be executed */
  fun getCommandLine(): List<String> {
    return listOf(command) + parameters
  }

  override fun execute(context: Map<String, Any>) {
    val commandLine = getCommandLine()

    // TODO: Implement actual command execution
    // This will be implemented when integrating with the execution framework
    println("Executing command: ${commandLine.joinToString(" ")}")

    if (inputs.isNotEmpty()) {
      println("  Inputs: ${inputs.joinToString(", ")}")
    }
    if (outputs.isNotEmpty()) {
      println("  Outputs: ${outputs.joinToString(", ")}")
    }
  }

  override fun validate(): List<String> {
    val errors = mutableListOf<String>()

    if (command.isBlank()) {
      errors.add("Command cannot be blank")
    }

    // Validate input files exist (when context allows)
    // TODO: Add file existence validation when filesystem access is available

    return errors
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
