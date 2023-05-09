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
package com.github.c64lib.rbt.processors.image.domain

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe

class ImageTest :
    BehaviorSpec({
      Given("An Image instance with dimensions 100x50") {
        val image = Image(100, 50)

        When("checking dimensions") {
          Then("the width should be 100 and the height should be 50") {
            image.width shouldBeExactly 100
            image.height shouldBeExactly 50
          }
        }

        When("setting and getting pixel colors") {
          val color = Color(100, 200, 150, 255)
          image[10, 20] = color

          Then("the color should be set and retrieved correctly") { image[10, 20] shouldBe color }
        }

        When("creating a subImage") {
          // Fill the image with unique colors for demonstration purposes
          for (y in 0 until image.height) {
            for (x in 0 until image.width) {
              image[x, y] = Color(x % 256, y % 256, (x + y) % 256, 255)
            }
          }

          val subImage = image.subImage(10, 20, 30, 15)

          Then("the subImage should have the correct dimensions") {
            subImage.width shouldBeExactly 30
            subImage.height shouldBeExactly 15
          }

          Then("the subImage should contain the correct region from the original image") {
            for (y in 0 until subImage.height) {
              for (x in 0 until subImage.width) {
                subImage[x, y] shouldBe image[x + 20, y + 10]
              }
            }
          }
        }

        When("extending the image") {
          // Fill the image with unique colors for demonstration purposes
          for (y in 0 until image.height) {
            for (x in 0 until image.width) {
              image[x, y] = Color(x % 256, y % 256, (x + y) % 256, 255)
            }
          }

          val fillColor = Color(0, 0, 0, 255)
          val extendedImage = image.extend(150, 80, fillColor)

          Then("the extended image should have the correct dimensions") {
            extendedImage.width shouldBeExactly 150
            extendedImage.height shouldBeExactly 80
          }

          Then(
              "the extended image should contain the original image and the fill color in the extended area") {
                for (y in 0 until extendedImage.height) {
                  for (x in 0 until extendedImage.width) {
                    if (x < image.width && y < image.height) {
                      extendedImage[x, y] shouldBe image[x, y]
                    } else {
                      extendedImage[x, y] shouldBe fillColor
                    }
                  }
                }
              }
        }
      }
    })
