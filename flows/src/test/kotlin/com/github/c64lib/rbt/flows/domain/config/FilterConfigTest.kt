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
package com.github.c64lib.rbt.flows.domain.config

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class FilterConfigTest :
    DescribeSpec({
      describe("FilterConfig") {
        describe("Nybbler filter") {
          it("can be created with lo and hi outputs") {
            val filter =
                FilterConfig.Nybbler(loOutput = "lo.bin", hiOutput = "hi.bin", normalizeHi = true)
            filter.loOutput shouldBe "lo.bin"
            filter.hiOutput shouldBe "hi.bin"
            filter.normalizeHi shouldBe true
          }

          it("can be created with lo only") {
            val filter = FilterConfig.Nybbler(loOutput = "lo.bin")
            filter.loOutput shouldBe "lo.bin"
            filter.hiOutput shouldBe null
            filter.normalizeHi shouldBe true
          }

          it("can be created with hi only") {
            val filter = FilterConfig.Nybbler(hiOutput = "hi.bin", normalizeHi = false)
            filter.loOutput shouldBe null
            filter.hiOutput shouldBe "hi.bin"
            filter.normalizeHi shouldBe false
          }

          it("has default normalizeHi=true") {
            val filter = FilterConfig.Nybbler()
            filter.normalizeHi shouldBe true
          }
        }

        describe("Interleaver filter") {
          it("can be created with multiple outputs") {
            val outputs = listOf("out1.bin", "out2.bin", "out3.bin")
            val filter = FilterConfig.Interleaver(outputs = outputs)
            filter.outputs shouldBe outputs
            filter.outputs shouldHaveSize 3
          }

          it("can be created with single output") {
            val filter = FilterConfig.Interleaver(outputs = listOf("single.bin"))
            filter.outputs shouldHaveSize 1
            filter.outputs.first() shouldBe "single.bin"
          }
        }

        describe("None filter") {
          it("is a singleton object") {
            val filter1 = FilterConfig.None
            val filter2 = FilterConfig.None
            (filter1 === filter2) shouldBe true
          }
        }
      }
    })
