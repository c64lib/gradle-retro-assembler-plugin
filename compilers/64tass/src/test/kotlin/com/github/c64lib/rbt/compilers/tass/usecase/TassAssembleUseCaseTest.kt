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
package com.github.c64lib.rbt.compilers.tass.usecase

import com.github.c64lib.rbt.compilers.tass.usecase.port.TassAssemblePort
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.File

class TassAssembleUseCaseTest :
    BehaviorSpec({
      Given("a TassAssembleUseCase with a mock port") {
        val port = mockk<TassAssemblePort>()
        val useCase = TassAssembleUseCase(port)

        every {
          port.assemble(
              any(),
              any(),
              any(),
              any(),
              any(),
              any(),
              any(),
              any(),
              any(),
              any(),
              any(),
              any(),
              any())
        } returns Unit

        When("applying a minimal command") {
          val command =
              TassAssembleCommand(
                  includeDirs = emptyList(),
                  defines = emptyMap(),
                  source = File("main.asm"),
                  outputFile = File("main.prg"))
          useCase.apply(command)

          Then("the port receives the unpacked command with defaults") {
            verify {
              port.assemble(
                  emptyList(),
                  emptyMap(),
                  File("main.asm"),
                  File("main.prg"),
                  "prg",
                  null,
                  null,
                  null,
                  null,
                  null,
                  null,
                  null,
                  null)
            }
          }
        }

        When("applying a fully populated command") {
          val command =
              TassAssembleCommand(
                  includeDirs = listOf(File("lib")),
                  defines = mapOf("DEBUG" to "1"),
                  source = File("game.asm"),
                  outputFile = File("game.bin"),
                  outputFormat = "bin",
                  listFile = File("game.lst"),
                  labelsFile = File("game.lbl"),
                  viceLabels = true,
                  cpu = "65c02",
                  caseSensitive = true,
                  asciiConversion = true,
                  quiet = true,
                  longBranch = true)
          useCase.apply(command)

          Then("the port receives every option") {
            verify {
              port.assemble(
                  listOf(File("lib")),
                  mapOf("DEBUG" to "1"),
                  File("game.asm"),
                  File("game.bin"),
                  "bin",
                  File("game.lst"),
                  File("game.lbl"),
                  true,
                  "65c02",
                  true,
                  true,
                  true,
                  true)
            }
          }
        }
      }
    })
