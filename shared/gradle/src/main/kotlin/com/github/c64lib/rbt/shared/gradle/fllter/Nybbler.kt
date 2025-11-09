/*
MIT License

Copyright (c) 2018-2025 c64lib: The Ultimate Commodore 64 Library
Copyright (c) 2018-2025 Maciej Małecki

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
package com.github.c64lib.rbt.shared.gradle.fllter

import com.github.c64lib.rbt.shared.processor.BinaryOutput

class Nybbler(
    private val low: BinaryOutput?,
    private val hi: BinaryOutput?,
    private val normalizeHi: Boolean = true
) : BinaryOutput {
  override fun write(data: ByteArray) {
    low?.let { it.write(data.map { value -> lowNibble(value) }.toByteArray()) }
    hi?.let { it.write(data.map { value -> highNibble(value) }.toByteArray()) }
  }

  private fun lowNibble(value: Byte): Byte = (value.toInt() and 0x0F).toByte()

  private fun highNibble(value: Byte): Byte = optionallyNormalize(value.toInt() and 0xF0).toByte()

  private fun optionallyNormalize(value: Int): Int =
      if (normalizeHi) {
        value shr 4
      } else {
        value
      }
}
