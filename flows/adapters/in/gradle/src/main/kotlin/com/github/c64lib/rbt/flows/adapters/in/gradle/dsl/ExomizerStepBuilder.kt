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
 * Type-safe DSL builder for Exomizer compression steps with full option support.
 *
 * Supports both raw and memory compression modes with all 15+ available Exomizer options.
 */
class ExomizerStepBuilder(private val name: String) {
  private val inputs = mutableListOf<String>()
  private val outputs = mutableListOf<String>()
  private var mode: String = "raw"
  // Raw mode options
  private var backwards: Boolean = false
  private var reverse: Boolean = false
  private var decrunch: Boolean = false
  private var compatibility: Boolean = false
  private var speedOverRatio: Boolean = false
  private var encoding: String? = null
  private var skipEncoding: Boolean = false
  private var maxOffset: Int = 65535
  private var maxLength: Int = 65535
  private var passes: Int = 100
  private var bitStreamTraits: Int? = null
  private var bitStreamFormat: Int? = null
  private var controlAddresses: String? = null
  private var quiet: Boolean = false
  private var brief: Boolean = false
  // Memory mode specific options
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
   * Configure raw mode compression with optional block for raw-specific options.
   *
   * @param block Configuration block for raw mode options
   */
  fun raw(block: (RawModeBuilder.() -> Unit)? = null) {
    mode = "raw"
    if (block != null) {
      val builder = RawModeBuilder(this)
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
    val builder = MemModeBuilder(this)
    builder.block()
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
        backwards = backwards,
        reverse = reverse,
        decrunch = decrunch,
        compatibility = compatibility,
        speedOverRatio = speedOverRatio,
        encoding = encoding,
        skipEncoding = skipEncoding,
        maxOffset = maxOffset,
        maxLength = maxLength,
        passes = passes,
        bitStreamTraits = bitStreamTraits,
        bitStreamFormat = bitStreamFormat,
        controlAddresses = controlAddresses,
        quiet = quiet,
        brief = brief,
        loadAddress = loadAddress,
        forward = forward)
  }

  /** Builder for raw mode configuration with all options. */
  class RawModeBuilder(private val parent: ExomizerStepBuilder) {
    var backwards: Boolean
      get() = parent.backwards
      set(value) {
        parent.backwards = value
      }

    var reverse: Boolean
      get() = parent.reverse
      set(value) {
        parent.reverse = value
      }

    var decrunch: Boolean
      get() = parent.decrunch
      set(value) {
        parent.decrunch = value
      }

    var compatibility: Boolean
      get() = parent.compatibility
      set(value) {
        parent.compatibility = value
      }

    var speedOverRatio: Boolean
      get() = parent.speedOverRatio
      set(value) {
        parent.speedOverRatio = value
      }

    var encoding: String?
      get() = parent.encoding
      set(value) {
        parent.encoding = value
      }

    var skipEncoding: Boolean
      get() = parent.skipEncoding
      set(value) {
        parent.skipEncoding = value
      }

    var maxOffset: Int
      get() = parent.maxOffset
      set(value) {
        parent.maxOffset = value
      }

    var maxLength: Int
      get() = parent.maxLength
      set(value) {
        parent.maxLength = value
      }

    var passes: Int
      get() = parent.passes
      set(value) {
        parent.passes = value
      }

    var bitStreamTraits: Int?
      get() = parent.bitStreamTraits
      set(value) {
        parent.bitStreamTraits = value
      }

    var bitStreamFormat: Int?
      get() = parent.bitStreamFormat
      set(value) {
        parent.bitStreamFormat = value
      }

    var controlAddresses: String?
      get() = parent.controlAddresses
      set(value) {
        parent.controlAddresses = value
      }

    var quiet: Boolean
      get() = parent.quiet
      set(value) {
        parent.quiet = value
      }

    var brief: Boolean
      get() = parent.brief
      set(value) {
        parent.brief = value
      }
  }

  /** Builder for memory mode configuration with all options plus memory-specific settings. */
  class MemModeBuilder(private val parent: ExomizerStepBuilder) {
    // All raw mode options accessible in mem mode
    var backwards: Boolean
      get() = parent.backwards
      set(value) {
        parent.backwards = value
      }

    var reverse: Boolean
      get() = parent.reverse
      set(value) {
        parent.reverse = value
      }

    var decrunch: Boolean
      get() = parent.decrunch
      set(value) {
        parent.decrunch = value
      }

    var compatibility: Boolean
      get() = parent.compatibility
      set(value) {
        parent.compatibility = value
      }

    var speedOverRatio: Boolean
      get() = parent.speedOverRatio
      set(value) {
        parent.speedOverRatio = value
      }

    var encoding: String?
      get() = parent.encoding
      set(value) {
        parent.encoding = value
      }

    var skipEncoding: Boolean
      get() = parent.skipEncoding
      set(value) {
        parent.skipEncoding = value
      }

    var maxOffset: Int
      get() = parent.maxOffset
      set(value) {
        parent.maxOffset = value
      }

    var maxLength: Int
      get() = parent.maxLength
      set(value) {
        parent.maxLength = value
      }

    var passes: Int
      get() = parent.passes
      set(value) {
        parent.passes = value
      }

    var bitStreamTraits: Int?
      get() = parent.bitStreamTraits
      set(value) {
        parent.bitStreamTraits = value
      }

    var bitStreamFormat: Int?
      get() = parent.bitStreamFormat
      set(value) {
        parent.bitStreamFormat = value
      }

    var controlAddresses: String?
      get() = parent.controlAddresses
      set(value) {
        parent.controlAddresses = value
      }

    var quiet: Boolean
      get() = parent.quiet
      set(value) {
        parent.quiet = value
      }

    var brief: Boolean
      get() = parent.brief
      set(value) {
        parent.brief = value
      }

    // Memory-specific options
    var loadAddress: String
      get() = parent.loadAddress
      set(value) {
        parent.loadAddress = value
      }

    var forward: Boolean
      get() = parent.forward
      set(value) {
        parent.forward = value
      }
  }
}
