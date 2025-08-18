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
import com.github.c64lib.rbt.flows.domain.steps.CommandStep
import java.io.File
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.OutputFiles

/** Gradle task for executing command-line steps with proper incremental build support. */
abstract class CommandTask : BaseFlowStepTask() {

  @get:OutputFiles abstract val outputFiles: ConfigurableFileCollection

  init {
    description = "Executes arbitrary command-line tools"
  }

  override fun executeStepLogic(step: FlowStep) {
    if (step !is CommandStep) {
      throw IllegalStateException(
          "CommandTask can only execute CommandStep instances, but got: ${step::class.simpleName}")
    }

    val validationErrors = validateStep(step)
    if (validationErrors.isNotEmpty()) {
      throw IllegalStateException(
          "Command step validation failed: ${validationErrors.joinToString(", ")}")
    }

    logger.info("Executing command: ${step.getCommandLine().joinToString(" ")}")
    logger.info("Working directory: ${project.projectDir.absolutePath}")

    // Execute the command
    val processBuilder = ProcessBuilder(step.getCommandLine())
    processBuilder.directory(project.projectDir)
    processBuilder.redirectErrorStream(true)

    try {
      val process = processBuilder.start()

      // Read and log output
      val output = process.inputStream.bufferedReader().use { it.readText() }
      if (output.isNotBlank()) {
        logger.info("Command output:\n$output")
      }

      val exitCode = process.waitFor()
      if (exitCode != 0) {
        throw RuntimeException(
            "Command failed with exit code $exitCode: ${step.getCommandLine().joinToString(" ")}")
      }

      logger.info("Command executed successfully")

      // Verify that expected output files were created
      step.outputs.forEach { outputPath ->
        val outputFile = File(outputPath)
        if (!outputFile.exists()) {
          logger.warn("Expected output file not found: $outputPath")
        }
      }
    } catch (e: Exception) {
      logger.error("Failed to execute command: ${step.getCommandLine().joinToString(" ")}", e)
      throw e
    }
  }

  override fun validateStep(step: FlowStep): List<String> {
    val errors = super.validateStep(step).toMutableList()

    if (step !is CommandStep) {
      errors.add("CommandTask requires a CommandStep, but got: ${step::class.simpleName}")
      return errors
    }

    // Command-specific validations
    val commandLine = step.getCommandLine()
    if (commandLine.isEmpty() || commandLine[0].isBlank()) {
      errors.add("Command step requires a valid command")
    }

    // Check that input files exist
    step.inputs.forEach { inputPath ->
      val inputFile = File(inputPath)
      if (!inputFile.exists()) {
        errors.add("Input file does not exist: $inputPath")
      }
    }

    return errors
  }
}
