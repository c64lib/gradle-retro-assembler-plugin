/*
MIT License

Copyright (c) 2018-2023 c64lib: The Ultimate Commodore 64 Library
Copyright (c) 2018-2023 Maciej Małecki

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

import com.github.c64lib.rbt.compilers.kickass.usecase.KickAssembleCommand
import com.github.c64lib.rbt.compilers.kickass.usecase.KickAssembleUseCase
import com.github.c64lib.rbt.shared.gradle.GROUP_BUILD
import com.github.c64lib.rbt.shared.gradle.dsl.RetroAssemblerPluginExtension
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.util.PatternSet

open class Assemble : DefaultTask() {

  init {
    description = "Runs assembler over all source files"
    group = GROUP_BUILD
  }

  @Input lateinit var extension: RetroAssemblerPluginExtension

  @Internal lateinit var kickAssembleUseCase: KickAssembleUseCase

  @TaskAction
  fun assemble() =
      sourceFiles().forEach { sourceFile ->
        kickAssembleUseCase.apply(
            KickAssembleCommand(
                libDirs = listOf(*extension.libDirs).map { file -> project.file(file) },
                defines = listOf(*extension.defines),
                values = extension.values,
                source = sourceFile))
      }

  private fun sourceFiles(): FileCollection =
      extension.srcDirs
          .map { srcDir ->
            project
                .fileTree(srcDir)
                .matching(PatternSet().include(*extension.includes).exclude(*extension.excludes))
          }
          .reduce { acc, rightHand -> acc.plus(rightHand) }
}
