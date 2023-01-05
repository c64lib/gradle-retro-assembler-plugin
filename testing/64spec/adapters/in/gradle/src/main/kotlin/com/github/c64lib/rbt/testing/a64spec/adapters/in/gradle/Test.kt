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
package com.github.c64lib.rbt.testing.a64spec.adapters.`in`.gradle

import com.github.c64lib.rbt.shared.gradle.GROUP_BUILD
import com.github.c64lib.rbt.shared.gradle.dsl.RetroAssemblerPluginExtension
import com.github.c64lib.rbt.testing.a64spec.usecase.Run64SpecTestUseCase
import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.util.PatternSet

open class Test : DefaultTask() {

  init {
    description = "Run tests written with 64spec"
    group = GROUP_BUILD
  }

  @Input lateinit var extension: RetroAssemblerPluginExtension

  @Internal lateinit var run64SpecTestUseCase: Run64SpecTestUseCase

  @TaskAction
  fun runSpec() {
    val testResults = launchAllTests()
    val isPositive = TestReport(testResults).generateTestReport { println(it) }
    if (!isPositive) {
      throw GradleException("There are errors in tests.")
    }
  }

  private fun launchAllTests() = testFiles().map(::launchTest)

  private fun launchTest(file: File) = run64SpecTestUseCase.apply(file)

  private fun testFiles() =
      extension.specDirs.flatMap {
        project.fileTree(it).matching(PatternSet().include(*extension.specIncludes))
      }
}
