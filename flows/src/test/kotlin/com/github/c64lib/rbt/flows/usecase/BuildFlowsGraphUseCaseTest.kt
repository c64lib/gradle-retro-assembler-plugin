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
import com.github.c64lib.rbt.flows.domain.Flow
import com.github.c64lib.rbt.flows.usecase.BuildFlowsGraphCommand
import com.github.c64lib.rbt.flows.usecase.BuildFlowsGraphUseCase
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class BuildFlowsGraphUseCaseTest :
    BehaviorSpec(
        {
          Given("a BuildFlowsGraphUseCase") {
            val useCase = BuildFlowsGraphUseCase()

            When("apply is called with valid flows") {
              Then("it builds graph successfully") {
                val flow1 = Flow("flow1", listOf("flow2"), emptyList())
                val flow2 = Flow("flow2", emptyList(), emptyList())
                val command = BuildFlowsGraphCommand(listOf(flow1, flow2))

                val result = useCase.apply(command)

                result.size shouldBe 1
                result[0].flow.name shouldBe "flow1"
                result[0].followUps[0].flow.name shouldBe "flow2"
              }
            }

            When("apply is called with empty flows") {
              Then("it returns an empty list") {
                val command = BuildFlowsGraphCommand(emptyList())

                val result = useCase.apply(command)

                result.isEmpty() shouldBe true
              }
            }

            When("apply is called with non-existent follow-up") {
              Then("it throws IllegalArgumentException") {
                val flow1 = Flow("flow1", listOf("flow2"), emptyList())
                val command = BuildFlowsGraphCommand(listOf(flow1))

                val exception = shouldThrow<IllegalArgumentException> { useCase.apply(command) }

                exception.message shouldBe "Flow flow2 not found"
              }
            }

            When("apply is called with multiple follow-ups") {
              Then("it builds graph successfully") {
                val flow1 = Flow("flow1", listOf("flow2", "flow3"), emptyList())
                val flow2 = Flow("flow2", emptyList(), emptyList())
                val flow3 = Flow("flow3", emptyList(), emptyList())
                val command = BuildFlowsGraphCommand(listOf(flow1, flow2, flow3))

                val result = useCase.apply(command)

                result.size shouldBe 1
                result[0].flow.name shouldBe "flow1"
                result[0].followUps.size shouldBe 2
                result[0].followUps[0].flow.name shouldBe "flow2"
                result[0].followUps[1].flow.name shouldBe "flow3"
              }
            }

            When("apply is called with three layers of follow-ups") {
              Then("it builds graph successfully") {
                val flow1 = Flow("flow1", listOf("flow2"), emptyList())
                val flow2 = Flow("flow2", listOf("flow3"), emptyList())
                val flow3 = Flow("flow3", emptyList(), emptyList())
                val command = BuildFlowsGraphCommand(listOf(flow1, flow2, flow3))

                val result = useCase.apply(command)

                result.size shouldBe 1
                result[0].flow.name shouldBe "flow1"
                result[0].followUps.size shouldBe 1
                result[0].followUps[0].flow.name shouldBe "flow2"
                result[0].followUps[0].followUps.size shouldBe 1
                result[0].followUps[0].followUps[0].flow.name shouldBe "flow3"
              }
            }

            When("apply is called with cyclic flows") {
              Then("it throws IllegalArgumentException") {
                val flow1 = Flow("flow1", listOf("flow2"), emptyList())
                val flow2 = Flow("flow2", listOf("flow1"), emptyList())
                val command = BuildFlowsGraphCommand(listOf(flow1, flow2))

                val exception = shouldThrow<IllegalArgumentException> { useCase.apply(command) }

                exception.message shouldBe "Cycle detected at flow flow1"
              }
            }
          }
        },
    )
