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
import com.github.c64lib.rbt.processors.image.usecase.port.WriteCharsetPort
import com.github.c64lib.rbt.shared.domain.Color
import java.io.File
import java.io.FileOutputStream
import normalize
import org.gradle.api.Project

class C64CharsetWriter(private val project: Project) : WriteCharsetPort {

  override fun write(image: Image, toFile: File, useBuildDir: Boolean) {
    require(image.width % 8 == 0) {
      "Image width must be a multiple of 8 for Commodore 64 hires charset"
    }
    require(image.height % 8 == 0) {
      "Image height must be a multiple of 8 for Commodore 64 hires charset"
    }

    val numBlocksX = image.width / 8
    val numBlocksY = image.height / 8
    val dataSize = numBlocksX * numBlocksY * 8
    val charsetData = ByteArray(dataSize)

    for (blockY in 0 until numBlocksY) {
      for (blockX in 0 until numBlocksX) {
        for (y in 0 until 8) {
          val yOffset = blockY * 8
          val xOffset = blockX * 8

          val byte =
              (pixelValue(image[xOffset, yOffset + y]) shl 7) or
                  (pixelValue(image[xOffset + 1, yOffset + y]) shl 6) or
                  (pixelValue(image[xOffset + 2, yOffset + y]) shl 5) or
                  (pixelValue(image[xOffset + 3, yOffset + y]) shl 4) or
                  (pixelValue(image[xOffset + 4, yOffset + y]) shl 3) or
                  (pixelValue(image[xOffset + 5, yOffset + y]) shl 2) or
                  (pixelValue(image[xOffset + 6, yOffset + y]) shl 1) or
                  pixelValue(image[xOffset + 7, yOffset + y])

          charsetData[blockY * numBlocksX * 8 + blockX * 8 + y] = byte.toByte()
        }
      }
    }

    FileOutputStream(normalize(project, toFile, useBuildDir)).use { it.write(charsetData) }
  }

  private fun pixelValue(color: Color) = if (color != Color(0, 0, 0, 255)) 1 else 0
}
