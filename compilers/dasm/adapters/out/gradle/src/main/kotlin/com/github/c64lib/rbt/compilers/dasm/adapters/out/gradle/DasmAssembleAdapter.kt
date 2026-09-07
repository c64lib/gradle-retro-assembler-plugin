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

import com.github.c64lib.rbt.compilers.dasm.domain.DasmAssemblerSettings
import com.github.c64lib.rbt.compilers.dasm.usecase.port.DasmAssemblePort
import java.io.File
import org.gradle.api.Project

/**
 * Gradle adapter that implements DasmAssemblePort by executing dasm via system command. Uses the
 * executable resolved in [DasmAssemblerSettings], falling back to `dasm` on the PATH.
 */
class DasmAssembleAdapter(
    private val project: Project,
    private val settings: DasmAssemblerSettings = DasmAssemblerSettings()
) : DasmAssemblePort {
  override fun assemble(
      libDirs: List<File>,
      defines: Map<String, String>,
      source: File,
      outputFormat: Int,
      outputFile: File?,
      listFile: File?,
      symbolFile: File?,
      verboseness: Int?,
      errorFormat: Int?,
      strictSyntax: Boolean?,
      removeOnError: Boolean?,
      symbolTableSort: Int?
  ) {
    val args =
        DasmCommandLineBuilder(source.toPath())
            .libDirs(libDirs.map { it.toPath() })
            .defines(defines)
            .outputFormat(outputFormat)
            .outputFile(outputFile?.toPath())
            .listFile(listFile?.toPath())
            .symbolFile(symbolFile?.toPath())
            .verboseness(verboseness)
            .errorFormat(errorFormat)
            .strictSyntax(strictSyntax)
            .removeOnError(removeOnError)
            .symbolTableSort(symbolTableSort)
            .build()

    val executable = settings.pathToExecutable?.absolutePath ?: "dasm"
    project.exec {
      it.executable = executable
      it.args = args
      printArgs(executable, args)
    }
  }

  private fun printArgs(executable: String, args: List<String>) {
    println("$executable ${args.joinToString(" ")}")
  }
}
