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

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain as shouldContainString

class CommandStepBuilderTest :
    BehaviorSpec({
      given("CommandStepBuilder") {
        `when`("using useFrom() with single input") {
          then("should return the first input path") {
            val builder = CommandStepBuilder("test", "tool")
            builder.from("input.txt")
            builder.to("output.txt")

            val result = builder.useFrom()

            result shouldBe "input.txt"
          }
        }

        `when`("using useTo() with single output") {
          then("should return the first output path") {
            val builder = CommandStepBuilder("test", "tool")
            builder.from("input.txt")
            builder.to("output.txt")

            val result = builder.useTo()

            result shouldBe "output.txt"
          }
        }

        `when`("using useFrom(0) and useTo(0)") {
          then("should be equivalent to useFrom() and useTo()") {
            val builder1 = CommandStepBuilder("test", "tool")
            builder1.from("input.txt")
            builder1.to("output.txt")

            val builder2 = CommandStepBuilder("test", "tool")
            builder2.from("input.txt")
            builder2.to("output.txt")

            builder1.useFrom(0) shouldBe builder1.useFrom()
            builder2.useTo(0) shouldBe builder2.useTo()
          }
        }

        `when`("using useFrom() with multiple inputs") {
          then("should return input at correct indices") {
            val builder = CommandStepBuilder("test", "tool")
            builder.from("file1.txt", "file2.txt", "file3.txt")
            builder.to("output.txt")

            builder.useFrom(0) shouldBe "file1.txt"
            builder.useFrom(1) shouldBe "file2.txt"
            builder.useFrom(2) shouldBe "file3.txt"
          }
        }

        `when`("using useTo() with multiple outputs") {
          then("should return output at correct indices") {
            val builder = CommandStepBuilder("test", "tool")
            builder.from("input.txt")
            builder.to("out1.txt", "out2.txt", "out3.txt")

            builder.useTo(0) shouldBe "out1.txt"
            builder.useTo(1) shouldBe "out2.txt"
            builder.useTo(2) shouldBe "out3.txt"
          }
        }

        `when`("using useFrom() without calling from() first") {
          then("should throw IllegalStateException") {
            val builder = CommandStepBuilder("test", "tool")
            builder.to("output.txt")

            val exception = shouldThrow<IllegalStateException> {
              builder.useFrom()
            }

            exception.message shouldContainString "no input paths have been defined"
            exception.message shouldContainString "Call from() first"
          }
        }

        `when`("using useTo() without calling to() first") {
          then("should throw IllegalStateException") {
            val builder = CommandStepBuilder("test", "tool")
            builder.from("input.txt")

            val exception = shouldThrow<IllegalStateException> {
              builder.useTo()
            }

            exception.message shouldContainString "no output paths have been defined"
            exception.message shouldContainString "Call to() first"
          }
        }

        `when`("using useFrom() with out-of-bounds index") {
          then("should throw IndexOutOfBoundsException") {
            val builder = CommandStepBuilder("test", "tool")
            builder.from("input.txt")
            builder.to("output.txt")

            val exception = shouldThrow<IndexOutOfBoundsException> {
              builder.useFrom(5)
            }

            exception.message shouldContainString "Cannot access input at index 5"
            exception.message shouldContainString "only 1 input"
            exception.message shouldContainString "Valid indices: 0..0"
          }
        }

        `when`("using useTo() with out-of-bounds index") {
          then("should throw IndexOutOfBoundsException") {
            val builder = CommandStepBuilder("test", "tool")
            builder.from("input.txt")
            builder.to("output.txt")

            val exception = shouldThrow<IndexOutOfBoundsException> {
              builder.useTo(3)
            }

            exception.message shouldContainString "Cannot access output at index 3"
            exception.message shouldContainString "only 1 output"
            exception.message shouldContainString "Valid indices: 0..0"
          }
        }

        `when`("using useFrom() with negative index") {
          then("should throw IndexOutOfBoundsException") {
            val builder = CommandStepBuilder("test", "tool")
            builder.from("input.txt")
            builder.to("output.txt")

            val exception = shouldThrow<IndexOutOfBoundsException> {
              builder.useFrom(-1)
            }

            exception.message shouldContainString "Cannot access input at index -1"
          }
        }

        `when`("using useTo() with negative index") {
          then("should throw IndexOutOfBoundsException") {
            val builder = CommandStepBuilder("test", "tool")
            builder.from("input.txt")
            builder.to("output.txt")

            val exception = shouldThrow<IndexOutOfBoundsException> {
              builder.useTo(-1)
            }

            exception.message shouldContainString "Cannot access output at index -1"
          }
        }

        `when`("calling useFrom() multiple times") {
          then("should return same value consistently") {
            val builder = CommandStepBuilder("test", "tool")
            builder.from("input.txt")
            builder.to("output.txt")

            val result1 = builder.useFrom()
            val result2 = builder.useFrom()

            result1 shouldBe result2
            result1 shouldBe "input.txt"
          }
        }

        `when`("calling useTo() multiple times") {
          then("should return same value consistently") {
            val builder = CommandStepBuilder("test", "tool")
            builder.from("input.txt")
            builder.to("output.txt")

            val result1 = builder.useTo()
            val result2 = builder.useTo()

            result1 shouldBe result2
            result1 shouldBe "output.txt"
          }
        }

        `when`("using useFrom() in param()") {
          then("should add resolved path to parameters") {
            val builder = CommandStepBuilder("test", "tool")
            builder.from("input.bin")
            builder.to("output.bin")
            builder.param(builder.useFrom())

            val step = builder.build()

            step.parameters shouldContain "input.bin"
          }
        }

        `when`("using useTo() in param()") {
          then("should add resolved path to parameters") {
            val builder = CommandStepBuilder("test", "tool")
            builder.from("input.bin")
            builder.to("output.bin")
            builder.param(builder.useTo())

            val step = builder.build()

            step.parameters shouldContain "output.bin"
          }
        }

        `when`("using useFrom() in option()") {
          then("should add resolved path to parameters") {
            val builder = CommandStepBuilder("test", "tool")
            builder.from("input.bin")
            builder.to("output.bin")
            builder.option("-i", builder.useFrom())

            val step = builder.build()

            step.parameters shouldContain "-i"
            step.parameters shouldContain "input.bin"
          }
        }

        `when`("using useTo() in option()") {
          then("should add resolved path to parameters") {
            val builder = CommandStepBuilder("test", "tool")
            builder.from("input.bin")
            builder.to("output.bin")
            builder.option("-o", builder.useTo())

            val step = builder.build()

            step.parameters shouldContain "-o"
            step.parameters shouldContain "output.bin"
          }
        }

        `when`("using shortcuts with withOption()") {
          then("should work with useFrom()") {
            val builder = CommandStepBuilder("test", "tool")
            builder.from("input.bin")
            builder.to("output.bin")
            builder.withOption("-i", builder.useFrom())

            val step = builder.build()

            step.parameters shouldContain "-i"
            step.parameters shouldContain "input.bin"
          }

          then("should work with useTo()") {
            val builder = CommandStepBuilder("test", "tool")
            builder.from("input.bin")
            builder.to("output.bin")
            builder.withOption("-o", builder.useTo())

            val step = builder.build()

            step.parameters shouldContain "-o"
            step.parameters shouldContain "output.bin"
          }
        }

        `when`("using useFrom() with index parameter in complex command") {
          then("should resolve correct input") {
            val builder = CommandStepBuilder("test", "tool")
            builder.from("file1.txt", "file2.txt")
            builder.to("output.txt")
            builder.option("-i1", builder.useFrom(0))
            builder.option("-i2", builder.useFrom(1))

            val step = builder.build()

            step.parameters shouldBe
                listOf("-i1", "file1.txt", "-i2", "file2.txt")
          }
        }

        `when`("using useTo() with index parameter in complex command") {
          then("should resolve correct output") {
            val builder = CommandStepBuilder("test", "tool")
            builder.from("input.txt")
            builder.to("out1.txt", "out2.txt")
            builder.option("-o1", builder.useTo(0))
            builder.option("-o2", builder.useTo(1))

            val step = builder.build()

            step.parameters shouldBe
                listOf("-o1", "out1.txt", "-o2", "out2.txt")
          }
        }

        `when`("using shortcuts in realistic exomizer command") {
          then("should build command with resolved paths") {
            val builder = CommandStepBuilder("exomize-game-linked", "exomizer")
            builder.from("build/game-linked.bin")
            builder.to("build/game-linked.z.bin")
            builder.param("raw")
            builder.flag("-T4")
            builder.option("-o", builder.useTo())
            builder.param(builder.useFrom())

            val step = builder.build()

            step.name shouldBe "exomize-game-linked"
            step.command shouldBe "exomizer"
            step.inputs shouldBe listOf("build/game-linked.bin")
            step.outputs shouldBe listOf("build/game-linked.z.bin")
            step.parameters shouldBe
                listOf("raw", "-T4", "-o", "build/game-linked.z.bin", "build/game-linked.bin")
          }
        }

        `when`("mixing from() and to() with multiple paths") {
          then("shortcuts should work correctly with indices") {
            val builder = CommandStepBuilder("process", "tool")
            builder.from("in1.txt")
            builder.from("in2.txt")
            builder.to("out1.txt")
            builder.to("out2.txt")

            builder.useFrom(0) shouldBe "in1.txt"
            builder.useFrom(1) shouldBe "in2.txt"
            builder.useTo(0) shouldBe "out1.txt"
            builder.useTo(1) shouldBe "out2.txt"
          }
        }

        `when`("using varargs from() and to()") {
          then("shortcuts should work with all paths") {
            val builder = CommandStepBuilder("process", "tool")
            builder.from("a.txt", "b.txt", "c.txt")
            builder.to("x.txt", "y.txt")

            builder.useFrom() shouldBe "a.txt"
            builder.useFrom(2) shouldBe "c.txt"
            builder.useTo() shouldBe "x.txt"
            builder.useTo(1) shouldBe "y.txt"
          }
        }

        `when`("out-of-bounds index with multiple paths") {
          then("should report correct bounds in exception") {
            val builder = CommandStepBuilder("test", "tool")
            builder.from("a.txt", "b.txt", "c.txt")
            builder.to("output.txt")

            val exception = shouldThrow<IndexOutOfBoundsException> {
              builder.useFrom(10)
            }

            exception.message shouldContainString "only 3 input"
            exception.message shouldContainString "Valid indices: 0..2"
          }
        }
      }
    })
