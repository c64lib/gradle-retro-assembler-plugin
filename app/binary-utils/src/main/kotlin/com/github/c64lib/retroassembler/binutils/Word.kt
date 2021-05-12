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
package com.github.c64lib.retroassembler.binutils

fun wordOf(lo: Int, hi: Int): Word {
  if (lo < 0 || lo > 255) {
    throw IllegalArgumentException("lo value beyond byte boundaries: $lo")
  }
  if (hi < 0 || hi > 255) {
    throw IllegalArgumentException("hi value beyond byte boundaries: $hi")
  }
  return Word(lo.toUnsignedByte(), hi.toUnsignedByte())
}

fun wordOf(value: Int): Word {
  if (value < 0 || value > 65535) {
    throw IllegalArgumentException("value beyond word boundaries: $value")
  }
  return Word(value.toUnsignedByte(), value.toUnsignedByteHi())
}

fun ByteArray.toWord() = wordOf(this[0].toUnsignedInt(), this[1].toUnsignedInt())

data class Word(val lo: Byte, val hi: Byte) {

  val value = hi.toUnsignedInt() * 256 + lo.toUnsignedInt()

  fun toByteArray() = byteArrayOf(lo, hi)

  override fun toString(): String {
    return value.toString()
  }
}
