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
package com.github.c64lib.rbt.processors.image.adapters.out.png

import com.github.c64lib.rbt.shared.domain.Color
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.io.File

class PngImageReaderTest :
    BehaviorSpec(
        {
          val imageReader = ReadPngImageAdapter()

          arrayOf("/test-png.png", "/test-png-2.png").forEach { fileName ->
            Given("a PNG file from resources") {
              val pngFile = File(javaClass.getResource(fileName).file)

              When("the image is read using PngImageReader") {
                val image = imageReader.read(pngFile)

                Then("the returned Image object should not be null and have valid dimensions") {
                  image shouldNotBe null
                  image.width shouldBe 32 * 4
                  image.height shouldBe 32
                }

                listOf(Pair(0, 0), Pair(12, 0), Pair(14, 1), Pair(6, 16)).forEach {
                  Then(
                      "the returned Image object should have proper transparent pixel at [${it.first}, ${it.second}]",
                  ) {
                    image[it.first, it.second] shouldBe Color(0, 0, 0, 255)
                  }
                }

                listOf(Pair(10, 0), Pair(11, 0), Pair(9, 1), Pair(7, 2)).forEach {
                  Then(
                      "the returned Image object should have proper set pixel at [${it.first}, ${it.second}]",
                  ) {
                    image[it.first, it.second] shouldBe Color(255, 255, 255, 255)
                  }
                }
              }
            }
          }
        },
    )
