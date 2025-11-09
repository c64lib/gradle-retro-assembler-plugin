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

import com.github.c64lib.rbt.flows.domain.config.Frequency
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class GoattrackerStepBuilderTest :
    BehaviorSpec({
      Given("a GoattrackerStepBuilder") {
        When("building step with basic configuration") {
          val builder = GoattrackerStepBuilder("test-goattracker")
          builder.from("input.sng")
          builder.to("output.sid")
          val step = builder.build()

          Then("it should create GoattrackerStep with correct properties") {
            step.name shouldBe "test-goattracker"
            step.inputs shouldBe listOf("input.sng")
            step.outputs shouldBe listOf("output.sid")
            step.config.frequency shouldBe Frequency.PAL
            step.config.channels shouldBe 3
            step.config.optimization shouldBe true
            step.config.executable shouldBe "gt2reloc"
          }
        }

        When("building with multiple input/output files") {
          val builder = GoattrackerStepBuilder("multi-file")
          builder.from("song1.sng", "song2.sng")
          builder.to("song1.sid", "song2.sid")
          val step = builder.build()

          Then("it should accept multiple inputs and outputs") {
            step.inputs shouldBe listOf("song1.sng", "song2.sng")
            step.outputs shouldBe listOf("song1.sid", "song2.sid")
          }
        }

        When("building with frequency configuration") {
          val builder = GoattrackerStepBuilder("freq-config")
          builder.from("input.sng")
          builder.to("output.sid")
          builder.frequency = Frequency.NTSC
          val step = builder.build()

          Then("it should apply frequency configuration") {
            step.config.frequency shouldBe Frequency.NTSC
          }
        }

        When("building with channels configuration") {
          val builder = GoattrackerStepBuilder("channels-config")
          builder.from("input.sng")
          builder.to("output.sid")
          builder.channels = 1
          val step = builder.build()

          Then("it should apply channels configuration") { step.config.channels shouldBe 1 }
        }

        When("building with optimization flag") {
          val builder = GoattrackerStepBuilder("optimize-config")
          builder.from("input.sng")
          builder.to("output.sid")
          builder.optimization = false
          val step = builder.build()

          Then("it should apply optimization configuration") {
            step.config.optimization shouldBe false
          }
        }

        When("building with custom executable path") {
          val builder = GoattrackerStepBuilder("exe-config")
          builder.from("input.sng")
          builder.to("output.sid")
          builder.executable = "/custom/path/gt2reloc"
          val step = builder.build()

          Then("it should apply executable configuration") {
            step.config.executable shouldBe "/custom/path/gt2reloc"
          }
        }

        When("building with buffered SID writes") {
          val builder = GoattrackerStepBuilder("buffered-config")
          builder.from("input.sng")
          builder.to("output.sid")
          builder.bufferedSidWrites = true
          val step = builder.build()

          Then("it should apply bufferedSidWrites configuration") {
            step.config.bufferedSidWrites shouldBe true
          }
        }

        When("building with disable optimization flag") {
          val builder = GoattrackerStepBuilder("disable-opt")
          builder.from("input.sng")
          builder.to("output.sid")
          builder.disableOptimization = true
          val step = builder.build()

          Then("it should apply disableOptimization configuration") {
            step.config.disableOptimization shouldBe true
          }
        }

        When("building with player memory location") {
          val builder = GoattrackerStepBuilder("player-mem")
          builder.from("input.sng")
          builder.to("output.sid")
          builder.playerMemoryLocation = 0x1000
          val step = builder.build()

          Then("it should apply playerMemoryLocation configuration") {
            step.config.playerMemoryLocation shouldBe 0x1000
          }
        }

        When("building with SFX support") {
          val builder = GoattrackerStepBuilder("sfx-config")
          builder.from("input.sng")
          builder.to("output.sid")
          builder.sfxSupport = true
          val step = builder.build()

          Then("it should apply sfxSupport configuration") { step.config.sfxSupport shouldBe true }
        }

        When("building with SID memory location") {
          val builder = GoattrackerStepBuilder("sid-mem")
          builder.from("input.sng")
          builder.to("output.sid")
          builder.sidMemoryLocation = 0x2000
          val step = builder.build()

          Then("it should apply sidMemoryLocation configuration") {
            step.config.sidMemoryLocation shouldBe 0x2000
          }
        }

        When("building with store author info") {
          val builder = GoattrackerStepBuilder("author-config")
          builder.from("input.sng")
          builder.to("output.sid")
          builder.storeAuthorInfo = true
          val step = builder.build()

          Then("it should apply storeAuthorInfo configuration") {
            step.config.storeAuthorInfo shouldBe true
          }
        }

        When("building with volume change support") {
          val builder = GoattrackerStepBuilder("volume-config")
          builder.from("input.sng")
          builder.to("output.sid")
          builder.volumeChangeSupport = true
          val step = builder.build()

          Then("it should apply volumeChangeSupport configuration") {
            step.config.volumeChangeSupport shouldBe true
          }
        }

        When("building with zero page location") {
          val builder = GoattrackerStepBuilder("zero-page-config")
          builder.from("input.sng")
          builder.to("output.sid")
          builder.zeroPageLocation = 0xF0
          val step = builder.build()

          Then("it should apply zeroPageLocation configuration") {
            step.config.zeroPageLocation shouldBe 0xF0
          }
        }

        When("building with zero page ghost registers") {
          val builder = GoattrackerStepBuilder("ghost-config")
          builder.from("input.sng")
          builder.to("output.sid")
          builder.zeropageGhostRegisters = true
          val step = builder.build()

          Then("it should apply zeropageGhostRegisters configuration") {
            step.config.zeropageGhostRegisters shouldBe true
          }
        }

        When("building with all advanced parameters") {
          val builder = GoattrackerStepBuilder("full-config")
          builder.from("input.sng")
          builder.to("output.sid")
          builder.frequency = Frequency.NTSC
          builder.channels = 1
          builder.optimization = false
          builder.executable = "/path/to/gt2reloc"
          builder.bufferedSidWrites = true
          builder.disableOptimization = true
          builder.playerMemoryLocation = 0x1000
          builder.sfxSupport = true
          builder.sidMemoryLocation = 0x2000
          builder.storeAuthorInfo = true
          builder.volumeChangeSupport = true
          builder.zeroPageLocation = 0xF0
          builder.zeropageGhostRegisters = true
          val step = builder.build()

          Then("it should apply all parameters correctly") {
            step.config.frequency shouldBe Frequency.NTSC
            step.config.channels shouldBe 1
            step.config.optimization shouldBe false
            step.config.executable shouldBe "/path/to/gt2reloc"
            step.config.bufferedSidWrites shouldBe true
            step.config.disableOptimization shouldBe true
            step.config.playerMemoryLocation shouldBe 0x1000
            step.config.sfxSupport shouldBe true
            step.config.sidMemoryLocation shouldBe 0x2000
            step.config.storeAuthorInfo shouldBe true
            step.config.volumeChangeSupport shouldBe true
            step.config.zeroPageLocation shouldBe 0xF0
            step.config.zeropageGhostRegisters shouldBe true
          }
        }

        When("building with null optional parameters") {
          val builder = GoattrackerStepBuilder("null-params")
          builder.from("input.sng")
          builder.to("output.sid")
          val step = builder.build()

          Then("optional parameters should be null") {
            step.config.bufferedSidWrites shouldBe null
            step.config.disableOptimization shouldBe null
            step.config.playerMemoryLocation shouldBe null
            step.config.sfxSupport shouldBe null
            step.config.sidMemoryLocation shouldBe null
            step.config.storeAuthorInfo shouldBe null
            step.config.volumeChangeSupport shouldBe null
            step.config.zeroPageLocation shouldBe null
            step.config.zeropageGhostRegisters shouldBe null
          }
        }
      }
    })
