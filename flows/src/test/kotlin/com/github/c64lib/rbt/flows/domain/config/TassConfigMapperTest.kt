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
package com.github.c64lib.rbt.flows.domain.config

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files

class TassConfigMapperTest :
    BehaviorSpec({
      isolationMode = IsolationMode.InstancePerTest

      val mapper = TassConfigMapper()
      val projectRoot = Files.createTempDirectory("tass-mapper-test").toFile()

      Given("a source file and an explicit output path") {
        val source = File(projectRoot, "src/main.asm")

        When("mapping with a relative output path") {
          val command = mapper.toTassCommand(TassConfig(), source, projectRoot, "build/main.prg")

          Then("the output is resolved against the project root") {
            command.outputFile shouldBe File(projectRoot, "build/main.prg")
            command.source shouldBe source
          }
        }

        When("mapping with an absolute output path") {
          val absolute = File(projectRoot, "abs/main.prg").absolutePath
          val command = mapper.toTassCommand(TassConfig(), source, projectRoot, absolute)

          Then("the absolute path is used as-is") { command.outputFile shouldBe File(absolute) }
        }
      }

      Given("a source file without explicit output") {
        val srcDir = File(projectRoot, "src").apply { mkdirs() }
        val source = File(srcDir, "main.asm")

        When("mapping with the default prg format") {
          val command = mapper.toTassCommand(TassConfig(), source, projectRoot)

          Then("the derived output keeps the basename with a .prg extension") {
            command.outputFile shouldBe File(srcDir, "main.prg")
          }
        }

        When("mapping with the bin format") {
          val command = mapper.toTassCommand(TassConfig(outputFormat = "bin"), source, projectRoot)

          Then("the derived output gets a .bin extension") {
            command.outputFile shouldBe File(srcDir, "main.bin")
            command.outputFormat shouldBe "bin"
          }
        }
      }

      Given("a config with options and existing include directories") {
        val libDir = File(projectRoot, "lib").apply { mkdirs() }
        val source = File(projectRoot, "main.asm")
        val config =
            TassConfig(
                includePaths = listOf("lib", "missing"),
                defines = mapOf("DEBUG" to "1"),
                listFile = "build/main.lst",
                labelsFile = "build/main.lbl",
                viceLabels = true,
                cpu = "65c02",
                caseSensitive = true,
                quiet = true)

        When("mapping to a command") {
          val command = mapper.toTassCommand(config, source, projectRoot, "main.prg")

          Then("only existing include directories are kept and options carry over") {
            command.includeDirs shouldBe listOf(libDir)
            command.defines shouldBe mapOf("DEBUG" to "1")
            command.listFile shouldBe File(projectRoot, "build/main.lst")
            command.labelsFile shouldBe File(projectRoot, "build/main.lbl")
            command.viceLabels shouldBe true
            command.cpu shouldBe "65c02"
            command.caseSensitive shouldBe true
            command.quiet shouldBe true
          }
        }
      }

      Given("source files on disk matching discovery patterns") {
        val srcDir = File(projectRoot, "asm").apply { mkdirs() }
        val fileA = File(srcDir, "a.asm").apply { writeText("* = \$0801") }
        val fileB = File(srcDir, "sub").apply { mkdirs() }.let { File(it, "b.asm") }
        fileB.writeText("* = \$0801")
        File(srcDir, "notes.txt").writeText("not assembly")

        val config = TassConfig(srcDirs = listOf("asm"))

        When("discovering source files") {
          val discovered = mapper.discoverSourceFiles(config, projectRoot)

          Then("only .asm files are found, recursively") {
            discovered shouldContainExactlyInAnyOrder listOf(fileA, fileB)
          }
        }

        When("mapping multiple sources") {
          val commands = mapper.toTassCommands(config, listOf(fileA, fileB), projectRoot)

          Then("one command per source is produced") { commands shouldHaveSize 2 }
        }
      }

      Given("additional input patterns") {
        val srcDir = File(projectRoot, "code").apply { mkdirs() }
        val incFile = File(srcDir, "macros.inc").apply { writeText("; macros") }
        File(srcDir, "main.asm").writeText("* = \$0801")

        val config = TassConfig(srcDirs = listOf("code"), additionalInputs = listOf("**/*.inc"))

        When("discovering additional inputs") {
          val discovered = mapper.discoverAdditionalInputFiles(config, projectRoot)

          Then("the include file is tracked") { discovered shouldBe listOf(incFile) }
        }

        When("no additional input patterns are configured") {
          val discovered =
              mapper.discoverAdditionalInputFiles(TassConfig(srcDirs = listOf("code")), projectRoot)

          Then("nothing is discovered") { discovered shouldBe emptyList() }
        }
      }
    })
