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

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class SourceFileValidationTest :
    BehaviorSpec({
      Given("a source file artifact") {
        val sourceFile =
            FlowArtifact.sourceFile(
                name = "main.asm",
                path = "src/main/asm/main.asm",
                description = "Main assembly source file")
        val flow = Flow(name = "assemble", consumes = listOf(sourceFile))
        val graph = FlowDependencyGraph()

        When("the flow is added to the graph and validated") {
          graph.addFlow(flow)
          val result = graph.validate()

          Then("the validation should succeed with no issues") {
            result.isValid shouldBe true
            result.issues.shouldBeEmpty()
          }
        }
      }

      Given("a produced artifact without a producer") {
        val producedArtifact =
            FlowArtifact.produced(
                name = "charset.bin",
                path = "build/processed/charset.bin",
                description = "Processed character set data")
        val flow = Flow(name = "assemble", consumes = listOf(producedArtifact))
        val graph = FlowDependencyGraph()

        When("the flow is added to the graph and validated") {
          graph.addFlow(flow)
          val result = graph.validate()

          Then("the validation should fail with a missing producer issue") {
            result.isValid shouldBe false
            result.hasErrors shouldBe true
            result.issues shouldHaveSize 1
            result.issues.first() shouldBe
                FlowValidationIssue.MissingArtifactProducer(producedArtifact, listOf("assemble"))
          }
        }
      }

      Given("a mix of source files and produced artifacts with proper producers") {
        val sourceFile = FlowArtifact.sourceFile(name = "main.asm", path = "src/main/asm/main.asm")
        val charsetData =
            FlowArtifact.produced(
                name = "charset.bin",
                path = "build/processed/charset.bin",
            )
        val charpadFlow = Flow(name = "charpad", produces = listOf(charsetData))
        val assembleFlow = Flow(name = "assemble", consumes = listOf(sourceFile, charsetData))
        val graph = FlowDependencyGraph()

        When("all flows are added to the graph and validated") {
          graph.addFlow(charpadFlow)
          graph.addFlow(assembleFlow)
          val result = graph.validate()

          Then("the validation should succeed with no issues") {
            result.isValid shouldBe true
            result.issues.shouldBeEmpty()
          }
        }
      }
    })
