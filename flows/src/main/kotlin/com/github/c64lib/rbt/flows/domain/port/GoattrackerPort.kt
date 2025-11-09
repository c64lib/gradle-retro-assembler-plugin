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
package com.github.c64lib.rbt.flows.domain.port

import com.github.c64lib.rbt.flows.domain.config.GoattrackerCommand

/**
 * Domain port for goattracker processing operations.
 *
 * This port defines the contract for GoatTracker SNG file processing within the flows domain,
 * abstracting away the specific processor implementation details. The port supports processing of
 * all GoatTracker song files and configuration options including frequency, channels, optimization,
 * and advanced gt2reloc parameters.
 */
interface GoattrackerPort {

  /**
   * Processes a single GoatTracker SNG file according to the specified command configuration.
   *
   * This method handles:
   * - SNG file format parsing and validation
   * - SID file generation with proper frequency and channel settings
   * - All optional gt2reloc parameters for advanced configuration
   * - File I/O with user-specified output file path
   *
   * @param command The goattracker command containing input file, output path, and configuration
   * @throws RuntimeException if the SNG file format is invalid or processing fails
   */
  fun process(command: GoattrackerCommand)

  /**
   * Processes multiple GoatTracker SNG files in sequence.
   *
   * If any processing fails, execution stops and an exception is thrown. This provides fail-fast
   * behavior for batch processing operations.
   *
   * @param commands The list of goattracker commands to execute
   * @throws RuntimeException if any SNG file processing fails
   */
  fun process(commands: List<GoattrackerCommand>) {
    commands.forEach { command -> process(command) }
  }
}
