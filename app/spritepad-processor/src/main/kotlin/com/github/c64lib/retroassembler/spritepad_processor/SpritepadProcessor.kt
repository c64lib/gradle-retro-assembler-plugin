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
package com.github.c64lib.retroassembler.spritepad_processor

import com.github.c64lib.processor.commons.InputByteStream
import com.github.c64lib.processor.commons.OutputProducer
import com.github.c64lib.processor.commons.Processor
import com.github.c64lib.retroassembler.spritepad_processor.spd4.SPD4Processor

internal interface SPDProcessor : Processor

class SpritepadProcessor(outputProducers: Collection<OutputProducer<*>>) {

  private val spriteProducers: Collection<SpriteProducer> =
      outputProducers.filterIsInstance<SpriteProducer>()

  fun process(inputByteStream: InputByteStream) =
      getProcessor(inputByteStream).process(inputByteStream)

  internal fun processSprites(action: (SpriteProducer) -> Unit) = spriteProducers.forEach(action)

  private fun getProcessor(inputByteStream: InputByteStream): SPDProcessor {
    val id = inputByteStream.read(3).map { it.toChar() }.joinToString(separator = "")
    if (id != "SPD") {
      throw InvalidSPDFormatException("SPD id is missing")
    }
    return when (val version = inputByteStream.readByte().toInt()
    ) {
      4, 5 -> SPD4Processor(this, version)
      else -> throw InvalidSPDFormatException("Unsupported version: $version")
    }
  }
}
