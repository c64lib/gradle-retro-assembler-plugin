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
package com.github.c64lib.gradle.spec

import com.github.c64lib.gradle.GROUP_BUILD
import com.github.c64lib.gradle.RetroAssemblerPluginExtension
import com.github.c64lib.gradle.asms.AssemblerFacadeFactory
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class AssembleSpec : DefaultTask() {

  init {
    description = "Runs assembler over all spec files"
    group = GROUP_BUILD
  }

  @Input lateinit var extension: RetroAssemblerPluginExtension

  @TaskAction
  fun assemble() {
    val asm = AssemblerFacadeFactory.of(extension.dialect, project, extension)
    asm.testFiles().forEach { file ->
      asm.assemble(
          file,
          "-o",
          prgFile(file),
          "-vicesymbols",
          ":on_exit=jam",
          ":write_final_results_to_file=true",
          ":change_character_set=true",
          ":result_file_name=" + resultFileName(file))
    }
  }
}
