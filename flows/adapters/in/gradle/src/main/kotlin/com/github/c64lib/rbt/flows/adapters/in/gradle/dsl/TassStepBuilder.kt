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

import com.github.c64lib.rbt.flows.domain.config.TassConfig
import com.github.c64lib.rbt.flows.domain.steps.TassStep

/** Type-safe DSL builder for 64tass assembly processing steps. */
class TassStepBuilder(private val name: String) {
  private val inputs = mutableListOf<String>()
  private val outputs = mutableListOf<String>()

  var outputFormat: String = "prg"
  var cpu: String? = null
  var caseSensitive: Boolean? = null
  var asciiConversion: Boolean? = null
  var quiet: Boolean? = null
  var longBranch: Boolean? = null
  var viceLabels: Boolean? = null
  var workDir: String = ".ra"

  private val includePaths = mutableListOf<String>()
  private val defines = mutableMapOf<String, String>()
  private val srcDirs = mutableListOf<String>()
  private val includes = mutableListOf<String>()
  private val excludes = mutableListOf<String>()
  private val additionalInputs = mutableListOf<String>()
  private var listFile: String? = null
  private var labelsFile: String? = null

  /** Specifies input sources for this 64tass step. */
  fun from(path: String) {
    inputs.add(path)
  }

  /** Specifies multiple input sources for this 64tass step. */
  fun from(vararg paths: String) {
    inputs.addAll(paths)
  }

  /** Specifies output destination for this 64tass step. */
  fun to(path: String) {
    outputs.add(path)
  }

  /** Specifies multiple output destinations for this 64tass step. */
  fun to(vararg paths: String) {
    outputs.addAll(paths)
  }

  /** Adds include search paths for 64tass. */
  fun includePaths(vararg paths: String) {
    includePaths.addAll(paths)
  }

  /** Adds a single include search path for 64tass. */
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

  /** Sets the output format ("prg" = CBM with load address, "bin" = raw binary). */
  fun outputFormat(format: String) {
    outputFormat = format
  }

  /** Sets the target CPU (e.g. "6502", "65c02", "65816", "65dtv02"). */
  fun cpu(cpu: String) {
    this.cpu = cpu
  }

  /** Enables case-sensitive symbol handling. */
  fun caseSensitive(sensitive: Boolean) {
    caseSensitive = sensitive
  }

  /** Enables ASCII to PETSCII conversion of string literals. */
  fun asciiConversion(convert: Boolean) {
    asciiConversion = convert
  }

  /** Suppresses non-error output of 64tass. */
  fun quiet(quiet: Boolean) {
    this.quiet = quiet
  }

  /** Enables automatic long branch generation. */
  fun longBranch(enabled: Boolean) {
    longBranch = enabled
  }

  /** Emits the labels file in VICE label format. */
  fun viceLabels(enabled: Boolean) {
    viceLabels = enabled
  }

  /** Sets the assembly listing file output path. */
  fun listFile(path: String) {
    listFile = path
  }

  /** Sets the labels file output path. */
  fun labelsFile(path: String) {
    labelsFile = path
  }

  /** Builds the TassStep from the configured builder. */
  fun build(): TassStep {
    val config =
        TassConfig(
            includePaths = includePaths,
            defines = defines.toMap(),
            outputFormat = outputFormat,
            listFile = listFile,
            labelsFile = labelsFile,
            viceLabels = viceLabels,
            cpu = cpu,
            caseSensitive = caseSensitive,
            asciiConversion = asciiConversion,
            quiet = quiet,
            longBranch = longBranch,
            srcDirs = srcDirs.ifEmpty { listOf(".") },
            includes = includes.ifEmpty { listOf("**/*.asm") },
            excludes = excludes.ifEmpty { listOf(".ra/**/*.asm") },
            workDir = workDir,
            additionalInputs = additionalInputs)

    return TassStep(name = name, inputs = inputs, outputs = outputs, config = config)
  }
}
