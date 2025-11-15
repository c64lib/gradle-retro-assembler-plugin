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
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

/**
 * Base class for all flow step tasks with common input/output tracking and incremental build
 * support.
 */
abstract class BaseFlowStepTask : DefaultTask() {

  @get:InputFiles
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val inputFiles: ConfigurableFileCollection

  @get:InputDirectory
  @get:PathSensitive(PathSensitivity.RELATIVE)
  @get:Optional
  abstract val inputDirectory: DirectoryProperty

  @get:OutputDirectory abstract val outputDirectory: DirectoryProperty

  @get:Internal abstract val flowStep: Property<FlowStep>

  init {
    group = "flows"
    // Set up automatic task dependencies based on file inputs/outputs
    // This will be handled by the FlowTasksGenerator
  }

  @TaskAction
  fun executeStep() {
    val step = flowStep.get()
    logger.info("Executing ${step.taskType} step: ${step.name}")

    // Ensure output directory exists
    outputDirectory.get().asFile.mkdirs()

    try {
      executeStepLogic(step)
      logger.info("Successfully completed ${step.taskType} step: ${step.name}")
    } catch (e: Exception) {
      logger.error("Failed to execute ${step.taskType} step: ${step.name}", e)
      throw e
    }
  }

  /** Subclasses implement this method to perform the actual step execution. */
  protected open fun executeStepLogic(step: FlowStep) {
    throw UnsupportedOperationException(
        "executeStepLogic must be implemented by subclass for step: ${step.name}")
  }

  /** Validates that the step can be executed with current inputs. */
  protected open fun validateStep(step: FlowStep): List<String> {
    val errors = mutableListOf<String>()

    // Check that input files exist if specified
    if (step.inputs.isNotEmpty() && inputFiles.isEmpty && !inputDirectory.isPresent) {
      errors.add("Step '${step.name}' requires input files but none were configured")
    }

    // Check that output directory is specified
    if (!outputDirectory.isPresent) {
      errors.add("Step '${step.name}' requires output directory")
    }

    return errors
  }
}
