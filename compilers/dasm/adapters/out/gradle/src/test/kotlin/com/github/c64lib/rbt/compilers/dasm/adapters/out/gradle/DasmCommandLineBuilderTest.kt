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
package com.github.c64lib.rbt.compilers.dasm.adapters.out.gradle

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

class DasmCommandLineBuilderTest :
    BehaviorSpec({
      isolationMode = IsolationMode.InstancePerTest

      val source = Paths.get("main.asm")

      Given("a builder with only a source file") {
        val args = DasmCommandLineBuilder(source).build()

        Then("the source path is the first and only argument") {
          args shouldBe listOf(source.absolutePathString())
        }
      }

      Given("a fully configured builder") {
        val libDir = Paths.get("lib")
        val output = Paths.get("main.prg")
        val listFile = Paths.get("main.lst")
        val symbolFile = Paths.get("main.sym")

        val args =
            DasmCommandLineBuilder(source)
                .libDirs(listOf(libDir))
                .defines(mapOf("DEBUG" to "1"))
                .outputFormat(2)
                .outputFile(output)
                .listFile(listFile)
                .symbolFile(symbolFile)
                .verboseness(3)
                .errorFormat(1)
                .strictSyntax(true)
                .removeOnError(true)
                .symbolTableSort(1)
                .build()

        Then("the source path comes first") { args.first() shouldBe source.absolutePathString() }

        Then("all options are rendered with dasm syntax") {
          args shouldContain "-I${libDir.absolutePathString()}"
          args shouldContain "-DDEBUG=1"
          args shouldContain "-f2"
          args shouldContain "-o${output.absolutePathString()}"
          args shouldContain "-l${listFile.absolutePathString()}"
          args shouldContain "-s${symbolFile.absolutePathString()}"
          args shouldContain "-v3"
          args shouldContain "-E1"
          args shouldContain "-S"
          args shouldContain "-R"
          args shouldContain "-T1"
        }
      }

      Given("a builder with out-of-range and disabled options") {
        val args =
            DasmCommandLineBuilder(source)
                .outputFormat(4)
                .verboseness(5)
                .errorFormat(3)
                .symbolTableSort(2)
                .strictSyntax(false)
                .removeOnError(false)
                .outputFile(null)
                .listFile(null)
                .symbolFile(null)
                .verboseness(null)
                .errorFormat(null)
                .symbolTableSort(null)
                .build()

        Then("out-of-range and null options are silently omitted") {
          args shouldBe listOf(source.absolutePathString())
        }
      }

      Given("a builder with several defines") {
        val args =
            DasmCommandLineBuilder(source)
                .defines(mapOf("A" to "1", "B" to "", "PLATFORM" to "C64"))
                .build()

        Then("each define is rendered as -Dkey=value") {
          args shouldContain "-DA=1"
          args shouldContain "-DB="
          args shouldContain "-DPLATFORM=C64"
        }

        Then("boundary format values are accepted") {
          DasmCommandLineBuilder(source).outputFormat(1).build() shouldContain "-f1"
          DasmCommandLineBuilder(source).outputFormat(3).build() shouldContain "-f3"
          DasmCommandLineBuilder(source).outputFormat(0).build() shouldNotContain "-f0"
          DasmCommandLineBuilder(source).verboseness(0).build() shouldContain "-v0"
          DasmCommandLineBuilder(source).verboseness(4).build() shouldContain "-v4"
          DasmCommandLineBuilder(source).errorFormat(0).build() shouldContain "-E0"
          DasmCommandLineBuilder(source).errorFormat(2).build() shouldContain "-E2"
          DasmCommandLineBuilder(source).symbolTableSort(0).build() shouldContain "-T0"
        }
      }
    })
