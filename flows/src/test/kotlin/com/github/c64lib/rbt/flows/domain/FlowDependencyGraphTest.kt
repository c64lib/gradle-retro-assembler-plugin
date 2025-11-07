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
package com.github.c64lib.rbt.flows.domain

import com.github.c64lib.rbt.flows.domain.config.CharpadOutputs
import com.github.c64lib.rbt.flows.domain.config.CharsetOutput
import com.github.c64lib.rbt.flows.domain.steps.CharpadStep
import com.github.c64lib.rbt.flows.domain.steps.SpritepadStep
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class FlowDependencyGraphTest :
    BehaviorSpec({
      given("an empty flow dependency graph") {
        val graph = FlowDependencyGraph()

        `when`("validating the empty graph") {
          val result = graph.validate()

          then("it should be valid") {
            result.isValid shouldBe true
            result.issues shouldHaveSize 0
          }
        }

        `when`("getting parallel execution order") {
          val executionOrder = graph.getParallelExecutionOrder()

          then("it should return empty list") { executionOrder shouldHaveSize 0 }
        }
      }

      given("a flow dependency graph with independent flows") {
        val graph = FlowDependencyGraph()
        val spriteArtifact = FlowArtifact("sprites.dat", "build/sprites.dat")
        val charsetArtifact = FlowArtifact("charset.dat", "build/charset.dat")

        val spriteFlow =
            Flow(
                name = "processSprites",
                steps = listOf(SpritepadStep("spritepad")),
                produces = listOf(spriteArtifact))

        val charsetFlow =
            Flow(
                name = "processCharset",
                steps =
                    listOf(
                        CharpadStep(
                            "charpad",
                            charpadOutputs =
                                CharpadOutputs(charsets = listOf(CharsetOutput("charset.chr"))))),
                produces = listOf(charsetArtifact))

        graph.addFlow(spriteFlow)
        graph.addFlow(charsetFlow)

        `when`("validating the graph") {
          val result = graph.validate()

          then("it should be valid with orphaned flow warnings") {
            result.hasErrors shouldBe false
            result.hasWarnings shouldBe true
            result.issues shouldHaveSize 2 // Two orphaned flows
            result.issues.forEach { issue ->
              issue.shouldBeInstanceOf<FlowValidationIssue.OrphanedFlow>()
            }
          }
        }

        `when`("getting parallel execution order") {
          val executionOrder = graph.getParallelExecutionOrder()

          then("both flows should execute in the same level (parallel)") {
            executionOrder shouldHaveSize 1
            executionOrder[0] shouldHaveSize 2
            executionOrder[0] shouldContain "processSprites"
            executionOrder[0] shouldContain "processCharset"
          }
        }

        `when`("checking parallel candidates for sprite flow") {
          val candidates = graph.getParallelCandidates("processSprites")

          then("charset flow should be a parallel candidate") {
            candidates shouldContain "processCharset"
          }
        }
      }

      given("a flow dependency graph with sequential dependencies") {
        val graph = FlowDependencyGraph()
        val spriteArtifact = FlowArtifact("sprites.dat", "build/sprites.dat")
        val binaryArtifact = FlowArtifact("game.prg", "build/game.prg")

        val spriteFlow = Flow(name = "processSprites", produces = listOf(spriteArtifact))

        val compileFlow =
            Flow(
                name = "compile",
                consumes = listOf(spriteArtifact),
                produces = listOf(binaryArtifact))

        val testFlow = Flow(name = "test", consumes = listOf(binaryArtifact))

        graph.addFlow(spriteFlow)
        graph.addFlow(compileFlow)
        graph.addFlow(testFlow)

        `when`("validating the graph") {
          val result = graph.validate()

          then("it should be valid") { result.isValid shouldBe true }
        }

        `when`("getting parallel execution order") {
          val executionOrder = graph.getParallelExecutionOrder()

          then("flows should execute in sequential order") {
            executionOrder shouldHaveSize 3
            executionOrder[0] shouldBe listOf("processSprites")
            executionOrder[1] shouldBe listOf("compile")
            executionOrder[2] shouldBe listOf("test")
          }
        }

        `when`("checking parallel candidates for sprite flow") {
          val candidates = graph.getParallelCandidates("processSprites")

          then("no other flows should be parallel candidates") { candidates shouldHaveSize 0 }
        }
      }

      given("a flow dependency graph with circular dependencies") {
        val graph = FlowDependencyGraph()
        val artifact1 = FlowArtifact("data1.dat", "build/data1.dat")
        val artifact2 = FlowArtifact("data2.dat", "build/data2.dat")

        val flow1 = Flow(name = "flow1", produces = listOf(artifact1), consumes = listOf(artifact2))

        val flow2 = Flow(name = "flow2", produces = listOf(artifact2), consumes = listOf(artifact1))

        graph.addFlow(flow1)
        graph.addFlow(flow2)

        `when`("validating the graph") {
          val result = graph.validate()

          then("it should detect circular dependency") {
            result.isValid shouldBe false
            result.hasErrors shouldBe true
            result.issues shouldHaveSize 1
            val issue = result.issues[0]
            issue.shouldBeInstanceOf<FlowValidationIssue.CircularDependency>()
          }
        }
      }

      given("a flow dependency graph with missing artifact producers") {
        val graph = FlowDependencyGraph()
        val missingArtifact = FlowArtifact("missing.dat", "build/missing.dat")

        val consumerFlow = Flow(name = "consumer", consumes = listOf(missingArtifact))

        graph.addFlow(consumerFlow)

        `when`("validating the graph") {
          val result = graph.validate()

          then("it should detect missing artifact producer") {
            result.isValid shouldBe false
            result.hasErrors shouldBe true
            result.issues shouldHaveSize 1
            val issue = result.issues[0]
            issue.shouldBeInstanceOf<FlowValidationIssue.MissingArtifactProducer>()
          }
        }
      }

      given("a flow dependency graph with complex parallel structure") {
        val graph = FlowDependencyGraph()

        // Create artifacts
        val spriteArtifact = FlowArtifact("sprites.dat", "build/sprites.dat")
        val charsetArtifact = FlowArtifact("charset.dat", "build/charset.dat")
        val musicArtifact = FlowArtifact("music.dat", "build/music.dat")
        val binaryArtifact = FlowArtifact("game.prg", "build/game.prg")

        // Create flows
        val spriteFlow = Flow("processSprites", produces = listOf(spriteArtifact))
        val charsetFlow = Flow("processCharset", produces = listOf(charsetArtifact))
        val musicFlow = Flow("processMusic", produces = listOf(musicArtifact))
        val compileFlow =
            Flow(
                "compile",
                consumes = listOf(spriteArtifact, charsetArtifact, musicArtifact),
                produces = listOf(binaryArtifact))
        val testFlow = Flow("test", consumes = listOf(binaryArtifact))

        graph.addFlow(spriteFlow)
        graph.addFlow(charsetFlow)
        graph.addFlow(musicFlow)
        graph.addFlow(compileFlow)
        graph.addFlow(testFlow)

        `when`("getting parallel execution order") {
          val executionOrder = graph.getParallelExecutionOrder()

          then("preprocessing flows should run in parallel, then compile, then test") {
            executionOrder shouldHaveSize 3
            executionOrder[0] shouldHaveSize 3 // All preprocessing in parallel
            executionOrder[0] shouldContain "processSprites"
            executionOrder[0] shouldContain "processCharset"
            executionOrder[0] shouldContain "processMusic"
            executionOrder[1] shouldBe listOf("compile")
            executionOrder[2] shouldBe listOf("test")
          }
        }

        `when`("checking parallel candidates for sprite processing") {
          val candidates = graph.getParallelCandidates("processSprites")

          then("charset and music processing should be parallel candidates") {
            candidates shouldHaveSize 2
            candidates shouldContain "processCharset"
            candidates shouldContain "processMusic"
          }
        }
      }
    })
