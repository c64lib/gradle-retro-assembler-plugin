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
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import java.io.File
import java.nio.file.Files

class AssemblyConfigMapperAdditionalInputsTest :
    BehaviorSpec({
      given("AssemblyConfigMapper with additional input patterns") {
        val mapper = AssemblyConfigMapper()

        `when`("config has no additional inputs") {
          val config = AssemblyConfig(additionalInputs = emptyList())
          val projectDir = Files.createTempDirectory("test-project").toFile()

          then("should return empty list") {
            val result = mapper.discoverAdditionalInputFiles(config, projectDir)
            result.shouldBeEmpty()
          }

          projectDir.deleteRecursively()
        }

        `when`("config has additional input patterns but no matching files") {
          val projectDir = Files.createTempDirectory("test-project").toFile()
          val config =
              AssemblyConfig(
                  srcDirs = listOf("."), additionalInputs = listOf("**/*.inc", "lib/**/*.asm"))

          then("should return empty list") {
            val result = mapper.discoverAdditionalInputFiles(config, projectDir)
            result.shouldBeEmpty()
          }

          projectDir.deleteRecursively()
        }

        `when`("config has additional input patterns with matching files") {
          val projectDir = Files.createTempDirectory("test-project").toFile()

          // Create test file structure
          val libDir = File(projectDir, "lib")
          libDir.mkdirs()
          val includesDir = File(projectDir, "includes")
          includesDir.mkdirs()

          val incFile1 = File(includesDir, "constants.inc")
          val incFile2 = File(libDir, "utility.inc")
          val asmFile = File(libDir, "helper.asm")
          val txtFile = File(projectDir, "readme.txt")

          incFile1.writeText("#define SCREEN_RAM \$0400")
          incFile2.writeText("#define BORDER_COLOR \$d020")
          asmFile.writeText("* = \$1000\n  rts")
          txtFile.writeText("This is a readme file")

          val config =
              AssemblyConfig(
                  srcDirs = listOf("."), additionalInputs = listOf("**/*.inc", "lib/**/*.asm"))

          then("should return matching files") {
            val result = mapper.discoverAdditionalInputFiles(config, projectDir)
            result shouldHaveSize 3
            result.map { it.name } shouldContain "constants.inc"
            result.map { it.name } shouldContain "utility.inc"
            result.map { it.name } shouldContain "helper.asm"
          }

          projectDir.deleteRecursively()
        }

        `when`("config has multiple source directories with additional inputs") {
          val projectDir = Files.createTempDirectory("test-project").toFile()

          // Create test file structure
          val srcDir = File(projectDir, "src")
          val libDir = File(projectDir, "lib")
          srcDir.mkdirs()
          libDir.mkdirs()

          val srcIncFile = File(srcDir, "src.inc")
          val libIncFile = File(libDir, "lib.inc")
          val otherFile = File(projectDir, "other.inc")

          srcIncFile.writeText("#define SRC_CONSTANT 1")
          libIncFile.writeText("#define LIB_CONSTANT 2")
          otherFile.writeText("#define OTHER_CONSTANT 3")

          val config =
              AssemblyConfig(srcDirs = listOf("src", "lib"), additionalInputs = listOf("*.inc"))

          then("should find files in all specified source directories") {
            val result = mapper.discoverAdditionalInputFiles(config, projectDir)
            result shouldHaveSize 2
            result.map { it.name } shouldContain "src.inc"
            result.map { it.name } shouldContain "lib.inc"
            // other.inc should not be included as it's not in src or lib directories
          }

          projectDir.deleteRecursively()
        }

        `when`("config has non-existent source directories") {
          val projectDir = Files.createTempDirectory("test-project").toFile()

          val config =
              AssemblyConfig(
                  srcDirs = listOf("nonexistent", "alsononexistent"),
                  additionalInputs = listOf("**/*.inc"))

          then("should return empty list") {
            val result = mapper.discoverAdditionalInputFiles(config, projectDir)
            result.shouldBeEmpty()
          }

          projectDir.deleteRecursively()
        }

        `when`("discovered additional input files include non-existent files") {
          val projectDir = Files.createTempDirectory("test-project").toFile()

          // Create only the directory structure but no files
          val srcDir = File(projectDir, "src")
          srcDir.mkdirs()

          val config =
              AssemblyConfig(srcDirs = listOf("src"), additionalInputs = listOf("**/*.inc"))

          then("should filter out non-existent files") {
            val result = mapper.discoverAdditionalInputFiles(config, projectDir)
            result.shouldBeEmpty()
          }

          projectDir.deleteRecursively()
        }

        `when`("config has complex glob patterns for additional inputs") {
          val projectDir = Files.createTempDirectory("test-project").toFile()

          // Create nested directory structure
          val srcDir = File(projectDir, "src")
          val libDir = File(srcDir, "lib")
          val utilsDir = File(libDir, "utils")
          srcDir.mkdirs()
          libDir.mkdirs()
          utilsDir.mkdirs()

          val file1 = File(srcDir, "main.inc")
          val file2 = File(libDir, "common.inc")
          val file3 = File(utilsDir, "helper.inc")
          val file4 = File(srcDir, "main.asm") // Should not match *.inc pattern

          file1.writeText("// main include")
          file2.writeText("// common include")
          file3.writeText("// helper include")
          file4.writeText("// main asm")

          val config =
              AssemblyConfig(srcDirs = listOf("src"), additionalInputs = listOf("**/*.inc"))

          then("should match all files with recursive pattern") {
            val result = mapper.discoverAdditionalInputFiles(config, projectDir)
            result shouldHaveSize 3
            result.map { it.name } shouldContain "main.inc"
            result.map { it.name } shouldContain "common.inc"
            result.map { it.name } shouldContain "helper.inc"
          }

          projectDir.deleteRecursively()
        }
      }

      given("AssemblyConfigMapper integration with existing functionality") {
        val mapper = AssemblyConfigMapper()

        `when`("config has both regular patterns and additional inputs") {
          val projectDir = Files.createTempDirectory("test-integration").toFile()

          // Create test files
          val srcDir = File(projectDir, "src")
          srcDir.mkdirs()

          val mainAsm = File(srcDir, "main.asm")
          val utilsAsm = File(srcDir, "utils.asm")
          val configInc = File(srcDir, "config.inc")
          val headerInc = File(srcDir, "header.inc")

          mainAsm.writeText("* = \$1000\n  jmp init")
          utilsAsm.writeText("init:\n  rts")
          configInc.writeText("#define VERSION 1")
          headerInc.writeText("#define HEADER_SIZE 10")

          val config =
              AssemblyConfig(
                  srcDirs = listOf("src"),
                  includes = listOf("**/*.asm"),
                  excludes = listOf("test/**/*.asm"),
                  additionalInputs = listOf("**/*.inc"),
                  outputFormat = OutputFormat.PRG)

          then("should discover both source files and additional input files separately") {
            val sourceFiles = mapper.discoverSourceFiles(config, projectDir)
            val additionalFiles = mapper.discoverAdditionalInputFiles(config, projectDir)

            sourceFiles shouldHaveSize 2
            sourceFiles.map { it.name } shouldContain "main.asm"
            sourceFiles.map { it.name } shouldContain "utils.asm"

            additionalFiles shouldHaveSize 2
            additionalFiles.map { it.name } shouldContain "config.inc"
            additionalFiles.map { it.name } shouldContain "header.inc"
          }

          projectDir.deleteRecursively()
        }
      }
    })
