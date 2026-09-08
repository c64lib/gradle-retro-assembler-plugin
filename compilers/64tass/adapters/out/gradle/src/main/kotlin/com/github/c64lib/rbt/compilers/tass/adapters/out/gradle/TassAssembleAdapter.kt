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

import com.github.c64lib.rbt.compilers.tass.usecase.port.TassAssemblePort
import java.io.File
import org.gradle.api.Project

/**
 * Gradle adapter that implements TassAssemblePort by executing 64tass via system command. Assumes
 * 64tass is installed and available in system PATH environment variable.
 */
class TassAssembleAdapter(private val project: Project) : TassAssemblePort {
  override fun assemble(
      includeDirs: List<File>,
      defines: Map<String, String>,
      source: File,
      outputFile: File,
      outputFormat: String,
      listFile: File?,
      labelsFile: File?,
      viceLabels: Boolean?,
      cpu: String?,
      caseSensitive: Boolean?,
      asciiConversion: Boolean?,
      quiet: Boolean?,
      longBranch: Boolean?
  ) {
    val args =
        TassCommandLineBuilder(source.toPath())
            .outputFormat(outputFormat)
            .cpu(cpu)
            .caseSensitive(caseSensitive)
            .asciiConversion(asciiConversion)
            .quiet(quiet)
            .longBranch(longBranch)
            .includeDirs(includeDirs.map { it.toPath() })
            .defines(defines)
            .listFile(listFile?.toPath())
            .labelsFile(labelsFile?.toPath(), viceLabels)
            .outputFile(outputFile.toPath())
            .build()

    project.exec {
      it.executable = "64tass"
      it.args = args
      printArgs(args)
    }
  }

  private fun printArgs(args: List<String>) {
    println("64tass ${args.joinToString(" ")}")
  }
}
