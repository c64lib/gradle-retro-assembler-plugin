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
package com.github.c64lib.rbt.processors.charpad.usecase

import com.github.c64lib.rbt.processors.charpad.domain.CharsetProducer
import com.github.c64lib.rbt.processors.charpad.domain.InvalidCTMFormatException
import com.github.c64lib.rbt.processors.charpad.domain.TileProducer
import com.github.c64lib.rbt.shared.binutils.byteArrayOfInts
import com.github.c64lib.rbt.shared.binutils.concat
import com.github.c64lib.rbt.shared.testutils.BinaryInputMock
import com.github.c64lib.rbt.shared.testutils.BinaryOutputMock
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class CharpadProcessorTest :
    BehaviorSpec({
      isolationMode = IsolationMode.InstancePerTest

      Given("CharpadProcessor with empty processors") {
        val processor = ProcessCharpadUseCase(emptyList(), true)
        When("process is called") {
          And("the input stream is empty") {
            val exception =
                shouldThrow<InvalidCTMFormatException> {
                  processor.apply(BinaryInputMock(ByteArray(0)))
                }
            Then("exception is thrown") { exception.message shouldBe "CTM id is missing" }
          }
          listOf(1, 2, 3, 4).forEach { version ->
            And("and input stream contains unsupported CMT version $version") {
              val exception =
                  shouldThrow<InvalidCTMFormatException> {
                    processor.apply(
                        BinaryInputMock("CTM".toByteArray() concat byteArrayOf(version.toByte())))
                  }
              Then("exception is thrown") {
                exception.message shouldBe "Unsupported version: $version"
              }
            }
          }
        }
      }

      Given("CharpadProcessor with charset processor") {
        val charsetOutput = BinaryOutputMock()
        And("whole charset producer is used") {
          val producer = CharsetProducer(output = charsetOutput)
          val processor = ProcessCharpadUseCase(listOf(producer), true)
          When("process is called") {
            processor.apply(
                BinaryInputMock(
                    CTMByteArrayMock(
                            version = 5,
                            charset =
                                byteArrayOfInts(0x00, 0x01, 0x02, 0x03, 0x10, 0x11, 0x12, 0x13),
                            tiles = ByteArray(16))
                        .bytes))
            Then("charset content is properly produced") {
              charsetOutput.bytes shouldBe
                  byteArrayOfInts(0x00, 0x01, 0x02, 0x03, 0x10, 0x11, 0x12, 0x13)
            }
          }
        }
        And("a subset of charset is used") {
          val producer = CharsetProducer(start = 1, end = 2, output = charsetOutput)
          val processor = ProcessCharpadUseCase(listOf(producer), true)
          When("process is called") {
            val char0 = byteArrayOfInts(0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07)
            val char1 = byteArrayOfInts(0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17)
            val char2 = byteArrayOfInts(0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27)
            processor.apply(
                BinaryInputMock(
                    CTMByteArrayMock(
                            version = 5,
                            charset = char0 concat char1 concat char2,
                            tiles = ByteArray(16))
                        .bytes))
            Then("subset of charset content is properly produced") {
              charsetOutput.bytes shouldBe char1
            }
          }
        }
      }

      Given("CharpadProcessor with tiles processor") {
        val tilesOutput = BinaryOutputMock()
        And("the whole tileset is used") {
          val producer = TileProducer(output = tilesOutput)
          val processor = ProcessCharpadUseCase(listOf(producer), true)
          When("process is called") {
            val tileData =
                byteArrayOfInts(0x00, 0x01, 0x10, 0x11, 0x20, 0x21, 0x30, 0x31) concat
                    byteArrayOfInts(0x00, 0x01, 0x10, 0x11, 0x20, 0x21, 0x30, 0x31)
            processor.apply(
                BinaryInputMock(
                    CTMByteArrayMock(
                            version = 5,
                            charset =
                                byteArrayOfInts(0x00, 0x01, 0x02, 0x03, 0x10, 0x11, 0x12, 0x13),
                            charAttributes = byteArrayOfInts(0x00),
                            tiles = tileData)
                        .bytes))
            Then("tiles content is properly produced") { tilesOutput.bytes shouldBe tileData }
          }
        }
      }
    })
