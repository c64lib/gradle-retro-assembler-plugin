/*
MIT License

Copyright (c) 2018-2023 c64lib: The Ultimate Commodore 64 Library
Copyright (c) 2018-2023 Maciej Małecki

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
package com.github.c64lib.rbt.testing.a64spec.usecase

import com.github.c64lib.rbt.emulators.vice.usecase.RunTestOnViceCommand
import com.github.c64lib.rbt.emulators.vice.usecase.RunTestOnViceUseCase
import java.io.File

class Run64SpecTestUseCase(private val runTestOnViceUseCase: RunTestOnViceUseCase) {
  fun apply(testSource: File): TestResult {
    runTestOnViceUseCase.apply(
        RunTestOnViceCommand(
            autostart = File(prgFile(testSource.absoluteFile)),
            monCommands = File(viceSymbolFile(testSource))))
    val resultFile = File(resultFile(testSource))
    return parseTestOutput(fromPetscii(resultFile.readBytes()))
  }

  private fun parseTestOutput(outputText: String): TestResult {
    val regex = Regex("\\((\\d+)/(\\d+)\\)")
    val matchResult: MatchResult? = regex.find(outputText)
    return if (matchResult != null) {
      TestResult(matchResult.groupValues[1].toInt(), matchResult.groupValues[2].toInt(), outputText)
    } else {
      TestResult(message = outputText)
    }
  }
  private fun fromPetscii(bytes: ByteArray) =
      bytes
          .asSequence()
          .map { value ->
            when {
              value == 13.toByte() -> System.lineSeparator()
              value >= 0 -> "" + (0 + value).toChar()
              else -> "" + (128 + value).toChar()
            }
          }
          .joinToString(separator = "")
}
