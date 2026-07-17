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

import com.github.c64lib.rbt.flows.domain.FlowStep
import com.github.c64lib.rbt.flows.domain.StepExecutionException
import com.github.c64lib.rbt.flows.usecase.port.TestPort
import com.github.c64lib.rbt.testing.a64spec.usecase.TestResult

/**
 * 64spec test step: assembles and runs one or more `*.spec.asm` specs on VICE.
 *
 * [specs] are the spec sources to execute; [inputs] additionally includes watched sources the specs
 * `#import` (so editing a library source re-runs the tests). All specs run; the step throws
 * afterwards if any spec failed. Requires [TestPort] injection via the Gradle task.
 */
data class TestStep(
    override val name: String,
    val specs: List<String> = emptyList(),
    override val inputs: List<String> = emptyList(),
    override val outputs: List<String> = emptyList(),
    private var testPort: TestPort? = null
) : FlowStep(name, "test", inputs, outputs) {

  /** Injects the test port dependency. Called by the adapter layer before execution. */
  fun setTestPort(port: TestPort) {
    this.testPort = port
  }

  override fun execute(context: Map<String, Any>) {
    val port = validatePort(testPort, "TestPort")
    val projectRootDir = getProjectRootDir(context)

    val specFiles = resolveInputFiles(specs, projectRootDir)

    val results = mutableListOf<TestResult>()
    try {
      specFiles.forEach { specFile ->
        port.assembleSpec(specFile)
        results.add(port.runSpec(specFile))
      }
    } catch (e: Exception) {
      throw StepExecutionException("Test execution failed: ${e.message}", name, e)
    }

    val failed = results.filter { it.successCount != it.totalCount }
    results.forEach { result -> println("  Tests execution ${result.message}") }
    if (failed.isNotEmpty()) {
      val totalSuccess = results.sumOf { it.successCount }
      val totalCount = results.sumOf { it.totalCount }
      throw StepExecutionException("64spec tests failed: overall ($totalSuccess/$totalCount)", name)
    }
  }

  override fun validate(): List<String> {
    val errors = mutableListOf<String>()

    if (specs.isEmpty()) {
      errors.add("Test step '$name' requires at least one spec (.spec.asm) file")
    }

    specs.forEach { specPath ->
      if (!specPath.endsWith(".spec.asm", ignoreCase = true)) {
        errors.add("Test step '$name' expects .spec.asm spec files, but got: $specPath")
      }
      if (specPath !in inputs) {
        errors.add("Test step '$name' spec '$specPath' must also be declared as an input")
      }
    }

    return errors
  }

  override fun getConfiguration(): Map<String, Any> =
      mapOf("specs" to specs, "inputs" to inputs, "outputs" to outputs)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is TestStep) return false

    return name == other.name &&
        specs == other.specs &&
        inputs == other.inputs &&
        outputs == other.outputs
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + specs.hashCode()
    result = 31 * result + inputs.hashCode()
    result = 31 * result + outputs.hashCode()
    return result
  }
}
