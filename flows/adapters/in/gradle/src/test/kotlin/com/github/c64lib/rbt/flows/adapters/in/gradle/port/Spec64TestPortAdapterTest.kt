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
package com.github.c64lib.rbt.flows.adapters.`in`.gradle.port

import com.github.c64lib.rbt.compilers.kickass.usecase.KickAssembleSpecUseCase
import com.github.c64lib.rbt.compilers.kickass.usecase.port.KickAssembleSpecPort
import com.github.c64lib.rbt.emulators.vice.usecase.RunTestOnViceUseCase
import com.github.c64lib.rbt.emulators.vice.usecase.port.RunTestOnVicePort
import com.github.c64lib.rbt.emulators.vice.usecase.port.ViceParameters
import com.github.c64lib.rbt.testing.a64spec.usecase.Run64SpecTestUseCase
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files

/** Captures the arguments passed to the spec-assembly port. */
private class CapturingSpecPort : KickAssembleSpecPort {
  var libDirs: List<File> = emptyList()
  var defines: List<String> = emptyList()
  var resultFileName: String = ""
  var source: File? = null

  override fun assemble(
      libDirs: List<File>,
      defines: List<String>,
      resultFileName: String,
      source: File
  ) {
    this.libDirs = libDirs
    this.defines = defines
    this.resultFileName = resultFileName
    this.source = source
  }
}

class Spec64TestPortAdapterTest :
    BehaviorSpec({
      given("a Spec64TestPortAdapter") {
        val specPort = CapturingSpecPort()
        val runCalls = mutableListOf<ViceParameters>()
        val vicePort =
            object : RunTestOnVicePort {
              var resultFile: File? = null
              var resultContent: String = ""

              override fun run(parameters: ViceParameters) {
                runCalls.add(parameters)
                resultFile?.writeText(resultContent)
              }
            }

        val libDir = File("lib")
        val adapter =
            Spec64TestPortAdapter(
                KickAssembleSpecUseCase(specPort),
                Run64SpecTestUseCase(RunTestOnViceUseCase(vicePort)),
                libDirs = listOf(libDir),
                defines = listOf("DEBUG"))

        `when`("assembling a spec") {
          val tempDir = Files.createTempDirectory("spec-adapter").toFile()
          val spec = File(tempDir, "math.spec.asm")
          spec.writeText("sfspec: init_spec()")

          adapter.assembleSpec(spec)

          then("libDirs and defines are propagated") {
            specPort.libDirs shouldContainExactly listOf(libDir)
            specPort.defines shouldContainExactly listOf("DEBUG")
            specPort.source shouldBe spec
          }

          then("the result file name follows the .specOut convention") {
            specPort.resultFileName shouldBe "math.spec.specOut"
          }

          tempDir.deleteRecursively()
        }

        `when`("running a spec") {
          val tempDir = Files.createTempDirectory("spec-adapter").toFile()
          val spec = File(tempDir, "math.spec.asm")
          spec.writeText("sfspec: init_spec()")
          // Run64SpecTestUseCase deletes any stale .specOut before running, so the fake VICE port
          // must write it during run() to mirror the real 64spec-in-VICE behaviour.
          vicePort.resultFile = File(tempDir, "math.spec.specOut")
          vicePort.resultContent = "Overall test report (3/3)"

          val result = adapter.runSpec(spec)

          then("it delegates to VICE and parses the result") {
            runCalls.size shouldBe 1
            result.successCount shouldBe 3
            result.totalCount shouldBe 3
          }

          tempDir.deleteRecursively()
        }
      }
    })
