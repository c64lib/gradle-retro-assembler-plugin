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
package com.github.c64lib.rbt.compilers.dasm.domain

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class DasmOperatingSystemTest :
    BehaviorSpec({
      Given("operating system names") {
        When("detecting Windows") {
          Then("win names map to WINDOWS") {
            DasmOperatingSystem.detect("Windows 11") shouldBe DasmOperatingSystem.WINDOWS
            DasmOperatingSystem.detect("Windows Server 2022") shouldBe DasmOperatingSystem.WINDOWS
          }
        }

        When("detecting macOS") {
          Then("mac names map to MACOS") {
            DasmOperatingSystem.detect("Mac OS X") shouldBe DasmOperatingSystem.MACOS
            DasmOperatingSystem.detect("Darwin") shouldBe DasmOperatingSystem.MACOS
          }
        }

        When("detecting Linux and others") {
          Then("everything else maps to LINUX") {
            DasmOperatingSystem.detect("Linux") shouldBe DasmOperatingSystem.LINUX
            DasmOperatingSystem.detect("FreeBSD") shouldBe DasmOperatingSystem.LINUX
          }
        }
      }

      Given("asset and executable names") {
        Then("they follow the dasm release layout") {
          DasmOperatingSystem.WINDOWS.assetName shouldBe "windows"
          DasmOperatingSystem.WINDOWS.executableName shouldBe "dasm.exe"
          DasmOperatingSystem.MACOS.assetName shouldBe "macos"
          DasmOperatingSystem.MACOS.executableName shouldBe "dasm"
          DasmOperatingSystem.LINUX.assetName shouldBe "linux"
          DasmOperatingSystem.LINUX.executableName shouldBe "dasm"
        }
      }
    })
