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
import com.github.c64lib.rbt.flows.domain.steps.CommandStep

/**
 * DSL builder for creating Flow definitions in build.gradle.kts files.
 *
 * Example usage:
 * ```kotlin
 * flows {
 *     flow("preprocessing") {
 *         description = "Process all assets"
 *
 *         charpadStep("charset") {
 *             from("src/assets/charset.ctm")
 *             to("build/assets/charset.chr", "build/assets/charset.map")
 *             compression = CharpadCompression.NONE
 *             exportFormat = CharpadFormat.STANDARD
 *         }
 *
 *         spritepadStep("sprites") {
 *             from("src/assets/sprites.spd")
 *             to("build/assets/sprites.spr")
 *             optimization = SpriteOptimization.SIZE
 *             format = SpriteFormat.MULTICOLOR
 *         }
 *     }
 *
 *     flow("compilation") {
 *         dependsOn("preprocessing")
 *
 *         assembleStep("main") {
 *             from("src/main/main.asm")
 *             to("build/output/main.prg")
 *             cpu = CpuType.MOS6510
 *             generateSymbols = true
 *             optimization = AssemblyOptimization.SPEED
 *             includePaths("build/assets", "lib/c64lib")
 *         }
 *     }
 * }
 * ```
 */
open class FlowDslBuilder {
  private val flows = mutableListOf<Flow>()

  fun flow(name: String, configure: FlowBuilder.() -> Unit): FlowDslBuilder {
    val flowBuilder = FlowBuilder(name)
    flowBuilder.configure()
    flows.add(flowBuilder.build())
    return this
  }

  internal fun build(): List<Flow> = flows.toList()
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
    val step = stepBuilder.build()
    steps.add(step)

    // Add artifacts for dependency tracking
    step.inputs.forEach { input ->
      inputs.add(FlowArtifact("${stepName}_input_${inputs.size}", input))
    }
    step.outputs.forEach { output ->
      outputs.add(FlowArtifact("${stepName}_output_${outputs.size}", output))
    }
  }

  /** Creates a type-safe Spritepad processing step. */
  fun spritepadStep(stepName: String, configure: SpritepadStepBuilder.() -> Unit) {
    val stepBuilder = SpritepadStepBuilder(stepName)
    stepBuilder.configure()
    val step = stepBuilder.build()
    steps.add(step)

    // Add artifacts for dependency tracking
    step.inputs.forEach { input ->
      inputs.add(FlowArtifact("${stepName}_input_${inputs.size}", input))
    }
    step.outputs.forEach { output ->
      outputs.add(FlowArtifact("${stepName}_output_${outputs.size}", output))
    }
  }

  /** Creates a type-safe GoatTracker processing step. */
  fun goattrackerStep(stepName: String, configure: GoattrackerStepBuilder.() -> Unit) {
    val stepBuilder = GoattrackerStepBuilder(stepName)
    stepBuilder.configure()
    val step = stepBuilder.build()
    steps.add(step)

    // Add artifacts for dependency tracking
    step.inputs.forEach { input ->
      inputs.add(FlowArtifact("${stepName}_input_${inputs.size}", input))
    }
    step.outputs.forEach { output ->
      outputs.add(FlowArtifact("${stepName}_output_${outputs.size}", output))
    }
  }

  /** Creates a type-safe Assembly processing step. */
  fun assembleStep(stepName: String, configure: AssembleStepBuilder.() -> Unit) {
    val stepBuilder = AssembleStepBuilder(stepName)
    stepBuilder.configure()
    val step = stepBuilder.build()
    steps.add(step)

    // Add artifacts for dependency tracking
    step.inputs.forEach { input ->
      inputs.add(FlowArtifact("${stepName}_input_${inputs.size}", input))
    }
    step.outputs.forEach { output ->
      outputs.add(FlowArtifact("${stepName}_output_${outputs.size}", output))
    }
  }

  /** Creates a type-safe Image processing step. */
  fun imageStep(stepName: String, configure: ImageStepBuilder.() -> Unit) {
    val stepBuilder = ImageStepBuilder(stepName)
    stepBuilder.configure()
    val step = stepBuilder.build()
    steps.add(step)

    // Add artifacts for dependency tracking
    step.inputs.forEach { input ->
      inputs.add(FlowArtifact("${stepName}_input_${inputs.size}", input))
    }
    step.outputs.forEach { output ->
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

  /** Creates a command step that can execute any CLI command. */
  fun command(commandName: String, configure: CommandStepBuilder.() -> Unit = {}): CommandStep {
    val builder = CommandStepBuilder(name, commandName)
    builder.configure()
    return builder.build()
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

/** Builder for command steps with fluent API. */
class CommandStepBuilder(private val name: String, private val command: String) {
  private val parameters = mutableListOf<String>()
  private val inputs = mutableListOf<String>()
  private val outputs = mutableListOf<String>()

  /** Add a parameter to the command */
  fun param(parameter: String): CommandStepBuilder {
    parameters.add(parameter)
    return this
  }

  /** Add multiple parameters to the command */
  fun params(vararg parameters: String): CommandStepBuilder {
    this.parameters.addAll(parameters)
    return this
  }

  /** Set input paths for this command step */
  fun from(vararg paths: String): CommandStepBuilder {
    inputs.addAll(paths)
    return this
  }

  /** Set output paths for this command step */
  fun to(vararg paths: String): CommandStepBuilder {
    outputs.addAll(paths)
    return this
  }

  internal fun build(): CommandStep {
    var step = CommandStep(name, command, inputs, outputs, parameters)
    return step
  }
}
