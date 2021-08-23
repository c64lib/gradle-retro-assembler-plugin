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
package com.github.c64lib.retroassembler.charpad_processor

import com.github.c64lib.retroassembler.binutils.concat
import com.github.c64lib.retroassembler.binutils.wordOf
import com.github.c64lib.retroassembler.charpad_processor.ctm.pre6.toCTM5Flags
import com.github.c64lib.retroassembler.charpad_processor.model.CTMHeader

internal class CTM5ByteArrayMock(
    header: CTMHeader,
    charset: ByteArray,
    charAttributes: ByteArray,
    tiles: ByteArray,
    tileColours: ByteArray,
    map: ByteArray
) {

  private val signature = "CTM".toByteArray() concat byteArrayOf(5.toByte())
  private val headerBytes =
      byteArrayOf(
          header.backgroundColour0,
          header.backgroundColour1,
          header.backgroundColour2,
          header.charColour,
          header.colouringMethod.value,
          header.toCTM5Flags()) concat
          wordOf(charset.size / 8 - 1).toByteArray() concat
          wordOf(tiles.size / 2 / (header.tileWidth ?: 1) / (header.tileHeight ?: 1) - 1)
              .toByteArray() concat
          byteArrayOf(header.tileWidth ?: 1, header.tileHeight ?: 1) concat
          wordOf(header.mapWidth).toByteArray() concat
          wordOf(header.mapHeight).toByteArray()

  val bytes: ByteArray =
      signature concat
          headerBytes concat
          charset concat
          charAttributes concat
          tiles concat
          tileColours concat
          map
}
