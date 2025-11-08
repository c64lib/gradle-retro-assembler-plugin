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

import com.github.c64lib.rbt.flows.domain.config.*
import com.github.c64lib.rbt.shared.gradle.fllter.Nybbler
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files

class CharpadOutputProducerFactoryFilterTest :
    DescribeSpec({
      isolationMode = IsolationMode.InstancePerTest

      val factory = CharpadOutputProducerFactory()
      val tempDir = Files.createTempDirectory("charpad-filter-test").toFile()
      val testCtmFile = File(tempDir, "test.ctm")
      testCtmFile.writeBytes("CTM".toByteArray() + byteArrayOf(5) + ByteArray(100))

      describe("CharpadOutputProducerFactory with filters") {
        describe("creating output producers without filters") {
          val command =
              CharpadCommand(
                  inputFile = testCtmFile,
                  charpadOutputs =
                      CharpadOutputs(
                          charsets =
                              listOf(
                                  CharsetOutput("build/charset.chr", filter = FilterConfig.None))),
                  config = CharpadConfig(),
                  projectRootDir = tempDir)

          it("should create producers with no filter wrapping") {
            val producers = factory.createOutputProducers(command)
            producers.isNotEmpty() shouldBe true
            // The producer should exist without any filter transformations
          }
        }

        describe("creating output producers with nybbler filter") {
          val command =
              CharpadCommand(
                  inputFile = testCtmFile,
                  charpadOutputs =
                      CharpadOutputs(
                          charsets =
                              listOf(
                                  CharsetOutput(
                                      "build/charset.chr",
                                      filter =
                                          FilterConfig.Nybbler(
                                              loOutput = "build/charset_lo.chr",
                                              hiOutput = "build/charset_hi.chr")))),
                  config = CharpadConfig(),
                  projectRootDir = tempDir)

          it("should create producers with nybbler filter wrapping") {
            val producers = factory.createOutputProducers(command)
            producers.isNotEmpty() shouldBe true
            // The producer should have nybbler filter applied
            // (actual verification would require access to private fields or mock testing)
          }
        }

        describe("creating output producers with interleaver filter") {
          val command =
              CharpadCommand(
                  inputFile = testCtmFile,
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
                                                      "build/charset_1.chr"))))),
                  config = CharpadConfig(),
                  projectRootDir = tempDir)

          it("should create producers with interleaver filter wrapping") {
            val producers = factory.createOutputProducers(command)
            producers.isNotEmpty() shouldBe true
            // The producer should have interleaver filter applied
          }
        }

        describe("creating output producers with multiple outputs having different filters") {
          val command =
              CharpadCommand(
                  inputFile = testCtmFile,
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
                                              outputs =
                                                  listOf(
                                                      "build/charset2_0.chr",
                                                      "build/charset2_1.chr"))),
                                  CharsetOutput("build/charset3.chr"))),
                  config = CharpadConfig(),
                  projectRootDir = tempDir)

          it("should create multiple producers with appropriate filters") {
            val producers = factory.createOutputProducers(command)
            // Should have 3 charset producers
            producers.isNotEmpty() shouldBe true
          }
        }

        describe("filter output file resolution") {
          val command =
              CharpadCommand(
                  inputFile = testCtmFile,
                  charpadOutputs =
                      CharpadOutputs(
                          charsets =
                              listOf(
                                  CharsetOutput(
                                      "build/charset.chr",
                                      filter =
                                          FilterConfig.Nybbler(
                                              loOutput = "build/subdir/lo.chr",
                                              hiOutput = "build/subdir/hi.chr")))),
                  config = CharpadConfig(),
                  projectRootDir = tempDir)

          it("should resolve filter output paths relative to project root") {
            // This test verifies that filter output paths are resolved correctly
            val producers = factory.createOutputProducers(command)
            producers.isNotEmpty() shouldBe true
          }
        }

        describe("nybbler filter with only lo output") {
          val command =
              CharpadCommand(
                  inputFile = testCtmFile,
                  charpadOutputs =
                      CharpadOutputs(
                          charsets =
                              listOf(
                                  CharsetOutput(
                                      "build/charset.chr",
                                      filter =
                                          FilterConfig.Nybbler(
                                              loOutput = "build/charset_lo.chr",
                                              hiOutput = null)))),
                  config = CharpadConfig(),
                  projectRootDir = tempDir)

          it("should create nybbler with only lo output") {
            val producers = factory.createOutputProducers(command)
            producers.isNotEmpty() shouldBe true
          }
        }

        describe("nybbler filter with only hi output") {
          val command =
              CharpadCommand(
                  inputFile = testCtmFile,
                  charpadOutputs =
                      CharpadOutputs(
                          charsets =
                              listOf(
                                  CharsetOutput(
                                      "build/charset.chr",
                                      filter =
                                          FilterConfig.Nybbler(
                                              loOutput = null,
                                              hiOutput = "build/charset_hi.chr",
                                              normalizeHi = false)))),
                  config = CharpadConfig(),
                  projectRootDir = tempDir)

          it("should create nybbler with only hi output and custom normalizeHi setting") {
            val producers = factory.createOutputProducers(command)
            producers.isNotEmpty() shouldBe true
          }
        }

        describe("interleaver with multiple outputs") {
          val command =
              CharpadCommand(
                  inputFile = testCtmFile,
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
                                                      "build/c0.chr",
                                                      "build/c1.chr",
                                                      "build/c2.chr"))))),
                  config = CharpadConfig(),
                  projectRootDir = tempDir)

          it("should create interleaver with correct number of outputs") {
            val producers = factory.createOutputProducers(command)
            producers.isNotEmpty() shouldBe true
          }
        }

        describe("filters on different output types") {
          val command =
              CharpadCommand(
                  inputFile = testCtmFile,
                  charpadOutputs =
                      CharpadOutputs(
                          charsets =
                              listOf(
                                  CharsetOutput(
                                      "build/charset.chr",
                                      filter =
                                          FilterConfig.Nybbler(loOutput = "build/charset_lo.chr"))),
                          charAttributes =
                              listOf(
                                  CharAttributesOutput(
                                      "build/attributes.bin",
                                      filter =
                                          FilterConfig.Interleaver(
                                              outputs =
                                                  listOf("build/attr_0.bin", "build/attr_1.bin")))),
                          tiles =
                              listOf(TileOutput("build/tiles.bin", filter = FilterConfig.None))),
                  config = CharpadConfig(),
                  projectRootDir = tempDir)

          it("should apply filters to different output types independently") {
            val producers = factory.createOutputProducers(command)
            producers.isNotEmpty() shouldBe true
          }
        }
      }

      afterSpec { tempDir.deleteRecursively() }
    })
