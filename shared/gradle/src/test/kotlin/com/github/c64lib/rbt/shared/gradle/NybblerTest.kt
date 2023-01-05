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

          it("produces lo nybbles and suppresses hi nybbles") {
            nybbler.write(byteArrayOf(0x01, 0x0f, 0x12, 0x1e))
            lo.bytes shouldBe byteArrayOf(0x01, 0x0f, 0x02, 0x0e)
          }
        }

        describe("with hi") {
          lateinit var hi: BinaryOutputMock

          beforeEach { hi = BinaryOutputMock() }

          describe("when hi is not normalized") {
            beforeEach { nybbler = Nybbler(null, hi, false) }

            it("produces hi nybbles with no shifting and suppresses lo nybbles") {
              nybbler.write(byteArrayOf(0x01, 0x0f, 0x12, 0x73))
              hi.bytes shouldBe byteArrayOf(0x00, 0x00, 0x10, 0x70)
            }
          }

          describe("when hi is normalized") {
            beforeEach { nybbler = Nybbler(null, hi, true) }

            it("produces hi nybbles and suppresses lo nybbles") {
              nybbler.write(byteArrayOf(0x01, 0x0f, 0x12, 0x73))
              hi.bytes shouldBe byteArrayOf(0x00, 0x00, 0x01, 0x07)
            }
          }
        }

        describe("with lo and hi") {
          lateinit var lo: BinaryOutputMock
          lateinit var hi: BinaryOutputMock

          beforeEach {
            lo = BinaryOutputMock()
            hi = BinaryOutputMock()
            nybbler = Nybbler(lo, hi)
          }

          it("separates and normalizes lo and hi nybbles") {
            nybbler.write(byteArrayOf(0x01, 0x02, 0x12, 0x73))
            lo.bytes shouldBe byteArrayOf(0x01, 0x02, 0x02, 0x03)
            hi.bytes shouldBe byteArrayOf(0x00, 0x00, 0x01, 0x07)
          }
        }
      }
    })
