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
import com.github.c64lib.retroassembler.binutils.isolateHiNybbles
import com.github.c64lib.retroassembler.binutils.isolateLoNybbles
import com.github.c64lib.retroassembler.binutils.toWord
import com.github.c64lib.retroassembler.charpad_processor.CTMProcessor
import com.github.c64lib.retroassembler.charpad_processor.CharpadProcessor
import com.github.c64lib.retroassembler.charpad_processor.InsufficientDataException
import com.github.c64lib.retroassembler.charpad_processor.model.CTMHeader
import com.github.c64lib.retroassembler.charpad_processor.model.ColouringMethod
import com.github.c64lib.retroassembler.charpad_processor.model.Dimensions
import com.github.c64lib.retroassembler.charpad_processor.model.TileSetDimensions
import kotlin.experimental.and

internal abstract class BlockBasedCTMProcessor(val charpadProcessor: CharpadProcessor) :
    CTMProcessor {

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
      charpadProcessor.processCharset { it.write(charData) }
    }
    return numChars
  }

  protected fun processMapBlock(inputByteStream: InputByteStream): Dimensions<Int> {
    readBlockMarker(inputByteStream)
    val mapWidth = inputByteStream.read(2).toWord().value
    val mapHeight = inputByteStream.read(2).toWord().value
    if (mapHeight > 0 && mapWidth > 0) {
      val mapData = inputByteStream.read(mapWidth * mapHeight * 2)
      charpadProcessor.processMap { it.write(mapWidth, mapHeight, mapData) }
    }
    return Dimensions(mapWidth, mapHeight)
  }

  /** Only for v6, v7. */
  protected fun processCharsetAttributesBlock(
      colouringMethod: ColouringMethod, numChars: Int, inputByteStream: InputByteStream
  ) {
    readBlockMarker(inputByteStream)
    if (numChars > 0) {
      val charAttributeData = inputByteStream.read(numChars)
      charpadProcessor.processCharAttributes { it.write(charAttributeData) }
      if (colouringMethod == ColouringMethod.PerChar) {
        charpadProcessor.processCharColours { it.write(isolateLoNybbles(charAttributeData)) }
      }
      charpadProcessor.processCharMaterials { it.write(isolateHiNybbles(charAttributeData)) }
    }
  }

  protected fun processTilesBlock(inputByteStream: InputByteStream): TileSetDimensions {
    readBlockMarker(inputByteStream)
    val numTiles = inputByteStream.read(2).toWord().value + 1
    val tileWidth = inputByteStream.readByte()
    val tileHeight = inputByteStream.readByte()
    val tileData = inputByteStream.read(numTiles * tileWidth.toInt() * tileHeight.toInt() * 2)
    charpadProcessor.processTiles { it.write(tileData) }
    return TileSetDimensions(numTiles, tileWidth, tileHeight)
  }

  protected fun processTilesTagsBlock(numTiles: Int, inputByteStream: InputByteStream) {
    readBlockMarker(inputByteStream)
    val tileTagsData = inputByteStream.read(numTiles)
    charpadProcessor.processTileTags { it.write(tileTagsData) }
  }

  protected fun processTilesNamesBlock(numTiles: Int, inputByteStream: InputByteStream) {
    readBlockMarker(inputByteStream)
    // TODO: tile names are ignored for now
    val tileNamesData = readTileNames(inputByteStream, numTiles)
  }

  /** Only for v8. */
  protected fun processCharsetMaterialsBlock(
      numChars: Int, inputByteStream: InputByteStream
  ): ByteArray? {
    readBlockMarker(inputByteStream)
    return if (numChars > 0) {
      val charMaterialData = inputByteStream.read(numChars)
      charpadProcessor.processCharMaterials { it.write(charMaterialData) }
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

  protected fun readBlockMarker(inputByteStream: InputByteStream): Byte {
    inputByteStream.readByte()
    val byte1 = inputByteStream.readByte()
    return byte1 and 0x0f.toByte()
  }
}
