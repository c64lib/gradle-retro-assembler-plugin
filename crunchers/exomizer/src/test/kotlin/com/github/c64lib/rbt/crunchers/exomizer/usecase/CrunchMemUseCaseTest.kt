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
package com.github.c64lib.rbt.crunchers.exomizer.usecase

import com.github.c64lib.rbt.crunchers.exomizer.domain.CrunchMemCommand
import com.github.c64lib.rbt.crunchers.exomizer.domain.ExomizerValidationException
import com.github.c64lib.rbt.crunchers.exomizer.domain.MemOptions
import com.github.c64lib.rbt.crunchers.exomizer.domain.RawOptions
import com.github.c64lib.rbt.crunchers.exomizer.usecase.port.ExecuteExomizerPort
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.io.File

class CrunchMemUseCaseTest :
    BehaviorSpec({
      given("CrunchMemUseCase") {
        `when`("apply is called") {
          then("should validate source file exists") {
            val tempDir = java.nio.file.Files.createTempDirectory("test").toFile()
            val port = MockExecuteExomizerPort()
            val useCase = CrunchMemUseCase(port)
            val output = File(tempDir, "output.bin")

            val nonExistentSource = File(tempDir, "nonexistent.bin")
            val memOptions = MemOptions()
            val command = CrunchMemCommand(nonExistentSource, output, memOptions)

            shouldThrow<ExomizerValidationException> { useCase.apply(command) }
          }

          then("should accept auto load address") {
            val tempDir = java.nio.file.Files.createTempDirectory("test").toFile()
            val port = MockExecuteExomizerPort()
            val useCase = CrunchMemUseCase(port)

            val source = File(tempDir, "input.bin")
            source.writeText("test data")
            val output = File(tempDir, "output.bin")

            val memOptions = MemOptions(loadAddress = "auto")
            val command = CrunchMemCommand(source, output, memOptions)

            useCase.apply(command)
            port.executedMem shouldBe true
          }

          then("should accept none load address") {
            val tempDir = java.nio.file.Files.createTempDirectory("test").toFile()
            val port = MockExecuteExomizerPort()
            val useCase = CrunchMemUseCase(port)

            val source = File(tempDir, "input.bin")
            source.writeText("test data")
            val output = File(tempDir, "output.bin")

            val memOptions = MemOptions(loadAddress = "none")
            val command = CrunchMemCommand(source, output, memOptions)

            useCase.apply(command)
            port.executedMem shouldBe true
          }

          then("should accept hex load address") {
            val tempDir = java.nio.file.Files.createTempDirectory("test").toFile()
            val port = MockExecuteExomizerPort()
            val useCase = CrunchMemUseCase(port)

            val source = File(tempDir, "input.bin")
            source.writeText("test data")
            val output = File(tempDir, "output.bin")

            val memOptions = MemOptions(loadAddress = "0x0800")
            val command = CrunchMemCommand(source, output, memOptions)

            useCase.apply(command)
            port.executedMem shouldBe true
          }

          then("should accept dollar-notation hex load address") {
            val tempDir = java.nio.file.Files.createTempDirectory("test").toFile()
            val port = MockExecuteExomizerPort()
            val useCase = CrunchMemUseCase(port)

            val source = File(tempDir, "input.bin")
            source.writeText("test data")
            val output = File(tempDir, "output.bin")

            val memOptions = MemOptions(loadAddress = "$0800")
            val command = CrunchMemCommand(source, output, memOptions)

            useCase.apply(command)
            port.executedMem shouldBe true
          }

          then("should accept decimal load address") {
            val tempDir = java.nio.file.Files.createTempDirectory("test").toFile()
            val port = MockExecuteExomizerPort()
            val useCase = CrunchMemUseCase(port)

            val source = File(tempDir, "input.bin")
            source.writeText("test data")
            val output = File(tempDir, "output.bin")

            val memOptions = MemOptions(loadAddress = "2048")
            val command = CrunchMemCommand(source, output, memOptions)

            useCase.apply(command)
            port.executedMem shouldBe true
          }

          then("should reject invalid load address") {
            val tempDir = java.nio.file.Files.createTempDirectory("test").toFile()
            val port = MockExecuteExomizerPort()
            val useCase = CrunchMemUseCase(port)

            val source = File(tempDir, "input.bin")
            source.writeText("test data")
            val output = File(tempDir, "output.bin")

            val memOptions = MemOptions(loadAddress = "invalid")
            val command = CrunchMemCommand(source, output, memOptions)

            shouldThrow<ExomizerValidationException> { useCase.apply(command) }
          }
        }
      }
    }) {
  private class MockExecuteExomizerPort : ExecuteExomizerPort {
    var executedRaw = false
    var executedMem = false

    override fun executeRaw(source: File, output: File, options: RawOptions) {
      executedRaw = true
    }

    override fun executeMem(source: File, output: File, options: MemOptions) {
      executedMem = true
    }
  }
}
