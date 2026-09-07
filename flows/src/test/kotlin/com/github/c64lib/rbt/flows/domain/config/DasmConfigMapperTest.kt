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
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files

class DasmConfigMapperTest :
    BehaviorSpec({
      isolationMode = IsolationMode.InstancePerTest

      val mapper = DasmConfigMapper()

      Given("a source file in a temp project") {
        val tempDir = Files.createTempDirectory("dasm-mapper").toFile()
        val sourceFile = File(tempDir, "main.asm")
        sourceFile.writeText("  lda #\$01")

        When("mapping with an explicit relative output path") {
          val command = mapper.toDasmCommand(DasmConfig(), sourceFile, tempDir, "build/main.prg")

          Then("the output file is resolved against the project root") {
            command.outputFile shouldBe File(tempDir, "build/main.prg")
            command.source shouldBe sourceFile
          }
        }

        When("mapping with an explicit absolute output path") {
          val absoluteOutput = File(tempDir, "abs/main.prg").absolutePath
          val command = mapper.toDasmCommand(DasmConfig(), sourceFile, tempDir, absoluteOutput)

          Then("the absolute output path is used as-is") {
            command.outputFile shouldBe File(absoluteOutput)
          }
        }

        When("mapping without an output path") {
          val command = mapper.toDasmCommand(DasmConfig(), sourceFile, tempDir)

          Then("the output file is derived from the source basename") {
            command.outputFile shouldBe File(sourceFile.parentFile, "main")
          }
        }

        When("mapping with list and symbol files") {
          val config = DasmConfig(listFile = "build/main.lst", symbolFile = "build/main.sym")
          val command = mapper.toDasmCommand(config, sourceFile, tempDir)

          Then("relative list and symbol files are resolved against the project root") {
            command.listFile shouldBe File(tempDir, "build/main.lst")
            command.symbolFile shouldBe File(tempDir, "build/main.sym")
          }
        }

        When("mapping with include paths") {
          val existingLib = File(tempDir, "lib")
          existingLib.mkdirs()
          val config = DasmConfig(includePaths = listOf("lib", "does-not-exist"))
          val command = mapper.toDasmCommand(config, sourceFile, tempDir)

          Then("only existing directories are kept as lib dirs") {
            command.libDirs shouldHaveSize 1
            command.libDirs.first() shouldBe File(tempDir, "lib")
          }
        }

        When("mapping all dasm options") {
          val config =
              DasmConfig(
                  defines = mapOf("DEBUG" to "1"),
                  outputFormat = 3,
                  verboseness = 4,
                  errorFormat = 2,
                  strictSyntax = true,
                  removeOnError = true,
                  symbolTableSort = 1)
          val command = mapper.toDasmCommand(config, sourceFile, tempDir)

          Then("all options are carried over to the command") {
            command.defines shouldBe mapOf("DEBUG" to "1")
            command.outputFormat shouldBe 3
            command.verboseness shouldBe 4
            command.errorFormat shouldBe 2
            command.strictSyntax shouldBe true
            command.removeOnError shouldBe true
            command.symbolTableSort shouldBe 1
          }
        }

        When("mapping multiple source files") {
          val secondFile = File(tempDir, "utils.asm")
          secondFile.writeText("  nop")

          val commands =
              mapper.toDasmCommands(DasmConfig(), listOf(sourceFile, secondFile), tempDir)

          Then("one command per source file is produced") {
            commands shouldHaveSize 2
            commands.map { it.source } shouldBe listOf(sourceFile, secondFile)
          }
        }

        tempDir.deleteRecursively()
      }

      Given("a project tree for source discovery") {
        val tempDir = Files.createTempDirectory("dasm-discover").toFile()
        val srcDir = File(tempDir, "src")
        srcDir.mkdirs()
        File(srcDir, "main.asm").writeText("  lda #\$01")
        File(srcDir, "notes.txt").writeText("not a source")
        val nested = File(srcDir, "lib")
        nested.mkdirs()
        File(nested, "macros.asm").writeText("  nop")

        When("discovering with default includes") {
          val config = DasmConfig(srcDirs = listOf("src"))
          val files = mapper.discoverSourceFiles(config, tempDir)

          Then("all .asm files are found recursively") {
            files shouldHaveSize 2
            files.map { it.name } shouldContain "main.asm"
            files.map { it.name } shouldContain "macros.asm"
          }
        }

        When("discovering with an exclude pattern") {
          val config = DasmConfig(srcDirs = listOf("src"), excludes = listOf("lib/**/*.asm"))
          val files = mapper.discoverSourceFiles(config, tempDir)

          Then("excluded files are filtered out") {
            files shouldHaveSize 1
            files.first().name shouldBe "main.asm"
          }
        }

        When("discovering in a non-existing source dir") {
          val config = DasmConfig(srcDirs = listOf("nope"))
          val files = mapper.discoverSourceFiles(config, tempDir)

          Then("no files are found") { files.shouldBeEmpty() }
        }

        When("discovering additional inputs without patterns") {
          val files = mapper.discoverAdditionalInputFiles(DasmConfig(), tempDir)

          Then("no files are found") { files.shouldBeEmpty() }
        }

        When("discovering additional inputs with a pattern") {
          val config = DasmConfig(srcDirs = listOf("src"), additionalInputs = listOf("**/*.txt"))
          val files = mapper.discoverAdditionalInputFiles(config, tempDir)

          Then("matching files are found") {
            files shouldHaveSize 1
            files.first().name shouldBe "notes.txt"
          }
        }

        tempDir.deleteRecursively()
      }
    })
