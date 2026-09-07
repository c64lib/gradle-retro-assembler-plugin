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
package com.github.c64lib.rbt.flows.adapters.`in`.gradle.assembly

import com.github.c64lib.rbt.flows.domain.config.DasmCommand
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.io.File

class DasmCommandAdapterTest :
    BehaviorSpec({
      isolationMode = IsolationMode.InstancePerTest

      val adapter = DasmCommandAdapter()

      Given("a fully populated domain DasmCommand") {
        val command =
            DasmCommand(
                libDirs = listOf(File("lib1"), File("lib2")),
                defines = mapOf("DEBUG" to "1", "PLATFORM" to "C64"),
                source = File("main.asm"),
                outputFormat = 2,
                outputFile = File("build/main.prg"),
                listFile = File("build/main.lst"),
                symbolFile = File("build/main.sym"),
                verboseness = 3,
                errorFormat = 1,
                strictSyntax = true,
                removeOnError = false,
                symbolTableSort = 1)

        When("converting to a DasmAssembleCommand") {
          val result = adapter.toDasmAssembleCommand(command)

          Then("every field is mapped one-to-one") {
            result.libDirs shouldBe listOf(File("lib1"), File("lib2"))
            result.defines shouldBe mapOf("DEBUG" to "1", "PLATFORM" to "C64")
            result.source shouldBe File("main.asm")
            result.outputFormat shouldBe 2
            result.outputFile shouldBe File("build/main.prg")
            result.listFile shouldBe File("build/main.lst")
            result.symbolFile shouldBe File("build/main.sym")
            result.verboseness shouldBe 3
            result.errorFormat shouldBe 1
            result.strictSyntax shouldBe true
            result.removeOnError shouldBe false
            result.symbolTableSort shouldBe 1
          }
        }
      }

      Given("a minimal domain DasmCommand") {
        val command =
            DasmCommand(libDirs = emptyList(), defines = emptyMap(), source = File("test.asm"))

        When("converting to a DasmAssembleCommand") {
          val result = adapter.toDasmAssembleCommand(command)

          Then("defaults and nulls are preserved") {
            result.libDirs shouldBe emptyList()
            result.defines shouldBe emptyMap()
            result.source shouldBe File("test.asm")
            result.outputFormat shouldBe 1
            result.outputFile shouldBe null
            result.listFile shouldBe null
            result.symbolFile shouldBe null
            result.verboseness shouldBe null
            result.errorFormat shouldBe null
            result.strictSyntax shouldBe null
            result.removeOnError shouldBe null
            result.symbolTableSort shouldBe null
          }
        }
      }

      Given("multiple domain DasmCommands") {
        val commands =
            listOf(
                DasmCommand(libDirs = emptyList(), defines = emptyMap(), source = File("a.asm")),
                DasmCommand(libDirs = emptyList(), defines = emptyMap(), source = File("b.asm")))

        When("converting the list") {
          val results = adapter.toDasmAssembleCommands(commands)

          Then("one command is produced per input") {
            results shouldHaveSize 2
            results.map { it.source } shouldBe listOf(File("a.asm"), File("b.asm"))
          }
        }
      }
    })
