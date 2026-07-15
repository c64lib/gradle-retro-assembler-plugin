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

import com.github.c64lib.rbt.flows.domain.Flow
import com.github.c64lib.rbt.flows.domain.FlowService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

/**
 * Verifies that flows built through the actual Gradle DSL produce correct implicit dependencies and
 * validate cleanly, matching real-world builds (tony-shaped pipelines).
 */
class FlowDslDependencyTest :
    BehaviorSpec({
      val service = FlowService()

      fun dependentFlows(): List<Flow> =
          FlowDslBuilder()
              .flow("assets") {
                commandStep("font", "charpad") {
                  from("src/font.ctm")
                  to("build/font.bin")
                }
              }
              .flow("compilation") {
                commandStep("compile", "kickass") {
                  from("src/main.asm", "build/font.bin")
                  to("build/main.prg")
                }
              }
              .build()

      // Mimics the structure of tony's flows: independent per-screen pipelines where each
      // flow chains steps via intermediate files and consumes only source assets externally.
      fun tonyShapedFlows(): List<Flow> =
          FlowDslBuilder()
              .flow("intro") {
                commandStep("font", "charpad") {
                  from("src/charpad/intro/font.ctm")
                  to("build/charpad/intro/font.charset.bin")
                }
                commandStep("loading", "charpad") {
                  from("src/charpad/intro/loading.ctm")
                  to("build/charpad/intro/loading.charset.bin")
                }
                // Same output as 'loading' — tony's intro flow really does this
                commandStep("loadingPicture", "charpad") {
                  from("src/charpad/intro/loading.ctm")
                  to("build/charpad/intro/loading.charset.bin")
                }
                commandStep("exomizeLoading", "exomizer") {
                  from("build/charpad/intro/loading.charset.bin")
                  to("build/charpad/intro/loading.charset.z.bin")
                }
                commandStep("compile", "kickass") {
                  from("src/kickass/intro/intro-linked.asm")
                  to("build/kickass/intro/intro-linked.bin")
                }
                commandStep("exomizeIntro", "exomizer") {
                  from("build/kickass/intro/intro-linked.bin")
                  to("build/kickass/intro/intro-linked.z.bin")
                }
              }
              .flow("title") {
                commandStep("music", "goattracker") {
                  from("src/music/tony-1.sng")
                  to("build/goattracker/title/tony-adam-vcs.sid")
                }
                commandStep("compile", "kickass") {
                  from("src/kickass/title/title-linked.asm")
                  to("build/kickass/title/title-linked.bin")
                }
                commandStep("exomizeTitle", "exomizer") {
                  from("build/kickass/title/title-linked.bin")
                  to("build/kickass/title/title-linked.z.bin")
                }
              }
              .flow("loader") {
                commandStep("compile", "kickass") {
                  from("src/kickass/loader/loader.asm")
                  to("build/kickass/loader/loader.prg")
                }
              }
              .flow("gameEnd") {
                commandStep("bitmap", "charpad") {
                  from("src/charpad/game-end/bitmap-game-end-e.ctm")
                  to("build/charpad/game-end/bitmap-game-end-e.charset.bin")
                }
                commandStep("compile", "kickass") {
                  from("src/kickass/game-end/game-end-linked.asm")
                  to("build/kickass/game-end/game-end-linked.bin")
                }
                commandStep("exomizeGameEnd", "exomizer") {
                  from("build/kickass/game-end/game-end-linked.bin")
                  to("build/kickass/game-end/game-end-linked.z.bin")
                }
              }
              .flow("gameOver") {
                commandStep("bitmap", "charpad") {
                  from("src/charpad/game-over/bitmap-game-over-e.ctm")
                  to("build/charpad/game-over/bitmap-game-over-e.charset.bin")
                }
                commandStep("compile", "kickass") {
                  from("src/kickass/game-over/game-over-linked.asm")
                  to("build/kickass/game-over/game-over-linked.prg")
                }
              }
              .flow("game") {
                commandStep("compile", "kickass") {
                  from("src/kickass/game/game-linked.asm")
                  to("build/kickass/game/game-linked.bin")
                }
                commandStep("exomizeGame", "exomizer") {
                  from("build/kickass/game/game-linked.bin")
                  to("build/kickass/game/game-linked.z.bin")
                }
              }
              .build()

      given("DSL-built flows where one flow consumes a file another produces") {
        `when`("validating the flows") {
          val result = service.validateFlows(dependentFlows())

          then("no error-severity issues are reported") { result.hasErrors shouldBe false }
        }

        `when`("computing the execution plan") {
          val plan = service.getExecutionPlan(dependentFlows())

          then("the producing flow executes in a wave before the consuming flow") {
            plan shouldHaveSize 2
            plan[0] shouldBe listOf("assets")
            plan[1] shouldBe listOf("compilation")
          }
        }
      }

      given("tony-shaped flows (independent pipelines consuming only source assets)") {
        `when`("validating the flows") {
          val result = service.validateFlows(tonyShapedFlows())

          then("no error-severity issues are reported") { result.hasErrors shouldBe false }
        }

        `when`("computing the execution plan") {
          val plan = service.getExecutionPlan(tonyShapedFlows())

          then("all six flows execute in a single parallel wave") {
            plan shouldHaveSize 1
            plan[0] shouldHaveSize 6
            plan[0] shouldContain "intro"
            plan[0] shouldContain "game"
          }
        }
      }
    })
