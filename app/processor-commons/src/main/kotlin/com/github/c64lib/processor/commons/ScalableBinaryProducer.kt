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
package com.github.c64lib.processor.commons

import com.github.c64lib.rbt.domain.shared.OutOfDataException

/**
 * Produces binary output from binary input.
 *
 * Is capable of working with equal portions of bytes and allows to specify start and end of the
 * window as well as portion size (scale).
 */
open class ScalableBinaryProducer(
    private val start: Int,
    end: Int,
    private val scale: Int = 1,
    private val output: Output<ByteArray>
) : OutputProducer<ByteArray> {

  private val scaledStart = scale(start)
  private val scaledEnd = scale(end)

  override fun write(data: ByteArray) =
      output.write(
          when {
            scaledStart >= data.size ->
                throw OutOfDataException(
                    "Not enough data to support start=$start; data size is ${data.size/scale}.")
            scaledEnd < data.size -> data.copyOfRange(scaledStart, scaledEnd)
            else -> data.copyOfRange(scaledStart, data.size)
          })

  private fun scale(value: Int): Int = value * scale
}
