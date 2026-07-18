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
package com.github.c64lib.rbt.testing.a64spec.usecase

import com.github.c64lib.rbt.emulators.vice.usecase.RunTestOnViceUseCase
import com.github.c64lib.rbt.emulators.vice.usecase.port.RunTestOnVicePort
import com.github.c64lib.rbt.emulators.vice.usecase.port.ViceParameters
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.file.shouldNotExist
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files

/**
 * Fake VICE port whose [run] optionally (re)writes the `.specOut` result file, like real VICE does.
 */
private class FakeVicePort(private val onRun: (ViceParameters) -> Unit = {}) : RunTestOnVicePort {
  var runCount = 0

  override fun run(parameters: ViceParameters) {
    runCount++
    onRun(parameters)
  }
}

class Run64SpecTestUseCaseTest :
    BehaviorSpec({
      fun tempSpec(): File {
        val tempDir = Files.createTempDirectory("run64spec-test").toFile()
        val spec = File(tempDir, "math.spec.asm")
        spec.writeText("sfspec: init_spec()")
        return spec
      }

      given("a spec whose VICE run writes a fresh .specOut") {
        val spec = tempSpec()
        val specOut = File(spec.parentFile, "math.spec.specOut")
        val vicePort = FakeVicePort { specOut.writeText("Overall test report (3/3)") }
        val useCase = Run64SpecTestUseCase(RunTestOnViceUseCase(vicePort))

        `when`("running the spec") {
          val result = useCase.apply(spec)

          then("it parses the fresh result") {
            result.successCount shouldBe 3
            result.totalCount shouldBe 3
            vicePort.runCount shouldBe 1
          }
        }
      }

      given("a stale .specOut from a previous run that VICE does not rewrite") {
        val spec = tempSpec()
        val specOut = File(spec.parentFile, "math.spec.specOut")
        specOut.writeText("Overall test report (3/3)")
        val vicePort = FakeVicePort() // does not write .specOut
        val useCase = Run64SpecTestUseCase(RunTestOnViceUseCase(vicePort))

        `when`("running the spec") {
          then("the stale result is deleted before the run and never read back as a pass") {
            shouldThrow<IllegalStateException> { useCase.apply(spec) }
            vicePort.runCount shouldBe 1
            specOut.shouldNotExist()
          }
        }
      }

      given("a stale .specOut that VICE overwrites with a different result") {
        val spec = tempSpec()
        val specOut = File(spec.parentFile, "math.spec.specOut")
        specOut.writeText("Overall test report (3/3)")
        val vicePort = FakeVicePort { specOut.writeText("Overall test report (1/3)") }
        val useCase = Run64SpecTestUseCase(RunTestOnViceUseCase(vicePort))

        `when`("running the spec") {
          val result = useCase.apply(spec)

          then("the new content is parsed, not the stale one") {
            result.successCount shouldBe 1
            result.totalCount shouldBe 3
          }
        }
      }

      given("a spec with pre-existing .prg and .vs, and no .specOut") {
        val spec = tempSpec()
        val prg = File(spec.parentFile, "math.spec.prg")
        val vs = File(spec.parentFile, "math.spec.vs")
        prg.writeText("prg-bytes")
        vs.writeText("vs-bytes")
        val vicePort = FakeVicePort {
          File(spec.parentFile, "math.spec.specOut").writeText("(2/2)")
        }
        val useCase = Run64SpecTestUseCase(RunTestOnViceUseCase(vicePort))

        `when`("running the spec") {
          useCase.apply(spec)

          then(".prg and .vs are left untouched") {
            prg.shouldExist()
            vs.shouldExist()
            prg.readText() shouldBe "prg-bytes"
            vs.readText() shouldBe "vs-bytes"
          }
        }
      }

      given("a VICE run that never produces a .specOut") {
        val spec = tempSpec()
        val vicePort = FakeVicePort() // never writes .specOut
        val useCase = Run64SpecTestUseCase(RunTestOnViceUseCase(vicePort))

        `when`("running the spec") {
          then("a descriptive exception is thrown instead of a missing-file crash") {
            val exception = shouldThrow<IllegalStateException> { useCase.apply(spec) }
            exception.message shouldBe
                "Spec 'math.spec.asm' did not produce a result file: expected " +
                    "'${resultFile(spec)}' after running VICE"
          }
        }
      }
    })
