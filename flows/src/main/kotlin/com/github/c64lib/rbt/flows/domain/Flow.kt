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

/**
 * Represents a named flow that can contain multiple steps executed in sequence. Flows can run in
 * parallel with other flows if they don't have conflicting dependencies.
 */
data class Flow(
    val name: String,
    val description: String = "",
    val steps: List<FlowStep> = emptyList(),
    val inputs: List<FlowArtifact> = emptyList(),
    val outputs: List<FlowArtifact> = emptyList(),
    val dependencies: List<String> = emptyList() // Names of other flows this flow depends on
) {
  /**
   * Returns true if this flow can run in parallel with the other flow. Flows can run in parallel if
   * neither depends on the other and they don't share conflicting artifacts.
   */
  fun canRunInParallelWith(other: Flow): Boolean {
    val hasDirectDependency = dependencies.contains(other.name) || other.dependencies.contains(name)
    val hasConflictingArtifacts =
        outputs.any { output ->
          other.outputs.any { otherOutput -> output.path == otherOutput.path }
        }
    return !hasDirectDependency && !hasConflictingArtifacts
  }
}

/** Represents a step within a flow that performs a specific action. */
data class FlowStep(
    val name: String,
    val taskType: String, // e.g., "assemble", "preprocess", "test"
    val configuration: Map<String, Any> = emptyMap()
)

/** Represents an artifact (file or directory) that is input to or output from a flow. */
data class FlowArtifact(val name: String, val path: File, val type: ArtifactType)

enum class ArtifactType {
  SOURCE_FILE,
  COMPILED_BINARY,
  PROCESSED_ASSET,
  DEPENDENCY_ARCHIVE,
  TEST_RESULT,
  BUILD_REPORT
}
