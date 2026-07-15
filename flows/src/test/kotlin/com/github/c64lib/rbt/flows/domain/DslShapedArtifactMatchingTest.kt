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

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

/**
 * Verifies that artifact matching is path-based, using artifacts shaped like the ones the Gradle
 * DSL adapter generates: producer and consumer names never match ("{step}_output_{n}" vs
 * "{step}_input_{n}"), only their paths do.
 */
class DslShapedArtifactMatchingTest :
    BehaviorSpec({
      fun inputArtifact(step: String, index: Int, path: String, sourceFile: Boolean = false) =
          FlowArtifact("${step}_input_$index", path, isSourceFile = sourceFile)
      fun outputArtifact(step: String, index: Int, path: String) =
          FlowArtifact("${step}_output_$index", path)

      given("two DSL-shaped flows where one consumes a path the other produces") {
        val producer =
            Flow(
                name = "assets",
                consumes = listOf(inputArtifact("font", 0, "src/font.ctm", sourceFile = true)),
                produces = listOf(outputArtifact("font", 0, "build/font.bin")))
        val consumer =
            Flow(
                name = "compilation",
                consumes =
                    listOf(
                        inputArtifact("compile", 0, "src/main.asm", sourceFile = true),
                        inputArtifact("compile", 1, "build/font.bin")),
                produces = listOf(outputArtifact("compile", 0, "build/main.prg")))
        val graph = FlowDependencyGraph().addFlow(producer).addFlow(consumer)

        `when`("validating the graph") {
          val result = graph.validate()

          then("no error-severity issues are reported") { result.hasErrors shouldBe false }
        }

        `when`("getting parallel execution order") {
          val executionOrder = graph.getParallelExecutionOrder()

          then("the producing flow executes before the consuming flow") {
            executionOrder shouldHaveSize 2
            executionOrder[0] shouldBe listOf("assets")
            executionOrder[1] shouldBe listOf("compilation")
          }
        }
      }

      given("a flow consuming an intermediate it produces itself (within-flow pipeline)") {
        val flow =
            Flow(
                name = "intro",
                consumes =
                    listOf(
                        inputArtifact("charpad", 0, "src/intro.ctm", sourceFile = true),
                        inputArtifact("exomize", 0, "build/intro.charset.bin")),
                produces =
                    listOf(
                        outputArtifact("charpad", 0, "build/intro.charset.bin"),
                        outputArtifact("exomize", 0, "build/intro.charset.z.bin")))
        val graph = FlowDependencyGraph().addFlow(flow)

        `when`("validating the graph") {
          val result = graph.validate()

          then("no missing-producer error is reported for the intermediate") {
            result.hasErrors shouldBe false
          }
        }

        `when`("getting parallel execution order") {
          val executionOrder = graph.getParallelExecutionOrder()

          then("the flow is schedulable without a self-dependency") {
            executionOrder shouldHaveSize 1
            executionOrder[0] shouldContain "intro"
          }
        }
      }

      given("two steps of the same flow producing the same output path") {
        // Mirrors tony's intro flow, where the 'loading' and 'loadingPicture' steps
        // both emit the same charset file.
        val flow =
            Flow(
                name = "intro",
                consumes = listOf(inputArtifact("loading", 0, "src/loading.ctm", sourceFile = true)),
                produces =
                    listOf(
                        outputArtifact("loading", 0, "build/loading.charset.bin"),
                        outputArtifact("loadingPicture", 1, "build/loading.charset.bin")))

        `when`("adding the flow to the graph") {
          then("no duplicate-producer exception is thrown") {
            shouldNotThrowAny { FlowDependencyGraph().addFlow(flow) }
          }
        }
      }

      given("two different flows producing the same output path") {
        val flowA =
            Flow(name = "flowA", produces = listOf(outputArtifact("stepA", 0, "build/shared.bin")))
        val flowB =
            Flow(name = "flowB", produces = listOf(outputArtifact("stepB", 0, "build/shared.bin")))

        `when`("adding both flows to the graph") {
          then("a duplicate-producer exception is thrown") {
            shouldThrow<FlowValidationException> {
              FlowDependencyGraph().addFlow(flowA).addFlow(flowB)
            }
          }
        }
      }
    })
