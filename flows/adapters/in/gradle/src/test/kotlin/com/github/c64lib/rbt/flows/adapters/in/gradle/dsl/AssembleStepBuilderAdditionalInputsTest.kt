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

import com.github.c64lib.rbt.flows.domain.config.AssemblyOptimization
import com.github.c64lib.rbt.flows.domain.config.CpuType
import com.github.c64lib.rbt.shared.domain.OutputFormat
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class AssembleStepBuilderAdditionalInputsTest :
    BehaviorSpec({
      given("AssembleStepBuilder with additional input methods") {
        `when`("using includeFiles method with multiple patterns") {
          val builder = AssembleStepBuilder("testStep")
          builder.includeFiles("**/*.inc", "lib/**/*.h", "common/**/*.asm")

          val step = builder.build()

          then("should set additional input patterns in config") {
            step.config.additionalInputs shouldHaveSize 3
            step.config.additionalInputs shouldContain "**/*.inc"
            step.config.additionalInputs shouldContain "lib/**/*.h"
            step.config.additionalInputs shouldContain "common/**/*.asm"
          }
        }

        `when`("using includeFile method with single pattern") {
          val builder = AssembleStepBuilder("testStep")
          builder.includeFile("**/*.inc")
          builder.includeFile("lib/**/*.h")

          val step = builder.build()

          then("should add patterns to additional inputs") {
            step.config.additionalInputs shouldHaveSize 2
            step.config.additionalInputs shouldContain "**/*.inc"
            step.config.additionalInputs shouldContain "lib/**/*.h"
          }
        }

        `when`("using watchFiles method with multiple patterns") {
          val builder = AssembleStepBuilder("testStep")
          builder.watchFiles("includes/**/*.inc", "headers/**/*.h")

          val step = builder.build()

          then("should set additional input patterns in config") {
            step.config.additionalInputs shouldHaveSize 2
            step.config.additionalInputs shouldContain "includes/**/*.inc"
            step.config.additionalInputs shouldContain "headers/**/*.h"
          }
        }

        `when`("using watchFile method with single pattern") {
          val builder = AssembleStepBuilder("testStep")
          builder.watchFile("**/*.inc")
          builder.watchFile("common/**/*.asm")

          val step = builder.build()

          then("should add patterns to additional inputs") {
            step.config.additionalInputs shouldHaveSize 2
            step.config.additionalInputs shouldContain "**/*.inc"
            step.config.additionalInputs shouldContain "common/**/*.asm"
          }
        }

        `when`("mixing includeFiles and includeFile methods") {
          val builder = AssembleStepBuilder("testStep")
          builder.includeFile("first.inc")
          builder.includeFiles("second.inc", "third.inc")
          builder.includeFile("fourth.inc")

          val step = builder.build()

          then("should combine all patterns correctly") {
            step.config.additionalInputs shouldHaveSize 4
            step.config.additionalInputs shouldContain "first.inc"
            step.config.additionalInputs shouldContain "second.inc"
            step.config.additionalInputs shouldContain "third.inc"
            step.config.additionalInputs shouldContain "fourth.inc"
          }
        }

        `when`("includeFiles method called multiple times") {
          val builder = AssembleStepBuilder("testStep")
          builder.includeFiles("first.inc", "second.inc")
          builder.includeFiles("third.inc", "fourth.inc") // Should replace previous

          val step = builder.build()

          then("should only keep the last set of patterns") {
            step.config.additionalInputs shouldHaveSize 4
            step.config.additionalInputs shouldContain "first.inc"
            step.config.additionalInputs shouldContain "second.inc"
            step.config.additionalInputs shouldContain "third.inc"
            step.config.additionalInputs shouldContain "fourth.inc"
          }
        }

        `when`("watchFiles method called multiple times") {
          val builder = AssembleStepBuilder("testStep")
          builder.watchFiles("first.inc", "second.inc")
          builder.watchFiles("third.inc")

          val step = builder.build()

          then("should only keep the last set of patterns") {
            step.config.additionalInputs shouldHaveSize 3
            step.config.additionalInputs shouldContain "first.inc"
            step.config.additionalInputs shouldContain "second.inc"
            step.config.additionalInputs shouldContain "third.inc"
          }
        }

        `when`("combining watchFiles and includeFiles") {
          val builder = AssembleStepBuilder("testStep")
          builder.includeFiles("first.inc", "second.inc")
          builder.watchFiles("third.inc", "fourth.inc")

          val step = builder.build()

          then("should only keep the watchFiles patterns") {
            step.config.additionalInputs shouldHaveSize 4
            step.config.additionalInputs shouldContain "first.inc"
            step.config.additionalInputs shouldContain "second.inc"
            step.config.additionalInputs shouldContain "third.inc"
            step.config.additionalInputs shouldContain "fourth.inc"
          }
        }

        `when`("building step with all configuration options including additional inputs") {
          val builder = AssembleStepBuilder("complexStep")
          builder.from("main.asm")
          builder.to("output.prg")
          builder.cpu = CpuType.MOS65C02
          builder.generateSymbols = false
          builder.optimization = AssemblyOptimization.SIZE
          builder.verbose = true
          builder.outputFormat = OutputFormat.PRG
          builder.workDir = "build"
          builder.includePath("lib")
          builder.define("DEBUG", "1")
          builder.srcDir("source")
          builder.include("**/*.s")
          builder.exclude("test/**/*.s")
          builder.includeFiles("**/*.inc", "**/*.h")

          val step = builder.build()

          then("should create step with all configurations including additional inputs") {
            step.name shouldBe "complexStep"
            step.inputs shouldContain "main.asm"
            step.outputs shouldContain "output.prg"
            step.config.generateSymbols shouldBe false
            step.config.verbose shouldBe true
            step.config.outputFormat shouldBe OutputFormat.PRG
            step.config.workDir shouldBe "build"
            step.config.includePaths shouldContain "lib"
            step.config.defines shouldBe mapOf("DEBUG" to "1")
            step.config.srcDirs shouldContain "source"
            step.config.includes shouldContain "**/*.s"
            step.config.excludes shouldContain "test/**/*.s"
            step.config.additionalInputs shouldHaveSize 2
            step.config.additionalInputs shouldContain "**/*.inc"
            step.config.additionalInputs shouldContain "**/*.h"
          }
        }

        `when`("building step without additional inputs") {
          val builder = AssembleStepBuilder("simpleStep")
          builder.from("main.asm")
          builder.to("output.prg")

          val step = builder.build()

          then("should have empty additional inputs list") {
            step.config.additionalInputs.size shouldBe 0
          }
        }
      }
    })
