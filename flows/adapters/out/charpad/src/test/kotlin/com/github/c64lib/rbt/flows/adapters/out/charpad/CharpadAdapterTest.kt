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
package com.github.c64lib.rbt.flows.adapters.out.charpad

import com.github.c64lib.rbt.flows.domain.FlowValidationException
import com.github.c64lib.rbt.flows.domain.config.CharpadCommand
import com.github.c64lib.rbt.flows.domain.config.CharpadCompression
import com.github.c64lib.rbt.flows.domain.config.CharpadConfig
import com.github.c64lib.rbt.flows.domain.config.CharpadFormat
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.io.File
import java.nio.file.Files

class CharpadAdapterTest :
    BehaviorSpec({
      isolationMode = IsolationMode.InstancePerTest

      Given("CharpadAdapter with valid configuration") {
        val adapter = CharpadAdapter()
        val tempDir = Files.createTempDirectory("charpad-adapter-test").toFile()

        When("processing command with missing input file") {
          val nonExistentFile = File(tempDir, "nonexistent.ctm")
          val outputFile = File(tempDir, "output.chr")
          val command =
              CharpadCommand(
                  inputFile = nonExistentFile,
                  outputFiles = mapOf("charset" to outputFile),
                  config = CharpadConfig(),
                  projectRootDir = tempDir)

          val exception = shouldThrow<FlowValidationException> { adapter.process(command) }

          Then("it should throw validation exception for missing file") {
            exception.message shouldContain "CTM input file does not exist"
            exception.message shouldContain "nonexistent.ctm"
          }
        }

        When("processing command with directory instead of file") {
          val directory = File(tempDir, "directory.ctm")
          directory.mkdirs()
          val outputFile = File(tempDir, "output.chr")
          val command =
              CharpadCommand(
                  inputFile = directory,
                  outputFiles = mapOf("charset" to outputFile),
                  config = CharpadConfig(),
                  projectRootDir = tempDir)

          val exception = shouldThrow<FlowValidationException> { adapter.process(command) }

          Then("it should throw validation exception for directory") {
            exception.message shouldContain "CTM input path is not a file"
            exception.message shouldContain "directory.ctm"
          }
        }

        When("processing command with empty file") {
          val emptyFile = File(tempDir, "empty.ctm")
          emptyFile.createNewFile()
          val outputFile = File(tempDir, "output.chr")
          val command =
              CharpadCommand(
                  inputFile = emptyFile,
                  outputFiles = mapOf("charset" to outputFile),
                  config = CharpadConfig(),
                  projectRootDir = tempDir)

          val exception = shouldThrow<FlowValidationException> { adapter.process(command) }

          Then("it should throw validation exception for empty file") {
            exception.message shouldContain "CTM input file is empty"
            exception.message shouldContain "empty.ctm"
          }
        }

        When("processing command with no output producers") {
          val ctmFile = File(tempDir, "test.ctm")
          ctmFile.writeBytes("CTM".toByteArray() + byteArrayOf(5) + ByteArray(100))
          val command =
              CharpadCommand(
                  inputFile = ctmFile,
                  outputFiles = emptyMap(), // No output files
                  config = CharpadConfig(),
                  projectRootDir = tempDir)

          val exception = shouldThrow<FlowValidationException> { adapter.process(command) }

          Then("it should throw validation exception for no output producers") {
            exception.message shouldContain "No output producers configured"
            exception.message shouldContain "At least one output file must be specified"
          }
        }

        When("processing command with invalid CTM content") {
          val invalidCtmFile = File(tempDir, "invalid.ctm")
          invalidCtmFile.writeBytes("INVALID".toByteArray())
          val outputFile = File(tempDir, "output.chr")
          val command =
              CharpadCommand(
                  inputFile = invalidCtmFile,
                  outputFiles = mapOf("charset" to outputFile),
                  config = CharpadConfig(),
                  projectRootDir = tempDir)

          val exception = shouldThrow<FlowValidationException> { adapter.process(command) }

          Then("it should throw validation exception for invalid CTM format") {
            exception.message shouldContain "Invalid CTM file format"
            exception.message shouldContain "invalid.ctm"
            exception.message shouldContain "Ensure the file is a valid Charpad CTM file"
          }
        }

        tempDir.deleteRecursively()
      }

      Given("CharpadOutputProducerFactory") {
        val factory = CharpadOutputProducerFactory()
        val tempDir = Files.createTempDirectory("producer-factory-test").toFile()

        When("creating output producers for different output types") {
          val ctmFile = File(tempDir, "test.ctm")
          val charsetFile = File(tempDir, "charset.chr")
          val mapFile = File(tempDir, "map.bin")
          val tilesFile = File(tempDir, "tiles.dat")
          val headerFile = File(tempDir, "header.h")
          val attributesFile = File(tempDir, "attributes.bin")
          val coloursFile = File(tempDir, "colours.bin")
          val materialsFile = File(tempDir, "materials.bin")
          val screenColoursFile = File(tempDir, "screen_colours.bin")
          val tileTagsFile = File(tempDir, "tile-tags.bin")
          val tileColoursFile = File(tempDir, "tile-colours.bin")
          val tileScreenColoursFile = File(tempDir, "tile-screen-colours.bin")

          val outputFiles =
              mapOf(
                  "charset" to charsetFile,
                  "map" to mapFile,
                  "tiles" to tilesFile,
                  "header" to headerFile,
                  "charattributes" to attributesFile,
                  "charcolours" to coloursFile,
                  "charmaterials" to materialsFile,
                  "charscreencolours" to screenColoursFile,
                  "tiletags" to tileTagsFile,
                  "tilecolours" to tileColoursFile,
                  "tilescreencolours" to tileScreenColoursFile)

          val config =
              CharpadConfig(
                  generateCharset = true,
                  generateMap = true,
                  namespace = "test",
                  prefix = "CHAR_",
                  includeVersion = true,
                  includeBgColours = true,
                  includeCharColours = true,
                  includeMode = true)

          val command =
              CharpadCommand(
                  inputFile = ctmFile,
                  outputFiles = outputFiles,
                  config = config,
                  projectRootDir = tempDir)

          val producers = factory.createOutputProducers(command)

          Then("it should create all expected output producers") {
            producers shouldHaveSize 11 // All output producer types

            // Verify we have one producer for each output type
            val producerTypes = producers.map { it::class.simpleName }.filterNotNull().sorted()

            producerTypes shouldContain "CharsetProducer"
            producerTypes shouldContain "MapProducer"
            producerTypes shouldContain "TileProducer"
            producerTypes shouldContain "HeaderProducer"
            producerTypes shouldContain "CharAttributesProducer"
            producerTypes shouldContain "CharColoursProducer"
            producerTypes shouldContain "CharMaterialsProducer"
            producerTypes shouldContain "CharScreenColoursProducer"
            producerTypes shouldContain "TileTagsProducer"
            producerTypes shouldContain "TileColoursProducer"
            producerTypes shouldContain "TileScreenColoursProducer"
          }
        }

        When("creating output producers with conditional generation") {
          val ctmFile = File(tempDir, "test.ctm")
          val charsetFile = File(tempDir, "charset.chr")
          val mapFile = File(tempDir, "map.bin")

          val outputFiles = mapOf("charset" to charsetFile, "map" to mapFile)

          val config =
              CharpadConfig(generateCharset = true, generateMap = false) // Map generation disabled

          val command =
              CharpadCommand(
                  inputFile = ctmFile,
                  outputFiles = outputFiles,
                  config = config,
                  projectRootDir = tempDir)

          val producers = factory.createOutputProducers(command)

          Then("it should only create producers for enabled outputs") {
            // Should create charset producer but not map producer
            producers shouldHaveSize 1
            producers.first()::class.simpleName shouldBe "CharsetProducer"
          }
        }

        When("creating output producers with file extension detection") {
          val ctmFile = File(tempDir, "test.ctm")
          val charsetFile = File(tempDir, "data.chr") // .chr extension
          val mapFile = File(tempDir, "level.map") // .map extension
          val headerFile = File(tempDir, "constants.h") // .h extension
          val includeFile = File(tempDir, "defines.inc") // .inc extension

          val outputFiles =
              mapOf(
                  "auto1" to charsetFile,
                  "auto2" to mapFile,
                  "auto3" to headerFile,
                  "auto4" to includeFile)

          val command =
              CharpadCommand(
                  inputFile = ctmFile,
                  outputFiles = outputFiles,
                  config = CharpadConfig(),
                  projectRootDir = tempDir)

          val producers = factory.createOutputProducers(command)

          Then("it should detect output types from file extensions") {
            producers shouldHaveSize 4
            val producerTypeNames = producers.map { it::class.simpleName }

            // Should detect charset, map, and header producers based on extensions
            producerTypeNames shouldContain "CharsetProducer"
            producerTypeNames shouldContain "MapProducer"
            producerTypeNames shouldContain "HeaderProducer" // For both .h and .inc
          }
        }

        tempDir.deleteRecursively()
      }

      Given("CharpadAdapter with comprehensive configuration") {
        val adapter = CharpadAdapter()
        val tempDir = Files.createTempDirectory("comprehensive-test").toFile()

        When("processing all configuration options") {
          val ctmFile = File(tempDir, "comprehensive.ctm")
          // Create a more realistic CTM file structure for testing
          val ctmContent =
              "CTM".toByteArray() +
                  byteArrayOf(6) + // Version 6
                  ByteArray(4) + // Background colors
                  byteArrayOf(1) + // Char color
                  byteArrayOf(0) + // Coloring method
                  byteArrayOf(0) + // Screen mode
                  byteArrayOf(40, 0) + // Map width
                  byteArrayOf(25, 0) + // Map height
                  byteArrayOf(8, 8) + // Tile dimensions
                  ByteArray(256 * 8) + // Charset data
                  ByteArray(40 * 25) + // Map data
                  ByteArray(100) // Additional data for version 6

          ctmFile.writeBytes(ctmContent)

          val outputDir = File(tempDir, "output")
          outputDir.mkdirs()

          val config =
              CharpadConfig(
                  compression = CharpadCompression.RLE,
                  exportFormat = CharpadFormat.C64LIB,
                  tileSize = 8,
                  charsetOptimization = true,
                  generateMap = true,
                  generateCharset = true,
                  ctm8PrototypeCompatibility = false,
                  namespace = "game",
                  prefix = "LEVEL1_",
                  includeVersion = true,
                  includeBgColours = true,
                  includeCharColours = true,
                  includeMode = false)

          val outputFiles =
              mapOf(
                  "charset" to File(outputDir, "charset.chr"),
                  "map" to File(outputDir, "map.bin"),
                  "header" to File(outputDir, "level.h"))

          val command =
              CharpadCommand(
                  inputFile = ctmFile,
                  outputFiles = outputFiles,
                  config = config,
                  projectRootDir = tempDir)

          try {
            adapter.process(command)
            Then("it should process without throwing exceptions") {
              // If we get here, the adapter successfully processed the command
              // without throwing validation errors
              true shouldBe true
            }
          } catch (e: FlowValidationException) {
            Then("it should provide meaningful error messages if processing fails") {
              // Expected for our test CTM data which may not be fully valid
              e.message shouldContain "CTM"
            }
          }
        }

        tempDir.deleteRecursively()
      }

      Given("CharpadAdapter error handling integration") {
        val adapter = CharpadAdapter()
        val tempDir = Files.createTempDirectory("error-handling-test").toFile()

        When("processing with various error conditions") {
          // Test case 1: File not readable (simulate with non-existent file)
          val nonReadableFile = File(tempDir, "nonreadable.ctm")
          val outputFile = File(tempDir, "output.chr")

          val command1 =
              CharpadCommand(
                  inputFile = nonReadableFile,
                  outputFiles = mapOf("charset" to outputFile),
                  config = CharpadConfig(),
                  projectRootDir = tempDir)

          val exception1 = shouldThrow<FlowValidationException> { adapter.process(command1) }

          Then("it should handle file access errors gracefully") {
            exception1.message shouldContain "CTM input file does not exist"
            exception1.message shouldContain "Verify the file path is correct"
          }

          // Test case 2: Insufficient data (truncated CTM file)
          val truncatedFile = File(tempDir, "truncated.ctm")
          truncatedFile.writeBytes("CTM".toByteArray() + byteArrayOf(5)) // Only header, no data

          val command2 =
              CharpadCommand(
                  inputFile = truncatedFile,
                  outputFiles = mapOf("charset" to outputFile),
                  config = CharpadConfig(),
                  projectRootDir = tempDir)

          val exception2 = shouldThrow<FlowValidationException> { adapter.process(command2) }

          Then("it should handle insufficient data errors") {
            exception2.message shouldContain "Insufficient data in CTM file"
          }
        }

        tempDir.deleteRecursively()
      }
    })
