/*
MIT License

Copyright (c) 2018-2023 c64lib: The Ultimate Commodore 64 Library

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
package com.github.c64lib.rbt.testing.a64spec.adapters.`in`.gradle

import com.github.c64lib.rbt.testing.a64spec.usecase.TestResult

class TestReport(private val testResults: List<TestResult>) {
  private class Result(val outputText: String, val success: Int = 0, val total: Int = 0) {

    val isPositive: Boolean
      get() = success == total

    private fun tag() =
        if (isPositive) {
          "Success"
        } else {
          "FAILED"
        }

    override fun toString(): String = "($success/$total) ${tag()}"
  }

  fun generateTestReport(outputFn: (value: String) -> Unit): Boolean {
    val result =
        testResults.fold(Result("")) { result, testResult ->
          outputFn("Tests execution ${testResult.message}")
          Result("", result.success + testResult.successCount, result.total + testResult.totalCount)
        }
    outputFn("Overall test report $result")
    return result.isPositive
  }
}
