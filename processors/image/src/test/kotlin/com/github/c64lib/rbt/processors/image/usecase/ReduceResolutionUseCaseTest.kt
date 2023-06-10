/*
MIT License

Copyright (c) 2018-2023 c64lib: The Ultimate Commodore 64 Library
Copyright (c) 2018-2023 Maciej Małecki

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
package com.github.c64lib.rbt.processors.image.usecase

import com.github.c64lib.rbt.processors.image.domain.Image
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class ReduceResolutionUseCaseTest :
    BehaviorSpec(
        {
          val useCase = ReduceResolutionUseCase()

          Given("an image of 4x4 and reduceY and reduceX both equal to 2") {
            val image = Image(4, 4)
            val command = ReduceResolutionCommand(image, 2, 2)

            When("the use case is applied") {
              val result = useCase.apply(command)

              Then("the result should be an image of 2x2") {
                result.width shouldBe 2
                result.height shouldBe 2
              }
            }
          }

          Given("an image of 10x10 and reduceY equal to 5 and reduceX equal to 2") {
            val image = Image(10, 10)
            val command = ReduceResolutionCommand(image, 5, 2)

            When("the use case is applied") {
              val result = useCase.apply(command)

              Then("the result should be an image of 5x2") {
                result.width shouldBe 5
                result.height shouldBe 2
              }
            }
          }

          Given("an image of 6x6 and reduceY and reduceX both equal to 1") {
            val image = Image(6, 6)
            val command = ReduceResolutionCommand(image, 1, 1)

            When("the use case is applied") {
              val result = useCase.apply(command)

              Then("the result should be an image of the same size 6x6") {
                result.width shouldBe 6
                result.height shouldBe 6
              }
            }
          }

          Given("an image of 6x6 and reduceY and reduceX both equal to 0") {
            val image = Image(6, 6)
            val command = ReduceResolutionCommand(image, 0, 0)

            Then("an exception should be thrown") {
              shouldThrow<IllegalArgumentException> { useCase.apply(command) }
            }
          }
        },
    )
