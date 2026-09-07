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
package com.github.c64lib.rbt.compilers.dasm.adapters.`in`.gradle

import com.github.c64lib.rbt.compilers.dasm.usecase.DasmAssembleCommand
import com.github.c64lib.rbt.compilers.dasm.usecase.DasmAssembleUseCase
import com.github.c64lib.rbt.shared.domain.OutputFormat
import com.github.c64lib.rbt.shared.gradle.GROUP_BUILD
import com.github.c64lib.rbt.shared.gradle.dsl.RetroAssemblerPluginExtension
import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.util.PatternSet

/**
 * Legacy `asm`-path task that assembles all discovered sources with dasm when `retroProject.dialect
 * = Dasm`.
 *
 * Output files are written next to their sources (`.prg` for PRG, `.bin` for BIN), mirroring the
 * KickAssembler behaviour of the classic `asm` task.
 */
open class DasmAssemble : DefaultTask() {

  init {
    description = "Runs dasm assembler over all source files"
    group = GROUP_BUILD
  }

  @Input lateinit var extension: RetroAssemblerPluginExtension

  @Internal lateinit var dasmAssembleUseCase: DasmAssembleUseCase

  @TaskAction
  fun assemble() =
      sourceFiles().forEach { sourceFile ->
        dasmAssembleUseCase.apply(
            DasmAssembleCommand(
                libDirs = extension.libDirs.map { file -> project.file(file) },
                defines = parseDefines(extension.defines),
                source = sourceFile,
                outputFormat = toDasmOutputFormat(extension.outputFormat),
                outputFile = deriveOutputFile(sourceFile, extension.outputFormat)))
      }

  private fun sourceFiles(): FileCollection =
      extension.srcDirs
          .map { srcDir ->
            project
                .fileTree(srcDir)
                .matching(PatternSet().include(*extension.includes).exclude(*extension.excludes))
          }
          .reduce { acc, rightHand -> acc.plus(rightHand) }

  // extension defines come as "NAME" or "NAME=VALUE" strings
  private fun parseDefines(defines: Array<String>): Map<String, String> =
      defines.associate { define ->
        val separator = define.indexOf('=')
        if (separator >= 0) {
          define.substring(0, separator) to define.substring(separator + 1)
        } else {
          define to ""
        }
      }

  // dasm -f formats: 1 = binary with 2-byte origin header (.prg-like), 3 = raw
  private fun toDasmOutputFormat(format: OutputFormat): Int =
      when (format) {
        OutputFormat.PRG -> 1
        OutputFormat.BIN -> 3
      }

  private fun deriveOutputFile(source: File, format: OutputFormat): File =
      File(
          source.parentFile,
          "${source.nameWithoutExtension}.${if (format == OutputFormat.PRG) "prg" else "bin"}")
}
