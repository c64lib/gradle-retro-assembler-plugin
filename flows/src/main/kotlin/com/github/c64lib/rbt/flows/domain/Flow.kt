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
package com.github.c64lib.rbt.flows.domain

import java.io.File

/** Represents a flow - a chain of related tasks that can be executed as a unit */
data class Flow(
    val name: String,
    val steps: List<FlowStep> = emptyList(),
    val dependsOn: List<String> = emptyList(), // Names of other flows this flow depends on
    val produces: List<FlowArtifact> = emptyList(), // Artifacts produced by this flow
    val consumes: List<FlowArtifact> = emptyList(), // Artifacts consumed by this flow
    val description: String = ""
) {
  /** Adds a step to this flow */
  fun addStep(step: FlowStep): Flow = copy(steps = steps + step)

  /** Adds a dependency on another flow */
  fun dependsOn(flowName: String): Flow = copy(dependsOn = dependsOn + flowName)

  /** Adds an artifact that this flow produces */
  fun produces(artifact: FlowArtifact): Flow = copy(produces = produces + artifact)

  /** Adds an artifact that this flow consumes */
  fun consumes(artifact: FlowArtifact): Flow = copy(consumes = consumes + artifact)
}

/** Represents a single step within a flow */
abstract class FlowStep(
    open val name: String,
    val taskType: String,
    open val inputs: List<String> = emptyList(),
    open val outputs: List<String> = emptyList()
) {
  /** Execute this step with the given context */
  abstract fun execute(context: Map<String, Any> = emptyMap())

  /** Validate that this step can be executed */
  open fun validate(): List<String> = emptyList()

  /** Get step-specific configuration for display/debugging */
  open fun getConfiguration(): Map<String, Any> = emptyMap()

  /**
   * Extracts the project root directory from the execution context.
   *
   * @param context The execution context map
   * @return The project root directory as a File
   * @throws StepExecutionException if project root directory is not found
   */
  protected fun getProjectRootDir(context: Map<String, Any>): File {
    return context["projectRootDir"] as? File
        ?: throw StepExecutionException("Project root directory not found in execution context", name)
  }

  /**
   * Resolves a list of input file paths to File objects, with support for both absolute and relative paths.
   *
   * @param inputPaths The input file paths to resolve
   * @param projectRootDir The project root directory for relative path resolution
   * @return A list of resolved File objects
   * @throws IllegalArgumentException if any file does not exist
   */
  protected fun resolveInputFiles(inputPaths: List<String>, projectRootDir: File): List<File> {
    return inputPaths.map { inputPath ->
      val file =
          if (File(inputPath).isAbsolute) {
            File(inputPath)
          } else {
            File(projectRootDir, inputPath)
          }

      if (!file.exists()) {
        throw IllegalArgumentException("Source file does not exist: ${file.absolutePath}")
      }

      file
    }
  }

  /**
   * Resolves a single input file path to a File object.
   *
   * @param inputPath The input file path to resolve
   * @param projectRootDir The project root directory for relative path resolution
   * @return The resolved File object
   * @throws StepExecutionException if the file does not exist
   */
  protected fun resolveInputFile(inputPath: String, projectRootDir: File): File {
    return resolveInputFiles(listOf(inputPath), projectRootDir).first()
  }

  /**
   * Resolves an output file path to a File object, with support for both absolute and relative paths.
   *
   * @param outputPath The output file path to resolve
   * @param projectRootDir The project root directory for relative path resolution
   * @return The resolved File object
   */
  protected fun resolveOutputFile(outputPath: String, projectRootDir: File): File {
    return if (File(outputPath).isAbsolute) {
      File(outputPath)
    } else {
      File(projectRootDir, outputPath)
    }
  }

  /**
   * Validates that a port is properly injected and returns it.
   *
   * @param port The port to validate
   * @param portName The name of the port (e.g., "AssemblyPort")
   * @return The port if not null
   * @throws StepExecutionException if the port is not injected
   */
  protected fun <T> validatePort(port: T?, portName: String): T {
    return port ?: throw StepExecutionException("$portName not injected", name)
  }
}

/**
 * Exception thrown during step validation when configuration is invalid.
 *
 * @param message The error message describing the validation failure
 * @param stepName The name of the step that failed validation
 */
class StepValidationException(override val message: String, val stepName: String) : Exception(message) {
  override fun toString(): String = "Step '$stepName': $message"
}

/**
 * Exception thrown during step execution when the step cannot complete.
 *
 * @param message The error message describing the execution failure
 * @param stepName The name of the step that failed
 * @param cause The underlying exception that caused the failure, if any
 */
class StepExecutionException(override val message: String, val stepName: String, override val cause: Throwable? = null) : Exception(message, cause) {
  override fun toString(): String = "Step '$stepName': $message"
}

/** Represents an artifact (file or resource) that flows between different flows */
data class FlowArtifact(
    val name: String,
    val path: String,
    val description: String = "",
    val isSourceFile: Boolean = false // Indicates if this artifact comes directly from source files
) {
  companion object {
    /** Creates a source file artifact that doesn't need a producer */
    fun sourceFile(name: String, path: String, description: String = ""): FlowArtifact =
        FlowArtifact(name, path, description, isSourceFile = true)

    /** Creates a produced artifact that should be generated by another flow */
    fun produced(name: String, path: String, description: String = ""): FlowArtifact =
        FlowArtifact(name, path, description, isSourceFile = false)
  }
}
