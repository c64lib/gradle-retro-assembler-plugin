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
package com.github.c64lib.rbt.flows.adapters.`in`.gradle

import com.github.c64lib.rbt.compilers.kickass.usecase.KickAssembleUseCase
import com.github.c64lib.rbt.flows.adapters.`in`.gradle.tasks.*
import com.github.c64lib.rbt.flows.domain.Flow
import com.github.c64lib.rbt.flows.domain.FlowStep
import com.github.c64lib.rbt.flows.domain.steps.*
import com.github.c64lib.rbt.flows.domain.steps.CommandStep
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection

/** Outbound adapter for Gradle that generates dedicated tasks for each flow step. */
class FlowTasksGenerator(
    private val project: Project,
    private val flows: Collection<Flow>,
    private val kickAssembleUseCase: KickAssembleUseCase? = null
) {
  private val tasksByFlowName = mutableMapOf<String, Task>()
  private val stepTasks = mutableListOf<Task>()

  /** Registers Gradle tasks for all flows and configures dependencies. */
  fun registerTasks() {
    val taskContainer = project.tasks

    // Create dedicated tasks for each step in each flow
    flows.forEach { flow ->
      val flowStepTasks = mutableListOf<Task>()

      flow.steps.forEachIndexed { index, step ->
        val stepTaskName =
            "flow${flow.name.replaceFirstChar { it.uppercaseChar() }}Step${step.name.replaceFirstChar { it.uppercaseChar() }}"

        val stepTask = createStepTask(taskContainer, stepTaskName, step, flow)
        stepTasks.add(stepTask)
        flowStepTasks.add(stepTask)

        // Set up step-level dependencies within the flow
        if (index > 0) {
          stepTask.dependsOn(flowStepTasks[index - 1])
        }
      }

      // Create a flow-level aggregation task
      val flowTaskName = "flow${flow.name.replaceFirstChar { it.uppercaseChar() }}"
      val flowTask =
          taskContainer.create(flowTaskName) { t ->
            t.group = "flows"
            t.description = "Executes all steps in flow '${flow.name}'"
            if (flowStepTasks.isNotEmpty()) {
              t.dependsOn(flowStepTasks.last()) // Depend on the last step
            }
          }
      tasksByFlowName[flow.name] = flowTask
    }

    // Set up flow-level dependencies
    flows.forEach { flow ->
      val flowTask = tasksByFlowName[flow.name] ?: return@forEach
      flow.dependsOn.forEach { depName ->
        tasksByFlowName[depName]?.let { depTask -> flowTask.dependsOn(depTask) }
      }
    }

    // Set up automatic file-based dependencies between step tasks
    setupFileDependencies()
  }

  private fun createStepTask(
      taskContainer: org.gradle.api.tasks.TaskContainer,
      taskName: String,
      step: FlowStep,
      flow: Flow
  ): Task =
      when (step) {
        is CommandStep -> {
          taskContainer.create(taskName, CommandTask::class.java) { task ->
            configureBaseTask(task, step, flow)
          }
        }
        // Handle new processor-specific step types
        is CharpadStep -> {
          taskContainer.create(taskName, CharpadTask::class.java) { task ->
            configureBaseTask(task, step, flow)
            configureOutputFiles(task, step)
          }
        }
        is SpritepadStep -> {
          taskContainer.create(taskName, SpritepadTask::class.java) { task ->
            configureBaseTask(task, step, flow)
            configureOutputFiles(task, step)
          }
        }
        is GoattrackerStep -> {
          taskContainer.create(taskName, GoattrackerTask::class.java) { task ->
            configureBaseTask(task, step, flow)
            configureOutputFiles(task, step)
          }
        }
        is AssembleStep -> {
          taskContainer.create(taskName, AssembleTask::class.java) { task ->
            configureBaseTask(task, step, flow)
            configureOutputFiles(task, step)
          }
        }
        is ImageStep -> {
          taskContainer.create(taskName, ImageTask::class.java) { task ->
            configureBaseTask(task, step, flow)
            configureOutputFiles(task, step)
          }
        }
        else ->
            taskContainer.create(taskName, BaseFlowStepTask::class.java) { task ->
              configureBaseTask(task, step, flow)
            }
      }

  private fun configureBaseTask(task: BaseFlowStepTask, step: FlowStep, flow: Flow) {
    task.group = "flows"
    task.description = "Executes ${step.taskType} step '${step.name}' in flow '${flow.name}'"
    task.flowStep.set(step)

    // Inject dependencies for specific task types
    if (task is AssembleTask && step is AssembleStep) {
      if (kickAssembleUseCase != null) {
        task.kickAssembleUseCase = kickAssembleUseCase

        // Register additional input files during configuration phase
        registerAdditionalInputFiles(task, step)
      } else {
        throw IllegalStateException(
            "KickAssembleUseCase not provided to FlowTasksGenerator but required for AssembleStep '${step.name}'")
      }
    }

    // Configure input files
    if (step.inputs.isNotEmpty()) {
      val inputFiles = step.inputs.map { project.file(it) }.filter { it.exists() }
      if (inputFiles.isNotEmpty()) {
        task.inputFiles.from(inputFiles)
      }

      // If there are input directories, configure them
      val inputDirs = step.inputs.map { project.file(it) }.filter { it.isDirectory }
      if (inputDirs.isNotEmpty()) {
        task.inputDirectory.set(inputDirs.first()) // Use first directory
      }
    }

    // Configure output directory
    if (step.outputs.isNotEmpty()) {
      val outputPath = step.outputs.first()
      val outputFile = project.file(outputPath)
      if (outputFile.isDirectory || outputPath.endsWith("/") || outputPath.endsWith("\\")) {
        task.outputDirectory.set(outputFile)
      } else {
        // If output is a file, use its parent directory
        task.outputDirectory.set(outputFile.parentFile)
      }
    } else {
      // Default output directory
      task.outputDirectory.set(project.file("build/flows/${flow.name}/${step.name}"))
    }
  }

  /**
   * Registers additional input files for AssembleTask during configuration phase. This must be done
   * during configuration, not execution, because Gradle finalizes file collections after the
   * configuration phase.
   */
  private fun registerAdditionalInputFiles(task: AssembleTask, step: AssembleStep) {
    val assemblyConfigMapper = com.github.c64lib.rbt.flows.domain.config.AssemblyConfigMapper()
    val additionalFiles =
        assemblyConfigMapper.discoverAdditionalInputFiles(step.config, project.projectDir)

    if (additionalFiles.isNotEmpty()) {
      task.additionalInputFiles.from(additionalFiles)
    }
  }

  private fun configureOutputFiles(task: Any, step: FlowStep) {
    // Configure output files for tasks that have the outputFiles property
    when (task) {
      is CharpadTask -> task.outputFiles.setFrom(getStepOutputFiles(step))
      is SpritepadTask -> task.outputFiles.setFrom(getStepOutputFiles(step))
      is AssembleTask -> task.outputFiles.setFrom(getStepOutputFiles(step))
      is GoattrackerTask -> task.outputFiles.setFrom(getStepOutputFiles(step))
      is ImageTask -> task.outputFiles.setFrom(getStepOutputFiles(step))
      is CommandTask -> task.outputFiles.setFrom(getStepOutputFiles(step))
    }
  }

  private fun getStepOutputFiles(step: FlowStep): FileCollection {
    val outputFiles = step.outputs.map { project.file(it) }
    return project.files(outputFiles)
  }

  private fun setupFileDependencies() {
    // Create a map of output paths to the tasks that produce them
    val outputToTask = mutableMapOf<String, Task>()

    stepTasks.forEach { task ->
      if (task is BaseFlowStepTask) {
        val step = task.flowStep.get()
        step.outputs.forEach { outputPath -> outputToTask[outputPath] = task }
      }
    }

    // Set up dependencies based on file relationships
    stepTasks.forEach { task ->
      if (task is BaseFlowStepTask) {
        val step = task.flowStep.get()
        step.inputs.forEach { inputPath ->
          outputToTask[inputPath]?.let { producerTask ->
            if (producerTask != task) {
              task.dependsOn(producerTask)
            }
          }
        }
      }
    }
  }
}
