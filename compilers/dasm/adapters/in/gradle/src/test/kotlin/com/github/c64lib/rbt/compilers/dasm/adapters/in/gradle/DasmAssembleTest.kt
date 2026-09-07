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
package com.github.c64lib.rbt.compilers.dasm.adapters.`in`.gradle

import com.github.c64lib.rbt.compilers.dasm.usecase.DasmAssembleUseCase
import com.github.c64lib.rbt.compilers.dasm.usecase.port.DasmAssemblePort
import com.github.c64lib.rbt.shared.domain.OutputFormat
import com.github.c64lib.rbt.shared.gradle.dsl.RetroAssemblerPluginExtension
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files
import org.gradle.testfixtures.ProjectBuilder

private class RecordedAssembly(
    val libDirs: List<File>,
    val defines: Map<String, String>,
    val source: File,
    val outputFormat: Int,
    val outputFile: File?
)

private class RecordingPort : DasmAssemblePort {
  val assemblies = mutableListOf<RecordedAssembly>()

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
    assemblies.add(RecordedAssembly(libDirs, defines, source, outputFormat, outputFile))
  }
}

class DasmAssembleTest :
    BehaviorSpec({
      isolationMode = IsolationMode.InstancePerTest

      fun newTask(
          configure: RetroAssemblerPluginExtension.() -> Unit
      ): Triple<DasmAssemble, RecordingPort, File> {
        val projectDir = Files.createTempDirectory("dasm-assemble-test").toFile()
        val project = ProjectBuilder.builder().withProjectDir(projectDir).build()
        val task = project.tasks.create("asmUnderTest", DasmAssemble::class.java)
        val port = RecordingPort()
        task.extension = RetroAssemblerPluginExtension().apply(configure)
        task.dasmAssembleUseCase = DasmAssembleUseCase(port)
        return Triple(task, port, projectDir)
      }

      Given("a project with one source file and PRG output") {
        val (task, port, projectDir) =
            newTask {
              srcDirs = arrayOf("src")
              defines = arrayOf("DEBUG=1", "BARE")
              libDirs = arrayOf("lib")
              outputFormat = OutputFormat.PRG
            }
        val srcDir = File(projectDir, "src")
        srcDir.mkdirs()
        val source = File(srcDir, "main.asm")
        source.writeText("  lda #\$01")

        When("assembling") {
          task.assemble()

          Then("one dasm invocation happens with mapped parameters") {
            port.assemblies shouldHaveSize 1
            val assembly = port.assemblies.first()
            assembly.source.canonicalFile shouldBe source.canonicalFile
            assembly.defines shouldBe mapOf("DEBUG" to "1", "BARE" to "")
            assembly.outputFormat shouldBe 1
            assembly.outputFile?.canonicalFile shouldBe File(srcDir, "main.prg").canonicalFile
            assembly.libDirs shouldHaveSize 1
          }
        }
      }

      Given("a project with BIN output format") {
        val (task, port, projectDir) = newTask { outputFormat = OutputFormat.BIN }
        val source = File(projectDir, "game.asm")
        source.writeText("  nop")

        When("assembling") {
          task.assemble()

          Then("dasm raw format and .bin output are used") {
            port.assemblies shouldHaveSize 1
            val assembly = port.assemblies.first()
            assembly.outputFormat shouldBe 3
            assembly.outputFile?.name shouldBe "game.bin"
          }
        }
      }
    })
