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

import com.github.c64lib.rbt.flows.adapters.`in`.gradle.dsl.*
import com.github.c64lib.rbt.flows.domain.Flow
import com.github.c64lib.rbt.flows.domain.FlowArtifact
import com.github.c64lib.rbt.flows.domain.FlowStep
import groovy.lang.Closure
import groovy.lang.DelegatesTo

/**
 * DSL builder for creating Flow definitions with processing steps.
 *
 * Flows define processing pipelines with proper dependency tracking and incremental build support.
 * Each flow can contain multiple steps that process files and execute only when inputs change.
 */
open class FlowDslBuilder {
  private val flows = mutableListOf<Flow>()

  fun flow(name: String, configure: FlowBuilder.() -> Unit): FlowDslBuilder {
    val flowBuilder = FlowBuilder(name)
    flowBuilder.configure()
    flows.add(flowBuilder.build())
    return this
  }

  /**
   * Groovy-friendly overload: binds the closure's delegate to [FlowBuilder] so that `flow("name") {
   * ... }` works from a Groovy `build.gradle`. Kotlin callers use the receiver-lambda overload
   * above.
   */
  fun flow(name: String, @DelegatesTo(FlowBuilder::class) configure: Closure<*>): FlowDslBuilder {
    val flowBuilder = bindClosure(FlowBuilder(name), configure)
    flows.add(flowBuilder.build())
    return this
  }

  /**
   * Returns all defined flows, with consumed artifacts that no flow produces marked as source files
   * — such inputs come from the project sources and must not be reported as missing artifact
   * producers during validation.
   */
  internal fun build(): List<Flow> {
    val producedPaths = flows.flatMap { flow -> flow.produces.map { it.path } }.toSet()
    return flows.map { flow ->
      flow.copy(
          consumes =
              flow.consumes.map { artifact ->
                if (artifact.isSourceFile || artifact.path in producedPaths) artifact
                else artifact.copy(isSourceFile = true)
              })
    }
  }
}

/**
 * Binds a Groovy [Closure]'s delegate to [builder] with [Closure.DELEGATE_FIRST] resolution, then
 * calls it. Shared by every Groovy-friendly DSL overload so the binding idiom exists exactly once.
 */
private fun <T> bindClosure(builder: T, closure: Closure<*>): T {
  closure.delegate = builder
  closure.resolveStrategy = Closure.DELEGATE_FIRST
  closure.call(builder)
  return builder
}

/** Builder for individual Flow definitions. */
class FlowBuilder(private val name: String) {
  var description: String = ""
  private val steps = mutableListOf<FlowStep>()
  private val inputs = mutableListOf<FlowArtifact>()
  private val outputs = mutableListOf<FlowArtifact>()
  private val dependencies = mutableListOf<String>()

  /** Adds a dependency on another flow. */
  fun dependsOn(flowName: String) {
    dependencies.add(flowName)
  }

  /** Adds multiple dependencies on other flows. */
  fun dependsOn(vararg flowNames: String) {
    dependencies.addAll(flowNames)
  }

  /** Creates a type-safe Charpad processing step. */
  fun charpadStep(stepName: String, configure: CharpadStepBuilder.() -> Unit) {
    val stepBuilder = CharpadStepBuilder(stepName)
    stepBuilder.configure()
    registerStep(stepName, stepBuilder.build())
  }

  /**
   * Groovy-friendly overload of [charpadStep] that binds the closure's delegate to
   * [CharpadStepBuilder] so that `charpadStep("name") { ... }` works from a Groovy `build.gradle`.
   */
  fun charpadStep(stepName: String, @DelegatesTo(CharpadStepBuilder::class) configure: Closure<*>) {
    val stepBuilder = bindClosure(CharpadStepBuilder(stepName), configure)
    registerStep(stepName, stepBuilder.build())
  }

  /** Creates a type-safe Spritepad processing step. */
  fun spritepadStep(stepName: String, configure: SpritepadStepBuilder.() -> Unit) {
    val stepBuilder = SpritepadStepBuilder(stepName)
    stepBuilder.configure()
    registerStep(stepName, stepBuilder.build())
  }

