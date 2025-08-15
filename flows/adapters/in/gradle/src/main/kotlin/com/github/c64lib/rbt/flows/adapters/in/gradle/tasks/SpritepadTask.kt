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

/** Gradle task for executing Spritepad processing steps with proper incremental build support. */
abstract class SpritepadTask : BaseFlowStepTask() {

  @get:OutputFiles abstract val outputFiles: ConfigurableFileCollection

  init {
    description = "Processes Spritepad (.spd) files to generate sprite data"
  }

  override fun executeStepLogic(step: FlowStep) {
    val validationErrors = validateStep(step)
    if (validationErrors.isNotEmpty()) {
      throw IllegalStateException(
          "Spritepad step validation failed: ${validationErrors.joinToString(", ")}")
    }

    logger.info("Processing Spritepad files from inputs: ${step.inputs}")
    logger.info("Output directory: ${outputDirectory.get().asFile.absolutePath}")

    // TODO: Integrate with actual Spritepad processor from processors/spritepad module
    // For now, simulate the processing
    step.inputs.forEach { inputPath ->
      logger.info("Processing Spritepad file: $inputPath")

      // Example: Convert .spd file to .spr file
      val inputFile = project.file(inputPath)
      if (inputFile.exists() && inputFile.extension == "spd") {
        val baseName = inputFile.nameWithoutExtension
        val outputDir = outputDirectory.get().asFile

        // Create output files (placeholder - actual implementation will use spritepad processor)
        val sprFile = outputDir.resolve("$baseName.spr")

        sprFile.writeText("// Generated sprite data from $inputPath\n")

        logger.info("Generated: ${sprFile.absolutePath}")
      } else {
        logger.warn("Input file not found or not a .spd file: $inputPath")
      }
    }
  }

  override fun validateStep(step: FlowStep): List<String> {
    val errors = super.validateStep(step).toMutableList()

    // Spritepad-specific validations
    if (step.inputs.isEmpty()) {
      errors.add("Spritepad step requires at least one input .spd file")
    }

    step.inputs.forEach { inputPath ->
      val inputFile = project.file(inputPath)
      if (!inputFile.name.endsWith(".spd")) {
        errors.add("Spritepad step expects .spd files, but got: $inputPath")
      }
    }

    return errors
  }
}
