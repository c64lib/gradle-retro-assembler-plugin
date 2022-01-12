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
package com.github.c64lib.retroassembler.charpad_processor.producer

import com.github.c64lib.retroassembler.binutils.BinaryOutputMock
import com.github.c64lib.retroassembler.binutils.byteArrayOfInts
import com.github.c64lib.retroassembler.binutils.concat
import com.github.c64lib.retroassembler.charpad_processor.model.MapCoord
import com.github.c64lib.retroassembler.charpad_processor.model.maxRightBottomMapCoord
import com.github.c64lib.retroassembler.charpad_processor.model.minTopLeftMapCoord
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class MapProducerTest :
    BehaviorSpec({
      isolationMode = IsolationMode.InstancePerTest
      Given("4x4 map") {
        val mapData =
            byteArrayOfInts(0x01, 0x02, 0x11, 0x12, 0x21, 0x22, 0x23, 0x24) concat
                byteArrayOfInts(0x31, 0x32, 0x41, 0x42, 0x51, 0x52, 0x53, 0x54) concat
                byteArrayOfInts(0x61, 0x61, 0x71, 0x72, 0x81, 0x82, 0x83, 0x84) concat
                byteArrayOfInts(0x91, 0x91, 0xA1, 0xA2, 0xB1, 0xB2, 0xB3, 0xB4)
        val width = 4
        val height = 4
        val outputMock = BinaryOutputMock()
        And("MapProducer unbounded") {
          val producer =
              MapProducer(
                  leftTop = minTopLeftMapCoord,
                  rightBottom = maxRightBottomMapCoord,
                  output = outputMock)
          When("write is called") {
            producer.write(width, height, mapData)
            Then("whole map is written") { outputMock.bytes shouldBe mapData }
          }
        }
        And("MapProducer bounded from leftTop [2,2]") {
          val producer =
              MapProducer(
                  leftTop = MapCoord(2, 2),
                  rightBottom = maxRightBottomMapCoord,
                  output = outputMock)
          When("write is called") {
            producer.write(width, height, mapData)
            Then("right bottom part of map is written") {
              outputMock.bytes shouldBe
                  (byteArrayOfInts(0x81, 0x82, 0x83, 0x84) concat
                      byteArrayOfInts(0xB1, 0xB2, 0xB3, 0xB4))
            }
          }
        }
        And("MapProducer bounded from bottomRight [2,2]") {
          val producer =
              MapProducer(
                  leftTop = minTopLeftMapCoord, rightBottom = MapCoord(2, 2), output = outputMock)
          When("write is called") {
            producer.write(width, height, mapData)
            Then("top left part of map is written") {
              outputMock.bytes shouldBe
                  (byteArrayOfInts(0x01, 0x02, 0x11, 0x12) concat
                      byteArrayOfInts(0x31, 0x32, 0x41, 0x42))
            }
          }
        }
        And("MapProducer bounded from [1,1] to [3,3]") {
          val producer =
              MapProducer(
                  leftTop = MapCoord(1, 1), rightBottom = MapCoord(3, 3), output = outputMock)
          When("write is called") {
            producer.write(width, height, mapData)
            Then("middle part of map is written") {
              outputMock.bytes shouldBe
                  (byteArrayOfInts(0x41, 0x42, 0x51, 0x52) concat
                      byteArrayOfInts(0x71, 0x72, 0x81, 0x82))
            }
          }
        }
      }
    })
