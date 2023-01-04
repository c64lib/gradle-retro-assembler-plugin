/*
MIT License

Copyright (c) 2018-2023 c64lib: The Ultimate Commodore 64 Library

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
package com.github.c64lib.rbt.compilers.kickass.adapters.`in`.gradle

import com.github.c64lib.rbt.compilers.kickass.usecase.KickAssembleSpecCommand
import com.github.c64lib.rbt.compilers.kickass.usecase.KickAssembleSpecUseCase
import com.github.c64lib.rbt.shared.gradle.GROUP_BUILD
import com.github.c64lib.rbt.shared.gradle.RetroAssemblerPluginExtension
import java.io.File
import kotlin.io.path.nameWithoutExtension
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.util.PatternSet

open class AssembleSpec : DefaultTask() {

  init {
    description = "Runs assembler over all spec files"
    group = GROUP_BUILD
  }

  @Input lateinit var extension: RetroAssemblerPluginExtension

  @Internal lateinit var kickAssembleSpecUseCase: KickAssembleSpecUseCase

  @TaskAction
  fun assemble() {

    testFiles().forEach { testFile ->
      kickAssembleSpecUseCase.apply(
          KickAssembleSpecCommand(
              libDirs = listOf(*extension.libDirs).map { file -> project.file(file) },
              defines = listOf(*extension.defines),
              resultFileName = toResultFileName(testFile),
              source = testFile))
    }
  }

  private fun testFiles(): FileCollection =
      extension.specDirs
          .map { specDir ->
            project.fileTree(specDir).matching(PatternSet().include(*extension.specIncludes))
          }
          .reduce { acc, rightHand -> acc.plus(rightHand) }

  private fun toResultFileName(file: File) = file.toPath().nameWithoutExtension + ".specOut"
}
