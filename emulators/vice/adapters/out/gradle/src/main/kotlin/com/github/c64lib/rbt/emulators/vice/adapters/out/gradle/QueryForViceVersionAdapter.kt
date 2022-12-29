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
package com.github.c64lib.rbt.emulators.vice.adapters.out.gradle

import com.github.c64lib.rbt.emulators.vice.domain.ViceException
import com.github.c64lib.rbt.emulators.vice.usecase.port.QueryForViceVersionPort
import com.github.c64lib.rbt.shared.domain.SemVer
import java.io.ByteArrayOutputStream
import org.gradle.api.Project

class QueryForViceVersionAdapter(private val project: Project) : QueryForViceVersionPort {
  override fun queryForVersion(executable: String): SemVer {
    val outputCapture = ByteArrayOutputStream()
    val result =
        project.exec {
          it.commandLine = listOf(executable, "-version")
          it.standardOutput = outputCapture
        }
    when (result.exitValue) {
      255 -> throw ViceException("Unsupported version of $executable.")
      0 -> Unit
      else ->
          throw ViceException("Error when running $executable, return code: ${result.exitValue}.")
    }
    return parse(outputCapture.toString())
  }

  private fun parse(output: String): SemVer {
    val pattern = ".*VICE ([0-9]+.[0-9]+.[0-9]*).*"
    val regex = Regex(pattern)
    val match = regex.find(output)
    return if (match != null) {
      val parts = match.groupValues[1].split(".")
      val patch =
          if (parts.size >= 3) {
            parts[2].toInt()
          } else {
            0
          }
      SemVer(parts[0].toInt(), parts[1].toInt(), patch)
    } else {
      throw ViceException("Cannot determine Vice version from \"$output\"")
    }
  }
}
