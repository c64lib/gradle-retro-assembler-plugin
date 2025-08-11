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
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.io.File

class FlowDependencyGraphTest :
    BehaviorSpec({
      Given("a flow dependency graph with valid flows") {
        val compileFlow =
            Flow(
                name = "compile",
                description = "Compile source files",
                steps = listOf(FlowStep("assemble", "kickass")),
                inputs =
                    listOf(FlowArtifact("source", File("src/main.asm"), ArtifactType.SOURCE_FILE)),
                outputs =
                    listOf(
                        FlowArtifact(
                            "binary", File("build/main.prg"), ArtifactType.COMPILED_BINARY)))

        val preprocessFlow =
            Flow(
                name = "preprocess",
                description = "Preprocess assets",
                steps = listOf(FlowStep("process", "charpad")),
                inputs =
                    listOf(
                        FlowArtifact(
                            "charset", File("assets/chars.ctm"), ArtifactType.SOURCE_FILE)),
                outputs =
                    listOf(
                        FlowArtifact(
                            "processed", File("build/chars.bin"), ArtifactType.PROCESSED_ASSET)))

        val testFlow =
            Flow(
                name = "test",
                description = "Run tests",
                steps = listOf(FlowStep("execute", "vice")),
                dependencies = listOf("compile"),
                inputs =
                    listOf(
                        FlowArtifact(
                            "binary", File("build/main.prg"), ArtifactType.COMPILED_BINARY)),
                outputs =
                    listOf(
                        FlowArtifact(
                            "results", File("build/test-results.xml"), ArtifactType.TEST_RESULT)))

        val flows = listOf(compileFlow, preprocessFlow, testFlow)
        val graph = FlowDependencyGraph(flows)

        When("validating the dependency graph") {
          Then("it should not throw any exception") { shouldNotThrowAny { graph.validate() } }
        }

        When("getting execution levels") {
          val levels = graph.getExecutionLevels()

          Then("it should organize flows into correct levels") {
            levels shouldHaveSize 2
            levels[0] shouldContainExactlyInAnyOrder listOf(compileFlow, preprocessFlow)
            levels[1] shouldContainExactly listOf(testFlow)
          }
        }

        When("getting parallel flows for compile") {
          val parallelFlows = graph.getParallelFlows("compile")

          Then("it should return flows that can run in parallel") {
            parallelFlows shouldContainExactly listOf(preprocessFlow)
          }
        }

        When("getting topological order") {
          val order = graph.getTopologicalOrder()

          Then("it should return flows in dependency order") {
            order shouldHaveSize 3
            // Compile and preprocess should come before test
            val compileIndex = order.indexOf(compileFlow)
            val preprocessIndex = order.indexOf(preprocessFlow)
            val testIndex = order.indexOf(testFlow)

            compileIndex shouldBe 0
            preprocessIndex shouldBe 1
            testIndex shouldBe 2
          }
        }

        When("getting all dependencies for test flow") {
          val dependencies = graph.getAllDependencies("test")

          Then("it should return all direct dependencies") {
            dependencies shouldContainExactly setOf("compile")
          }
        }

        When("getting dependents of compile flow") {
          val dependents = graph.getDependents("compile")

          Then("it should return flows that depend on it") {
            dependents shouldContainExactly setOf("test")
          }
        }
      }

      Given("a flow dependency graph with circular dependencies") {
        val flowA = Flow(name = "flowA", dependencies = listOf("flowB"))
        val flowB = Flow(name = "flowB", dependencies = listOf("flowC"))
        val flowC = Flow(name = "flowC", dependencies = listOf("flowA"))

        val flows = listOf(flowA, flowB, flowC)
        val graph = FlowDependencyGraph(flows)

        When("validating the dependency graph") {
          Then("it should throw FlowDependencyException") {
            val exception = shouldThrow<FlowDependencyException> { graph.validate() }
            exception.message shouldContain "Circular dependency detected"
          }
        }

        When("getting execution levels") {
          Then("it should throw FlowDependencyException") {
            val exception = shouldThrow<FlowDependencyException> { graph.getExecutionLevels() }
            exception.message shouldContain "Circular dependency detected"
          }
        }
      }

      Given("a flow dependency graph with missing dependencies") {
        val flowWithMissingDep = Flow(name = "flowA", dependencies = listOf("nonExistentFlow"))

        val flows = listOf(flowWithMissingDep)
        val graph = FlowDependencyGraph(flows)

        When("validating the dependency graph") {
          Then("it should throw FlowDependencyException") {
            val exception = shouldThrow<FlowDependencyException> { graph.validate() }
            exception.message shouldContain "depends on unknown flow: 'nonExistentFlow'"
          }
        }
      }

      Given("a flow dependency graph with complex dependencies") {
        val sourceFlow = Flow(name = "source")
        val compileFlow = Flow(name = "compile", dependencies = listOf("source"))
        val preprocessFlow = Flow(name = "preprocess", dependencies = listOf("source"))
        val packageFlow = Flow(name = "package", dependencies = listOf("compile", "preprocess"))
        val testFlow = Flow(name = "test", dependencies = listOf("package"))
        val deployFlow = Flow(name = "deploy", dependencies = listOf("test"))

        val flows =
            listOf(sourceFlow, compileFlow, preprocessFlow, packageFlow, testFlow, deployFlow)
        val graph = FlowDependencyGraph(flows)

        When("getting execution levels") {
          val levels = graph.getExecutionLevels()

          Then("it should organize flows into correct hierarchical levels") {
            levels shouldHaveSize 5
            levels[0] shouldContainExactly listOf(sourceFlow)
            levels[1] shouldContainExactlyInAnyOrder listOf(compileFlow, preprocessFlow)
            levels[2] shouldContainExactly listOf(packageFlow)
            levels[3] shouldContainExactly listOf(testFlow)
            levels[4] shouldContainExactly listOf(deployFlow)
          }
        }

        When("getting all dependencies for deploy flow") {
          val dependencies = graph.getAllDependencies("deploy")

          Then("it should return all transitive dependencies") {
            dependencies shouldContainExactlyInAnyOrder
                setOf("test", "package", "compile", "preprocess", "source")
          }
        }

        When("getting dependents of source flow") {
          val dependents = graph.getDependents("source")

          Then("it should return all flows that transitively depend on it") {
            dependents shouldContainExactlyInAnyOrder
                setOf("compile", "preprocess", "package", "test", "deploy")
          }
        }
      }

      Given("a flow dependency graph with conflicting artifacts") {
        val flow1 =
            Flow(
                name = "flow1",
                outputs =
                    listOf(
                        FlowArtifact(
                            "output", File("build/shared.bin"), ArtifactType.COMPILED_BINARY)))
        val flow2 =
            Flow(
                name = "flow2",
                outputs =
                    listOf(
                        FlowArtifact(
                            "output", File("build/shared.bin"), ArtifactType.COMPILED_BINARY)))

        val flows = listOf(flow1, flow2)
        val graph = FlowDependencyGraph(flows)

        When("getting parallel flows for flow1") {
          val parallelFlows = graph.getParallelFlows("flow1")

          Then("it should not include flow2 due to conflicting artifacts") {
            parallelFlows shouldHaveSize 0
          }
        }
      }

      Given("an empty flow dependency graph") {
        val graph = FlowDependencyGraph(emptyList())

        When("getting execution levels") {
          val levels = graph.getExecutionLevels()

          Then("it should return empty list") { levels shouldHaveSize 0 }
        }

        When("getting topological order") {
          val order = graph.getTopologicalOrder()

          Then("it should return empty list") { order shouldHaveSize 0 }
        }
      }

      Given("a flow dependency graph with non-existent flow queries") {
        val flow = Flow(name = "existingFlow")
        val graph = FlowDependencyGraph(listOf(flow))

        When("getting parallel flows for non-existent flow") {
          Then("it should throw FlowDependencyException") {
            val exception =
                shouldThrow<FlowDependencyException> { graph.getParallelFlows("nonExistentFlow") }
            exception.message shouldContain "Flow not found: nonExistentFlow"
          }
        }

        When("getting dependencies for non-existent flow") {
          Then("it should throw FlowDependencyException") {
            val exception =
                shouldThrow<FlowDependencyException> { graph.getAllDependencies("nonExistentFlow") }
            exception.message shouldContain "Flow not found: nonExistentFlow"
          }
        }

        When("getting dependents for non-existent flow") {
          Then("it should throw FlowDependencyException") {
            val exception =
                shouldThrow<FlowDependencyException> { graph.getDependents("nonExistentFlow") }
            exception.message shouldContain "Flow not found: nonExistentFlow"
          }
        }
      }
    })
