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

import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

class BinUtilsTest {

  @Test
  fun `toUnsignedInt(0) = 0`() {
    val value: Byte = 0x00
    assertEquals(0, value.toUnsignedInt())
  }

  @Test
  fun `toUnsignedInt(1) = 1`() {
    val value: Byte = 0x01
    assertEquals(1, value.toUnsignedInt())
  }

  @Test
  fun `toUnsignedInt(127) = 127`() {
    val value: Byte = 127
    assertEquals(127, value.toUnsignedInt())
  }

  @Test
  fun `toUnsignedInt(-128) = 128`() {
    val value: Byte = -128
    assertEquals(128, value.toUnsignedInt())
  }

  @Test
  fun `toUnsignedInt(-1) = 255`() {
    val value: Byte = -1
    assertEquals(255, value.toUnsignedInt())
  }

  @Test
  fun `(0,0)toWord() = 0`() {
    assertEquals(0, byteArrayOf(0.toByte(), 0.toByte()).toWord())
  }

  @Test
  fun `(1,0)toWord() = 1`() {
    assertEquals(1, byteArrayOf(1.toByte(), 0.toByte()).toWord())
  }

  @Test
  fun `(0,1)toWord() = 256`() {
    assertEquals(256, byteArrayOf(0.toByte(), 1.toByte()).toWord())
  }

  @Test
  fun `(255,0)toWord() = 255`() {
    assertEquals(255, byteArrayOf((-1).toByte(), 0.toByte()).toWord())
  }

  @Test
  fun `(0,255)toWord() = 65280`() {
    assertEquals(65280, byteArrayOf(0.toByte(), (-1).toByte()).toWord())
  }
}
