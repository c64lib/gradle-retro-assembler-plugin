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
package com.github.c64lib.rbt.compilers.tass.usecase

import com.github.c64lib.rbt.compilers.tass.usecase.port.TassAssemblePort
import java.io.File

/**
 * Use case for 64tass assembly compilation. Delegates to TassAssemblePort for actual command-line
 * execution.
 */
class TassAssembleUseCase(private val tassAssemblePort: TassAssemblePort) {
  fun apply(command: TassAssembleCommand) =
      tassAssemblePort.assemble(
          command.includeDirs,
          command.defines,
          command.source,
          command.outputFile,
          command.outputFormat,
          command.listFile,
          command.labelsFile,
          command.viceLabels,
          command.cpu,
          command.caseSensitive,
          command.asciiConversion,
          command.quiet,
          command.longBranch)
}

/** Immutable command data structure for 64tass assembly compilation. */
data class TassAssembleCommand(
    val includeDirs: List<File>,
    val defines: Map<String, String>,
    val source: File,
    val outputFile: File,
    val outputFormat: String = "prg",
    val listFile: File? = null,
    val labelsFile: File? = null,
    val viceLabels: Boolean? = null,
    val cpu: String? = null,
    val caseSensitive: Boolean? = null,
    val asciiConversion: Boolean? = null,
    val quiet: Boolean? = null,
    val longBranch: Boolean? = null
)
