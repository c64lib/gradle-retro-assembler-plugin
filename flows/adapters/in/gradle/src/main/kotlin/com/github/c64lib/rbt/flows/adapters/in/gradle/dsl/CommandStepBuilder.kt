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

  internal fun build(): CommandStep {
    return CommandStep(name, command, inputs.toList(), outputs.toList(), parameters.toList())
  }
}
