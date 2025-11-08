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
import com.github.c64lib.rbt.flows.domain.steps.ImageStep
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.OutputFiles

/**
 * Gradle task for executing image processing steps with proper incremental build support.
 *
 * This task:
 * 1. Validates the ImageStep configuration
 * 2. Creates an ImageAdapter to implement the ImagePort
 * 3. Injects the adapter into the domain step
 * 4. Executes the domain step with Gradle context (project directory, output directory)
 * 5. Handles errors and logging
 */
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

    if (step !is ImageStep) {
      throw IllegalStateException("Expected ImageStep but got ${step::class.simpleName}")
    }

    logger.info("Executing ImageStep '${step.name}' with configuration: ${step.config}")
    logger.info("Input files: ${step.inputs}")
    logger.info("Output directory: ${outputDirectory.get().asFile.absolutePath}")

    try {
      // Create ImageAdapter for actual image processing (lazy import to avoid circular dependency)
      val adapterClass =
          Class.forName("com.github.c64lib.rbt.flows.adapters.out.image.ImageAdapter")
      val imageAdapter = adapterClass.getDeclaredConstructor().newInstance()
      val imagePort = imageAdapter as com.github.c64lib.rbt.flows.domain.port.ImagePort
      step.setImagePort(imagePort)

      // Create execution context with project information
      val executionContext =
          mapOf(
              "projectRootDir" to project.projectDir,
              "outputDirectory" to outputDirectory.get().asFile,
              "logger" to logger)

      // Execute the step using its domain logic
      step.execute(executionContext)

      logger.info("Successfully completed image step '${step.name}'")
    } catch (e: Exception) {
      logger.error("Image processing failed for step '${step.name}': ${e.message}", e)
      throw e
    }
  }

  override fun validateStep(step: FlowStep): List<String> {
    val errors = super.validateStep(step).toMutableList()

    if (step !is ImageStep) {
      errors.add("Expected ImageStep but got ${step::class.simpleName}")
      return errors
    }

    // Use the domain validation from ImageStep
    errors.addAll(step.validate())

    return errors
  }
}
