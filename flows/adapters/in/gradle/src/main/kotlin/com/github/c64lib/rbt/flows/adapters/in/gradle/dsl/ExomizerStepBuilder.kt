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

import com.github.c64lib.rbt.flows.domain.steps.ExomizerStep

/**
 * Type-safe DSL builder for Exomizer compression steps.
 *
 * Supports both raw and memory compression modes with flexible configuration.
 */
class ExomizerStepBuilder(private val name: String) {
  private val inputs = mutableListOf<String>()
  private val outputs = mutableListOf<String>()
  private var mode: String = "raw"
  private var loadAddress: String = "auto"
  private var forward: Boolean = false

  /**
   * Specifies the input file to compress.
   *
   * @param path Input file path (relative to project root or absolute)
   */
  fun from(path: String) {
    inputs.clear()
    inputs.add(path)
  }

  /**
   * Specifies the output file path.
   *
   * @param path Output file path (relative to project root or absolute)
   */
  fun to(path: String) {
    outputs.clear()
    outputs.add(path)
  }

  /**
   * Configure raw mode compression.
   *
   * Block parameter is provided for potential future raw-specific options.
   */
  fun raw(block: (RawModeBuilder.() -> Unit)? = null) {
    mode = "raw"
    if (block != null) {
      val builder = RawModeBuilder()
      builder.block()
    }
  }

  /**
   * Configure memory mode compression.
   *
   * @param block Configuration block for memory-specific options
   */
  fun mem(block: MemModeBuilder.() -> Unit) {
    mode = "mem"
    val builder = MemModeBuilder()
    builder.block()
    loadAddress = builder.loadAddress
    forward = builder.forward
  }

  /**
   * Build and return the configured ExomizerStep.
   *
   * @return Configured step ready for execution
   */
  fun build(): ExomizerStep {
    return ExomizerStep(
        name = name,
        inputs = inputs.toList(),
        outputs = outputs.toList(),
        mode = mode,
        loadAddress = loadAddress,
        forward = forward)
  }

  /** Builder for raw mode configuration. */
  class RawModeBuilder {
    // Placeholder for future raw mode options
  }

  /** Builder for memory mode configuration. */
  class MemModeBuilder {
    var loadAddress: String = "auto"
    var forward: Boolean = false
  }
}
