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

/** Gradle task for executing image processing steps with proper incremental build support. */
abstract class ImageTask : BaseFlowStepTask() {

  @get:OutputFiles abstract val outputFiles: ConfigurableFileCollection

  init {
    description = "Processes image files to generate C64-compatible formats"
  }

  override fun executeStepLogic(step: FlowStep) {
    val validationErrors = validateStep(step)
    if (validationErrors.isNotEmpty()) {
      throw IllegalStateException(
          "Image step validation failed: ${validationErrors.joinToString(", ")}")
    }

    logger.info("Processing image files from inputs: ${step.inputs}")
    logger.info("Output directory: ${outputDirectory.get().asFile.absolutePath}")

    // TODO: Integrate with actual Image processor from processors/image module
    // For now, simulate the processing
    step.inputs.forEach { inputPath ->
      logger.info("Processing image file: $inputPath")

      val inputFile = project.file(inputPath)
      if (inputFile.exists() && isImageFile(inputFile.extension)) {
        val baseName = inputFile.nameWithoutExtension
        val outputDir = outputDirectory.get().asFile

        // Create output files (placeholder - actual implementation will use image processor)
        val koalaFile = outputDir.resolve("$baseName.kla")
        val screenFile = outputDir.resolve("$baseName.scr")
        val colorFile = outputDir.resolve("$baseName.col")

        koalaFile.writeText("// Generated Koala bitmap from $inputPath\n")
        screenFile.writeText("// Generated screen data from $inputPath\n")
        colorFile.writeText("// Generated color data from $inputPath\n")

        logger.info("Generated: ${koalaFile.absolutePath}")
        logger.info("Generated: ${screenFile.absolutePath}")
        logger.info("Generated: ${colorFile.absolutePath}")
      } else {
        logger.warn("Input file not found or not a supported image file: $inputPath")
      }
    }
  }

  override fun validateStep(step: FlowStep): List<String> {
    val errors = super.validateStep(step).toMutableList()

    // Image-specific validations
    if (step.inputs.isEmpty()) {
      errors.add("Image step requires at least one input image file")
    }

    step.inputs.forEach { inputPath ->
      val inputFile = project.file(inputPath)
      if (!isImageFile(inputFile.extension)) {
        errors.add("Image step expects image files (png, jpg, jpeg, bmp, gif), but got: $inputPath")
      }
    }

    return errors
  }

  private fun isImageFile(extension: String): Boolean {
    return extension.lowercase() in setOf("png", "jpg", "jpeg", "bmp", "gif")
  }
}
