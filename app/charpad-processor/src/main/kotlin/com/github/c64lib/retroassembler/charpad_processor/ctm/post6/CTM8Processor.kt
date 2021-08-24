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
import com.github.c64lib.retroassembler.binutils.combineNybbles
import com.github.c64lib.retroassembler.binutils.convertToHiNybbles
import com.github.c64lib.retroassembler.binutils.isolateEachNth
import com.github.c64lib.retroassembler.binutils.toUnsignedByte
import com.github.c64lib.retroassembler.charpad_processor.CharpadProcessor
import com.github.c64lib.retroassembler.charpad_processor.model.CTMHeader
import com.github.c64lib.retroassembler.charpad_processor.model.ColouringMethod
import com.github.c64lib.retroassembler.charpad_processor.model.Dimensions
import com.github.c64lib.retroassembler.charpad_processor.model.ScreenMode
import com.github.c64lib.retroassembler.charpad_processor.model.colouringMethodFrom
import com.github.c64lib.retroassembler.charpad_processor.model.screenModeFrom
import kotlin.experimental.and

internal class CTM8Processor(charpadProcessor: CharpadProcessor) :
    BlockBasedCTMProcessor(charpadProcessor) {

  override fun process(inputByteStream: InputByteStream) {
    val header = readHeader(inputByteStream)
    val colouringMethod = colouringMethodFrom(header.colouringMethod)
    val screenMode = screenModeFrom(header.displayMode)
    val (lo, hi) = screenMode.getPaletteIndexes()
    val primaryColorIndex = screenMode.getPrimaryColorIndex()

    // block 0 charset
    val numChars = processCharsetBlock(inputByteStream)

    // block 1 char materials
    val charMaterialData = processCharsetMaterialsBlock(numChars, inputByteStream)

    if (colouringMethod == ColouringMethod.PerChar) {
      // block 2 char colours
      readBlockMarker(inputByteStream)
      if (numChars > 0) {
        val charColoursData = inputByteStream.read(numChars * 4)
        charpadProcessor.processCharColours {
          it.write(isolateEachNth(charColoursData, 4, primaryColorIndex))
        }
        charpadProcessor.processCharScreenColours {
          it.write(
              combineNybbles(
                  isolateEachNth(charColoursData, 4, lo), isolateEachNth(charColoursData, 4, hi)))
        }
        if (charMaterialData != null) {
          charpadProcessor.processCharAttributes {
            it.write(
                combineNybbles(
                    isolateEachNth(charColoursData, 4, primaryColorIndex), charMaterialData))
          }
        }
      }
    } else if (charMaterialData != null) {
      charpadProcessor.processCharAttributes { it.write(convertToHiNybbles(charMaterialData)) }
    }

    var tileWidth: Byte? = null
    var tileHeight: Byte? = null

    if (header.flags and CTM8Flags.TileSys.bit != 0.toByte()) {
      // block n tiles
      val (numTiles, width, height) = processTilesBlock(inputByteStream)
      tileWidth = width
      tileHeight = height

      if (colouringMethodFrom(header.colouringMethod) == ColouringMethod.PerTile) {
        // block n tile colours
        readBlockMarker(inputByteStream)
        val tileColoursData = inputByteStream.read(numTiles * 4)
        charpadProcessor.processTileColours {
          it.write(isolateEachNth(tileColoursData, 4, primaryColorIndex))
        }
        charpadProcessor.processTileScreenColours {
          it.write(
              combineNybbles(
                  isolateEachNth(tileColoursData, 4, lo), isolateEachNth(tileColoursData, 4, hi)))
        }
      }

      // block n tile tags
      processTilesTagsBlock(numTiles, inputByteStream)
      // block n tile names
      processTilesNamesBlock(numTiles, inputByteStream)
    }

    // block n map
    val (mapWidth, mapHeight) = processMapBlock(inputByteStream)

    // all data here, process header
    charpadProcessor.processHeader {
      it.write(header.toHeader(tileWidth, tileHeight, mapWidth, mapHeight))
    }
  }

  private fun readHeader(inputByteStream: InputByteStream): CTM8Header {
    val displayMode = inputByteStream.readByte()
    val colouringMethod = inputByteStream.readByte()
    val flags = inputByteStream.readByte()
    val screenColor = inputByteStream.readByte()
    val multicolor1 = inputByteStream.readByte()
    val multicolor2 = inputByteStream.readByte()
    val backgroundColor4 = inputByteStream.readByte()
    val colorBase0 = inputByteStream.readByte()
    val colorBase1 = inputByteStream.readByte()
    val colorBase2 = inputByteStream.readByte()
    val colorBase3 = inputByteStream.readByte()

    return CTM8Header(
        displayMode = displayMode,
        screenColour = screenColor,
        multicolour1 = multicolor1,
        multicolour2 = multicolor2,
        backgroundColour4 = backgroundColor4,
        charColour0 = colorBase0,
        charColour1 = colorBase1,
        charColour2 = colorBase2,
        charColour3 = colorBase3,
        colouringMethod = colouringMethod,
        flags = flags)
  }
}

internal enum class CTM8Flags(val bit: Byte) {
  TileSys(0x01)
}

internal data class CTM8Header(
    val displayMode: Byte,
    val screenColour: Byte,
    val multicolour1: Byte,
    val multicolour2: Byte,
    val backgroundColour4: Byte,
    val charColour0: Byte,
    val charColour1: Byte,
    val charColour2: Byte,
    val charColour3: Byte,
    val colouringMethod: Byte,
    val flags: Byte
) {

  fun toHeader(tileWidth: Byte?, tileHeight: Byte?, mapWidth: Int, mapHeight: Int): CTMHeader =
      CTMHeader(
          backgroundColour0 = screenColour,
          backgroundColour1 = multicolour1,
          backgroundColour2 = multicolour2,
          backgroundColour3 = backgroundColour4,
          charColour = charColour3,
          colouringMethod = colouringMethodFrom(colouringMethod),
          screenMode = screenModeFrom(displayMode),
          tileDimensions =
              if (flags and CTM7Flags.TileSys.bit != 0.toUnsignedByte()) {
                Dimensions(tileWidth!!, tileHeight!!)
              } else {
                null
              },
          mapDimensions = Dimensions(mapWidth, mapHeight))
}

internal data class ScreenMemoryPalette(val lo: Int, val hi: Int)

internal fun ScreenMode.getPaletteIndexes(): ScreenMemoryPalette =
    when (this) {
      ScreenMode.TextHires, ScreenMode.TextMulticolor, ScreenMode.TextExtendedBackground ->
          ScreenMemoryPalette(0, 0)
      ScreenMode.BitmapHires -> ScreenMemoryPalette(0, 3)
      ScreenMode.BitmapMulticolor -> ScreenMemoryPalette(1, 2)
    }

internal fun ScreenMode.getPrimaryColorIndex(): Int =
    when (this) {
      ScreenMode.TextHires, ScreenMode.TextMulticolor, ScreenMode.TextExtendedBackground -> 3
      ScreenMode.BitmapMulticolor -> 3
      ScreenMode.BitmapHires -> 1
    }
