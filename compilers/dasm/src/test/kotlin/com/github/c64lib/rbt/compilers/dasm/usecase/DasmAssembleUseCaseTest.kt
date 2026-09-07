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
package com.github.c64lib.rbt.compilers.dasm.usecase

import com.github.c64lib.rbt.compilers.dasm.usecase.port.DasmAssemblePort
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.io.File

private class RecordingDasmAssemblePort : DasmAssemblePort {
  var libDirs: List<File>? = null
  var defines: Map<String, String>? = null
  var source: File? = null
  var outputFormat: Int? = null
  var outputFile: File? = null
  var listFile: File? = null
  var symbolFile: File? = null
  var verboseness: Int? = null
  var errorFormat: Int? = null
  var strictSyntax: Boolean? = null
  var removeOnError: Boolean? = null
  var symbolTableSort: Int? = null
  var callCount = 0

  override fun assemble(
      libDirs: List<File>,
      defines: Map<String, String>,
      source: File,
      outputFormat: Int,
      outputFile: File?,
      listFile: File?,
      symbolFile: File?,
      verboseness: Int?,
      errorFormat: Int?,
      strictSyntax: Boolean?,
      removeOnError: Boolean?,
      symbolTableSort: Int?
  ) {
    this.libDirs = libDirs
    this.defines = defines
    this.source = source
    this.outputFormat = outputFormat
    this.outputFile = outputFile
    this.listFile = listFile
    this.symbolFile = symbolFile
    this.verboseness = verboseness
    this.errorFormat = errorFormat
    this.strictSyntax = strictSyntax
    this.removeOnError = removeOnError
    this.symbolTableSort = symbolTableSort
    callCount++
  }
}

class DasmAssembleUseCaseTest :
    BehaviorSpec({
      isolationMode = IsolationMode.InstancePerTest

      Given("a DasmAssembleUseCase with a recording port") {
        val port = RecordingDasmAssemblePort()
        val useCase = DasmAssembleUseCase(port)

        When("applying a fully populated command") {
          val command =
              DasmAssembleCommand(
                  libDirs = listOf(File("lib")),
                  defines = mapOf("DEBUG" to "1"),
                  source = File("main.asm"),
                  outputFormat = 2,
                  outputFile = File("main.prg"),
                  listFile = File("main.lst"),
                  symbolFile = File("main.sym"),
                  verboseness = 3,
                  errorFormat = 1,
                  strictSyntax = true,
                  removeOnError = false,
                  symbolTableSort = 1)
          useCase.apply(command)

          Then("the port receives every command field unchanged") {
            port.callCount shouldBe 1
            port.libDirs shouldBe listOf(File("lib"))
            port.defines shouldBe mapOf("DEBUG" to "1")
            port.source shouldBe File("main.asm")
            port.outputFormat shouldBe 2
            port.outputFile shouldBe File("main.prg")
            port.listFile shouldBe File("main.lst")
            port.symbolFile shouldBe File("main.sym")
            port.verboseness shouldBe 3
            port.errorFormat shouldBe 1
            port.strictSyntax shouldBe true
            port.removeOnError shouldBe false
            port.symbolTableSort shouldBe 1
          }
        }

        When("applying a minimal command") {
          val command =
              DasmAssembleCommand(
                  libDirs = emptyList(), defines = emptyMap(), source = File("test.asm"))
          useCase.apply(command)

          Then("defaults are forwarded to the port") {
            port.callCount shouldBe 1
            port.outputFormat shouldBe 1
            port.outputFile shouldBe null
            port.listFile shouldBe null
            port.symbolFile shouldBe null
            port.verboseness shouldBe null
            port.errorFormat shouldBe null
            port.strictSyntax shouldBe null
            port.removeOnError shouldBe null
            port.symbolTableSort shouldBe null
          }
        }
      }
    })
