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
package com.github.c64lib.rbt.compilers.tass.adapters.out.gradle

import java.nio.file.Path
import kotlin.io.path.absolutePathString

/**
 * Builder for constructing 64tass command-line arguments. Follows fluent builder pattern for easy
 * parameter composition. Options come first and the source file is appended last, as 64tass
 * expects: 64tass [options] sourcefile. The output file is always passed via -o because 64tass
 * would otherwise write to `a.out`.
 */
internal class TassCommandLineBuilder(private val source: Path) {

  private val args: MutableList<String> = mutableListOf()

  fun includeDirs(includeDirs: List<Path>): TassCommandLineBuilder {
    includeDirs.forEach { dir ->
      args.add("-I")
      args.add(dir.absolutePathString())
    }
    return this
  }

  fun defines(defines: Map<String, String>): TassCommandLineBuilder {
    defines.forEach { (key, value) ->
      args.add("-D")
      args.add(if (value.isEmpty()) key else "$key=$value")
    }
    return this
  }

  fun outputFormat(format: String): TassCommandLineBuilder {
    when (format) {
      "prg" -> args.add("--cbm-prg")
      "bin" -> args.add("--nostart")
    }
    return this
  }

  fun cpu(cpu: String?): TassCommandLineBuilder {
    if (cpu != null) {
      args.add("--m$cpu")
    }
    return this
  }

  fun caseSensitive(caseSensitive: Boolean?): TassCommandLineBuilder {
    if (caseSensitive == true) {
      args.add("--case-sensitive")
    }
    return this
  }

  fun asciiConversion(asciiConversion: Boolean?): TassCommandLineBuilder {
    if (asciiConversion == true) {
      args.add("--ascii")
    }
    return this
  }

  fun quiet(quiet: Boolean?): TassCommandLineBuilder {
    if (quiet == true) {
      args.add("--quiet")
    }
    return this
  }

  fun longBranch(longBranch: Boolean?): TassCommandLineBuilder {
    if (longBranch == true) {
      args.add("--long-branch")
    }
    return this
  }

  fun listFile(listFile: Path?): TassCommandLineBuilder {
    if (listFile != null) {
      args.add("-L")
      args.add(listFile.absolutePathString())
    }
    return this
  }

  // 64tass requires format selectors like --vice-labels to precede the -l option they apply to
  fun labelsFile(labelsFile: Path?, viceLabels: Boolean?): TassCommandLineBuilder {
    if (labelsFile != null) {
      if (viceLabels == true) {
        args.add("--vice-labels")
      }
      args.add("-l")
      args.add(labelsFile.absolutePathString())
    }
    return this
  }

  fun outputFile(outputFile: Path): TassCommandLineBuilder {
    args.add("-o")
    args.add(outputFile.absolutePathString())
    return this
  }

  fun build(): List<String> = args + source.absolutePathString()
}
