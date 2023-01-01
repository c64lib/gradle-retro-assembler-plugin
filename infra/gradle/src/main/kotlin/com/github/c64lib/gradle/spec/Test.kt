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

import com.github.c64lib.rbt.domain.AssemblerType
import com.github.c64lib.rbt.emulators.vice.adapters.out.gradle.RunTestOnViceAdapter
import com.github.c64lib.rbt.emulators.vice.domain.AutostartPrgMode
import com.github.c64lib.rbt.emulators.vice.usecase.RunTestOnViceCommand
import com.github.c64lib.rbt.emulators.vice.usecase.RunTestOnViceUseCase
import com.github.c64lib.rbt.shared.gradle.GROUP_BUILD
import com.github.c64lib.rbt.shared.gradle.RetroAssemblerPluginExtension
import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.util.PatternSet

open class Test : DefaultTask() {

  init {
    description = "Run tests written with 64spec"
    group = GROUP_BUILD
  }

  @Input lateinit var extension: RetroAssemblerPluginExtension

  @TaskAction
  fun runSpec() {
    assert(extension.dialect == AssemblerType.KickAssembler) {
      "The specified dialect ${extension.dialect} cannot be used with 64spec, only ${AssemblerType.KickAssembler} is supported"
    }
    launchAllTests()
    val isPositive = TestReport(testFiles()).generateTestReport { println(it) }
    if (!isPositive) {
      throw GradleException("There are errors in tests.")
    }
  }

  private fun launchAllTests() = testFiles().forEach(::launchTest)

  private fun launchTest(file: File) =
      RunTestOnViceUseCase(RunTestOnViceAdapter(project))
          .apply(
              RunTestOnViceCommand(
                  executable = extension.viceExecutable,
                  autostart = File(prgFile(file.absoluteFile)),
                  monCommands = File(viceSymbolFile(file)),
                  autostartPrgMode = AutostartPrgMode.valueOf(extension.viceAutostartPrgMode.name),
                  verbose = extension.verbose,
                  headless = extension.viceHeadless))

  private fun testFiles() =
      extension.specDirs.flatMap {
        project.fileTree(it).matching(PatternSet().include(*extension.specIncludes))
      }
}
