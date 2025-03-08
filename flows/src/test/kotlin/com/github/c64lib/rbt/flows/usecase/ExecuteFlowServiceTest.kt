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
import com.github.c64lib.rbt.flows.usecase.port.ExecuteTaskPort
import com.github.c64lib.rbt.flows.usecase.port.TaskOutcome
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class ExecuteFlowServiceTest :
    BehaviorSpec(
        {
          Given("an ExecuteFlowService") {
            When("apply is called with a flow containing steps and modifiers") {
              Then("it executes each step and applies modifiers to outcomes") {
                val executeTaskPort = mockk<ExecuteTaskPort>()
                val useCase = ExecuteFlowService(executeTaskPort)

                val stepContent = mockk<FlowStepContent>()

                val step = FlowStep(content = stepContent)

                val flow = Flow("flow1", emptyList(), listOf(step))

                every { step.content.name() } returns "step1"
                every { step.content.executor().execute() } returns TaskOutcome.SUCCESS
                every { executeTaskPort.execute("step1", any()) } returns TaskOutcome.SUCCESS

                useCase.execute(flow)

                verify { executeTaskPort.execute("step1", any()) }
              }
            }

            When("apply is called with a flow containing no steps") {
              Then("it does nothing") {
                val executeTaskPort = mockk<ExecuteTaskPort>()
                val useCase = ExecuteFlowService(executeTaskPort)

                val flow = Flow("flow1", emptyList(), emptyList())

                useCase.execute(flow)

                verify(exactly = 0) { executeTaskPort.execute(any(), any()) }
              }
            }

            When("apply is called with a step that has no modifiers") {
              Then("it executes the step without applying any modifiers") {
                val executeTaskPort = mockk<ExecuteTaskPort>()
                val useCase = ExecuteFlowService(executeTaskPort)

                val step = mockk<FlowStep>()
                val flow = Flow("flow1", emptyList(), listOf(step))

                every { step.content.name() } returns "step1"
                every { step.content.executor().execute() } returns TaskOutcome.SUCCESS
                every { executeTaskPort.execute("step1", any()) } returns TaskOutcome.SUCCESS

                useCase.execute(flow)

                verify { executeTaskPort.execute("step1", any()) }
              }
            }
          }
        },
    )
