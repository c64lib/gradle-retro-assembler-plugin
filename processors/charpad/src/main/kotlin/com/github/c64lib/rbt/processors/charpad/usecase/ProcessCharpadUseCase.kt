/*
MIT License

Copyright (c) 2018-2025 c64lib: The Ultimate Commodore 64 Library
Copyright (c) 2018-2025 Maciej Małecki

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
package com.github.c64lib.rbt.processors.charpad.usecase

import com.github.c64lib.rbt.processors.charpad.domain.CharAttributesProducer
import com.github.c64lib.rbt.processors.charpad.domain.CharColoursProducer
import com.github.c64lib.rbt.processors.charpad.domain.CharMaterialsProducer
import com.github.c64lib.rbt.processors.charpad.domain.CharScreenColoursProducer
import com.github.c64lib.rbt.processors.charpad.domain.CharsetProducer
import com.github.c64lib.rbt.processors.charpad.domain.HeaderProducer
import com.github.c64lib.rbt.processors.charpad.domain.InvalidCTMFormatException
import com.github.c64lib.rbt.processors.charpad.domain.MapProducer
import com.github.c64lib.rbt.processors.charpad.domain.TileColoursProducer
import com.github.c64lib.rbt.processors.charpad.domain.TileProducer
import com.github.c64lib.rbt.processors.charpad.domain.TileScreenColoursProducer
import com.github.c64lib.rbt.processors.charpad.domain.TileTagsProducer
import com.github.c64lib.rbt.processors.charpad.usecase.post6.CTM6Processor
import com.github.c64lib.rbt.processors.charpad.usecase.post6.CTM7Processor
import com.github.c64lib.rbt.processors.charpad.usecase.post6.CTM8Processor
import com.github.c64lib.rbt.processors.charpad.usecase.post6.CTM9Processor
import com.github.c64lib.rbt.processors.charpad.usecase.pre6.CTM5Processor
import com.github.c64lib.rbt.shared.processor.InputByteStream
import com.github.c64lib.rbt.shared.processor.OutputProducer
import com.github.c64lib.rbt.shared.processor.Processor

internal interface CTMProcessor : Processor

class ProcessCharpadUseCase(
    outputProducers: Collection<OutputProducer<*>>,
    private val ctm8PrototypeCompatibility: Boolean
) {

  private val charsetProducers: Collection<CharsetProducer> =
      outputProducers.filterIsInstance<CharsetProducer>()

  private val charAttributesProducers: Collection<CharAttributesProducer> =
      outputProducers.filterIsInstance<CharAttributesProducer>()

  private val charColoursProducers: Collection<CharColoursProducer> =
      outputProducers.filterIsInstance<CharColoursProducer>()

  private val charScreenColoursProducers: Collection<CharScreenColoursProducer> =
      outputProducers.filterIsInstance<CharScreenColoursProducer>()

  private val charMaterialsProducers: Collection<CharMaterialsProducer> =
      outputProducers.filterIsInstance<CharMaterialsProducer>()

  private val tileProducers: Collection<TileProducer> =
      outputProducers.filterIsInstance<TileProducer>()

  private val tileTagsProducers: Collection<TileTagsProducer> =
      outputProducers.filterIsInstance<TileTagsProducer>()

  private val tileColoursProducers: Collection<TileColoursProducer> =
      outputProducers.filterIsInstance<TileColoursProducer>()

  private val tileScreenColoursProducers: Collection<TileScreenColoursProducer> =
      outputProducers.filterIsInstance<TileScreenColoursProducer>()

  private val mapProducers: Collection<MapProducer> =
      outputProducers.filterIsInstance<MapProducer>()

  private val headerProducers: Collection<HeaderProducer> =
      outputProducers.filterIsInstance<HeaderProducer>()

  fun apply(inputByteStream: InputByteStream) =
      getProcessor(inputByteStream).process(inputByteStream)

  internal fun processCharset(action: (CharsetProducer) -> Unit) = charsetProducers.forEach(action)
  internal fun processCharAttributes(action: (CharAttributesProducer) -> Unit) =
      charAttributesProducers.forEach(action)
  internal fun processCharColours(action: (CharColoursProducer) -> Unit) =
      charColoursProducers.forEach(action)
  internal fun processCharScreenColours(action: (CharScreenColoursProducer) -> Unit) =
      charScreenColoursProducers.forEach(action)
  internal fun processCharMaterials(action: (CharMaterialsProducer) -> Unit) =
      charMaterialsProducers.forEach(action)
  internal fun processTiles(action: (TileProducer) -> Unit) = tileProducers.forEach(action)
  internal fun processTileTags(action: (TileTagsProducer) -> Unit) =
      tileTagsProducers.forEach(action)
  internal fun processTileColours(action: (TileColoursProducer) -> Unit) =
      tileColoursProducers.forEach(action)
  internal fun processTileScreenColours(action: (TileScreenColoursProducer) -> Unit) =
      tileScreenColoursProducers.forEach(action)
  internal fun processMap(action: (MapProducer) -> Unit) = mapProducers.forEach(action)
  internal fun processHeader(action: (HeaderProducer) -> Unit) = headerProducers.forEach(action)

  private fun getProcessor(inputByteStream: InputByteStream): CTMProcessor {
    val id = inputByteStream.read(3).map { it.toChar() }.joinToString(separator = "")
    if (id != "CTM") {
      throw InvalidCTMFormatException("CTM id is missing")
    }
    return when (val version = inputByteStream.readByte().toInt()) {
      5 -> CTM5Processor(this@ProcessCharpadUseCase)
      6 -> CTM6Processor(this@ProcessCharpadUseCase)
      7 -> CTM7Processor(this@ProcessCharpadUseCase)
      8,
      82 -> CTM8Processor(this@ProcessCharpadUseCase, version, ctm8PrototypeCompatibility)
      9 -> CTM9Processor(this@ProcessCharpadUseCase)
      else -> throw InvalidCTMFormatException("Unsupported version: $version")
    }
  }
}
