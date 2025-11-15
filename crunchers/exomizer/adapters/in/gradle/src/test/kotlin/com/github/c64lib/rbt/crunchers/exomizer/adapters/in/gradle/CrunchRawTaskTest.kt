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

class CrunchRawTaskTest :
    BehaviorSpec({
      given("CrunchRaw adapter") {
        `when`("port is created") {
          then("should not be null") {
            val mockPort = MockExecuteExomizerPort()
            mockPort shouldNotBe null
          }
        }

        `when`("executeRaw is called") {
          then("should store correct parameters") {
            val mockPort = MockExecuteExomizerPort()
            val tempDir = java.nio.file.Files.createTempDirectory("test").toFile()
            val source = File(tempDir, "input.bin")
            source.writeText("test data")
            val output = File(tempDir, "output.bin")

            val options = RawOptions(backwards = true, quiet = false)
            mockPort.executeRaw(source, output, options)

            mockPort.lastSource shouldBe source
            mockPort.lastOutput shouldBe output
            mockPort.lastRawOptions shouldBe options
            tempDir.deleteRecursively()
          }

          then("should handle all option combinations") {
            val mockPort = MockExecuteExomizerPort()
            val tempDir = java.nio.file.Files.createTempDirectory("test").toFile()
            val source = File(tempDir, "input.bin")
            source.writeText("test data")
            val output = File(tempDir, "output.bin")

            val options =
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

            mockPort.executeRaw(source, output, options)

            mockPort.lastRawOptions shouldBe options
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
