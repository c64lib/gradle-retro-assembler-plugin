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

import com.github.c64lib.rbt.flows.adapters.`in`.gradle.command.GradleCommandPortAdapter
import com.github.c64lib.rbt.flows.domain.FlowStep
import com.github.c64lib.rbt.flows.domain.steps.CommandStep
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFiles

/** Gradle task for executing command-line steps with proper incremental build support. */
abstract class CommandTask : BaseFlowStepTask() {

  @get:OutputFiles abstract val outputFiles: ConfigurableFileCollection

  /** CommandPortAdapter for actual CLI execution - created internally with Gradle logger */
  @get:Internal
  val commandPortAdapter: GradleCommandPortAdapter by lazy { GradleCommandPortAdapter(logger) }

  init {
    description = "Executes arbitrary command-line tools with incremental build support"
  }

  override fun executeStepLogic(step: FlowStep) {
    val validationErrors = validateStep(step)
    if (validationErrors.isNotEmpty()) {
      throw IllegalStateException(
          "Command step validation failed: ${validationErrors.joinToString(", ")}")
    }

    if (step !is CommandStep) {
      throw IllegalStateException("Expected CommandStep but got ${step::class.simpleName}")
    }

    logger.info("Executing CommandStep '${step.name}' with command: ${step.command}")
    logger.info("Parameters: ${step.parameters}")
    logger.info("Input files: ${step.inputs}")
    logger.info("Output files: ${step.outputs}")
    logger.info("Working directory: ${project.projectDir.absolutePath}")

    try {
      // Inject the command port adapter into the step
      step.setCommandPort(commandPortAdapter)

      // Create execution context with project information and optional settings
      val executionContext = buildExecutionContext()

      // Execute the command step through the domain layer
      step.execute(executionContext)

      logger.info("Command step '${step.name}' executed successfully")
    } catch (e: Exception) {
      logger.error("Failed to execute command step '${step.name}': ${e.message}", e)
      throw e
    }
  }

  /**
   * Builds the execution context for the command step. This context provides the step with
   * necessary runtime information.
   */
  private fun buildExecutionContext(): Map<String, Any> {
    val context =
        mutableMapOf<String, Any>(
            "projectRootDir" to project.projectDir,
            "outputDirectory" to outputDirectory.get().asFile,
            "logger" to logger)

    // Add environment variables if any are defined in project properties
    val environmentVariables = extractEnvironmentVariables()
    if (environmentVariables.isNotEmpty()) {
      context["environment"] = environmentVariables
    }

    // Add timeout if defined in project properties
    extractTimeoutSeconds()?.let { timeout -> context["timeoutSeconds"] = timeout }

    return context
  }

  /**
   * Extracts environment variables from project properties. Looks for properties with pattern:
   * command.env.{KEY}={VALUE}
   */
  private fun extractEnvironmentVariables(): Map<String, String> {
    val envVars = mutableMapOf<String, String>()

    project.properties.forEach { (key, value) ->
      if (key.startsWith("command.env.") && value != null) {
        val envKey = key.removePrefix("command.env.")
        envVars[envKey] = value.toString()
      }
    }

    return envVars
  }

  /**
   * Extracts timeout configuration from project properties. Looks for property:
   * command.timeout.seconds
   */
  private fun extractTimeoutSeconds(): Long? {
    val timeoutProperty = project.findProperty("command.timeout.seconds")
    return when {
      timeoutProperty is Number -> timeoutProperty.toLong()
      timeoutProperty is String -> timeoutProperty.toLongOrNull()
      else -> null
    }
  }

  override fun validateStep(step: FlowStep): List<String> {
    val errors = super.validateStep(step).toMutableList()

    if (step !is CommandStep) {
      errors.add("CommandTask requires a CommandStep, but got: ${step::class.simpleName}")
      return errors
    }

    // Command-specific validations
    if (step.command.isBlank()) {
      errors.add("Command step requires a valid command executable")
    }

    // Validate that input files are properly configured in Gradle
    if (step.inputs.isNotEmpty() && inputFiles.isEmpty) {
      errors.add("Command step declares input files but Gradle inputFiles is not configured")
    }

    // Validate that output files are properly configured in Gradle
    if (step.outputs.isNotEmpty() && outputFiles.isEmpty) {
      errors.add("Command step declares output files but Gradle outputFiles is not configured")
    }

    return errors
  }
}
