/*
MIT License

Copyright (c) 2018-2021 c64lib: The Ultimate Commodore 64 Library

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
package com.github.c64lib.gradle.asms

import com.github.c64lib.gradle.RetroAssemblerPluginExtension
import com.github.c64lib.gradle.asms.kickassembler.KickAssemblerFacade
import com.github.c64lib.retroassembler.domain.AssemblerType
import java.io.File
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.process.ExecResult

interface AssemblerFacade {
  fun installDevKit()
  fun targetFiles(): FileCollection
  fun sourceFiles(): FileCollection
  fun testFiles(): FileCollection
  fun assemble(sourceFile: File, vararg parameters: String): ExecResult
}

object AssemblerFacadeFactory {
  fun of(
      assembler: AssemblerType, project: Project, extension: RetroAssemblerPluginExtension
  ): AssemblerFacade =
      when (assembler) {
        AssemblerType.KickAssembler -> KickAssemblerFacade(project, extension)
        AssemblerType.None -> throw GradleException("Assembler dialect is not selected")
      }
}
