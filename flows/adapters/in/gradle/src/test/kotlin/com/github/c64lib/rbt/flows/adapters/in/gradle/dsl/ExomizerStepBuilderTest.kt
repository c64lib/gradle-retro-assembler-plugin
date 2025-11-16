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
package com.github.c64lib.rbt.flows.adapters.`in`.gradle.dsl

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class ExomizerStepBuilderTest :
    BehaviorSpec({
      given("ExomizerStepBuilder") {
        `when`("building raw mode step") {
          then("should create with correct configuration") {
            val builder = ExomizerStepBuilder("crunch_raw")
            builder.from("input.bin")
            builder.to("output.bin")
            builder.raw()

            val step = builder.build()

            step.name shouldBe "crunch_raw"
            step.inputs shouldBe listOf("input.bin")
            step.outputs shouldBe listOf("output.bin")
            step.mode shouldBe "raw"
          }

          then("should configure all raw mode options") {
            val builder = ExomizerStepBuilder("crunch_raw")
            builder.from("input.bin")
            builder.to("output.bin")
            builder.raw {
              backwards = true
              reverse = false
              maxOffset = 32768
              maxLength = 16384
              passes = 50
              quiet = true
            }

            val step = builder.build()

            step.mode shouldBe "raw"
            step.backwards shouldBe true
            step.reverse shouldBe false
            step.maxOffset shouldBe 32768
            step.maxLength shouldBe 16384
            step.passes shouldBe 50
            step.quiet shouldBe true
          }
        }

        `when`("building mem mode step") {
          then("should use default values") {
            val builder = ExomizerStepBuilder("crunch_mem")
            builder.from("input.bin")
            builder.to("output.bin")
            builder.mem {}

            val step = builder.build()

            step.name shouldBe "crunch_mem"
            step.inputs shouldBe listOf("input.bin")
            step.outputs shouldBe listOf("output.bin")
            step.mode shouldBe "mem"
            step.loadAddress shouldBe "auto"
            step.forward shouldBe false
          }

          then("should accept custom memory-specific configuration") {
            val builder = ExomizerStepBuilder("crunch_mem")
            builder.from("input.bin")
            builder.to("output.bin")
            builder.mem {
              loadAddress = "0x0800"
              forward = true
            }

            val step = builder.build()

            step.mode shouldBe "mem"
            step.loadAddress shouldBe "0x0800"
            step.forward shouldBe true
          }

          then("should configure all options including raw and memory-specific") {
            val builder = ExomizerStepBuilder("crunch_mem")
            builder.from("input.bin")
            builder.to("output.bin")
            builder.mem {
              backwards = true
              passes = 75
              maxOffset = 16384
              quiet = true
              loadAddress = "$2000"
              forward = true
            }

            val step = builder.build()

            step.mode shouldBe "mem"
            step.backwards shouldBe true
            step.passes shouldBe 75
            step.maxOffset shouldBe 16384
            step.quiet shouldBe true
            step.loadAddress shouldBe "$2000"
            step.forward shouldBe true
          }
        }

        `when`("configuring input and output paths") {
          then("should overwrite previous value on second from() call") {
            val builder = ExomizerStepBuilder("crunch")
            builder.from("input.bin")
            builder.from("input2.bin")
            builder.to("output.bin")
            builder.raw()

            val step = builder.build()

            step.inputs shouldBe listOf("input2.bin")
          }
        }
      }
    })
