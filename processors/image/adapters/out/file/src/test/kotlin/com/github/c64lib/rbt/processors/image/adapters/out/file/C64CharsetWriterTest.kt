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
package com.github.c64lib.rbt.processors.image.adapters.out.file

import com.github.c64lib.rbt.processors.image.domain.Color
import com.github.c64lib.rbt.processors.image.domain.Image
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files

class C64CharsetWriterTest :
    BehaviorSpec({
      fun createTestImage(): Image {
        val image = Image(16, 8)

        // Draw some example pixels
        image[0, 0] = Color(255, 255, 255, 255)
        image[7, 0] = Color(255, 255, 255, 255)
        image[15, 7] = Color(255, 255, 255, 255)

        return image
      }

      fun readCharsetData(file: File): ByteArray {
        return Files.readAllBytes(file.toPath())
      }

      Given("a C64CharsetWriter instance") {
        val writer = C64CharsetWriter()

        When("writing a valid image to a file") {
          val image = createTestImage()
          val outputFile = File.createTempFile("charset_", ".bin")
          outputFile.deleteOnExit()

          writer.write(image, outputFile)

          Then("the resulting file should have the correct size and data") {
            val charsetData = readCharsetData(outputFile)

            charsetData.size shouldBe 16
            charsetData[0] shouldBe 0b10000001.toByte()
            charsetData[15] shouldBe 0b00000001.toByte()
          }
        }
      }
    })
