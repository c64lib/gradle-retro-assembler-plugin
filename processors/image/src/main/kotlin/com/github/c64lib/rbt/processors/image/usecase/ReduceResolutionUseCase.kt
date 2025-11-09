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
package com.github.c64lib.rbt.processors.image.usecase

import com.github.c64lib.rbt.processors.image.domain.Image

data class ReduceResolutionCommand(val image: Image, val reduceY: Int, val reduceX: Int)

class ReduceResolutionUseCase {
  fun apply(command: ReduceResolutionCommand): Image {
    require(command.reduceY > 0) { "reduceY must be greater than 0" }
    require(command.reduceX > 0) { "reduceX must be greater than 0" }

    val newWidth = command.image.width / command.reduceX
    val newHeight = command.image.height / command.reduceY
    val newImage = Image(newWidth, newHeight)

    for (y in 0 until newHeight) {
      for (x in 0 until newWidth) {
        newImage[x, y] = command.image[x * command.reduceX, y * command.reduceY]
      }
    }

    return newImage
  }
}
