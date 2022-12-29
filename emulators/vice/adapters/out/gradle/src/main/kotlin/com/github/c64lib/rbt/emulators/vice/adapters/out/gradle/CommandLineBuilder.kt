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

internal class CommandLineBuilder(
    private val executable: String,
    private val startWithDefault: Boolean = true
) {

  private val switches: MutableList<String> = mutableListOf(executable)

  init {
    if (startWithDefault) {
      switch("default")
    }
  }

  fun switch(name: String, value: String? = null): CommandLineBuilder {
    switches += "-$name"
    if (value != null) {
      switches += value
    }
    return this
  }

  fun switchIf(condition: Boolean, name: String, value: String? = null): CommandLineBuilder {
    if (condition) {
      switch(name, value)
    }
    return this
  }

  fun toggleSwitch(name: String, value: Boolean?): CommandLineBuilder {
    if (value != null) {
      switches +=
          if (value) {
            "-"
          } else {
            "+"
          } + name
    }
    return this
  }

  fun toggleSwitchIf(condition: Boolean, name: String, value: Boolean?): CommandLineBuilder {
    if (condition) {
      toggleSwitch(name, value)
    }
    return this
  }

  fun build(): List<String> = switches.toList()

  override fun toString(): String = switches.joinToString(" ")
}
