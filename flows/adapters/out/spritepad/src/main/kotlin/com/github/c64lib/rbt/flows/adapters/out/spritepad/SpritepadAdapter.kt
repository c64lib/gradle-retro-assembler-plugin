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
package com.github.c64lib.rbt.flows.adapters.out.spritepad

import com.c64lib.rbt.processors.spritepad.usecase.ProcessSpritepadUseCase
import com.github.c64lib.rbt.flows.domain.FlowValidationException
import com.github.c64lib.rbt.flows.domain.config.SpritepadCommand
import com.github.c64lib.rbt.flows.domain.port.SpritepadPort
import com.github.c64lib.rbt.shared.processor.InputByteStream
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

/**
 * Adapter implementation of SpritepadPort that bridges flows domain to spritepad processor module.
 *
 * This adapter translates SpritepadCommand instances to ProcessSpritepadUseCase calls while
 * maintaining hexagonal architecture boundaries. It supports all SPD format versions and sprite
 * output types including range-based sprite selection.
 */
class SpritepadAdapter(
    private val outputProducerFactory: SpritepadOutputProducerFactory =
        SpritepadOutputProducerFactory()
) : SpritepadPort {

  override fun process(command: SpritepadCommand) {
    try {
      // Validate input file exists and is readable
      validateInputFile(command.inputFile)

      // Create output producers based on command configuration and output files
      val outputProducers = outputProducerFactory.createOutputProducers(command)

      // Validate that at least one output producer was created
      if (outputProducers.isEmpty()) {
        throw FlowValidationException(
            "No output producers configured for spritepad processing. " +
                "At least one output file must be specified with sprite output configuration.")
      }

      // Create ProcessSpritepadUseCase with output producers
      val processSpritepadUseCase = ProcessSpritepadUseCase(outputProducers)

      // Create input stream from the SPD file
      val inputStream = FileInputByteStream(FileInputStream(command.inputFile))

      // Execute spritepad processing
      processSpritepadUseCase.apply(inputStream)
    } catch (e: FlowValidationException) {
      // Re-throw flows validation exceptions as-is
      throw e
    } catch (e: FileNotFoundException) {
      // Map file not found errors to flows validation errors
      throw FlowValidationException(
          "SPD input file not found: '${command.inputFile.absolutePath}'. " +
              "Verify the file path is correct and the file exists.")
    } catch (e: IOException) {
      // Map I/O errors to flows validation errors with context
      throw FlowValidationException(
          "I/O error while processing SPD file '${command.inputFile.name}': ${e.message}. " +
              "Check file permissions and disk space.")
    } catch (e: SecurityException) {
      // Map security errors to flows validation errors
      throw FlowValidationException(
          "Security error accessing SPD file '${command.inputFile.name}': ${e.message}. " +
              "Check file permissions.")
    } catch (e: OutOfMemoryError) {
      // Map memory errors to flows validation errors
      throw FlowValidationException(
          "Out of memory while processing SPD file '${command.inputFile.name}'. " +
              "The SPD file may be too large or corrupted.")
    } catch (e: Exception) {
      // Map any other unexpected errors to generic flows validation errors
      throw FlowValidationException(
          "Unexpected error while processing SPD file '${command.inputFile.name}': ${e.message}. " +
              "This may indicate a bug in the spritepad processor or an unsupported SPD format.")
    }
  }

  /**
   * Validates that the input SPD file exists and is readable.
   *
   * @param inputFile The input SPD file to validate
   * @throws FlowValidationException if the file is invalid
   */
  private fun validateInputFile(inputFile: java.io.File) {
    if (!inputFile.exists()) {
      throw FlowValidationException(
          "SPD input file does not exist: '${inputFile.absolutePath}'. " +
              "Verify the file path is correct.")
    }

    if (!inputFile.isFile()) {
      throw FlowValidationException(
          "SPD input path is not a file: '${inputFile.absolutePath}'. " +
              "Ensure the path points to a valid SPD file.")
    }

    if (!inputFile.canRead()) {
      throw FlowValidationException(
          "Cannot read SPD input file: '${inputFile.absolutePath}'. " + "Check file permissions.")
    }

    if (inputFile.length() == 0L) {
      throw FlowValidationException(
          "SPD input file is empty: '${inputFile.absolutePath}'. " +
              "Ensure the SPD file contains valid spritepad data.")
    }

    // Validate SPD file extension (optional but helpful)
    if (!inputFile.name.endsWith(".spd", ignoreCase = true)) {
      // This is a warning, not an error - users should be able to use any file name
      // But we can log this as a potential issue
      // For now, we'll allow it to proceed
    }
  }

  /** InputByteStream implementation that wraps a FileInputStream for spritepad processing. */
  private class FileInputByteStream(private val inputStream: InputStream) : InputByteStream {
    private var readCounter = 0

    override fun read(amount: Int): ByteArray {
      val buffer = ByteArray(amount)
      val size = inputStream.read(buffer)
      if (size == -1) {
        throw IOException("Unexpected end of file reached while reading SPD data")
      }
      readCounter += size
      return buffer.copyOfRange(0, size)
    }

    override fun readCounter(): Int = readCounter
  }
}
