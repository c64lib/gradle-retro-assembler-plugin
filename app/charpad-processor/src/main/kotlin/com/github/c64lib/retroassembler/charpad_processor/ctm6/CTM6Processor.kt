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
package com.github.c64lib.retroassembler.charpad_processor.ctm6

import com.github.c64lib.retroassembler.binutils.toWord
import com.github.c64lib.retroassembler.charpad_processor.CTMProcessor
import com.github.c64lib.retroassembler.charpad_processor.CharpadProcessor
import com.github.c64lib.retroassembler.charpad_processor.ColouringMethod
import com.github.c64lib.retroassembler.charpad_processor.InsufficientDataException
import com.github.c64lib.retroassembler.charpad_processor.ctm5.CTM5Flags
import com.github.c64lib.retroassembler.domain.processor.InputByteStream
import kotlin.experimental.and

internal class CTM6Processor(
    private val charpadProcessor: CharpadProcessor, private val version: Int
) : CTMProcessor {

  override fun process(inputByteStream: InputByteStream) {
    val header = readHeader(inputByteStream)

    // block 0 charset
    val charHeader = readBlockMarker(inputByteStream)
    val numChars = inputByteStream.read(2).toWord().value + 1
    if (numChars > 0) {
      val charData = inputByteStream.read(numChars * 8)
      charpadProcessor.processCharset { it.write(charData) }
    }

    // block 1 char attrs
    val charAttrHeader = readBlockMarker(inputByteStream)
    if (numChars > 0) {
      val charAttributeData = inputByteStream.read(numChars)
      charpadProcessor.processCharAttributes { it.write(charAttributeData) }
    }

    if (header.flags and CTM5Flags.TileSys.bit != 0.toByte()) {
      // block n tiles
      val tilesHeader = readBlockMarker(inputByteStream)
      val numTiles = inputByteStream.read(2).toWord().value + 1
      val tileWidth = inputByteStream.readByte()
      val tileHeight = inputByteStream.readByte()
      val tileData = inputByteStream.read(numTiles * tileWidth.toInt() * tileHeight.toInt() * 2)
      charpadProcessor.processTiles { it.write(tileData) }

      if (header.colouringMethod == ColouringMethod.PerTile.value) {
        // block n tile colours
        val tileColoursHeader = readBlockMarker(inputByteStream)
        val tileColoursData = inputByteStream.read(numTiles)
        charpadProcessor.processTileColours { it.write(tileColoursData) }
      }

      // block n tile tags
      val tileTagsHeader = readBlockMarker(inputByteStream)
      // TODO: tiles tags are ignored for now
      val tileTagsData = inputByteStream.read(numTiles)

      // block n tile names
      val tileNamesHeader = readBlockMarker(inputByteStream)
      // TODO: tile names are ignored for now
      val tileNamesData = readTileNames(inputByteStream, numTiles)
    }

    // block n map
    val mapHeader = readBlockMarker(inputByteStream)
    val mapWidth = inputByteStream.read(2).toWord().value
    val mapHeight = inputByteStream.read(2).toWord().value
    if (mapHeight > 0 && mapWidth > 0) {
      val mapData = inputByteStream.read(mapWidth * mapHeight * 2)
      charpadProcessor.processMap { it.write(mapWidth, mapHeight, mapData) }
    }
  }

  private fun readTileNames(inputByteStream: InputByteStream, numTiles: Int): Array<String> =
      (1..numTiles).map { readTileName(inputByteStream) }.toTypedArray()

  private fun readTileName(inputByteStream: InputByteStream): String {
    var i = 0
    var value = inputByteStream.readByte()
    val result = StringBuffer(32)

    while (i < 32 && value != 0.toByte()) {
      ++i
      result.append(value.toChar())
      value = inputByteStream.readByte()
    }
    if (i == 32) {
      value = inputByteStream.readByte()
      if (value != 0.toByte()) {
        throw InsufficientDataException(
            "Lack of null termination in 32 character tile name: $result")
      }
    }
    return result.toString()
  }

  private fun readHeader(inputByteStream: InputByteStream): CTM6Header {
    val screenColor = inputByteStream.readByte()
    val multicolor1 = inputByteStream.readByte()
    val multicolor2 = inputByteStream.readByte()
    if (version > 6) {
      // TODO bg4 is now ignored
      inputByteStream.readByte()
    }
    val charColor = inputByteStream.readByte()
    val colouringMethod = inputByteStream.readByte()
    if (version > 6) {
      // TODO ignore screen mode for now
      inputByteStream.readByte()
    }
    val flags = inputByteStream.readByte()
    return CTM6Header(
        screenColor = screenColor,
        multicolor1 = multicolor1,
        multicolor2 = multicolor2,
        charColor = charColor,
        colouringMethod = colouringMethod,
        flags = flags)
  }

  private fun readBlockMarker(inputByteStream: InputByteStream): Byte {
    val byte0 = inputByteStream.readByte()
    val byte1 = inputByteStream.readByte()
    return byte1 and 0x0f.toByte()
  }
}

internal enum class CTM6Flags(val bit: Byte) {
  MCM(0x01),
  TileSys(0x02)
}

internal data class CTM6Header(
    val screenColor: Byte,
    val multicolor1: Byte,
    val multicolor2: Byte,
    val charColor: Byte,
    val colouringMethod: Byte,
    val flags: Byte)
