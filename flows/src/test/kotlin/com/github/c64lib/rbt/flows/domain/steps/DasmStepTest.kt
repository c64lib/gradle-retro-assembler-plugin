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
import com.github.c64lib.rbt.flows.domain.config.DasmCommand
import com.github.c64lib.rbt.flows.domain.config.DasmConfig
import com.github.c64lib.rbt.flows.usecase.port.DasmAssemblyPort
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.io.File
import java.nio.file.Files

class DasmStepTest :
    BehaviorSpec({
      isolationMode = IsolationMode.InstancePerTest

      Given("a DasmStep with default configuration") {
        val step =
            DasmStep(
                name = "testDasm", inputs = listOf("test.asm"), outputs = listOf("build/test.prg"))

        When("getting step properties") {
          Then("it should have correct name and type") {
            step.name shouldBe "testDasm"
            step.taskType shouldBe "dasm"
            step.inputs shouldBe listOf("test.asm")
            step.outputs shouldBe listOf("build/test.prg")
          }

          And("it should have default configuration") {
            step.config.outputFormat shouldBe 1
            step.config.includePaths shouldBe emptyList()
            step.config.defines shouldBe emptyMap()
            step.config.verboseness shouldBe null
            step.config.srcDirs shouldBe listOf(".")
            step.config.includes shouldBe listOf("**/*.asm")
            step.config.excludes shouldBe listOf(".ra/**/*.asm")
            step.config.workDir shouldBe ".ra"
          }
        }
      }

      Given("a DasmStep with custom configuration") {
        val config =
            DasmConfig(
                includePaths = listOf("lib1", "lib2"),
                defines = mapOf("DEBUG" to "1", "VERSION" to "2.0"),
                outputFormat = 2,
                verboseness = 3,
                strictSyntax = true,
                removeOnError = true)

        val step =
            DasmStep(
                name = "customDasm",
                inputs = listOf("main.asm"),
                outputs = listOf("build/main.bin"),
                config = config)

        When("getting configuration") {
          Then("it should return the correct configuration map") {
            val configMap = step.getConfiguration()
            configMap["outputFormat"] shouldBe 2
            configMap["includePaths"] shouldBe listOf("lib1", "lib2")
            configMap["defines"] shouldBe mapOf("DEBUG" to "1", "VERSION" to "2.0")
            configMap["verboseness"] shouldBe 3
            configMap["strictSyntax"] shouldBe true
            configMap["removeOnError"] shouldBe true
          }
        }
      }

      Given("a DasmStep without dasm port") {
        val step =
            DasmStep(
                name = "testDasm", inputs = listOf("test.asm"), outputs = listOf("build/test.prg"))

        val tempDir = Files.createTempDirectory("test-project").toFile()
        val testFile = File(tempDir, "test.asm")
        testFile.writeText("  lda #\$01")

        val context = mapOf<String, Any>("projectRootDir" to tempDir)

        When("executing without dasm port injection") {
          val exception = shouldThrow<StepExecutionException> { step.execute(context) }

          Then("it should throw an exception about missing dasm port") {
            exception.message shouldBe "DasmAssemblyPort not injected"
            exception.stepName shouldBe "testDasm"
          }
        }

        tempDir.deleteRecursively()
      }

      Given("a DasmStep with mock dasm port and explicit output") {
        val executedCommands = mutableListOf<DasmCommand>()
        val mockPort =
            object : DasmAssemblyPort {
              override fun assemble(command: DasmCommand) {
                executedCommands.add(command)
              }
            }

        val step =
            DasmStep(
                name = "testDasm", inputs = listOf("test.asm"), outputs = listOf("build/test.prg"))
        step.setDasmPort(mockPort)

        val tempDir = Files.createTempDirectory("test-project").toFile()
        val testFile = File(tempDir, "test.asm")
        testFile.writeText("  lda #\$01")

        val context = mapOf<String, Any>("projectRootDir" to tempDir)

        When("executing with a single input and explicit output") {
          step.execute(context)

          Then("it should execute one command with the resolved output file") {
            executedCommands shouldHaveSize 1

            val command = executedCommands.first()
            command.source.name shouldBe "test.asm"
            command.outputFormat shouldBe 1
            command.defines shouldBe emptyMap()
            command.libDirs shouldBe emptyList()
            command.outputFile shouldBe File(tempDir, "build/test.prg")
          }
        }

        tempDir.deleteRecursively()
      }

      Given("a DasmStep with multiple input files") {
        val executedCommands = mutableListOf<DasmCommand>()
        val mockPort =
            object : DasmAssemblyPort {
              override fun assemble(command: DasmCommand) {
                executedCommands.add(command)
              }
            }

        val config =
            DasmConfig(
                defines = mapOf("PLATFORM" to "C64"),
                includePaths = listOf("lib"),
                outputFormat = 3)

        val step =
            DasmStep(
                name = "multiDasm",
                inputs = listOf("main.asm", "utils.asm"),
                outputs = listOf("build/main.raw", "build/utils.raw"),
                config = config)
        step.setDasmPort(mockPort)

        val tempDir = Files.createTempDirectory("test-project").toFile()
        val libDir = File(tempDir, "lib")
        libDir.mkdirs()

        val mainFile = File(tempDir, "main.asm")
        mainFile.writeText("  lda #\$01")

        val utilsFile = File(tempDir, "utils.asm")
        utilsFile.writeText("  nop")

        val context = mapOf<String, Any>("projectRootDir" to tempDir)

        When("executing with multiple files") {
          step.execute(context)

          Then("it should create separate commands for each file") {
            executedCommands shouldHaveSize 2

            val mainCommand = executedCommands.find { it.source.name == "main.asm" }!!
            val utilsCommand = executedCommands.find { it.source.name == "utils.asm" }!!

            mainCommand.outputFormat shouldBe 3
            utilsCommand.outputFormat shouldBe 3

            mainCommand.defines shouldBe mapOf("PLATFORM" to "C64")

            mainCommand.libDirs shouldHaveSize 1
            mainCommand.libDirs.first().name shouldBe "lib"
          }
        }

        tempDir.deleteRecursively()
      }

      Given("validation scenarios") {
        When("validating step with no inputs") {
          val step = DasmStep(name = "noInputs", inputs = emptyList(), outputs = listOf("out.prg"))

          val errors = step.validate()

          Then("it should report missing inputs") {
            errors shouldContain "Dasm step 'noInputs' requires at least one input .asm file"
          }
        }

        When("validating step with no outputs") {
          val step =
              DasmStep(name = "noOutputs", inputs = listOf("test.asm"), outputs = emptyList())

          val errors = step.validate()

          Then("it should report missing outputs") {
            errors shouldContain "Dasm step 'noOutputs' requires at least one output file"
          }
        }

        When("validating step with invalid file extensions") {
          val step =
              DasmStep(
                  name = "invalidFiles",
                  inputs = listOf("test.txt", "data.bin"),
                  outputs = listOf("out.prg"))

          val errors = step.validate()

          Then("it should report invalid file extensions") {
            errors shouldContain
                "Dasm step 'invalidFiles' expects .asm or .s files, but got: test.txt"
            errors shouldContain
                "Dasm step 'invalidFiles' expects .asm or .s files, but got: data.bin"
          }
        }

        When("validating step with out-of-range output format") {
          val step =
              DasmStep(
                  name = "badFormat",
                  inputs = listOf("test.asm"),
                  outputs = listOf("out.prg"),
                  config = DasmConfig(outputFormat = 4))

          val errors = step.validate()

          Then("it should report invalid output format") {
            errors shouldContain "Dasm step 'badFormat' output format must be 1-3, but got: 4"
          }
        }

        When("validating step with out-of-range verboseness") {
          val step =
              DasmStep(
                  name = "badVerbose",
                  inputs = listOf("test.asm"),
                  outputs = listOf("out.prg"),
                  config = DasmConfig(verboseness = 5))

          val errors = step.validate()

          Then("it should report invalid verboseness") {
            errors shouldContain "Dasm step 'badVerbose' verboseness must be 0-4, but got: 5"
          }
        }

        When("validating step with out-of-range error format") {
          val step =
              DasmStep(
                  name = "badError",
                  inputs = listOf("test.asm"),
                  outputs = listOf("out.prg"),
                  config = DasmConfig(errorFormat = 3))

          val errors = step.validate()

          Then("it should report invalid error format") {
            errors shouldContain "Dasm step 'badError' error format must be 0-2, but got: 3"
          }
        }

        When("validating step with out-of-range symbol table sort") {
          val step =
              DasmStep(
                  name = "badSort",
                  inputs = listOf("test.asm"),
                  outputs = listOf("out.prg"),
                  config = DasmConfig(symbolTableSort = 2))

          val errors = step.validate()

          Then("it should report invalid symbol table sort") {
            errors shouldContain "Dasm step 'badSort' symbol table sort must be 0-1, but got: 2"
          }
        }

        When("validating valid step") {
          val step =
              DasmStep(
                  name = "validStep",
                  inputs = listOf("main.asm", "utils.s"),
                  outputs = listOf("build/main.prg"),
                  config =
                      DasmConfig(
                          outputFormat = 2, verboseness = 0, errorFormat = 2, symbolTableSort = 1))

          val errors = step.validate()

          Then("it should pass validation") { errors.shouldBeEmpty() }
        }
      }

      Given("context validation scenarios") {
        val step =
            DasmStep(name = "contextTest", inputs = listOf("test.asm"), outputs = listOf("out.prg"))
        step.setDasmPort(
            object : DasmAssemblyPort {
              override fun assemble(command: DasmCommand) {}
            })

        When("executing without project root directory in context") {
          val context = emptyMap<String, Any>()

          val exception = shouldThrow<StepExecutionException> { step.execute(context) }

          Then("it should throw exception about missing project root") {
            exception.message shouldBe "Project root directory not found in execution context"
          }
        }

        When("executing with non-existent input file") {
          val tempDir = Files.createTempDirectory("test-project").toFile()
          val context = mapOf<String, Any>("projectRootDir" to tempDir)

          val exception = shouldThrow<IllegalArgumentException> { step.execute(context) }

          Then("it should throw exception about missing file") {
            exception.message shouldContain "Source file does not exist:"
            exception.message shouldContain "test.asm"
          }

          tempDir.deleteRecursively()
        }
      }

      Given("equality and string representation") {
        val config1 = DasmConfig(outputFormat = 1)
        val config2 = DasmConfig(outputFormat = 2)

        val step1 = DasmStep("test", listOf("a.asm"), listOf("a.prg"), config1)
        val step2 = DasmStep("test", listOf("a.asm"), listOf("a.prg"), config1)
        val step3 = DasmStep("test", listOf("a.asm"), listOf("a.prg"), config2)

        When("comparing equal steps") {
          Then("they should be equal") {
            (step1 == step2) shouldBe true
            step1.hashCode() shouldBe step2.hashCode()
          }
        }

        When("comparing different steps") {
          Then("they should not be equal") { (step1 == step3) shouldBe false }
        }

        When("getting string representation") {
          Then("it should contain step details") {
            val stepString = step1.toString()
            stepString shouldContain "DasmStep"
            stepString shouldContain "name='test'"
            stepString shouldContain "inputs=[a.asm]"
            stepString shouldContain "outputs=[a.prg]"
          }
        }
      }
    })
