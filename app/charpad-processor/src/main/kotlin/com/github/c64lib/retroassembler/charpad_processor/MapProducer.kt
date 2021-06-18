/*
MIT License

Copyright (c) 2018-2021 c64lib: The Ultimate Commodore 64 Library

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
package com.github.c64lib.retroassembler.charpad_processor

import com.github.c64lib.retroassembler.domain.processor.BinaryProducer
import com.github.c64lib.retroassembler.domain.processor.Output
import com.github.c64lib.retroassembler.domain.shared.IllegalInputException

class MapProducer(
    private val leftTop: MapCoord, private val rightBottom: MapCoord, output: Output<ByteArray>
) : BinaryProducer(output) {

  fun write(width: Int, height: Int, data: ByteArray) =
      write(cutMap(width, height, leftTop, calcRightBottom(width, height), data))

  private fun cutMap(
      width: Int, height: Int, leftTop: MapCoord, rightBottom: MapCoord, data: ByteArray
  ): ByteArray =
      checkInput(width, height, leftTop, rightBottom) { rightBottomAdjusted ->
        cutMapLeftRight(
            ByteArray(0),
            width,
            leftTop.x,
            rightBottomAdjusted.x,
            data.copyOfRange(leftTop.y * width * 2, rightBottomAdjusted.y * width * 2))
      }

  private fun checkInput(
      width: Int,
      height: Int,
      leftTop: MapCoord,
      rightBottom: MapCoord,
      perform: (rightBottomAdjusted: MapCoord) -> ByteArray
  ): ByteArray =
      when {
        width < 1 -> throw IllegalInputException("Width < 1: $width")
        height < 1 -> throw IllegalInputException("Height < 1: $height")
        leftTop.x < 0 -> throw IllegalInputException("Left < 0: ${leftTop.x}")
        rightBottom.x <= leftTop.x ->
            throw IllegalInputException("Right <= Left: ${rightBottom.x} <= ${leftTop.x}")
        leftTop.y < 0 -> throw IllegalInputException("Top < 0: ${leftTop.y}")
        rightBottom.y <= leftTop.y ->
            throw IllegalInputException("Bottom <= Top: ${rightBottom.y} <= ${leftTop.y}")
        else -> perform.invoke(adjust(width, height, rightBottom))
      }

  private fun adjust(width: Int, height: Int, rightBottom: MapCoord): MapCoord =
      MapCoord(
          if (rightBottom.x > width) {
            width
          } else {
            rightBottom.x
          },
          if (rightBottom.y > height) {
            height
          } else {
            rightBottom.y
          })

  private tailrec fun cutMapLeftRight(
      prefix: ByteArray, width: Int, leftMargin: Int, rightMargin: Int, data: ByteArray
  ): ByteArray =
      if (data.size < width * 2) {
        prefix
      } else {
        cutMapLeftRight(
            prefix + data.copyOfRange(leftMargin * 2, rightMargin * 2),
            width,
            leftMargin,
            rightMargin,
            data.copyOfRange(width * 2, data.size))
      }

  private fun calcRightBottom(width: Int, height: Int): MapCoord =
      rightBottom ?: MapCoord(width, height)
}
