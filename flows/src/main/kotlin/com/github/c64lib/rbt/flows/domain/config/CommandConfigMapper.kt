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
package com.github.c64lib.rbt.flows.domain.config

import com.github.c64lib.rbt.flows.domain.steps.CommandStep
import java.io.File

/**
 * Maps CommandStep from the flows domain to CommandCommand.
 *
 * This mapper handles the structural differences between the domain step configuration and the
 * command execution format, while maintaining architectural boundaries.
 */
class CommandConfigMapper {

  /**
   * Creates a CommandCommand from CommandStep and execution context.
   *
   * @param step The command step containing the CLI configuration
   * @param projectRootDir The project root directory for resolving relative paths
   * @param environment Additional environment variables to set for the command
   * @param timeoutSeconds Optional timeout for command execution
   * @return CommandCommand ready for execution
   */
  fun toCommandCommand(
      step: CommandStep,
      projectRootDir: File,
      environment: Map<String, String> = emptyMap(),
      timeoutSeconds: Long? = null
  ): CommandCommand {
    val inputFiles = resolveInputFiles(step.inputs, projectRootDir)
    val outputFiles = resolveOutputFiles(step.outputs, projectRootDir)

    return CommandCommand(
        executable = step.command,
        arguments = step.parameters,
        workingDirectory = projectRootDir,
        environment = environment,
        inputFiles = inputFiles,
        outputFiles = outputFiles,
        timeoutSeconds = timeoutSeconds)
  }

  /**
   * Resolves input file paths relative to the project root directory.
   *
   * @param inputPaths List of input file paths (can be relative or absolute)
   * @param projectRootDir The project root directory for resolving relative paths
   * @return List of resolved File objects
   */
  private fun resolveInputFiles(inputPaths: List<String>, projectRootDir: File): List<File> {
    return inputPaths.map { inputPath ->
      if (File(inputPath).isAbsolute) {
        File(inputPath)
      } else {
        File(projectRootDir, inputPath)
      }
    }
  }

  /**
   * Resolves output file paths relative to the project root directory.
   *
   * @param outputPaths List of output file paths (can be relative or absolute)
   * @param projectRootDir The project root directory for resolving relative paths
   * @return List of resolved File objects
   */
  private fun resolveOutputFiles(outputPaths: List<String>, projectRootDir: File): List<File> {
    return outputPaths.map { outputPath ->
      if (File(outputPath).isAbsolute) {
        File(outputPath)
      } else {
        File(projectRootDir, outputPath)
      }
    }
  }
}
