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
package com.github.c64lib.rbt.compilers.kickass.adapters.out.gradle

import com.github.c64lib.rbt.compilers.kickass.domain.KickAssemblerSettings
import com.github.c64lib.rbt.shared.domain.OutputFormat
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.name

internal class CommandLineBuilder(private val settings: KickAssemblerSettings) {

  private val args: MutableList<String> = mutableListOf()

  fun libDirs(libDirs: List<Path>): CommandLineBuilder {
    args.addAll(libDirs.flatMap { libDir -> listOf("-libdir", libDir.absolutePathString()) })
    return this
  }

  fun defines(defines: List<String>): CommandLineBuilder {
    args.addAll(defines.flatMap { define -> listOf("-define", define) })
    return this
  }

  fun variable(name: String, value: String): CommandLineBuilder {
    args.add(":$name=$value")
    return this
  }

  fun outputFormat(outputFormat: OutputFormat): CommandLineBuilder {
    if (outputFormat == OutputFormat.BIN) {
      args.add("-binfile")
    }
    return this
  }

  fun variables(values: Map<String, String>): CommandLineBuilder {
    values.entries.forEach { variable(it.key, it.value) }
    return this
  }

  fun source(source: Path): CommandLineBuilder {
    args.add(source.absolutePathString())
    return this
  }

  fun output(output: Path): CommandLineBuilder {
    args.addAll(listOf("-o", output.absolutePathString()))
    return this
  }

  fun outputFile(outputFile: Path?): CommandLineBuilder {
    if (outputFile != null) {
      args.addAll(listOf("-o", outputFile.absolutePathString()))
    }
    return this
  }

  fun outputDirectory(outputDirectory: Path?): CommandLineBuilder {
    if (outputDirectory != null) {
      args.addAll(listOf("-odir", outputDirectory.absolutePathString()))
    }
    return this
  }

  fun viceSymbols(): CommandLineBuilder {
    args.add("-vicesymbols")
    return this
  }

  fun build(): List<String> = args.toList()
}
