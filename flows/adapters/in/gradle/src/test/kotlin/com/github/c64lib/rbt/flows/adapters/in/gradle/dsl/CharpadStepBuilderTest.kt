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
package com.github.c64lib.rbt.flows.adapters.`in`.gradle.dsl

import com.github.c64lib.rbt.flows.domain.config.CharpadCompression
import com.github.c64lib.rbt.flows.domain.config.CharpadFormat
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class CharpadStepBuilderTest :
    BehaviorSpec({
      Given("a CharpadStepBuilder") {
        When("building step with basic configuration") {
          val builder = CharpadStepBuilder("test-charpad")
          builder.from("input.ctm")
          builder.charset { output = "charset.chr" }
          val step = builder.build()

          Then("it should create CharpadStep with correct properties") {
            step.name shouldBe "test-charpad"
            step.inputs shouldBe listOf("input.ctm")
            step.outputs shouldBe listOf("charset.chr")
            step.charpadOutputs.charsets shouldHaveSize 1
            step.charpadOutputs.charsets.first().output shouldBe "charset.chr"
          }
        }

        When("building with multiple input files") {
          val builder = CharpadStepBuilder("multi-input")
          builder.from("file1.ctm")
          builder.from("file2.ctm", "file3.ctm")
          builder.charset { output = "output.chr" }
          val step = builder.build()

          Then("it should accept multiple inputs") {
            step.inputs shouldBe listOf("file1.ctm", "file2.ctm", "file3.ctm")
          }
        }

        When("building with all output types") {
          val builder = CharpadStepBuilder("all-outputs")
          builder.from("input.ctm")
          builder.charset { output = "charset.chr" }
          builder.map { output = "map.bin" }
          builder.tiles { output = "tiles.bin" }
          builder.charsetColours { output = "colours.bin" }
          builder.charsetAttributes { output = "attrs.bin" }
          builder.charsetMaterials { output = "materials.bin" }
          builder.charsetScreenColours { output = "screen_colours.bin" }
          builder.tileTags { output = "tile_tags.bin" }
          builder.tileColours { output = "tile_colours.bin" }
          builder.tileScreenColours { output = "tile_screen_colours.bin" }
          builder.meta { output = "header.h" }
          val step = builder.build()

          Then("it should include all output types") {
            step.outputs shouldHaveSize 11
            step.charpadOutputs.charsets shouldHaveSize 1
            step.charpadOutputs.maps shouldHaveSize 1
            step.charpadOutputs.tiles shouldHaveSize 1
            step.charpadOutputs.charColours shouldHaveSize 1
            step.charpadOutputs.charAttributes shouldHaveSize 1
            step.charpadOutputs.charMaterials shouldHaveSize 1
            step.charpadOutputs.charScreenColours shouldHaveSize 1
            step.charpadOutputs.tileTags shouldHaveSize 1
            step.charpadOutputs.tileColours shouldHaveSize 1
            step.charpadOutputs.tileScreenColours shouldHaveSize 1
            step.charpadOutputs.metadata shouldHaveSize 1
          }
        }

        When("building with range parameters") {
          val builder = CharpadStepBuilder("range-test")
          builder.from("input.ctm")
          builder.charset {
            output = "charset1.chr"
            start = 0
            end = 128
          }
          builder.charset {
            output = "charset2.chr"
            start = 128
            end = 256
          }
          val step = builder.build()

          Then("it should support multiple outputs with different ranges") {
            step.charpadOutputs.charsets shouldHaveSize 2
            step.charpadOutputs.charsets[0].start shouldBe 0
            step.charpadOutputs.charsets[0].end shouldBe 128
            step.charpadOutputs.charsets[1].start shouldBe 128
            step.charpadOutputs.charsets[1].end shouldBe 256
          }
        }

        When("building with map region parameters") {
          val builder = CharpadStepBuilder("map-region")
          builder.from("input.ctm")
          builder.map {
            output = "region.bin"
            left = 5
            top = 10
            right = 35
            bottom = 20
          }
          val step = builder.build()

          Then("it should support rectangular regions") {
            step.charpadOutputs.maps shouldHaveSize 1
            val map = step.charpadOutputs.maps.first()
            map.left shouldBe 5
            map.top shouldBe 10
            map.right shouldBe 35
            map.bottom shouldBe 20
          }
        }

        When("building with metadata configuration") {
          val builder = CharpadStepBuilder("metadata-test")
          builder.from("input.ctm")
          builder.meta {
            output = "header.h"
            namespace = "GAME"
            prefix = "CHR_"
            includeVersion = true
            includeBgColours = false
            includeCharColours = true
            includeMode = false
          }
          val step = builder.build()

          Then("it should apply metadata configuration") {
            val meta = step.charpadOutputs.metadata.first()
            meta.namespace shouldBe "GAME"
            meta.prefix shouldBe "CHR_"
            meta.includeVersion shouldBe true
            meta.includeBgColours shouldBe false
            meta.includeCharColours shouldBe true
            meta.includeMode shouldBe false
          }
        }

        When("building with custom compression and format") {
          val builder = CharpadStepBuilder("config-test")
          builder.from("input.ctm")
          builder.charset { output = "output.chr" }
          builder.compression = CharpadCompression.RLE
          builder.exportFormat = CharpadFormat.OPTIMIZED
          builder.tileSize = 16
          builder.charsetOptimization = false
          builder.generateMap = false
          builder.generateCharset = true
          val step = builder.build()

          Then("it should apply configuration options") {
            step.config.compression shouldBe CharpadCompression.RLE
            step.config.exportFormat shouldBe CharpadFormat.OPTIMIZED
            step.config.tileSize shouldBe 16
            step.config.charsetOptimization shouldBe false
            step.config.generateMap shouldBe false
            step.config.generateCharset shouldBe true
          }
        }

        When("building with default metadata flags") {
          val builder = CharpadStepBuilder("meta-defaults")
          builder.from("input.ctm")
          builder.meta { output = "header.h" }
          val step = builder.build()

          Then("it should use global metadata defaults") {
            val meta = step.charpadOutputs.metadata.first()
            meta.namespace shouldBe ""
            meta.prefix shouldBe ""
            meta.includeVersion shouldBe false
            meta.includeBgColours shouldBe true
            meta.includeCharColours shouldBe true
            meta.includeMode shouldBe false
          }
        }

        When("building with global and per-output metadata") {
          val builder = CharpadStepBuilder("mixed-meta")
          builder.from("input.ctm")
          builder.namespace = "GLOBAL"
          builder.prefix = "GLOBAL_"
          builder.meta {
            output = "specific.h"
            namespace = "SPECIFIC"
            prefix = "SPEC_"
          }
          val step = builder.build()

          Then("it should use per-output metadata over global") {
            step.config.namespace shouldBe "GLOBAL"
            step.config.prefix shouldBe "GLOBAL_"
            val meta = step.charpadOutputs.metadata.first()
            meta.namespace shouldBe "SPECIFIC"
            meta.prefix shouldBe "SPEC_"
          }
        }

        When("building with no outputs") {
          val builder = CharpadStepBuilder("no-outputs")
          builder.from("input.ctm")
          val step = builder.build()

          Then("it should have empty output lists") {
            step.outputs.shouldBeEmpty()
            step.charpadOutputs.charsets.shouldBeEmpty()
            step.charpadOutputs.maps.shouldBeEmpty()
          }
        }
      }
    })
