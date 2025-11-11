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
package com.github.c64lib.rbt.flows.domain.steps

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.shouldBe

class CommandStepTest :
    BehaviorSpec({
      given("a basic command step") {
        val step = CommandStep("compile", "kickass").from("src/main.asm").to("build/main.prg")

        `when`("getting its properties") {
          then("it should have correct basic properties") {
            step.name shouldBe "compile"
            step.taskType shouldBe "command"
            step.getCommandLine() shouldBe listOf("kickass")
          }
        }

        `when`("validating the step") {
          val errors = step.validate()

          then("it should be valid") { errors shouldHaveSize 0 }
        }
      }

      given("a basic command step without inputs/outputs") {
        val step = CommandStep("compile", "kickass")

        `when`("validating the step") {
          val errors = step.validate()

          then("it should pass validation (input/output requirement deferred to adapters)") {
            errors shouldHaveSize 0
          }
        }
      }

      given("a command step with parameters using + operator") {
        val step =
            CommandStep("compile", "kickass").from("src/main.asm").to("build/main.prg") +
                "-cpu" +
                "6510" +
                "-o" +
                "build/main.prg"

        `when`("getting the command line") {
          val commandLine = step.getCommandLine()

          then("it should include all parameters") {
            commandLine shouldBe listOf("kickass", "-cpu", "6510", "-o", "build/main.prg")
          }
        }

        `when`("getting inputs and outputs") {
          then("it should have correct paths") {
            step.inputs shouldBe listOf("src/main.asm")
            step.outputs shouldBe listOf("build/main.prg")
          }
        }

        `when`("getting configuration") {
          val config = step.getConfiguration()

          then("it should contain command details") {
            config shouldContainKey "command"
            config shouldContainKey "parameters"
            config["command"] shouldBe "kickass"
            config["parameters"] shouldBe listOf("-cpu", "6510", "-o", "build/main.prg")
          }
        }
      }

      given("a command step with multiple inputs and outputs") {
        val step =
            CommandStep("process", "tool")
                .from("input1.txt", "input2.txt")
                .to("output1.dat", "output2.dat") + "--batch" + "--verbose"

        `when`("checking inputs and outputs") {
          then("it should handle multiple paths") {
            step.inputs shouldHaveSize 2
            step.inputs shouldContain "input1.txt"
            step.inputs shouldContain "input2.txt"
            step.outputs shouldHaveSize 2
            step.outputs shouldContain "output1.dat"
            step.outputs shouldContain "output2.dat"
          }
        }

        `when`("getting command line") {
          val commandLine = step.getCommandLine()

          then("it should include all parameters") {
            commandLine shouldBe listOf("tool", "--batch", "--verbose")
          }
        }
      }

      given("a command step with blank command") {
        val step = CommandStep("invalid", "").from("input.txt")

        `when`("validating the step") {
          val errors = step.validate()

          then("it should report validation error") {
            errors shouldHaveSize 1
            errors shouldContain "Command cannot be blank"
          }
        }
      }

      given("two identical command steps") {
        val step1 =
            CommandStep("test", "echo").from("input.txt").to("output.txt") + "hello" + "world"
        val step2 =
            CommandStep("test", "echo").from("input.txt").to("output.txt") + "hello" + "world"

        `when`("comparing them") {
          then("they should be equal") {
            step1 shouldBe step2
            step1.hashCode() shouldBe step2.hashCode()
          }
        }
      }

      given("two different command steps") {
        val step1 = CommandStep("test1", "echo") + "hello"
        val step2 = CommandStep("test2", "echo") + "world"

        `when`("comparing them") {
          then("they should not be equal") { (step1 == step2) shouldBe false }
        }
      }
    })
