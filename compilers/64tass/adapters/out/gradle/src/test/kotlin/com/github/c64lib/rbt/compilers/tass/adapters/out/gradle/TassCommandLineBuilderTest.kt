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
package com.github.c64lib.rbt.compilers.tass.adapters.out.gradle

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class TassCommandLineBuilderTest :
    BehaviorSpec({
      val source = Path.of("src", "main.asm")
      val sourceAbs = source.absolutePathString()

      Given("a builder with only the mandatory output file") {
        When("building") {
          val args =
              TassCommandLineBuilder(source)
                  .outputFormat("prg")
                  .outputFile(Path.of("main.prg"))
                  .build()

          Then("it emits the CBM prg format, -o, and the source last") {
            args shouldBe
                listOf("--cbm-prg", "-o", Path.of("main.prg").absolutePathString(), sourceAbs)
          }
        }
      }

      Given("the bin output format") {
        When("building") {
          val args =
              TassCommandLineBuilder(source)
                  .outputFormat("bin")
                  .outputFile(Path.of("main.bin"))
                  .build()

          Then("it emits --nostart instead of --cbm-prg") {
            args shouldContainInOrder listOf("--nostart", "-o")
            args shouldNotContain "--cbm-prg"
          }
        }
      }

      Given("every option set") {
        When("building") {
          val args =
              TassCommandLineBuilder(source)
                  .outputFormat("prg")
                  .cpu("65816")
                  .caseSensitive(true)
                  .asciiConversion(true)
                  .quiet(true)
                  .longBranch(true)
                  .includeDirs(listOf(Path.of("lib1"), Path.of("lib2")))
                  .defines(linkedMapOf("DEBUG" to "1", "FAST" to ""))
                  .listFile(Path.of("main.lst"))
                  .labelsFile(Path.of("main.lbl"), viceLabels = true)
                  .outputFile(Path.of("main.prg"))
                  .build()

          Then("all flags use their verified 64tass spellings") {
            args shouldContainInOrder
                listOf(
                    "--cbm-prg",
                    "--m65816",
                    "--case-sensitive",
                    "--ascii",
                    "--quiet",
                    "--long-branch",
                    "-I",
                    Path.of("lib1").absolutePathString(),
                    "-I",
                    Path.of("lib2").absolutePathString(),
                    "-D",
                    "DEBUG=1",
                    "-D",
                    "FAST",
                    "-L",
                    Path.of("main.lst").absolutePathString(),
                    "--vice-labels",
                    "-l",
                    Path.of("main.lbl").absolutePathString(),
                    "-o",
                    Path.of("main.prg").absolutePathString(),
                    sourceAbs)
          }

          And("the source file is the last argument") { args.last() shouldBe sourceAbs }
        }
      }

      Given("disabled boolean options and no labels file") {
        When("building") {
          val args =
              TassCommandLineBuilder(source)
                  .outputFormat("prg")
                  .cpu(null)
                  .caseSensitive(false)
                  .asciiConversion(null)
                  .quiet(false)
                  .longBranch(null)
                  .listFile(null)
                  .labelsFile(null, viceLabels = true)
                  .outputFile(Path.of("main.prg"))
                  .build()

          Then("no optional flags are emitted") {
            args shouldBe
                listOf("--cbm-prg", "-o", Path.of("main.prg").absolutePathString(), sourceAbs)
          }
        }
      }

      Given("a labels file without vice format") {
        When("building") {
          val args =
              TassCommandLineBuilder(source)
                  .outputFormat("prg")
                  .labelsFile(Path.of("main.lbl"), viceLabels = null)
                  .outputFile(Path.of("main.prg"))
                  .build()

          Then("-l is emitted without --vice-labels") {
            args shouldContainInOrder listOf("-l", Path.of("main.lbl").absolutePathString())
            args shouldNotContain "--vice-labels"
          }
        }
      }
    })
