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
import com.github.c64lib.rbt.flows.domain.steps.AssembleStep
import com.github.c64lib.rbt.flows.domain.steps.CommandStep
import com.github.c64lib.rbt.flows.domain.steps.DasmStep
import com.github.c64lib.rbt.flows.domain.steps.ExomizerStep
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

/**
 * FlowTasksGenerator domain model tests.
 *
 * Tests verify that Flow and Step domain objects can be properly constructed with various
 * configurations representing different build pipeline scenarios.
 */
class FlowTasksGeneratorTest :
    BehaviorSpec({
      given("flow domain models") {
        `when`("creating empty flow collection") {
          then("should support empty flows") {
            val flows = emptyList<Flow>()
            flows.size shouldBe 0
          }
        }

        `when`("creating single flow with one step") {
          then("should construct properly") {
            val step = CommandStep(name = "step1", command = "echo")
            val flow = Flow(name = "flow1", steps = listOf(step))
            flow.name shouldBe "flow1"
            flow.steps.size shouldBe 1
          }
        }

        `when`("creating multiple flows") {
          then("should handle all flows") {
            val flow1 =
                Flow(name = "flow1", steps = listOf(CommandStep(name = "s1", command = "cmd")))
            val flow2 =
                Flow(name = "flow2", steps = listOf(CommandStep(name = "s2", command = "cmd")))
            val flow3 =
                Flow(name = "flow3", steps = listOf(CommandStep(name = "s3", command = "cmd")))
            val flows = listOf(flow1, flow2, flow3)
            flows.size shouldBe 3
          }
        }

        `when`("creating flows with dependencies") {
          then("should track dependency relationships") {
            Flow(name = "preprocess", steps = listOf(CommandStep(name = "pre", command = "cmd")))
            val flow2 =
                Flow(
                    name = "compile",
                    steps = listOf(CommandStep(name = "comp", command = "cmd")),
                    dependsOn = listOf("preprocess"))
            val flow3 =
                Flow(
                    name = "package",
                    steps = listOf(CommandStep(name = "pkg", command = "cmd")),
                    dependsOn = listOf("compile"))
            flow3.dependsOn shouldBe listOf("compile")
            flow2.dependsOn shouldBe listOf("preprocess")
          }
        }

        `when`("creating flow with multiple steps") {
          then("should preserve step ordering") {
            val step1 = CommandStep(name = "step1", command = "cmd1")
            val step2 = CommandStep(name = "step2", command = "cmd2")
            val step3 = CommandStep(name = "step3", command = "cmd3")
            val flow = Flow(name = "pipeline", steps = listOf(step1, step2, step3))
            flow.steps.size shouldBe 3
            flow.steps[0].name shouldBe "step1"
            flow.steps[1].name shouldBe "step2"
            flow.steps[2].name shouldBe "step3"
          }
        }

        `when`("creating flow with different step types") {
          then("should accept all supported step types") {
            val steps =
                listOf(
                    CommandStep(name = "cmd", command = "echo"),
                    AssembleStep(
                        name = "asm", inputs = listOf("main.asm"), outputs = listOf("out.prg")),
                    DasmStep(
                        name = "dasm", inputs = listOf("main.dasm"), outputs = listOf("out.prg")),
                    ExomizerStep(
                        name = "exo", inputs = listOf("in.bin"), outputs = listOf("out.z")))
            val flow = Flow(name = "mixed", steps = steps)
            flow.steps.size shouldBe 4
          }
        }

        `when`("creating steps with file-based dependencies") {
          then("should track input-output relationships") {
            val step1 = CommandStep(name = "gen", command = "gen", outputs = listOf("out.asm"))
            val step2 =
                AssembleStep(name = "asm", inputs = listOf("out.asm"), outputs = listOf("prog.prg"))
            val flow = Flow(name = "build", steps = listOf(step1, step2))
            flow.steps[0].outputs shouldBe listOf("out.asm")
            flow.steps[1].inputs shouldBe listOf("out.asm")
          }
        }

        `when`("creating steps without outputs") {
          then("should allow empty output lists") {
            val step = CommandStep(name = "report", command = "echo", outputs = emptyList())
            val flow = Flow(name = "report", steps = listOf(step))
            flow.steps[0].outputs.size shouldBe 0
          }
        }

        `when`("creating steps with multiple inputs and outputs") {
          then("should handle complex I/O scenarios") {
            val step1 =
                CommandStep(
                    name = "preprocess",
                    command = "tool",
                    inputs = listOf("input1.txt", "input2.txt"),
                    outputs = listOf("out1.txt", "out2.txt"))
            val step2 =
                CommandStep(
                    name = "process",
                    command = "tool2",
                    inputs = listOf("out1.txt", "out2.txt"),
                    outputs = listOf("final.txt"))
            val flow = Flow(name = "pipeline", steps = listOf(step1, step2))
            flow.steps[0].inputs.size shouldBe 2
            flow.steps[0].outputs.size shouldBe 2
            flow.steps[1].inputs.size shouldBe 2
            flow.steps[1].outputs.size shouldBe 1
          }
        }

        `when`("creating complex build graph with shared dependencies") {
          then("should handle multi-flow scenarios") {
            val shared =
                Flow(
                    name = "shared",
                    steps =
                        listOf(
                            CommandStep(
                                name = "gen", command = "gen", outputs = listOf("shared.asm"))))
            val prog1 =
                Flow(
                    name = "prog1",
                    steps =
                        listOf(
                            CommandStep(name = "pre1", command = "pre"),
                            AssembleStep(
                                name = "asm1",
                                inputs = listOf("shared.asm"),
                                outputs = listOf("p1.prg"))),
                    dependsOn = listOf("shared"))
            val prog2 =
                Flow(
                    name = "prog2",
                    steps =
                        listOf(
                            CommandStep(name = "pre2", command = "pre"),
                            AssembleStep(
                                name = "asm2",
                                inputs = listOf("shared.asm"),
                                outputs = listOf("p2.prg"))),
                    dependsOn = listOf("shared"))
            val flows = listOf(shared, prog1, prog2)
            flows.size shouldBe 3
            flows[0].steps.size shouldBe 1
            flows[1].steps.size shouldBe 2
            flows[2].steps.size shouldBe 2
          }
        }
      }
    })
