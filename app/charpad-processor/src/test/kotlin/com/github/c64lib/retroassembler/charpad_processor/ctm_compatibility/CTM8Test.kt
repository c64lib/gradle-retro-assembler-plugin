/*
MIT License

Copyright (c) 2018-2021 c64lib: The Ultimate Commodore 64 Library

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
package com.github.c64lib.retroassembler.charpad_processor.ctm_compatibility

import com.github.c64lib.retroassembler.binutils.BinaryOutputMock
import com.github.c64lib.retroassembler.binutils.InputByteStreamAdapter
import com.github.c64lib.retroassembler.charpad_processor.CharpadProcessor
import com.github.c64lib.retroassembler.charpad_processor.producer.CharAttributesProducer
import com.github.c64lib.retroassembler.charpad_processor.producer.CharColoursProducer
import com.github.c64lib.retroassembler.charpad_processor.producer.CharsetProducer
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class CTM8Test :
    BehaviorSpec({
      isolationMode = IsolationMode.InstancePerTest
      Given("Text Hires with per Character colouring and no tile set") {
        val charsetOutput = BinaryOutputMock()
        val charsetColourOutput = BinaryOutputMock()
        val charsetAttributesOutput = BinaryOutputMock()
        val processor =
            CharpadProcessor(
                listOf(
                    CharsetProducer(output = charsetOutput),
                    CharColoursProducer(output = charsetColourOutput),
                    CharAttributesProducer(output = charsetAttributesOutput)))
        When("process is called") {
          processor.process(
              InputByteStreamAdapter(
                  this.javaClass.getResourceAsStream("/text-hi-per-char-notiles-ctm8.ctm")))
          Then("charset content is read") {
            charsetOutput.bytes shouldBe
                byteArrayOf(0x00, 0x7E, 0x42, 0x00, 0x00, 0x42, 0x7E, 0x00) +
                    byteArrayOf(0x00, 0x18, 0x18, 0x24, 0x24, 0x42, 0x42, 0x00) +
                    byteArrayOf(0x00, 0x7E, 0x42, 0x40, 0x40, 0x42, 0x7E, 0x00) +
                    byteArrayOf(0x00, 0x66, 0x42, 0x42, 0x42, 0x42, 0x66, 0x00)
          }
        //          Then("char colours are read") {
        //            charsetColourOutput.bytes shouldBe byteArrayOf(6, 3, 2, 1)
        //          }
        //          Then("char attributes are read") {
        //            charsetAttributesOutput.bytes shouldBe byteArrayOf(6, 3, 2, 1)
        //          }
        }
      }
    })
