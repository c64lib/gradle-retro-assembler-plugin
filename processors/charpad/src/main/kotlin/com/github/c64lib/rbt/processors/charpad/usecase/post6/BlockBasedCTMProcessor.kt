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

import com.github.c64lib.rbt.processors.charpad.domain.ColouringMethod
import com.github.c64lib.rbt.processors.charpad.domain.Dimensions
import com.github.c64lib.rbt.processors.charpad.domain.InsufficientDataException
import com.github.c64lib.rbt.processors.charpad.domain.InvalidCTMFormatException
import com.github.c64lib.rbt.processors.charpad.domain.TileSetDimensions
import com.github.c64lib.rbt.processors.charpad.usecase.CTMProcessor
import com.github.c64lib.rbt.processors.charpad.usecase.ProcessCharpadUseCase
import com.github.c64lib.rbt.shared.binutils.isolateHiNybbles
import com.github.c64lib.rbt.shared.binutils.isolateLoNybbles
import com.github.c64lib.rbt.shared.binutils.toUnsignedByte
import com.github.c64lib.rbt.shared.binutils.toUnsignedInt
import com.github.c64lib.rbt.shared.binutils.toWord
import com.github.c64lib.rbt.shared.processor.InputByteStream

internal abstract class BlockBasedCTMProcessor(val processCharpadUseCase: ProcessCharpadUseCase) :
    CTMProcessor {

  protected var blockCounter = 0

  /**
   * Process charset definition block.
   *
   * @return number of characters available in the block
   */
  protected fun processCharsetBlock(inputByteStream: InputByteStream): Int {
    readBlockMarker(inputByteStream)
    val numChars = inputByteStream.read(2).toWord().value + 1
    if (numChars > 0) {
      val charData = inputByteStream.read(numChars * 8)
      processCharpadUseCase.processCharset { it.write(charData) }
    }
    return numChars
  }

  protected fun processMapBlock(inputByteStream: InputByteStream): Dimensions<Int> {
    readBlockMarker(inputByteStream)
    val mapWidth = inputByteStream.read(2).toWord().value
    val mapHeight = inputByteStream.read(2).toWord().value
    ensureMapSizeLimits(mapWidth, mapHeight)
    if (mapHeight > 0 && mapWidth > 0) {
      val mapData = inputByteStream.read(mapWidth * mapHeight * 2)
      processCharpadUseCase.processMap { it.write(mapWidth, mapHeight, mapData) }
    }
    return Dimensions(mapWidth, mapHeight)
  }

  private fun ensureMapSizeLimits(mapWidth: Int, mapHeight: Int) {
    val limit = 8192
    if (mapWidth > limit) {
      throw InvalidCTMFormatException("Map width too big: $mapWidth.")
    }
    if (mapHeight > limit) {
      throw InvalidCTMFormatException("Map height too big: $mapHeight.")
    }
  }

  /** Only for v6, v7. */
  protected fun processCharsetAttributesBlock(
      colouringMethod: ColouringMethod,
      numChars: Int,
      inputByteStream: InputByteStream
  ) {
    readBlockMarker(inputByteStream)
    if (numChars > 0) {
      val charAttributeData = inputByteStream.read(numChars)
      processCharpadUseCase.processCharAttributes { it.write(charAttributeData) }
      if (colouringMethod == ColouringMethod.PerChar) {
        processCharpadUseCase.processCharColours { it.write(isolateLoNybbles(charAttributeData)) }
      }
      processCharpadUseCase.processCharMaterials { it.write(isolateHiNybbles(charAttributeData)) }
    }
  }

  protected fun processTilesBlock(inputByteStream: InputByteStream): TileSetDimensions {
    readBlockMarker(inputByteStream)
    val numTiles = inputByteStream.read(2).toWord().value + 1
    val tileWidth = inputByteStream.readByte()
    val tileHeight = inputByteStream.readByte()
    val tileData = inputByteStream.read(numTiles * tileWidth.toInt() * tileHeight.toInt() * 2)
    processCharpadUseCase.processTiles { it.write(tileData) }
    return TileSetDimensions(numTiles, tileWidth, tileHeight)
  }

  protected fun processTilesTagsBlock(numTiles: Int, inputByteStream: InputByteStream) {
    readBlockMarker(inputByteStream)
    val tileTagsData = inputByteStream.read(numTiles)
    processCharpadUseCase.processTileTags { it.write(tileTagsData) }
  }

  protected fun processTilesNamesBlock(numTiles: Int, inputByteStream: InputByteStream) {
    readBlockMarker(inputByteStream)
    // TODO: tile names are ignored for now
    val tileNamesData = readTileNames(inputByteStream, numTiles)
  }

  /** Only for v8. */
  protected fun processCharsetMaterialsBlock(
      numChars: Int,
      inputByteStream: InputByteStream
  ): ByteArray? {
    readBlockMarker(inputByteStream)
    return if (numChars > 0) {
      val charMaterialData = inputByteStream.read(numChars)
      processCharpadUseCase.processCharMaterials { it.write(charMaterialData) }
      charMaterialData
    } else {
      null
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

  protected fun readBlockMarker(inputByteStream: InputByteStream): Int {
    val byte0Pos = inputByteStream.readCounter()
    val byte0 = inputByteStream.readByte().toUnsignedInt()
    val byte0Mark = 0xDA.toUnsignedByte().toUnsignedInt()
    if (byte0 != byte0Mark) {
      throw InvalidCTMFormatException(
          "Unexpected block marker byte 0 found for block $blockCounter: $byte0 <> $byte0Mark at position #$byte0Pos.")
    }
    val byte1Pos = inputByteStream.readCounter()
    val byte1 = inputByteStream.readByte().toUnsignedInt()
    if ((byte1 and 0xF0) != 0xB0) {
      throw InvalidCTMFormatException(
          "Unexpected block marker byte 1 found for block $blockCounter: $byte1 at position #$byte1Pos.")
    }

    val count = byte1 and 0x0F
    if (count != blockCounter) {
      throw InvalidCTMFormatException(
          "Unexpected block count found for $blockCounter: $count at position #$byte1Pos.")
    }
    ++blockCounter
    return count
  }
}
