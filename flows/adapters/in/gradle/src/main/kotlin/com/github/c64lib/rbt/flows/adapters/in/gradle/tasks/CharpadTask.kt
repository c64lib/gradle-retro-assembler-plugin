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
package com.github.c64lib.rbt.flows.adapters.`in`.gradle.tasks

import com.github.c64lib.rbt.flows.domain.FlowStep
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.OutputFiles

/** Gradle task for executing Charpad processing steps with proper incremental build support. */
abstract class CharpadTask : BaseFlowStepTask() {

  @get:OutputFiles abstract val outputFiles: ConfigurableFileCollection

  init {
    description = "Processes Charpad (.ctm) files to generate character sets and maps"
  }

  override fun executeStepLogic(step: FlowStep) {
    val validationErrors = validateStep(step)
    if (validationErrors.isNotEmpty()) {
      throw IllegalStateException(
          "Charpad step validation failed: ${validationErrors.joinToString(", ")}")
    }

    logger.info("Processing Charpad files from inputs: ${step.inputs}")
    logger.info("Output directory: ${outputDirectory.get().asFile.absolutePath}")

    // TODO: Integrate with actual Charpad processor from processors/charpad module
    // For now, simulate the processing
    step.inputs.forEach { inputPath ->
      logger.info("Processing Charpad file: $inputPath")

      // Example: Convert .ctm file to .chr and .map files
      val inputFile = project.file(inputPath)
      if (inputFile.exists() && inputFile.extension == "ctm") {
        val baseName = inputFile.nameWithoutExtension
        val outputDir = outputDirectory.get().asFile

        // Create output files (placeholder - actual implementation will use charpad processor)
        val chrFile = outputDir.resolve("$baseName.chr")
        val mapFile = outputDir.resolve("$baseName.map")

        chrFile.writeText("// Generated character set from $inputPath\n")
        mapFile.writeText("// Generated character map from $inputPath\n")

        logger.info("Generated: ${chrFile.absolutePath}")
        logger.info("Generated: ${mapFile.absolutePath}")
      } else {
        logger.warn("Input file not found or not a .ctm file: $inputPath")
      }
    }
  }

  override fun validateStep(step: FlowStep): List<String> {
    val errors = super.validateStep(step).toMutableList()

    // Charpad-specific validations
    if (step.inputs.isEmpty()) {
      errors.add("Charpad step requires at least one input .ctm file")
    }

    step.inputs.forEach { inputPath ->
      val inputFile = project.file(inputPath)
      if (!inputFile.name.endsWith(".ctm")) {
        errors.add("Charpad step expects .ctm files, but got: $inputPath")
      }
    }

    return errors
  }
}
