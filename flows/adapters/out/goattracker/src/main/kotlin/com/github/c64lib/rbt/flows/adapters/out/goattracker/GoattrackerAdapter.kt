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
package com.github.c64lib.rbt.flows.adapters.out.goattracker

import com.github.c64lib.rbt.flows.domain.FlowValidationException
import com.github.c64lib.rbt.flows.domain.config.GoattrackerCommand
import com.github.c64lib.rbt.flows.domain.port.GoattrackerPort
import com.github.c64lib.rbt.processors.goattracker.usecase.PackSongCommand
import com.github.c64lib.rbt.processors.goattracker.usecase.PackSongUseCase
import com.github.c64lib.rbt.processors.goattracker.usecase.port.ExecuteGt2RelocPort
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

/**
 * Adapter implementation of GoattrackerPort that bridges flows domain to goattracker processor
 * module.
 *
 * This adapter translates GoattrackerCommand instances to PackSongUseCase calls while maintaining
 * hexagonal architecture boundaries. It supports all GoatTracker SNG file processing with complete
 * configuration options including frequency, channels, optimization, and advanced gt2reloc
 * parameters.
 */
class GoattrackerAdapter(private val executeGt2RelocPort: ExecuteGt2RelocPort) : GoattrackerPort {

  override fun process(command: GoattrackerCommand) {
    try {
      // Validate input file exists and is readable
      validateInputFile(command.inputFile)

      // Create PackSongCommand from GoattrackerCommand
      val packSongCommand =
          PackSongCommand(
              source = command.inputFile,
              output = command.output,
              executable = command.config.executable,
              useBuildDir = false,
              bufferedSidWrites = command.config.bufferedSidWrites,
              disableOptimization = command.config.disableOptimization,
              playerMemoryLocation = command.config.playerMemoryLocation,
              sfxSupport = command.config.sfxSupport,
              sidMemoryLocation = command.config.sidMemoryLocation,
              storeAuthorInfo = command.config.storeAuthorInfo,
              volumeChangeSupport = command.config.volumeChangeSupport,
              zeroPageLocation = command.config.zeroPageLocation,
              zeropageGhostRegisters = command.config.zeropageGhostRegisters)

      // Create PackSongUseCase with injected ExecuteGt2RelocPort
      val packSongUseCase = PackSongUseCase(executeGt2RelocPort)

      // Execute goattracker processing
      packSongUseCase.apply(packSongCommand)
    } catch (e: FlowValidationException) {
      // Re-throw flows validation exceptions as-is
      throw e
    } catch (e: FileNotFoundException) {
      // Map file not found errors to flows validation errors
      throw FlowValidationException(
          "SNG input file not found: '${command.inputFile.absolutePath}'. " +
              "Verify the file path is correct and the file exists.")
    } catch (e: IOException) {
      // Map I/O errors to flows validation errors with context
      throw FlowValidationException(
          "I/O error while processing SNG file '${command.inputFile.name}': ${e.message}. " +
              "Check file permissions and disk space.")
    } catch (e: SecurityException) {
      // Map security errors to flows validation errors
      throw FlowValidationException(
          "Security error accessing SNG file '${command.inputFile.name}': ${e.message}. " +
              "Check file permissions.")
    } catch (e: OutOfMemoryError) {
      // Map memory errors to flows validation errors
      throw FlowValidationException(
          "Out of memory while processing SNG file '${command.inputFile.name}'. " +
              "The SNG file may be too large or corrupted.")
    } catch (e: IllegalArgumentException) {
      // Map argument errors (invalid config) to flows validation errors
      throw FlowValidationException(
          "Invalid configuration for GoatTracker processing of '${command.inputFile.name}': ${e.message}")
    } catch (e: Exception) {
      // Map any other unexpected errors to generic flows validation errors
      throw FlowValidationException(
          "Unexpected error while processing SNG file '${command.inputFile.name}': ${e.message}. " +
              "This may indicate a bug in the goattracker processor or an unsupported SNG format.")
    }
  }

  /**
   * Validates that the input SNG file exists and is readable.
   *
   * @param inputFile The input SNG file to validate
   * @throws FlowValidationException if the file is invalid
   */
  private fun validateInputFile(inputFile: File) {
    if (!inputFile.exists()) {
      throw FlowValidationException(
          "SNG input file does not exist: '${inputFile.absolutePath}'. " +
              "Verify the file path is correct.")
    }

    if (!inputFile.isFile) {
      throw FlowValidationException(
          "SNG input path is not a file: '${inputFile.absolutePath}'. " +
              "Ensure the path points to a valid SNG file.")
    }

    if (!inputFile.canRead()) {
      throw FlowValidationException(
          "Cannot read SNG input file: '${inputFile.absolutePath}'. Check file permissions.")
    }

    if (inputFile.length() == 0L) {
      throw FlowValidationException(
          "SNG input file is empty: '${inputFile.absolutePath}'. " +
              "Ensure the SNG file contains valid GoatTracker song data.")
    }

    // Validate SNG file extension (optional but helpful)
    if (!inputFile.name.endsWith(".sng", ignoreCase = true)) {
      // This is a warning, not an error - users should be able to use any file name
      // But we can log this as a potential issue
      // For now, we'll allow it to proceed
    }
  }
}
