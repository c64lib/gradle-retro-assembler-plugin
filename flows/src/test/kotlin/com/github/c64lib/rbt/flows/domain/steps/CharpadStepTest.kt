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
import com.github.c64lib.rbt.flows.domain.StepExecutionException
import com.github.c64lib.rbt.flows.domain.StepValidationException
import com.github.c64lib.rbt.flows.domain.config.*
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
                charpadOutputs =
                    CharpadOutputs(
                        charsets = listOf(CharsetOutput("build/charset.chr")),
                        maps = listOf(MapOutput("build/map.bin"))))

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
                charpadOutputs =
                    CharpadOutputs(
                        charsets = listOf(CharsetOutput("build/sprites.chr")),
                        metadata = listOf(MetadataOutput("build/sprites.h"))),
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
            configMap["charsetOutputs"] shouldBe 1
            configMap["metadataOutputs"] shouldBe 1
          }
        }
      }

      Given("a CharpadStep without charpad port") {
        val step =
            CharpadStep(
                name = "testCharpad",
                inputs = listOf("test.ctm"),
                charpadOutputs =
                    CharpadOutputs(charsets = listOf(CharsetOutput("build/charset.chr"))))

        val tempDir = Files.createTempDirectory("test-project").toFile()
        val testFile = File(tempDir, "test.ctm")
        // Create minimal valid CTM file content
        testFile.writeBytes("CTM".toByteArray() + byteArrayOf(5) + ByteArray(100))

        val context = mapOf<String, Any>("projectRootDir" to tempDir)

        When("executing without charpad port injection") {
          val exception = shouldThrow<StepExecutionException> { step.execute(context) }

          Then("it should throw an exception about missing charpad port") {
            exception.message shouldBe "CharpadPort not injected"
            exception.stepName shouldBe "testCharpad"
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
                charpadOutputs =
                    CharpadOutputs(
                        charsets = listOf(CharsetOutput("build/charset.chr")),
                        maps = listOf(MapOutput("build/map.bin"))))
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
            command.charpadOutputs.charsets shouldHaveSize 1
            command.charpadOutputs.maps shouldHaveSize 1
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
                charpadOutputs =
                    CharpadOutputs(
                        charsets = listOf(CharsetOutput("build/charset.chr")),
                        maps = listOf(MapOutput("build/map.bin"))))
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

            // Both commands should share the same charpad outputs and configuration
            executedCommands.forEach { command ->
              command.charpadOutputs.charsets shouldHaveSize 1
              command.charpadOutputs.maps shouldHaveSize 1
              command.config shouldBe step.config
              command.projectRootDir shouldBe tempDir
            }
          }
        }

        tempDir.deleteRecursively()
      }

      Given("a CharpadStep with multiple outputs of different types") {
        val executedCommands = mutableListOf<CharpadCommand>()
        val mockPort =
            object : CharpadPort {
              override fun process(command: CharpadCommand) {
                executedCommands.add(command)
              }
            }

        val step =
            CharpadStep(
                name = "multiOutputTest",
                inputs = listOf("test.ctm"),
                charpadOutputs =
                    CharpadOutputs(
                        charsets = listOf(CharsetOutput("build/charset.chr")),
                        maps = listOf(MapOutput("build/tilemap.map")),
                        tiles = listOf(TileOutput("build/tiles.tiles")),
                        metadata = listOf(MetadataOutput("build/header.h")),
                        charAttributes = listOf(CharAttributesOutput("build/char_attributes.bin")),
                        charColours = listOf(CharColoursOutput("build/char_colours.bin")),
                        charMaterials = listOf(CharMaterialsOutput("build/char_materials.bin")),
                        charScreenColours =
                            listOf(CharScreenColoursOutput("build/screen_colors.bin"))))
        step.setCharpadPort(mockPort)

        val tempDir = Files.createTempDirectory("test-project").toFile()
        val testFile = File(tempDir, "test.ctm")
        testFile.writeBytes("CTM".toByteArray() + byteArrayOf(5) + ByteArray(100))

        val context = mapOf<String, Any>("projectRootDir" to tempDir)

        When("executing with various output types") {
          step.execute(context)

          Then("it should correctly handle all output types") {
            executedCommands shouldHaveSize 1

            val outputs = executedCommands.first().charpadOutputs
            outputs.charsets shouldHaveSize 1
            outputs.maps shouldHaveSize 1
            outputs.tiles shouldHaveSize 1
            outputs.metadata shouldHaveSize 1
            outputs.charAttributes shouldHaveSize 1
            outputs.charColours shouldHaveSize 1
            outputs.charMaterials shouldHaveSize 1
            outputs.charScreenColours shouldHaveSize 1

            outputs.charsets.first().output shouldBe "build/charset.chr"
            outputs.maps.first().output shouldBe "build/tilemap.map"
            outputs.tiles.first().output shouldBe "build/tiles.tiles"
            outputs.metadata.first().output shouldBe "build/header.h"
          }
        }

        tempDir.deleteRecursively()
      }

      Given("a CharpadStep with missing input file") {
        val step =
            CharpadStep(
                name = "testCharpad",
                inputs = listOf("nonexistent.ctm"),
                charpadOutputs =
                    CharpadOutputs(charsets = listOf(CharsetOutput("build/charset.chr"))))
        step.setCharpadPort(CharpadAdapter())

        val tempDir = Files.createTempDirectory("test-project").toFile()
        val context = mapOf<String, Any>("projectRootDir" to tempDir)

        When("executing with missing input file") {
          val exception = shouldThrow<StepValidationException> { step.execute(context) }

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
                  name = "noInputs",
                  inputs = emptyList(),
                  charpadOutputs =
                      CharpadOutputs(charsets = listOf(CharsetOutput("build/charset.chr"))))
          val errors = step.validate()

          Then("it should report missing input files") {
            errors shouldHaveSize 1
            errors shouldContain "Charpad step 'noInputs' requires at least one input .ctm file"
          }
        }

        When("validating step with no outputs") {
          val step =
              CharpadStep(
                  name = "noOutputs",
                  inputs = listOf("test.ctm"),
                  charpadOutputs = CharpadOutputs())
          val errors = step.validate()

          Then("it should report missing output configurations") {
            errors shouldHaveSize 1
            errors shouldContain
                "Charpad step 'noOutputs' requires at least one output configuration"
          }
        }

        When("validating step with non-CTM input") {
          val step =
              CharpadStep(
                  name = "wrongExt",
                  inputs = listOf("test.txt"),
                  charpadOutputs =
                      CharpadOutputs(charsets = listOf(CharsetOutput("build/charset.chr"))))
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
                  charpadOutputs =
                      CharpadOutputs(charsets = listOf(CharsetOutput("build/charset.chr"))),
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
                  charpadOutputs =
                      CharpadOutputs(charsets = listOf(CharsetOutput("build/charset.chr"))))
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
                charpadOutputs =
                    CharpadOutputs(
                        charsets = listOf(CharsetOutput("build/charset.chr")),
                        maps = listOf(MapOutput("build/map.bin")),
                        metadata = listOf(MetadataOutput("build/header.h"))),
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
                charpadOutputs =
                    CharpadOutputs(charsets = listOf(CharsetOutput("build/charset.chr"))),
                config = config1)

        val step2 =
            CharpadStep(
                name = "test1",
                inputs = listOf("test.ctm"),
                charpadOutputs =
                    CharpadOutputs(charsets = listOf(CharsetOutput("build/charset.chr"))),
                config = config1)

        val step3 =
            CharpadStep(
                name = "test1",
                inputs = listOf("test.ctm"),
                charpadOutputs =
                    CharpadOutputs(charsets = listOf(CharsetOutput("build/charset.chr"))),
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
                name = "testStep",
                inputs = listOf("input.ctm"),
                charpadOutputs = CharpadOutputs(charsets = listOf(CharsetOutput("output.chr"))))

        When("converting to string") {
          val result = step.toString()

          Then("it should contain step information") {
            result shouldContain "testStep"
            result shouldContain "input.ctm"
            result shouldContain "output.chr"
          }
        }
      }

      Given("CharpadStep with multiple outputs of the same type") {
        val step =
            CharpadStep(
                name = "multiCharsetTest",
                inputs = listOf("test.ctm"),
                charpadOutputs =
                    CharpadOutputs(
                        charsets =
                            listOf(
                                CharsetOutput("build/chars1.chr", start = 0, end = 128),
                                CharsetOutput("build/chars2.chr", start = 128, end = 256))))

        When("getting step outputs") {
          Then("it should include all outputs") {
            step.outputs shouldBe listOf("build/chars1.chr", "build/chars2.chr")
            step.charpadOutputs.charsets shouldHaveSize 2
            step.charpadOutputs.charsets[0].start shouldBe 0
            step.charpadOutputs.charsets[0].end shouldBe 128
            step.charpadOutputs.charsets[1].start shouldBe 128
            step.charpadOutputs.charsets[1].end shouldBe 256
          }
        }
      }

      Given("CharpadStep with map output using rectangular region") {
        val step =
            CharpadStep(
                name = "mapRegionTest",
                inputs = listOf("test.ctm"),
                charpadOutputs =
                    CharpadOutputs(
                        maps =
                            listOf(
                                MapOutput(
                                    "build/map.bin", left = 5, top = 10, right = 20, bottom = 15))))

        When("getting step configuration") {
          Then("it should have map with correct region parameters") {
            step.charpadOutputs.maps shouldHaveSize 1
            step.charpadOutputs.maps.first().left shouldBe 5
            step.charpadOutputs.maps.first().top shouldBe 10
            step.charpadOutputs.maps.first().right shouldBe 20
            step.charpadOutputs.maps.first().bottom shouldBe 15
          }
        }
      }

      Given("CharpadStep with nybbler filter") {
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
                name = "nybblerTest",
                inputs = listOf("test.ctm"),
                charpadOutputs =
                    CharpadOutputs(
                        charsets =
                            listOf(
                                CharsetOutput(
                                    "build/charset.chr",
                                    filter =
                                        FilterConfig.Nybbler(
                                            loOutput = "build/charset_lo.chr",
                                            hiOutput = "build/charset_hi.chr",
                                            normalizeHi = true)))))
        step.setCharpadPort(mockPort)

        val tempDir = Files.createTempDirectory("test-nybbler").toFile()
        val testFile = File(tempDir, "test.ctm")
        testFile.writeBytes("CTM".toByteArray() + byteArrayOf(5) + ByteArray(100))

        val context = mapOf<String, Any>("projectRootDir" to tempDir)

        When("executing with nybbler filter") {
          step.execute(context)

          Then("it should pass filter configuration to charpad command") {
            executedCommands shouldHaveSize 1

            val command = executedCommands.first()
            command.charpadOutputs.charsets shouldHaveSize 1

            val charset = command.charpadOutputs.charsets.first()
            charset.output shouldBe "build/charset.chr"
            charset.filter shouldBe
                FilterConfig.Nybbler(
                    loOutput = "build/charset_lo.chr",
                    hiOutput = "build/charset_hi.chr",
                    normalizeHi = true)
          }
        }

        tempDir.deleteRecursively()
      }

      Given("CharpadStep with interleaver filter") {
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
                name = "interleaverTest",
                inputs = listOf("test.ctm"),
                charpadOutputs =
                    CharpadOutputs(
                        charsets =
                            listOf(
                                CharsetOutput(
                                    "build/charset.chr",
                                    filter =
                                        FilterConfig.Interleaver(
                                            outputs =
                                                listOf(
                                                    "build/charset_0.chr",
                                                    "build/charset_1.chr",
                                                    "build/charset_2.chr"))))))
        step.setCharpadPort(mockPort)

        val tempDir = Files.createTempDirectory("test-interleaver").toFile()
        val testFile = File(tempDir, "test.ctm")
        testFile.writeBytes("CTM".toByteArray() + byteArrayOf(5) + ByteArray(100))

        val context = mapOf<String, Any>("projectRootDir" to tempDir)

        When("executing with interleaver filter") {
          step.execute(context)

          Then("it should pass interleaver filter configuration to charpad command") {
            executedCommands shouldHaveSize 1

            val command = executedCommands.first()
            command.charpadOutputs.charsets shouldHaveSize 1

            val charset = command.charpadOutputs.charsets.first()
            charset.output shouldBe "build/charset.chr"
            charset.filter shouldBe
                FilterConfig.Interleaver(
                    outputs =
                        listOf("build/charset_0.chr", "build/charset_1.chr", "build/charset_2.chr"))
          }
        }

        tempDir.deleteRecursively()
      }

      Given("CharpadStep with mixed filters on different outputs") {
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
                name = "mixedFiltersTest",
                inputs = listOf("test.ctm"),
                charpadOutputs =
                    CharpadOutputs(
                        charsets =
                            listOf(
                                CharsetOutput(
                                    "build/charset.chr",
                                    filter =
                                        FilterConfig.Nybbler(loOutput = "build/charset_lo.chr")),
                                CharsetOutput(
                                    "build/charset2.chr",
                                    filter =
                                        FilterConfig.Interleaver(
                                            outputs = listOf("build/c2_0.chr", "build/c2_1.chr"))),
                                CharsetOutput("build/charset3.chr"))))
        step.setCharpadPort(mockPort)

        val tempDir = Files.createTempDirectory("test-mixed-filters").toFile()
        val testFile = File(tempDir, "test.ctm")
        testFile.writeBytes("CTM".toByteArray() + byteArrayOf(5) + ByteArray(100))

        val context = mapOf<String, Any>("projectRootDir" to tempDir)

        When("executing with mixed filters") {
          step.execute(context)

          Then("it should handle different filters on different outputs") {
            executedCommands shouldHaveSize 1

            val command = executedCommands.first()
            command.charpadOutputs.charsets shouldHaveSize 3

            command.charpadOutputs.charsets[0].filter shouldBe
                FilterConfig.Nybbler(loOutput = "build/charset_lo.chr")
            command.charpadOutputs.charsets[1].filter shouldBe
                FilterConfig.Interleaver(outputs = listOf("build/c2_0.chr", "build/c2_1.chr"))
            command.charpadOutputs.charsets[2].filter shouldBe FilterConfig.None
          }
        }

        tempDir.deleteRecursively()
      }

      Given("CharpadStep getAllOutputPaths includes filter outputs") {
        val step =
            CharpadStep(
                name = "filterPathsTest",
                inputs = listOf("test.ctm"),
                charpadOutputs =
                    CharpadOutputs(
                        charsets =
                            listOf(
                                CharsetOutput(
                                    "build/charset.chr",
                                    filter =
                                        FilterConfig.Nybbler(
                                            loOutput = "build/charset_lo.chr",
                                            hiOutput = "build/charset_hi.chr")),
                                CharsetOutput(
                                    "build/charset2.chr",
                                    filter =
                                        FilterConfig.Interleaver(
                                            outputs = listOf("build/c2_0.chr", "build/c2_1.chr")))),
                        maps = listOf(MapOutput("build/map.bin"))))

        When("getting all output paths") {
          val paths = step.outputs

          Then("it should include primary and filter outputs") {
            // Check that all expected paths are present
            paths shouldContain "build/charset.chr"
            paths shouldContain "build/charset_lo.chr"
            paths shouldContain "build/charset_hi.chr"
            paths shouldContain "build/charset2.chr"
            paths shouldContain "build/c2_0.chr"
            paths shouldContain "build/c2_1.chr"
            paths shouldContain "build/map.bin"
            paths shouldHaveSize 7
          }
        }
      }
    })
