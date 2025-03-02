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
package com.github.c64lib.rbt.processors.image.adapters.out.file

import com.github.c64lib.rbt.processors.image.domain.Image
import com.github.c64lib.rbt.processors.image.usecase.port.WriteSpritePort
import com.github.c64lib.rbt.shared.domain.Color
import java.io.File
import java.io.FileOutputStream
import normalize
import org.gradle.api.Project

class C64SpriteWriter(
    private val project: Project,
) : WriteSpritePort {
  override fun write(image: Image, toFile: File, useBuildDir: Boolean) {
    require(image.width == 24) { "Sprite width must be 24 pixels for Commodore 64 hires sprites" }
    require(image.height == 21) { "Sprite height must be 21 pixels for Commodore 64 hires sprites" }

    val spriteData = ByteArray(64)

    for (y in 0 until 21) {
      for (x in 0 until 24 step 8) {
        val byte =
            (pixelValue(image[x, y]) shl 7) or
                (pixelValue(image[x + 1, y]) shl 6) or
                (pixelValue(image[x + 2, y]) shl 5) or
                (pixelValue(image[x + 3, y]) shl 4) or
                (pixelValue(image[x + 4, y]) shl 3) or
                (pixelValue(image[x + 5, y]) shl 2) or
                (pixelValue(image[x + 6, y]) shl 1) or
                pixelValue(image[x + 7, y])

        spriteData[y * 3 + x / 8] = byte.toByte()
      }
    }

    FileOutputStream(normalize(project, toFile, useBuildDir)).use { it.write(spriteData) }
  }

  private fun pixelValue(color: Color): Int {
    return if (color != Color(0, 0, 0, 255)) 1 else 0
  }
}
