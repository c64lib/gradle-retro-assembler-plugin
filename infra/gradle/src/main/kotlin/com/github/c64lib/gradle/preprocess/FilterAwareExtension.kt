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
import org.gradle.api.Action
import org.gradle.api.tasks.Nested

abstract class FilterAwareExtension {
  var output: File? = null

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

  internal fun resolveOutput(buffers: MutableList<OutputBuffer>): BinaryOutput =
      when {
        hasOutput -> {
          val fos =
              OutputBuffer(output ?: throw IllegalConfigurationException("Output is not specified"))
          buffers.add(fos)
          fos
        }
        hasNybbler -> {
          val lo = getNybbler().loOutput?.let { OutputBuffer(it) }
          val hi = getNybbler().hiOutput?.let { OutputBuffer(it) }
          lo?.let { buffers.add(lo) }
          hi?.let { buffers.add(hi) }
          Nybbler(lo, hi, getNybbler().normalizeHi)
        }
        hasInterleavers -> {
          val outputBuffers =
              interleavers.filter { it.output != null }
                  .map {
                    OutputBuffer(
                        it.output ?: throw IllegalConfigurationException("Output is not specified"))
                  }
          outputBuffers.forEach { buffers.add(it) }
          BinaryInterleaver(List.ofAll(outputBuffers))
        }
        else ->
            throw IllegalConfigurationException(
                "Either output or at least one output filters must be configured.")
      }
}