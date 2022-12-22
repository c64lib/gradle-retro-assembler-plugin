/*
MIT License

Copyright (c) 2018-2022 c64lib: The Ultimate Commodore 64 Library

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
package com.github.c64lib.gradle.emu.vice

import java.io.ByteArrayOutputStream
import org.gradle.api.Action
import org.gradle.api.Project
import java.util.stream.*

class Vice(private val project: Project) {

  fun run(action: Action<ViceSpec>) {
    val spec = ViceSpec()
    action.execute(spec)
    val output = ByteArrayOutputStream()
    project.exec {
      it.commandLine = spec.makeCommandLine()
      it.standardOutput = output
      val commandLine = it.commandLine.stream().collect(Collectors.joining(" "))
      println("Executing Vice: $commandLine")
    }

    println("Vice executed with output:\n $output")
  }
}
