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
package com.github.c64lib.rbt.flows.adapters.`in`.gradle.dsl

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class DasmStepBuilderTest :
    BehaviorSpec({
      isolationMode = IsolationMode.InstancePerTest

      Given("a DasmStepBuilder with defaults") {
        val builder = DasmStepBuilder("defaultStep")

        When("building without any configuration") {
          val step = builder.build()

          Then("the step carries default configuration") {
            step.name shouldBe "defaultStep"
            step.inputs shouldBe emptyList()
            step.outputs shouldBe emptyList()
            step.config.outputFormat shouldBe 1
            step.config.verboseness shouldBe null
            step.config.errorFormat shouldBe null
            step.config.strictSyntax shouldBe null
            step.config.removeOnError shouldBe null
            step.config.symbolTableSort shouldBe null
            step.config.listFile shouldBe null
            step.config.symbolFile shouldBe null
            step.config.srcDirs shouldBe listOf(".")
            step.config.includes shouldBe listOf("**/*.asm")
            step.config.excludes shouldBe listOf(".ra/**/*.asm")
            step.config.workDir shouldBe ".ra"
            step.config.additionalInputs shouldBe emptyList()
          }
        }
      }

      Given("a fully configured DasmStepBuilder") {
        val builder = DasmStepBuilder("fullStep")
        builder.from("main.asm", "utils.asm")
        builder.to("build/main.prg", "build/utils.prg")
        builder.includePaths("lib1", "lib2")
        builder.includePath("lib3")
        builder.define("DEBUG", "1")
        builder.define("BARE")
        builder.defines("PLATFORM" to "C64")
        builder.srcDirs("src", "asm")
        builder.srcDir("extra")
        builder.includes("**/*.s")
        builder.include("**/*.asm")
        builder.excludes("build/**")
        builder.exclude("tmp/**")
        builder.additionalInputs("**/*.inc")
        builder.additionalInput("**/*.mac")
        builder.outputFormat(3)
        builder.verboseness(2)
        builder.errorFormat(1)
        builder.strictSyntax(true)
        builder.removeOnError(true)
        builder.symbolTableSort(1)
        builder.listFile("build/main.lst")
        builder.symbolFile("build/main.sym")

        When("building") {
          val step = builder.build()

          Then("all inputs and outputs are carried over") {
            step.inputs shouldBe listOf("main.asm", "utils.asm")
            step.outputs shouldBe listOf("build/main.prg", "build/utils.prg")
          }

          And("all configuration values are carried over") {
            step.config.includePaths shouldBe listOf("lib1", "lib2", "lib3")
            step.config.defines shouldBe mapOf("DEBUG" to "1", "BARE" to "", "PLATFORM" to "C64")
            step.config.srcDirs shouldBe listOf("src", "asm", "extra")
            step.config.includes shouldBe listOf("**/*.s", "**/*.asm")
            step.config.excludes shouldBe listOf("build/**", "tmp/**")
            step.config.additionalInputs shouldBe listOf("**/*.inc", "**/*.mac")
            step.config.outputFormat shouldBe 3
            step.config.verboseness shouldBe 2
            step.config.errorFormat shouldBe 1
            step.config.strictSyntax shouldBe true
            step.config.removeOnError shouldBe true
            step.config.symbolTableSort shouldBe 1
            step.config.listFile shouldBe "build/main.lst"
            step.config.symbolFile shouldBe "build/main.sym"
          }
        }
      }

      Given("a DasmStepBuilder using property assignment") {
        val builder = DasmStepBuilder("propStep")
        builder.outputFormat = 2
        builder.verboseness = 4
        builder.errorFormat = 0
        builder.strictSyntax = false
        builder.removeOnError = false
        builder.symbolTableSort = 0
        builder.workDir = "work"

        When("building") {
          val step = builder.build()

          Then("property values are carried over") {
            step.config.outputFormat shouldBe 2
            step.config.verboseness shouldBe 4
            step.config.errorFormat shouldBe 0
            step.config.strictSyntax shouldBe false
            step.config.removeOnError shouldBe false
            step.config.symbolTableSort shouldBe 0
            step.config.workDir shouldBe "work"
          }
        }
      }
    })
