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
package com.github.c64lib.rbt.processors.charpad.usecase.post6

import com.github.c64lib.rbt.processors.charpad.domain.CTMHeader
import com.github.c64lib.rbt.processors.charpad.domain.ColouringMethod
import com.github.c64lib.rbt.processors.charpad.domain.Dimensions
import com.github.c64lib.rbt.processors.charpad.domain.InvalidCTMFormatException
import com.github.c64lib.rbt.processors.charpad.domain.ScreenMode
import com.github.c64lib.rbt.processors.charpad.domain.colouringMethodFrom
import com.github.c64lib.rbt.processors.charpad.domain.screenModeFrom
import com.github.c64lib.rbt.processors.charpad.usecase.ProcessCharpadUseCase
import com.github.c64lib.rbt.shared.binutils.combineNybbles
import com.github.c64lib.rbt.shared.binutils.convertToHiNybbles
import com.github.c64lib.rbt.shared.binutils.isolateEachNth
import com.github.c64lib.rbt.shared.binutils.toUnsignedByte
import com.github.c64lib.rbt.shared.processor.InputByteStream
import kotlin.experimental.and

internal class CTM9Processor(charpadProcessor: ProcessCharpadUseCase) :
    BlockBasedCTMProcessor(charpadProcessor) {

  override fun process(inputByteStream: InputByteStream) {

    blockCounter = 0

    val header = readHeader(inputByteStream)
    val colouringMethod = colouringMethodFrom(header.colouringMethod)
    val screenMode = screenModeFrom(header.displayMode)
    val (lo, hi) = screenMode.getPaletteIndexes()
    val primaryColorIndex = screenMode.getPrimaryColorIndex()

    val coloursSize =
        when (screenMode) {
          ScreenMode.BitmapHires -> 2
          ScreenMode.BitmapMulticolor -> 3
          else -> 1
        }

    // block 0 charset
    val numChars = processCharsetBlock(inputByteStream)

    // block 1 char materials
    val charMaterialData = processCharsetMaterialsBlock(numChars, inputByteStream)

    if (colouringMethod == ColouringMethod.PerChar) {
      // block 2 char colours
      readBlockMarker(inputByteStream)
      if (numChars > 0) {
        val charColoursData = inputByteStream.read(numChars * coloursSize)
        processCharpadUseCase.processCharColours {
          it.write(isolateEachNth(charColoursData, coloursSize, primaryColorIndex))
        }
        processCharpadUseCase.processCharScreenColours {
          it.write(
              combineNybbles(
                  isolateEachNth(charColoursData, coloursSize, lo),
                  isolateEachNth(charColoursData, coloursSize, hi),
              ),
          )
        }
        if (charMaterialData != null) {
          processCharpadUseCase.processCharAttributes {
            it.write(
                combineNybbles(
                    isolateEachNth(charColoursData, coloursSize, primaryColorIndex),
                    charMaterialData,
                ),
            )
          }
        }
      }
    } else if (charMaterialData != null) {
      processCharpadUseCase.processCharAttributes { it.write(convertToHiNybbles(charMaterialData)) }
    }

    var tileWidth: Byte? = null
    var tileHeight: Byte? = null

    if (header.flags and CTM8Flags.TileSys.bit != 0.toByte()) {
      // block n tiles
      val (numTiles, width, height) = processTilesBlock(inputByteStream)
      tileWidth = width
      tileHeight = height
      ensureTileSizeLimits(tileWidth, tileHeight)

      if (colouringMethodFrom(header.colouringMethod) == ColouringMethod.PerTile) {
        // block n tile colours
        readBlockMarker(inputByteStream)
        val tileColoursData = inputByteStream.read(numTiles * coloursSize)
        processCharpadUseCase.processTileColours {
          it.write(isolateEachNth(tileColoursData, coloursSize, primaryColorIndex))
        }
        processCharpadUseCase.processTileScreenColours {
          it.write(
              combineNybbles(
                  isolateEachNth(tileColoursData, coloursSize, lo),
                  isolateEachNth(tileColoursData, coloursSize, hi),
              ),
          )
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
    processCharpadUseCase.processHeader {
      it.write(
          header.toHeader(tileWidth, tileHeight, mapWidth, mapHeight),
      )
    }
  }

  private fun ensureTileSizeLimits(tileWidth: Byte, tileHeight: Byte) {
    val limit = 10
    if (tileWidth > limit) {
      throw InvalidCTMFormatException("Tile width too big: $tileWidth.")
    }
    if (tileHeight > limit) {
      throw InvalidCTMFormatException("Tile height too big: $tileHeight.")
    }
  }

  private fun readHeader(inputByteStream: InputByteStream): CTM9Header {
    val displayMode = inputByteStream.readByte()
    val colouringMethod = inputByteStream.readByte()
    val flags = inputByteStream.readByte()

    val flexigridWidth = inputByteStream.readByte() + 256 * inputByteStream.readByte()
    val flexigridHeight = inputByteStream.readByte() + 256 * inputByteStream.readByte()
    inputByteStream.readByte() // ignore

    val screenColor = inputByteStream.readByte()
    val multicolor1 = inputByteStream.readByte()
    val multicolor2 = inputByteStream.readByte()
    val backgroundColor4 = inputByteStream.readByte()
    val colorBase0 = inputByteStream.readByte()
    val colorBase1 = inputByteStream.readByte()
    val colorBase2 = inputByteStream.readByte()
    val colorBase3: Byte = 0

    return CTM9Header(
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
        flags = flags,
        flexigridWidth = flexigridWidth,
        flexigridHeight = flexigridHeight,
    )
  }

  private fun ScreenMode.getPaletteIndexes(): ScreenMemoryPalette =
      when (this) {
        ScreenMode.TextHires,
        ScreenMode.TextMulticolor,
        ScreenMode.TextExtendedBackground -> ScreenMemoryPalette(0, 0)
        ScreenMode.BitmapHires -> ScreenMemoryPalette(0, 1)
        ScreenMode.BitmapMulticolor -> ScreenMemoryPalette(1, 2)
      }

  private fun ScreenMode.getPrimaryColorIndex(): Int = 0
}

internal enum class CTM9Flags(val bit: Byte) {
  TileSys(0x01)
}

internal data class CTM9Header(
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
    val flags: Byte,
    val flexigridWidth: Int,
    val flexigridHeight: Int,
) {

  fun toHeader(tileWidth: Byte?, tileHeight: Byte?, mapWidth: Int, mapHeight: Int): CTMHeader =
      CTMHeader(
          version = 9,
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
          mapDimensions = Dimensions(mapWidth, mapHeight),
      )
}
