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
 * Domain abstraction for charpad processing commands.
 *
 * This data class encapsulates all the information needed to process Charpad CTM files within the
 * flows domain, abstracting away the specific processor implementation details.
 */
data class CharpadCommand(
    val inputFile: File,
    val charpadOutputs: CharpadOutputs,
    val config: CharpadConfig,
    val projectRootDir: File,
    val workingDirectory: File = projectRootDir
) {

  /** Returns all output file paths as a list for dependency tracking and validation. */
  fun getAllOutputFiles(): List<File> {
    return charpadOutputs.getAllOutputPaths().map { path ->
      if (File(path).isAbsolute) {
        File(path)
      } else {
        File(projectRootDir, path)
      }
    }
  }

  /** Returns a human-readable representation of the charpad processing command. */
  override fun toString(): String {
    val outputCount = charpadOutputs.getAllOutputPaths().size
    return "CharpadCommand(input=${inputFile.name}, outputs=$outputCount, config=$config)"
  }
}
