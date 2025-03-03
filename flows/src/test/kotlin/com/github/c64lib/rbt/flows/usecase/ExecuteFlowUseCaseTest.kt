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
package com.github.c64lib.rbt.flows.usecase

import com.github.c64lib.rbt.flows.domain.Flow
import com.github.c64lib.rbt.flows.domain.FlowStep
import com.github.c64lib.rbt.flows.domain.FlowStepContent
import com.github.c64lib.rbt.flows.domain.FlowStepModifier
import com.github.c64lib.rbt.flows.domain.FlowStepOutcome
import com.github.c64lib.rbt.flows.domain.MatchAllModifierScenario
import com.github.c64lib.rbt.flows.domain.ModifierExecutor
import com.github.c64lib.rbt.flows.usecase.port.ExecuteStepPort
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.File

class ExecuteFlowUseCaseTest :
    BehaviorSpec(
        {
          Given("an ExecuteFlowUseCase") {
            When("apply is called with a flow containing steps and modifiers") {
              Then("it executes each step and applies modifiers to outcomes") {
                val executeStepPort = mockk<ExecuteStepPort>()
                val useCase = ExecuteFlowUseCase(executeStepPort)

                val stepContent = mockk<FlowStepContent>()
                val modifierExecutor = mockk<ModifierExecutor>()
                val modifier =
                    FlowStepModifier(
                        modifierExecutor = modifierExecutor, scenario = MatchAllModifierScenario)

                val step = FlowStep(content = stepContent, modifiers = listOf(modifier))
                val outcome = FlowStepOutcome("id", File("foobar.txt"))

                val flow = Flow("flow1", emptyList(), listOf(step))
                val command = ExecuteFlowCommand(flow)

                every { step.content.name() } returns "step1"
                every { step.content.executor().execute() } returns setOf(outcome)
                every { executeStepPort.execute("step1", any()) } returns setOf(outcome)
                every { modifierExecutor.execute(any()) } returns Unit

                useCase.apply(command)

                verify { executeStepPort.execute("step1", any()) }
                verify { modifierExecutor.execute(any()) }
              }
            }

            When("apply is called with a flow containing no steps") {
              Then("it does nothing") {
                val executeStepPort = mockk<ExecuteStepPort>()
                val useCase = ExecuteFlowUseCase(executeStepPort)

                val flow = Flow("flow1", emptyList(), emptyList())
                val command = ExecuteFlowCommand(flow)

                useCase.apply(command)

                verify(exactly = 0) { executeStepPort.execute(any(), any()) }
              }
            }

            When("apply is called with a step that has no modifiers") {
              Then("it executes the step without applying any modifiers") {
                val executeStepPort = mockk<ExecuteStepPort>()
                val useCase = ExecuteFlowUseCase(executeStepPort)

                val step = mockk<FlowStep>()
                val flow = Flow("flow1", emptyList(), listOf(step))
                val command = ExecuteFlowCommand(flow)

                every { step.content.name() } returns "step1"
                every { step.content.executor().execute() } returns emptySet()
                every { executeStepPort.execute("step1", any()) } returns emptySet()
                every { step.modifiers } returns emptyList()

                useCase.apply(command)

                verify { executeStepPort.execute("step1", any()) }
              }
            }
          }
        },
    )
