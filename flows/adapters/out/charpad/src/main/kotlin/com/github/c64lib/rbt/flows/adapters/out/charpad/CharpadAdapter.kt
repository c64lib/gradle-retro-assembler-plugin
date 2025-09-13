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
package com.github.c64lib.rbt.flows.adapters.out.charpad

import com.github.c64lib.rbt.flows.domain.config.CharpadCommand
import com.github.c64lib.rbt.flows.domain.port.CharpadPort
import com.github.c64lib.rbt.processors.charpad.usecase.ProcessCharpadUseCase
import com.github.c64lib.rbt.shared.processor.InputByteStream
import java.io.FileInputStream
import java.io.InputStream

/**
 * Adapter implementation of CharpadPort that bridges flows domain to charpad processor module.
 *
 * This adapter translates CharpadCommand instances to ProcessCharpadUseCase calls while maintaining
 * hexagonal architecture boundaries. It supports all CTM format versions (5-9) and all output
 * producer types including charset, maps, tiles, attributes, colors, materials, and metadata.
 */
class CharpadAdapter(
    private val outputProducerFactory: CharpadOutputProducerFactory = CharpadOutputProducerFactory()
) : CharpadPort {

  override fun process(command: CharpadCommand) {
    try {
      // Create output producers based on command configuration and output files
      val outputProducers = outputProducerFactory.createOutputProducers(command)

      // Create ProcessCharpadUseCase with output producers and ctm8 compatibility flag
      val processCharpadUseCase =
          ProcessCharpadUseCase(
              outputProducers = outputProducers,
              ctm8PrototypeCompatibility = command.config.ctm8PrototypeCompatibility)

      // Create input stream from the CTM file
      val inputStream = FileInputByteStream(FileInputStream(command.inputFile))

      // Execute charpad processing
      processCharpadUseCase.apply(inputStream)
    } catch (e: Exception) {
      throw RuntimeException("Failed to process charpad file: ${command.inputFile.name}", e)
    }
  }

  /** InputByteStream implementation that wraps a FileInputStream for charpad processing. */
  private class FileInputByteStream(private val inputStream: InputStream) : InputByteStream {
    private var readCounter = 0

    override fun read(amount: Int): ByteArray {
      val buffer = ByteArray(amount)
      val size = inputStream.read(buffer)
      readCounter += size
      return buffer.copyOfRange(0, size)
    }

    override fun readCounter(): Int = readCounter
  }
}
