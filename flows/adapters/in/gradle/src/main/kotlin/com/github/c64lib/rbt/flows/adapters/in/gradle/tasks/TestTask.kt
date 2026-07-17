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

import com.github.c64lib.rbt.compilers.kickass.usecase.KickAssembleSpecUseCase
import com.github.c64lib.rbt.flows.adapters.`in`.gradle.port.Spec64TestPortAdapter
import com.github.c64lib.rbt.flows.domain.FlowStep
import com.github.c64lib.rbt.flows.domain.steps.TestStep
import com.github.c64lib.rbt.shared.gradle.dsl.RetroAssemblerPluginExtension
import com.github.c64lib.rbt.testing.a64spec.usecase.Run64SpecTestUseCase
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFiles

/** Gradle task for executing 64spec test steps with proper incremental build support. */
abstract class TestTask : BaseFlowStepTask() {

  @get:OutputFiles abstract val outputFiles: ConfigurableFileCollection

  /** KickAssembleSpecUseCase for spec assembly - injected by FlowTasksGenerator. */
  @get:Internal lateinit var kickAssembleSpecUseCase: KickAssembleSpecUseCase

  /** Run64SpecTestUseCase for running specs on VICE - injected by FlowTasksGenerator. */
  @get:Internal lateinit var run64SpecTestUseCase: Run64SpecTestUseCase

  /** Plugin extension supplying libDirs/defines and VICE settings - injected by generator. */
  @get:Internal lateinit var extension: RetroAssemblerPluginExtension

  init {
    description = "Runs 64spec tests"
  }

  override fun executeStepLogic(step: FlowStep) {
    val validationErrors = validateStep(step)
    if (validationErrors.isNotEmpty()) {
      throw IllegalStateException(
          "Test step validation failed: ${validationErrors.joinToString(", ")}")
    }

    if (step !is TestStep) {
      throw IllegalStateException("Expected TestStep but got ${step::class.simpleName}")
    }

    val libDirs = listOf(*extension.libDirs).map { project.file(it) }
    val defines = listOf(*extension.defines)

    val portAdapter =
        Spec64TestPortAdapter(kickAssembleSpecUseCase, run64SpecTestUseCase, libDirs, defines)
    step.setTestPort(portAdapter)

    val executionContext =
        mapOf(
            "projectRootDir" to project.projectDir,
            "outputDirectory" to outputDirectory.get().asFile,
            "logger" to logger)

    step.execute(executionContext)
  }

  override fun validateStep(step: FlowStep): List<String> {
    val errors = super.validateStep(step).toMutableList()

    if (step !is TestStep) {
      errors.add("Expected TestStep but got ${step::class.simpleName}")
      return errors
    }

    errors.addAll(step.validate())

    if (!::kickAssembleSpecUseCase.isInitialized) {
      errors.add("KickAssembleSpecUseCase not injected for TestTask")
    }
    if (!::run64SpecTestUseCase.isInitialized) {
      errors.add("Run64SpecTestUseCase not injected for TestTask")
    }
    if (!::extension.isInitialized) {
      errors.add("RetroAssemblerPluginExtension not injected for TestTask")
    }

    return errors
  }
}
