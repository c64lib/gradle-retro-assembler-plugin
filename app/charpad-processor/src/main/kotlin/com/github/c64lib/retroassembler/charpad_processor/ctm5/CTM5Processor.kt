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
package com.github.c64lib.retroassembler.charpad_processor.ctm5

import com.github.c64lib.retroassembler.binutils.toWord
import com.github.c64lib.retroassembler.charpad_processor.CTMProcessor
import com.github.c64lib.retroassembler.charpad_processor.CharpadProcessor
import com.github.c64lib.retroassembler.charpad_processor.ColouringMethod
import com.github.c64lib.retroassembler.charpad_processor.Flags
import com.github.c64lib.retroassembler.domain.processor.InputByteStream
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.or

internal class CTM5Processor(private val charpadProcessor: CharpadProcessor) : CTMProcessor {
  override fun process(inputByteStream: InputByteStream) {
    val header = readHeader(inputByteStream)

    if (header.numChars > 0) {
      val charData = inputByteStream.read(header.numChars * 8)
      charpadProcessor.charsetProducers.forEach { it.write(charData) }

      val charAttributeData = inputByteStream.read(header.numChars)
      charpadProcessor.charAttributesProducers.forEach { it.write(charAttributeData) }
    }

    if (header.flags and CTM5Flags.TileSys.bit != 0.toByte()) {
      if (header.flags and CTM5Flags.CharEx.bit == 0.toByte()) {
        val tileData =
            inputByteStream.read(
                header.numTiles * header.tileWidth.toInt() * header.tileHeight.toInt() * 2)
        charpadProcessor.tileProducers.forEach { it.write(tileData) }
      }

      if (header.colouringMethod == ColouringMethod.PerTile.value) {
        val tileColoursData = inputByteStream.read(header.numTiles)
        charpadProcessor.tileColoursProducers.forEach { it.write(tileColoursData) }
      }
    }

    if (header.mapHeight > 0 && header.mapWidth > 0) {
      val mapData = inputByteStream.read(header.mapWidth * header.mapHeight * 2)
      charpadProcessor.mapProducers.forEach { it.write(header.mapWidth, header.mapHeight, mapData) }
    }
  }

  private fun readHeader(inputByteStream: InputByteStream): CTM5Header {
    val screenColor = inputByteStream.readByte()
    val multicolor1 = inputByteStream.readByte()
    val multicolor2 = inputByteStream.readByte()
    val charColor = inputByteStream.readByte()
    val colouringMethod = inputByteStream.readByte()
    val flags = inputByteStream.readByte()
    val numChars = inputByteStream.read(2).toWord().value + 1
    val numTiles = inputByteStream.read(2).toWord().value + 1
    val tileWidth = inputByteStream.readByte()
    val tileHeight = inputByteStream.readByte()
    val mapWidth = inputByteStream.read(2).toWord().value
    val mapHeight = inputByteStream.read(2).toWord().value
    return CTM5Header(
        screenColor = screenColor,
        multicolor1 = multicolor1,
        multicolor2 = multicolor2,
        charColor = charColor,
        colouringMethod = colouringMethod,
        flags = flags,
        numChars = numChars,
        numTiles = numTiles,
        tileWidth = tileWidth,
        tileHeight = tileHeight,
        mapWidth = mapWidth,
        mapHeight = mapHeight)
  }
}

internal enum class CTM5Flags(val bit: Byte) {
  TileSys(0x01),
  CharEx(0x02),
  MCM(0x04)
}

internal fun Flags.toCTM5Flag(): CTM5Flags =
    when (this) {
      Flags.TileSys -> CTM5Flags.TileSys
      Flags.MCM -> CTM5Flags.MCM
      Flags.CharEx -> CTM5Flags.CharEx
    }

fun toCTM5Byte(flags: EnumSet<Flags>): Byte =
    flags.fold(0.toByte()) { acc, flag -> acc or flag.toCTM5Flag().bit }

internal data class CTM5Header(
    val screenColor: Byte,
    val multicolor1: Byte,
    val multicolor2: Byte,
    val charColor: Byte,
    val colouringMethod: Byte,
    val flags: Byte,
    val numChars: Int,
    val numTiles: Int,
    val tileWidth: Byte,
    val tileHeight: Byte,
    val mapWidth: Int,
    val mapHeight: Int)
