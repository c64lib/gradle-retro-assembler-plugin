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
import com.github.c64lib.retroassembler.binutils.isolateEachNth
import com.github.c64lib.retroassembler.charpad_processor.CharpadProcessor
import com.github.c64lib.retroassembler.charpad_processor.ColouringMethod
import com.github.c64lib.retroassembler.charpad_processor.ScreenMode
import com.github.c64lib.retroassembler.charpad_processor.colouringMethodFrom
import com.github.c64lib.retroassembler.charpad_processor.screenModeFrom
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
      val charColours = readBlockMarker(inputByteStream)
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
    }

    if (header.flags and CTM8Flags.TileSys.bit != 0.toByte()) {
      // block n tiles
      val (numTiles, _, _) = processTilesBlock(inputByteStream)

      if (colouringMethodFrom(header.colouringMethod) == ColouringMethod.PerTile) {
        // block n tile colours
        val tileColoursHeader = readBlockMarker(inputByteStream)
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
    processMapBlock(inputByteStream)
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
        screenColor = screenColor,
        multicolor1 = multicolor1,
        multicolor2 = multicolor2,
        backgroundColor4 = backgroundColor4,
        charColor0 = colorBase0,
        charColor1 = colorBase1,
        charColor2 = colorBase2,
        charColor3 = colorBase3,
        colouringMethod = colouringMethod,
        flags = flags)
  }
}

internal enum class CTM8Flags(val bit: Byte) {
  TileSys(0x01)
}

internal data class CTM8Header(
    val displayMode: Byte,
    val screenColor: Byte,
    val multicolor1: Byte,
    val multicolor2: Byte,
    val backgroundColor4: Byte,
    val charColor0: Byte,
    val charColor1: Byte,
    val charColor2: Byte,
    val charColor3: Byte,
    val colouringMethod: Byte,
    val flags: Byte)

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
