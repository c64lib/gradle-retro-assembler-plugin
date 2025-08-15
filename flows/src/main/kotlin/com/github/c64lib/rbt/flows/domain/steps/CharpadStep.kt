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
import com.github.c64lib.rbt.flows.domain.config.CharpadConfig

/** Domain model for Charpad processing steps with type-safe configuration. */
class CharpadStep(
    name: String,
    inputs: List<String> = emptyList(),
    outputs: List<String> = emptyList(),
    val config: CharpadConfig = CharpadConfig()
) : FlowStep(name, "charpad", inputs, outputs) {

  override fun execute(context: Map<String, Any>) {
    println("Executing Charpad step: $name")
    println("  Configuration: $config")
    println("  Processing ${inputs.size} input file(s)")

    // TODO: Integrate with actual Charpad processor
    inputs.forEach { inputPath ->
      println("  Processing: $inputPath")
      // The actual implementation will use the charpad processor module
    }

    outputs.forEach { outputPath -> println("  Generating: $outputPath") }
  }

  override fun validate(): List<String> {
    val errors = mutableListOf<String>()

    if (inputs.isEmpty()) {
      errors.add("Charpad step '$name' requires at least one input .ctm file")
    }

    if (outputs.isEmpty()) {
      errors.add("Charpad step '$name' requires at least one output file")
    }

    // Validate input file extensions
    inputs.forEach { inputPath ->
      if (!inputPath.endsWith(".ctm", ignoreCase = true)) {
        errors.add("Charpad step '$name' expects .ctm files, but got: $inputPath")
      }
    }

    // Validate tile size
    if (config.tileSize !in listOf(8, 16, 32)) {
      errors.add("Charpad step '$name' tile size must be 8, 16, or 32, but got: ${config.tileSize}")
    }

    return errors
  }

  override fun getConfiguration(): Map<String, Any> {
    return mapOf(
        "compression" to config.compression.name,
        "exportFormat" to config.exportFormat.name,
        "tileSize" to config.tileSize,
        "charsetOptimization" to config.charsetOptimization,
        "generateMap" to config.generateMap,
        "generateCharset" to config.generateCharset)
  }

  override fun toString(): String {
    return "CharpadStep(name='$name', inputs=$inputs, outputs=$outputs, config=$config)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is CharpadStep) return false

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
