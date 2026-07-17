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
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class TestStepBuilderTest :
    BehaviorSpec({
      given("a TestStepBuilder with specs and watched inputs") {
        `when`("building") {
          then("specs and watched inputs are combined into inputs, specs kept separate") {
            val builder = TestStepBuilder("runSpecs")
            builder.specs("spec/math.spec.asm", "spec/mem.spec.asm")
            builder.from("lib/math.asm", "lib/mem.asm")

            val step = builder.build()

            step.name shouldBe "runSpecs"
            step.specs shouldContainExactly listOf("spec/math.spec.asm", "spec/mem.spec.asm")
            step.inputs shouldContainExactly
                listOf("spec/math.spec.asm", "spec/mem.spec.asm", "lib/math.asm", "lib/mem.asm")
          }
        }

        `when`("building with a single spec added via spec()") {
          then("the spec is included") {
            val builder = TestStepBuilder("single")
            builder.spec("spec/math.spec.asm")

            val step = builder.build()

            step.specs shouldContainExactly listOf("spec/math.spec.asm")
            step.inputs shouldContainExactly listOf("spec/math.spec.asm")
          }
        }

        `when`("building") {
          then("each spec derives .prg/.vs/.specOut outputs") {
            val builder = TestStepBuilder("runSpecs")
            builder.specs("spec/math.spec.asm")

            val step = builder.build()

            step.outputs shouldContainExactly
                listOf("spec/math.spec.prg", "spec/math.spec.vs", "spec/math.spec.specOut")
          }
        }
      }
    })
