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
import com.github.c64lib.rbt.shared.domain.Color
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class SplitImageUseCaseTest :
    BehaviorSpec({
      Given("A CutImageUseCase instance") {
        val splitImageUseCase = SplitImageUseCase()

        And("An Image instance with dimensions 100x50") {
          val image =
              Image(100, 50).apply {
                for (y in 0 until height) {
                  for (x in 0 until width) {
                    this[x, y] = Color(x % 256, y % 256, (x + y) % 256, 255)
                  }
                }
              }

          When("a CutImageCommand with valid parameters is provided") {
            val command = SplitImageCommand(image, 20, 25)
            val subImages = splitImageUseCase.apply(command)

            Then("it should return an array of Image objects with the correct dimensions") {
              subImages shouldHaveSize 10
              subImages.forEach { subImage ->
                subImage.width shouldBe 20
                subImage.height shouldBe 25
              }
            }

            Then("the subimages should contain the correct region from the original image") {
              for (i in subImages.indices) {
                val subImage = subImages[i]
                val offsetX = (i % 5) * 20
                val offsetY = (i / 5) * 25

                for (y in 0 until subImage.height) {
                  for (x in 0 until subImage.width) {
                    subImage[x, y] shouldBe image[x + offsetX, y + offsetY]
                  }
                }
              }
            }
          }

          When("a CutImageCommand with invalid parameters is provided") {
            Then("it should throw an exception") {
              val invalidCommand = SplitImageCommand(image, 0, 50)
              shouldThrow<IllegalArgumentException> { splitImageUseCase.apply(invalidCommand) }
            }
          }
        }
      }
    })