  /**
   * Groovy-friendly overload of [spritepadStep] that binds the closure's delegate to
   * [SpritepadStepBuilder] so that `spritepadStep("name") { ... }` works from a Groovy
   * `build.gradle`.
   */
  fun spritepadStep(
      stepName: String,
      @DelegatesTo(SpritepadStepBuilder::class) configure: Closure<*>
  ) {
    val stepBuilder = bindClosure(SpritepadStepBuilder(stepName), configure)
    registerStep(stepName, stepBuilder.build())
  }

  /** Creates a type-safe GoatTracker processing step. */
  fun goattrackerStep(stepName: String, configure: GoattrackerStepBuilder.() -> Unit) {
    val stepBuilder = GoattrackerStepBuilder(stepName)
    stepBuilder.configure()
    registerStep(stepName, stepBuilder.build())
  }

  /**
   * Groovy-friendly overload of [goattrackerStep] that binds the closure's delegate to
   * [GoattrackerStepBuilder] so that `goattrackerStep("name") { ... }` works from a Groovy
   * `build.gradle`.
   */
  fun goattrackerStep(
      stepName: String,
      @DelegatesTo(GoattrackerStepBuilder::class) configure: Closure<*>
  ) {
    val stepBuilder = bindClosure(GoattrackerStepBuilder(stepName), configure)
    registerStep(stepName, stepBuilder.build())
  }

  /** Creates a type-safe Assembly processing step. */
  fun assembleStep(stepName: String, configure: AssembleStepBuilder.() -> Unit) {
    val stepBuilder = AssembleStepBuilder(stepName)
    stepBuilder.configure()
    registerStep(stepName, stepBuilder.build())
  }

  /**
   * Groovy-friendly overload of [assembleStep] that binds the closure's delegate to
   * [AssembleStepBuilder] so that `assembleStep("name") { ... }` works from a Groovy
   * `build.gradle`.
   */
  fun assembleStep(
      stepName: String,
      @DelegatesTo(AssembleStepBuilder::class) configure: Closure<*>
  ) {
    val stepBuilder = bindClosure(AssembleStepBuilder(stepName), configure)
    registerStep(stepName, stepBuilder.build())
  }

  /** Creates a type-safe dasm assembly processing step. */
  fun dasmStep(stepName: String, configure: DasmStepBuilder.() -> Unit) {
    val stepBuilder = DasmStepBuilder(stepName)
    stepBuilder.configure()
    registerStep(stepName, stepBuilder.build())
  }

  /**
   * Groovy-friendly overload of [dasmStep] that binds the closure's delegate to [DasmStepBuilder]
   * so that `dasmStep("name") { ... }` works from a Groovy `build.gradle`.
   */
  fun dasmStep(stepName: String, @DelegatesTo(DasmStepBuilder::class) configure: Closure<*>) {
    val stepBuilder = bindClosure(DasmStepBuilder(stepName), configure)
    registerStep(stepName, stepBuilder.build())
  }

  /** Creates a type-safe Image processing step. */
  fun imageStep(stepName: String, configure: ImageStepBuilder.() -> Unit) {
    val stepBuilder = ImageStepBuilder(stepName)
    stepBuilder.configure()
    registerStep(stepName, stepBuilder.build())
  }

  /**
   * Groovy-friendly overload of [imageStep] that binds the closure's delegate to [ImageStepBuilder]
   * so that `imageStep("name") { ... }` works from a Groovy `build.gradle`.
   */
  fun imageStep(stepName: String, @DelegatesTo(ImageStepBuilder::class) configure: Closure<*>) {
    val stepBuilder = bindClosure(ImageStepBuilder(stepName), configure)
    registerStep(stepName, stepBuilder.build())
  }

  /** Creates a type-safe Exomizer compression step. */
  fun exomizerStep(stepName: String, configure: ExomizerStepBuilder.() -> Unit) {
    val stepBuilder = ExomizerStepBuilder(stepName)
    stepBuilder.configure()
    registerStep(stepName, stepBuilder.build())
  }

  /**
   * Groovy-friendly overload of [exomizerStep] that binds the closure's delegate to
   * [ExomizerStepBuilder] so that `exomizerStep("name") { ... }` works from a Groovy
   * `build.gradle`.
   */
  fun exomizerStep(
      stepName: String,
      @DelegatesTo(ExomizerStepBuilder::class) configure: Closure<*>
  ) {
    val stepBuilder = bindClosure(ExomizerStepBuilder(stepName), configure)
    registerStep(stepName, stepBuilder.build())
  }

