/*
MIT License

Copyright (c) 2018-2022 c64lib: The Ultimate Commodore 64 Library

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
package com.github.c64lib.retroassembler.binary_interleaver

import com.github.c64lib.retroassembler.binutils.BinaryOutputMock
import com.github.c64lib.retroassembler.domain.shared.IllegalInputException
import io.vavr.collection.List
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

class BinaryInterleaverTest {

  @Test
  fun `interleaver with single output just passes thru`() {
    // given
    val output = BinaryOutputMock()
    val interleaver = BinaryInterleaver(List.of(output))
    val input = byteArrayOf(0x01, 0x02, 0x03, 0x04)
    // when
    interleaver.write(input)
    // then
    assertTrue(input.contentEquals(output.bytes))
  }

  @Test
  fun `interleaver with two outputs splits bytes evenly`() {
    // given
    val output0 = BinaryOutputMock()
    val output1 = BinaryOutputMock()
    val interleaver = BinaryInterleaver(List.of(output0, output1))
    val input = byteArrayOf(0x01, 0x02, 0x03, 0x04)
    // when
    interleaver.write(input)
    // then
    assertTrue(byteArrayOf(0x01, 0x03).contentEquals(output0.bytes))
    assertTrue(byteArrayOf(0x02, 0x04).contentEquals(output1.bytes))
  }

  @Test
  fun `interleaver throws exception if bytes cannot be split evenly`() {
    // given
    val output0 = BinaryOutputMock()
    val output1 = BinaryOutputMock()
    val interleaver = BinaryInterleaver(List.of(output0, output1))
    val input = byteArrayOf(0x01, 0x02, 0x03)
    // when
    try {
      interleaver.write(input)
      fail<Unit>("Should fail")
    } catch (ex: IllegalInputException) {}
  }
}
