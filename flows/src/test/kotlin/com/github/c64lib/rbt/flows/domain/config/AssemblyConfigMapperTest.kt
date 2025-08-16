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

import com.github.c64lib.rbt.shared.domain.OutputFormat
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files

class AssemblyConfigMapperTest :
    BehaviorSpec({
      isolationMode = IsolationMode.InstancePerTest

      Given("AssemblyConfigMapper with default configuration") {
        val mapper = AssemblyConfigMapper()
        val config = AssemblyConfig()
        val tempDir = Files.createTempDirectory("test-project").toFile()
        val sourceFile = File(tempDir, "test.asm")
        sourceFile.writeText("lda #$01")

        When("mapping to assembly command") {
          val command = mapper.toAssemblyCommand(config, sourceFile, tempDir)

          Then("it should create correct command") {
            command.source shouldBe sourceFile
            command.outputFormat shouldBe OutputFormat.PRG
            command.defines.shouldBeEmpty()
            command.values.shouldBeEmpty()
            command.libDirs.shouldBeEmpty()
          }
        }

        tempDir.deleteRecursively()
      }

      Given("AssemblyConfigMapper with custom configuration") {
        val mapper = AssemblyConfigMapper()
        val tempDir = Files.createTempDirectory("test-project").toFile()

        // Create include directories
        val lib1Dir = File(tempDir, "lib1")
        val lib2Dir = File(tempDir, "lib2")
        lib1Dir.mkdirs()
        lib2Dir.mkdirs()

        val config =
            AssemblyConfig(
                includePaths = listOf("lib1", "lib2", "nonexistent"),
                defines = mapOf("DEBUG" to "1", "PLATFORM" to "C64", "EMPTY" to ""),
                outputFormat = OutputFormat.BIN)

        val sourceFile = File(tempDir, "main.asm")
        sourceFile.writeText("nop")

        When("mapping to assembly command") {
          val command = mapper.toAssemblyCommand(config, sourceFile, tempDir)

          Then("it should map include paths to existing directories only") {
            command.libDirs shouldHaveSize 2
            command.libDirs.map { it.name } shouldContainExactly listOf("lib1", "lib2")
          }

          And("it should split defines correctly") {
            command.defines shouldContainExactly listOf("DEBUG", "PLATFORM", "EMPTY")
            command.values shouldBe mapOf("DEBUG" to "1", "PLATFORM" to "C64")
          }

          And("it should use correct output format") {
            command.outputFormat shouldBe OutputFormat.BIN
          }
        }

        tempDir.deleteRecursively()
      }

      Given("AssemblyConfigMapper with absolute paths") {
        val mapper = AssemblyConfigMapper()
        val tempDir = Files.createTempDirectory("test-project").toFile()
        val absoluteLibDir = Files.createTempDirectory("absolute-lib").toFile()

        val config =
            AssemblyConfig(includePaths = listOf("relative/lib", absoluteLibDir.absolutePath))

        val sourceFile = File(tempDir, "test.asm")
        sourceFile.writeText("nop")

        When("mapping paths") {
          val command = mapper.toAssemblyCommand(config, sourceFile, tempDir)

          Then("it should handle both relative and absolute paths") {
            // Only absolute lib should exist, relative one won't be found
            command.libDirs shouldHaveSize 1
            command.libDirs.first() shouldBe absoluteLibDir
          }
        }

        tempDir.deleteRecursively()
        absoluteLibDir.deleteRecursively()
      }

      Given("assembly commands creation") {
        val mapper = AssemblyConfigMapper()
        val tempDir = Files.createTempDirectory("test-project").toFile()

        val file1 = File(tempDir, "main.asm")
        file1.writeText("main")

        val config = AssemblyConfig(defines = mapOf("SIMPLE" to "test"))

        When("creating commands from existing files") {
          val commands = mapper.toAssemblyCommands(config, listOf(file1), tempDir)

          Then("it should create commands correctly") {
            commands shouldHaveSize 1
            val command = commands.first()
            command.defines shouldContain "SIMPLE"
            command.values shouldBe mapOf("SIMPLE" to "test")
            command.source.name shouldBe "main.asm"
          }
        }

        tempDir.deleteRecursively()
      }

      Given("basic pattern matching") {
        val mapper = AssemblyConfigMapper()

        When("testing pattern matching with reflection") {
          Then("basic patterns should work") {
            // Use reflection to test the private method
            val method =
                mapper.javaClass.getDeclaredMethod(
                    "matchesGlobPattern", String::class.java, String::class.java)
            method.isAccessible = true

            // Test simple exact match
            val result1 = method.invoke(mapper, "test.asm", "test.asm") as Boolean
            result1 shouldBe true

            // Test simple mismatch
            val result2 = method.invoke(mapper, "test.asm", "other.asm") as Boolean
            result2 shouldBe false
          }
        }
      }

      Given("multiple source files mapping") {
        val mapper = AssemblyConfigMapper()
        val tempDir = Files.createTempDirectory("test-project").toFile()

        val file1 = File(tempDir, "main.asm")
        val file2 = File(tempDir, "utils.asm")
        file1.writeText("main")
        file2.writeText("utils")

        val config =
            AssemblyConfig(defines = mapOf("VERSION" to "1.0"), outputFormat = OutputFormat.BIN)

        When("mapping multiple source files") {
          val commands = mapper.toAssemblyCommands(config, listOf(file1, file2), tempDir)

          Then("it should create separate commands for each file") {
            commands shouldHaveSize 2

            val mainCommand = commands.find { it.source.name == "main.asm" }!!
            val utilsCommand = commands.find { it.source.name == "utils.asm" }!!

            mainCommand.defines shouldContain "VERSION"
            mainCommand.values shouldBe mapOf("VERSION" to "1.0")
            mainCommand.outputFormat shouldBe OutputFormat.BIN

            utilsCommand.defines shouldContain "VERSION"
            utilsCommand.values shouldBe mapOf("VERSION" to "1.0")
            utilsCommand.outputFormat shouldBe OutputFormat.BIN
          }
        }

        tempDir.deleteRecursively()
      }

      Given("file discovery basic functionality") {
        val mapper = AssemblyConfigMapper()
        val tempDir = Files.createTempDirectory("test-project").toFile()

        When("discovering files from non-existent directory") {
          val config = AssemblyConfig(srcDirs = listOf("nonexistent"))

          val files = mapper.discoverSourceFiles(config, tempDir)

          Then("it should return empty list") { files.shouldBeEmpty() }
        }

        tempDir.deleteRecursively()
      }
    })
