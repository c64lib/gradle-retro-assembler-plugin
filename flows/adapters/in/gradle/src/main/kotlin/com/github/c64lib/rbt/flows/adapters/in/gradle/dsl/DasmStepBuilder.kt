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

import com.github.c64lib.rbt.flows.domain.config.DasmConfig
import com.github.c64lib.rbt.flows.domain.steps.DasmStep

/** Type-safe DSL builder for dasm assembly processing steps. */
class DasmStepBuilder(private val name: String) {
  private val inputs = mutableListOf<String>()
  private val outputs = mutableListOf<String>()

  var outputFormat: Int = 1
  var verboseness: Int? = null
  var errorFormat: Int? = null
  var strictSyntax: Boolean? = null
  var removeOnError: Boolean? = null
  var symbolTableSort: Int? = null
  var workDir: String = ".ra"

  private val includePaths = mutableListOf<String>()
  private val defines = mutableMapOf<String, String>()
  private val srcDirs = mutableListOf<String>()
  private val includes = mutableListOf<String>()
  private val excludes = mutableListOf<String>()
  private val additionalInputs = mutableListOf<String>()
  private var listFile: String? = null
  private var symbolFile: String? = null

  /** Specifies input sources for this dasm step. */
  fun from(path: String) {
    inputs.add(path)
  }

  /** Specifies multiple input sources for this dasm step. */
  fun from(vararg paths: String) {
    inputs.addAll(paths)
  }

  /** Specifies output destination for this dasm step. */
  fun to(path: String) {
    outputs.add(path)
  }

  /** Specifies multiple output destinations for this dasm step. */
  fun to(vararg paths: String) {
    outputs.addAll(paths)
  }

  /** Adds include paths for dasm. */
  fun includePaths(vararg paths: String) {
    includePaths.addAll(paths)
  }

  /** Adds a single include path for dasm. */
  fun includePath(path: String) {
    includePaths.add(path)
  }

  /** Adds preprocessor defines. */
  fun define(name: String, value: String = "") {
    defines[name] = value
  }

  /** Adds multiple preprocessor defines. */
  fun defines(vararg pairs: Pair<String, String>) {
    defines.putAll(pairs)
  }

  /** Sets source directories for file discovery. */
  fun srcDirs(vararg dirs: String) {
    srcDirs.addAll(dirs)
  }

  /** Adds a source directory for file discovery. */
  fun srcDir(dir: String) {
    srcDirs.add(dir)
  }

  /** Sets file inclusion patterns. */
  fun includes(vararg patterns: String) {
    includes.addAll(patterns)
  }

  /** Adds a file inclusion pattern. */
  fun include(pattern: String) {
    includes.add(pattern)
  }

  /** Sets file exclusion patterns. */
  fun excludes(vararg patterns: String) {
    excludes.addAll(patterns)
  }

  /** Adds a file exclusion pattern. */
  fun exclude(pattern: String) {
    excludes.add(pattern)
  }

  /** Adds patterns for additional input files (dependencies). */
  fun additionalInputs(vararg patterns: String) {
    additionalInputs.addAll(patterns)
  }

  /** Adds a pattern for additional input files. */
  fun additionalInput(pattern: String) {
    additionalInputs.add(pattern)
  }

  /** Sets the dasm output format (1-3). */
  fun outputFormat(format: Int) {
    outputFormat = format
  }

  /** Sets the verboseness level (0-4). */
  fun verboseness(level: Int) {
    verboseness = level
  }

  /** Sets the error format (0=MS, 1=Dillon, 2=GNU). */
  fun errorFormat(format: Int) {
    errorFormat = format
  }

  /** Enables strict syntax checking. */
  fun strictSyntax(strict: Boolean) {
    strictSyntax = strict
  }

  /** Enables removing output on errors. */
  fun removeOnError(remove: Boolean) {
    removeOnError = remove
  }

  /** Sets symbol table sort order (0=alphabetical, 1=address). */
  fun symbolTableSort(sort: Int) {
    symbolTableSort = sort
  }

  /** Sets the list file output path. */
  fun listFile(path: String) {
    listFile = path
  }

  /** Sets the symbol file output path. */
  fun symbolFile(path: String) {
    symbolFile = path
  }

  /** Builds the DasmStep from the configured builder. */
  fun build(): DasmStep {
    val config =
        DasmConfig(
            includePaths = includePaths,
            defines = defines.toMap(),
            outputFormat = outputFormat,
            listFile = listFile,
            symbolFile = symbolFile,
            verboseness = verboseness,
            errorFormat = errorFormat,
            strictSyntax = strictSyntax,
            removeOnError = removeOnError,
            symbolTableSort = symbolTableSort,
            srcDirs = srcDirs.ifEmpty { listOf(".") },
            includes = includes.ifEmpty { listOf("**/*.asm") },
            excludes = excludes.ifEmpty { listOf(".ra/**/*.asm") },
            workDir = workDir,
            additionalInputs = additionalInputs)

    return DasmStep(name = name, inputs = inputs, outputs = outputs, config = config)
  }
}
