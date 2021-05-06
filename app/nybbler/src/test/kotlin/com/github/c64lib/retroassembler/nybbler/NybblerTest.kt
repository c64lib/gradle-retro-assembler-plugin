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
package com.github.c64lib.retroassembler.nybbler

import com.github.c64lib.retroassembler.binutils.BinaryOutputMock
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class NybblerTest :
    DescribeSpec({
      describe("Nybbler") {
        lateinit var nybbler: Nybbler

        describe("with null lo and hi") {
          beforeEach { nybbler = Nybbler(null, null) }

          it("has no effect") { nybbler.write(byteArrayOf(0x01, 0x11, 0x0f, 0x21)) }
        }

        describe("with lo") {
          lateinit var lo: BinaryOutputMock

          beforeEach {
            lo = BinaryOutputMock()
            nybbler = Nybbler(lo, null)
          }

          it("produces lo bytes and suppresses hi bytes") {
            nybbler.write(byteArrayOf(0x01, 0x0f, 0x12, 0x1e))
            lo.bytes shouldBe byteArrayOf(0x01, 0x0f, 0x02, 0x0e)
          }
        }
      }
    })
