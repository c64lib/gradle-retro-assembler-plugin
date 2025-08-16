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

import com.github.c64lib.rbt.flows.domain.config.AssemblyCommand
import com.github.c64lib.rbt.flows.domain.config.AssemblyConfig
import com.github.c64lib.rbt.flows.domain.config.AssemblyOptimization
import com.github.c64lib.rbt.flows.domain.config.CpuType
import com.github.c64lib.rbt.flows.domain.port.AssemblyPort
import com.github.c64lib.rbt.shared.domain.OutputFormat
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

class AssembleStepTest :
    BehaviorSpec({
      isolationMode = IsolationMode.InstancePerTest

      Given("an AssembleStep with default configuration") {
        val step =
            AssembleStep(
                name = "testAssemble",
                inputs = listOf("test.asm"),
                outputs = listOf("build/test.prg"))

        When("getting step properties") {
          Then("it should have correct name and type") {
            step.name shouldBe "testAssemble"
            step.taskType shouldBe "assemble"
            step.inputs shouldBe listOf("test.asm")
            step.outputs shouldBe listOf("build/test.prg")
          }

          And("it should have default configuration") {
            step.config.cpu shouldBe CpuType.MOS6510
            step.config.generateSymbols shouldBe true
            step.config.optimization shouldBe AssemblyOptimization.SPEED
            step.config.verbose shouldBe false
            step.config.outputFormat shouldBe OutputFormat.PRG
            step.config.srcDirs shouldBe listOf(".")
            step.config.includes shouldBe listOf("**/*.asm")
            step.config.excludes shouldBe listOf(".ra/**/*.asm")
            step.config.workDir shouldBe ".ra"
          }
        }
      }

      Given("an AssembleStep with custom configuration") {
        val config =
            AssemblyConfig(
                cpu = CpuType.MOS6502,
                generateSymbols = false,
                optimization = AssemblyOptimization.SIZE,
                includePaths = listOf("lib1", "lib2"),
                defines = mapOf("DEBUG" to "1", "VERSION" to "2.0"),
                verbose = true,
                outputFormat = OutputFormat.BIN,
                srcDirs = listOf("src", "asm"),
                includes = listOf("**/*.s", "**/*.asm"),
                excludes = listOf("build/**/*.asm"),
                workDir = "build")

        val step =
            AssembleStep(
                name = "customAssemble",
                inputs = listOf("main.asm", "utils.asm"),
                outputs = listOf("build/main.raw", "build/utils.raw"),
                config = config)

        When("getting configuration") {
          Then("it should return the correct configuration map") {
            val configMap = step.getConfiguration()
            configMap["cpu"] shouldBe "MOS6502"
            configMap["generateSymbols"] shouldBe false
            configMap["optimization"] shouldBe "SIZE"
            configMap["includePaths"] shouldBe listOf("lib1", "lib2")
            configMap["defines"] shouldBe mapOf("DEBUG" to "1", "VERSION" to "2.0")
            configMap["verbose"] shouldBe true
          }
        }
      }

      Given("an AssembleStep without assembly port") {
        val step =
            AssembleStep(
                name = "testAssemble",
                inputs = listOf("test.asm"),
                outputs = listOf("build/test.prg"))

        val tempDir = Files.createTempDirectory("test-project").toFile()
        val testFile = File(tempDir, "test.asm")
        testFile.writeText(".text \"Hello World\"")

        val context = mapOf<String, Any>("projectRootDir" to tempDir)

        When("executing without assembly port injection") {
          val exception = shouldThrow<IllegalStateException> { step.execute(context) }

          Then("it should throw an exception about missing assembly port") {
            exception.message shouldBe
                "AssemblyPort not injected for step 'testAssemble'. Call setAssemblyPort() before execution."
          }
        }

        tempDir.deleteRecursively()
      }

      Given("an AssembleStep with mock assembly port") {
        val executedCommands = mutableListOf<AssemblyCommand>()
        val mockPort =
            object : AssemblyPort {
              override fun assemble(command: AssemblyCommand) {
                executedCommands.add(command)
              }

              override fun assemble(commands: List<AssemblyCommand>) {
                executedCommands.addAll(commands)
              }
            }

        val step =
            AssembleStep(
                name = "testAssemble",
                inputs = listOf("test.asm"),
                outputs = listOf("build/test.prg"))
        step.setAssemblyPort(mockPort)

        val tempDir = Files.createTempDirectory("test-project").toFile()
        val testFile = File(tempDir, "test.asm")
        testFile.writeText(".text \"Hello World\"")

        val context = mapOf<String, Any>("projectRootDir" to tempDir)

        When("executing with valid inputs") {
          step.execute(context)

          Then("it should execute assembly commands") {
            executedCommands shouldHaveSize 1

            val command = executedCommands.first()
            command.source.name shouldBe "test.asm"
            command.outputFormat shouldBe OutputFormat.PRG
            command.defines shouldBe emptyList()
            command.values shouldBe emptyMap()
            command.libDirs shouldBe emptyList()
          }
        }

        tempDir.deleteRecursively()
      }

      Given("an AssembleStep with multiple input files") {
        val executedCommands = mutableListOf<AssemblyCommand>()
        val mockPort =
            object : AssemblyPort {
              override fun assemble(command: AssemblyCommand) {
                executedCommands.add(command)
              }

              override fun assemble(commands: List<AssemblyCommand>) {
                executedCommands.addAll(commands)
              }
            }

        val config =
            AssemblyConfig(
                defines = mapOf("PLATFORM" to "C64", "DEBUG" to ""),
                includePaths = listOf("lib"),
                outputFormat = OutputFormat.BIN)

        val step =
            AssembleStep(
                name = "multiAssemble",
                inputs = listOf("main.asm", "utils.asm"),
                outputs = listOf("build/main.raw", "build/utils.raw"),
                config = config)
        step.setAssemblyPort(mockPort)

        val tempDir = Files.createTempDirectory("test-project").toFile()
        val libDir = File(tempDir, "lib")
        libDir.mkdirs()

        val mainFile = File(tempDir, "main.asm")
        mainFile.writeText("lda #$01")

        val utilsFile = File(tempDir, "utils.asm")
        utilsFile.writeText("nop")

        val context = mapOf<String, Any>("projectRootDir" to tempDir)

        When("executing with multiple files") {
          step.execute(context)

          Then("it should create separate commands for each file") {
            executedCommands shouldHaveSize 2

            val mainCommand = executedCommands.find { it.source.name == "main.asm" }!!
            val utilsCommand = executedCommands.find { it.source.name == "utils.asm" }!!

            mainCommand.outputFormat shouldBe OutputFormat.BIN
            utilsCommand.outputFormat shouldBe OutputFormat.BIN

            mainCommand.defines shouldContain "PLATFORM"
            mainCommand.defines shouldContain "DEBUG"
            mainCommand.values shouldBe mapOf("PLATFORM" to "C64")

            mainCommand.libDirs shouldHaveSize 1
            mainCommand.libDirs.first().name shouldBe "lib"
          }
        }

        tempDir.deleteRecursively()
      }

      Given("validation scenarios") {
        When("validating step with no inputs") {
          val step =
              AssembleStep(
                  name = "noInputs", inputs = emptyList(), outputs = listOf("build/test.prg"))

          val errors = step.validate()

          Then("it should report missing inputs") {
            errors shouldContain "Assembly step 'noInputs' requires at least one input .asm file"
          }
        }

        When("validating step with no outputs") {
          val step =
              AssembleStep(name = "noOutputs", inputs = listOf("test.asm"), outputs = emptyList())

          val errors = step.validate()

          Then("it should report missing outputs") {
            errors shouldContain "Assembly step 'noOutputs' requires at least one output file"
          }
        }

        When("validating step with invalid file extensions") {
          val step =
              AssembleStep(
                  name = "invalidFiles",
                  inputs = listOf("test.txt", "data.bin"),
                  outputs = listOf("build/test.prg"))

          val errors = step.validate()

          Then("it should report invalid file extensions") {
            errors shouldContain
                "Assembly step 'invalidFiles' expects .asm or .s files, but got: test.txt"
            errors shouldContain
                "Assembly step 'invalidFiles' expects .asm or .s files, but got: data.bin"
          }
        }

        When("validating step with blank include paths") {
          val config = AssemblyConfig(includePaths = listOf("valid/path", ""))
          val step =
              AssembleStep(
                  name = "blankPaths",
                  inputs = listOf("test.asm"),
                  outputs = listOf("build/test.prg"),
                  config = config)

          val errors = step.validate()

          Then("it should report blank include paths") {
            errors shouldContain "Assembly step 'blankPaths' include path cannot be blank"
          }
        }

        When("validating valid step") {
          val step =
              AssembleStep(
                  name = "validStep",
                  inputs = listOf("main.asm", "utils.s"),
                  outputs = listOf("build/main.prg", "build/utils.prg"))

          val errors = step.validate()

          Then("it should pass validation") { errors.shouldBeEmpty() }
        }
      }

      Given("context validation scenarios") {
        val step =
            AssembleStep(
                name = "contextTest",
                inputs = listOf("test.asm"),
                outputs = listOf("build/test.prg"))
        step.setAssemblyPort(
            object : AssemblyPort {
              override fun assemble(command: AssemblyCommand) {}
            })

        When("executing without project root directory in context") {
          val context = emptyMap<String, Any>()

          val exception = shouldThrow<IllegalStateException> { step.execute(context) }

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
        val config1 = AssemblyConfig(cpu = CpuType.MOS6502)
        val config2 = AssemblyConfig(cpu = CpuType.MOS6510)

        val step1 = AssembleStep("test", listOf("a.asm"), listOf("a.prg"), config1)
        val step2 = AssembleStep("test", listOf("a.asm"), listOf("a.prg"), config1)
        val step3 = AssembleStep("test", listOf("a.asm"), listOf("a.prg"), config2)

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
            stepString shouldContain "AssembleStep"
            stepString shouldContain "name='test'"
            stepString shouldContain "inputs=[a.asm]"
            stepString shouldContain "outputs=[a.prg]"
          }
        }
      }
    })
