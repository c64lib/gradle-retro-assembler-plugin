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
 * A generic flow step that represents traditional processor-based tasks. This is used for existing
 * step types like charpad, spritepad, assemble, etc.
 */
class GenericStep(
    name: String,
    taskType: String,
    inputs: List<String> = emptyList(),
    outputs: List<String> = emptyList(),
    private val configuration: Map<String, Any> = emptyMap()
) : FlowStep(name, taskType, inputs, outputs) {

  override fun execute(context: Map<String, Any>) {
    // TODO: Implement actual step execution based on taskType
    // This will integrate with the existing processor framework
    println("Executing $taskType step: $name")

    if (inputs.isNotEmpty()) {
      println("  Inputs: ${inputs.joinToString(", ")}")
    }
    if (outputs.isNotEmpty()) {
      println("  Outputs: ${outputs.joinToString(", ")}")
    }
    if (configuration.isNotEmpty()) {
      println("  Configuration: $configuration")
    }
  }

  override fun validate(): List<String> {
    val errors = mutableListOf<String>()

    // Validate based on task type
    when (taskType) {
      "charpad",
      "spritepad" -> {
        if (inputs.isEmpty()) {
          errors.add("$taskType step requires input files")
        }
        if (outputs.isEmpty()) {
          errors.add("$taskType step requires output destination")
        }
      }
      "assemble" -> {
        if (inputs.isEmpty()) {
          errors.add("Assemble step requires source files")
        }
        if (outputs.isEmpty()) {
          errors.add("Assemble step requires output destination")
        }
      }
    }

    return errors
  }

  override fun getConfiguration(): Map<String, Any> {
    return configuration +
        mapOf("taskType" to taskType, "inputCount" to inputs.size, "outputCount" to outputs.size)
  }

  override fun toString(): String {
    return "GenericStep(name='$name', taskType='$taskType', inputs=$inputs, outputs=$outputs, config=$configuration)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is GenericStep) return false

    return name == other.name &&
        taskType == other.taskType &&
        inputs == other.inputs &&
        outputs == other.outputs &&
        configuration == other.configuration
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + taskType.hashCode()
    result = 31 * result + inputs.hashCode()
    result = 31 * result + outputs.hashCode()
    result = 31 * result + configuration.hashCode()
    return result
  }
}
