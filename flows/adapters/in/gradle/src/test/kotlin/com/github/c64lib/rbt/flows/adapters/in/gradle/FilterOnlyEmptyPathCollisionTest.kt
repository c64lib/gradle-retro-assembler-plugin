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

import com.github.c64lib.rbt.flows.adapters.`in`.gradle.dsl.CharpadStepBuilder
import com.github.c64lib.rbt.flows.domain.FlowService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe

/**
 * Regression coverage for issue #181: a filter-only Charpad output block (an
 * `interleaver`/`nybbler` filter with no primary `output`) carries an empty primary path. Before
 * the fix this empty path was registered as a `''` produced [FlowArtifact], and two flows each
 * declaring such a block collided during flow validation ("Artifact path '' is produced by multiple
 * flows"), aborting every Gradle task at configuration time.
 *
 * These tests drive the real DSL → `build()` → [FlowService.validateFlows] path (where the empty
 * path is actually born), not hand-built `Flow.produces`, so they faithfully reproduce the bug.
 */
class FilterOnlyEmptyPathCollisionTest :
    BehaviorSpec({
      Given("a Charpad step with a filter-only tiles block (no primary output)") {
        val step =
            CharpadStepBuilder("font")
                .apply {
                  from("intro.ctm")
                  tiles {
                    interleaver { outputs = listOf("intro.tiles.bin", "intro-odd.tiles.bin") }
                  }
                }
                .build()

        When("computing the step outputs") {
          Then("the real interleaver sub-outputs are present") {
            step.outputs shouldContainAll listOf("intro.tiles.bin", "intro-odd.tiles.bin")
          }

          Then("no empty output path leaks in") { step.outputs shouldNotContain "" }
        }

        And("the block still counts as producing outputs") {
          step.charpadOutputs.hasOutputs() shouldBe true
        }
      }

      Given("a Charpad step with an empty meta block") {
        // meta {} is added unconditionally by the builder, so an unset output stays "".
        val step =
            CharpadStepBuilder("meta-empty")
                .apply {
                  from("intro.ctm")
                  charset { output = "charset.chr" }
                  meta {}
                }
                .build()

        When("computing the step outputs") {
          Then("the real charset output survives and no empty path leaks in") {
            step.outputs shouldContain "charset.chr"
            step.outputs shouldNotContain ""
          }
        }
      }

      Given("two flows that each contain a filter-only Charpad block") {
        // Reproduces tony's intro/game topology from issue #181, driven through FlowDslBuilder —
        // the same entry point the Gradle plugin uses (it also marks unproduced consumed inputs as
        // source files, so raw .ctm inputs are not reported as missing producers).
        val flows =
            FlowDslBuilder()
                .apply {
                  flow("intro") {
                    charpadStep("font") {
                      from("intro.ctm")
                      tiles {
                        interleaver { outputs = listOf("intro.tiles.bin", "intro-odd.tiles.bin") }
                      }
                    }
                  }
                  flow("game") {
                    charpadStep("tiles") {
                      from("game.ctm")
                      tiles {
                        interleaver { outputs = listOf("game.tiles.bin", "game-odd.tiles.bin") }
                      }
                    }
                  }
                }
                .build()

        val introFlow = flows.first { it.name == "intro" }
        val gameFlow = flows.first { it.name == "game" }

        When("validating both flows together") {
          val result = FlowService().validateFlows(flows)

          Then("validation does not fail on an empty artifact path") {
            // Would throw FlowValidationException("Artifact path '' is produced by multiple flows")
            // before the fix; reaching here at all means the empty-path collision is gone.
            result.hasErrors shouldBe false
          }

          Then("neither flow registers an empty produced artifact") {
            introFlow.produces.map { it.path } shouldNotContain ""
            gameFlow.produces.map { it.path } shouldNotContain ""
          }

          Then("the real filter sub-outputs are still registered as produced artifacts") {
            introFlow.produces.map { it.path } shouldContainAll
                listOf("intro.tiles.bin", "intro-odd.tiles.bin")
            gameFlow.produces.map { it.path } shouldContainAll
                listOf("game.tiles.bin", "game-odd.tiles.bin")
          }
        }
      }
    })
