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

import com.github.c64lib.rbt.emulators.vice.domain.AutostartPrgMode
import com.github.c64lib.rbt.emulators.vice.domain.JamAction
import com.github.c64lib.rbt.emulators.vice.usecase.port.RunTestOnVicePort
import java.io.ByteArrayOutputStream
import java.io.File
import org.gradle.api.Project

class RunTestOnViceAdapter(private val project: Project) : RunTestOnVicePort {
  override fun run(executable: String, testToRun: File, monCommandsFile: File, verbose: Boolean) {
    val output = ByteArrayOutputStream()
    project.exec {
      val cliBuilder =
          CommandLineBuilder(executable, true)
              .switch("warp")
              .switchIf(verbose, "verbose")
              .switch("console")
              .toggleSwitch("drive8truedrive", false)
              .toggleSwitch("virtualdev8", true)
              .toggleSwitch("autostart-handle-tde", false)
              .toggleSwitch("fslongnames", true)
              .toggleSwitch("sound", false)
              .switch("fs8", testToRun.parent)
              .switch("autostart", testToRun.absolutePath)
              .switch("jamaction", JamAction.QUIT.code.toString())
              .switch("autostartprgmode", AutostartPrgMode.VIRTUAL_FS.code.toString())
              .switch("moncommands", monCommandsFile.absolutePath)
              .switch("chdir", testToRun.parent)

      it.commandLine = cliBuilder.build()
      it.standardOutput = output
      if (verbose) {
        println("Executing Vice: $cliBuilder")
      }
    }
    if (verbose) {
      println("Vice executed with output:\n $output")
    }
  }
}
