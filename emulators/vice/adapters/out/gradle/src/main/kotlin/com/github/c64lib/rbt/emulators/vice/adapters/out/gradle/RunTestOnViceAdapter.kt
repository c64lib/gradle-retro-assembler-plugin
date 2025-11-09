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
package com.github.c64lib.rbt.emulators.vice.adapters.out.gradle

import com.github.c64lib.rbt.emulators.vice.domain.JamAction
import com.github.c64lib.rbt.emulators.vice.usecase.port.RunTestOnVicePort
import com.github.c64lib.rbt.emulators.vice.usecase.port.ViceParameters
import com.github.c64lib.rbt.shared.gradle.dsl.RetroAssemblerPluginExtension
import java.io.ByteArrayOutputStream
import org.gradle.api.Project

class RunTestOnViceAdapter(
    private val project: Project,
    private val extension: RetroAssemblerPluginExtension
) : RunTestOnVicePort {
  override fun run(parameters: ViceParameters) {
    val output = ByteArrayOutputStream()
    project.exec {
      val cliBuilder =
          CommandLineBuilder(extension.viceExecutable, true)
              .switch("warp")
              .switchIf(extension.verbose, "verbose")
              .switchIf(extension.viceHeadless, "console")
              .toggleSwitchIf(!extension.viceHeadless, "confirmonexit", false)
              .toggleSwitch("drive8truedrive", false)
              .toggleSwitch("virtualdev8", true)
              .toggleSwitch("autostart-handle-tde", false)
              .toggleSwitch("fslongnames", true)
              .toggleSwitch("sound", false)
              .switch("fs8", parameters.testToRun.parent)
              .switch("autostart", parameters.testToRun.absolutePath)
              .switch("jamaction", JamAction.QUIT.code.toString())
              .switch("autostartprgmode", extension.viceAutostartPrgMode.code.toString())
              .switch("moncommands", parameters.monCommandsFile.absolutePath)
              .switch("chdir", parameters.testToRun.parent)

      it.commandLine = cliBuilder.build()
      it.standardOutput = output
      if (extension.verbose) {
        println("Executing Vice: $cliBuilder")
      }
    }
    if (extension.verbose) {
      println("Vice executed with output:\n $output")
    }
  }
}
