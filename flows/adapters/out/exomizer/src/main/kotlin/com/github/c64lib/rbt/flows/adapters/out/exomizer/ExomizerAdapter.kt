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
package com.github.c64lib.rbt.flows.adapters.out.exomizer

import com.github.c64lib.rbt.crunchers.exomizer.adapters.`in`.gradle.GradleExomizerAdapter
import com.github.c64lib.rbt.crunchers.exomizer.domain.CrunchMemCommand
import com.github.c64lib.rbt.crunchers.exomizer.domain.CrunchRawCommand
import com.github.c64lib.rbt.crunchers.exomizer.domain.MemOptions
import com.github.c64lib.rbt.crunchers.exomizer.domain.RawOptions
import com.github.c64lib.rbt.crunchers.exomizer.usecase.CrunchMemUseCase
import com.github.c64lib.rbt.crunchers.exomizer.usecase.CrunchRawUseCase
import com.github.c64lib.rbt.crunchers.exomizer.usecase.port.ExecuteExomizerPort
import com.github.c64lib.rbt.flows.domain.FlowValidationException
import com.github.c64lib.rbt.flows.domain.port.ExomizerPort
import java.io.File

/**
 * Adapter implementation of ExomizerPort that bridges flows domain to crunchers domain.
 *
 * This adapter translates exomizer compression requests from flows layer to use cases in the
 * crunchers domain while maintaining hexagonal architecture boundaries.
 */
class ExomizerAdapter(
    private val executeExomizerPort: ExecuteExomizerPort = GradleExomizerAdapter()
) : ExomizerPort {

  private val crunchRawUseCase = CrunchRawUseCase(executeExomizerPort)
  private val crunchMemUseCase = CrunchMemUseCase(executeExomizerPort)

  override fun crunchRaw(source: File, output: File) {
    try {
      validateInputFile(source)
      validateOutputPath(output)

      val options = RawOptions()
      val command = CrunchRawCommand(source = source, output = output, options = options)
      crunchRawUseCase.apply(command)
    } catch (e: FlowValidationException) {
      throw e
    } catch (e: Exception) {
      throw FlowValidationException(
          "Exomizer raw compression failed for '${source.name}': ${e.message}. " +
              "Verify the file is a valid binary file and exomizer is available in PATH.")
    }
  }

  override fun crunchMem(source: File, output: File, loadAddress: String, forward: Boolean) {
    try {
      validateInputFile(source)
      validateOutputPath(output)

      val options = MemOptions(loadAddress = loadAddress, forward = forward)
      val command = CrunchMemCommand(source = source, output = output, options = options)
      crunchMemUseCase.apply(command)
    } catch (e: FlowValidationException) {
      throw e
    } catch (e: Exception) {
      throw FlowValidationException(
          "Exomizer memory compression failed for '${source.name}': ${e.message}. " +
              "Verify load address format and exomizer is available in PATH.")
    }
  }

  /**
   * Validates that the input file exists and is readable.
   *
   * @param inputFile The input file to validate
   * @throws FlowValidationException if the file is invalid
   */
  private fun validateInputFile(inputFile: File) {
    if (!inputFile.exists()) {
      throw FlowValidationException(
          "Exomizer input file does not exist: '${inputFile.absolutePath}'. " +
              "Verify the file path is correct.")
    }

    if (!inputFile.isFile()) {
      throw FlowValidationException(
          "Exomizer input path is not a file: '${inputFile.absolutePath}'. " +
              "Ensure the path points to a valid binary file.")
    }

    if (!inputFile.canRead()) {
      throw FlowValidationException(
          "Cannot read exomizer input file: '${inputFile.absolutePath}'. " +
              "Check file permissions.")
    }
  }

  /**
   * Validates that the output path is writable.
   *
   * @param outputFile The output file to validate
   * @throws FlowValidationException if the output path is invalid
   */
  private fun validateOutputPath(outputFile: File) {
    val parentDir = outputFile.parentFile
    if (parentDir != null && !parentDir.exists()) {
      try {
        parentDir.mkdirs()
      } catch (e: Exception) {
        throw FlowValidationException(
            "Cannot create output directory for exomizer: '${parentDir.absolutePath}'. " +
                "Check write permissions.")
      }
    }

    if (parentDir != null && !parentDir.canWrite()) {
      throw FlowValidationException(
          "Cannot write exomizer output file: '${outputFile.absolutePath}'. " +
              "Check directory write permissions.")
    }
  }
}
