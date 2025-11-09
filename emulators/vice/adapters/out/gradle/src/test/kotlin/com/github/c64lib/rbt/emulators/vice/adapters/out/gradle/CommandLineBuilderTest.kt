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
package com.github.c64lib.rbt.emulators.vice.adapters.out.gradle

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class CommandLineBuilderTest :
    BehaviorSpec({
      lateinit var builder: CommandLineBuilder

      beforeEach { builder = CommandLineBuilder("myExecutable") }

      given("a CommandLineBuilder with startWithDefault true") {
        builder = CommandLineBuilder("myExecutable", true)

        `when`("build is called") {
          val result = builder.build()

          then("it should return list with executable and default switch") {
            result shouldBe listOf("myExecutable", "-default")
          }
        }
      }

      given("a CommandLineBuilder with startWithDefault false") {
        builder = CommandLineBuilder("myExecutable", false)

        `when`("build is called") {
          val result = builder.build()

          then("it should return list with only executable") {
            result shouldBe listOf("myExecutable")
          }
        }
      }

      given("a CommandLineBuilder") {
        `when`("switch is called with a name and value") {
          builder.switch("option", "value")
          val result = builder.build()

          then("it should add switch with value") {
            result shouldBe listOf("myExecutable", "-default", "-option", "value")
          }
        }

        `when`("switch is called with a name only") {
          builder.switch("option")
          val result = builder.build()

          then("it should add switch without value") {
            result shouldBe listOf("myExecutable", "-default", "-option")
          }
        }

        `when`("switchIf is called with true condition") {
          builder.switchIf(true, "option", "value")
          val result = builder.build()

          then("it should add switch") {
            result shouldBe listOf("myExecutable", "-default", "-option", "value")
          }
        }

        `when`("switchIf is called with false condition") {
          builder.switchIf(false, "option", "value")
          val result = builder.build()

          then("it should not add switch") { result shouldBe listOf("myExecutable", "-default") }
        }

        `when`("toggleSwitch is called with true value") {
          builder.toggleSwitch("option", true)
          val result = builder.build()

          then("it should add switch with minus prefix") {
            result shouldBe listOf("myExecutable", "-default", "-option")
          }
        }

        `when`("toggleSwitch is called with false value") {
          builder.toggleSwitch("option", false)
          val result = builder.build()

          then("it should add switch with plus prefix") {
            result shouldBe listOf("myExecutable", "-default", "+option")
          }
        }

        `when`("toggleSwitchIf is called with true condition") {
          builder.toggleSwitchIf(true, "option", true)
          val result = builder.build()

          then("it should add toggle switch") {
            result shouldBe listOf("myExecutable", "-default", "-option")
          }
        }

        `when`("toggleSwitchIf is called with false condition") {
          builder.toggleSwitchIf(false, "option", true)
          val result = builder.build()

          then("it should not add toggle switch") {
            result shouldBe listOf("myExecutable", "-default")
          }
        }

        `when`("toString is called") {
          builder.switch("option", "value")
          val result = builder.toString()

          then("it should return switches joined by spaces") {
            result shouldBe "myExecutable -default -option value"
          }
        }

        `when`("switch is called with blank name") {
          then("it should throw exception") {
            val exception = shouldThrow<IllegalArgumentException> { builder.switch("", "value") }
            exception.message shouldBe "Switch name cannot be blank"
          }
        }

        `when`("switch is called with blank value") {
          then("it should throw exception") {
            val exception = shouldThrow<IllegalArgumentException> { builder.switch("option", "") }
            exception.message shouldBe "Switch value cannot be blank"
          }
        }
      }
    })
