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
package com.github.c64lib.rbt.crunchers.exomizer.usecase

import com.github.c64lib.rbt.crunchers.exomizer.domain.CrunchMemCommand
import com.github.c64lib.rbt.crunchers.exomizer.domain.ExomizerExecutionException
import com.github.c64lib.rbt.crunchers.exomizer.domain.ExomizerValidationException
import com.github.c64lib.rbt.crunchers.exomizer.usecase.port.ExecuteExomizerPort

/**
 * Use case for memory mode Exomizer compression.
 *
 * Validates input, load address format, and delegates execution to the port.
 */
class CrunchMemUseCase(private val executeExomizerPort: ExecuteExomizerPort) {

  /**
   * Execute memory mode compression.
   *
   * @param command Command containing source, output, and memory options
   * @throws ExomizerValidationException if source doesn't exist, output is not writable,
   * ```
   *         or load address format is invalid
   * @throws ExomizerExecutionException
   * ```
   * if compression fails
   */
  fun apply(command: CrunchMemCommand) {
    validateCommand(command)

    try {
      executeExomizerPort.executeMem(command.source, command.output, command.options)
    } catch (e: ExomizerExecutionException) {
      throw e
    } catch (e: Exception) {
      throw ExomizerExecutionException("Failed to execute Exomizer memory compression", e)
    }
  }

  private fun validateCommand(command: CrunchMemCommand) {
    if (!command.source.exists()) {
      throw ExomizerValidationException(
          "Source file does not exist: ${command.source.absolutePath}")
    }

    val outputParent = command.output.parentFile
    if (outputParent != null && !outputParent.exists()) {
      throw ExomizerValidationException(
          "Output directory does not exist: ${outputParent.absolutePath}")
    }

    if (outputParent != null && !outputParent.canWrite()) {
      throw ExomizerValidationException(
          "Output directory is not writable: ${outputParent.absolutePath}")
    }

    validateLoadAddress(command.options.loadAddress)
  }

  private fun validateLoadAddress(loadAddress: String) {
    if (loadAddress == "auto" || loadAddress == "none") {
      return
    }

    // Try to parse as hexadecimal or decimal address
    try {
      if (loadAddress.startsWith("0x", ignoreCase = true)) {
        loadAddress.substring(2).toLong(16)
      } else if (loadAddress.startsWith("$")) {
        loadAddress.substring(1).toLong(16)
      } else {
        loadAddress.toLong()
      }
    } catch (e: NumberFormatException) {
      throw ExomizerValidationException(
          "Invalid load address format: '$loadAddress'. Use 'auto', 'none', or a valid hex/decimal address.")
    }
  }
}
