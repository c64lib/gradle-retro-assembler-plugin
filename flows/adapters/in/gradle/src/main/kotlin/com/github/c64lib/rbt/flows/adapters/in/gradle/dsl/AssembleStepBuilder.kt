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
import com.github.c64lib.rbt.flows.domain.steps.AssembleStep

/** Type-safe DSL builder for Assembly processing steps. */
class AssembleStepBuilder(private val name: String) {
  private val inputs = mutableListOf<String>()
  private val outputs = mutableListOf<String>()

  var cpu: CpuType = CpuType.MOS6510
  var generateSymbols: Boolean = true
  var optimization: AssemblyOptimization = AssemblyOptimization.SPEED
  var verbose: Boolean = false

  private val includePaths = mutableListOf<String>()
  private val defines = mutableMapOf<String, String>()

  /** Specifies input sources for this Assembly step. */
  fun from(path: String) {
    inputs.add(path)
  }

  /** Specifies multiple input sources for this Assembly step. */
  fun from(vararg paths: String) {
    inputs.addAll(paths)
  }

  /** Specifies output destination for this Assembly step. */
  fun to(path: String) {
    outputs.add(path)
  }

  /** Specifies multiple output destinations for this Assembly step. */
  fun to(vararg paths: String) {
    outputs.addAll(paths)
  }

  /** Adds include paths for the assembler. */
  fun includePaths(vararg paths: String) {
    includePaths.addAll(paths)
  }

  /** Adds a single include path for the assembler. */
  fun includePath(path: String) {
    includePaths.add(path)
  }

  /** Adds preprocessor defines. */
  fun define(name: String, value: String) {
    defines[name] = value
  }

  /** Adds multiple preprocessor defines. */
  fun defines(vararg pairs: Pair<String, String>) {
    defines.putAll(pairs)
  }

  internal fun build(): AssembleStep {
    val config =
        AssemblyConfig(
            cpu = cpu,
            generateSymbols = generateSymbols,
            optimization = optimization,
            includePaths = includePaths.toList(),
            defines = defines.toMap(),
            verbose = verbose)
    return AssembleStep(name, inputs, outputs, config)
  }
}
