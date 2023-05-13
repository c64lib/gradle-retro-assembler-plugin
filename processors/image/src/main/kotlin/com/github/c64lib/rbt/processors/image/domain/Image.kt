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

import com.github.c64lib.rbt.shared.domain.Color

class Image(val width: Int, val height: Int) {

  private val pixels = Array(height) { Array(width) { Color(0, 0, 0, 255) } }

  operator fun get(x: Int, y: Int): Color {
    require(x in 0 until width) { "x coordinate out of bounds" }
    require(y in 0 until height) { "y coordinate out of bounds" }
    return pixels[y][x]
  }

  operator fun set(x: Int, y: Int, color: Color) {
    require(x in 0 until width) { "x coordinate out of bounds" }
    require(y in 0 until height) { "y coordinate out of bounds" }
    pixels[y][x] = color
  }

  fun subImage(top: Int, left: Int, width: Int, height: Int): Image {
    require(top in 0 until this.height) { "Top coordinate out of bounds" }
    require(left in 0 until this.width) { "Left coordinate out of bounds" }
    require(top + height <= this.height) { "Height out of bounds" }
    require(left + width <= this.width) { "Width out of bounds" }

    val newImage = Image(width, height)
    for (y in 0 until height) {
      for (x in 0 until width) {
        newImage[x, y] = this[left + x, top + y]
      }
    }
    return newImage
  }

  fun extend(newWidth: Int, newHeight: Int, fillColor: Color = Color(0, 0, 0, 255)): Image {
    require(newWidth >= width) { "New width must be greater or equal to the current width" }
    require(newHeight >= height) { "New height must be greater or equal to the current height" }

    val newImage = Image(newWidth, newHeight)

    // Copy original image pixels
    for (y in 0 until height) {
      for (x in 0 until width) {
        newImage[x, y] = this[x, y]
      }
    }

    // Fill the extended area with the specified color
    for (y in 0 until newHeight) {
      for (x in 0 until newWidth) {
        if (x >= width || y >= height) {
          newImage[x, y] = fillColor
        }
      }
    }

    return newImage
  }

  fun dump() {
    for (y in 0 until height) {
      for (x in 0 until width) {
        print(
            if (pixels[y][x] == Color(0, 0, 0, 255)) {
              "."
            } else {
              "#"
            },
        )
      }
      println()
    }
  }
}
