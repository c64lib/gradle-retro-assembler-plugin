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

/** Manages the dependency graph of flows and provides validation and analysis capabilities */
internal class FlowDependencyGraph {
  private val flows = mutableMapOf<String, Flow>()
  private val dependencyEdges = mutableMapOf<String, MutableSet<String>>()
  private val artifactProducers = mutableMapOf<FlowArtifact, String>()
  private val artifactConsumers = mutableMapOf<FlowArtifact, MutableSet<String>>()

  /** Adds a flow to the dependency graph */
  fun addFlow(flow: Flow): FlowDependencyGraph {
    flows[flow.name] = flow

    // Build explicit dependency edges
    dependencyEdges.getOrPut(flow.name) { mutableSetOf() }.addAll(flow.dependsOn)

    // Track artifact producers and consumers
    flow.produces.forEach { artifact ->
      if (artifactProducers.containsKey(artifact)) {
        throw FlowValidationException(
            "Artifact '${artifact.name}' is produced by multiple flows: '${artifactProducers[artifact]}' and '${flow.name}'")
      }
      artifactProducers[artifact] = flow.name
    }

    flow.consumes.forEach { artifact ->
      artifactConsumers.getOrPut(artifact) { mutableSetOf() }.add(flow.name)
    }

    return this
  }

  /** Validates the dependency graph for issues like circular dependencies and missing artifacts */
  fun validate(): FlowValidationResult {
    val issues = mutableListOf<FlowValidationIssue>()

    // Check for circular dependencies
    issues.addAll(detectCircularDependencies())

    // Check for missing artifact producers
    issues.addAll(detectMissingArtifactProducers())

    // Check for orphaned flows (no consumers for their artifacts)
    issues.addAll(detectOrphanedFlows())

    return FlowValidationResult(issues)
  }

  /**
   * Returns the execution order of flows that can run in parallel
   * @return List of flow groups where each group can execute in parallel
   */
  fun getParallelExecutionOrder(): List<List<String>> {
    val validationResult = validate()
    if (validationResult.hasErrors) {
      throw FlowValidationException(
          "Cannot determine execution order: ${validationResult.issues.filter { it.severity == IssueSeverity.ERROR }.joinToString(", ") { it.message }}")
    }

    val executionLevels = mutableListOf<List<String>>()
    val processed = mutableSetOf<String>()
    val allFlowNames = flows.keys.toSet()

    while (processed.size < allFlowNames.size) {
      val currentLevel = mutableListOf<String>()

      // Find flows that have all their dependencies satisfied
      for (flowName in allFlowNames - processed) {
        val flow = flows[flowName]!!
        val allDependenciesMet = getAllDependencies(flowName).all { it in processed }

        if (allDependenciesMet) {
          currentLevel.add(flowName)
        }
      }

      if (currentLevel.isEmpty()) {
        throw FlowValidationException(
            "Unable to resolve execution order - possible circular dependency detected")
      }

      executionLevels.add(currentLevel)
      processed.addAll(currentLevel)
    }

    return executionLevels
  }

  /** Gets all dependencies for a flow (both explicit and implicit through artifacts) */
  private fun getAllDependencies(flowName: String): Set<String> {
    val flow = flows[flowName] ?: return emptySet()
    val allDeps = mutableSetOf<String>()

    // Add explicit dependencies
    allDeps.addAll(flow.dependsOn)

    // Add implicit dependencies through consumed artifacts
    flow.consumes.forEach { artifact ->
      artifactProducers[artifact]?.let { producerFlow ->
        if (producerFlow != flowName) {
          allDeps.add(producerFlow)
        }
      }
    }

    return allDeps
  }

  private fun detectCircularDependencies(): List<FlowValidationIssue> {
    val issues = mutableListOf<FlowValidationIssue>()
    val visiting = mutableSetOf<String>()
    val visited = mutableSetOf<String>()
    val reportedCycles = mutableSetOf<Set<String>>() // Track cycles we've already reported

    fun visitFlow(flowName: String, path: List<String>): Boolean {
      if (flowName in visiting) {
        val cycleStart = path.indexOf(flowName)
        if (cycleStart >= 0) {
          val cycle = path.drop(cycleStart) + flowName
          // Only report this cycle if we haven't seen it before and if we're at the start node
          val cycleSet = cycle.toSet()
          if (cycleSet !in reportedCycles && flowName == cycle.first()) {
            reportedCycles.add(cycleSet)
            issues.add(FlowValidationIssue.CircularDependency(cycle))
          }
        }
        return true
      }

      if (flowName in visited) return false

      visiting.add(flowName)
      val newPath = path + flowName

      val dependencies = getAllDependencies(flowName)
      for (dep in dependencies) {
        visitFlow(dep, newPath)
      }

      visiting.remove(flowName)
      visited.add(flowName)
      return false
    }

    flows.keys.forEach { flowName ->
      if (flowName !in visited) {
        visitFlow(flowName, emptyList())
      }
    }

    return issues
  }

