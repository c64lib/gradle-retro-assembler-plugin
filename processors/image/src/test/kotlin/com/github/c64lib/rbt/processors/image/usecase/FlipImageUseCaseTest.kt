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
import com.github.c64lib.rbt.shared.domain.Axis
import com.github.c64lib.rbt.shared.domain.Color
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class FlipImageUseCaseTest :
    BehaviorSpec({
      val useCase = FlipImageUseCase()

      Given("A 2x2 image with distinct colors at each pixel") {
        val image = Image(2, 2)
        image[0, 0] = Color(255, 0, 0, 255)
        image[1, 0] = Color(0, 255, 0, 255)
        image[0, 1] = Color(0, 0, 255, 255)
        image[1, 1] = Color(255, 255, 255, 255)

        When("flip image along the X axis") {
          val flipped = useCase.apply(FlipImageCommand(image, Axis.X))

          Then("the image should be flipped along the X axis") {
            flipped[0, 0] shouldBe Color(0, 0, 255, 255)
            flipped[1, 0] shouldBe Color(255, 255, 255, 255)
            flipped[0, 1] shouldBe Color(255, 0, 0, 255)
            flipped[1, 1] shouldBe Color(0, 255, 0, 255)
          }
        }

        When("flip image along the Y axis") {
          val flipped = useCase.apply(FlipImageCommand(image, Axis.Y))

          Then("the image should be flipped along the Y axis") {
            flipped[0, 0] shouldBe Color(0, 255, 0, 255)
            flipped[1, 0] shouldBe Color(255, 0, 0, 255)
            flipped[0, 1] shouldBe Color(255, 255, 255, 255)
            flipped[1, 1] shouldBe Color(0, 0, 255, 255)
          }
        }

        When("flip image along both axes") {
          val flipped = useCase.apply(FlipImageCommand(image, Axis.BOTH))

          Then("the image should be flipped along both axes") {
            flipped[0, 0] shouldBe Color(255, 255, 255, 255)
            flipped[1, 0] shouldBe Color(0, 0, 255, 255)
            flipped[0, 1] shouldBe Color(0, 255, 0, 255)
            flipped[1, 1] shouldBe Color(255, 0, 0, 255)
          }
        }
      }
    })
