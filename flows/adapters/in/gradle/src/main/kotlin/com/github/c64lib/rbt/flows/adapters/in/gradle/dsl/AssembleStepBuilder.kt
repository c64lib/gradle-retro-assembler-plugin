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
import com.github.c64lib.rbt.shared.domain.OutputFormat

/** Type-safe DSL builder for Assembly processing steps. */
class AssembleStepBuilder(private val name: String) {
  private val inputs = mutableListOf<String>()
  private val outputs = mutableListOf<String>()

  var cpu: CpuType = CpuType.MOS6510
  var generateSymbols: Boolean = true
  var optimization: AssemblyOptimization = AssemblyOptimization.SPEED
  var verbose: Boolean = false
  var outputFormat: OutputFormat = OutputFormat.PRG
  var workDir: String = ".ra"

  private val includePaths = mutableListOf<String>()
  private val defines = mutableMapOf<String, String>()
  private val srcDirs = mutableListOf<String>()
  private val includes = mutableListOf<String>()
  private val excludes = mutableListOf<String>()
  private val additionalInputs = mutableListOf<String>()

  init {
    // Set defaults that match the enhanced AssemblyConfig
    //    srcDirs.add(".")
    //    includes.add("**/*.asm")
    //    excludes.add(".ra/**/*.asm")
  }

  fun outputFormat(format: OutputFormat) {
    outputFormat = format
  }

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

  /** Sets source directories for file discovery. */
  fun srcDirs(vararg dirs: String) {
    srcDirs.clear() // TODO remove
    srcDirs.addAll(dirs)
  }

  /** Adds a source directory for file discovery. */
  fun srcDir(dir: String) {
    srcDirs.add(dir)
  }

  /** Sets include patterns for file discovery. */
  fun includes(vararg patterns: String) {
    includes.clear() // TODO remove
    includes.addAll(patterns)
  }

  /** Adds an include pattern for file discovery. */
  fun include(pattern: String) {
    includes.add(pattern)
  }

  /** Sets exclude patterns for file discovery. */
  fun excludes(vararg patterns: String) {
    excludes.clear() // TODO remove
    excludes.addAll(patterns)
  }

  /** Adds an exclude pattern for file discovery. */
  fun exclude(pattern: String) {
    excludes.add(pattern)
  }

  /** Sets additional input file patterns for tracking indirect dependencies (includes/imports). */
  fun includeFiles(vararg patterns: String) {
    additionalInputs.addAll(patterns)
  }

  /** Adds a single additional input file pattern for tracking indirect dependencies. */
  fun includeFile(pattern: String) {
    additionalInputs.add(pattern)
  }

  /** Sets file patterns to watch for changes (alias for includeFiles). */
  fun watchFiles(vararg patterns: String) {
    additionalInputs.addAll(patterns)
  }

  /** Adds a single file pattern to watch for changes (alias for includeFile). */
  fun watchFile(pattern: String) {
    additionalInputs.add(pattern)
  }

  internal fun build(): AssembleStep {
    val config =
        AssemblyConfig(
            cpu = cpu,
            generateSymbols = generateSymbols,
            optimization = optimization,
            includePaths = includePaths.toList(),
            defines = defines.toMap(),
            verbose = verbose,
            outputFormat = outputFormat,
            srcDirs = srcDirs.toList(),
            includes = includes.toList(),
            excludes = excludes.toList(),
            workDir = workDir,
            additionalInputs = additionalInputs.toList())
    return AssembleStep(name, inputs, outputs, config)
  }
}
