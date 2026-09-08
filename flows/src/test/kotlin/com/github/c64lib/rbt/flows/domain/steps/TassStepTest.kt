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

import com.github.c64lib.rbt.flows.domain.StepExecutionException
import com.github.c64lib.rbt.flows.domain.config.TassCommand
import com.github.c64lib.rbt.flows.domain.config.TassConfig
import com.github.c64lib.rbt.flows.usecase.port.TassAssemblyPort
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.io.File
import java.nio.file.Files

class TassStepTest :
    BehaviorSpec({
      isolationMode = IsolationMode.InstancePerTest

      Given("a TassStep with default configuration") {
        val step =
            TassStep(
                name = "testTass", inputs = listOf("test.asm"), outputs = listOf("build/test.prg"))

        When("getting step properties") {
          Then("it should have correct name and type") {
            step.name shouldBe "testTass"
            step.taskType shouldBe "tass"
            step.inputs shouldBe listOf("test.asm")
            step.outputs shouldBe listOf("build/test.prg")
          }

          And("it should have default configuration") {
            step.config.outputFormat shouldBe "prg"
            step.config.cpu shouldBe null
            step.config.srcDirs shouldBe listOf(".")
            step.config.includes shouldBe listOf("**/*.asm")
            step.config.excludes shouldBe listOf(".ra/**/*.asm")
            step.config.workDir shouldBe ".ra"
          }
        }

        When("validating") { Then("there should be no errors") { step.validate().shouldBeEmpty() } }
      }

      Given("a TassStep with invalid configuration") {
        When("inputs are empty") {
          val step = TassStep(name = "t", outputs = listOf("a.prg"))
          Then("validation reports the missing input") {
            val errors = step.validate()
            errors shouldHaveSize 1
            errors.first() shouldContain "requires at least one input"
          }
        }

        When("outputs are empty") {
          val step = TassStep(name = "t", inputs = listOf("a.asm"))
          Then("validation reports the missing output") {
            val errors = step.validate()
            errors shouldHaveSize 1
            errors.first() shouldContain "requires at least one output"
          }
        }

        When("an input has a wrong extension") {
          val step = TassStep(name = "t", inputs = listOf("a.txt"), outputs = listOf("a.prg"))
          Then("validation reports the extension") {
            val errors = step.validate()
            errors shouldHaveSize 1
            errors.first() shouldContain "expects .asm or .s files"
          }
        }

        When("the output format is unsupported") {
          val step =
              TassStep(
                  name = "t",
                  inputs = listOf("a.asm"),
                  outputs = listOf("a.xex"),
                  config = TassConfig(outputFormat = "xex"))
          Then("validation reports the format") {
            val errors = step.validate()
            errors shouldHaveSize 1
            errors.first() shouldContain "output format"
          }
        }

        When("the cpu is not a known 64tass cpu") {
          val step =
              TassStep(
                  name = "t",
                  inputs = listOf("a.asm"),
                  outputs = listOf("a.prg"),
                  config = TassConfig(cpu = "z80"))
          Then("validation reports the cpu") {
            val errors = step.validate()
            errors shouldHaveSize 1
            errors.first() shouldContain "cpu"
          }
        }

        When("the cpu is a known 64tass cpu") {
          val step =
              TassStep(
                  name = "t",
                  inputs = listOf("a.asm"),
                  outputs = listOf("a.prg"),
                  config = TassConfig(cpu = "65816"))
          Then("validation passes") { step.validate().shouldBeEmpty() }
        }
      }

      Given("a TassStep without an injected port") {
        val step =
            TassStep(
                name = "testTass", inputs = listOf("test.asm"), outputs = listOf("build/test.prg"))

        val tempDir = Files.createTempDirectory("tass-test-project").toFile()
        val testFile = File(tempDir, "test.asm")
        testFile.writeText("* = \$0801")

        val context = mapOf<String, Any>("projectRootDir" to tempDir)

        When("executing") {
          val exception = shouldThrow<StepExecutionException> { step.execute(context) }

          Then("it should report the missing port") {
            exception.message shouldContain "TassAssemblyPort not injected"
          }
        }
      }

      Given("a TassStep with an injected recording port") {
        val recorded = mutableListOf<TassCommand>()
        val port =
            object : TassAssemblyPort {
              override fun assemble(command: TassCommand) {
                recorded.add(command)
              }
            }

        val tempDir = Files.createTempDirectory("tass-test-project").toFile()
        File(tempDir, "test.asm").writeText("* = \$0801")

        val context = mapOf<String, Any>("projectRootDir" to tempDir)

        When("executing a single-source step with explicit output") {
          val step =
              TassStep(
                  name = "testTass",
                  inputs = listOf("test.asm"),
                  outputs = listOf("build/test.prg"))
          step.setTassPort(port)
          step.execute(context)

          Then("exactly one command is assembled with the explicit output") {
            recorded shouldHaveSize 1
            recorded.first().source.name shouldBe "test.asm"
            recorded.first().outputFile shouldBe File(tempDir, "build/test.prg")
          }
        }

        When("the port fails") {
          val failingPort =
              object : TassAssemblyPort {
                override fun assemble(command: TassCommand) {
                  throw IllegalStateException("64tass exploded")
                }
              }
          val step =
              TassStep(
                  name = "testTass",
                  inputs = listOf("test.asm"),
                  outputs = listOf("build/test.prg"))
          step.setTassPort(failingPort)

          val exception = shouldThrow<StepExecutionException> { step.execute(context) }

          Then("the failure is wrapped with the step name") {
            exception.message shouldContain "64tass compilation failed"
          }
        }
      }
    })
