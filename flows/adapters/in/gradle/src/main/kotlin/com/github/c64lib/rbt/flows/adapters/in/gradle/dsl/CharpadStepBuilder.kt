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

import com.github.c64lib.rbt.flows.domain.config.*
import com.github.c64lib.rbt.flows.domain.steps.CharpadStep

/** Type-safe DSL builder for Charpad processing steps. */
class CharpadStepBuilder(private val name: String) {
  private val inputs = mutableListOf<String>()
  private val outputs = mutableListOf<String>()

  var compression: CharpadCompression = CharpadCompression.NONE
  var exportFormat: CharpadFormat = CharpadFormat.STANDARD
  var tileSize: Int = 8
  var charsetOptimization: Boolean = true
  var generateMap: Boolean = true
  var generateCharset: Boolean = true

  /** Specifies input sources for this Charpad step. */
  fun from(path: String) {
    inputs.add(path)
  }

  /** Specifies multiple input sources for this Charpad step. */
  fun from(vararg paths: String) {
    inputs.addAll(paths)
  }

  /** Specifies output destination for this Charpad step. */
  fun to(path: String) {
    outputs.add(path)
  }

  /** Specifies multiple output destinations for this Charpad step. */
  fun to(vararg paths: String) {
    outputs.addAll(paths)
  }

  internal fun build(): CharpadStep {
    val config =
        CharpadConfig(
            compression = compression,
            exportFormat = exportFormat,
            tileSize = tileSize,
            charsetOptimization = charsetOptimization,
            generateMap = generateMap,
            generateCharset = generateCharset)
    return CharpadStep(name, inputs, outputs, config)
  }
}