  private fun detectMissingArtifactProducers(): List<FlowValidationIssue> {
    val issues = mutableListOf<FlowValidationIssue>()

    artifactConsumers.forEach { (artifact, consumers) ->
      // Skip validation for source files - they don't need producers
      if (artifact !in artifactProducers && !artifact.isSourceFile) {
        issues.add(FlowValidationIssue.MissingArtifactProducer(artifact, consumers.toList()))
      }
    }

    return issues
  }

  private fun detectOrphanedFlows(): List<FlowValidationIssue> {
    val issues = mutableListOf<FlowValidationIssue>()

    flows.values.forEach { flow ->
      val hasConsumers =
          flow.produces.any { artifact -> artifactConsumers[artifact]?.isNotEmpty() == true }
      val hasExplicitDependents =
          flows.values.any { otherFlow -> otherFlow.dependsOn.contains(flow.name) }

      if (!hasConsumers && !hasExplicitDependents && flow.produces.isNotEmpty()) {
        issues.add(FlowValidationIssue.OrphanedFlow(flow.name, flow.produces))
      }
    }

    return issues
  }

  /** Gets flows that can potentially run in parallel with the given flow */
  fun getParallelCandidates(flowName: String): Set<String> {
    val flow = flows[flowName] ?: return emptySet()
    val flowDependencies = getAllTransitiveDependencies(flowName)
    val flowDependents = getAllTransitiveDependents(flowName)

    return flows.keys
        .filter { otherFlow ->
          otherFlow != flowName &&
              otherFlow !in flowDependencies &&
              otherFlow !in flowDependents &&
              !hasResourceConflict(flowName, otherFlow)
        }
        .toSet()
  }

  private fun getAllTransitiveDependencies(flowName: String): Set<String> {
    val visited = mutableSetOf<String>()
    val toVisit = mutableSetOf(flowName)

    while (toVisit.isNotEmpty()) {
      val current = toVisit.first()
      toVisit.remove(current)

      if (current in visited) continue
      visited.add(current)

      val directDeps = getAllDependencies(current)
      toVisit.addAll(directDeps - visited)
    }

    return visited - flowName
  }

  private fun getAllTransitiveDependents(flowName: String): Set<String> {
    val dependents = mutableSetOf<String>()

    flows.values.forEach { flow ->
      val transitiveDeps = getAllTransitiveDependencies(flow.name)
      if (flowName in transitiveDeps) {
        dependents.add(flow.name)
      }
    }

    return dependents
  }

  private fun getFlowDependents(flowName: String): Set<String> {
    return flows.values
        .filter { flow -> getAllDependencies(flow.name).contains(flowName) }
        .map { it.name }
        .toSet()
  }

  private fun hasResourceConflict(flow1: String, flow2: String): Boolean {
    val flow1Obj = flows[flow1] ?: return false
    val flow2Obj = flows[flow2] ?: return false

    // Check for overlapping input/output files
    val flow1Outputs = flow1Obj.steps.flatMap { it.outputs }.toSet()
    val flow2Outputs = flow2Obj.steps.flatMap { it.outputs }.toSet()
    val flow1Inputs = flow1Obj.steps.flatMap { it.inputs }.toSet()
    val flow2Inputs = flow2Obj.steps.flatMap { it.inputs }.toSet()

    return flow1Outputs.intersect(flow2Outputs).isNotEmpty() ||
        flow1Outputs.intersect(flow2Inputs).isNotEmpty() ||
        flow1Inputs.intersect(flow2Outputs).isNotEmpty()
  }
}

/** Result of flow dependency validation */
data class FlowValidationResult(val issues: List<FlowValidationIssue>) {
  val isValid: Boolean
    get() = issues.isEmpty()
  val hasWarnings: Boolean
    get() = issues.any { it.severity == IssueSeverity.WARNING }
  val hasErrors: Boolean
    get() = issues.any { it.severity == IssueSeverity.ERROR }
}

/** Validation issues that can occur in flow dependency graphs */
sealed class FlowValidationIssue(val message: String, val severity: IssueSeverity) {
  data class CircularDependency(val cycle: List<String>) :
      FlowValidationIssue(
          "Circular dependency detected: ${cycle.joinToString(" -> ")}", IssueSeverity.ERROR)

  data class MissingArtifactProducer(val artifact: FlowArtifact, val consumers: List<String>) :
      FlowValidationIssue(
          "Artifact '${artifact.name}' is consumed by ${consumers.joinToString(", ")} but no flow produces it",
          IssueSeverity.ERROR)

  data class OrphanedFlow(val flowName: String, val unusedArtifacts: List<FlowArtifact>) :
      FlowValidationIssue(
          "Flow '$flowName' produces artifacts that are never consumed: ${unusedArtifacts.joinToString(", ") { it.name }}",
          IssueSeverity.WARNING)
}

enum class IssueSeverity {
  WARNING,
  ERROR
}

/** Exception thrown when flow validation fails */
class FlowValidationException(message: String) : Exception(message)
