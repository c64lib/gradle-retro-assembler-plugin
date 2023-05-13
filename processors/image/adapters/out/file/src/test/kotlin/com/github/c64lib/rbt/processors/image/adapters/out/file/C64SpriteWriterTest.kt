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

import com.github.c64lib.rbt.processors.image.domain.Image
import com.github.c64lib.rbt.shared.domain.Color
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files
import org.gradle.api.Project
import org.mockito.Mockito

class C64SpriteWriterTest :
    BehaviorSpec({
      fun createTestImage(): Image {
        val image = Image(24, 21)

        // Draw some example pixels
        image[0, 0] = Color(255, 255, 255, 255)
        image[7, 0] = Color(255, 255, 255, 255)
        image[10, 20] = Color(255, 255, 255, 255)

        return image
      }

      fun readSpriteData(file: File): ByteArray {
        return Files.readAllBytes(file.toPath())
      }

      Given("a C64SpriteWriter instance") {
        val project = Mockito.mock(Project::class.java)
        val writer = C64SpriteWriter(project)

        When("writing a valid image to a file") {
          val image = createTestImage()
          val outputFile = File.createTempFile("sprite_", ".bin")
          outputFile.deleteOnExit()

          writer.write(image, outputFile, false)

          Then("the resulting file should have the correct size and data") {
            val spriteData = readSpriteData(outputFile)

            spriteData.size shouldBe 64
            spriteData[0] shouldBe 0b10000001.toByte()
            spriteData[61] shouldBe 0b00100000.toByte()
            spriteData[62] shouldBe 0
            spriteData[63] shouldBe 0
          }
        }
      }
    })
