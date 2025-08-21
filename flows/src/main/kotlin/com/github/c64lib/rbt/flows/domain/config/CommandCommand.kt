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
package com.github.c64lib.rbt.flows.domain.config

import java.io.File

/**
 * Domain abstraction for CLI command execution.
 *
 * This data class encapsulates all the information needed to execute a CLI command within the flows
 * domain, abstracting away the specific execution implementation details.
 */
data class CommandCommand(
    val executable: String,
    val arguments: List<String> = emptyList(),
    val workingDirectory: File,
    val environment: Map<String, String> = emptyMap(),
    val inputFiles: List<File> = emptyList(),
    val outputFiles: List<File> = emptyList(),
    val timeoutSeconds: Long? = null
) {
  /**
   * Returns the complete command line as a list of strings. The first element is the executable,
   * followed by all arguments.
   */
  fun getCommandLine(): List<String> = listOf(executable) + arguments

  /** Returns a human-readable representation of the command line. */
  fun getCommandLineString(): String = getCommandLine().joinToString(" ")
}
