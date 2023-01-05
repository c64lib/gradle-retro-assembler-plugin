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
package com.github.c64lib.rbt.shared.gradle

import com.github.c64lib.rbt.shared.domain.IllegalConfigurationException
import java.io.File
import javax.inject.Inject
import org.gradle.api.file.ProjectLayout

abstract class OutputExtension<T>
@Inject
constructor(buildDir: String, private val project: ProjectLayout) {
  var output: File? = null

  private val buildDir = project.buildDirectory.dir(buildDir).get().asFile.toPath()

  private val hasOutput: Boolean
    get() = output != null

  fun resolveOutput(buffers: MutableList<T>, outputToBuildDir: Boolean): T {
    val fos =
        createBuffer(
            normalize(output, outputToBuildDir)
                ?: throw IllegalConfigurationException("Output is not specified"))
    buffers.add(fos)
    return fos
  }

  protected abstract fun createBuffer(output: File): T

  private fun normalize(output: File?, outputToBuildDir: Boolean): File? =
      if (output != null && outputToBuildDir) {
        // TODO refactor!
        val outputRelativePath =
            project.projectDirectory.asFile.toPath().relativize(output.toPath())
        val resultPath = buildDir.resolve(outputRelativePath)
        resultPath.parent?.toFile()?.mkdirs()
        resultPath.toFile()
      } else {
        output
      }
}
