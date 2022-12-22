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

enum class JamAction(val code: Int) {
  SHOW_DIALOG(0),
  CONTINUE(1),
  START_MON(2),
  SOFT_RESET(3),
  HARD_RESET(4),
  QUIT(5),
  NONE(-1)
}

enum class AutostartPrgMode(val code: Int) {
  VIRTUAL_FS(0),
  INJECT_TO_RAM(1),
  COPY_TO_D64(2),
  NONE(-1)
}

class ViceSpec {
  var executable = "x64"
  var warpMode = false
  var autostart = ""
  var headless = false
  var jamAction = JamAction.NONE
  var autostartPrgMode = AutostartPrgMode.NONE
  var monCommands = ""
  var chdir = ""
  var verbose = false

  fun makeCommandLine(): List<String> {
    val cli = mutableListOf(executable)
    if (warpMode) {
      cli += "-warp"
    }
    if (autostart.isNotEmpty()) {
      cli += listOf("-autostart", autostart)
    }
    cli +=
        if (headless) {
          "-console"
        } else {
          "+confirmonexit"
        }
    if (jamAction != JamAction.NONE) {
      cli += listOf("-jamaction", jamAction.code.toString())
    }
    if (autostartPrgMode != AutostartPrgMode.NONE) {
      cli += listOf("-autostartprgmode", autostartPrgMode.code.toString())
    }
    if (monCommands.isNotEmpty()) {
      cli += listOf("-moncommands", monCommands)
    }
    if (chdir.isNotEmpty()) {
      cli += listOf("-chdir", chdir)
    }
    return cli.toList()
  }
}
