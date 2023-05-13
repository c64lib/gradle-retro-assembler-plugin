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

data class SplitImageCommand(val image: Image, val subImageWidth: Int, val subImageHeight: Int)

class SplitImageUseCase {
  fun apply(command: SplitImageCommand): Array<Image> {
    require(command.subImageWidth > 0) { "Subimage width must be greater than 0" }
    require(command.subImageHeight > 0) { "Subimage height must be greater than 0" }
    require(command.image.width % command.subImageWidth == 0) {
      "Image width must be a multiple of subimage width"
    }
    require(command.image.height % command.subImageHeight == 0) {
      "Image height must be a multiple of subimage height"
    }

    val numRows = command.image.height / command.subImageHeight
    val numCols = command.image.width / command.subImageWidth
    val subImages = mutableListOf<Image>()

    for (row in 0 until numRows) {
      for (col in 0 until numCols) {
        val left = col * command.subImageWidth
        val top = row * command.subImageHeight
        subImages.add(
            command.image.subImage(top, left, command.subImageWidth, command.subImageHeight))
      }
    }

    return subImages.toTypedArray()
  }
}
