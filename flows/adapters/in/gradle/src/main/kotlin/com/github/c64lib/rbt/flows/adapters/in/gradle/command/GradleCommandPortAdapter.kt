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
package com.github.c64lib.rbt.flows.adapters.`in`.gradle.command

import com.github.c64lib.rbt.flows.domain.config.CommandCommand
import com.github.c64lib.rbt.flows.domain.port.CommandPort
import java.util.concurrent.TimeUnit
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Adapter implementation of CommandPort that executes CLI commands using Java ProcessBuilder.
 *
 * This adapter is responsible for translating domain-level command execution requests to actual
 * process execution while maintaining hexagonal architecture boundaries and providing proper error
 * handling, logging, and platform compatibility.
 */
class GradleCommandPortAdapter(
    private val logger: Logger = Logging.getLogger(GradleCommandPortAdapter::class.java)
) : CommandPort {

  override fun execute(command: CommandCommand) {
    logger.info("Executing command: ${command.getCommandLineString()}")
    logger.info("Working directory: ${command.workingDirectory.absolutePath}")

    validateCommand(command)

    val processBuilder = createProcessBuilder(command)
    val process = startProcess(processBuilder, command)
    val output = captureOutput(process, command)
    val exitCode = waitForCompletion(process, command)

    handleResult(exitCode, output, command)
  }

  override fun executeWithOutput(command: CommandCommand): String {
    logger.info("Executing command with output capture: ${command.getCommandLineString()}")
    logger.info("Working directory: ${command.workingDirectory.absolutePath}")

    validateCommand(command)

    val processBuilder = createProcessBuilder(command)
    val process = startProcess(processBuilder, command)
    val output = captureOutput(process, command)
    val exitCode = waitForCompletion(process, command)

    if (exitCode != 0) {
      throw RuntimeException(
          "Command failed with exit code $exitCode: ${command.getCommandLineString()}\nOutput: $output")
    }

    return output
  }

  /**
   * Validates that the command can be executed.
   *
   * @param command The command to validate
   * @throws IllegalArgumentException if the command is invalid
   */
  private fun validateCommand(command: CommandCommand) {
    if (command.executable.isBlank()) {
      throw IllegalArgumentException("Command executable cannot be blank")
    }

    if (!command.workingDirectory.exists()) {
      throw IllegalArgumentException(
          "Working directory does not exist: ${command.workingDirectory.absolutePath}")
    }

    if (!command.workingDirectory.isDirectory) {
      throw IllegalArgumentException(
          "Working directory is not a directory: ${command.workingDirectory.absolutePath}")
    }

    // Validate input files exist if specified
    command.inputFiles.forEach { inputFile ->
      if (!inputFile.exists()) {
        throw IllegalArgumentException("Input file does not exist: ${inputFile.absolutePath}")
      }
    }

    // Ensure output directories exist
    command.outputFiles.forEach { outputFile ->
      val parentDir = outputFile.parentFile
      if (parentDir != null && !parentDir.exists()) {
        logger.info("Creating output directory: ${parentDir.absolutePath}")
        parentDir.mkdirs()
      }
    }
  }

  /**
   * Creates and configures a ProcessBuilder for the command.
   *
   * @param command The command to create a ProcessBuilder for
   * @return Configured ProcessBuilder
   */
  private fun createProcessBuilder(command: CommandCommand): ProcessBuilder {
    val processBuilder = ProcessBuilder(command.getCommandLine())
    processBuilder.directory(command.workingDirectory)
    processBuilder.redirectErrorStream(true) // Merge stderr into stdout

    // Add environment variables
    if (command.environment.isNotEmpty()) {
      val environment = processBuilder.environment()
      command.environment.forEach { (key, value) -> environment[key] = value }
      if (logger.isDebugEnabled) {
        logger.debug("Added environment variables: {}", command.environment)
      }
    }

    return processBuilder
  }

  /**
   * Starts the process and handles startup errors.
   *
   * @param processBuilder The configured ProcessBuilder
   * @param command The original command for error reporting
   * @return Started Process
   * @throws RuntimeException if the process fails to start
   */
  private fun startProcess(processBuilder: ProcessBuilder, command: CommandCommand): Process {
    return try {
      processBuilder.start()
    } catch (e: Exception) {
      throw RuntimeException("Failed to start command: ${command.getCommandLineString()}", e)
    }
  }

  /**
   * Captures output from the running process.
   *
   * @param process The running process
   * @param command The original command for error reporting
   * @return Captured output as string
   */
  private fun captureOutput(process: Process, command: CommandCommand): String {
    return try {
      process.inputStream.bufferedReader().use { reader -> reader.readText() }
    } catch (e: Exception) {
      logger.warn("Failed to capture output for command: ${command.getCommandLineString()}", e)
      ""
    }
  }

  /**
   * Waits for process completion with optional timeout.
   *
   * @param process The running process
   * @param command The original command for timeout and error reporting
   * @return Process exit code
   * @throws RuntimeException if timeout occurs or waiting fails
   */
  private fun waitForCompletion(process: Process, command: CommandCommand): Int {
    return try {
      val timeoutSeconds = command.timeoutSeconds
      if (timeoutSeconds != null) {
        val completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
        if (!completed) {
          process.destroyForcibly()
          throw RuntimeException(
              "Command timed out after $timeoutSeconds seconds: ${command.getCommandLineString()}")
        }
      } else {
        process.waitFor()
      }
      process.exitValue()
    } catch (e: InterruptedException) {
      process.destroyForcibly()
      Thread.currentThread().interrupt()
      throw RuntimeException(
          "Command execution was interrupted: ${command.getCommandLineString()}", e)
    }
  }

  /**
   * Handles the command execution result.
   *
   * @param exitCode The process exit code
   * @param output The captured output
   * @param command The original command for reporting
   * @throws RuntimeException if the command failed
   */
  private fun handleResult(exitCode: Int, output: String, command: CommandCommand) {
    if (output.isNotBlank()) {
      logger.info("Command output:\n$output")
    }

    if (exitCode != 0) {
      throw RuntimeException(
          "Command failed with exit code $exitCode: ${command.getCommandLineString()}\nOutput: $output")
    }

    logger.info("Command executed successfully")

    // Verify that expected output files were created
    command.outputFiles.forEach { outputFile ->
      if (!outputFile.exists()) {
        logger.warn("Expected output file was not created: ${outputFile.absolutePath}")
      } else {
        logger.info("Output file created: ${outputFile.absolutePath}")
      }
    }
  }
}
