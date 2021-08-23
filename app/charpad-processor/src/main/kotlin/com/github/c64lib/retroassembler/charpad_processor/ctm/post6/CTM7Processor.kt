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
import com.github.c64lib.retroassembler.binutils.toWord
import com.github.c64lib.retroassembler.charpad_processor.CTMHeader
import com.github.c64lib.retroassembler.charpad_processor.CharpadProcessor
import com.github.c64lib.retroassembler.charpad_processor.ColouringMethod
import com.github.c64lib.retroassembler.charpad_processor.colouringMethodFrom
import com.github.c64lib.retroassembler.charpad_processor.screenModeFrom
import kotlin.experimental.and

internal class CTM7Processor(charpadProcessor: CharpadProcessor) :
    BlockBasedCTMProcessor(charpadProcessor) {

  override fun process(inputByteStream: InputByteStream) {
    // CTM header
    val header = readHeader(inputByteStream)
    // block 0 charset
    val numChars = processCharsetBlock(inputByteStream)
    // block 1 char attrs
    processCharsetAttributesBlock(numChars, inputByteStream)

    var tileWidth: Byte? = null
    var tileHeight: Byte? = null

    if (header.flags and CTM7Flags.TileSys.bit != 0.toByte()) {
      // block n tiles
      val tilesHeader = readBlockMarker(inputByteStream)
      val numTiles = inputByteStream.read(2).toWord().value + 1
      tileWidth = inputByteStream.readByte()
      tileHeight = inputByteStream.readByte()
      val tileData = inputByteStream.read(numTiles * tileWidth.toInt() * tileHeight.toInt() * 2)
      charpadProcessor.processTiles { it.write(tileData) }

      if (colouringMethodFrom(header.colouringMethod) == ColouringMethod.PerTile) {
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
    val (mapWidth, mapHeight) = processMapBlock(inputByteStream)

    // all data here, process header
    charpadProcessor.processHeader {
      it.write(header.toHeader(tileWidth, tileHeight, mapWidth, mapHeight))
    }
  }

  private fun readHeader(inputByteStream: InputByteStream): CTM7Header {
    val screenColor = inputByteStream.readByte()
    val multicolor1 = inputByteStream.readByte()
    val multicolor2 = inputByteStream.readByte()
    val backgroundColour4 = inputByteStream.readByte()
    val charColor = inputByteStream.readByte()
    val colouringMethod = inputByteStream.readByte()
    val screenMode = inputByteStream.readByte()
    val flags = inputByteStream.readByte()
    return CTM7Header(
        screenColour = screenColor,
        multicolour1 = multicolor1,
        multicolour2 = multicolor2,
        backgroundColour4 = backgroundColour4,
        charColour = charColor,
        colouringMethod = colouringMethod,
        screenMode = screenMode,
        flags = flags)
  }
}

internal enum class CTM7Flags(val bit: Byte) {
  TileSys(0x01)
}

internal data class CTM7Header(
    val screenColour: Byte,
    val multicolour1: Byte,
    val multicolour2: Byte,
    val backgroundColour4: Byte,
    val charColour: Byte,
    val colouringMethod: Byte,
    val screenMode: Byte,
    val flags: Byte
) {

  fun toHeader(tileWidth: Byte?, tileHeight: Byte?, mapWidth: Int, mapHeight: Int): CTMHeader =
      CTMHeader(
          screenColour = screenColour,
          multicolour1 = multicolour1,
          multicolour2 = multicolour2,
          charColour = charColour,
          colouringMethod = colouringMethodFrom(colouringMethod),
          useTiles = flags and CTM7Flags.TileSys.bit != 0.toUnsignedByte(),
          screenMode = screenModeFrom(screenMode),
          tileWidth = tileWidth,
          tileHeight = tileHeight,
          mapWidth = mapWidth,
          mapHeight = mapHeight)
}
