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
package com.github.c64lib.rbt.crunchers.exomizer.adapters.`in`.gradle

import com.github.c64lib.rbt.crunchers.exomizer.domain.MemOptions
import com.github.c64lib.rbt.crunchers.exomizer.domain.RawOptions
import com.github.c64lib.rbt.crunchers.exomizer.usecase.port.ExecuteExomizerPort
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.io.File

class CrunchMemTaskTest :
    BehaviorSpec({
      given("CrunchMem adapter") {
        `when`("port is created") {
          then("should not be null") {
            val mockPort = MockExecuteExomizerPort()
            mockPort shouldNotBe null
          }
        }

        `when`("executeMem is called") {
          then("should store correct parameters with default load address") {
            val mockPort = MockExecuteExomizerPort()
            val tempDir = java.nio.file.Files.createTempDirectory("test").toFile()
            val source = File(tempDir, "input.bin")
            source.writeText("test data")
            val output = File(tempDir, "output.bin")

            val options = MemOptions(loadAddress = "auto", forward = false)
            mockPort.executeMem(source, output, options)

            mockPort.lastSource shouldBe source
            mockPort.lastOutput shouldBe output
            mockPort.lastMemOptions shouldBe options
            tempDir.deleteRecursively()
          }

          then("should handle custom load addresses") {
            val mockPort = MockExecuteExomizerPort()
            val tempDir = java.nio.file.Files.createTempDirectory("test").toFile()
            val source = File(tempDir, "input.bin")
            source.writeText("test data")
            val output = File(tempDir, "output.bin")

            val optionsHex = MemOptions(loadAddress = "0x0800")
            mockPort.executeMem(source, output, optionsHex)
            mockPort.lastMemOptions?.loadAddress shouldBe "0x0800"

            val optionsDollar = MemOptions(loadAddress = "$2000")
            mockPort.executeMem(source, output, optionsDollar)
            mockPort.lastMemOptions?.loadAddress shouldBe "$2000"

            val optionsDecimal = MemOptions(loadAddress = "2048")
            mockPort.executeMem(source, output, optionsDecimal)
            mockPort.lastMemOptions?.loadAddress shouldBe "2048"

            val optionsNone = MemOptions(loadAddress = "none")
            mockPort.executeMem(source, output, optionsNone)
            mockPort.lastMemOptions?.loadAddress shouldBe "none"

            tempDir.deleteRecursively()
          }

          then("should handle all option combinations including memory-specific") {
            val mockPort = MockExecuteExomizerPort()
            val tempDir = java.nio.file.Files.createTempDirectory("test").toFile()
            val source = File(tempDir, "input.bin")
            source.writeText("test data")
            val output = File(tempDir, "output.bin")

            val rawOptions =
                RawOptions(
                    backwards = true,
                    reverse = true,
                    compatibility = true,
                    speedOverRatio = true,
                    encoding = "custom",
                    skipEncoding = true,
                    maxOffset = 32768,
                    maxLength = 1024,
                    passes = 50,
                    bitStreamTraits = 5,
                    bitStreamFormat = 20,
                    controlAddresses = "1234",
                    quiet = true,
                    brief = true)

            val options =
                MemOptions(rawOptions = rawOptions, loadAddress = "0x0801", forward = true)

            mockPort.executeMem(source, output, options)

            mockPort.lastMemOptions shouldBe options
            mockPort.lastMemOptions?.loadAddress shouldBe "0x0801"
            mockPort.lastMemOptions?.forward shouldBe true
            tempDir.deleteRecursively()
          }
        }
      }
    }) {
  private class MockExecuteExomizerPort : ExecuteExomizerPort {
    var lastSource: File? = null
    var lastOutput: File? = null
    var lastRawOptions: RawOptions? = null
    var lastMemOptions: MemOptions? = null

    override fun executeRaw(source: File, output: File, options: RawOptions) {
      lastSource = source
      lastOutput = output
      lastRawOptions = options
    }

    override fun executeMem(source: File, output: File, options: MemOptions) {
      lastSource = source
      lastOutput = output
      lastMemOptions = options
    }
  }
}
