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
import com.github.c64lib.rbt.flows.domain.config.GoattrackerConfig

/** Domain model for GoatTracker processing steps with type-safe configuration. */
class GoattrackerStep(
    name: String,
    inputs: List<String> = emptyList(),
    outputs: List<String> = emptyList(),
    val config: GoattrackerConfig = GoattrackerConfig()
) : FlowStep(name, "goattracker", inputs, outputs) {

  override fun execute(context: Map<String, Any>) {
    println("Executing GoatTracker step: $name")
    println("  Configuration: $config")
    println("  Processing ${inputs.size} input file(s)")

    // TODO: Integrate with actual GoatTracker processor
    inputs.forEach { inputPath ->
      println("  Processing: $inputPath")
      // The actual implementation will use the goattracker processor module
    }

    outputs.forEach { outputPath -> println("  Generating: $outputPath") }
  }

  override fun validate(): List<String> {
    val errors = mutableListOf<String>()

    if (inputs.isEmpty()) {
      errors.add("GoatTracker step '$name' requires at least one input .sng file")
    }

    if (outputs.isEmpty()) {
      errors.add("GoatTracker step '$name' requires at least one output file")
    }

    // Validate input file extensions
    inputs.forEach { inputPath ->
      if (!inputPath.endsWith(".sng", ignoreCase = true)) {
        errors.add("GoatTracker step '$name' expects .sng files, but got: $inputPath")
      }
    }

    // Validate channels
    if (config.channels !in 1..3) {
      errors.add(
          "GoatTracker step '$name' channels must be between 1 and 3, but got: ${config.channels}")
    }

    return errors
  }

  override fun getConfiguration(): Map<String, Any> {
    return mapOf(
        "exportFormat" to config.exportFormat.name,
        "optimization" to config.optimization,
        "frequency" to config.frequency.name,
        "channels" to config.channels,
        "filterSupport" to config.filterSupport)
  }

  override fun toString(): String {
    return "GoattrackerStep(name='$name', inputs=$inputs, outputs=$outputs, config=$config)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is GoattrackerStep) return false

    return name == other.name &&
        inputs == other.inputs &&
        outputs == other.outputs &&
        config == other.config
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + inputs.hashCode()
    result = 31 * result + outputs.hashCode()
    result = 31 * result + config.hashCode()
    return result
  }
}
