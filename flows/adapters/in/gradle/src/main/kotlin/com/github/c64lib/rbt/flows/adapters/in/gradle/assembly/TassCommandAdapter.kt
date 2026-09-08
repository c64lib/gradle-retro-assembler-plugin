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
package com.github.c64lib.rbt.flows.adapters.`in`.gradle.assembly

import com.github.c64lib.rbt.compilers.tass.usecase.TassAssembleCommand
import com.github.c64lib.rbt.flows.domain.config.TassCommand

/**
 * Adapter that converts domain-level TassCommand to 64tass compiler-specific TassAssembleCommand.
 *
 * This adapter is responsible for bridging the architectural boundary between the flows domain and
 * the specific 64tass compiler implementation.
 */
class TassCommandAdapter {

  /**
   * Converts a domain TassCommand to a TassAssembleCommand.
   *
   * @param tassCommand The domain-level 64tass command
   * @return TassAssembleCommand ready for TassAssembleUseCase execution
   */
  fun toTassAssembleCommand(tassCommand: TassCommand): TassAssembleCommand {
    return TassAssembleCommand(
        includeDirs = tassCommand.includeDirs,
        defines = tassCommand.defines,
        source = tassCommand.source,
        outputFile = tassCommand.outputFile,
        outputFormat = tassCommand.outputFormat,
        listFile = tassCommand.listFile,
        labelsFile = tassCommand.labelsFile,
        viceLabels = tassCommand.viceLabels,
        cpu = tassCommand.cpu,
        caseSensitive = tassCommand.caseSensitive,
        asciiConversion = tassCommand.asciiConversion,
        quiet = tassCommand.quiet,
        longBranch = tassCommand.longBranch)
  }

  /**
   * Converts multiple domain TassCommands to TassAssembleCommands.
   *
   * @param tassCommands List of domain-level 64tass commands
   * @return List of TassAssembleCommands ready for execution
   */
  fun toTassAssembleCommands(tassCommands: List<TassCommand>): List<TassAssembleCommand> {
    return tassCommands.map { toTassAssembleCommand(it) }
  }
}