  /** Creates a type-safe 64spec test step. */
  fun testStep(stepName: String, configure: TestStepBuilder.() -> Unit) {
    val stepBuilder = TestStepBuilder(stepName)
    stepBuilder.configure()
    registerStep(stepName, stepBuilder.build())
  }

  /**
   * Groovy-friendly overload of [testStep] that binds the closure's delegate to [TestStepBuilder]
   * so that `testStep("name") { ... }` works from a Groovy `build.gradle`.
   */
  fun testStep(stepName: String, @DelegatesTo(TestStepBuilder::class) configure: Closure<*>) {
    val stepBuilder = bindClosure(TestStepBuilder(stepName), configure)
    registerStep(stepName, stepBuilder.build())
  }

  /** Creates a type-safe Command execution step. */
  fun commandStep(stepName: String, command: String, configure: CommandStepBuilder.() -> Unit) {
    val stepBuilder = CommandStepBuilder(stepName, command)
    stepBuilder.configure()
    registerStep(stepName, stepBuilder.build())
  }

  /**
   * Groovy-friendly overload of [commandStep] that binds the closure's delegate to
   * [CommandStepBuilder] so that `commandStep("name", "cmd") { ... }` works from a Groovy
   * `build.gradle`.
   */
  fun commandStep(
      stepName: String,
      command: String,
      @DelegatesTo(CommandStepBuilder::class) configure: Closure<*>
  ) {
    val stepBuilder = bindClosure(CommandStepBuilder(stepName, command), configure)
    registerStep(stepName, stepBuilder.build())
  }

  /** Adds a built step and registers its input/output artifacts for dependency tracking. */
  private fun registerStep(stepName: String, step: FlowStep) {
    steps.add(step)
    // Skip empty paths: a filter-only step output (e.g. Charpad `tiles { interleaver { ... } }`)
    // carries an empty primary path whose real files come from the filter sub-outputs. Registering
    // it as an artifact would create a `''` producer path that collides across flows during
    // validation (issue #181). Mirrors the empty-path filtering in FlowTasksGenerator.
    step.inputs
        .filter { it.isNotEmpty() }
        .forEach { input -> inputs.add(FlowArtifact("${stepName}_input_${inputs.size}", input)) }
    step.outputs
        .filter { it.isNotEmpty() }
        .forEach { output ->
          outputs.add(FlowArtifact("${stepName}_output_${outputs.size}", output))
        }
  }

  internal fun build(): Flow =
      Flow(
          name = name,
          steps = steps,
          dependsOn = dependencies,
          produces = outputs,
          consumes = inputs,
          description = description)
}

/** Builder for individual steps. */
class StepBuilder(private val name: String) {
  private val config = mutableMapOf<String, Any>()
  private val inputs = mutableListOf<FlowArtifact>()
  private val outputs = mutableListOf<FlowArtifact>()

  /** Specifies input sources for this step. */
  fun from(path: String) {
    inputs.add(FlowArtifact("${name}_input", path))
    config["inputPath"] = path
  }

  /** Specifies multiple input sources for this step. */
  fun from(vararg paths: String) {
    paths.forEach { path -> inputs.add(FlowArtifact("${name}_input_${inputs.size}", path)) }
    config["inputPaths"] = paths.toList()
  }

  /** Specifies output destination for this step. */
  fun to(path: String) {
    outputs.add(FlowArtifact("${name}_output", path))
    config["outputPath"] = path
  }

  /** Specifies multiple output destinations for this step. */
  fun to(vararg paths: String) {
    paths.forEach { path -> outputs.add(FlowArtifact("${name}_output_${outputs.size}", path)) }
    config["outputPaths"] = paths.toList()
  }

  /** Adds custom configuration for this step. */
  fun configure(key: String, value: Any) {
    config[key] = value
  }

  /** Adds custom configuration using a map. */
  fun configure(configuration: Map<String, Any>) {
    config.putAll(configuration)
  }

  private fun inferTaskType(): String =
      when {
        name.contains("charpad", ignoreCase = true) -> "charpad"
        name.contains("spritepad", ignoreCase = true) -> "spritepad"
        name.contains("goattracker", ignoreCase = true) -> "goattracker"
        name.contains("image", ignoreCase = true) -> "image"
        name.contains("assemble", ignoreCase = true) -> "assemble"
        name.contains("test", ignoreCase = true) -> "test"
        name.contains("dependencies", ignoreCase = true) -> "dependencies"
        else -> "generic"
      }
}
