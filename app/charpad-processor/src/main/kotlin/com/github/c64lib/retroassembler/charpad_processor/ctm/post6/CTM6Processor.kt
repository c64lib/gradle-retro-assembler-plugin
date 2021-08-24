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
package com.github.c64lib.retroassembler.charpad_processor.ctm.post6

import com.github.c64lib.processor.commons.InputByteStream
import com.github.c64lib.retroassembler.binutils.toUnsignedByte
import com.github.c64lib.retroassembler.charpad_processor.CharpadProcessor
import com.github.c64lib.retroassembler.charpad_processor.model.CTMHeader
import com.github.c64lib.retroassembler.charpad_processor.model.ColouringMethod
import com.github.c64lib.retroassembler.charpad_processor.model.Dimensions
import com.github.c64lib.retroassembler.charpad_processor.model.ScreenMode
import com.github.c64lib.retroassembler.charpad_processor.model.colouringMethodFrom
import kotlin.experimental.and

internal class CTM6Processor(charpadProcessor: CharpadProcessor) :
    BlockBasedCTMProcessor(charpadProcessor) {

  override fun process(inputByteStream: InputByteStream) {
    // CTM header
    val header = readHeader(inputByteStream)
    // block 0 charset
    val numChars = processCharsetBlock(inputByteStream)
    // block 1 char attrs
    processCharsetAttributesBlock(
        colouringMethodFrom(header.colouringMethod), numChars, inputByteStream)

    var tileWidth: Byte? = null
    var tileHeight: Byte? = null

    if (header.flags and CTM6Flags.TileSys.bit != 0.toByte()) {
      // block n tiles
      val (numTiles, width, height) = processTilesBlock(inputByteStream)
      tileWidth = width
      tileHeight = height

      if (colouringMethodFrom(header.colouringMethod) == ColouringMethod.PerTile) {
        // block n tile colours
        val tileColoursHeader = readBlockMarker(inputByteStream)
        val tileColoursData = inputByteStream.read(numTiles)
        charpadProcessor.processTileColours { it.write(tileColoursData) }
      }

      // block n tile tags
      processTilesTagsBlock(numTiles, inputByteStream)
      // block n tile names
      processTilesNamesBlock(numTiles, inputByteStream)
    }
    // block n map
    val (mapWidth, mapHeight) = processMapBlock(inputByteStream)

    charpadProcessor.processHeader {
      it.write(header.toHeader(tileWidth, tileHeight, mapWidth, mapHeight))
    }
  }

  private fun readHeader(inputByteStream: InputByteStream): CTM6Header {
    val screenColor = inputByteStream.readByte()
    val multicolor1 = inputByteStream.readByte()
    val multicolor2 = inputByteStream.readByte()
    val charColor = inputByteStream.readByte()
    val colouringMethod = inputByteStream.readByte()
    val flags = inputByteStream.readByte()
    return CTM6Header(
        screenColour = screenColor,
        multicolour1 = multicolor1,
        multicolour2 = multicolor2,
        charColour = charColor,
        colouringMethod = colouringMethod,
        flags = flags)
  }
}

internal enum class CTM6Flags(val bit: Byte) {
  MCM(0x01),
  TileSys(0x02)
}

internal data class CTM6Header(
    val screenColour: Byte,
    val multicolour1: Byte,
    val multicolour2: Byte,
    val charColour: Byte,
    val colouringMethod: Byte,
    val flags: Byte
) {

  fun toHeader(tileWidth: Byte?, tileHeight: Byte?, mapWidth: Int, mapHeight: Int): CTMHeader =
      CTMHeader(
          version = 6,
          backgroundColour0 = screenColour,
          backgroundColour1 = multicolour1,
          backgroundColour2 = multicolour2,
          backgroundColour3 = 0,
          charColour = charColour,
          colouringMethod = colouringMethodFrom(colouringMethod),
          screenMode =
              if (flags and CTM6Flags.MCM.bit != 0.toUnsignedByte()) {
                ScreenMode.TextMulticolor
              } else {
                ScreenMode.TextHires
              },
          tileDimensions =
              if (flags and CTM6Flags.TileSys.bit != 0.toUnsignedByte()) {
                Dimensions(tileWidth!!, tileHeight!!)
              } else {
                null
              },
          mapDimensions = Dimensions(mapWidth, mapHeight))
}
