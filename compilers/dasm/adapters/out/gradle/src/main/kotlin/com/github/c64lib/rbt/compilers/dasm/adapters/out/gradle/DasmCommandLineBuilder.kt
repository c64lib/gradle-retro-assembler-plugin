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
package com.github.c64lib.rbt.compilers.dasm.adapters.out.gradle

import java.nio.file.Path
import kotlin.io.path.absolutePathString

/**
 * Builder for constructing dasm command-line arguments. Follows fluent builder pattern for easy
 * parameter composition. Source file must be added first as dasm expects: dasm sourcefile [options]
 */
internal class DasmCommandLineBuilder(source: Path) {

  private val args: MutableList<String> = mutableListOf(source.absolutePathString())

  fun libDirs(libDirs: List<Path>): DasmCommandLineBuilder {
    args.addAll(libDirs.map { libDir -> "-I${libDir.absolutePathString()}" })
    return this
  }

  fun defines(defines: Map<String, String>): DasmCommandLineBuilder {
    defines.forEach { (key, value) -> args.add("-D$key=$value") }
    return this
  }

  fun outputFormat(format: Int): DasmCommandLineBuilder {
    if (format in 1..3) {
      args.add("-f$format")
    }
    return this
  }

  fun outputFile(outputFile: Path?): DasmCommandLineBuilder {
    if (outputFile != null) {
      args.add("-o${outputFile.absolutePathString()}")
    }
    return this
  }

  fun listFile(listFile: Path?): DasmCommandLineBuilder {
    if (listFile != null) {
      args.add("-l${listFile.absolutePathString()}")
    }
    return this
  }

  fun symbolFile(symbolFile: Path?): DasmCommandLineBuilder {
    if (symbolFile != null) {
      args.add("-s${symbolFile.absolutePathString()}")
    }
    return this
  }

  fun verboseness(level: Int?): DasmCommandLineBuilder {
    if (level != null && level in 0..4) {
      args.add("-v$level")
    }
    return this
  }

  fun errorFormat(format: Int?): DasmCommandLineBuilder {
    if (format != null && format in 0..2) {
      args.add("-E$format")
    }
    return this
  }

  fun strictSyntax(strict: Boolean?): DasmCommandLineBuilder {
    if (strict == true) {
      args.add("-S")
    }
    return this
  }

  fun removeOnError(remove: Boolean?): DasmCommandLineBuilder {
    if (remove == true) {
      args.add("-R")
    }
    return this
  }

  fun symbolTableSort(sort: Int?): DasmCommandLineBuilder {
    if (sort != null && sort in 0..1) {
      args.add("-T$sort")
    }
    return this
  }

  fun build(): List<String> = args.toList()
}
