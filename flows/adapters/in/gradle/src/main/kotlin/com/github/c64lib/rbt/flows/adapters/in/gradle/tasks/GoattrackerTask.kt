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

/** Gradle task for executing GoatTracker processing steps with proper incremental build support. */
abstract class GoattrackerTask : BaseFlowStepTask() {

  @get:OutputFiles abstract val outputFiles: ConfigurableFileCollection

  init {
    description = "Processes GoatTracker (.sng) files to generate music data"
  }

  override fun executeStepLogic(step: FlowStep) {
    val validationErrors = validateStep(step)
    if (validationErrors.isNotEmpty()) {
      throw IllegalStateException(
          "GoatTracker step validation failed: ${validationErrors.joinToString(", ")}")
    }

    logger.info("Processing GoatTracker files from inputs: ${step.inputs}")
    logger.info("Output directory: ${outputDirectory.get().asFile.absolutePath}")

    // TODO: Integrate with actual GoatTracker processor from processors/goattracker module
    // For now, simulate the processing
    step.inputs.forEach { inputPath ->
      logger.info("Processing GoatTracker file: $inputPath")

      val inputFile = project.file(inputPath)
      if (inputFile.exists() && inputFile.extension == "sng") {
        val baseName = inputFile.nameWithoutExtension
        val outputDir = outputDirectory.get().asFile

        // Create output files (placeholder - actual implementation will use goattracker processor)
        val sidFile = outputDir.resolve("$baseName.sid")
        val dataFile = outputDir.resolve("$baseName.asm")

        sidFile.writeText("// Generated SID music from $inputPath\n")
        dataFile.writeText("// Generated music data from $inputPath\n")

        logger.info("Generated: ${sidFile.absolutePath}")
        logger.info("Generated: ${dataFile.absolutePath}")
      } else {
        logger.warn("Input file not found or not a .sng file: $inputPath")
      }
    }
  }

  override fun validateStep(step: FlowStep): List<String> {
    val errors = super.validateStep(step).toMutableList()

    // GoatTracker-specific validations
    if (step.inputs.isEmpty()) {
      errors.add("GoatTracker step requires at least one input .sng file")
    }

    step.inputs.forEach { inputPath ->
      val inputFile = project.file(inputPath)
      if (!inputFile.name.endsWith(".sng")) {
        errors.add("GoatTracker step expects .sng files, but got: $inputPath")
      }
    }

    return errors
  }
}
