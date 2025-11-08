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
package com.github.c64lib.rbt.flows.domain.config

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class CharpadOutputsFilterTest :
    DescribeSpec({
      describe("CharpadOutputs with filters") {
        describe("getAllOutputPaths with no filters") {
          val outputs =
              CharpadOutputs(
                  charsets = listOf(CharsetOutput("charset.chr")),
                  maps = listOf(MapOutput("map.bin")))

          it("should return only primary output paths") {
            val paths = outputs.getAllOutputPaths()
            paths shouldContainExactlyInAnyOrder listOf("charset.chr", "map.bin")
            paths shouldHaveSize 2
          }
        }

        describe("getAllOutputPaths with nybbler filter on charset") {
          val outputs =
              CharpadOutputs(
                  charsets =
                      listOf(
                          CharsetOutput(
                              "charset.chr",
                              filter =
                                  FilterConfig.Nybbler(
                                      loOutput = "charset_lo.chr", hiOutput = "charset_hi.chr"))),
                  maps = listOf(MapOutput("map.bin")))

          it("should include primary output and filter outputs") {
            val paths = outputs.getAllOutputPaths()
            paths shouldContainExactlyInAnyOrder
                listOf("charset.chr", "charset_lo.chr", "charset_hi.chr", "map.bin")
            paths shouldHaveSize 4
          }
        }

        describe("getAllOutputPaths with nybbler filter (lo only)") {
          val outputs =
              CharpadOutputs(
                  charsets =
                      listOf(
                          CharsetOutput(
                              "charset.chr",
                              filter = FilterConfig.Nybbler(loOutput = "charset_lo.chr"))))

          it("should include primary output and lo output only") {
            val paths = outputs.getAllOutputPaths()
            paths shouldContainExactlyInAnyOrder listOf("charset.chr", "charset_lo.chr")
            paths shouldHaveSize 2
          }
        }

        describe("getAllOutputPaths with nybbler filter (hi only)") {
          val outputs =
              CharpadOutputs(
                  charsets =
                      listOf(
                          CharsetOutput(
                              "charset.chr",
                              filter = FilterConfig.Nybbler(hiOutput = "charset_hi.chr"))))

          it("should include primary output and hi output only") {
            val paths = outputs.getAllOutputPaths()
            paths shouldContainExactlyInAnyOrder listOf("charset.chr", "charset_hi.chr")
            paths shouldHaveSize 2
          }
        }

        describe("getAllOutputPaths with interleaver filter") {
          val outputs =
              CharpadOutputs(
                  charsets =
                      listOf(
                          CharsetOutput(
                              "charset.chr",
                              filter =
                                  FilterConfig.Interleaver(
                                      outputs =
                                          listOf(
                                              "charset_0.chr", "charset_1.chr", "charset_2.chr")))))

          it("should include primary output and all interleaver outputs") {
            val paths = outputs.getAllOutputPaths()
            paths shouldContainExactlyInAnyOrder
                listOf("charset.chr", "charset_0.chr", "charset_1.chr", "charset_2.chr")
            paths shouldHaveSize 4
          }
        }

        describe("getAllOutputPaths with multiple charsets with filters") {
          val outputs =
              CharpadOutputs(
                  charsets =
                      listOf(
                          CharsetOutput(
                              "charset1.chr",
                              filter =
                                  FilterConfig.Nybbler(
                                      loOutput = "charset1_lo.chr", hiOutput = "charset1_hi.chr")),
                          CharsetOutput(
                              "charset2.chr",
                              filter =
                                  FilterConfig.Interleaver(
                                      outputs = listOf("c2_0.chr", "c2_1.chr")))))

          it("should include all primary and filter outputs") {
            val paths = outputs.getAllOutputPaths()
            paths shouldContainExactlyInAnyOrder
                listOf(
                    "charset1.chr",
                    "charset1_lo.chr",
                    "charset1_hi.chr",
                    "charset2.chr",
                    "c2_0.chr",
                    "c2_1.chr")
            paths shouldHaveSize 6
          }
        }

        describe("getAllOutputPaths with mixed output types and filters") {
          val outputs =
              CharpadOutputs(
                  charsets =
                      listOf(
                          CharsetOutput(
                              "charset.chr",
                              filter = FilterConfig.Nybbler(loOutput = "charset_lo.chr"))),
                  charAttributes =
                      listOf(
                          CharAttributesOutput(
                              "attributes.bin",
                              filter =
                                  FilterConfig.Interleaver(
                                      outputs = listOf("attr_0.bin", "attr_1.bin")))),
                  maps = listOf(MapOutput("map.bin")))

          it("should include outputs from all types") {
            val paths = outputs.getAllOutputPaths()
            paths shouldContainExactlyInAnyOrder
                listOf(
                    "charset.chr",
                    "charset_lo.chr",
                    "attributes.bin",
                    "attr_0.bin",
                    "attr_1.bin",
                    "map.bin")
            paths shouldHaveSize 6
          }
        }

        describe("hasOutputs with filters") {
          it("should return true when primary outputs exist") {
            val outputs = CharpadOutputs(charsets = listOf(CharsetOutput("charset.chr")))
            outputs.hasOutputs() shouldBe true
          }

          it("should return true when filter outputs exist") {
            val outputs =
                CharpadOutputs(
                    charsets =
                        listOf(
                            CharsetOutput(
                                "charset.chr",
                                filter = FilterConfig.Nybbler(loOutput = "charset_lo.chr"))))
            outputs.hasOutputs() shouldBe true
          }

          it("should return false when no outputs exist") {
            val outputs = CharpadOutputs()
            outputs.hasOutputs() shouldBe false
          }
        }

        describe("CharsetOutput with different filter types") {
          it("should support None filter (default)") {
            val output = CharsetOutput("charset.chr")
            output.filter shouldBe FilterConfig.None
          }

          it("should support Nybbler filter") {
            val filter = FilterConfig.Nybbler(loOutput = "lo.chr", hiOutput = "hi.chr")
            val output = CharsetOutput("charset.chr", filter = filter)
            output.filter shouldBe filter
          }

          it("should support Interleaver filter") {
            val filter = FilterConfig.Interleaver(outputs = listOf("o1.chr", "o2.chr"))
            val output = CharsetOutput("charset.chr", filter = filter)
            output.filter shouldBe filter
          }
        }

        describe("RangeOutput implementations with filters") {
          val filterConfig = FilterConfig.Nybbler(loOutput = "filtered_lo.bin")

          it("CharAttributesOutput should support filters") {
            val output = CharAttributesOutput("attr.bin", filter = filterConfig)
            output.filter shouldBe filterConfig
          }

          it("CharColoursOutput should support filters") {
            val output = CharColoursOutput("colours.bin", filter = filterConfig)
            output.filter shouldBe filterConfig
          }

          it("TileOutput should support filters") {
            val output = TileOutput("tiles.bin", filter = filterConfig)
            output.filter shouldBe filterConfig
          }

          it("TileTagsOutput should support filters") {
            val output = TileTagsOutput("tags.bin", filter = filterConfig)
            output.filter shouldBe filterConfig
          }

          it("TileColoursOutput should support filters") {
            val output = TileColoursOutput("tilecolours.bin", filter = filterConfig)
            output.filter shouldBe filterConfig
          }

          it("TileScreenColoursOutput should support filters") {
            val output = TileScreenColoursOutput("screencolours.bin", filter = filterConfig)
            output.filter shouldBe filterConfig
          }

          it("CharScreenColoursOutput should support filters") {
            val output = CharScreenColoursOutput("charscreencolours.bin", filter = filterConfig)
            output.filter shouldBe filterConfig
          }

          it("CharMaterialsOutput should support filters") {
            val output = CharMaterialsOutput("materials.bin", filter = filterConfig)
            output.filter shouldBe filterConfig
          }
        }

        describe("MapOutput without filter") {
          it("should not have filter field since it doesn't implement RangeOutput") {
            val output = MapOutput("map.bin")
            output.output shouldBe "map.bin"
            // MapOutput is not a RangeOutput and doesn't support filters
          }
        }
      }
    })
