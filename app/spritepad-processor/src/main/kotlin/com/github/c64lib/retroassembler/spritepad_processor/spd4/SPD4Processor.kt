/*
MIT License

Copyright (c) 2018-2023 c64lib: The Ultimate Commodore 64 Library

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
package com.github.c64lib.retroassembler.spritepad_processor.spd4

import com.github.c64lib.rbt.shared.binutils.toWord
import com.github.c64lib.rbt.shared.gradle.processor.InputByteStream
import com.github.c64lib.retroassembler.spritepad_processor.SPDProcessor
import com.github.c64lib.retroassembler.spritepad_processor.SpritepadProcessor

internal class SPD4Processor(
    private val spritepadProcessor: SpritepadProcessor,
    private val version: Int
) : SPDProcessor {
  override fun process(inputByteStream: InputByteStream) {
    val header = readHeader(inputByteStream)
    val spriteData = inputByteStream.read(header.spriteQuantity * 64)
    spritepadProcessor.processSprites { it.write(spriteData) }
  }

  private fun readHeader(inputByteStream: InputByteStream): SPD4Header {
    val flags = inputByteStream.readByte()
    val spriteQuantity = inputByteStream.read(2).toWord().value
    val tileQuantity = inputByteStream.read(2).toWord().value
    val spriteAnimationQuantity = inputByteStream.readByte() + 1
    val tileAnimationQuantity = inputByteStream.readByte() + 1
    val tileWidth = inputByteStream.readByte()
    val tileHeight = inputByteStream.readByte()
    val backgroundColour = inputByteStream.readByte()
    val multiColour1 = inputByteStream.readByte()
    val multiColour2 = inputByteStream.readByte()
    val spriteOverlayDistance = inputByteStream.read(2).toWord().value
    val tileOverlayDistance = inputByteStream.read(2).toWord().value
    return SPD4Header(
        flags = flags,
        spriteQuantity = spriteQuantity,
        tileQuantity = tileQuantity,
        spriteAnimationQuantity = spriteAnimationQuantity,
        tileAnimationQuantity = tileAnimationQuantity,
        tileWidth = tileWidth,
        tileHeight = tileHeight,
        backgroundColour = backgroundColour,
        multiColour1 = multiColour1,
        multiColour2 = multiColour2,
        spriteOverlayDistance = spriteOverlayDistance,
        tileOverlayDistance = tileOverlayDistance)
  }
}

internal data class SPD4Header(
    val flags: Byte,
    val spriteQuantity: Int,
    val tileQuantity: Int,
    val spriteAnimationQuantity: Int,
    val tileAnimationQuantity: Int,
    val tileWidth: Byte,
    val tileHeight: Byte,
    val backgroundColour: Byte,
    val multiColour1: Byte,
    val multiColour2: Byte,
    val spriteOverlayDistance: Int,
    val tileOverlayDistance: Int
)
