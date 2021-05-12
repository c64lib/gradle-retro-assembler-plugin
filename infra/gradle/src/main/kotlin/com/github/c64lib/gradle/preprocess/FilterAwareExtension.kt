/*
MIT License

Copyright (c) 2018 c64lib: The Ultimate Commodore 64 Library

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
package com.github.c64lib.gradle.preprocess

import com.github.c64lib.retroassembler.binary_interleaver.BinaryInterleaver
import com.github.c64lib.retroassembler.domain.processor.BinaryOutput
import com.github.c64lib.retroassembler.domain.shared.IllegalConfigurationException
import com.github.c64lib.retroassembler.nybbler.Nybbler
import io.vavr.collection.List
import java.io.File
import java.util.*
import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.Nested

// TODO this must be moved to the charpad, refactor!
private const val CHARPAD_DIR = "charpad"

abstract class FilterAwareExtension @Inject constructor(private val project: ProjectLayout) {
  var output: File? = null

  private val charpadBuildDir = project.buildDirectory.dir(CHARPAD_DIR).get().asFile.toPath()

  private val interleavers: MutableList<InterleaverExtension> = LinkedList()

  @Nested abstract fun getNybbler(): NybblerExtension

  fun nybbler(action: Action<NybblerExtension>) {
    action.execute(getNybbler())
  }

  fun interleaver(action: Action<InterleaverExtension>) {
    val ex = InterleaverExtension()
    action.execute(ex)
    interleavers.add(ex)
  }

  private val hasOutput: Boolean
    get() = output != null

  private val hasNybbler: Boolean
    get() = getNybbler().hiOutput != null || getNybbler().loOutput != null

  private val hasInterleavers: Boolean
    get() = interleavers.isNotEmpty()

  internal fun resolveOutput(
      buffers: MutableList<OutputBuffer>, outputToBuildDir: Boolean
  ): BinaryOutput =
      when {
        hasOutput -> {
          val fos =
              FileOutputBuffer(
                  normalize(output, outputToBuildDir)
                      ?: throw IllegalConfigurationException("Output is not specified"))
          buffers.add(fos)
          fos
        }
        hasNybbler -> {
          val lo = normalize(getNybbler().loOutput, outputToBuildDir)?.let { FileOutputBuffer(it) }
          val hi = normalize(getNybbler().hiOutput, outputToBuildDir)?.let { FileOutputBuffer(it) }
          lo?.let { buffers.add(lo) }
          hi?.let { buffers.add(hi) }
          Nybbler(lo, hi, getNybbler().normalizeHi)
        }
        hasInterleavers -> {
          val outputBuffers =
              interleavers.map {
                val interleaverOutput = normalize(it.output, outputToBuildDir)
                if (interleaverOutput != null) {
                  FileOutputBuffer(interleaverOutput)
                } else {
                  DevNull()
                }
              }
          outputBuffers.forEach { buffers.add(it) }
          BinaryInterleaver(List.ofAll(outputBuffers))
        }
        else ->
            throw IllegalConfigurationException(
                "Either output or at least one output filters must be configured.")
      }

  private fun normalize(output: File?, outputToBuildDir: Boolean): File? =
      if (output != null && outputToBuildDir) {
        // TODO refactor!
        val outputRelativePath =
            project.projectDirectory.asFile.toPath().relativize(output.toPath())
        val resultPath = charpadBuildDir.resolve(outputRelativePath)
        resultPath.parent?.toFile()?.mkdirs()
        resultPath.toFile()
      } else {
        output
      }
}
