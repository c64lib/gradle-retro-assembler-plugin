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
import org.gradle.api.Project
import org.gradle.api.Task

/** Outbound adapter for Gradle that generates tasks for each flow definition. */
class FlowTasksGenerator(private val project: Project, private val flows: Collection<Flow>) {
  private val tasksByFlowName = mutableMapOf<String, Task>()

  /** Registers Gradle tasks for all flows and configures dependencies. */
  fun registerTasks() {
    val taskContainer = project.tasks

    // Create one task per flow
    flows.forEach { flow ->
      val taskName = "flow${flow.name.replaceFirstChar { it.uppercaseChar() }}"
      val task =
          taskContainer.create(taskName) { t ->
            t.group = "flows"
            t.description = "Executes flow '${flow.name}'"
            // Placeholder action; actual execution will use domain logic
            t.doLast { println("[FlowTasksGenerator] Running flow '${flow.name}'") }
          }
      tasksByFlowName[flow.name] = task
    }

    // Set up task dependencies based on flow dependencies
    flows.forEach { flow ->
      val task = tasksByFlowName[flow.name] ?: return@forEach
      flow.dependsOn.forEach { depName ->
        tasksByFlowName[depName]?.let { depTask -> task.dependsOn(depTask) }
      }
    }
  }
}
