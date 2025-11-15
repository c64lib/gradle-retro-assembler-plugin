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
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class GradleExomizerAdapterTest :
    BehaviorSpec({
      given("GradleExomizerAdapter") {
        val tempDir = java.nio.file.Files.createTempDirectory("test").toFile()

        `when`("raw mode options are configured") {
          then("should handle minimal options correctly") {
            val options = RawOptions()
            options.backwards shouldBe false
            options.reverse shouldBe false
            options.maxOffset shouldBe 65535
            options.maxLength shouldBe 65535
            options.passes shouldBe 100
          }

          then("should handle all boolean flags") {
            val options =
                RawOptions(
                    backwards = true,
                    reverse = true,
                    compatibility = true,
                    speedOverRatio = true,
                    skipEncoding = true,
                    quiet = true,
                    brief = true)

            options.backwards shouldBe true
            options.reverse shouldBe true
            options.compatibility shouldBe true
            options.speedOverRatio shouldBe true
            options.skipEncoding shouldBe true
            options.quiet shouldBe true
            options.brief shouldBe true
          }

          then("should handle string options") {
            val options = RawOptions(encoding = "my-encoding", controlAddresses = "1234,5678")

            options.encoding shouldBe "my-encoding"
            options.controlAddresses shouldBe "1234,5678"
          }

          then("should handle numeric options") {
            val options =
                RawOptions(
                    maxOffset = 32768,
                    maxLength = 512,
                    passes = 200,
                    bitStreamTraits = 3,
                    bitStreamFormat = 15)

            options.maxOffset shouldBe 32768
            options.maxLength shouldBe 512
            options.passes shouldBe 200
            options.bitStreamTraits shouldBe 3
            options.bitStreamFormat shouldBe 15
          }
        }

        `when`("memory mode options are configured") {
          then("should use auto load address by default") {
            val options = MemOptions()
            options.loadAddress shouldBe "auto"
            options.forward shouldBe false
          }

          then("should accept various load address formats") {
            listOf("auto", "none", "0x0800", "$2000", "2048", "0x0801").forEach { address ->
              val options = MemOptions(loadAddress = address)
              options.loadAddress shouldBe address
            }
          }

          then("should handle forward flag") {
            val options = MemOptions(forward = true)
            options.forward shouldBe true
          }

          then("should combine raw options with memory-specific options") {
            val rawOptions = RawOptions(backwards = true, quiet = true)
            val options =
                MemOptions(rawOptions = rawOptions, loadAddress = "0x0800", forward = true)

            options.backwards shouldBe true
            options.quiet shouldBe true
            options.loadAddress shouldBe "0x0800"
            options.forward shouldBe true
          }
        }

        afterSpec { tempDir.deleteRecursively() }
      }
    })
