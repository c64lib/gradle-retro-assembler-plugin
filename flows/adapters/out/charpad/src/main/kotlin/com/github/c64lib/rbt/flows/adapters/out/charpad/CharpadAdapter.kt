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

import com.github.c64lib.rbt.flows.domain.FlowValidationException
import com.github.c64lib.rbt.flows.domain.config.CharpadCommand
import com.github.c64lib.rbt.flows.domain.port.CharpadPort
import com.github.c64lib.rbt.processors.charpad.domain.InsufficientDataException
import com.github.c64lib.rbt.processors.charpad.domain.InvalidCTMFormatException
import com.github.c64lib.rbt.processors.charpad.usecase.ProcessCharpadUseCase
import com.github.c64lib.rbt.shared.processor.InputByteStream
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
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
      // Validate input file exists and is readable
      validateInputFile(command.inputFile)

      // Create output producers based on command configuration and output files
      val outputProducers = outputProducerFactory.createOutputProducers(command)

      // Validate that at least one output producer was created
      if (outputProducers.isEmpty()) {
        throw FlowValidationException(
            "No output producers configured for charpad processing. " +
                "At least one output file must be specified with a recognized type " +
                "(charset, map, tiles, header, etc.)")
      }

      // Create ProcessCharpadUseCase with output producers and ctm8 compatibility flag
      val processCharpadUseCase =
          ProcessCharpadUseCase(
              outputProducers = outputProducers,
              ctm8PrototypeCompatibility = command.config.ctm8PrototypeCompatibility)

      // Create input stream from the CTM file
      val inputStream = FileInputByteStream(FileInputStream(command.inputFile))

      // Execute charpad processing
      processCharpadUseCase.apply(inputStream)
    } catch (e: FlowValidationException) {
      // Re-throw flows validation exceptions as-is
      throw e
    } catch (e: InvalidCTMFormatException) {
      // Map charpad-specific CTM format errors to flows validation errors
      throw FlowValidationException(
          "Invalid CTM file format in '${command.inputFile.name}': ${e.message}. " +
              "Ensure the file is a valid Charpad CTM file (versions 5-9 supported).")
    } catch (e: InsufficientDataException) {
      // Map charpad-specific data errors to flows validation errors
      throw FlowValidationException(
          "Insufficient data in CTM file '${command.inputFile.name}': ${e.message}. " +
              "The CTM file appears to be corrupted or truncated.")
    } catch (e: FileNotFoundException) {
      // Map file not found errors to flows validation errors
      throw FlowValidationException(
          "CTM input file not found: '${command.inputFile.absolutePath}'. " +
              "Verify the file path is correct and the file exists.")
    } catch (e: IOException) {
      // Map I/O errors to flows validation errors with context
      throw FlowValidationException(
          "I/O error while processing CTM file '${command.inputFile.name}': ${e.message}. " +
              "Check file permissions and disk space.")
    } catch (e: SecurityException) {
      // Map security errors to flows validation errors
      throw FlowValidationException(
          "Security error accessing CTM file '${command.inputFile.name}': ${e.message}. " +
              "Check file permissions.")
    } catch (e: OutOfMemoryError) {
      // Map memory errors to flows validation errors
      throw FlowValidationException(
          "Out of memory while processing CTM file '${command.inputFile.name}'. " +
              "The CTM file may be too large or corrupted.")
    } catch (e: Exception) {
      // Map any other unexpected errors to generic flows validation errors
      throw FlowValidationException(
          "Unexpected error while processing CTM file '${command.inputFile.name}': ${e.message}. " +
              "This may indicate a bug in the charpad processor or an unsupported CTM format.")
    }
  }

  /**
   * Validates that the input CTM file exists and is readable.
   *
   * @param inputFile The input CTM file to validate
   * @throws FlowValidationException if the file is invalid
   */
  private fun validateInputFile(inputFile: java.io.File) {
    if (!inputFile.exists()) {
      throw FlowValidationException(
          "CTM input file does not exist: '${inputFile.absolutePath}'. " +
              "Verify the file path is correct.")
    }

    if (!inputFile.isFile()) {
      throw FlowValidationException(
          "CTM input path is not a file: '${inputFile.absolutePath}'. " +
              "Ensure the path points to a valid CTM file.")
    }

    if (!inputFile.canRead()) {
      throw FlowValidationException(
          "Cannot read CTM input file: '${inputFile.absolutePath}'. " + "Check file permissions.")
    }

    if (inputFile.length() == 0L) {
      throw FlowValidationException(
          "CTM input file is empty: '${inputFile.absolutePath}'. " +
              "Ensure the CTM file contains valid charpad data.")
    }

    // Validate CTM file extension (optional but helpful)
    if (!inputFile.name.endsWith(".ctm", ignoreCase = true)) {
      // This is a warning, not an error - users should be able to use any file name
      // But we can log this as a potential issue
      // For now, we'll allow it to proceed
    }
  }

  /** InputByteStream implementation that wraps a FileInputStream for charpad processing. */
  private class FileInputByteStream(private val inputStream: InputStream) : InputByteStream {
    private var readCounter = 0

    override fun read(amount: Int): ByteArray {
      val buffer = ByteArray(amount)
      val size = inputStream.read(buffer)
      if (size == -1) {
        throw InsufficientDataException("Unexpected end of file reached while reading CTM data")
      }
      readCounter += size
      return buffer.copyOfRange(0, size)
    }

    override fun readCounter(): Int = readCounter
  }
}
