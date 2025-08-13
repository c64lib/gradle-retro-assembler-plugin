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
package com.github.c64lib.rbt.flows.adapters.out.gradle

import com.github.c64lib.rbt.flows.domain.Flow
import com.github.c64lib.rbt.flows.domain.FlowService
import com.github.c64lib.rbt.flows.domain.IssueSeverity
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/** Gradle task that bridges to the domain Flow execution logic. */
abstract class FlowExecutionTask : DefaultTask() {
  @Internal lateinit var flow: Flow

  @Internal val flowService: FlowService = FlowService()

  @TaskAction
  fun executeFlow() {
    // Validate the graph before execution
    val validation = flowService.validateFlows(listOf(flow))
    if (validation.hasErrors) {
      throw IllegalStateException(
          "Flow '${flow.name}' validation failed: ${validation.issues.filter { it.severity == IssueSeverity.ERROR }.joinToString { it.message }}")
    }
    // Execute the flow domain logic (placeholder: implement step executors)
    println("[FlowExecutionTask] Executing flow '${flow.name}' with ${'$'}{flow.steps.size} steps")
    // TODO: integrate actual step executors for each FlowStep in flow.steps
  }
}
