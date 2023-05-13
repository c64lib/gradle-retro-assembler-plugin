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
import com.github.c64lib.rbt.processors.image.usecase.port.ReadImagePort
import java.io.File

data class CutSourceImageCommand(
    val file: File,
    val width: Int,
    val height: Int,
    val left: Int = 0,
    val top: Int = 0
)

class CutSourceImageUseCase(private val readImagePort: ReadImagePort) {

  fun apply(command: CutSourceImageCommand): Array<Image> {
    val sourceImage = readImagePort.read(command.file)

    require(command.width > 0) { "Subimage width must be greater than 0" }
    require(command.height > 0) { "Subimage height must be greater than 0" }
    require(command.width + command.left <= sourceImage.width) {
      "Subimage width must be less than or equal to the image width"
    }
    require(command.height + command.top <= sourceImage.height) {
      "Subimage height must be less than or equal to the image height"
    }

    val subImages = mutableListOf<Image>()
    var x = command.left

    while (x + command.width <= sourceImage.width) {
      subImages.add(sourceImage.subImage(command.top, x, command.width, command.height))
      x += command.width
    }

    return subImages.toTypedArray()
  }
}
