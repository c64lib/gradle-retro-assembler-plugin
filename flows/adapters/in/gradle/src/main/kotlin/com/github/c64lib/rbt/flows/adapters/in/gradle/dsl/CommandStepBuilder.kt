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
package com.github.c64lib.rbt.flows.adapters.`in`.gradle.dsl

import com.github.c64lib.rbt.flows.domain.steps.CommandStep

/** Type-safe DSL builder for CLI command execution steps. */
class CommandStepBuilder(private val name: String, private val command: String) {
  private val inputs = mutableListOf<String>()
  private val outputs = mutableListOf<String>()
  private val parameters = mutableListOf<String>()

  /** Specifies input sources for this command step. */
  fun from(path: String) {
    inputs.add(path)
  }

  /** Specifies multiple input sources for this command step. */
  fun from(vararg paths: String) {
    inputs.addAll(paths)
  }

  /** Specifies output destination for this command step. */
  fun to(path: String) {
    outputs.add(path)
  }

  /** Specifies multiple output destinations for this command step. */
  fun to(vararg paths: String) {
    outputs.addAll(paths)
  }

  /** Adds a single parameter to the command. */
  fun param(parameter: String) {
    parameters.add(parameter)
  }

  /** Adds multiple parameters to the command. */
  fun params(vararg params: String) {
    parameters.addAll(params)
  }

  /** Adds parameters from a list. */
  fun params(params: List<String>) {
    parameters.addAll(params)
  }

  /** Adds a flag parameter (e.g., "-v" or "--verbose"). */
  fun flag(flag: String) {
    parameters.add(flag)
  }

  /** Adds multiple flag parameters. */
  fun flags(vararg flags: String) {
    parameters.addAll(flags)
  }

  /** Adds a parameter with a value (e.g., "-o", "output.file"). */
  fun option(flag: String, value: String) {
    parameters.add(flag)
    parameters.add(value)
  }

  /** Adds multiple option pairs. */
  fun options(vararg pairs: Pair<String, String>) {
    pairs.forEach { (flag, value) ->
      parameters.add(flag)
      parameters.add(value)
    }
  }

  /**
   * Convenience method for adding common input/output options. Automatically adds the flag and
   * corresponding file path.
   */
  fun inputOption(flag: String, inputPath: String) {
    parameters.add(flag)
    parameters.add(inputPath)
    inputs.add(inputPath)
  }

  /**
   * Convenience method for adding common output options. Automatically adds the flag and
   * corresponding file path.
   */
  fun outputOption(flag: String, outputPath: String) {
    parameters.add(flag)
    parameters.add(outputPath)
    outputs.add(outputPath)
  }

  /**
   * Builder-style method for chaining parameter additions. Returns the builder for method chaining.
   */
  fun with(parameter: String): CommandStepBuilder {
    parameters.add(parameter)
    return this
  }

  /**
   * Builder-style method for chaining multiple parameters. Returns the builder for method chaining.
   */
  fun with(vararg params: String): CommandStepBuilder {
    parameters.addAll(params)
    return this
  }

  /** Builder-style method for chaining option pairs. Returns the builder for method chaining. */
  fun withOption(flag: String, value: String): CommandStepBuilder {
    parameters.add(flag)
    parameters.add(value)
    return this
  }

  /** Clears all parameters. Useful for conditional parameter building. */
  fun clearParams() {
    parameters.clear()
  }

  /** Clears all inputs. */
  fun clearInputs() {
    inputs.clear()
  }

  /** Clears all outputs. */
  fun clearOutputs() {
    outputs.clear()
  }

  /** Gets current parameters (immutable view). */
  fun getCurrentParams(): List<String> = parameters.toList()

  /** Gets current inputs (immutable view). */
  fun getCurrentInputs(): List<String> = inputs.toList()

  /** Gets current outputs (immutable view). */
  fun getCurrentOutputs(): List<String> = outputs.toList()

  /**
   * Returns the input path at the specified index (default: 0 for first input).
   * Useful for referencing input paths defined via [from] in parameter values.
   *
   * @param index Zero-based index into the inputs list. Defaults to 0 (first input).
   * @return The input path at the specified index.
   * @throws IllegalStateException if no inputs have been defined via [from].
   * @throws IndexOutOfBoundsException if the index exceeds the number of inputs.
   *
   * Example:
   * ```kotlin
   * commandStep("process", "tool") {
   *     from("input.txt")
   *     to("output.txt")
   *     param(useFrom())              // Uses "input.txt"
   *     option("-i", useFrom(0))      // Same as useFrom()
   * }
   * ```
   *
   * With multiple inputs:
   * ```kotlin
   * commandStep("process", "tool") {
   *     from("file1.txt", "file2.txt", "file3.txt")
   *     to("output.txt")
   *     param(useFrom(0))             // Uses "file1.txt"
   *     param(useFrom(1))             // Uses "file2.txt"
   *     param(useFrom(2))             // Uses "file3.txt"
   * }
   * ```
   */
  fun useFrom(index: Int = 0): String {
    if (inputs.isEmpty()) {
      throw IllegalStateException(
        "Cannot use useFrom() - no input paths have been defined. " +
        "Call from() first to define input paths."
      )
    }
    if (index < 0 || index >= inputs.size) {
      throw IndexOutOfBoundsException(
        "Cannot access input at index $index - only ${inputs.size} input(s) defined. " +
        "Valid indices: 0..${inputs.size - 1}"
      )
    }
    return inputs[index]
  }

  /**
   * Returns the output path at the specified index (default: 0 for first output).
   * Useful for referencing output paths defined via [to] in parameter values.
   *
   * @param index Zero-based index into the outputs list. Defaults to 0 (first output).
   * @return The output path at the specified index.
   * @throws IllegalStateException if no outputs have been defined via [to].
   * @throws IndexOutOfBoundsException if the index exceeds the number of outputs.
   *
   * Example:
   * ```kotlin
   * commandStep("compress", "exomizer") {
   *     from("input.bin")
   *     to("output.z.bin")
   *     param(useFrom())              // Uses "input.bin"
   *     option("-o", useTo())         // Uses "output.z.bin"
   * }
   * ```
   *
   * With multiple outputs:
   * ```kotlin
   * commandStep("process", "tool") {
   *     from("input.txt")
   *     to("out1.txt", "out2.txt", "out3.txt")
   *     option("-o1", useTo(0))       // Uses "out1.txt"
   *     option("-o2", useTo(1))       // Uses "out2.txt"
   *     option("-o3", useTo(2))       // Uses "out3.txt"
   * }
   * ```
   */
  fun useTo(index: Int = 0): String {
    if (outputs.isEmpty()) {
      throw IllegalStateException(
        "Cannot use useTo() - no output paths have been defined. " +
        "Call to() first to define output paths."
      )
    }
    if (index < 0 || index >= outputs.size) {
      throw IndexOutOfBoundsException(
        "Cannot access output at index $index - only ${outputs.size} output(s) defined. " +
        "Valid indices: 0..${outputs.size - 1}"
      )
    }
    return outputs[index]
  }

  internal fun build(): CommandStep {
    return CommandStep(name, command, inputs.toList(), outputs.toList(), parameters.toList())
  }
}
