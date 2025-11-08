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
package com.github.c64lib.rbt.flows.adapters.`in`.gradle

import com.github.c64lib.rbt.flows.adapters.out.charpad.CharpadAdapter
import com.github.c64lib.rbt.flows.domain.config.*
import com.github.c64lib.rbt.flows.domain.steps.CharpadStep
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files

/**
 * Integration tests for Charpad processing with nybbler and interleaver filters.
 *
 * These tests exercise the full charpad pipeline including filter application with real CharPad CTM
 * files.
 */
class CharpadIntegrationTest :
    BehaviorSpec({
      isolationMode = IsolationMode.InstancePerTest

      Given("CharpadStep with nybbler filter integration test") {
        val tempDir = Files.createTempDirectory("charpad-nybbler-integration").toFile()
        val buildDir = File(tempDir, "build")
        buildDir.mkdirs()

        // Copy real CTM test file
        val ctmFile = File(tempDir, "test.ctm")
        val ctmResourceStream =
            this.javaClass.getResourceAsStream("/test-integration.ctm")
                ?: throw IllegalStateException("CTM test resource file not found")

        ctmResourceStream.use { input ->
          ctmFile.outputStream().use { output -> input.copyTo(output) }
        }

        val step =
            CharpadStep(
                name = "nybblerIntegrationTest",
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

        step.setCharpadPort(CharpadAdapter())
        val context = mapOf<String, Any>("projectRootDir" to tempDir)

        When("verifying nybbler filter configuration") {
          Then("the step should be correctly configured with nybbler filter") {
            step.charpadOutputs.charsets shouldHaveSize 1
            val charset = step.charpadOutputs.charsets.first()

            charset.output shouldBe "build/charset.chr"
            charset.filter shouldBe
                FilterConfig.Nybbler(
                    loOutput = "build/charset_lo.chr",
                    hiOutput = "build/charset_hi.chr",
                    normalizeHi = true)
          }

          And("all output paths including filter outputs should be in step outputs") {
            step.outputs shouldContain "build/charset.chr"
            step.outputs shouldContain "build/charset_lo.chr"
            step.outputs shouldContain "build/charset_hi.chr"
            step.outputs shouldHaveSize 3
          }

          And("filter type should be correct") {
            val filter = step.charpadOutputs.charsets.first().filter
            (filter is FilterConfig.Nybbler) shouldBe true
            (filter as FilterConfig.Nybbler).normalizeHi shouldBe true
          }
        }

        tempDir.deleteRecursively()
      }

      Given("CharpadStep with interleaver filter integration test") {
        val tempDir = Files.createTempDirectory("charpad-interleaver-integration").toFile()
        val buildDir = File(tempDir, "build")
        buildDir.mkdirs()

        // Copy real CTM test file
        val ctmFile = File(tempDir, "test.ctm")
        val ctmResourceStream =
            this.javaClass.getResourceAsStream("/test-integration.ctm")
                ?: throw IllegalStateException("CTM test resource file not found")

        ctmResourceStream.use { input ->
          ctmFile.outputStream().use { output -> input.copyTo(output) }
        }

        val step =
            CharpadStep(
                name = "interleaverIntegrationTest",
                inputs = listOf("test.ctm"),
                charpadOutputs =
                    CharpadOutputs(
                        tiles =
                            listOf(
                                TileOutput(
                                    "build/tiles.bin",
                                    filter =
                                        FilterConfig.Interleaver(
                                            outputs =
                                                listOf(
                                                    "build/tiles_0.bin", "build/tiles_1.bin"))))))

        step.setCharpadPort(CharpadAdapter())
        val context = mapOf<String, Any>("projectRootDir" to tempDir)

        When("verifying interleaver filter configuration") {
          Then("the step should be correctly configured with interleaver filter") {
            step.charpadOutputs.tiles shouldHaveSize 1
            val tile = step.charpadOutputs.tiles.first()

            tile.output shouldBe "build/tiles.bin"
            tile.filter shouldBe
                FilterConfig.Interleaver(outputs = listOf("build/tiles_0.bin", "build/tiles_1.bin"))
          }

          And("all output paths including interleaver outputs should be in step outputs") {
            step.outputs shouldContain "build/tiles.bin"
            step.outputs shouldContain "build/tiles_0.bin"
            step.outputs shouldContain "build/tiles_1.bin"
            step.outputs shouldHaveSize 3
          }

          And("filter should distribute outputs correctly") {
            val filter = step.charpadOutputs.tiles.first().filter
            (filter is FilterConfig.Interleaver) shouldBe true
            (filter as FilterConfig.Interleaver).outputs shouldHaveSize 2
          }
        }

        tempDir.deleteRecursively()
      }

      Given("CharpadStep with mixed filters on different outputs") {
        val tempDir = Files.createTempDirectory("charpad-mixed-filters").toFile()
        val buildDir = File(tempDir, "build")
        buildDir.mkdirs()

        val ctmFile = File(tempDir, "test.ctm")
        val ctmResourceStream =
            this.javaClass.getResourceAsStream("/test-integration.ctm")
                ?: throw IllegalStateException("CTM test resource file not found")

        ctmResourceStream.use { input ->
          ctmFile.outputStream().use { output -> input.copyTo(output) }
        }

        val step =
            CharpadStep(
                name = "mixedFiltersIntegration",
                inputs = listOf("test.ctm"),
                charpadOutputs =
                    CharpadOutputs(
                        charsets =
                            listOf(
                                CharsetOutput(
                                    "build/charset.chr",
                                    filter =
                                        FilterConfig.Nybbler(loOutput = "build/charset_lo.chr"))),
                        tiles =
                            listOf(
                                TileOutput(
                                    "build/tiles.bin",
                                    filter =
                                        FilterConfig.Interleaver(
                                            outputs =
                                                listOf("build/tiles_0.bin", "build/tiles_1.bin")))),
                        maps = listOf(MapOutput("build/map.bin"))))

        step.setCharpadPort(CharpadAdapter())
        val context = mapOf<String, Any>("projectRootDir" to tempDir)

        When("verifying mixed filter configuration") {
          Then("the step should be correctly configured with different filters") {
            step.charpadOutputs.charsets shouldHaveSize 1
            step.charpadOutputs.tiles shouldHaveSize 1
            step.charpadOutputs.maps shouldHaveSize 1

            val charset = step.charpadOutputs.charsets.first()
            (charset.filter is FilterConfig.Nybbler) shouldBe true

            val tile = step.charpadOutputs.tiles.first()
            (tile.filter is FilterConfig.Interleaver) shouldBe true

            val map = step.charpadOutputs.maps.first()
            map.filter shouldBe FilterConfig.None
          }

          And("all primary and filter outputs should be included in outputs") {
            step.outputs shouldContain "build/charset.chr"
            step.outputs shouldContain "build/charset_lo.chr"
            step.outputs shouldContain "build/tiles.bin"
            step.outputs shouldContain "build/tiles_0.bin"
            step.outputs shouldContain "build/tiles_1.bin"
            step.outputs shouldContain "build/map.bin"
            step.outputs shouldHaveSize 6
          }

          And("filter types should be correctly applied") {
            val charsetFilter = step.charpadOutputs.charsets.first().filter
            (charsetFilter as? FilterConfig.Nybbler)?.loOutput shouldBe "build/charset_lo.chr"

            val tileFilter = step.charpadOutputs.tiles.first().filter
            (tileFilter as? FilterConfig.Interleaver)?.outputs?.size shouldBe 2
          }
        }

        tempDir.deleteRecursively()
      }

      Given("CharpadStep with partial nybbler configuration (lo only)") {
        val tempDir = Files.createTempDirectory("charpad-nybbler-lo-only").toFile()
        val buildDir = File(tempDir, "build")
        buildDir.mkdirs()

        val ctmFile = File(tempDir, "test.ctm")
        val ctmResourceStream =
            this.javaClass.getResourceAsStream("/test-integration.ctm")
                ?: throw IllegalStateException("CTM test resource file not found")

        ctmResourceStream.use { input ->
          ctmFile.outputStream().use { output -> input.copyTo(output) }
        }

        val step =
            CharpadStep(
                name = "nybblerLoOnlyTest",
                inputs = listOf("test.ctm"),
                charpadOutputs =
                    CharpadOutputs(
                        charsets =
                            listOf(
                                CharsetOutput(
                                    "build/charset.chr",
                                    filter =
                                        FilterConfig.Nybbler(loOutput = "build/charset_lo.chr")))))

        step.setCharpadPort(CharpadAdapter())
        val context = mapOf<String, Any>("projectRootDir" to tempDir)

        When("verifying partial nybbler configuration") {
          Then("the configuration should correctly represent lo-only nybbler") {
            val charset = step.charpadOutputs.charsets.first()

            charset.output shouldBe "build/charset.chr"
            charset.filter shouldBe FilterConfig.Nybbler(loOutput = "build/charset_lo.chr")
          }

          And("only primary and lo output should be in getAllOutputPaths") {
            step.outputs shouldContain "build/charset.chr"
            step.outputs shouldContain "build/charset_lo.chr"
            step.outputs shouldHaveSize 2
          }

          And("hi output should NOT be in the outputs") {
            step.outputs.contains("build/charset_hi.chr") shouldBe false
          }

          And("nybbler filter should have only loOutput set") {
            val filter = step.charpadOutputs.charsets.first().filter
            (filter as FilterConfig.Nybbler).loOutput shouldBe "build/charset_lo.chr"
            (filter as FilterConfig.Nybbler).hiOutput shouldBe null
          }
        }

        tempDir.deleteRecursively()
      }

      Given("CharpadStep flow dependency resolution with filters") {
        val step =
            CharpadStep(
                name = "dependencyTest",
                inputs = listOf("test.ctm"),
                charpadOutputs =
                    CharpadOutputs(
                        charsets =
                            listOf(
                                CharsetOutput(
                                    "build/charset1.chr",
                                    filter =
                                        FilterConfig.Nybbler(
                                            loOutput = "build/charset1_lo.chr",
                                            hiOutput = "build/charset1_hi.chr")),
                                CharsetOutput(
                                    "build/charset2.chr",
                                    filter =
                                        FilterConfig.Interleaver(
                                            outputs =
                                                listOf(
                                                    "build/charset2_0.chr",
                                                    "build/charset2_1.chr",
                                                    "build/charset2_2.chr")))),
                        charAttributes =
                            listOf(
                                CharAttributesOutput(
                                    "build/attributes.bin",
                                    filter =
                                        FilterConfig.Nybbler(
                                            hiOutput = "build/attributes_hi.bin")))))

        When("getting all output paths") {
          val allPaths = step.outputs

          Then("all primary outputs should be included") {
            allPaths shouldContain "build/charset1.chr"
            allPaths shouldContain "build/charset2.chr"
            allPaths shouldContain "build/attributes.bin"
          }

          And("all nybbler filter outputs should be included") {
            allPaths shouldContain "build/charset1_lo.chr"
            allPaths shouldContain "build/charset1_hi.chr"
            allPaths shouldContain "build/attributes_hi.bin"
          }

          And("all interleaver filter outputs should be included") {
            allPaths shouldContain "build/charset2_0.chr"
            allPaths shouldContain "build/charset2_1.chr"
            allPaths shouldContain "build/charset2_2.chr"
          }

          And("total count should match all outputs") {
            // 3 primary + 2 charset1 nybbler + 3 charset2 interleaver + 1 attributes nybbler = 9
            allPaths shouldHaveSize 9
          }
        }
      }
    })
