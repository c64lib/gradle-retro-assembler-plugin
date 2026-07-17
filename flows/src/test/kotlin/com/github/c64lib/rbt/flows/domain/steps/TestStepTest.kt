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
package com.github.c64lib.rbt.flows.domain.steps

import com.github.c64lib.rbt.flows.domain.StepExecutionException
import com.github.c64lib.rbt.flows.usecase.port.TestPort
import com.github.c64lib.rbt.testing.a64spec.usecase.TestResult
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files

/** Records the sequence of port calls so tests can assert assemble-before-run ordering. */
private class RecordingTestPort(private val results: Map<String, TestResult>) : TestPort {
  val assembled = mutableListOf<String>()
  val ran = mutableListOf<String>()

  override fun assembleSpec(source: File) {
    assembled.add(source.name)
  }

  override fun runSpec(source: File): TestResult {
    ran.add(source.name)
    return results[source.name] ?: TestResult(1, 1, "(1/1)")
  }
}

class TestStepTest :
    BehaviorSpec({
      isolationMode = IsolationMode.InstancePerTest

      fun projectWith(vararg files: String): File {
        val tempDir = Files.createTempDirectory("test-step").toFile()
        files.forEach { rel ->
          val f = File(tempDir, rel)
          f.parentFile.mkdirs()
          f.writeText("sfspec: init_spec()")
        }
        return tempDir
      }

      Given("a TestStep with specs and watched inputs") {
        val step =
            TestStep(
                name = "runSpecs",
                specs = listOf("spec/math.spec.asm"),
                inputs = listOf("spec/math.spec.asm", "lib/math.asm"),
                outputs = listOf("spec/math.specOut"))

        When("getting step properties") {
          Then("it exposes name, type, specs and inputs") {
            step.name shouldBe "runSpecs"
            step.taskType shouldBe "test"
            step.specs shouldBe listOf("spec/math.spec.asm")
            step.inputs shouldBe listOf("spec/math.spec.asm", "lib/math.asm")
            step.outputs shouldBe listOf("spec/math.specOut")
          }
        }
      }

      Given("a TestStep with a passing spec") {
        val port = RecordingTestPort(mapOf("math.spec.asm" to TestResult(3, 3, "(3/3)")))
        val step =
            TestStep(
                name = "runSpecs",
                specs = listOf("spec/math.spec.asm"),
                inputs = listOf("spec/math.spec.asm"))
        step.setTestPort(port)
        val tempDir = projectWith("spec/math.spec.asm")
        val context = mapOf<String, Any>("projectRootDir" to tempDir)

        When("executing") {
          step.execute(context)

          Then("it assembles then runs the spec") {
            port.assembled shouldBe listOf("math.spec.asm")
            port.ran shouldBe listOf("math.spec.asm")
          }
        }

        tempDir.deleteRecursively()
      }

      Given("a TestStep where one of several specs fails") {
        val port =
            RecordingTestPort(
                mapOf(
                    "a.spec.asm" to TestResult(2, 2, "(2/2)"),
                    "b.spec.asm" to TestResult(1, 3, "(1/3)"),
                    "c.spec.asm" to TestResult(4, 4, "(4/4)")))
        val step =
            TestStep(
                name = "runSpecs",
                specs = listOf("spec/a.spec.asm", "spec/b.spec.asm", "spec/c.spec.asm"),
                inputs = listOf("spec/a.spec.asm", "spec/b.spec.asm", "spec/c.spec.asm"))
        step.setTestPort(port)
        val tempDir = projectWith("spec/a.spec.asm", "spec/b.spec.asm", "spec/c.spec.asm")
        val context = mapOf<String, Any>("projectRootDir" to tempDir)

        When("executing") {
          val exception = shouldThrow<StepExecutionException> { step.execute(context) }

          Then("all specs still run and the step fails afterwards") {
            port.ran shouldBe listOf("a.spec.asm", "b.spec.asm", "c.spec.asm")
            exception.message shouldBe "64spec tests failed: overall (7/9)"
            exception.stepName shouldBe "runSpecs"
          }
        }

        tempDir.deleteRecursively()
      }

      Given("a TestStep without a port") {
        val step =
            TestStep(
                name = "noPort",
                specs = listOf("spec/math.spec.asm"),
                inputs = listOf("spec/math.spec.asm"))
        val tempDir = projectWith("spec/math.spec.asm")
        val context = mapOf<String, Any>("projectRootDir" to tempDir)

        When("executing without port injection") {
          val exception = shouldThrow<StepExecutionException> { step.execute(context) }

          Then("it throws about the missing port") {
            exception.message shouldBe "TestPort not injected"
            exception.stepName shouldBe "noPort"
          }
        }

        tempDir.deleteRecursively()
      }

      Given("validation scenarios") {
        When("validating a step with no specs") {
          val errors = TestStep(name = "noSpecs").validate()

          Then("it reports the missing spec") {
            errors shouldContain "Test step 'noSpecs' requires at least one spec (.spec.asm) file"
          }
        }

        When("validating a step whose spec is not a .spec.asm file") {
          val step =
              TestStep(
                  name = "badSpec",
                  specs = listOf("spec/math.asm"),
                  inputs = listOf("spec/math.asm"))
          val errors = step.validate()

          Then("it rejects the non-spec file") {
            errors shouldContain
                "Test step 'badSpec' expects .spec.asm spec files, but got: spec/math.asm"
          }
        }

        When("validating a step whose spec is missing from inputs") {
          val step =
              TestStep(
                  name = "missingInput",
                  specs = listOf("spec/math.spec.asm"),
                  inputs = listOf("lib/math.asm"))
          val errors = step.validate()

          Then("it requires the spec to be an input") {
            errors shouldContain
                "Test step 'missingInput' spec 'spec/math.spec.asm' must also be declared as an input"
          }
        }

        When("validating a valid step with specs and watched non-spec inputs") {
          val step =
              TestStep(
                  name = "valid",
                  specs = listOf("spec/math.spec.asm"),
                  inputs = listOf("spec/math.spec.asm", "lib/math.asm"))
          val errors = step.validate()

          Then("watched non-spec inputs are accepted and validation passes") {
            errors.shouldBeEmpty()
          }
        }
      }

      Given("only specs (not watched inputs) are executed") {
        val port = RecordingTestPort(emptyMap())
        val step =
            TestStep(
                name = "runSpecs",
                specs = listOf("spec/math.spec.asm"),
                inputs = listOf("spec/math.spec.asm", "lib/math.asm", "lib/util.asm"))
        step.setTestPort(port)
        val tempDir = projectWith("spec/math.spec.asm", "lib/math.asm", "lib/util.asm")
        val context = mapOf<String, Any>("projectRootDir" to tempDir)

        When("executing") {
          step.execute(context)

          Then("only the spec files are assembled and run") {
            port.assembled shouldHaveSize 1
            port.assembled shouldBe listOf("math.spec.asm")
            port.ran shouldBe listOf("math.spec.asm")
          }
        }

        tempDir.deleteRecursively()
      }
    })
