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
import com.github.c64lib.rbt.flows.domain.StepValidationException
import com.github.c64lib.rbt.flows.domain.port.ExomizerPort
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.io.File

class ExomizerStepTest :
    BehaviorSpec({
      given("ExomizerStep") {
        val tempDir = java.nio.file.Files.createTempDirectory("exomizer-step-test").toFile()
        val inputFile = File(tempDir, "input.bin")
        val outputFile = File(tempDir, "output.bin")
        inputFile.writeText("test data")

        `when`("raw mode step is created") {
          then("should have correct default values") {
            val step =
                ExomizerStep(
                    name = "crunch_raw",
                    inputs = listOf("input.bin"),
                    outputs = listOf("output.bin"),
                    mode = "raw")

            step.name shouldBe "crunch_raw"
            step.inputs shouldBe listOf("input.bin")
            step.outputs shouldBe listOf("output.bin")
            step.mode shouldBe "raw"
            step.loadAddress shouldBe "auto"
            step.forward shouldBe false
          }

          then("should validate successfully") {
            val step =
                ExomizerStep(
                    name = "crunch_raw",
                    inputs = listOf("input.bin"),
                    outputs = listOf("output.bin"))

            val errors = step.validate()
            errors.shouldBe(emptyList())
          }
        }

        `when`("memory mode step is created") {
          then("should support default load address") {
            val step =
                ExomizerStep(
                    name = "crunch_mem",
                    inputs = listOf("input.bin"),
                    outputs = listOf("output.bin"),
                    mode = "mem",
                    loadAddress = "auto")

            step.mode shouldBe "mem"
            step.loadAddress shouldBe "auto"
            step.forward shouldBe false
          }

          then("should support custom hex load address") {
            val step =
                ExomizerStep(
                    name = "crunch_mem",
                    inputs = listOf("input.bin"),
                    outputs = listOf("output.bin"),
                    mode = "mem",
                    loadAddress = "0x0800")

            val errors = step.validate()
            errors.shouldBe(emptyList())
          }

          then("should support dollar notation load address") {
            val step =
                ExomizerStep(
                    name = "crunch_mem",
                    inputs = listOf("input.bin"),
                    outputs = listOf("output.bin"),
                    mode = "mem",
                    loadAddress = "$2000")

            val errors = step.validate()
            errors.shouldBe(emptyList())
          }

          then("should support decimal load address") {
            val step =
                ExomizerStep(
                    name = "crunch_mem",
                    inputs = listOf("input.bin"),
                    outputs = listOf("output.bin"),
                    mode = "mem",
                    loadAddress = "2048")

            val errors = step.validate()
            errors.shouldBe(emptyList())
          }

          then("should support 'none' load address") {
            val step =
                ExomizerStep(
                    name = "crunch_mem",
                    inputs = listOf("input.bin"),
                    outputs = listOf("output.bin"),
                    mode = "mem",
                    loadAddress = "none")

            val errors = step.validate()
            errors.shouldBe(emptyList())
          }

          then("should support forward flag") {
            val step =
                ExomizerStep(
                    name = "crunch_mem",
                    inputs = listOf("input.bin"),
                    outputs = listOf("output.bin"),
                    mode = "mem",
                    forward = true)

            step.forward shouldBe true
          }
        }

        `when`("validating step configuration") {
          then("should reject missing input") {
            val step =
                ExomizerStep(
                    name = "bad_step", inputs = emptyList(), outputs = listOf("output.bin"))

            val errors = step.validate()
            errors.shouldBe(listOf("Exomizer step 'bad_step' requires an input file"))
          }

          then("should reject missing output") {
            val step =
                ExomizerStep(name = "bad_step", inputs = listOf("input.bin"), outputs = emptyList())

            val errors = step.validate()
            errors.shouldBe(listOf("Exomizer step 'bad_step' requires an output file"))
          }

          then("should reject invalid mode") {
            val step =
                ExomizerStep(
                    name = "bad_step",
                    inputs = listOf("input.bin"),
                    outputs = listOf("output.bin"),
                    mode = "invalid")

            val errors = step.validate()
            errors.any { it.contains("mode must be 'raw' or 'mem'") } shouldBe true
          }

          then("should reject invalid load address format") {
            val step =
                ExomizerStep(
                    name = "bad_step",
                    inputs = listOf("input.bin"),
                    outputs = listOf("output.bin"),
                    mode = "mem",
                    loadAddress = "not_a_number")

            val errors = step.validate()
            errors.any { it.contains("invalid load address") } shouldBe true
          }

          then("should accept lowercase mode values") {
            val rawStep =
                ExomizerStep(
                    name = "test",
                    inputs = listOf("input.bin"),
                    outputs = listOf("output.bin"),
                    mode = "raw")

            val memStep =
                ExomizerStep(
                    name = "test",
                    inputs = listOf("input.bin"),
                    outputs = listOf("output.bin"),
                    mode = "mem")

            rawStep.validate().shouldBe(emptyList())
            memStep.validate().shouldBe(emptyList())
          }
        }

        `when`("executing step without port") {
          then("should throw StepExecutionException") {
            val step =
                ExomizerStep(
                    name = "no_port", inputs = listOf("input.bin"), outputs = listOf("output.bin"))

            val context = mapOf<String, Any>("projectRootDir" to tempDir)

            shouldThrow<StepExecutionException> { step.execute(context) }
          }
        }

        `when`("executing step with invalid input file") {
          then("should throw StepValidationException") {
            val step =
                ExomizerStep(
                    name = "bad_input",
                    inputs = listOf("nonexistent.bin"),
                    outputs = listOf("output.bin"))

            step.setExomizerPort(MockExomizerPort())

            val context = mapOf<String, Any>("projectRootDir" to tempDir)

            shouldThrow<StepValidationException> { step.execute(context) }
          }
        }

        `when`("executing raw mode step") {
          then("should delegate to port") {
            val step =
                ExomizerStep(
                    name = "crunch_raw",
                    inputs = listOf("input.bin"),
                    outputs = listOf("output.bin"),
                    mode = "raw")

            val mockPort = MockExomizerPort()
            step.setExomizerPort(mockPort)

            val context = mapOf<String, Any>("projectRootDir" to tempDir)
            step.execute(context)

            mockPort.lastRawCrunchInput shouldBe inputFile
            mockPort.lastRawCrunchOutput shouldBe outputFile
          }
        }

        `when`("executing mem mode step") {
          then("should delegate to port with memory options") {
            val step =
                ExomizerStep(
                    name = "crunch_mem",
                    inputs = listOf("input.bin"),
                    outputs = listOf("output.bin"),
                    mode = "mem",
                    loadAddress = "0x0800",
                    forward = true)

            val mockPort = MockExomizerPort()
            step.setExomizerPort(mockPort)

            val context = mapOf<String, Any>("projectRootDir" to tempDir)
            step.execute(context)

            mockPort.lastMemCrunchInput shouldBe inputFile
            mockPort.lastMemCrunchOutput shouldBe outputFile
            mockPort.lastLoadAddress shouldBe "0x0800"
            mockPort.lastForward shouldBe true
          }
        }

        `when`("getting configuration") {
          then("raw mode should show mode and N/A for mem options") {
            val step =
                ExomizerStep(
                    name = "test",
                    inputs = listOf("input.bin"),
                    outputs = listOf("output.bin"),
                    mode = "raw")

            val config = step.getConfiguration()
            config["mode"] shouldBe "raw"
            config["loadAddress"] shouldBe "N/A"
            config["forward"] shouldBe "N/A"
          }

          then("mem mode should show all options") {
            val step =
                ExomizerStep(
                    name = "test",
                    inputs = listOf("input.bin"),
                    outputs = listOf("output.bin"),
                    mode = "mem",
                    loadAddress = "0x0800",
                    forward = true)

            val config = step.getConfiguration()
            config["mode"] shouldBe "mem"
            config["loadAddress"] shouldBe "0x0800"
            config["forward"] shouldBe true
          }
        }

        afterSpec { tempDir.deleteRecursively() }
      }
    }) {
  private class MockExomizerPort : ExomizerPort {
    var lastRawCrunchInput: File? = null
    var lastRawCrunchOutput: File? = null
    var lastMemCrunchInput: File? = null
    var lastMemCrunchOutput: File? = null
    var lastLoadAddress: String? = null
    var lastForward: Boolean? = null

    override fun crunchRaw(source: File, output: File) {
      lastRawCrunchInput = source
      lastRawCrunchOutput = output
    }

    override fun crunchMem(source: File, output: File, loadAddress: String, forward: Boolean) {
      lastMemCrunchInput = source
      lastMemCrunchOutput = output
      lastLoadAddress = loadAddress
      lastForward = forward
    }
  }
}
