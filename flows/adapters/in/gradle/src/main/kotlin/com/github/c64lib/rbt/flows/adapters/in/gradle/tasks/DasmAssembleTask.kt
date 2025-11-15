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

import com.github.c64lib.rbt.compilers.dasm.usecase.DasmAssembleUseCase
import com.github.c64lib.rbt.flows.adapters.`in`.gradle.assembly.DasmCommandAdapter
import com.github.c64lib.rbt.flows.adapters.`in`.gradle.assembly.DasmPortAdapter
import com.github.c64lib.rbt.flows.domain.FlowStep
import com.github.c64lib.rbt.flows.domain.config.DasmConfigMapper
import com.github.c64lib.rbt.flows.domain.steps.DasmStep
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFiles

/** Gradle task for executing dasm assembly steps with proper incremental build support. */
abstract class DasmAssembleTask : BaseFlowStepTask() {

  @get:OutputFiles abstract val outputFiles: ConfigurableFileCollection

  /** Additional input files for tracking indirect dependencies (includes/imports) */
  @get:InputFiles abstract val additionalInputFiles: ConfigurableFileCollection

  /** DasmAssembleUseCase for actual assembly compilation - injected by FlowTasksGenerator */
  @get:Internal lateinit var dasmAssembleUseCase: DasmAssembleUseCase

  private val dasmConfigMapper = DasmConfigMapper()

  init {
    description = "Assembles source files using dasm assembler"
  }

  override fun executeStepLogic(step: FlowStep) {
    val validationErrors = validateStep(step)
    if (validationErrors.isNotEmpty()) {
      throw IllegalStateException(
          "Dasm assemble step validation failed: ${validationErrors.joinToString(", ")}")
    }

    if (step !is DasmStep) {
      throw IllegalStateException("Expected DasmStep but got ${step::class.simpleName}")
    }

    logger.info("Executing DasmStep '${step.name}' with configuration: ${step.config}")
    logger.info("Input files: ${step.inputs}")
    logger.info("Additional input files: ${additionalInputFiles.files.map { it.name }}")
    logger.info("Output directory: ${outputDirectory.get().asFile.absolutePath}")

    try {
      // Inject the dasm assembly port adapter into the step
      val dasmPortAdapter = DasmPortAdapter(dasmAssembleUseCase, DasmCommandAdapter())
      step.setDasmPort(dasmPortAdapter)

      // Create execution context with project information
      val executionContext =
          mapOf(
              "projectRootDir" to project.projectDir,
              "outputDirectory" to outputDirectory.get().asFile,
              "logger" to logger)

      // Execute the step using its domain logic
      step.execute(executionContext)

      logger.info("Successfully completed dasm assembly step '${step.name}'")
    } catch (e: Exception) {
      logger.error("Dasm assembly compilation failed for step '${step.name}': ${e.message}", e)
      throw e
    }
  }

  override fun validateStep(step: FlowStep): List<String> {
    val errors = super.validateStep(step).toMutableList()

    if (step !is DasmStep) {
      errors.add("Expected DasmStep but got ${step::class.simpleName}")
      return errors
    }

    // Use the domain validation from DasmStep
    errors.addAll(step.validate())

    // Validate that DasmAssembleUseCase has been injected
    if (!::dasmAssembleUseCase.isInitialized) {
      errors.add("DasmAssembleUseCase not injected for DasmAssembleTask")
    }

    return errors
  }
}
