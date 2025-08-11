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

/**
 * Represents a dependency graph of flows that validates dependencies and determines execution
 * order. This class ensures that flows are executed in the correct order while maximizing
 * parallelization opportunities.
 */
class FlowDependencyGraph(private val flows: List<Flow>) {

  private val flowsByName = flows.associateBy { it.name }
  private val dependencyMap = buildDependencyMap()

  /**
   * Validates the dependency graph for circular dependencies and missing flow references.
   * @throws FlowDependencyException if validation fails
   */
  fun validate() {
    validateFlowReferences()
    validateNoCycles()
  }

  /**
   * Returns flows organized into execution levels where all flows in the same level can be executed
   * in parallel.
   */
  fun getExecutionLevels(): List<List<Flow>> {
    validate()

    val levels = mutableListOf<List<Flow>>()
    val completed = mutableSetOf<String>()
    val remaining = flows.map { it.name }.toMutableSet()

    while (remaining.isNotEmpty()) {
      val currentLevel =
          remaining.filter { flowName ->
            val flow = flowsByName[flowName]!!
            flow.dependencies.all { dep -> completed.contains(dep) }
          }

      if (currentLevel.isEmpty()) {
        throw FlowDependencyException(
            "Unable to resolve dependencies - possible circular dependency detected")
      }

      levels.add(currentLevel.map { flowsByName[it]!! })
      completed.addAll(currentLevel)
      remaining.removeAll(currentLevel.toSet())
    }

    return levels
  }

  /** Returns all flows that can run in parallel with the given flow. */
  fun getParallelFlows(flowName: String): List<Flow> {
    val targetFlow =
        flowsByName[flowName] ?: throw FlowDependencyException("Flow not found: $flowName")

    return flows.filter { flow -> flow.name != flowName && targetFlow.canRunInParallelWith(flow) }
  }

  /** Returns the topological order of flows for sequential execution. */
  fun getTopologicalOrder(): List<Flow> {
    return getExecutionLevels().flatten()
  }

  /** Returns all direct and transitive dependencies of the given flow. */
  fun getAllDependencies(flowName: String): Set<String> {
    val flow = flowsByName[flowName] ?: throw FlowDependencyException("Flow not found: $flowName")

    val allDeps = mutableSetOf<String>()
    val toProcess = flow.dependencies.toMutableList()

    while (toProcess.isNotEmpty()) {
      val dep = toProcess.removeAt(0)
      if (allDeps.add(dep)) {
        val depFlow =
            flowsByName[dep] ?: throw FlowDependencyException("Dependency not found: $dep")
        toProcess.addAll(depFlow.dependencies)
      }
    }

    return allDeps
  }

  /** Returns all flows that directly or transitively depend on the given flow. */
  fun getDependents(flowName: String): Set<String> {
    if (!flowsByName.containsKey(flowName)) {
      throw FlowDependencyException("Flow not found: $flowName")
    }

    return flows
        .filter { flow -> getAllDependencies(flow.name).contains(flowName) }
        .map { it.name }
        .toSet()
  }

  private fun buildDependencyMap(): Map<String, Set<String>> {
    return flows.associate { flow -> flow.name to flow.dependencies.toSet() }
  }

  private fun validateFlowReferences() {
    flows.forEach { flow ->
      flow.dependencies.forEach { dep ->
        if (!flowsByName.containsKey(dep)) {
          throw FlowDependencyException("Flow '${flow.name}' depends on unknown flow: '$dep'")
        }
      }
    }
  }

  private fun validateNoCycles() {
    val visited = mutableSetOf<String>()
    val recursionStack = mutableSetOf<String>()

    flows.forEach { flow ->
      if (!visited.contains(flow.name)) {
        if (hasCycleDfs(flow.name, visited, recursionStack)) {
          throw FlowDependencyException("Circular dependency detected involving flow: ${flow.name}")
        }
      }
    }
  }

  private fun hasCycleDfs(
      flowName: String,
      visited: MutableSet<String>,
      recursionStack: MutableSet<String>
  ): Boolean {
    visited.add(flowName)
    recursionStack.add(flowName)

    val flow = flowsByName[flowName]!!
    flow.dependencies.forEach { dep ->
      if (!visited.contains(dep)) {
        if (hasCycleDfs(dep, visited, recursionStack)) {
          return true
        }
      } else if (recursionStack.contains(dep)) {
        return true
      }
    }

    recursionStack.remove(flowName)
    return false
  }
}

/** Exception thrown when there are issues with flow dependencies. */
class FlowDependencyException(message: String) : Exception(message)
