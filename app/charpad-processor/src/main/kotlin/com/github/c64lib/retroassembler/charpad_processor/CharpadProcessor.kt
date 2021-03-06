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

import com.github.c64lib.retroassembler.charpad_processor.ctm5.CTM5Processor
import com.github.c64lib.retroassembler.charpad_processor.ctm6.CTM6Processor
import com.github.c64lib.retroassembler.domain.processor.InputByteStream
import com.github.c64lib.retroassembler.domain.processor.OutputProducer
import com.github.c64lib.retroassembler.domain.processor.Processor

internal interface CTMProcessor : Processor

class CharpadProcessor(outputProducers: Collection<OutputProducer<*>>) {

  private val charsetProducers: Collection<CharsetProducer> =
      outputProducers.filterIsInstance<CharsetProducer>()

  private val charAttributesProducers: Collection<CharAttributesProducer> =
      outputProducers.filterIsInstance<CharAttributesProducer>()

  private val tileProducers: Collection<TileProducer> =
      outputProducers.filterIsInstance<TileProducer>()

  private val tileColoursProducers: Collection<TileColoursProducer> =
      outputProducers.filterIsInstance<TileColoursProducer>()

  private val mapProducers: Collection<MapProducer> =
      outputProducers.filterIsInstance<MapProducer>()

  fun process(inputByteStream: InputByteStream) =
      getProcessor(inputByteStream).process(inputByteStream)

  internal fun processCharset(action: (CharsetProducer) -> Unit) = charsetProducers.forEach(action)
  internal fun processCharAttributes(action: (CharAttributesProducer) -> Unit) =
      charAttributesProducers.forEach(action)
  internal fun processTiles(action: (TileProducer) -> Unit) = tileProducers.forEach(action)
  internal fun processTileColours(action: (TileColoursProducer) -> Unit) =
      tileColoursProducers.forEach(action)
  internal fun processMap(action: (MapProducer) -> Unit) = mapProducers.forEach(action)

  private fun getProcessor(inputByteStream: InputByteStream): CTMProcessor {
    val id = inputByteStream.read(3).map { it.toChar() }.joinToString(separator = "")
    if (id != "CTM") {
      throw InvalidCTMFormatException("CTM id is missing")
    }
    return when (val version = inputByteStream.readByte().toInt()
    ) {
      5 -> CTM5Processor(this@CharpadProcessor)
      6, 7 -> CTM6Processor(this@CharpadProcessor, version)
      else -> throw InvalidCTMFormatException("Unsupported version: $version")
    }
  }
}
