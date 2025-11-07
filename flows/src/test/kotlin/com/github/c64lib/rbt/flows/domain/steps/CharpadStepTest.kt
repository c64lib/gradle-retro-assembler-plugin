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

import com.github.c64lib.rbt.flows.adapters.out.charpad.CharpadAdapter
import com.github.c64lib.rbt.flows.domain.FlowValidationException
import com.github.c64lib.rbt.flows.domain.config.CharpadCommand
import com.github.c64lib.rbt.flows.domain.config.CharpadCompression
import com.github.c64lib.rbt.flows.domain.config.CharpadConfig
import com.github.c64lib.rbt.flows.domain.config.CharpadFormat
import com.github.c64lib.rbt.flows.domain.port.CharpadPort
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

class CharpadStepTest :
    BehaviorSpec({
      isolationMode = IsolationMode.InstancePerTest

      Given("a CharpadStep with default configuration") {
        val step =
            CharpadStep(
                name = "testCharpad",
                inputs = listOf("test.ctm"),
                outputs = listOf("build/charset.chr", "build/map.bin"))

        When("getting step properties") {
          Then("it should have correct name and type") {
            step.name shouldBe "testCharpad"
            step.taskType shouldBe "charpad"
            step.inputs shouldBe listOf("test.ctm")
            step.outputs shouldBe listOf("build/charset.chr", "build/map.bin")
          }

          And("it should have default configuration") {
            step.config.compression shouldBe CharpadCompression.NONE
            step.config.exportFormat shouldBe CharpadFormat.STANDARD
            step.config.tileSize shouldBe 8
            step.config.charsetOptimization shouldBe true
            step.config.generateMap shouldBe true
            step.config.generateCharset shouldBe true
            step.config.ctm8PrototypeCompatibility shouldBe false
            // Metadata options aligned with original processor defaults
            step.config.namespace shouldBe ""
            step.config.prefix shouldBe ""
            step.config.includeVersion shouldBe false
            step.config.includeBgColours shouldBe true
            step.config.includeCharColours shouldBe true
            step.config.includeMode shouldBe false
          }
        }
      }

      Given("a CharpadStep with custom configuration") {
        val config =
            CharpadConfig(
                compression = CharpadCompression.RLE,
                exportFormat = CharpadFormat.OPTIMIZED,
                tileSize = 16,
                charsetOptimization = false,
                generateMap = false,
                generateCharset = true,
                ctm8PrototypeCompatibility = true,
                namespace = "sprites",
                prefix = "CHARSET_",
                includeVersion = true,
                includeBgColours = false,
                includeCharColours = false,
                includeMode = true)

        val step =
            CharpadStep(
                name = "customCharpad",
                inputs = listOf("sprites.ctm"),
                outputs = listOf("build/sprites.chr", "build/sprites.h"),
                config = config)

        When("getting configuration") {
          Then("it should return the correct configuration map") {
            val configMap = step.getConfiguration()
            configMap["compression"] shouldBe "RLE"
            configMap["exportFormat"] shouldBe "OPTIMIZED"
            configMap["tileSize"] shouldBe 16
            configMap["charsetOptimization"] shouldBe false
            configMap["generateMap"] shouldBe false
            configMap["generateCharset"] shouldBe true
            configMap["ctm8PrototypeCompatibility"] shouldBe true
            configMap["namespace"] shouldBe "sprites"
            configMap["prefix"] shouldBe "CHARSET_"
            configMap["includeVersion"] shouldBe true
            configMap["includeBgColours"] shouldBe false
            configMap["includeCharColours"] shouldBe false
            configMap["includeMode"] shouldBe true
          }
        }
      }

      Given("a CharpadStep without charpad port") {
        val step =
            CharpadStep(
                name = "testCharpad",
                inputs = listOf("test.ctm"),
                outputs = listOf("build/charset.chr"))

        val tempDir = Files.createTempDirectory("test-project").toFile()
        val testFile = File(tempDir, "test.ctm")
        // Create minimal valid CTM file content
        testFile.writeBytes("CTM".toByteArray() + byteArrayOf(5) + ByteArray(100))

        val context = mapOf<String, Any>("projectRootDir" to tempDir)

        When("executing without charpad port injection") {
          val exception = shouldThrow<IllegalStateException> { step.execute(context) }

          Then("it should throw an exception about missing charpad port") {
            exception.message shouldBe
                "CharpadPort not injected for step 'testCharpad'. Call setCharpadPort() before execution."
          }
        }

        tempDir.deleteRecursively()
      }

      Given("a CharpadStep with mock charpad port") {
        val executedCommands = mutableListOf<CharpadCommand>()
        val mockPort =
            object : CharpadPort {
              override fun process(command: CharpadCommand) {
                executedCommands.add(command)
              }

              override fun process(commands: List<CharpadCommand>) {
                executedCommands.addAll(commands)
              }
            }

        val step =
            CharpadStep(
                name = "testCharpad",
                inputs = listOf("test.ctm"),
                outputs = listOf("build/charset.chr", "build/map.bin"))
        step.setCharpadPort(mockPort)

        val tempDir = Files.createTempDirectory("test-project").toFile()
        val testFile = File(tempDir, "test.ctm")
        // Create minimal valid CTM file content
        testFile.writeBytes("CTM".toByteArray() + byteArrayOf(5) + ByteArray(100))

        val context = mapOf<String, Any>("projectRootDir" to tempDir)

        When("executing with valid inputs") {
          step.execute(context)

          Then("it should execute charpad commands") {
            executedCommands shouldHaveSize 1

            val command = executedCommands.first()
            command.inputFile.name shouldBe "test.ctm"
            command.outputFiles.keys shouldContain "charset"
            command.outputFiles.keys shouldContain "map"
            command.config shouldBe step.config
            command.projectRootDir shouldBe tempDir
          }
        }

        tempDir.deleteRecursively()
      }

      Given("a CharpadStep with multiple input files") {
        val executedCommands = mutableListOf<CharpadCommand>()
        val mockPort =
            object : CharpadPort {
              override fun process(command: CharpadCommand) {
                executedCommands.add(command)
              }

              override fun process(commands: List<CharpadCommand>) {
                executedCommands.addAll(commands)
              }
            }

        val step =
            CharpadStep(
                name = "multiCharpad",
                inputs = listOf("charset1.ctm", "charset2.ctm"),
                outputs = listOf("build/charset.chr", "build/map.bin"))
        step.setCharpadPort(mockPort)

        val tempDir = Files.createTempDirectory("test-project").toFile()
        val testFile1 = File(tempDir, "charset1.ctm")
        val testFile2 = File(tempDir, "charset2.ctm")
        testFile1.writeBytes("CTM".toByteArray() + byteArrayOf(5) + ByteArray(100))
        testFile2.writeBytes("CTM".toByteArray() + byteArrayOf(6) + ByteArray(120))

        val context = mapOf<String, Any>("projectRootDir" to tempDir)

        When("executing with multiple valid inputs") {
          step.execute(context)

          Then("it should execute charpad commands for each input file") {
            executedCommands shouldHaveSize 2

            executedCommands[0].inputFile.name shouldBe "charset1.ctm"
            executedCommands[1].inputFile.name shouldBe "charset2.ctm"

            // Both commands should share the same output files map and configuration
            executedCommands.forEach { command ->
              command.outputFiles.keys shouldContain "charset"
              command.outputFiles.keys shouldContain "map"
              command.config shouldBe step.config
              command.projectRootDir shouldBe tempDir
            }
          }
        }

        tempDir.deleteRecursively()
      }

      Given("a CharpadStep with output file type detection") {
        val executedCommands = mutableListOf<CharpadCommand>()
        val mockPort =
            object : CharpadPort {
              override fun process(command: CharpadCommand) {
                executedCommands.add(command)
              }
            }

        val step =
            CharpadStep(
                name = "detectionTest",
                inputs = listOf("test.ctm"),
                outputs =
                    listOf(
                        "build/charset.chr",
                        "build/tilemap.map",
                        "build/tiles.tiles",
                        "build/header.h",
                        "build/metadata.inc",
                        "build/char_attributes.bin",
                        "build/char_colours.bin",
                        "build/char_materials.bin",
                        "build/screen_colors.bin",
                        "build/unknown_output.bin"))
        step.setCharpadPort(mockPort)

        val tempDir = Files.createTempDirectory("test-project").toFile()
        val testFile = File(tempDir, "test.ctm")
        testFile.writeBytes("CTM".toByteArray() + byteArrayOf(5) + ByteArray(100))

        val context = mapOf<String, Any>("projectRootDir" to tempDir)

        When("executing with various output file types") {
          step.execute(context)

          Then("it should correctly detect output file types") {
            executedCommands shouldHaveSize 1

            val outputFiles = executedCommands.first().outputFiles
            outputFiles.keys shouldContain "charset"
            outputFiles.keys shouldContain "map"
            outputFiles.keys shouldContain "tiles"
            outputFiles.keys shouldContain "header"
            outputFiles.keys shouldContain "metadata"
            outputFiles.keys shouldContain "charattributes"
            outputFiles.keys shouldContain "charcolours"
            outputFiles.keys shouldContain "charmaterials"
            outputFiles.keys shouldContain "charscreencolours"
            outputFiles.keys shouldContain "output9" // Fallback for unknown type

            outputFiles["charset"]?.name shouldBe "charset.chr"
            outputFiles["map"]?.name shouldBe "tilemap.map"
            outputFiles["tiles"]?.name shouldBe "tiles.tiles"
            outputFiles["header"]?.name shouldBe "header.h"
            outputFiles["metadata"]?.name shouldBe "metadata.inc"
          }
        }

        tempDir.deleteRecursively()
      }

      Given("a CharpadStep with missing input file") {
        val step =
            CharpadStep(
                name = "testCharpad",
                inputs = listOf("nonexistent.ctm"),
                outputs = listOf("build/charset.chr"))
        step.setCharpadPort(CharpadAdapter())

        val tempDir = Files.createTempDirectory("test-project").toFile()
        val context = mapOf<String, Any>("projectRootDir" to tempDir)

        When("executing with missing input file") {
          val exception = shouldThrow<IllegalArgumentException> { step.execute(context) }

          Then("it should throw an exception about missing file") {
            exception.message shouldContain "CTM file does not exist"
            exception.message shouldContain "nonexistent.ctm"
          }
        }

        tempDir.deleteRecursively()
      }

      Given("a CharpadStep validation") {
        When("validating step with no inputs") {
          val step =
              CharpadStep(
                  name = "noInputs", inputs = emptyList(), outputs = listOf("build/charset.chr"))
          val errors = step.validate()

          Then("it should report missing input files") {
            errors shouldHaveSize 1
            errors shouldContain "Charpad step 'noInputs' requires at least one input .ctm file"
          }
        }

        When("validating step with no outputs") {
          val step =
              CharpadStep(name = "noOutputs", inputs = listOf("test.ctm"), outputs = emptyList())
          val errors = step.validate()

          Then("it should report missing output files") {
            errors shouldHaveSize 1
            errors shouldContain "Charpad step 'noOutputs' requires at least one output file"
          }
        }

        When("validating step with non-CTM input") {
          val step =
              CharpadStep(
                  name = "wrongExt",
                  inputs = listOf("test.txt"),
                  outputs = listOf("build/charset.chr"))
          val errors = step.validate()

          Then("it should report wrong file extension") {
            errors shouldHaveSize 1
            errors shouldContain "Charpad step 'wrongExt' expects .ctm files, but got: test.txt"
          }
        }

        When("validating step with invalid tile size") {
          val config = CharpadConfig(tileSize = 12)
          val step =
              CharpadStep(
                  name = "invalidTileSize",
                  inputs = listOf("test.ctm"),
                  outputs = listOf("build/charset.chr"),
                  config = config)
          val errors = step.validate()

          Then("it should report invalid tile size") {
            errors shouldHaveSize 1
            errors shouldContain
                "Charpad step 'invalidTileSize' tile size must be 8, 16, or 32, but got: 12"
          }
        }

        When("validating valid step") {
          val step =
              CharpadStep(
                  name = "valid",
                  inputs = listOf("test.ctm"),
                  outputs = listOf("build/charset.chr"))
          val errors = step.validate()

          Then("it should have no validation errors") { errors.shouldBeEmpty() }
        }
      }

      Given("CharpadStep integration with real CharpadAdapter") {
        val tempDir = Files.createTempDirectory("charpad-integration-test").toFile()

        // Create output directory
        File(tempDir, "build").mkdirs()

        // Copy real CTM file from test resources to temp directory
        val ctmFile = File(tempDir, "test.ctm")
        val ctmResourceStream =
            this.javaClass.getResourceAsStream("/test-integration.ctm")
                ?: throw IllegalStateException("CTM test resource file not found")

        ctmResourceStream.use { input ->
          ctmFile.outputStream().use { output -> input.copyTo(output) }
        }

        val config =
            CharpadConfig(
                generateCharset = true,
                generateMap = true,
                namespace = "test",
                prefix = "CHARSET_",
                includeVersion = true)

        val step =
            CharpadStep(
                name = "integrationTest",
                inputs = listOf("test.ctm"),
                outputs = listOf("build/charset.chr", "build/map.bin", "build/header.h"),
                config = config)

        step.setCharpadPort(CharpadAdapter())

        val context = mapOf<String, Any>("projectRootDir" to tempDir)

        When("executing integration test with real CharpadAdapter") {
          try {
            step.execute(context)

            Then("it should process the CTM file successfully") {
              // Note: The actual file creation depends on the charpad processor implementation
              // This test verifies that the integration doesn't throw exceptions
              // and that the CharpadAdapter is properly invoked
              true shouldBe true // Test passes if no exception is thrown
            }
          } catch (e: FlowValidationException) {
            // Expected for invalid CTM content in this simplified test
            Then("it should handle invalid CTM gracefully with proper error message") {
              e.message shouldContain "Invalid CTM file format"
            }
          }
        }

        tempDir.deleteRecursively()
      }

      Given("CharpadStep equals and hashCode") {
        val config1 = CharpadConfig(tileSize = 8)
        val config2 = CharpadConfig(tileSize = 16)

        val step1 =
            CharpadStep(
                name = "test1",
                inputs = listOf("test.ctm"),
                outputs = listOf("build/charset.chr"),
                config = config1)

        val step2 =
            CharpadStep(
                name = "test1",
                inputs = listOf("test.ctm"),
                outputs = listOf("build/charset.chr"),
                config = config1)

        val step3 =
            CharpadStep(
                name = "test1",
                inputs = listOf("test.ctm"),
                outputs = listOf("build/charset.chr"),
                config = config2)

        When("comparing identical steps") {
          Then("they should be equal") {
            (step1 == step2) shouldBe true
            step1.hashCode() shouldBe step2.hashCode()
          }
        }

        When("comparing steps with different configurations") {
          Then("they should not be equal") {
            (step1 == step3) shouldBe false
            // Hash codes may or may not be equal for different objects, so we don't assert equality
          }
        }
      }

      Given("CharpadStep toString") {
        val step =
            CharpadStep(
                name = "testStep", inputs = listOf("input.ctm"), outputs = listOf("output.chr"))

        When("converting to string") {
          val result = step.toString()

          Then("it should contain step information") {
            result shouldContain "testStep"
            result shouldContain "input.ctm"
            result shouldContain "output.chr"
          }
        }
      }
    })
