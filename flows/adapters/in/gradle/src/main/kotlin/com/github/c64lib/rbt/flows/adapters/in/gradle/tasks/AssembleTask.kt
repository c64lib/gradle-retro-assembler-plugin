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

import com.github.c64lib.rbt.compilers.kickass.usecase.KickAssembleUseCase
import com.github.c64lib.rbt.flows.adapters.`in`.gradle.assembly.KickAssemblerPortAdapter
import com.github.c64lib.rbt.flows.domain.FlowStep
import com.github.c64lib.rbt.flows.domain.config.AssemblyConfigMapper
import com.github.c64lib.rbt.flows.domain.steps.AssembleStep
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFiles

/** Gradle task for executing assembly steps with proper incremental build support. */
abstract class AssembleTask : BaseFlowStepTask() {

  @get:OutputFiles abstract val outputFiles: ConfigurableFileCollection

  /** Additional input files for tracking indirect dependencies (includes/imports) */
  @get:InputFiles abstract val additionalInputFiles: ConfigurableFileCollection

  /** KickAssembleUseCase for actual assembly compilation - injected by FlowTasksGenerator */
  @get:Internal lateinit var kickAssembleUseCase: KickAssembleUseCase

  private val assemblyConfigMapper = AssemblyConfigMapper()

  init {
    description = "Assembles source files using Kick Assembler"
  }

  override fun executeStepLogic(step: FlowStep) {
    val validationErrors = validateStep(step)
    if (validationErrors.isNotEmpty()) {
      throw IllegalStateException(
          "Assemble step validation failed: ${validationErrors.joinToString(", ")}")
    }

    if (step !is AssembleStep) {
      throw IllegalStateException("Expected AssembleStep but got ${step::class.simpleName}")
    }

    logger.info("Executing AssembleStep '${step.name}' with configuration: ${step.config}")
    logger.info("Input files: ${step.inputs}")
    logger.info("Additional input files: ${additionalInputFiles.files.map { it.name }}")
    logger.info("Output directory: ${outputDirectory.get().asFile.absolutePath}")

    try {
      // Inject the assembly port adapter into the step
      val assemblyPortAdapter = KickAssemblerPortAdapter(kickAssembleUseCase)
      step.setAssemblyPort(assemblyPortAdapter)

      // Create execution context with project information
      val executionContext =
          mapOf(
              "projectRootDir" to project.projectDir,
              "outputDirectory" to outputDirectory.get().asFile,
              "logger" to logger)

      // Execute the step using its domain logic
      step.execute(executionContext)

      logger.info("Successfully completed assembly step '${step.name}'")
    } catch (e: Exception) {
      logger.error("Assembly compilation failed for step '${step.name}': ${e.message}", e)
      throw e
    }
  }

  override fun validateStep(step: FlowStep): List<String> {
    val errors = super.validateStep(step).toMutableList()

    if (step !is AssembleStep) {
      errors.add("Expected AssembleStep but got ${step::class.simpleName}")
      return errors
    }

    // Use the domain validation from AssembleStep
    errors.addAll(step.validate())

    // Validate that KickAssembleUseCase has been injected
    if (!::kickAssembleUseCase.isInitialized) {
      errors.add("KickAssembleUseCase not injected for AssembleTask")
    }

    return errors
  }
}
