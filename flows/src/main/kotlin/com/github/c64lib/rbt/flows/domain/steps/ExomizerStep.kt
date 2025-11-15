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
import com.github.c64lib.rbt.flows.domain.StepValidationException
import com.github.c64lib.rbt.flows.domain.port.ExomizerPort
import java.io.File

/**
 * Exomizer compression step.
 *
 * Supports raw and memory compression modes. Validates input file existence and output path
 * writability. Requires ExomizerPort injection via Gradle task.
 */
data class ExomizerStep(
    override val name: String,
    override val inputs: List<String> = emptyList(),
    override val outputs: List<String> = emptyList(),
    val mode: String = "raw", // "raw" or "mem"
    val loadAddress: String = "auto", // for mem mode
    val forward: Boolean = false, // for mem mode
    private var exomizerPort: ExomizerPort? = null
) : FlowStep(name, "exomizer", inputs, outputs) {

  /**
   * Injects the exomizer port dependency. This is called by the adapter layer when the step is
   * prepared for execution.
   */
  fun setExomizerPort(port: ExomizerPort) {
    this.exomizerPort = port
  }

  override fun execute(context: Map<String, Any>) {
    val port = exomizerPort ?: throw StepExecutionException("ExomizerPort not injected", name)

    val projectRootDir = getProjectRootDir(context)

    if (inputs.isEmpty() || outputs.isEmpty()) {
      throw StepValidationException("Exomizer step requires both input and output paths", name)
    }

    val inputFile =
        if (File(inputs[0]).isAbsolute) {
          File(inputs[0])
        } else {
          File(projectRootDir, inputs[0])
        }

    if (!inputFile.exists()) {
      throw StepValidationException("Input file does not exist: ${inputFile.absolutePath}", name)
    }

    val outputFile =
        if (File(outputs[0]).isAbsolute) {
          File(outputs[0])
        } else {
          File(projectRootDir, outputs[0])
        }

    try {
      when (mode.lowercase()) {
        "raw" -> port.crunchRaw(inputFile, outputFile)
        "mem" -> port.crunchMem(inputFile, outputFile, loadAddress, forward)
        else -> throw StepValidationException("Unknown Exomizer mode: $mode", name)
      }
    } catch (e: StepExecutionException) {
      throw e
    } catch (e: StepValidationException) {
      throw e
    } catch (e: Exception) {
      throw StepExecutionException("Exomizer compression failed: ${e.message}", name, e)
    }

    println("  Generated output: ${outputs[0]}")
  }

  override fun validate(): List<String> {
    val errors = mutableListOf<String>()

    if (inputs.isEmpty()) {
      errors.add("Exomizer step '$name' requires an input file")
    }

    if (outputs.isEmpty()) {
      errors.add("Exomizer step '$name' requires an output file")
    }

    if (mode != "raw" && mode != "mem") {
      errors.add("Exomizer step '$name' mode must be 'raw' or 'mem', but got: '$mode'")
    }

    if (mode == "mem" && loadAddress != "auto" && loadAddress != "none") {
      // Try to validate load address format
      try {
        if (loadAddress.startsWith("0x", ignoreCase = true)) {
          loadAddress.substring(2).toLong(16)
        } else if (loadAddress.startsWith("$")) {
          loadAddress.substring(1).toLong(16)
        } else {
          loadAddress.toLong()
        }
      } catch (e: NumberFormatException) {
        errors.add(
            "Exomizer step '$name' has invalid load address: '$loadAddress'. Use 'auto', 'none', or a valid hex/decimal address.")
      }
    }

    return errors
  }

  override fun getConfiguration(): Map<String, Any> {
    return mapOf(
        "mode" to mode,
        "loadAddress" to (if (mode == "mem") loadAddress else "N/A"),
        "forward" to (if (mode == "mem") forward else "N/A"))
  }
}
